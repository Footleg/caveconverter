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
package footleg.cavesurvey.converter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.reader.CompassParser;
import footleg.cavesurvey.data.reader.DxfParser;
import footleg.cavesurvey.data.reader.PocketTopoParser;
import footleg.cavesurvey.data.reader.SurvexParser;
import footleg.cavesurvey.data.writer.SurvexWriter;
import footleg.cavesurvey.data.writer.TopoRobotWriter;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Command line application for converting cave survey data from one file format to another.
 * Currently it can read some survex files, text export files from PocketTopo and DXF format
 * data. It can write toporobot and survex files.
 *  
 * @author      Footleg
 * @version     2017.01.12                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 * @to.do
 * TODO Write Therion reader and writer
 */
public class CaveConverter {
	private static final String filePath = "./";
	private static Date today;
	
	public static String newline = System.getProperty("line.separator");
	
	public static final String DATA_ORDER_CAT_FROMSTN = "FROM";
	public static final String DATA_ORDER_CAT_TOSTN = "TO";
	public static final String DATA_ORDER_CAT_LENGTH = "LENGTH";
	public static final String DATA_ORDER_CAT_BEARING = "BEARING";
	public static final String DATA_ORDER_CAT_CLINO = "GRADIENT";
	public static final String DATA_ORDER_CAT_IGNOREALL = "IGNOREALL";
	public static final String DATA_ORDER_CAT_FROMDEPTH = "FROMDEPTH";
	public static final String DATA_ORDER_CAT_TODEPTH = "TODEPTH";
	public static final String DATA_ORDER_CAT_DEPTHCHANGE = "DEPTHCHANGE";


	/**
	 * Command line options for True, False or 'Use Default'
	 */
	public static enum CmdlineOpt {
	    T, F, D 
	}

	/**
	 * Command line options for splays output
	 */
	public static enum CmdlineSplaysOpt {
	    Named, None, Default, Anon
	}

	/**
	 * Supported cave survey data formats for input (these are the formats the application can read)
	 */
	public static enum SurveyDataInputFormats {
		Compass,
		DXF,
		PocketTopo,
	    Survex
	}
	
	/**
	 * Supported cave survey data formats for output (these are the formats the application can write)
	 */
	public static enum SurveyDataOutputFormats {
	    Survex,
		Toporobot
	}

	private static String dataFormats[][] = {{"Compass","dat"},{"DXF","dxf"},{"PocketTopo","txt"},{"Survex","svx"},{"Toporobot","text"}};

	/**
	 * Units for length measurements
	 */
	public static enum LengthUnit {
	    Metres,
	    Feet,
	    Yards
	}

	/**
	 * Units for bearing measurements
	 */
	public static enum BearingUnit {
	    Degrees,
	    Grads,
	    Minutes
	}

	/**
	 * Units for gradient measurements
	 */
	public static enum GradientUnit {
	    Degrees,
	    Grads,
	    Minutes,
	    Percent
	}
	
	/**
	 * Supplies the display name for survey data input formats
	 * @param format The data format enum value
	 * @return Display name for this data format
	 */
	public static String getFormatName(SurveyDataInputFormats format) {
		String formatName = "";
		switch (format) {
		case Compass:
			formatName = dataFormats[0][0];
			break;
		case DXF:
			formatName = dataFormats[1][0];
			break;
		case PocketTopo:
			formatName = dataFormats[2][0];
			break;
		case Survex:
			formatName = dataFormats[3][0];
			break;
		}
		return formatName;
	}
	
	/**
	 * Supplies the display name for survey data output formats
	 * @param format The data format enum value
	 * @return Display name for this data format
	 */
	public static String getFormatName(SurveyDataOutputFormats format) {
		String formatName = "";
		switch (format) {
		case Survex:
			formatName = dataFormats[3][0];
			break;
		case Toporobot:
			formatName = dataFormats[4][0];
			break;
		}
		return formatName;
	}
	
	/**
	 * Returns the file format for a specified file name extension
	 * @param fileExtn The filename extension 
	 * @return The survey data input format matching the file extn (or null if no match)
	 */
	public static SurveyDataInputFormats inputFormatFromFileExtn( String fileExtn ) {
		SurveyDataInputFormats format = null;
		
		if ( fileExtn.equalsIgnoreCase( dataFormats[0][1] ) ) {
			format = SurveyDataInputFormats.Compass;
		}
		else if ( fileExtn.equalsIgnoreCase( dataFormats[1][1] ) ) {
			format = SurveyDataInputFormats.DXF;
		}
		else if ( fileExtn.equalsIgnoreCase( dataFormats[2][1] ) ) {
			format = SurveyDataInputFormats.PocketTopo;
		}
		else if ( fileExtn.equalsIgnoreCase( dataFormats[3][1] ) ) {
			format = SurveyDataInputFormats.Survex;
		}
		
		return format;
	}
	
    /**
	 * @param args Command line arguments
	 */
	public static void main( String[] args ) {
		//Default input and output filenames
		String inputFilename = "input.svx";
		String outputFilename = "output.text";
		SurveyDataInputFormats inputFormat = null;
		SurveyDataOutputFormats outputFormat = null;
		CmdLineLogger logger = new CmdLineLogger();
		
		//Set date if not already set
		if ( today == null ) {
			//Set date to todays datetime
			Calendar cal = Calendar.getInstance(TimeZone.getDefault());
			today = cal.getTime();
		}
		//Check for arguments
		if (args.length > 0) {
			//Set input filename from 1st argument
			inputFilename = args[0];
			if (args.length > 1) {
				//Set output filename from 2nd argument
				outputFilename = args[1];
				if (args.length > 2) {
					//Set input format from 3rd argument
					try {
						inputFormat = UtilityFunctions.inputDataFormatFromLetterCode( parseSingleCharacterArgument( args[2] ) );
						if (args.length > 3) {
							//Set output format from 4th argument
							outputFormat = UtilityFunctions.outputDataFormatFromLetterCode( parseSingleCharacterArgument( args[3] ) );
							
							//Set options flags from 5th argument onwards
							CmdlineSplaysOpt splaysOpt = CmdlineSplaysOpt.Default;
							boolean anonSplaysOpt = false;
							CmdlineOpt genLRUDOpt = CmdlineOpt.F;
							String charSetEncoding = "UTF8";
							for (int iOpts = 4; iOpts < args.length; iOpts++ ) {
								if ( args[iOpts].compareToIgnoreCase("nosplays") == 0 ) {
									splaysOpt = CmdlineSplaysOpt.None;
								}
								else if ( args[iOpts].compareToIgnoreCase("splays") == 0 ) {
									splaysOpt = CmdlineSplaysOpt.Named;
								}
								if ( args[iOpts].compareToIgnoreCase("anonsplays") == 0 ) {
									anonSplaysOpt = true;
								}
								if ( args[iOpts].compareToIgnoreCase("lrud") == 0 ) {
									genLRUDOpt = CmdlineOpt.T;
								}
								if ( args[iOpts].compareToIgnoreCase("charset") == 0 ) {
									if ( iOpts + 1 < args.length ) {
										charSetEncoding = args[iOpts + 1];
									}
								}
							}

							//Set splays to anonymous if option was set and splays are being output
							if ( ( splaysOpt != CmdlineSplaysOpt.None ) && ( anonSplaysOpt == true ) ) {
								splaysOpt = CmdlineSplaysOpt.Anon;
							}
							//Call file convert method
							convertFile(inputFilename, outputFilename, inputFormat, outputFormat, splaysOpt, 
									genLRUDOpt, charSetEncoding, logger );
							
							//Write log file
							logger.writeLogToFile(  filePath + "CaveConverter.log", charSetEncoding );
						}
					} catch (ParseException e) {
						//Log error
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void setToday(Date date) {
		CaveConverter.today = date;
	}

	/**
	 * Parses a string argument into a single character.
	 *
	 * @param  argument String to be converting into a single character
	 * @return The char which was contained in the string argument
	 * @throws ParseException If the argument is not a single character length string
	 */
	private static char parseSingleCharacterArgument( String argument ) throws ParseException {
		char[] chars = argument.toCharArray();
		char argChar = 'x';
		
		if ( chars.length == 1 ) {
			argChar = chars[0];
		}
		else {
			ParseException e = new ParseException("Failed to parse argument to single character. " +
				"Argument was '" + argument + "'.", 1);
			throw e;
		}
		
		return argChar;
	}
	
	/**
	 * Reads in a survey data file and converts it to another format which is written out to a file.
	 *
	 * @param  inputFilename   Name and location of file to be converted
	 * @param  outputFilename  Name and location of file to be output
	 * @param  inputFormat     Format of input file
	 * @param  outputFormat    Format of file to be generated
	 * @param  splaysOpt       Indicates if a splays output option was set to True, False or to use default for writer (T,F,D)
	 * @param  generateLRUDOpt Indicates whether to generate LRUD data from splays before writing out data
	 * @param  charSetEncoding Character set encoding to be used when reading and writing files
	 * @param  logger          Logging class to output information, warning and error messages to
	 * @throws ParseException  Exception raised when information in a survey data file is not supported or valid for the format
	 */
	public static void convertFile( String inputFilename, String outputFilename, SurveyDataInputFormats inputFormat, 
			SurveyDataOutputFormats outputFormat, CmdlineSplaysOpt splaysOpt, CmdlineOpt generateLRUDOpt, 
			String charSetEncoding, Logger logger ) throws ParseException {
		//Declare structure to hold survey data
		CaveSurvey surveyData = null;
		List<String> multiFileRefs = null;
		boolean multifile = false;
		
		//Prepare log message
		String msg = "Reading data file '" + inputFilename + "' with format " + getFormatName( inputFormat )
			+ ". Splays option: ";
		switch ( splaysOpt ) {
		case None:
			msg += "None";
			break;
		case Anon:
			msg += "Anonymous";
			break;
		case Default:
			msg += "Default";
			break;
		case Named:
			msg += "Named to Stations";
			break;
		}
		if ( inputFormat == SurveyDataInputFormats.Survex ) {
			multifile = true;
			multiFileRefs = new ArrayList<String>();
		}
		//Read input data file
		logger.logMessage( msg );
		List<String> fileData = UtilityFunctions.readTextFile( new File( inputFilename ), charSetEncoding,
				multifile, multiFileRefs, logger );
		
		//Parse file data
		if ( inputFormat == SurveyDataInputFormats.Survex ) {
			//Parse Survex data
			SurvexParser parser = new SurvexParser(logger);
			surveyData = parser.parseFile( fileData, multiFileRefs );
		}
		else if ( inputFormat == SurveyDataInputFormats.PocketTopo ) {
			//Parse PocketTopo data
			PocketTopoParser parser = new PocketTopoParser(logger);
			surveyData = parser.parseFile( fileData );
		}
		else if ( inputFormat == SurveyDataInputFormats.DXF ) {
			//Parse Autocad DXF data polylines into survey series
			DxfParser parser = new DxfParser(logger);
			surveyData = parser.parseFile(fileData, 0);
		}
		else if ( inputFormat == SurveyDataInputFormats.Compass ) {
			//Parse Compass data file
			CompassParser parser = new CompassParser(logger);
			surveyData = parser.parseFile(fileData);
		}
		else {
			//Unsupported input format argument
			logger.logMessage("Unsupported input format argument: " + inputFormat + " is not a valid input format.");
		}
		
		//Set options flag for generating LRUD data
		boolean generateLRUD = false;
		if (generateLRUDOpt == CmdlineOpt.T) {
			generateLRUD = true;
		}
		
		//Generate LRUD data if required
		if ( generateLRUD ) {
			logger.logMessage("Generating LRUD data from splays...");
			surveyData.generateLRUDfromSplays();
		}
		
		//Convert data to output format
		List<String> outputData = null;
		if ( surveyData != null ) {
			if ( outputFormat == SurveyDataOutputFormats.Survex ) {
				//Set options flag for splays (default to true)
				SurvexWriter.SplayFormats outputSplays = SurvexWriter.SplayFormats.Flagged;
				if ( splaysOpt == CmdlineSplaysOpt.None ) {
					outputSplays = SurvexWriter.SplayFormats.None;
				}
				else if ( splaysOpt == CmdlineSplaysOpt.Anon ) {
					outputSplays = SurvexWriter.SplayFormats.Anonymous;
				}
				//Generate Survex format data
				SurvexWriter writer = new SurvexWriter(logger);
				outputData = writer.generateSurvexData( surveyData, outputSplays );
			}
			else if ( outputFormat == SurveyDataOutputFormats.Toporobot ) {
				//Set options flag for splays (default to false)
				boolean outputSplays = false;
				if ( ( splaysOpt == CmdlineSplaysOpt.Named ) ||  ( splaysOpt == CmdlineSplaysOpt.Anon ) ) {
					outputSplays = true;
				}
				//Generate Toporobot format data
				TopoRobotWriter writer = new TopoRobotWriter(logger);
				outputData = writer.generateToporobotData( surveyData, today, outputSplays );
			}
//			else if ( outputFormat == SurveyDataOutputFormats.Compass ) {
//				//Generate Compass format data
//				CompassWriter writer = new CompassWriter();
//				outputData = writer.generateCompassData( surveyData );
//			}
			else {
				//Unsupported output format argument
				logger.logMessage("Unsupported output format argument: " + outputFormat + " is not a valid output format.");
			}
		}
		
		//Write output file
		if ( outputData.size() > 0 ) {
			String outputFilePath = filePath + outputFilename;
			logger.logMessage( "Writing output file: " + outputFilePath );
			String error = UtilityFunctions.writeTextFile( outputData, outputFilePath, charSetEncoding );
			if ( error.length() > 0 ) {
				logger.logMessage( error );
			}
		}
	}
	
	public static String padNumber(int num, int padWidth) {
		String numStr = "" + num;
		
		return padString(numStr,padWidth);
	}
	
	/**
	 * Generates a string from a number with specified number of decimal places and padded with spaces
	 * to a specified length string
	 * @param num Number to convert to string
	 * @param decPlaces Number of decimal places to represent the number to
	 * @param padWidth Minimum length of string to return
	 * @return Formatted string representation of number
	 */
	public static String padNumber(double num, int decPlaces, int padWidth) {
		String formatStr = "0.";
		for (int i = 0; i < decPlaces; i++) {
			formatStr += '0';
		}
		DecimalFormat formatter = new DecimalFormat(formatStr);
		
		return padString(formatter.format(num), padWidth);
	}

	/**
	 * Prefixes a string with space characters to make the string a minimum length if
	 * the string is shorter than the specified minimum length
	 * 
	 * @param data The string to pad with spaces
	 * @param padWidth The minimum length the string should have
	 * @return The string padded with space characters to make it at least the specified length
	 */
	public static String padString(String data, int padWidth) {
		String padded;
		//Pad number with spaces if not already as wide as pad width
		if ( data.length() < padWidth ) {
			//Pad with lots of spaces
			padded = "                    " + data;
			//then trim to padded width
			padded = padded.substring(padded.length() - padWidth);
		}
		else {
			//Already wide enough, so just return as is
			padded = data;
		}
		
		return padded;
	}
	
}
