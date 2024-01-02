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
package footleg.cavesurvey.regression;

import java.io.FileNotFoundException;
import java.text.ParseException;

import footleg.cavesurvey.converter.CaveConverter;
import footleg.cavesurvey.converter.CaveConverter.CmdlineOpt;
import footleg.cavesurvey.converter.CaveConverter.CmdlineSplaysOpt;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataInputFormats;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataOutputFormats;
import footleg.cavesurvey.converter.CmdLineLogger;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Regression helper class which runs data file conversions and compares the output with reference
 * files. Tests pass if the output file is identical to the reference file. A file name prefix
 * is given as the argument for each test conversion. The test will look for an input file with
 * this prefix and the suffix '_in._extn_' and compare the output with a reference file with
 * this prefix and the suffix '_ref._extn_' (where _extn_ is the appropriate file extension for
 * the file format.
 *  
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public final class RunFileConverter {
	final static String refFilesLocation = "./test/data/regression/";
	final static String genFilesLocation = "./build/test/data/";
	final static String defaultCharset = "UTF8";
	
	private static boolean oldJRE = false;

	private static final char compassFormat = 'c';
	private static final char dxfFormat = 'd';
	private static final char pocketTopoFormat = 'p';
	private static final char survexFormat = 's';
	private static final char toporobotFormat = 't';

	public static void setOldJRE(boolean oldJRE) {
		RunFileConverter.oldJRE = oldJRE;
	}

	public static String convertCompassToSurvex( String filePrefix, CmdlineSplaysOpt splaysOpt, CmdlineOpt genLRUDOpt, 
			String charSetEncoding ) throws ParseException, FileNotFoundException {
		//Compare resulting file with expected file
		String fileCompare = convertFile( filePrefix, SurveyDataInputFormats.Compass, SurveyDataOutputFormats.Survex, 
				splaysOpt, genLRUDOpt, charSetEncoding, new CmdLineLogger() );
		return fileCompare;
	}

	public static String convertDxfToSurvex( String filePrefix, CmdlineSplaysOpt splaysOpt, CmdlineOpt genLRUDOpt, 
			String charSetEncoding ) throws ParseException, FileNotFoundException {
		//Compare resulting file with expected file
		String fileCompare = convertFile( filePrefix, SurveyDataInputFormats.DXF, SurveyDataOutputFormats.Survex, 
				splaysOpt, genLRUDOpt, charSetEncoding, new CmdLineLogger() );
		return fileCompare;
	}
	
	public static String convertSurvexToSurvex( String filePrefix, CmdlineSplaysOpt splaysOpt, CmdlineOpt genLRUDOpt, 
			String charSetEncoding ) throws ParseException, FileNotFoundException {
		//Compare resulting file with expected file
		String fileCompare = convertFile( filePrefix, SurveyDataInputFormats.Survex, SurveyDataOutputFormats.Survex, 
				splaysOpt, genLRUDOpt, charSetEncoding, new CmdLineLogger() );
		return fileCompare;
	}
	
	public static String convertSurvexToTopoRobot( String filePrefix, CmdlineSplaysOpt splaysOpt, CmdlineOpt genLRUDOpt, 
			String charSetEncoding ) throws ParseException, FileNotFoundException {
		//Compare resulting file with expected file
		String fileCompare = convertFile( filePrefix, SurveyDataInputFormats.Survex, SurveyDataOutputFormats.Toporobot, 
				splaysOpt, genLRUDOpt, charSetEncoding, new CmdLineLogger() );
		return fileCompare;
	}
	
	public static String convertDxfToTopoRobot( String filePrefix, CmdlineSplaysOpt splaysOpt, CmdlineOpt genLRUDOpt, 
			String charSetEncoding ) throws ParseException, FileNotFoundException {
		//Compare resulting file with expected file
		String fileCompare = convertFile( filePrefix, SurveyDataInputFormats.DXF, SurveyDataOutputFormats.Toporobot, 
				splaysOpt, genLRUDOpt, charSetEncoding, new CmdLineLogger() );
		return fileCompare;
	}
	
	public static String convertPocketTopoToSurvex( String filePrefix, CmdlineSplaysOpt splaysOpt, CmdlineOpt genLRUDOpt, 
			String charSetEncoding ) throws ParseException, FileNotFoundException {
		//Compare resulting file with expected file
		String fileCompare = convertFile( filePrefix, SurveyDataInputFormats.PocketTopo, SurveyDataOutputFormats.Survex, 
				splaysOpt, genLRUDOpt, charSetEncoding, new CmdLineLogger() );
		return fileCompare;
	}
	
	public static String convertPocketTopoToToporobot( String filePrefix, CmdlineSplaysOpt splaysOpt, CmdlineOpt genLRUDOpt, 
			String charSetEncoding ) throws ParseException, FileNotFoundException {
		//Compare resulting file with expected file
		String fileCompare = convertFile( filePrefix, SurveyDataInputFormats.PocketTopo, SurveyDataOutputFormats.Toporobot, 
				splaysOpt, genLRUDOpt, charSetEncoding, new CmdLineLogger() );
		return fileCompare;
	}
	
	private static String generateFileSuffix( SurveyDataInputFormats format ) {
		String fileSuffix = "";
		
		if( format == SurveyDataInputFormats.PocketTopo ) {
			fileSuffix = ".txt";
		}
		else if( format == SurveyDataInputFormats.DXF ) {
			fileSuffix = ".dxf";
		}
		else if( format == SurveyDataInputFormats.Survex ) {
			fileSuffix = ".svx";
		}
		else if( format == SurveyDataInputFormats.Compass ) {
			fileSuffix = ".dat";
		}
		else {
			fileSuffix = ".txt";
		}
		
		return fileSuffix;
	}
	
	private static String generateFileSuffix( SurveyDataOutputFormats format ) {
		String fileSuffix = "";
		
		if( format == SurveyDataOutputFormats.Survex ) {
			fileSuffix = ".svx";
		}
		else if( format == SurveyDataOutputFormats.Toporobot ) {
			fileSuffix = ".text";
		}
		else {
			fileSuffix = ".svx";
		}
		
		return fileSuffix;
	}
	
	
	private static String generateInputFilename( String filePrefix, SurveyDataInputFormats fromFormat ) {
		return refFilesLocation + filePrefix + "_in" + generateFileSuffix( fromFormat );
	}

	private static char letterCodeFromInputDataFormat( SurveyDataInputFormats format ) {
		char letterCode = 'x'; //Invalid code
		
		switch ( format) {
		case Compass:
			letterCode = compassFormat;
			break;
		case DXF:
			letterCode = dxfFormat;
			break;
		case PocketTopo:
			letterCode = pocketTopoFormat;
			break;
		case Survex:
			letterCode = survexFormat;
			break;
		}
		
		return letterCode;
	}

	private static char letterCodeFromOutputDataFormat( SurveyDataOutputFormats format ) {
		char letterCode = 'x'; //Invalid code
		
		switch ( format) {
		case Survex:
			letterCode = survexFormat;
			break;
		case Toporobot:
			letterCode = toporobotFormat;
			break;
		}
		
		return letterCode;
	}

	private static String generateFilename( String location, String filePrefix, SurveyDataInputFormats fromFormat, SurveyDataOutputFormats toFormat, 
			CmdlineSplaysOpt splaysOpt, String suffix ) {
		String splayOptString = "";
		if ( splaysOpt == CmdlineSplaysOpt.Named ) {
			splayOptString = "_spl";
		}
		else if ( splaysOpt == CmdlineSplaysOpt.None ) {
			splayOptString = "_nsp";
		}
		else if ( splaysOpt == CmdlineSplaysOpt.Anon ) {
			splayOptString = "_asp";
		}
		return location + filePrefix + "_" + letterCodeFromInputDataFormat( fromFormat ) + letterCodeFromOutputDataFormat( toFormat ) + splayOptString + "_" + suffix + generateFileSuffix( toFormat );
	}
	
	private static String generateRefFilename( String filePrefix, SurveyDataInputFormats fromFormat, SurveyDataOutputFormats toFormat, CmdlineSplaysOpt splaysOpt, boolean oldJRE ) {
		String suffix = "ref";
		if ( oldJRE ) {
			suffix += "7";
		}
		return generateFilename( refFilesLocation, filePrefix, fromFormat, toFormat, splaysOpt, suffix );
	}
	
	private static String generateOutputFilename( String filePrefix, SurveyDataInputFormats fromFormat, SurveyDataOutputFormats toFormat, CmdlineSplaysOpt splaysOpt ) {
		return generateFilename( genFilesLocation, filePrefix, fromFormat, toFormat, splaysOpt, "testgen" );
	}

	/**
	 * Sets the date used as 'today' in conversions so that output files match reference files
	 * with dates in them
	 */
	private static void setDateForConversions() {
		//Set date to a fixed date rather than the actual date at runtime so that test output uses a known date
		try {
			CaveConverter.setToday( UtilityFunctions.stringToDate("16/08/2012 13:14:15", "dd/MM/yyyy HH:mm:ss") );
		} catch (ParseException e) {
			//Just print out error (will never happen with hard coded arguments used in method call above)
			e.printStackTrace();
		}
	}
	
	private static String convertFile( String filePrefix, SurveyDataInputFormats fromFormat, SurveyDataOutputFormats toFormat, 
			CmdlineSplaysOpt splaysOpt, CmdlineOpt genLRUDOpt, String charSetEncoding, CmdLineLogger logger ) throws ParseException, FileNotFoundException {
		
		String inputFile = generateInputFilename(filePrefix, fromFormat);
		String outputFile = generateOutputFilename(filePrefix, fromFormat, toFormat, splaysOpt);
		String referenceFile = generateRefFilename(filePrefix, fromFormat, toFormat, splaysOpt, oldJRE);
		
		setDateForConversions();
		 
		//Do file conversion
		CaveConverter.convertFile(inputFile, outputFile, fromFormat, toFormat, splaysOpt, genLRUDOpt, charSetEncoding, logger);

		//Compare resulting file with expected file
		String fileCompare = DiffFiles.diffFiles( referenceFile, outputFile );
		
		return fileCompare;
	}
	
	public static String convertUsingCmdLine( String filePrefix, char fromFormatCode, char toFormatCode, String options ) throws ParseException, FileNotFoundException {
		SurveyDataInputFormats fromFormat = UtilityFunctions.inputDataFormatFromLetterCode( fromFormatCode );
		SurveyDataOutputFormats toFormat = UtilityFunctions.outputDataFormatFromLetterCode( toFormatCode );
		String inputFile = generateInputFilename(filePrefix, fromFormat);
		String outputFile = generateOutputFilename(filePrefix, fromFormat, toFormat, CmdlineSplaysOpt.None);
		String referenceFile = generateRefFilename(filePrefix, fromFormat, toFormat, CmdlineSplaysOpt.None, oldJRE);
		
		//Set file suffix from command line parameters
		String fileSuffix = "_cmd";
		if ( options.contains("nosplays") ) {
			fileSuffix += "n";
		}
		else if ( options.contains("splays") ) {
			fileSuffix += "s";
		}
		if ( options.contains("lrud") ) {
			fileSuffix += "l";
		}
		
		//Change output and ref filenames to cmdline suffixes
		outputFile = outputFile.replace("_nsp", fileSuffix);
		referenceFile = referenceFile.replace("_nsp", fileSuffix);
		
		setDateForConversions();

		String[] opts = options.split(" ");
		int numArgs = 4 + opts.length;
		String[] args = new String[numArgs];
		args[0] = inputFile;
		args[1] = outputFile;
		args[2] = "" + letterCodeFromInputDataFormat( fromFormat );
		args[3] = "" + letterCodeFromOutputDataFormat( toFormat );
		for (int iArg = 0; iArg < opts.length; iArg++ ) {
			args[4 + iArg] = opts[iArg];
		}

		CaveConverter.main(args);
		
		//Compare resulting file with expected file
		String fileCompare = DiffFiles.diffFiles( referenceFile, outputFile );
		
		return fileCompare;
	}
}
