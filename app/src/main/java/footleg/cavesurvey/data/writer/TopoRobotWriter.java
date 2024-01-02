/**
 * Copyright (C) 2009-2017 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
 * 
 * This file is part of Cave Converter.
 * 
 * Cave Converter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Cave Converter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Cave Converter.  If not, see <http://www.gnu.org/licenses/>.
 */
package footleg.cavesurvey.data.writer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import footleg.cavesurvey.converter.CaveConverter;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.SeriesLink;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.data.model.SurveySeries.ToStnLRUD;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Writer for TopoRobot file format text data.
 * 
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class TopoRobotWriter {
	private List<ToStnLRUD> terminalLRUDCache;
	private Logger logger;
	
	public TopoRobotWriter( Logger logger ) {
		super();
		this.logger = logger;
	}
		
	/**
	 * Generates TopoRobot format data from a cave survey
	 * 
	 * @param surveyData The cave survey model to generate TopoRobot data for
	 * @param defaultDate The date to put in the data header
	 * @param outputSplays Flag to switch on/off whether splay legs are included in the output
	 * @return Text lines of TopoRobot format data
	 */
	public List<String> generateToporobotData( CaveSurvey surveyData, Date defaultDate, boolean outputSplays ) {
		List<String> outputData = new ArrayList<String>();
		terminalLRUDCache = new ArrayList<ToStnLRUD>();
		
		/**
		 * Pre-process series data to make it suitable for toporobot export. Toporobot only supports
		 * numeric station names, and linear chains of survey legs (i.e. No branching in a survey
		 * series). To convert the data to a suitable structure from a hierarchical nested set of
		 * potentially branched survey series the following processes are applied to the data.
		 * 	1) Create a new list of survey series where no series contains any inner series. All
		 *     series are named using the fully expanded original hierarchical name for the series.
		 *     All links between series are stored in one of the series involved in the link rather
		 *     than at a higher level outside the series. 
		 * 	2) All branches in the resulting series are removed by spliting each series containing
		 *     a branch at the junction.
		 *  3) Merge series which are joined end to end into a single series to minimise the number
		 *     of linear series in the survey.
		 * 	4) Split series which are not linked by their start stations at the linking station so 
		 *     that all series links use only the first station to link to another series.
		 *     
		 *  The result of this is that the naming of series will no longer reflect the original
		 *  naming of series form the input survey, and stations will be renumbered.
		 *  
		 *  TODO This writer has some issues. Some surveys with multiple loops can get split into 
		 *  two parts which are not linked to each other. Some links have comments indicating the 
		 *  stations are id -1 and some series report they are not linked to any others.
		 */

		logger.logMessage("Generating Toporobot format data...");

		//Convert all series including nested series into one big series with full qualified names in links cache
		SurveySeries rawData = convertToSingleSeries(surveyData, outputSplays);

		//Convert this to linear series
		SurveySeries processedSurveyData = UtilityFunctions.convertToLinearSeries( rawData, logger );
		
		List<int[]> preferredLinkSeriesCache = new ArrayList<int[]>();

		//Generate formatted date and time strings for headers
		String dateFormat = "yy/MM/dd HH:mm:ss";
		String dateTime = UtilityFunctions.dateToString(defaultDate, dateFormat);
		String todaysDate = dateTime.substring(6, 8) + '/' + dateTime.substring(3, 5) + '/' + dateTime.substring(0, 2);
		
		//Create file header
		outputData.add( "    -6     1   1   1   1 Cave Name");
		outputData.add( "    -5     1   1   1   1        0.00        0.00        0.00     1     0");
		outputData.add( "    -4     1   1   1   1 " + dateTime + "  CaveConverter");
		outputData.add( "    -3     1   1   1   1");
		outputData.add( "    -2     1   1   1   1 " + todaysDate + "  Converted     Data          0    0.00   0   1");
		outputData.add( "    -1     1   1   1   1  360.00  360.00    0.05    1.00    1.00  100.00    0.00");

		//Generate data lines from survey data
		List<SeriesLink> links = processedSurveyData.getLinks();
		ListIterator<SurveySeries> seriesItr = processedSurveyData.getInnerSeriesList().listIterator();
		int seriesNo = 0; //1 based index of stations, for writing directly into file
		while ( seriesItr.hasNext() ) {
			SurveySeries series = seriesItr.next();
			seriesNo++;
			
			//Generate series header
			outputData.add( CaveConverter.padNumber(seriesNo,6) +  "    -2   1   1   1 Series " + series.getSeriesName() );
			
			//Initialise links data line
			String linkLine = CaveConverter.padNumber(seriesNo,6) +  "    -1   1   1   1";
			//Initialise link from ourself to ourself, as that is how toborobot represents unlinked series
			int linkSeries1 = seriesNo;
			int linkStn1 = 0;
			int linkSeries2 = seriesNo;
			int linkStn2 = series.legCount();
			boolean foundFirstLink = false;
			
			//Now find the links to any series this one starts or ends at a connection with
			
			//Loop through links looking for links to start stn or end stn of this series
			for (int i = 0; i < links.size(); i++ ) {
				//Check whether this link is to this series
				if ( series.getSeriesName() == links.get(i).getSeries1() ) {
					//Found link to our series, check if it is to start or end of this series
					int linkType = 0; //0=not to this series, 1=to start of this series, 2=to end of this series
					if ( series.getLegRaw(0).getFromStn().getId() == links.get(i).getStn1().getId() ) {
						linkType = 1;
					}
					else if ( series.getLegRaw( series.legCount() - 1 ).getToStn().getId() == links.get(i).getStn1().getId() ) {
						linkType = 2;
					}
					if ( linkType > 0 ) {
						//Found a link to this series, so determine the index of the linked series
						int linkSeriesIdx = -1;
						int j = 0;
						String series2Name = links.get(i).getSeries2();
						while ( linkSeriesIdx < 0 && j < processedSurveyData.getInnerSeriesList().size() ) {
							if ( processedSurveyData.getInnerSeriesList().get(j).getSeriesName() == series2Name ) {
								linkSeriesIdx = j;
							}
							j++;
						}
						if ( linkSeriesIdx >= 0 ) {
							//Found the index of the parent series, now need to find the index of the link station
							//Stn number should be 1 based index of station in series, 
							//not the actual value stored in that series
							int idx = 0;
							int linkStnIdx = -1;
							int linkStnId = links.get(i).getStn2().getId();

							/*
							 * Cache series index as preferred for this stn number if not already found.
							 * This is so that where multiple series all branch off from the same point,
							 * all the links are to the same one of these series. Otherwise we can end
							 * up with multiple sets of series linked to the others in the group, but
							 * the groups not connected to each other. This preferred series cache
							 * ensures that the first series found is used for all other links to
							 * series starting at this station.
							 */
							int preferredLinkSeries = 0 ;
							//Look for this stn num in cache
							for ( int k = 0; k < preferredLinkSeriesCache.size(); k++ ) {
								if ( preferredLinkSeriesCache.get(k)[0] == linkStnId ) {
									//Found, so get preferred series from cache
									preferredLinkSeries = preferredLinkSeriesCache.get(k)[1];
									k = preferredLinkSeriesCache.size();
								}
							}
							//If not found in cache then cache this series as the preferred one
							if ( preferredLinkSeries == 0 ) {
								int[] rec = new int[2];
								rec[0] = linkStnId;
								rec[1] = linkSeriesIdx;
								preferredLinkSeriesCache.add(rec);
							}
							
							//Get link series
							SurveySeries linkSeries;
							if ( ( preferredLinkSeries > 0 ) && ( preferredLinkSeries != seriesNo ) ) {
								//Use preferred series instead of the given link
								linkSeries = processedSurveyData.getInnerSeries(preferredLinkSeries);
								linkSeriesIdx = preferredLinkSeries;
							}
							else {
								linkSeries = processedSurveyData.getInnerSeries(linkSeriesIdx);
							}
							
							
							while ( linkStnIdx == -1) {
								//Check if stn name matches our link name
								if ( idx >= linkSeries.legCount() ) {
									//Check if link is to final stn
									if ( linkSeries.getLegRaw(idx-1).getToStn().getId() == linkStnId ) {
										//Found match
										linkStnIdx = idx;
									}
									else {
										//No matches, set value to cause loop to exit
										linkStnIdx = -2;
									}
								}
								else if ( linkSeries.getLegRaw(idx).getFromStn().getId() == linkStnId ) {
									//Found match
									linkStnIdx = idx;
								}
								else {
									//Keep looking
									idx++;
								}
							}

							//We have now identified the series number and station number for the link
							int linkSeriesNum = linkSeriesIdx + 1;
							//Set link to start or end station
							if ( linkType == 1 ) {
								linkSeries1 = linkSeriesNum;
								linkStn1 = linkStnIdx;
								if ( foundFirstLink == false ) {
									//This is first link for this series, so set flag
									foundFirstLink = true;
								}
								else {
									//This is 2nd link to this series, so stop looking for more
									break;
								}
							}
							else {
								linkSeries2 = linkSeriesNum;
								linkStn2 = linkStnIdx;
								if ( foundFirstLink == false ) {
									//This is first link for this series, so set flag
									foundFirstLink = true;
								}
								else {
									//This is 2nd link to this series, so stop looking for more
									break;
								}
							}
						}
					}
				}
			}

			//Add link details to data line: linked series no, linked station index, this series number, first station (0)
			linkLine += CaveConverter.padNumber(linkSeries1,8) + CaveConverter.padNumber(linkStn1,8);
			linkLine += CaveConverter.padNumber(linkSeries2,8) + CaveConverter.padNumber(linkStn2,8);

			//Finish line
			linkLine += CaveConverter.padNumber(series.legCount(),8) + "       3       0";
			outputData.add( linkLine ); 

			//First line is LRUD for first station
			String blankLRUD = "    0.00    0.00    0.00    0.00";
			String lrud = blankLRUD;
			if ( series.legCount() > 0 ) {
				lrud = lrudForLeg( series.getLegCorrected( 0 ) );
			}
			outputData.add( CaveConverter.padNumber(seriesNo,6) +  CaveConverter.padNumber(0,6) +  "   1   1   1" +
				"    0.00    0.00    0.00" + lrud); 
			//Loop through legs in series to write out remaining stations
			for (int legIdx = 0; legIdx < series.legCount(); legIdx++ ) {
				SurveyLeg leg = series.getLegCorrected(legIdx);
				
				//Use lrud from next leg as toporobot puts lrud on 'to' station of each leg
				lrud = blankLRUD;
				if ( series.legCount() > legIdx + 1 ) {
					lrud = lrudForLeg( series.getLegCorrected( legIdx + 1 ) );
				}
				else if ( series.legCount() == legIdx + 1 ) {
					//Last leg in chain, so check for cached LRUD data for toStn
					ListIterator<ToStnLRUD> legIter = terminalLRUDCache.listIterator();
					while (legIter.hasNext() ) {
						ToStnLRUD toStnLrudData = legIter.next();
						
						if ( toStnLrudData.getFromStn().getName().compareTo( leg.getToStn().getName() ) == 0 ) {
							//Copy terminal station LRUD data to temp leg to convert to data string
							SurveyLeg tempLeg = new SurveyLeg();
							tempLeg.setLeft( toStnLrudData.getLeft(), LengthUnit.Metres );
							tempLeg.setRight( toStnLrudData.getRight(), LengthUnit.Metres );
							tempLeg.setUp( toStnLrudData.getUp(), LengthUnit.Metres );
							tempLeg.setDown( toStnLrudData.getDown(), LengthUnit.Metres );
							lrud = lrudForLeg( tempLeg );
							//Remove from iterator and exit loop
							legIter.remove();
							break;
						}
					}
				}
				
				String dataLine = CaveConverter.padNumber(seriesNo,6) +  CaveConverter.padNumber( legIdx+1, 6 ) +
					"   1   1   1" +
					CaveConverter.padNumber( leg.getLength(LengthUnit.Metres), 2, 8 ) + 
					CaveConverter.padNumber( leg.getCompass(BearingUnit.Degrees), 2, 8 ) + 
					CaveConverter.padNumber( leg.getClino(GradientUnit.Degrees), 2, 8 ) + lrud;
				outputData.add( dataLine ); 
			}
		}
		
		return outputData;
	}
	
	/**
	 * Generate string representation of LRUD data for a survey leg
	 * @param leg Input leg
	 * @return String representation of LRUD data
	 */
	private String lrudForLeg(SurveyLeg leg) {
		double down = leg.getDown(LengthUnit.Metres);
		if ( down < 0 ) {
			down = 0.0;
		}
		return CaveConverter.padNumber(leg.getLeft(LengthUnit.Metres),2,8) + CaveConverter.padNumber(leg.getRight(LengthUnit.Metres),2,8) + 
				CaveConverter.padNumber(leg.getUp(LengthUnit.Metres),2,8) + CaveConverter.padNumber(down,2,8);
	}
	
	private void addLegsToMasterSeries( SurveySeries series, SurveySeries rawData, String parentSeriesPrefix, 
			List<List<String>> linkCache, boolean outputSplays ) {
		//Copy legs from this series into master series, translating using links cache when matching
		for ( int j = 0; j < series.legCount(); j++ ) {
			SurveyLeg leg = series.getLegCorrected(j);
			//Ignore splay legs if output splays option is set to false   
			if ( ( ( outputSplays == false ) && leg.isSplay() ) == false ) {
				//Get fully expanded station names for leg
				String fromStn = parentSeriesPrefix + "." + series.getSeriesName() + "." + 
						leg.getFromStn().getName();
				String toStn = parentSeriesPrefix + "." + series.getSeriesName() + "." + 
						leg.getToStn().getName();
				/*
				 * Check for these stn names in links cache and if found, use name of first equivalent station 
				 * so no links are required because all stations will be linked via a common named station
				 */
				for ( int k = 0; k < linkCache.size(); k++ ) {
					List<String> rec = linkCache.get(k);
					for ( int m = 1; m < rec.size(); m++ ) {
						if ( rec.get(m).compareToIgnoreCase( fromStn ) == 0 ) {
							fromStn = rec.get(0);
						}
						else if ( rec.get(m).compareToIgnoreCase( toStn ) == 0 ) {
							toStn = rec.get(0);
						}
					}
				}
	
				//Clone leg and create new stn numbers from stn full names
				SurveyLeg newLeg = leg.clone();
				newLeg.setFromStn( UtilityFunctions.createStationFromNameForSeries(fromStn, rawData) );
				newLeg.setToStn(UtilityFunctions.createStationFromNameForSeries(toStn, rawData) );
				//Add to master raw survey series
				rawData.addLeg(newLeg);
			}
		}
		//Cache LRUD data from final stations in a series
		for ( int j = 0; j < series.getToStnLRUDs().size(); j++ ) {
			ToStnLRUD toStnLrudData = series.getToStnLRUDs().get(j);
			//Get fully expanded station name for from stn and set as stn name in cache
			String fromStn = parentSeriesPrefix + "." + series.getSeriesName() + "." + 
					toStnLrudData.getFromStn().getName();
			toStnLrudData.getFromStn().setName(fromStn);
			terminalLRUDCache.add(toStnLrudData);		
		}
	}
	
	private void addLinksToCache(SurveySeries series, String parentSeriesPrefix, List<List<String>> linkCache ){
		String seriesFullName = parentSeriesPrefix + "." + series.getSeriesName();
		List<SeriesLink> links = series.getLinks();
		for ( int j = 0; j < links.size(); j++ ) {
			//Get fully expanded linked station pair
			String linkSeries1 = "";
			if ( links.get(j).getSeries1().length() > 0 ) {
				linkSeries1 = "." + links.get(j).getSeries1();
			}
			String stn1 = seriesFullName + linkSeries1 + "." + 
				links.get(j).getStn1().getName();
			String linkSeries2 = "";
			if ( links.get(j).getSeries2().length() > 0 ) {
				linkSeries2 = "." + links.get(j).getSeries2();
			}
			String stn2 = seriesFullName + linkSeries2 + "." + 
					links.get(j).getStn2().getName();
			/**
			 * Need one station name to always take precedence in all links
			 * so check if either of these stations is already in the cache,
			 * and if so add other station to the existing entry
			 */
			boolean newRec = true;
			for ( int k = 0; k < linkCache.size(); k++ ) {
				List<String> rec = linkCache.get(k);
				for ( int m = 0; m < rec.size(); m++ ) {
					if ( rec.get(m).equalsIgnoreCase( stn1 ) ) {
						newRec = false;
						//Add other stn to this list
						rec.add( stn2 );
						m = rec.size();
					}
					else if ( rec.get(m).equalsIgnoreCase( stn2 ) ) {
						newRec = false;
						//Add other stn to this list
						rec.add( stn1 );
						m = rec.size();
					}
				}
			}
			if ( newRec ) {
				//Cache this link as a new record
				List<String> rec = new ArrayList<String>();
				rec.add(stn1);
				rec.add(stn2);
				linkCache.add(rec);
			}
		}
	}
	
	private void processSeriesLinks(SurveySeries series, String masterSeriesNamePrefix, List<List<String>> linkCache){
		//Process links for this series
		logger.logMessage("Processing links from series: " + series.getSeriesName() );
		addLinksToCache( series, masterSeriesNamePrefix, linkCache );
		
		//Process any inner series
		String seriesNamePrefix = masterSeriesNamePrefix + "." + series.getSeriesName();
		ListIterator<SurveySeries> iterator = series.getInnerSeriesList().listIterator();
		while (iterator.hasNext() ) {
			SurveySeries innerSeries = iterator.next();
			
			//Process any inner series
			processSeriesLinks(innerSeries, seriesNamePrefix, linkCache);
		}
	}
	
	private void processSeriesLegs(SurveySeries series, SurveySeries masterSeries, String masterSeriesNamePrefix, 
			List<List<String>> linkCache, boolean outputSplays ){
		//Process legs from this series
		logger.logMessage("Processing legs from series: " + series.getSeriesName() );
		addLegsToMasterSeries( series, masterSeries, masterSeriesNamePrefix, linkCache, outputSplays );
		
		//Process any inner series
		String seriesNamePrefix = masterSeriesNamePrefix + "." + series.getSeriesName();
		ListIterator<SurveySeries> iterator = series.getInnerSeriesList().listIterator();
		while (iterator.hasNext() ) {
			SurveySeries innerSeries = iterator.next();
			
			//Process any inner series
			processSeriesLegs(innerSeries, masterSeries, seriesNamePrefix, linkCache, outputSplays);
		}
	}
	
	/**
	 * Creates one master survey series from a cave survey
	 * @param surveyData The cave survey model to convert to a single series
	 * @param outputSplays Indicates whether to include splay legs in the file or not
	 * @return The single survey series representing the cave survey
	 */
	public SurveySeries convertToSingleSeries( CaveSurvey surveyData, boolean outputSplays ) {
		logger.logMessage("Flattening survey series heirarchy...");

		SurveySeries rawData = new SurveySeries("root");
		List<List<String>> linkCache = new ArrayList<List<String>>();
		String seriesNamePrefix = rawData.getSeriesName();
		
		//Create cache of links from all series
		ListIterator<SurveySeries> iterator = surveyData.listIterator();
		while (iterator.hasNext() ) {
			SurveySeries series = iterator.next();
			
			//Process links from this series
			processSeriesLinks(series, seriesNamePrefix, linkCache);
		}
		
		//Process legs from all series once all series links have been handled
		ListIterator<SurveySeries> iterator2 = surveyData.listIterator();
		while (iterator2.hasNext() ) {
			SurveySeries series = iterator2.next();
			
			//Process legs from this series
			processSeriesLegs(series, rawData, seriesNamePrefix, linkCache, outputSplays);
		}
		
		/*
		 * At this stage the legs are all in one master series, and the station names have been set so
		 * that any links are implicit due to having mapped all equivalent stations to the same station names.
		 */
		
		//Debug dump links cache
		for ( int k = 0; k < linkCache.size(); k++ ) {
			List<String> rec = linkCache.get(k);
			String msg = "Linked";
			for ( int m = 0; m < rec.size(); m++ ) {
				msg += ":" + rec.get(m);
			}
			logger.logMessage(msg);
		}
		
		return rawData;
	}

}
