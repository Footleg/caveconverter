/**
 * Copyright (C) 2009-2015 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
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

import static org.junit.Assert.*;

import org.junit.Test;

import footleg.cavesurvey.converter.CaveConverter.SurveyDataInputFormats;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataOutputFormats;

/**
 * Unit test class for {@link footleg.cavesurvey.converter.CaveConverter}
 * 
 * @author      Footleg
 * @version     2015.08.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class CaveConverterTest {

	/**
	 * Test method for {@link footleg.cavesurvey.converter.CaveConverter#getFormatName(SurveyDataInputFormats)}.
	 */
	@Test
	public void testGetFormatNameSurveyDataInputFormats() {
		String testCompassDisplayName = CaveConverter.getFormatName( SurveyDataInputFormats.Compass );
		assertEquals("Test correct display name is returned for format", "Compass", testCompassDisplayName);

		String testDXFDisplayName = CaveConverter.getFormatName( SurveyDataInputFormats.DXF );
		assertEquals("Test correct display name is returned for format", "DXF", testDXFDisplayName);

		String testPocketTopoDisplayName = CaveConverter.getFormatName( SurveyDataInputFormats.PocketTopo );
		assertEquals("Test correct display name is returned for format", "PocketTopo", testPocketTopoDisplayName);

		String testSurvexDisplayName = CaveConverter.getFormatName( SurveyDataInputFormats.Survex );
		assertEquals("Test correct display name is returned for format", "Survex", testSurvexDisplayName);
	}

	/**
	 * Test method for {@link footleg.cavesurvey.converter.CaveConverter#getFormatName(SurveyDataOutputFormats)}.
	 */
	@Test
	public void testGetFormatNameSurveyDataOutputFormats() {
		String testSurvexDisplayName = CaveConverter.getFormatName( SurveyDataOutputFormats.Survex );
		assertEquals("Test correct display name is returned for format", "Survex", testSurvexDisplayName);

		String testToporobotDisplayName = CaveConverter.getFormatName( SurveyDataOutputFormats.Toporobot );
		assertEquals("Test correct display name is returned for format", "Toporobot", testToporobotDisplayName);
	}

	/**
	 * Test method for {@link footleg.cavesurvey.converter.CaveConverter#getFormatName(SurveyDataOutputFormats)}.
	 */
	@Test
	public void testInputFormatFromFileExtn() {
		//Compass files
		SurveyDataInputFormats testCompassL = CaveConverter.inputFormatFromFileExtn( "dat" );
		assertEquals("Test correct format is returned for Compass DAT file (lowercase extn)", SurveyDataInputFormats.Compass, testCompassL);
		
		SurveyDataInputFormats testCompassU = CaveConverter.inputFormatFromFileExtn( "DAT" );
		assertEquals("Test correct format is returned for Compass DAT file (uppercase extn)", SurveyDataInputFormats.Compass, testCompassU);
		
		SurveyDataInputFormats testCompassM = CaveConverter.inputFormatFromFileExtn( "dAt" );
		assertEquals("Test correct format is returned for Compass DAT file (mixedcase extn)", SurveyDataInputFormats.Compass, testCompassM);
		
		//DXF files
		SurveyDataInputFormats testDXFL = CaveConverter.inputFormatFromFileExtn( "dxf" );
		assertEquals("Test correct format is returned for DXF DAT file (lowercase extn)", SurveyDataInputFormats.DXF, testDXFL);
		
		SurveyDataInputFormats testDXFU = CaveConverter.inputFormatFromFileExtn( "DXF" );
		assertEquals("Test correct format is returned for DXF DAT file (uppercase extn)", SurveyDataInputFormats.DXF, testDXFU);
		
		SurveyDataInputFormats testDXFM = CaveConverter.inputFormatFromFileExtn( "dXf" );
		assertEquals("Test correct format is returned for DXF DAT file (mixedcase extn)", SurveyDataInputFormats.DXF, testDXFM);
		
		//PocketTopo files
		SurveyDataInputFormats testPocketTopoL = CaveConverter.inputFormatFromFileExtn( "txt" );
		assertEquals("Test correct format is returned for PocketTopo DAT file (lowercase extn)", SurveyDataInputFormats.PocketTopo, testPocketTopoL);
		
		SurveyDataInputFormats testPocketTopoU = CaveConverter.inputFormatFromFileExtn( "TXT" );
		assertEquals("Test correct format is returned for PocketTopo DAT file (uppercase extn)", SurveyDataInputFormats.PocketTopo, testPocketTopoU);
		
		SurveyDataInputFormats testPocketTopoM = CaveConverter.inputFormatFromFileExtn( "tXt" );
		assertEquals("Test correct format is returned for PocketTopo DAT file (mixedcase extn)", SurveyDataInputFormats.PocketTopo, testPocketTopoM);
		
		//Survex files
		SurveyDataInputFormats testSurvexL = CaveConverter.inputFormatFromFileExtn( "svx" );
		assertEquals("Test correct format is returned for Survex DAT file (lowercase extn)", SurveyDataInputFormats.Survex, testSurvexL);
		
		SurveyDataInputFormats testSurvexU = CaveConverter.inputFormatFromFileExtn( "SVX" );
		assertEquals("Test correct format is returned for Survex DAT file (uppercase extn)", SurveyDataInputFormats.Survex, testSurvexU);
		
		SurveyDataInputFormats testSurvexM = CaveConverter.inputFormatFromFileExtn( "sVx" );
		assertEquals("Test correct format is returned for Survex DAT file (mixedcase extn)", SurveyDataInputFormats.Survex, testSurvexM);
		
		//Unknown
		SurveyDataInputFormats testUnknown = CaveConverter.inputFormatFromFileExtn( "xml" );
		assertEquals("Test null returned for unsupported file extn", null, testUnknown);
	}

}
