/**
 * Copyright (C) 2024 Paul Fretwell - https://github.com/Footleg/caveconverter
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

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.text.ParseException;

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.converter.CaveConverter.CmdlineOpt;
import footleg.cavesurvey.converter.CaveConverter.CmdlineSplaysOpt;

/**
 * Regression test class which runs data file conversions and compares the
 * output with reference
 * files. Tests pass if the output file is identical to the reference file. A
 * file name prefix
 * is given as the argument for each test conversion. The test will look for an
 * input file with
 * this prefix and the suffix '_in.svx' and compare the output with a reference
 * file with
 * this prefix and the suffix '_ref.svx'.
 * 
 * @author Footleg
 * @version 2024.01.03 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class SurvexToSurvexTest {

	@Test
	public void testNightMare() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data
		String fileCompare = RunFileConverter.convertSurvexToSurvex("NightMare", CmdlineSplaysOpt.Default, CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing NightMare Survex file from Survex with reference.");
	}

	@Test
	public void testCalibrations() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data
		String fileCompare = RunFileConverter.convertSurvexToSurvex("calibrations", CmdlineSplaysOpt.Default,
				CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing calibrations Survex file from Survex with reference.");
	}

	@Test
	public void testDataOrder() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data
		String fileCompare = RunFileConverter.convertSurvexToSurvex("survex_different_data_order",
				CmdlineSplaysOpt.Default,
				CmdlineOpt.F, RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing variations in data order Survex file from Survex with reference.");
	}

	@Test
	public void testNestedDataOrder() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data
		String fileCompare = RunFileConverter.convertSurvexToSurvex("survex_data_order_nested",
				CmdlineSplaysOpt.Default,
				CmdlineOpt.F, RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing variations in nested data order Survex file from Survex with reference.");
	}

	@Test
	public void testFlags() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data
		String fileCompare = RunFileConverter.convertSurvexToSurvex("flags", CmdlineSplaysOpt.Default, CmdlineOpt.F,
				RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare, "Comparing flags Survex file from Survex with reference.");
	}

	@Test
	public void testNestedFlags() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data
		String fileCompare = RunFileConverter.convertSurvexToSurvex("nested_series_flags", CmdlineSplaysOpt.Named,
				CmdlineOpt.F,
				RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing nested flags Survex file from Survex with reference.");
	}

	@Test
	public void testNosurvey() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data
		String fileCompare = RunFileConverter.convertSurvexToSurvex("nosurvey", CmdlineSplaysOpt.Default, CmdlineOpt.F,
				RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing nosurvey Survex file from Survex with reference.");
	}

	@Test
	public void testAnonymousBlocks() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data
		String fileCompare = RunFileConverter.convertSurvexToSurvex("anonymous_blocks", CmdlineSplaysOpt.Named,
				CmdlineOpt.F,
				RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing anonymous blocks Survex file from Survex with reference.");
	}

	@Test
	public void testUnits() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data using different units representations
		String fileCompare = RunFileConverter.convertSurvexToSurvex("units_test", CmdlineSplaysOpt.Default,
				CmdlineOpt.F, RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing all units Survex file from Survex with reference.");
	}

	@Test
	public void testSurvexEverything() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data containing everything a Survex file can
		// contain
		String fileCompare = RunFileConverter.convertSurvexToSurvex("survex_everything", CmdlineSplaysOpt.Anon,
				CmdlineOpt.F, RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing Survex file from Survex file containing everything survex supports with reference.");
	}

	@Test
	public void testSwilEntNosplays() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data, removing splays
		String fileCompare = RunFileConverter.convertUsingCmdLine("SwilEnt", 's', 's', "nosplays lrud");
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing SwilEnt Survex file from Survex with reference, removing splays.");
	}

	@Test
	public void testSwilEntSplaysLRUD() throws ParseException, FileNotFoundException {
		// Read and Write Survex format data, removing splays
		String fileCompare = RunFileConverter.convertUsingCmdLine("SwilEnt", 's', 's', "splays lrud");
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing SwilEnt Survex file from Survex with reference, removing splays.");
	}

}
