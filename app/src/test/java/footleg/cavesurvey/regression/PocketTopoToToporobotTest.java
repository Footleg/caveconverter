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
import footleg.cavesurvey.tools.TestHelper;

/**
 * Regression test class which runs data file conversions and compares the output with reference
 * files. Tests pass if the output file is identical to the reference file. A file name prefix
 * is given as the argument for each test conversion. The test will look for an input file with
 * this prefix and the suffix '_in.txt' and compare the output with a reference file with
 * this prefix and the suffix '_ref.text'.
 *  
 * @author      Footleg
 * @version     2016.12.14                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class PocketTopoToToporobotTest {

	@Test
	public void testPocketTopoToToporobotGourAven() throws ParseException, FileNotFoundException {
		//Convert PocketTopo text export file to Toporobot format data
		String fileCompare = RunFileConverter.convertPocketTopoToToporobot( "GourAven", CmdlineSplaysOpt.Default, CmdlineOpt.T, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing GourAven toporobot file from PocketTopo with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testPocketTopoToToporobotHSC() throws ParseException, FileNotFoundException {
		//Convert PocketTopo text export file to Toporobot format data
		String fileCompare = RunFileConverter.convertPocketTopoToToporobot( "HSC", CmdlineSplaysOpt.Default, CmdlineOpt.T, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing HSC toporobot file from PocketTopo with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testPocketTopoToToporobotStomps() throws ParseException, FileNotFoundException {
		//Convert PocketTopo text export file to Toporobot format data
		String fileCompare = RunFileConverter.convertPocketTopoToToporobot( "Stomps", CmdlineSplaysOpt.Default, CmdlineOpt.T, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing Stomps toporobot file from PocketTopo with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);	
	}

	@Test
	public void testPocketTopoToToporobotUzuGour() throws ParseException, FileNotFoundException {
		//Convert PocketTopo text export file to Toporobot format data
		String fileCompare = RunFileConverter.convertPocketTopoToToporobot( "Uzu-Gour", CmdlineSplaysOpt.Default, CmdlineOpt.T, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing Uzu-Gour toporobot file from PocketTopo with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);
	}

	@Test
	public void testPocketTopoToToporobotSloppy2ZigZags() throws ParseException, FileNotFoundException {
		//Test for older JRE as test result is different due to JRE rounding bug
		boolean oldJRE = TestHelper.jreRoundingBug();
		RunFileConverter.setOldJRE(oldJRE);
		//Convert PocketTopo text export file to Toporobot format data
		String fileCompare = RunFileConverter.convertPocketTopoToToporobot( "Sloppy2ZigZags", CmdlineSplaysOpt.Default, CmdlineOpt.T, 
				RunFileConverter.defaultCharset );
		//Reset oldJRE flag for subsequent tests
		RunFileConverter.setOldJRE(false);
		assertEquals("Comparing Sloppy2ZigZags toporobot file from PocketTopo with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);	
	}

	@Test
	public void testPocketTopoToToporobotTripComment() throws ParseException, FileNotFoundException {
		//Convert PocketTopo text export file to Toporobot format data
		String fileCompare = RunFileConverter.convertPocketTopoToToporobot( "TripComment", CmdlineSplaysOpt.Default, CmdlineOpt.T, 
				RunFileConverter.defaultCharset );
		assertEquals("Comparing TripComment toporobot file from PocketTopo with reference.", DiffFiles.FILES_IDENTICAL, fileCompare);	
	}

}
