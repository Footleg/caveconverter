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
package footleg.cavesurvey.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataInputFormats;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataOutputFormats;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.Equate;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.data.model.SurveyStation;

/**
 * Utility class for functions shared with other code in application.
 * 
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public final class UtilityFunctions {
	private static final double feetPerMetre = 3.280839895;

	private static final char compassFormat = 'c';
	private static final char dxfFormat = 'd';
	private static final char pocketTopoFormat = 'p';
	private static final char survexFormat = 's';
	private static final char toporobotFormat = 't';

	public static final String POCKETTOPO_DATE_FORMAT = "yyyy/MM/dd";
	public static final String SURVEXDATE_FORMAT = "yyyy.MM.dd";
	
	/**
	 * Convert cave survey data input format codes to enum values
	 *
	 * @param  letterCode Single character indicating a cave survey data format
	 * @return Survey format value which the given letter code represents
	 * @throws ParseException raised if the letter code character does not match a supported survey input format
	 */
	public static SurveyDataInputFormats inputDataFormatFromLetterCode( char letterCode ) throws ParseException {
		SurveyDataInputFormats format = null;
		
		switch (letterCode) {
		case compassFormat:
			format = SurveyDataInputFormats.Compass;
			break;
		case dxfFormat:
			format = SurveyDataInputFormats.DXF;
			break;
		case pocketTopoFormat:
			format = SurveyDataInputFormats.PocketTopo;
			break;
		case survexFormat:
			format = SurveyDataInputFormats.Survex;
			break;
		default:
			//Error: Unknown format code
			ParseException e = new ParseException("Unknown data format letter code: " +
					letterCode + "'.", 1);
			throw e;
		}
		
		return format;
	}
	
	/**
	 * Convert cave survey data output format codes to enum values
	 *
	 * @param  letterCode Single character indicating a cave survey data format
	 * @return Survey format value which the given letter code represents
	 * @throws ParseException raised if the letter code character does not match a supported survey output format
	 */
	public static SurveyDataOutputFormats outputDataFormatFromLetterCode( char letterCode ) throws ParseException {
		SurveyDataOutputFormats format = null;
		
		switch (letterCode) {
		case survexFormat:
			format = SurveyDataOutputFormats.Survex;
			break;
		case toporobotFormat:
			format = SurveyDataOutputFormats.Toporobot;
			break;
		default:
			//Error: Unknown format code
			ParseException e = new ParseException("Unknown data format letter code: " +
					letterCode + "'.", 1);
			throw e;
		}
		
		return format;
	}
	

	/**
	 * Math function to calculate the average compass bearing from a set
	 * of compass readings. The calculation used determines the sum of the
	 * vectors. This is the equivalent of converting the bearings from unit 
	 * vectors each with a polar coordinate to x and y components, averaging
	 * the x and y components to produce a resulting vector, and then taking
	 * the polar coordinate bearing of that vector as the result. For a pair
	 * of compass readings this average represents the mean direction of the
	 * two survey legs. For a set of two or more readings it represents to
	 * mean direction of all the bearings giving them equal weight. This is 
	 * different to the numerical average of several repeated readings of an
	 * instrument (like 3 DistoX shots from PocketTopo), but the results are
	 * the same (at a precision of two decimal places) for either averaging 
	 * method for sets of bearings with no more than 2 degrees variation
	 * between the readings.
	 *
	 * @param  bearings Array of compass bearings to calculate average from
	 * @return  Mean compass bearing in range 0-360 degrees
	 */
	public static double averageCompassBearings( double[] bearings ) {
  		double meanBearing = 0.0;

		double accumulatorCos = 0.0;
		double accumulatorSin = 0.0;
		
		//Loop through all bearings
		for ( int bearingIdx = 0; bearingIdx < bearings.length; bearingIdx++ ) {
			//Convert bearing to radians
			Double bearing = Math.toRadians( bearings[bearingIdx] );
			
			//Accumulate sum of sin and cos of bearings
		    accumulatorCos += Math.cos(bearing);
		    accumulatorSin += Math.sin(bearing);
		}

		double average_angle_atan2 = Math.atan2(accumulatorSin, accumulatorCos);
		meanBearing = Math.toDegrees(average_angle_atan2);
		
		//Adjust bearing to within range of 0-360 degrees
		return adjustBearingWithinDegreesRange( meanBearing, 0, 360 );
	}

	/**
	 * Adjusts a bearing in degrees to a value within the range given
	 * @param bearing The bearing to adjust
	 * @param min The minimum value to allow
	 * @param max The lowest value to adjust down (adjusted bearing must be less than this value)
	 * @return Bearing adjusted to within range given
	 */
	public static double adjustBearingWithinDegreesRange(double bearing, double min, double max) {
		double adjustedBearing = bearing;
		
		while ( adjustedBearing < min ) {
			adjustedBearing += 360;
		}
		while ( adjustedBearing >= max ) {
			adjustedBearing -= 360;
		}
		
		return adjustedBearing;
	}
	
	/**
	 * Math function to calculate the difference between two compass bearings
	 * @param angle1 First compass bearing
	 * @param angle2 Second compass bearing
	 * @return difference in degrees between the two bearings
	 */
	public static double bearingDifferenceDegrees( double angle1, double angle2) {
		double dif = Math.abs( angle1 - angle2 ) % 360;

		if (dif > 180)
		    dif = 360 - dif;
	    return dif;
	}

	/**
	 * Creates a new survey station for a series using a given name for the station.
	 * 
	 * The name is mapped to a numerical station id using the series, and the station
	 * is created with that id. If the name is already in use then the id will be the
	 * same id as that used for the matching station in the series.
	 *
	 * @param  stnName The name to give the new survey station
	 * @param  series  The survey series which this station is going to be added to
	 * @return New survey station object with an id mapped to the name for this series
	 */
	public static SurveyStation createStationFromNameForSeries( String stnName, SurveySeries series ) {
		SurveyStation stn = new SurveyStation( 0 );
		stn.setName( stnName );
		series.setStationIdFromName(stn);
		
		return stn;
	}
	
	/**
	 * Date string parsing function to return a date type from a text representation of the date.
	 *
	 * @param  dateString Text representation of a date
	 * @param  dateFormat Defines the format for the text representation of the date 
	 * @throws ParseException if the string is not a valid date
	 * @return The date parsed from the string parameter
	 */
	public static Date stringToDate( String dateString, String dateFormat ) throws ParseException {
		Date dateVal;
		
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);	
		sdf.setTimeZone(TimeZone.getDefault());          

		dateVal = sdf.parse(dateString);
		
		return dateVal;
	}

	/**
	 * Method which generates a string representing a given date.
	 *
	 * @param  date The date to be converted to text 
	 * @param  dateFormat Defines the format for the text representation of the date 
	 * @return Text representation of the date in the format requested
	 */
	public static String dateToString( Date date, String dateFormat ) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);	
		sdf.setTimeZone(TimeZone.getDefault());          
		
		return sdf.format( date );
	}

	/**
	 * Generates a string representing the date on the day the method
	 * is called.
	 *
	 * @param  dateFormat Defines the format for the text representation of the date 
	 * @return Text representation of today's date in the format requested
	 */
	public static String todaysDateAsString( String dateFormat ) {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		Date dateTimeNow = cal.getTime();
		
		return dateToString( dateTimeNow, dateFormat );
	}

	/**
	 * Converts a string list into a string of items from the list 
	 * separated with a given string
	 * 
	 * @param list String list of items to convert to a string
	 * @param separator String to separate items with in the output
	 * @return Dot separated string of the items from the input list
	 */
	public static String stringListToDotSeparatedString( List<String> list, String separator) {
		String items = "";
		
		ListIterator<String> primaryIterator = list.listIterator();
		if ( primaryIterator.hasNext() ) {
			//First item
			items += primaryIterator.next();
		}
		while ( primaryIterator.hasNext() ) {
			items += separator + primaryIterator.next();
		}
		
		return items;
	}
	
	/**
	 * Splits a string of data items separated by whitespace into an array of
	 * individual data items. Tab and space characters are treated as 
	 * separators unless enclosed in quotes.
	 *
	 * @param  dataString String of data items separated by white space 
	 * @return Array of data items extracted from the input data string
	 */
	public static List<String> parseDataStringIntoDataItems( String dataString ) {
		List<String> dataItems = new ArrayList<String>();
		String currItem = "";
		boolean insideQuotes = false;
		
		//Parse input string character by character
		for (int i = 0; i < dataString.length(); i++) {
			//Check if inside quotes
			if ( insideQuotes ) {
				//Check for ending quotes
				if ( dataString.charAt(i)  == '\"' ) {
					//End of item, so update array with data for this item
					dataItems.add( currItem );
					//Reset for next item
					currItem = "";
					insideQuotes = false;
				}
				else {
					//Non-quote, and non-whitespace character, so add to end of current item
					currItem += dataString.charAt(i);
				}
			}
			else if ( dataString.charAt(i)  == '\"' ) {
				//Found start of new quoted data item
				insideQuotes = true;
			}
			else if ( dataString.charAt(i)  == ' ' || dataString.charAt(i)  == '\t' ) {
				//Found whitespace, check if this is the end of a data item
				if ( currItem.length() > 0 ) {
					//End of item, so update array with data for this item
					dataItems.add( currItem );
					//Reset for next item
					currItem = "";
				}
			}
			else {
				//Non-quote, and non-whitespace character, so add to end of current item
				currItem += dataString.charAt(i);
			}
		}

		//End of data, check if the last item was added
		if ( currItem.length() > 0 ) {
			//Last item, so update array with data for this item
			dataItems.add( currItem );
		}

		return dataItems;
	}
	
	/**
	 * Splits a comment string into comment lines, splitting at line separator 
	 * characters and removing enclosing quotes.
	 *
	 * @param  comment Comment string to be parsed 
	 * @return Array of comment line from the input comment string
	 */
	public static String[] parseTripComment( String comment ) {
		String dataString = comment.replace("\\r", "\r");
		String[] dataItems = dataString.split("\r");
		
		return dataItems;
	}
		
	/**
	 * Converts a list of equates gathered during file parsing into series links in the survey.
	 * 
	 * A helper function shared by some of the data readers, which generate a list
	 * of equate classes from equivalent stations in the input files. This method 
	 * matches these equates to series in the survey, and creates a series link in the
	 * appropriate series to link the series to another series in the survey.
	 * 
	 * @param equates ArrayList of equates to be processed
	 * @param allSeries The cave survey to add the series links into
	 */
	public static void processEquates( List<Equate> equates, CaveSurvey allSeries ) {
		//Process each of the equates in turn
		Iterator<Equate> equateIterator = equates.listIterator();
		while ( equateIterator.hasNext() ) {
			Equate equate = equateIterator.next();
			
			//Find the series path in common between the equates and determine which is the inner series if nested
			String innerSeries;
			String outerSeries;
			String innerSeriesStn;
			String outerSeriesStn;
			
			if ( equate.getSeries1().length() > equate.getSeries2().length() ) {
				//First series is inner series to second series
				outerSeries = equate.getSeries2();
				outerSeriesStn = equate.getStn2();
				innerSeries = equate.getSeries1();
				innerSeriesStn = equate.getStn1();
			}
			else {
				//First series is same as or outer series to second series
				outerSeries = equate.getSeries1();
				outerSeriesStn = equate.getStn1();
				innerSeries = equate.getSeries2();
				innerSeriesStn = equate.getStn2();
			}
			
			//Find parent series, and remove path to it from the two series paths
			List<String> outerHierarchy = new ArrayList<String>(Arrays.asList(outerSeries.split("[.]")));
			List<String> innerHierarchy = new ArrayList<String>(Arrays.asList(innerSeries.split("[.]")));
			List<String> parentHierarchy = new ArrayList<String>();
			
			boolean matching = true;
			while ( matching && (outerHierarchy.size() > 0 ) ) {
				if (outerHierarchy.get(0).equalsIgnoreCase( innerHierarchy.get(0) ) ) {
					//Common series name to both series in equate, so move to parent
					parentHierarchy.add( outerHierarchy.remove(0) );
					innerHierarchy.remove(0);
				}
				else {
					matching = false;
				}
			}
			
			//Find innermost series which is a parent of the equates series
			SurveySeries liveSeries = null;
//			boolean dealtWithUnmatchedSeries = false;
			
			if ( parentHierarchy.size() > 0 ) {
				//Iterate over series names in parent path looking for matching inner most series
				ListIterator<String> pathIterator = parentHierarchy.listIterator();
				String liveName = pathIterator.next();

				//First find the outer series for the outer path name
				ListIterator<SurveySeries> primaryIterator = allSeries.listIterator();
				while ( primaryIterator.hasNext() ) {
					SurveySeries outermostSeries = primaryIterator.next();
					if ( outermostSeries.getSeriesName().equalsIgnoreCase( liveName ) ) {
						//Go to next level in path
						liveSeries = outermostSeries;
						break;
					}
					else {
						//Not found, so keep looking
						matching = false; //Just here to allow breakpoint debugging
					}
				}

				//Did we find outer series?
				if ( liveSeries != null ) {
					//Now iterate over path looking for matching inner series for each level
					if ( pathIterator.hasNext() ) {
						liveSeries = findInnerSeriesByName( liveSeries, pathIterator);
					}
					
					//If we got here without any exceptions being thrown then we should have the correct parent series
					matching = true; //Just here to allow breakpoint debugging
					
				}
				else {
					//Failed to find matching outer series
					throw new RuntimeException( "Equate series outer name '" + 
							liveName + "' did not match any cave name." );
				}
					
			}
			else {
				//No parent in common which indicates that the link is invalid as one of 
				//the series is not in the cave survey at all.  
				//TODO Fix this for pockettopo imports where only part of a cave is present. Need a dummy link in this case.
				//This is no longer where the PocketTopo files raise an error.
				throw new RuntimeException( "Series in equate did not match any series name. Series1: '" + 
											equate.getSeries1() + "'. Series2: '"  + equate.getSeries2() + "'." );
				//Check that at least one of the series matches a series name
//				int series1Idx = lookupSeriesIndexFromSeriesName( equate.getSeries1(), allSeries );
//				if ( series1Idx >= 0 ) {
//					//Add link to series 1
//					SurveyStation stn = createStationFromNameForSeries( equate.getStn1(), allSeries.get( series1Idx ) );
//					SurveyStation fake = new SurveyStation( 0 );
//					fake.setName( equate.getStn2() );
//					allSeries.get( series1Idx ).addLink( equate.getSeries1(), stn, equate.getSeries2(), fake );
//					dealtWithUnmatchedSeries = true;
//				}
//				else {
//					int series2Idx = lookupSeriesIndexFromSeriesName( equate.getSeries2(), allSeries );
//					if ( series2Idx >= 0 ) {
//						//Add link to series 2
//						SurveyStation stn = createStationFromNameForSeries( equate.getStn2(), allSeries.get( series2Idx ) );
//						SurveyStation fake = new SurveyStation( 0 );
//						fake.setName( equate.getStn1() );
//						allSeries.get( series2Idx ).addLink( equate.getSeries1(), fake, equate.getSeries2(), stn );
//						dealtWithUnmatchedSeries = true;
//					}
//				}
			}
			
			//By here, live series is the parent series for both equates.
			//The outer and inner hierarchies indicate the paths to the inner series from this point
				
			if ( liveSeries != null ) {
				String equatePart1;
				SurveySeries part1Series;
				SurveyStation stn1 = null;
				if ( outerHierarchy.isEmpty() ) {
					equatePart1 = "";
					part1Series = liveSeries;
					stn1 = createStationFromNameForSeries( outerSeriesStn, part1Series );
				} 
				else {
					equatePart1 = stringListToDotSeparatedString( outerHierarchy, ".");
					try {
						part1Series = findInnerSeriesByName( liveSeries, outerHierarchy.listIterator() );
						stn1 = createStationFromNameForSeries( outerSeriesStn, part1Series );
					}
					catch (RuntimeException ex) {
						//PocketTopo files often have equates to stations in series not in the file
						stn1 = new SurveyStation( 0 );
						stn1.setName( equate.getStn1() );
					}
				}
					
				String equatePart2;
				SurveySeries part2Series;
				SurveyStation stn2 = null;
				if ( innerHierarchy.isEmpty() ) {
					equatePart2 = "";
					part2Series = liveSeries;
					stn2 = createStationFromNameForSeries( innerSeriesStn, part2Series );
				} 
				else {
					equatePart2 = stringListToDotSeparatedString( innerHierarchy, ".");
					try {
						part2Series = findInnerSeriesByName( liveSeries, innerHierarchy.listIterator() );
						stn2 = createStationFromNameForSeries( innerSeriesStn, part2Series );
					}
					catch (RuntimeException ex) {
						//PocketTopo files often have equates to stations in series not in the file
						stn2 = new SurveyStation( 0 );
						stn2.setName( equate.getStn2() );
					}
				}

				//Put link into the parent series of the two linked series
				liveSeries.addLink(equatePart1, stn1, equatePart2, stn2);
			}
			else {
				//No parent series found for this equate
				throw new RuntimeException( "Equate series are not inside same series. Series1: '" + 
											equate.getSeries1() + "'. Series2: '"  + equate.getSeries2() + "'." );
			}
		}
		
	}
	
	/**
	 * 
	 * @param outerSeries
	 * @param pathIterator
	 * @return
	 */
	private static SurveySeries findInnerSeriesByName( SurveySeries outerSeries, ListIterator<String> pathIterator) {
		SurveySeries liveSeries = outerSeries;
		//Now iterate over path looking for matching inner series for each level
		while ( pathIterator.hasNext() ) {
			String liveName = pathIterator.next();
			liveSeries = liveSeries.findInnerSeriesByName( liveName );
			if (liveSeries == null ) {
				//Failed to match a series at this level
				throw new RuntimeException( "Equate series name '" + 
						liveName + "' did not match any inner series name." );
			}
		}
		
		return liveSeries;
	}
	
	public static void logSurveyDebugData( CaveSurvey allSeries, Logger logger ) {
		//Debug dump
		logger.logMessage("============================ Cave Survey Data Summary ============================");
		logger.logMessage("Survey contains " + allSeries.size() + " top level series.");
		for (int i = 0; i < allSeries.size(); i++ ) {
			logSurveySeriesSummary( allSeries.get(i), "", logger );
		}
	}
	
	private static void logSurveySeriesSummary( SurveySeries series, String seriesParent, Logger logger ) {
		String fullSeriesPath = seriesParent + series.getSeriesName();
		String message = "Series: " + fullSeriesPath;
		if ( series.legCount() > 0 ) {
			message += " (" + series.legCount() + " legs)";
		}
		if ( series.innerSeriesCount() > 0 ) {
			message += " (contains " + series.innerSeriesCount() + " child series)";
		}
		logger.logMessage( message );
		//Recursively log details of inner series
		for (int i = 0; i < series.innerSeriesCount(); i++ ) {
			logSurveySeriesSummary( series.getInnerSeries(i), fullSeriesPath + "/", logger );
		}
	}
	
	/**
	 * Reads a text file and builds an ArrayList of strings, one for each line 
	 * of the file. If the multi-file option is set then file lines will be checked
	 * for include statements referencing other files, and if found these files contents
	 * will be inserted into the output in place of the include statement lines.
	 *
	 * @param  inputFile File to be read
	 * @param  characterSetEncoding Character set encoding to use when reading the text file
	 * @param  multiFile If set to true then file lines are checked for includes for other files to be inserted
	 * @param  multifileLineRefs Used to output an ArrayList of strings indicating the original file and line 
	 *             number for each data line in the output data. Requires an empty list to be passed in.
	 * @param  logger Logging class to output information, warning and error messages to
	 * @return ArrayList of strings, one for each line of the input filename
	 */
	public static List<String> readTextFile( File inputFile, String characterSetEncoding, 
			boolean multiFile,  List<String> multifileLineRefs, Logger logger ) {
		List<String> dataLines = new ArrayList<String>();

		BufferedReader bufferedReader = null;

		try {
			FileInputStream fis = new FileInputStream( inputFile );
			InputStreamReader isr = new InputStreamReader( fis, characterSetEncoding );
			bufferedReader = new BufferedReader ( isr );

			String text;
			int lineNo = 0;
			while ( ( text = bufferedReader.readLine() ) != null) {
				lineNo++;
				boolean addLine = true;
				
				if ( multiFile ) {
					//Check line for include statement
					String line = text.trim();
					if ( line.toLowerCase().startsWith( "*include" ) ) {
						//Extract path to include file
						String pathAddn = "";
						for ( int i = 8; i < line.length(); i++ ) {
							//Take all characters after *include up to comment char or line end
							if ( line.charAt(i) == ';' ) {
								break;
							}
							else {
								pathAddn += line.charAt(i);
							}
						}
						pathAddn = pathAddn.trim();
						//Check for quotes around pathAddn and remove them if found
						if ( pathAddn.charAt(0) == '"' &&  pathAddn.charAt( pathAddn.length() - 1 ) == '"' ) {
							pathAddn = pathAddn.substring(1, pathAddn.length() - 1 );
						}
						boolean extPresent = (pathAddn.lastIndexOf('.') >= 0) 
								&& pathAddn.substring( pathAddn.lastIndexOf('.') ).toLowerCase().equals(".svx");
						String nextFilePath = folderFromFile( inputFile );
						File nextFile = new File( nextFilePath + File.separatorChar + pathAddn );
						if ( extPresent == false && nextFile.exists() == false ) {
							//Include statement potentially needs file extension adding, try lower case first
							nextFile = new File( nextFilePath + File.separatorChar + pathAddn + ".svx" );
							if ( nextFile.exists() == false ) {
								//Still not found, try upper case first
								nextFile = new File( nextFilePath + File.separatorChar + pathAddn + ".SVX" );
							}
						}
						logger.logMessage( "Including file: " + nextFile.getPath() );
						List<String> fileData = UtilityFunctions.readTextFile( nextFile, characterSetEncoding, multiFile,
								multifileLineRefs, logger );
						dataLines.addAll(fileData);
						addLine = false;
					}
					else {
						//Not an include line in a multifile read, so store line ref
						multifileLineRefs.add( inputFile.getPath() + ":" + lineNo );
					}
				}
				if ( addLine ) {
					dataLines.add( text );
				}
			}
		} 
		catch (FileNotFoundException ex) {
			logger.logError( ex.getMessage() );
		} 
		catch (IOException ex) {
			logger.logError( ex.getMessage() );
		} 
		finally {
			if ( bufferedReader != null ) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					logger.logError( ex.getMessage() );
				}
			}
		}
        
        return dataLines;
	}

	/**
	 * Write a text file from the data in an ArrayList of strings, one line 
	 * in the file per string in the List.
	 *
	 * @param  fileContents ArrayList of strings, one for each line of the input filename
	 * @param  fileName Full path and name of file to be written
	 * @param  characterSetEncoding Character set encoding to use when writing the text file
	 * @return Error message if write failed, or empty string if success
	 */
	public static String writeTextFile (List<String> fileContents, String fileName, String characterSetEncoding ) {
		String error = "";

		BufferedWriter textWriter = null;
		try {
			//Create file writer
			FileOutputStream fos = new FileOutputStream( fileName );
	        Writer out = new OutputStreamWriter(fos, characterSetEncoding);
	        textWriter = new BufferedWriter(out);

			//Write strings from list to the file
			Iterator<String> iterator = fileContents.listIterator();
			while ( iterator.hasNext() ) {
				String line = iterator.next();
				if ( iterator.hasNext() ) {
					textWriter.write( line + System.getProperty("line.separator") );
				}
				else {
					textWriter.write( line );
				}
			}
		}
		catch (IOException ex) {
			error = "Error writing file: " + ex.getLocalizedMessage();
		}
		finally {
			if ( textWriter != null ) {
				try {
					textWriter.close();
				}
				catch (IOException ex) {
					error = "Error closing file: " + ex.getLocalizedMessage();
				}
			}
		}

		return error;
	}

	/**
	 * Checks for station in the series being built. If found then adding this leg would mean the
	 * series looped back onto itself, which we cannot do in Toporobot format.
	 * @param iter Iterator for the list of stations already added to the series
	 * @param checkStn The station to check for in the series being built up
	 * @return True if station is found in the list cache
	 */
	private static boolean checkForLoopback( ListIterator<SurveyStation> iter, SurveyStation checkStn ) {
		boolean loopDetected = false;

		while ( iter.hasNext() ) {
			if ( checkStn.getId() == iter.next().getId() ) {
				loopDetected = true;
				break;
			}
			
		}
		return loopDetected;
	}
	/**
	 * Checks for a station in all the series so far, to avoid series crossing other series in 
	 * the middle of each other, as TopoRobot format cannot support links of this sort.
	 * @param caveSoFar
	 * @param checkStn
	 * @return
	 */
	private static boolean checkForCrossover( SurveySeries caveSoFar, SurveyStation checkStn ) {
		boolean crossingDetected = false;
		
		ListIterator<SurveySeries> seriesIter = caveSoFar.getInnerSeriesList().listIterator();
		while ( seriesIter.hasNext() ) {
			//Check for station in all legs bar the final one in this series
			SurveySeries series = seriesIter.next();
			for ( int i=0; i < series.legCount() - 1; i++ ) {
				SurveyLeg leg = series.getLegRaw(i);
				//Check leg to station as from station in first leg starts series
				//so not a crossover, and all other from stations are also to stations
				if ( checkStn.getId() == leg.getToStn().getId() ) {
					crossingDetected = true;
					i = series.legCount();
				}
			}
			if ( crossingDetected == true ) {
				break;
			}
		}
		return crossingDetected;
	}
	
	/**
	 * Splits a single survey series into a list of series without branches or loops.
	 * The input series must not contain any nested series. The output series will
	 * contain one or more inner series and the links between them, but no legs itself.
	 * This method requires that any survey station can be identified by it's ID alone,
	 * so stations with different names but the same ID are assumed to be the same station.
	 * 
	 * TODO Somewhere in here is a bug which allows series to be linked together in unconnected
	 * groups. It happens when two series are linked to each other via the same station. The
	 * link is in effect duplicated, rather than one of the series being linked to another part of
	 * the cave. There is also an issue that both ends of the series do not get linked to the
	 * rest of the cave. Sometimes both the start and end stations are linked to another series
	 * but only one of these links is created in the toporobot file.
	 * 
	 * @param  seriesIn A survey series which contains branches
	 * @param  logger Logging class to output information, warning and error messages to
	 * @return A series containing one or more inner series, all linked by their first 
	 *         stations to one of the other series. All series will be linear chains.
	 */
	public static SurveySeries convertToLinearSeries( SurveySeries seriesIn, Logger logger ) {
		
		
		SurveySeries outputSeries = new SurveySeries( seriesIn.getSeriesName() );
		
		//Check the input series contain no inner series
		if ( seriesIn.innerSeriesCount() > 0 ) {
			throw new RuntimeException( "Nested series cannot be converted to linear chains." );
		}
		
		//Loop through all the legs creating inner series of linear chains until all legs are
		//handled or remaining legs cannot be linked to any other legs.
		boolean legsRemain = true;
		while (legsRemain) {
			//Create a new series to start building a new linear chain
			SurveySeries series = new SurveySeries( seriesIn.getSeriesName() ); 
			series.setCalibrationFromAnotherSeries( seriesIn );
			
			//Create cache of all stations used to check for loops back onto stations already used
			List<SurveyStation> seriesStns = new ArrayList<SurveyStation>();
			
			//Initialise last station flag to invalid id
			int lastStnId = -999;

			//Loop through all remaining legs and add connected legs to new series
			int addedLegsCount = 0;
			boolean legsAdded = true; //Initialise true to get into first pass of loops
			//Keep looping over all remaining legs until no more are being added to this series
			while ( legsAdded ) {
				legsAdded = false; //Reset
				
				int i = 0;
				while ( i < seriesIn.legCount() ) {
					int initialInnerAddedLegCount = addedLegsCount;
					SurveyLeg curLeg = seriesIn.getLegRaw(i);
					
					//Check if first leg for new series (always add), or leg follows last leg added
					if ( ( series.legCount() == 0 ) || ( lastStnId == curLeg.getFromStn().getId() ) ) {
						//Check that 'to' station has not already been used in the series
						boolean loopDetected = checkForLoopback( seriesStns.listIterator(), curLeg.getToStn() );
						if (loopDetected == false) {
							boolean crossDetected = false;
							if ( series.legCount() > 0 ) {
								//Only check for cross over if not first station in a new series
								crossDetected = checkForCrossover( outputSeries, curLeg.getFromStn() );
							}
							if (crossDetected == false) {
								//Add leg to end of series
								series.addLeg(curLeg);
								//Update last stn
								lastStnId = curLeg.getToStn().getId();
								//Increment added legs count
								addedLegsCount++;
								//Add new station from this leg into stations cache
								seriesStns.add( curLeg.getToStn() );
							}
						}
					}
					else if ( curLeg.getToStn().getId() == series.getLegRaw(0).getFromStn().getId() ) {
						//Leg fits on start of series, so insert at beginning if not looping back onto series

						//Check that 'from' station has not already been used in the series
						boolean loopDetected = checkForLoopback( seriesStns.listIterator(), curLeg.getFromStn() );
						if (loopDetected == false) {
							boolean crossDetected = checkForCrossover( outputSeries, curLeg.getToStn() );
							if (crossDetected == false) {
								series.addLeg(curLeg, 0);
								//Increment added legs count
								addedLegsCount++;
								//Add new station from this leg into stations cache
								seriesStns.add( curLeg.getFromStn() );
							}
						}
					}

					if (initialInnerAddedLegCount < addedLegsCount ) {
						//Leg was added, so remove from input series
						seriesIn.removeLeg(i);
						//Update flag to indicate a leg has been added
						legsAdded = true;
					}
					else {
						//Leg was not used, so increment index to test next leg
						i++;
					}
				}
			}
			
			//Check whether any legs were added to this series (if not then exit)
			if ( series.legCount() > 0 ) {
				/*
				 * New series created, set name from to station of first leg (fromStn name has often been changed by equating) 
				 * up to the last dot, and add the series index to ensure series name is unique
				 */
				String originalSeriesStn2Name = seriesIn.getMappedStnName( series.getLegRaw(0).getToStn().getId() );
				//Base new series name on parent series and station name
				String originalSeriesName = seriesIn.getSeriesName() + "-" + originalSeriesStn2Name;
				int dotPos = originalSeriesStn2Name.lastIndexOf(".");
				if ( dotPos > 0 ) {
		 			//Station name has a series name prefix, so use this in preference to parent series name
					originalSeriesName = originalSeriesStn2Name.substring(0, dotPos );
				}
				series.setSeriesName( (outputSeries.innerSeriesCount() + 1) + "-" + originalSeriesName );
				//Add to list
				outputSeries.addSeries(series);
			}
			else {
				//No legs were processed on last pass, so exit the loop
				legsRemain = false;
			}
		}
		
		//Check that all legs were moved into output series
		if ( seriesIn.legCount() > 0 ) {
			throw new RuntimeException( "Some legs could not be linked to other legs in the series." );
		}

		//Now loop through all the series and for each one match the start 
		//station to a station in another series
		logger.logMessage( "Linking series:" );
		for ( int searchIdx = 0; searchIdx < outputSeries.innerSeriesCount(); searchIdx++ ) {
			SurveySeries seriesToLink = outputSeries.getInnerSeries(searchIdx);

			//Get first and last stns
			SurveyStation firstStn = seriesToLink.getLegRaw(0).getFromStn();
			SurveyStation endStn = seriesToLink.getLegRaw( seriesToLink.legCount() - 1 ).getToStn();

			//Check all the other series for station matching the start station for this series
			int matchingIdx = findStationMatchingIdInInnerSeries(firstStn.getId(), searchIdx, outputSeries);
			if ( matchingIdx > -1 ){
				logger.logMessage( "Series: " + outputSeries.getInnerSeries(searchIdx).getSeriesName() +
						" start stn linked to " + outputSeries.getInnerSeries(matchingIdx).getSeriesName() );
				//Found match, so add link to parent series
				outputSeries.addLink(outputSeries.getInnerSeries(searchIdx).getSeriesName(), firstStn, 
						outputSeries.getInnerSeries(matchingIdx).getSeriesName(), firstStn);
			}
//			else {
				/*
				 * Need to check for end stn matches even if a start station link was found or loops are never closed 
				 * and sets of series can be created which are not connected to other sets. 
				 * TODO The commented out else block fixes the links not being set, but now a series which
				 * starts where another series ends is linked twice, once in each direction. We should add a method
				 * to the series class to check if a link already exists, and only add these links if not already defined
				 * in the reverse direction.
				 */
				//No links to start of this series, so check if end station matches
				matchingIdx = findStationMatchingIdInInnerSeries(endStn.getId(), searchIdx, outputSeries);
				if ( matchingIdx > -1 ){
					logger.logMessage( "Series: " + outputSeries.getInnerSeries(searchIdx).getSeriesName() +
							" end stn linked to " + outputSeries.getInnerSeries(matchingIdx).getSeriesName() );
					//Found match, so need to reverse this series and add a link for it
//No need to reverse as we now support end links					outputSeries.getInnerSeries(searchIdx).reverseSeries();
					outputSeries.addLink(outputSeries.getInnerSeries(searchIdx).getSeriesName(), endStn, 
							outputSeries.getInnerSeries(matchingIdx).getSeriesName(), endStn);
				}
//			}
		}
		
		return outputSeries;
	}
	
	/**
	 * Looks for an inner series with a station which matches the id given, and if found 
	 * then returns the index of that inner series. An index of an inner series to
	 * exclude can be provided, to prevent a series being matched to itself.
	 */
	private static int findStationMatchingIdInInnerSeries(int id, int excludeSeriesIdx, SurveySeries series){
		int matchingIdx = -1;
		
		ListIterator<SurveySeries> innerItr = series.getInnerSeriesList().listIterator();
		int checkIdx = -1;
		while ( matchingIdx == -1 && innerItr.hasNext() ) {
			SurveySeries check = innerItr.next();
			checkIdx++;
			//Do not check against the same series as ourself
			if ( excludeSeriesIdx != checkIdx ) {
				//Loop through legs in check series
				for ( int legIdx = 0; legIdx < check.legCount(); legIdx++ ) {
					SurveyLeg leg = check.getLegRaw(legIdx);
					//Looking for matching stations
					if ( id == leg.getFromStn().getId() ) {
						//Found match to a from station
						matchingIdx = checkIdx;
						break;
					}
					else if ( id == leg.getToStn().getId() ) {
						//Found match to a to stn
						matchingIdx = checkIdx;
						break;
					}
				}
			}
		}
		
		return matchingIdx;
	}
	
	/**
	 * Removes whitespace and splits a line into items separated by whitespace
	 * Returns and array of items from the input data line
	 * 
	 * @param  dataIn Data string to convert into data items
	 * @return Array of data strings split at white space from the input data line
	 */
	public static String[] cleanAndSplitDataLine( String dataIn ) {
		//Process all white space down to single space chars
		String dataLine = dataIn.replace('\t', ' ').trim();
		while ( dataLine.contains("  ") ) {
			dataLine = dataLine.replaceAll("  ", " ");
		}
		return dataLine.split(" ");
	}

	/**
	 * Formats a message including the line number for file parsing messages
	 * 
	 * @param  message Message to append line number to
	 * @param  lineNo  Line number to append to message text
	 * @return Message text with line number appended
	 */
	public static String formatFileParserMsg( String message, int lineNo ) {
		String msg = message;
		
		msg += " (Line: " + lineNo + ")";
		return msg; 
	}

	/**
	 * Formats a message including a string line reference for file parsing messages
	 * 
	 * @param  message Message to append line number to
	 * @param  lineRef  Line reference to append to message text
	 * @return Message text with line number appended
	 */
	public static String formatFileParserMsg( String message, String lineRef ) {
		String msg = message;
		
		msg += " (Line ref: " + lineRef + ")";
		return msg; 
	}
	
	/**
	 * Generates a data format line suitable for Survex and Therion format files
	 * 
	 * @param series Survey series to generate the data format line for
	 * @param legIsDiving Flag to indicate whether the leg following this format header is a diving leg
	 * @return Data format line without prefix character (i.e. no * needed for Survex)
	 */
	public static String dataFormatLineForSeries( SurveySeries series, boolean legIsDiving ) {
		List<String> dataOrder = series.getDataOrder();
		List<String> dataOrder2 = series.getDataOrder2();
		
		//Determine if correct data order item for normal or diving in case of mixed data series
		if ( dataOrder2.size() > 0 ) {
			if ( dataOrderIsDiving( dataOrder ) != legIsDiving ) {
				//Primary data order does not match required type, so use secondary order
				dataOrder = dataOrder2;
			}
		}
		
		String dataOrderLine = "";
		boolean diving = false;
		
		//Check for diving
		for (int i = 0; i < dataOrder.size(); i++) {
			String item = dataOrder.get(i);
			dataOrderLine += " " + item.toLowerCase();
			if ( item.contains("DEPTH") ) {
				diving = true;
			}
		}
		
		if ( diving == true ) {
			dataOrderLine = "diving" + dataOrderLine;
		}
		else {
			dataOrderLine = "normal" + dataOrderLine;
		}

		//Prefix with data keyword, or return empty string if there was no data order specified in the series
		if ( dataOrderLine.length() > 7 ) {
			dataOrderLine = "data " + dataOrderLine;
		}
		else {
			dataOrderLine = "";
		}
		
		return dataOrderLine;
	}
	
	/**
	 * Compares the strings in two string lists, and returns true if they are all identical
	 * @param list1 One of the pair of string lists to compare
	 * @param list2 One of the pair of string lists to compare
	 * @return True if string lists are identical, otherwise false
	 */
	public static boolean compareStringLists( List<String> list1, List<String> list2 ) {
		boolean match = true;
		
		if ( list1.size() == list2.size() ) {
			for ( int i = 0; i < list1.size(); i++ ) {
				if ( list1.get(i).compareTo( list2.get(i) ) != 0 ) {
					match = false;
					break;
				}
			}
		}
		else {
			match = false;
		}
		
		return match;
	}
	
	/**
	 * Converts a length measurement to a length in metres.
	 * 
	 * @param length Length measurement to be converted
	 * @param units Units of the length measurement as provided
	 * @return Length in units of metres
	 */
	public static double lengthToMetres(double length, LengthUnit units) {
		double metreLength = 0;

		switch (units) {
		case Metres :
			metreLength = length;
			break;
		case Feet :
			metreLength = length / feetPerMetre;
			break;
		case Yards :
			metreLength = length * 3 / feetPerMetre;
			break;
		default:
			//In case other units are added to enum, but not coded yet; return zero
			break;
		}

		return metreLength;
	}
	
	/**
	 * Converts the length measurement to a specified unit from specified value in metres.
	 * 
	 * @param metrelength Length measurement in metres to convert to different units
	 * @param units Units to convert length to
	 * @return Length in units specified
	 */
	public static double lengthFromMetres(double metrelength, LengthUnit units) {
		double convertedLength = 0;

		switch (units) {
		case Metres :
			convertedLength = metrelength;
			break;
		case Feet :
			convertedLength = metrelength * feetPerMetre;
			break;
		case Yards :
			convertedLength = metrelength * feetPerMetre / 3;
			break;
		default:
			//In case other units are added to enum, but not coded yet; return zero
			break;
		}

		return convertedLength;
	}

	/**
	 * Converts a bearing measurement to a bearing in degrees.
	 * @param bearing Bearing measurement to be converted
	 * @param units Units of the bearing measurement as provided
	 * @return Bearing in units of degrees
	 */
	public static double bearingToDegrees(double bearing, BearingUnit units) {
		double degrees = -999;

		switch (units) {
		case Degrees :
			degrees = bearing;
			break;
		case Grads :
			degrees = gradsToDegrees( bearing );
			break;
		case Minutes :
			degrees = bearing / 60;
			break;
		default:
			//In case other units are added to enum, but not coded yet; return -999
			break;
		}

		return degrees;
	}

	/**
	 * Converts the bearing measurement to a specified unit from specified value in degrees.
	 * 
	 * @param bearingDegrees Bearing measurement in degrees to convert to different units
	 * @param units Units to convert bearing to
	 * @return Bearing in units specified
	 */
	public static double bearingFromDegrees(double bearingDegrees, BearingUnit units) {
		double convertedBearing = -999;

		switch (units) {
		case Degrees :
			convertedBearing = bearingDegrees;
			break;
		case Grads :
			convertedBearing = degreesToGrads( bearingDegrees );
			break;
		case Minutes :
			convertedBearing = bearingDegrees * 60;
			break;
		default:
			//In case other units are added to enum, but not coded yet; return -999
			break;
		}

		return convertedBearing;
	}

	/**
	 * Converts a gradient (inclination) measurement to an angle in degrees.
	 * @param gradient Inclination measurement to be converted
	 * @param units Units of the inclination measurement as provided
	 * @return Inclination in units of degrees
	 */
	public static double gradientToDegrees(double gradient, GradientUnit units) {
		double degrees = -999;

		switch (units) {
		case Degrees :
			degrees = bearingToDegrees( gradient, BearingUnit.Degrees );
			break;
		case Grads :
			degrees = bearingToDegrees( gradient, BearingUnit.Grads );
			break;
		case Minutes :
			degrees = bearingToDegrees( gradient, BearingUnit.Minutes );
			break;
		case Percent :
			degrees = 180 * ( Math.atan( gradient / 100 ) ) / Math.PI;
			break;
		default:
			//In case other units are added to enum, but not coded yet; return -999
			break;
		}

		return degrees;
	}

	/**
	 * Converts the bearing measurement to a specified unit from specified value in degrees.
	 * 
	 * @param gradientDegrees Inclination measurement in degrees to convert to different units
	 * @param units Units to convert bearing to
	 * @return Bearing in units specified
	 */
	public static double gradientFromDegrees(double gradientDegrees, GradientUnit units) {
		double convertedGradient = -999;

		switch (units) {
		case Degrees :
			convertedGradient = bearingFromDegrees( gradientDegrees, BearingUnit.Degrees );
			break;
		case Grads :
			convertedGradient = bearingFromDegrees( gradientDegrees, BearingUnit.Grads );
			break;
		case Minutes :
			convertedGradient = bearingFromDegrees( gradientDegrees, BearingUnit.Minutes );
			break;
		case Percent :
			convertedGradient = 100.0 * Math.tan(Math.PI * gradientDegrees / 180.0);
			break;
		default:
			//In case other units are added to enum, but not coded yet; return -999
			break;
		}

		return convertedGradient;
	}
	
	/**
	 * Determines if a data order list is for diving data or normal
	 * @param checkDataOrder Data order list from a SurveySeries
	 * @return True if the data order list is for diving data, other wise false for normal data
	 */
	public static boolean dataOrderIsDiving(List<String> checkDataOrder) {
		boolean isDiving = false;
		if ( checkDataOrder != null && 
				( checkDataOrder.contains("DEPTHCHANGE") || checkDataOrder.contains("FROMDEPTH") ) ) {
			isDiving = true;
		}
		return isDiving;

	}
	
	private static double gradsToDegrees(double grads) {
		double tmp = 360.0 * grads / 400.0;
		return tmp;
	}

	private static double degreesToGrads(double degrees) {
		return 400.0 * degrees / 360.0;
	}

	/**
	 * Returns the folder location of a file
	 * @param file File to get the folder path from
	 * @return Path to the folder containing the file
	 */
	private static String folderFromFile( File file ) {
		String path = file.getPath();
		String folderPath = ".";
		String folderSep = File.separator;
		
		int splitPos = path.lastIndexOf( folderSep );
		if ( splitPos >= 0 ) {
			folderPath = path.substring( 0, splitPos );
		}
		
		return folderPath;
	}
	
}
