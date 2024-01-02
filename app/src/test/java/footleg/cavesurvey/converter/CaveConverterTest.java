/**
 * Copyright (C) 2018 Paul Fretwell - https://github.com/Footleg/caveconverter
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
 * @version     2018.04.15                                (ISO 8601 YYYY.MM.DD)
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
	 * Test method for {@link footleg.cavesurvey.converter.CaveConverter#inputFormatFromFileExtn(String)}.
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

	/**
	 * Test method for {@link footleg.cavesurvey.converter.CaveConverter#decimalNumberStringEnglish(double)}.
	 */
	@Test
	public void testDecimalNumberStringEnglish() {
		double testNum1 = 0.0;
		String testFormated1 = CaveConverter.decimalNumberStringEnglish(testNum1);
		assertEquals("Test zero double returned as 0.0", "0.0", testFormated1);
				
		double testNum2 = 1.0;
		String testFormated2 = CaveConverter.decimalNumberStringEnglish(testNum2);
		assertEquals("Test whole number double returned as x.y", "1.0", testFormated2);

		double testNum3 = 1.4;
		String testFormated3 = CaveConverter.decimalNumberStringEnglish(testNum3);
		assertEquals("Test 1dp double returned as x.y", "1.4", testFormated3);

		double testNum4 = 1.0001;
		String testFormated4 = CaveConverter.decimalNumberStringEnglish(testNum4);
		assertEquals("Test whole number when rounded to 2dp double returned as x.y", "1.0", testFormated4);

		double testNum5 = 5.304;
		String testFormated5 = CaveConverter.decimalNumberStringEnglish(testNum5);
		assertEquals("Test double number when rounded to 2dp ends with zero returned as x.y", "5.3", testFormated5);

		double testNum6 = 1.006;
		String testFormated6 = CaveConverter.decimalNumberStringEnglish(testNum6);
		assertEquals("Test rounded up to 2dp double returned as x.yz", "1.01", testFormated6);

		double testNum7 = 1.23456;
		String testFormated7 = CaveConverter.decimalNumberStringEnglish(testNum7);
		assertEquals("Test 5dp double returned as x.yz", "1.23", testFormated7);

		double testNum8 = 12345.6;
		String testFormated8 = CaveConverter.decimalNumberStringEnglish(testNum8);
		assertEquals("Test 1dp double returned as xxxxx.yz", "12345.6", testFormated8);

		double testNum9 = 1234.56;
		String testFormated9 = CaveConverter.decimalNumberStringEnglish(testNum9);
		assertEquals("Test 2dp double returned as xxxx.yz", "1234.56", testFormated9);

		double testNum10 = 123.456;
		String testFormated10 = CaveConverter.decimalNumberStringEnglish(testNum10);
		assertEquals("Test 3dp double returned as xxx.yz", "123.46", testFormated10);

		double testNum11 = -0.0001;
		String testFormated11 = CaveConverter.decimalNumberStringEnglish(testNum11);
		assertEquals("Test negative number rounded to zero double returned as -0.0", "-0.0", testFormated11);

		double testNum12 = -4.0;
		String testFormated12 = CaveConverter.decimalNumberStringEnglish(testNum12);
		assertEquals("Test negative whole number double returned as -x.y", "-4.0", testFormated12);

		double testNum13 = -1.4;
		String testFormated13 = CaveConverter.decimalNumberStringEnglish(testNum13);
		assertEquals("Test negative 1dp double returned as -x.y", "-1.4", testFormated13);

		double testNum14 = -1.0001;
		String testFormated14 = CaveConverter.decimalNumberStringEnglish(testNum14);
		assertEquals("Test negative whole number when rounded to 2dp double returned as -x.y", "-1.0", testFormated14);

		double testNum15 = -5.304;
		String testFormated15 = CaveConverter.decimalNumberStringEnglish(testNum15);
		assertEquals("Test negative double number when rounded to 2dp ends with zero returned as -x.y", "-5.3", testFormated15);

		double testNum16 = -1.006;
		String testFormated16 = CaveConverter.decimalNumberStringEnglish(testNum16);
		assertEquals("Test negative rounded up to 2dp double returned as -x.yz", "-1.01", testFormated16);

		double testNum17 = -1.23456;
		String testFormated17 = CaveConverter.decimalNumberStringEnglish(testNum17);
		assertEquals("Test negative 5dp double returned as -x.yz", "-1.23", testFormated17);

		double testNum18 = -12345.6;
		String testFormated18 = CaveConverter.decimalNumberStringEnglish(testNum18);
		assertEquals("Test negative 1dp double returned as -xxxxx.yz", "-12345.6", testFormated18);

		double testNum19 = -1234.56;
		String testFormated19 = CaveConverter.decimalNumberStringEnglish(testNum19);
		assertEquals("Test negative 2dp double returned as -xxxx.yz", "-1234.56", testFormated19);

		double testNum20 = -123.456;
		String testFormated20 = CaveConverter.decimalNumberStringEnglish(testNum20);
		assertEquals("Test negative 3dp double returned as -xxx.yz", "-123.46", testFormated20);

	}
	
}
