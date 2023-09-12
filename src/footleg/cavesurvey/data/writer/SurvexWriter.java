/**
 * Copyright (C) 2018 Paul Fretwell - https://github.com/Footleg/caveconverter
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
import footleg.cavesurvey.data.model.SurveyStation;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Writer for Survex file format text data.
 * 
 * @author      Footleg
 * @version     2018.04.15                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 * @to.do
 * TODO Handle nosurvey legs (currently they are converted to equates in error)
 * TODO Output entrance flags
 * TODO Output fix flags
 * TODO Output calibration comments fields
 * TODO Output team fields
 * TODO Output instrument fields
 * TODO Output units
 * TODO Indent block contents including nested blocks
 * TODO Support option to output multiple files using includes and equates in master file and one series per file in child files
 */
public class SurvexWriter {
	private Logger logger;
	
	public SurvexWriter( Logger logger ) {
		super();
		this.logger = logger;
	}

	/**
	 * Supported splay formats for output
	 */
	public static enum SplayFormats {
		None,
		Flagged,
		Anonymous
	}

	/**
	 * Generates Survex format data from a cave survey
	 * 
	 * @param surveyData The cave survey model to generate Survex data for
	 * @param splaysOutput Indicate format of splay legs included in the output. 
	 * @return Text lines of Survex format data
	 */
	public List<String> generateSurvexData( CaveSurvey surveyData, SplayFormats splaysOutput ) {
		List<String> outputData = new ArrayList<String>();
		
		logger.logMessage("Generating Survex format data...");
		
		//Create dummy parent series to pass in neutral calibration settings for top level series
		SurveySeries parentSeries = new SurveySeries("parent");
		
		//Loop through all series
		ListIterator<SurveySeries> seriesIterator = surveyData.listIterator();
		while ( seriesIterator.hasNext() ) {
			SurveySeries series = seriesIterator.next();

			outputData.addAll( generateSurvexDataSeries( series, parentSeries, splaysOutput ) );
		}

		return outputData;
	}
	
	/**
	 * Generates Survex format data for a survey series
	 * 
	 * @param series Survey series to generate Survex format data from
	 * @param parentSeries Survey series which is the parent of the series being passed in (used for calibrations)
	 * @return Text lines for a Survex format series data block
	 */
	private List<String> generateSurvexDataSeries( SurveySeries series, SurveySeries parentSeries, SplayFormats splaysOutput ) {
		List<String> outputData = new ArrayList<String>();
		List<String> legsData = new ArrayList<String>();
		List<String> fixedStnsData = new ArrayList<String>();
		
		//List of passage data blocks. Each block is a child list of the station name and the LRUD data line.
		List<List<String[]>> passageData = new ArrayList<List<String[]>>();

		boolean duplicateFlagOn = false;
		boolean surfaceFlagOn = false;
		boolean splayFlagOn = false;
		boolean nosurveyFlagOn = false;
		int splayNameSequence = 0;
		
		//Start series block
		outputData.add( "*BEGIN " + substIllegalNameChars( series.getSeriesName() ) );
		
		//Write comment if present
		if ( series.getComment().length() > 0 ) {
			String[] dataItems = UtilityFunctions.parseTripComment( series.getComment() );
			for (String line : dataItems) {
				outputData.add( ";" + line );
			}
			outputData.add( "" );
		}
		
		//Write date if present
		if ( series.getSurveyDate() != null ) {
			outputData.add( "*DATE " + UtilityFunctions.dateToString( series.getSurveyDate(), UtilityFunctions.SURVEXDATE_FORMAT)  );
		}
		
		//Write calibration lines if different to parent series
		//TODO Output Units for series and calibrations in these units (at same time as measurements in series units)
		if ( series.getDeclination() != parentSeries.getDeclination() ) {
			outputData.add( "*CALIBRATE declination " + CaveConverter.decimalNumberStringEnglish( series.getDeclination() ) );
		}
		if ( series.getTapeCalibration(LengthUnit.Metres) != parentSeries.getTapeCalibration(LengthUnit.Metres) ) {
			outputData.add( "*CALIBRATE tape " + CaveConverter.padNumber( series.getTapeCalibration(LengthUnit.Metres), 2, 0 ) );
		}
		if ( series.getCompassCalibration(BearingUnit.Degrees) != parentSeries.getCompassCalibration(BearingUnit.Degrees) ) {
			outputData.add( "*CALIBRATE compass " + CaveConverter.decimalNumberStringEnglish( series.getCompassCalibration(BearingUnit.Degrees) ) );
		}
		if ( series.getClinoCalibration(GradientUnit.Degrees) != parentSeries.getClinoCalibration(GradientUnit.Degrees) ) {
			outputData.add( "*CALIBRATE clino " + CaveConverter.decimalNumberStringEnglish( series.getClinoCalibration(GradientUnit.Degrees) ) );
		}
		outputData.add( "" );
		
		//Alias - for stations if splays output format is anonymous
		if ( splaysOutput == SplayFormats.Anonymous ) {
			outputData.add( "*alias station - .." );
			outputData.add( "" );
		}
		

		//Add equates to output
		ListIterator<SeriesLink> linksIterator = series.getLinks().listIterator();
		while ( linksIterator.hasNext() ) {
			SeriesLink link = linksIterator.next();
			String equate = "*EQUATE ";

			String series1Prefix = "";
			String series2Prefix = "";
			if ( link.getSeries1().length() > 0 ) {
				series1Prefix = substIllegalNameChars( link.getSeries1() ) + ".";
			}
			if ( link.getSeries2().length() > 0 ) {
				series2Prefix = substIllegalNameChars( link.getSeries2() ) + ".";
			}
			equate += series1Prefix + substIllegalNameChars( link.getStn1().getName() ) + " " +
					  series2Prefix + substIllegalNameChars( link.getStn2().getName() );

			outputData.add( equate );
		}
		outputData.add( "");
		
		//Write data format block if present and different to parent series
		boolean skipDataOrderLine = false;
		if ( series.hasDataOrder() ) {
			if ( parentSeries.hasDataOrder() ) {
				if ( UtilityFunctions.compareStringLists( series.getDataOrder(), parentSeries.getDataOrder() ) ) {
					if ( series.getDataOrder2().size() == 0 && parentSeries.getDataOrder2().size() == 0 ) {
						//Identical to parent and neither is a mix of data formats, so skip redeclaring data order in this series
						skipDataOrderLine = true;
					}
				}
			}
		}
		
		//Add data order header here if no legs in series, so it can apply to child series.
		//(Series with legs will have data order headers written as needed in the leg processing loop later)
		if ( ( skipDataOrderLine == false ) && ( series.legCount() == 0 ) && ( series.hasDataOrder() ) ) {
			outputData.add( "*" + UtilityFunctions.dataFormatLineForSeries( series, false ) );
		}

		//Loop through the series legs and LRUD data writing details of each leg found
		int psuedoLegCount = series.legCount() + series.getToStnLRUDs().size();
		boolean lastLegWasDiving = false;
		for ( int psuedoLegIdx = 0; psuedoLegIdx < psuedoLegCount; psuedoLegIdx++ ) {
			//Determine if real leg of psuedo leg representing LRUD data on a toStn
			SurveyLeg leg;
			if ( psuedoLegIdx < series.legCount() ) {
				//Real leg
				leg = series.getLegRaw(psuedoLegIdx);
				//If no data order in series, then skip data order headers unless diving data
				if ( ( series.hasDataOrder() == false ) && ( leg.isDiving() == false ) ) {
					//No data order in series, so skip it
					skipDataOrderLine = true;
				}
			}
			else {
				//Need to generate a psudeo leg to hold the LRUD data for this toStn
				leg = new SurveyLeg();
				ToStnLRUD lrud = series.getToStnLRUDs().get( psuedoLegIdx - series.legCount() );
				leg.setFromStn( lrud.getFromStn() );
				leg.setLeft( lrud.getLeft(), LengthUnit.Metres );
				leg.setRight( lrud.getRight(), LengthUnit.Metres );
				leg.setUp( lrud.getUp(), LengthUnit.Metres );
				leg.setDown( lrud.getDown(), LengthUnit.Metres );
			}
			
			String fromStn = leg.getFromStn().getName();
			String fromStnVal = substIllegalNameChars( fromStn );
			
			/* Check for valid leg. We do not want to write output for psuedo legs in a series which
			 * contain only a 'from station' with LRUD data on it,
			 * so only write leg data if a 'to station' exists or leg is a splay
			 */
			if ( ( leg.isSplay() ) || ( leg.getToStn() != null ) ) {
				String toStn;
				if ( leg.getToStn() == null ) {
					//Splay leg without a 'to' station. Create a station name from the leg from station
					toStn = leg.getFromStn().getName() + "-" + splayNameSequence++;
				}
				else {
					toStn = leg.getToStn().getName();
				}
				String toStnVal = substIllegalNameChars( toStn );
				
				if ( leg.isNosurvey() || leg.getLength( LengthUnit.Metres ) > 0 ) {
					//Determine whether to write data type header
					if ( ( leg.isNosurvey() ) && nosurveyFlagOn == false ) {
						legsData.add( "*data nosurvey from to");
						nosurveyFlagOn = true;
					}
					else if ( ( leg.isNosurvey() == false ) && nosurveyFlagOn ) {
						legsData.add( "*" + UtilityFunctions.dataFormatLineForSeries( series, leg.isDiving() ) );
						nosurveyFlagOn = false;
					}

					//Determine if any flags need setting
					String flagsSetting = "";
					//Determine whether to set or clear duplicate flag
					if ( ( leg.isDuplicate() ) && duplicateFlagOn == false ) {
						flagsSetting += " DUPLICATE";
						duplicateFlagOn = true;
					}
					else if ( ( leg.isDuplicate() == false ) && duplicateFlagOn ) {
						flagsSetting += " NOT DUPLICATE";
						duplicateFlagOn = false;
					}
					
					//Determine whether to set or clear surface flag
					if ( ( leg.isSurface() ) && surfaceFlagOn == false ) {
						flagsSetting += " SURFACE";
						surfaceFlagOn = true;
					}
					else if ( ( leg.isSurface() == false ) && surfaceFlagOn ) {
						flagsSetting += " NOT SURFACE";
						surfaceFlagOn = false;
					}
					if ( splaysOutput == SplayFormats.Flagged ) {
						//Determine whether to set or clear splays flag
						if ( ( leg.isSplay() ) && splayFlagOn == false ) {
							flagsSetting += " SPLAY";
							splayFlagOn = true;
						}
						else if ( ( leg.isSplay() == false ) && splayFlagOn ) {
							flagsSetting += " NOT SPLAY";
							splayFlagOn = false;
						}
					}
					//Write flags line if any flags changed
					if ( flagsSetting.length() > 0 ) {
						legsData.add( "*FLAGS" + flagsSetting);
					}
					if ( ( ( splaysOutput == SplayFormats.None ) && leg.isSplay() ) == false ) {
						//Leg or a splay to be written
						
						//Write data order line if first leg, or different format to last leg when secondary data order
						boolean needDataOrderHeader = false;
						if ( psuedoLegIdx == 0 ) {
							//First leg, so write data order line regardless, and set flag for type
							if ( skipDataOrderLine == false ) {
								needDataOrderHeader = true;
							}
						}
						else {
							//Only write data order line if leg type has changed since last leg
							if ( series.getDataOrder2().size() > 0 ) {
								if ( lastLegWasDiving != leg.isDiving() ) {
									needDataOrderHeader = true;
								}
							}
						}
						//Set flag ready for next leg	
						lastLegWasDiving = leg.isDiving();
						//Output data order header line if needed before this leg
						if ( needDataOrderHeader ) {
							legsData.add( "*" + UtilityFunctions.dataFormatLineForSeries( series, leg.isDiving() ) );
						}
						//Determine data order to write out
						List<String> dataOrder = series.getDataOrder();
						if ( dataOrder.size() == 0 ) {
							//No data order specified, so use Survex default order
							dataOrder.add( CaveConverter.DATA_ORDER_CAT_FROMSTN );
							dataOrder.add( CaveConverter.DATA_ORDER_CAT_TOSTN );
							dataOrder.add( CaveConverter.DATA_ORDER_CAT_LENGTH );
							dataOrder.add( CaveConverter.DATA_ORDER_CAT_BEARING );
							dataOrder.add( CaveConverter.DATA_ORDER_CAT_CLINO );
						}
						else {
							List<String> dataOrder2 = series.getDataOrder2();
							
							//Determine if correct data order item for normal or diving in case of mixed data series
							if ( dataOrder2.size() > 0 ) {
								if ( UtilityFunctions.dataOrderIsDiving( dataOrder ) != leg.isDiving() ) {
									//Primary data order does not match required type, so use secondary order
									dataOrder = dataOrder2;
								}
							}
						}
						
						String legLine = "";
						
						//Write data in data order
						for ( int i = 0; i < dataOrder.size(); i++ ) {
							String item = dataOrder.get(i);
							if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_FROMSTN ) == 0 ) {
								legLine += fromStnVal + "\t";
							}
							else if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_TOSTN ) == 0 ) {
								if ( ( splaysOutput == SplayFormats.Anonymous ) && leg.isSplay() ) {
									//Anonymous splay, so no name for toStn
									legLine += "-\t";
								}
								else {
									legLine += toStnVal + "\t";
								}
							}	
							else if ( nosurveyFlagOn == false ) {
								if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_LENGTH ) == 0 ) {
									legLine += CaveConverter.padNumber( leg.getLength(LengthUnit.Metres), 2, 5 ) + "\t";
								}	
								else if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_BEARING ) == 0 ) {
									legLine += CaveConverter.padNumber( leg.getCompass(BearingUnit.Degrees), 2, 6 ) + "\t";
								}
								else if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_CLINO ) == 0 ) {
									legLine += CaveConverter.padNumber( leg.getClino(GradientUnit.Degrees), 2, 6 ) + "\t";
								}
								else if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_FROMDEPTH ) == 0 ) {
									legLine += CaveConverter.padNumber( leg.getFromDepth(LengthUnit.Metres), 2, 5 ) + "\t";
								}
								else if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_TODEPTH ) == 0 ) {
									legLine += CaveConverter.padNumber( leg.getToDepth(LengthUnit.Metres), 2, 5 ) + "\t";
								}
								else if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_DEPTHCHANGE ) == 0 ) {
									legLine += CaveConverter.padNumber( leg.getDepthChange(LengthUnit.Metres), 2, 5 ) + "\t";
								}
								else if ( item.compareTo( CaveConverter.DATA_ORDER_CAT_IGNOREALL ) == 0 ) {
								}
							}
						}
						
						//Add comment to end of line
						if ( leg.getComment().length() > 0 ) {
							if ( dataOrder.get( dataOrder.size() - 1 ).compareTo( CaveConverter.DATA_ORDER_CAT_IGNOREALL ) != 0 ) {
								//Use comment separator
								legLine += ";";
							}
							legLine += leg.getComment();
						}
						else {
							//Trim whitespace off end of line
							legLine = legLine.trim();
						}

						legsData.add( legLine );
					}
				}
				else {
					//Zero length leg
					if ( leg.isSplay() == false ) {
						//Valid leg, so write as equate
						legsData.add( "*EQUATE " + fromStnVal + "\t" + toStnVal );
					}
				}

				//Add FIXed points to fixed stns block for series
				SurveyStation stn = leg.getFromStn();
				for (int i=0; i < 2; i++) {
					//Loop twice, to process from and to stations
					if ( stn.isFixed() == true ) {
						String fixedStnLine = "*FIX " + substIllegalNameChars( stn.getName() );
						fixedStnLine += "\t" + CaveConverter.decimalNumberStringEnglish( stn.getEasting() ) + "\t" + 
								CaveConverter.decimalNumberStringEnglish( stn.getNorthing() ) + "\t" + 
								CaveConverter.decimalNumberStringEnglish( stn.getAltitude() );
						fixedStnsData.add( fixedStnLine );
					}
					//After first pass using FromStn, get toStn if there is one for second pass
					if ( leg.getToStn() == null ) {
						break;
					}
					stn = leg.getToStn();
				}
			}

			//Process LRUD data for non-splay, non-surface legs, including legs with no LRUD data.
			if ( ( leg.isSplay() == false ) && ( leg.isSurface() == false ) ) {
				//Start new passage data block if leg does not connect the fromStn containing the LRUD data
				//to the start or end of another passage data block in the file
				boolean newBlock = false;
				int activeBlockIdx = passageData.size() - 1; //Initialise to last block in list
				boolean addAtFront = false;
				if ( passageData.size() == 0 ) {
					//Create first block for first lrud station in series
					newBlock = true;
				}
				else {
					//Check if station linked to a station at either end of an existing block
					// We are looking for the toStn at either end of an existing block
					boolean foundExistingBlock = false;

					for (int idx = 0; idx < passageData.size(); idx++ ) {
						List<String[]> block = passageData.get(idx);
						
						if ( leg.getToStn() != null ) {
							//Check if this block starts with the toStn for the leg
							if ( ( block.size() == 1 ) && ( block.get(0)[0].equals( leg.getToStn().getName() ) ) ) {
								//Leg toStn matches only station in block, so leg could go before or after this LRUD
								//We need to know which direction the leg for the existing LRUD measurements was in
								if ( block.get(0)[2].equals( leg.getFromStn().getName() ) ) {
									//This leg follows the one previously added to this block, so add at end
									activeBlockIdx = idx;
									addAtFront = false;
									foundExistingBlock = true;
								}
								else {
									//This leg does NOT follow the one which started this block, so it must go before it
									activeBlockIdx = idx;
									addAtFront = true;
									foundExistingBlock = true;
								}
							}
							else if ( block.get(block.size() - 1)[0].equals( leg.getToStn().getName() ) ) {
								//Matching toStn at end, so add LRUD to end of this block
								activeBlockIdx = idx;
								addAtFront = false;
								foundExistingBlock = true;
							}
							else if ( block.get(0)[0].equals( leg.getToStn().getName() ) ) {
								//Matching toStn at start, so add LRUD to start of this block
								activeBlockIdx = idx;
								addAtFront = true;
								foundExistingBlock = true;
							}
						}
						if ( foundExistingBlock == false ) {
							//Need to find another leg which has a toStn matching the fromStn of this leg
							for (int idx2 = 0; idx2 < series.legCount(); idx2++ ) {
								SurveyLeg chkLeg = series.getLegRaw(idx2);
								if ( ( chkLeg.getToStn() != null ) 
								&& ( chkLeg.getToStn().getName().equals(leg.getFromStn().getName() ) ) ) {
									//Found a leg which precedes the leg we are adding LRUD data from.
									//Check if the fromStn of this preceding leg is the last stn in the block
									if ( block.get(block.size() - 1)[0].equals( chkLeg.getFromStn().getName() ) ) {
										//Matching fromStn for previous leg at end of block, so add LRUD to end of this block
										activeBlockIdx = idx;
										addAtFront = false;
										foundExistingBlock = true;
									}
// This last case turns out not to be needed as it is generally better to start a new passage data block in this case.
//									else {
//										//Check if the preceding leg starts this block 
//										//(but is not already covered going down this block)
//										int blockIdx2 = 1;
//										if ( block.size() == 1 ) {
//											blockIdx2 = 0;
//										}
//										if ( ( block.get(0)[0].equals( chkLeg.getFromStn().getName() ) )
//										&& ( block.get(blockIdx2)[0].equals( chkLeg.getToStn().getName() ) == false ) ) {
//											//Matching toStn at start, so add LRUD to start of this block
//											activeBlockIdx = idx;
//											addAtFront = true;
//											foundExistingBlock = true;
//											logger.logMessage("Stn " + leg.getFromStn().getName() + " fromStn for leg matched toStn of leg " +
//													chkLeg.getFromStn().getName() + "-" + chkLeg.getToStn().getName() + 
//													" where fromStn was at start of PD block but leg is not covered in this block");
//										}
//									}
									if ( foundExistingBlock ) {
										break;
									}
								}
							}
						}
						if ( foundExistingBlock ) {
							break;
						}
					}

					if ( foundExistingBlock == false ) {
						//Create new block for station as it is not connected to any other existing block
						newBlock = true;
					}
				}
				
				if (newBlock) {
					passageData.add( new ArrayList<String[]>() );
					activeBlockIdx = passageData.size() - 1;
				}
				
				String[] lrudLine = createLrudLine( leg );
				if ( addAtFront ) {
					passageData.get(activeBlockIdx).add(0, lrudLine);
				}
				else {
					passageData.get(activeBlockIdx).add( lrudLine );
				}
			}
		}
		//Add missing toStns from legs which close loops
		addMissingPassageDataAtLoopClosureStations( series, passageData );
		
		//Replace LRUD values with those from the actual legs matching the pairs of stations
		//for the current and next line in each block and remove single station blocks
		reprocessPassageDataBlocks( series, passageData );
		
		//Merge passage data blocks where one block ends with the same station as another starts
		combinePassageDataBlocks( passageData );
		
		//Write fixes data block
		if ( fixedStnsData.size() > 0 ) {
			outputData.addAll( fixedStnsData );
		}
		
		//Write legs data block
		outputData.addAll( legsData );
		
		//Turn off splays flag if on
		if ( splayFlagOn ) {
			outputData.add( "*FLAGS NOT SPLAY");
			splayFlagOn = false;
		}

		//Write passage data block
		if ( passageData.size() > 0 ) {
			outputData.add( "");
			//outputData.addAll( passageData );
			ListIterator<List<String[]>> blocksIter = passageData.listIterator();
			while ( blocksIter.hasNext() ) {
				List<String[]> block = blocksIter.next();
				outputData.add("*data passage station left right up down");
				ListIterator<String[]> blockIter = block.listIterator();
				while ( blockIter.hasNext() ) {
					String[] line = blockIter.next();
					//line[0] is fromStn name, and needs illegal characters substituting
					//line[1] is the LRUD numbers, line[2] is the toStn name and is not output
					outputData.add( substIllegalNameChars( line[0] ) + "\t" + line[1] );
				}
			}
		}

		//Loop through inner series
		ListIterator<SurveySeries> seriesIterator = series.getInnerSeriesList().listIterator();
		while ( seriesIterator.hasNext() ) {
			SurveySeries innerSeries = seriesIterator.next();

			outputData.addAll( generateSurvexDataSeries( innerSeries, series, splaysOutput ) );
		}

		//Close the series
		outputData.add( "*END " + substIllegalNameChars( series.getSeriesName() ) );
		outputData.add( "" );

		return outputData;
	}
	
	/* Replace illegal characters in station or series names with Survex allowed name characters.
	 * This method should store previous conversions in case ambiguous cases might arise, and
	 * use a different character to prevent converted stations becoming ambiguous.
	 * e.g. CF+ and CF' do not want to both be changed to CF_ or CF-, but all occurrences of 
	 * CF+ would want to be converted to the same name. We could also check the series for 
	 * occurrences of the same name as an existing legal station (so if CF_1 already existed,
	 * we would not want to convert CF+ to CF_1).	 * 
	 */
	private String substIllegalNameChars( String stnName ) {
		String result = "";
		
		for (int i=0; i < stnName.length(); i++){
			char c = stnName.charAt(i);
			String sub = String.valueOf(c);
			
			if ( c < 48 ) {
				//Check for characters with ascii values below '0'
				switch (c) {
				case 32:
					//	32		<space>
					sub = "_";
					break;
				case 33:
					//	33		!
					sub = "_ex";
					break;
				case 34:
					//	34		"
					sub = "_dq";
					break;
				case 35:
					//	35		#
					sub = "_hs";
					break;
				case 36:
					//	36		$
					sub = "_dl";
					break;
				case 37:
					//	37		%
					sub = "_pc";
					break;
				case 38:
					//	38		&
					sub = "_am";
					break;
				case 39:
					//	39		'
					sub = "_sq";
					break;
				case 40:
					//	40		(
					sub = "_ob";
					break;
				case 41:
					//	41		)
					sub = "_cb";
					break;
				case 42:
					//	42		*
					sub = "_as";
					break;
				case 43:
					//	43		+
					sub = "_pl";
					break;
				case 44:
					//	44		,
					sub = "_cm";
					break;
				case 47:
					//  47		/
					sub = "_fs";
					break;
				}
			}
			else if ( c > 57 ) {
				if ( c < 65 ) {
					//Check for characters with ascii values between '9' and 'A'
					switch (c) {
					case 58:
						//	58		:
						sub = "_co";
						break;
					case 59:
						//	59		;
						sub = "_sc";
						break;
					case 60:
						//	60		<
						sub = "_lt";
						break;
					case 61:
						//	61		=
						sub = "_eq";
						break;
					case 62:
						//	62		>
						sub = "_gt";
						break;
					case 63:
						//	63		?
						sub = "_qm";
						break;
					case 64:
						//	64		@
						sub = "_at";
						break;
					}
				}
				else if ( c > 90 ) {
					if ( c < 97 ) {
						//Check for characters with ascii values between 'Z' and 'a'
						switch (c) {
						case 91:
							//	91		[
							sub = "_os";
							break;
						case 92:
							//	92		\
							sub = "_bs";
							break;
						case 93:
							//	93		]
							sub = "_cs";
							break;
						case 94:
							//	94		^
							sub = "_ht";
							break;
						case 95:
							//	95		_
							//Allowed character
							//sub = "_";
							break;
						case 96:
							//	96		`
							sub = "_gr";
							break;
						}
					}
					else if ( c > 122 ) {
						//Check for characters with ascii values between 'z' and '....'
						switch (c) {
						case 123:
							//	123		{
							sub = "_oc";
							break;
						case 124:
							//	124		|
							sub = "_pi";
							break;
						case 125:
							//	125		}
							sub = "_cc";
							break;
						case 126:
							//	126		~
							sub = "_ti";
							break;
						default:
							//All extended ascii characters, replace with ascii code
							int code = 0 + c;
							sub = "_asc" + code;
						}
					}
				}
			}
			
			result += sub;
		}
		
		return result;
	}

	/* Replace LRUD values with those from actual leg represented by current and next station in block 
	 * and remove single station blocks. Sometimes at junctions the initial LRUD data put into the block
	 * is from a leg which is not the correct leg for the next station added to the block. So where a
	 * leg can be matched to both the station on the data line, and the next station in the block then
	 * we can update the LRUD data to be from that leg.
	 */
	private void reprocessPassageDataBlocks( SurveySeries series, List<List<String[]>> passageData ) {
		//Process all blocks
		ListIterator<List<String[]>> blocksIter = passageData.listIterator();
		while ( blocksIter.hasNext() ) {
			List<String[]> block = blocksIter.next();
		
			//Remove block if only contains a single station as these are obsolete
			if ( block.size() < 2 ) {
				blocksIter.remove();
			}
			else {
				//Update LRUD data for lines in block
				boolean noLrudDataInBlock = true;
				//Loop through each pair of stations in the block
				for ( int blockIdx = 0; blockIdx < ( block.size() - 1 ); blockIdx++ ) {
					String[] line1 = block.get(blockIdx);
					String[] line2 = block.get(blockIdx + 1);
					
					//Look for a leg representing this line1
					boolean legMatchesPassageData = false;
					for ( int legIdx = 0; legIdx < series.legCount(); legIdx++ ) {
						SurveyLeg leg = series.getLegRaw(legIdx);
						if ( ( leg.isSplay() == false )
						&& ( leg.getToStn() != null )
						&& ( leg.getLeft(LengthUnit.Metres) + leg.getRight(LengthUnit.Metres) + 
								leg.getUp(LengthUnit.Metres) + leg.getDown(LengthUnit.Metres) > 0.0 ) ) {
							//Leg is a real leg with LRUD data, check for match both ways
							if ( ( line1[0].equals( leg.getFromStn().getName() ) )
							&& ( line2[0].equals( leg.getToStn().getName() ) ) ) {
								legMatchesPassageData = true;
							}
//Do not look for reverse leg match because LRUD will be on opposite end of leg to the passage data line we are updating							
//							else if ( ( line2[0].equals( leg.getFromStn().getName() ) )
//							&& ( line1[0].equals( leg.getToStn().getName() ) ) ) {
//								legMatchesPassageData = true;
//							}
						}
						//If a matching leg was found then regenerate LRUD data for this line using the leg
						if ( legMatchesPassageData ) {
							//Replace LRUD data for this line with data from this leg
							String[] newLine = createLrudLine(leg);
							if ( line1[1].equals( newLine[1] ) == false ) {
								block.remove(blockIdx);
								block.add(blockIdx, newLine);
							}
							break; //Exit legs for loop
						}
					}
					
					//Check if the line has any LRUD data if this block has not had any so far
					if ( ( noLrudDataInBlock ) 
					&& ( ( line1[1].equals( " 0.00\t 0.00\t 0.00\t 0.00" ) == false ) 
						|| ( line2[1].equals( " 0.00\t 0.00\t 0.00\t 0.00" ) == false ) ) 
					) {
						noLrudDataInBlock = false;
					}
				}
				
				//Remove block if no LRUD data on any station
				if ( noLrudDataInBlock ) {
					blocksIter.remove();
				}
			}
		}
	}
	

	/* Combine any passage data blocks which start with the same station as another block ends with.
	 */
	private void combinePassageDataBlocks( List<List<String[]>> passageData ) {
		//Process all blocks
		int outerIdx = 0;
		while ( outerIdx < passageData.size() ) {
			List<String[]> block = passageData.get( outerIdx );
		
			String lastStn = block.get( block.size() - 1 )[0];
			
			//Iterate through all blocks to see if any start with the last station in this block
			for ( int innerIdx1 = 0; innerIdx1 < passageData.size(); innerIdx1++ ) {
				List<String[]> innerBlock = passageData.get( innerIdx1 );
				
				if ( innerBlock.equals( block ) == false ) {
					String innerStartStn = innerBlock.get( 0 )[0];

					if ( innerStartStn.equals( lastStn ) ) {
						boolean removeInner = true;
						//Append this inner block to the end of the main iterator block
						for ( int innerIdx2 = 0; innerIdx2 < innerBlock.size(); innerIdx2++ ) {
							if ( innerIdx2 == 0 ) {
								//Don't copy first station, but check it matches LRUD of last item in block we are appending to
								if ( block.get( block.size() - 1 )[1].equals( innerBlock.get( 0 )[1] ) == false )  {
									logger.logMessage("Unable to merge passage data blocks due to LRUD data mismatch: " +
											"Block ending " + block.get( block.size() - 1 )[0] + "=" + block.get( block.size() - 1 )[1] + ", " +
											"Block starting " + innerBlock.get( 0 )[0] + "=" + innerBlock.get( 0 )[1]);
									removeInner = false;
									break;
								}
							}
							else {
								block.add( innerBlock.get( innerIdx2 ) );
							}
						}
						if ( removeInner ) {
							//Remove the inner block now it has been copied
							passageData.remove( innerIdx1 );
							outerIdx--;
							//Update last station to new end station
							lastStn = block.get( block.size() - 1 )[0];
						}
					}
				}	
			}
			outerIdx++;
		}
	}
	
	/* When a leg closes a loop, we need to add the LRUD for the toStn of the loop closure by finding that station 
	 * with the LRUD data in another leg. In Stomps, the leg 31-25 has LRUD on 31. But there is no terminal stn25. 
	 * Perhaps there should be? Or easier to analyse all passage data blocks here, consolidate blocks which are adjoined
	 * and then look for any legs where the toStn is not represented and add them by copying data from another leg.
	 */
	private void addMissingPassageDataAtLoopClosureStations( SurveySeries series, List<List<String[]>> passageData ) {
		
		//Check for legs with LRUD data on the toStn which are not represented in blocks
		for ( int legIdx = 0; legIdx < series.legCount(); legIdx++ ) {
			SurveyLeg leg = series.getLegRaw(legIdx);
			if ( ( leg.isSplay() == false ) && ( leg.getLeft(LengthUnit.Metres) + leg.getRight(LengthUnit.Metres) + 
					leg.getUp(LengthUnit.Metres) + leg.getDown(LengthUnit.Metres) > 0.0 ) ) {
				//Check for a leg which has LRUD data on the toStn of this leg
				for ( int legIdx2 = 0; legIdx2 < series.legCount(); legIdx2++ ) {
					SurveyLeg leg2 = series.getLegRaw(legIdx2);
					if ( ( leg.isSplay() == false ) 
					&& ( leg.getToStn() != null )		
					&& ( leg.getLeft(LengthUnit.Metres) + leg.getRight(LengthUnit.Metres) + 
							leg.getUp(LengthUnit.Metres) + leg.getDown(LengthUnit.Metres) > 0.0 ) 
					&& ( leg.getToStn().getName().equals( leg2.getFromStn().getName() ) ) ) {
						//Check if the leg is represented in a passage data block
						boolean foundLegInPassData = false;
						ListIterator<List<String[]>> blocksIter = passageData.listIterator();
						while ( blocksIter.hasNext() ) {
							List<String[]> block = blocksIter.next();
							//Look for pair of lines in this block which represent this leg
							for ( int blockIdx = 0; blockIdx < ( block.size() - 1 ); blockIdx++ ) {
								String[] line1 = block.get(blockIdx);
								String[] line2 = block.get(blockIdx + 1);
								if ( ( line1[0].equals( leg.getFromStn().getName() ) )
								&& ( line2[0].equals( leg.getToStn().getName() ) ) ) {
									foundLegInPassData = true;
									break; //Exit block for loop
								}
								else if ( ( line2[0].equals( leg.getFromStn().getName() ) )
								&& ( line1[0].equals( leg.getToStn().getName() ) ) ) {
									foundLegInPassData = true;
									break; //Exit block for loop
								}
							}
							if ( foundLegInPassData ) {
								break; //Exit passage data blocks loop
							}
						}
						if ( foundLegInPassData == false ) {
							//Leg with toStn LRUD is missing from passage data. 
							//Check if any passage data block starts or ends with one of the stns from the leg
							//so we can add it
							ListIterator<List<String[]>> blocksIter2 = passageData.listIterator();
							while ( blocksIter2.hasNext() ) {
								List<String[]> blockForAdd = blocksIter2.next();
								int insertPoint = 0; //0=not found, 1=start, 2=end
								if ( blockForAdd.get(blockForAdd.size() - 1)[0].equals( leg.getFromStn().getName() ) ) {
									//Leg from station found at end of block
									insertPoint = 2;
								} 
								else if ( blockForAdd.get(0)[0].equals( leg.getFromStn().getName() ) ) {
									//Leg from station found at start of block
									insertPoint = 1;
								} 
								if ( insertPoint > 0 ) {
									//Loop through all passage data block lines looking for one which matches the toStn
									ListIterator<List<String[]>> blocksIter3 = passageData.listIterator();
									while ( blocksIter3.hasNext() ) {
										List<String[]> blockToCopyFrom = blocksIter3.next();
										boolean lineAdded = false;
										ListIterator<String[]> blockIter = blockToCopyFrom.listIterator();
										while ( blockIter.hasNext() ) {
											String[] line = blockIter.next();
											
											if ( line[0].equals( leg.getToStn().getName() ) ) {
												//Insert suitable LRUD line here
												if ( insertPoint == 1) {
													//Insert at start
													blockForAdd.add(0, line);
												}
												else {
													//Insert at end
													blockForAdd.add(line);
												}
												lineAdded = true;
												break; //Exit loop through block looking for line to add
											}
										}
										
										if ( lineAdded ) {
											break; //Exit loop looking for passage data block to add line from
										}
									}
									
									break; //Exit passage data blocks2 loop
								}
							}
						}
						break; //Exit loop leg2 as leg has been processed
					}
				}
			}
		}
		

	}
	
	private String[] createLrudLine( SurveyLeg bestLeg ) {
		String[] lrudLine = new String[3];
		lrudLine[0] = bestLeg.getFromStn().getName();
		lrudLine[1] =  
				CaveConverter.padNumber(bestLeg.getLeft(LengthUnit.Metres),2,5) + "\t" + 
				CaveConverter.padNumber(bestLeg.getRight(LengthUnit.Metres),2,5) + "\t" + 
				CaveConverter.padNumber(bestLeg.getUp(LengthUnit.Metres),2,5) + "\t" + 
				CaveConverter.padNumber(bestLeg.getDown(LengthUnit.Metres),2,5);
		if ( bestLeg.getToStn() != null ) {
			lrudLine[2] = bestLeg.getToStn().getName();
		}
		return lrudLine;

	}
	
}
