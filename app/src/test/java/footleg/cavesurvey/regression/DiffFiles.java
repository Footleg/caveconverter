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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import footleg.cavesurvey.converter.CmdLineLogger;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * File differencing class which checks the contents of two files for any difference.
 * Used for regressions tests.
 *  
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public final class DiffFiles {
	final static String FILES_IDENTICAL = "IDENTICAL";

	public static String diffFiles(String referenceFile, String testFile) throws FileNotFoundException {
		String result = FILES_IDENTICAL;
		CmdLineLogger logger = new CmdLineLogger();
		
		List<String> original = UtilityFunctions.readTextFile( new File( referenceFile ), 
				RunFileConverter.defaultCharset, false, null, logger );
		List<String> revised  = UtilityFunctions.readTextFile( new File( testFile ), 
				RunFileConverter.defaultCharset, false, null, logger );
		
		//Throw exception if either file data array is zero length (as this indicates the file was not found)
		if ( original.size() == 0 ) {
			throw new FileNotFoundException("Reference data file '" + referenceFile + "' not found (or was empty).");
		}
		if ( revised.size() == 0 ) {
			throw new FileNotFoundException("Output data file '" + revised + "' not found (or was empty).");
		}
		
		
		int shortestFileLength = original.size();
		if (revised.size() < shortestFileLength ) {
			shortestFileLength = revised.size();
		}
		
		//Check for differences in file lines
		for (int i = 0; i < shortestFileLength; i++) {
			String ori = original.get(i);
			String rev = revised.get(i);
			if ( ori.equals( rev ) == false ) {
				//Found a difference, so report lines and exit loop
				result = "Files differ at line " + (i + 1) + 
						 ". Reference line: '" + original.get(i) + "'; Generated line: '" + revised.get(i) + "'.";
				break;
			}
		}
		
		//Check for different file lengths if no line differences found
		if ( ( original.size() != revised.size() ) && ( result.equals(FILES_IDENTICAL) ) ) {
			result = "Files are different lengths. " + 
					 "Reference file length: " + original.size() + "; Generated file length: " + revised.size() + ".";
		}
		
		return result;
		
	}

}
