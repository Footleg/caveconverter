/**
 * Copyright (C) 2023 Paul Fretwell - https://github.com/Footleg/caveconverter
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
 * this prefix and the suffix '_in.dxf' and compare the output with a reference file with
 * this prefix and the suffix '_ref.dxf'.
 *  
 * @author      Footleg
 * @version     2023.06.22                                (ISO 8601 YYYY.MM.DD)
 * @since       8                                         (The Java version used)
 */
public class CompassToSurvexTest {

	@Test
	public void testCompassToSurvexMinimal() throws ParseException, FileNotFoundException {
		//Convert Compass file with minimal length comments and flags to Survex format data
		String fileCompare = RunFileConverter.convertCompassToSurvex( "Minimal", CmdlineSplaysOpt.Default, CmdlineOpt.F, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing Minimal survex file from Compass with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testCompassToSurvexCalTest() throws ParseException, FileNotFoundException {
		//Convert Compass file with minimal length comments and flags to Survex format data
		String fileCompare = RunFileConverter.convertCompassToSurvex( "CalTest", CmdlineSplaysOpt.Default, CmdlineOpt.F, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing CalTest survex file from Compass with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testCompassToSurvexOldHeadersTest() throws ParseException, FileNotFoundException {
		//Convert Compass file with minimal length comments and flags to Survex format data
		String fileCompare = RunFileConverter.convertCompassToSurvex( "OldCompassHeaders", CmdlineSplaysOpt.Default, CmdlineOpt.F, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing OldCompassHeaders survex file from Compass with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testCompassToSurvexAwkwardCharsANSITest() throws ParseException, FileNotFoundException {
		//Convert ANSI format Compass file with extended character set station names to Survex format data
		String fileCompare = RunFileConverter.convertCompassToSurvex( "AwkwardCharsANSI", CmdlineSplaysOpt.Default, CmdlineOpt.F, 
				"Cp1252" );
		assertEquals("Comparing AwkwardChars survex file from ANSI Compass with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testCompassToSurvexAwkwardCharsUTF8Test() throws ParseException, FileNotFoundException {
		//Convert UTF8 format Compass file with extended character set station names to Survex format data
		String fileCompare = RunFileConverter.convertCompassToSurvex( "AwkwardCharsUTF8", CmdlineSplaysOpt.Default, CmdlineOpt.F, 
				"UTF8" );
		assertEquals("Comparing AwkwardChars survex file from UTF8 Compass with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}
	
	@Test
	public void testCompassToSurvexShortReal() throws ParseException, FileNotFoundException {
		//Convert Compass file with minimal length comments and flags to Survex format data
		String fileCompare = RunFileConverter.convertCompassToSurvex( "ShortRealData", CmdlineSplaysOpt.Default, CmdlineOpt.F, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing Short real data to survex file from real Compass data with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}
	
	@Test
	public void testCompassToSurvexOtterHole() throws ParseException, FileNotFoundException {
		//Convert Compass file to Survex format data
		String fileCompare = RunFileConverter.convertCompassToSurvex( "OtterHole", CmdlineSplaysOpt.Default, CmdlineOpt.F, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing Compass data to survex file from Otter Hole survey data.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

}
