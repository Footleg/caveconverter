/**
 * Copyright (C) 2009-2016 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
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

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.text.ParseException;

import org.junit.Test;

import footleg.cavesurvey.converter.CaveConverter.CmdlineOpt;
import footleg.cavesurvey.converter.CaveConverter.CmdlineSplaysOpt;

/**
 * Regression test class which runs data file conversions and compares the output with reference
 * files. Tests pass if the output file is identical to the reference file. A file name prefix
 * is given as the argument for each test conversion. The test will look for an input file with
 * this prefix and the suffix '_in.svx' and compare the output with a reference file with
 * this prefix and the suffix '_ref.text'.
 *  
 * @author      Footleg
 * @version     2016.12.14                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class SurvexToToporobotTest {

	@Test
	public void testCrossoverCmdline() throws ParseException, FileNotFoundException {
		//Convert Survex data file to Toporobot text format using default settings and test result
		String fileCompare = RunFileConverter.convertUsingCmdLine("Crossover", 's', 't', "");
		assertEquals("Comparing Crossover Toporobot file from Survex with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testFlagsSplaysCmdline() throws ParseException, FileNotFoundException {
		//Convert Survex data file to Toporobot text format retaining splays and test result
		String fileCompare = RunFileConverter.convertUsingCmdLine("flags", 's', 't', "splays");
		assertEquals("Comparing Flags Toporobot file from Survex with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testSwilEnt() throws ParseException, FileNotFoundException {
		//Convert Survex data file to Toporobot text format removing splays but generating LRUD data from them and test result
		String fileCompare = RunFileConverter.convertSurvexToTopoRobot( "SwilEnt", CmdlineSplaysOpt.None, CmdlineOpt.T, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing SwilEnt Toporobot file from Survex with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testCaseInsensitive() throws ParseException, FileNotFoundException {
		//Convert Survex data file to Toporobot text format removing splays but generating LRUD data from them and test result
		String fileCompare = RunFileConverter.convertSurvexToTopoRobot( "CaseInsensitive", CmdlineSplaysOpt.None, CmdlineOpt.T, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing CaseInsensitive Toporobot file from Survex with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

}
