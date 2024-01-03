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
import footleg.cavesurvey.tools.TestHelper;

/**
 * Regression test class which runs data file conversions and compares the
 * output with reference
 * files. Tests pass if the output file is identical to the reference file. A
 * file name prefix
 * is given as the argument for each test conversion. The test will look for an
 * input file with
 * this prefix and the suffix '_in.txt' and compare the output with a reference
 * file with
 * this prefix and the suffix '_ref.svx'.
 * 
 * @author Footleg
 * @version 2024.01.03 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class PocketTopoToSurvexTest {

	@Test
	public void testPocketTopoToSurvexGourAven() throws ParseException, FileNotFoundException {
		// Test for older JRE as test result is different due to JRE rounding bug
		boolean oldJRE = TestHelper.jreRoundingBug();
		RunFileConverter.setOldJRE(oldJRE);
		// Convert PocketTopo text export file to Survex format data
		String fileCompare = RunFileConverter.convertPocketTopoToSurvex("GourAven", CmdlineSplaysOpt.Default,
				CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		// Reset oldJRE flag for subsequent tests
		RunFileConverter.setOldJRE(false);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing GourAven survex file from PocketTopo with reference.");
	}

	@Test
	public void testPocketTopoToSurvexHSC() throws ParseException, FileNotFoundException {
		// Convert PocketTopo text export file to Survex format data
		String fileCompare = RunFileConverter.convertPocketTopoToSurvex("HSC", CmdlineSplaysOpt.Default, CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing HSC survex file from PocketTopo with reference.");
	}

	@Test
	public void testPocketTopoToSurvexStomps() throws ParseException, FileNotFoundException {
		// Test for older JRE as test result is different due to JRE rounding bug
		boolean oldJRE = TestHelper.jreRoundingBug();
		RunFileConverter.setOldJRE(oldJRE);
		// Convert PocketTopo text export file to Survex format data
		String fileCompare = RunFileConverter.convertPocketTopoToSurvex("Stomps", CmdlineSplaysOpt.Default,
				CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		// Reset oldJRE flag for subsequent tests
		RunFileConverter.setOldJRE(false);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing Stomps survex file from PocketTopo with reference.");
	}

	@Test
	public void testPocketTopoToSurvexUzuGour() throws ParseException, FileNotFoundException {
		// Convert PocketTopo text export file to Survex format data
		String fileCompare = RunFileConverter.convertPocketTopoToSurvex("Uzu-Gour", CmdlineSplaysOpt.Default,
				CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing Uzu-Gour survex file from PocketTopo with reference.");
	}

	@Test
	public void testPocketTopoToSurvexSwil20120909() throws ParseException, FileNotFoundException {
		// Test for older JRE as test result is different due to JRE rounding bug
		boolean oldJRE = TestHelper.jreRoundingBug();
		RunFileConverter.setOldJRE(oldJRE);
		// Convert PocketTopo text export file to Survex format data
		String fileCompare = RunFileConverter.convertPocketTopoToSurvex("Swil20120909", CmdlineSplaysOpt.Default,
				CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		// Reset oldJRE flag for subsequent tests
		RunFileConverter.setOldJRE(false);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing Swil20120909 survex file from PocketTopo with reference.");
	}

	@Test
	public void testPocketTopoToSurvexSloppy2ZigZags() throws ParseException, FileNotFoundException {
		// Test for older JRE as test result is different due to JRE rounding bug
		boolean oldJRE = TestHelper.jreRoundingBug();
		RunFileConverter.setOldJRE(oldJRE);
		// Convert PocketTopo text export file to Survex format data
		String fileCompare = RunFileConverter.convertPocketTopoToSurvex("Sloppy2ZigZags", CmdlineSplaysOpt.Default,
				CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		// Reset oldJRE flag for subsequent tests
		RunFileConverter.setOldJRE(false);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing Sloppy2ZigZags survex file from PocketTopo with reference.");
	}

	@Test
	public void testPocketTopoToSurvexT_LRUD() throws ParseException, FileNotFoundException {
		// Test for older JRE as test result is different due to JRE rounding bug
		boolean oldJRE = TestHelper.jreRoundingBug();
		RunFileConverter.setOldJRE(oldJRE);

		// Convert PocketTopo text export file to Survex format data
		/*
		 * This was a particularly difficult case for LRUD generation in Survex as
		 * reverse legs get put
		 * into a passage data block after the first leg in the passage data was
		 * forwards, so the rest
		 * of the block got built with all the LRUD backwards. This case is now fixed
		 * and it detects the
		 * 2nd leg going into the block does not follow the leg already added and put it
		 * before that leg
		 * in the block, resulting the data being in the correct order.
		 */
		String fileCompare = RunFileConverter.convertPocketTopoToSurvex("T_LRUD", CmdlineSplaysOpt.Default,
				CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		// Reset oldJRE flag for subsequent tests
		RunFileConverter.setOldJRE(false);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing T_LRUD survex file from PocketTopo with reference.");
	}

	@Test
	public void testPocketTopoToSurvexTripComment() throws ParseException, FileNotFoundException {
		// Test for older JRE as test result is different due to JRE rounding bug
		boolean oldJRE = TestHelper.jreRoundingBug();
		RunFileConverter.setOldJRE(oldJRE);
		// Convert PocketTopo text export file to Survex format data
		String fileCompare = RunFileConverter.convertPocketTopoToSurvex("TripComment", CmdlineSplaysOpt.Default,
				CmdlineOpt.T,
				RunFileConverter.defaultCharset);
		// Reset oldJRE flag for subsequent tests
		RunFileConverter.setOldJRE(false);
		assertEquals(DiffFiles.FILES_IDENTICAL, fileCompare,
				"Comparing TripComment survex file from PocketTopo with reference.");
	}

}
