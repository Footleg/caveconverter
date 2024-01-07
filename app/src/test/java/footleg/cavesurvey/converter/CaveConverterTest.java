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
package footleg.cavesurvey.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.converter.CaveConverter.SurveyDataInputFormats;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataOutputFormats;

/**
 * Unit test class for {@link footleg.cavesurvey.converter.CaveConverter}
 * 
 * @author Footleg
 * @version 2024.01.03 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class CaveConverterTest {

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.converter.CaveConverter#getFormatName(SurveyDataInputFormats)}.
	 */
	@Test
	public void testGetFormatNameSurveyDataInputFormats() {
		String testCompassDisplayName = CaveConverter.getFormatName(SurveyDataInputFormats.Compass);
		assertEquals("Compass", testCompassDisplayName, "Test correct display name is returned for format");

		String testDXFDisplayName = CaveConverter.getFormatName(SurveyDataInputFormats.DXF);
		assertEquals("DXF", testDXFDisplayName, "Test correct display name is returned for format");

		String testPocketTopoDisplayName = CaveConverter.getFormatName(SurveyDataInputFormats.PocketTopo);
		assertEquals("PocketTopo", testPocketTopoDisplayName, "Test correct display name is returned for format");

		String testSurvexDisplayName = CaveConverter.getFormatName(SurveyDataInputFormats.Survex);
		assertEquals("Survex", testSurvexDisplayName, "Test correct display name is returned for format");
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.converter.CaveConverter#getFormatName(SurveyDataOutputFormats)}.
	 */
	@Test
	public void testGetFormatNameSurveyDataOutputFormats() {
		String testSurvexDisplayName = CaveConverter.getFormatName(SurveyDataOutputFormats.Survex);
		assertEquals("Survex", testSurvexDisplayName, "Test correct display name is returned for format");

		String testToporobotDisplayName = CaveConverter.getFormatName(SurveyDataOutputFormats.Toporobot);
		assertEquals("Toporobot", testToporobotDisplayName, "Test correct display name is returned for format");
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.converter.CaveConverter#inputFormatFromFileExtn(String)}.
	 */
	@Test
	public void testInputFormatFromFileExtn() {
		// Compass files
		SurveyDataInputFormats testCompassL = CaveConverter.inputFormatFromFileExtn("dat");
		assertEquals(SurveyDataInputFormats.Compass, testCompassL,
				"Test correct format is returned for Compass DAT file (lowercase extn)");

		SurveyDataInputFormats testCompassU = CaveConverter.inputFormatFromFileExtn("DAT");
		assertEquals(SurveyDataInputFormats.Compass, testCompassU,
				"Test correct format is returned for Compass DAT file (uppercase extn)");

		SurveyDataInputFormats testCompassM = CaveConverter.inputFormatFromFileExtn("dAt");
		assertEquals(SurveyDataInputFormats.Compass, testCompassM,
				"Test correct format is returned for Compass DAT file (mixedcase extn)");

		// DXF files
		SurveyDataInputFormats testDXFL = CaveConverter.inputFormatFromFileExtn("dxf");
		assertEquals(SurveyDataInputFormats.DXF, testDXFL,
				"Test correct format is returned for DXF DAT file (lowercase extn)");

		SurveyDataInputFormats testDXFU = CaveConverter.inputFormatFromFileExtn("DXF");
		assertEquals(SurveyDataInputFormats.DXF, testDXFU,
				"Test correct format is returned for DXF DAT file (uppercase extn)");

		SurveyDataInputFormats testDXFM = CaveConverter.inputFormatFromFileExtn("dXf");
		assertEquals(SurveyDataInputFormats.DXF, testDXFM,
				"Test correct format is returned for DXF DAT file (mixedcase extn)");

		// PocketTopo files
		SurveyDataInputFormats testPocketTopoL = CaveConverter.inputFormatFromFileExtn("txt");
		assertEquals(SurveyDataInputFormats.PocketTopo, testPocketTopoL,
				"Test correct format is returned for PocketTopo DAT file (lowercase extn)");

		SurveyDataInputFormats testPocketTopoU = CaveConverter.inputFormatFromFileExtn("TXT");
		assertEquals(SurveyDataInputFormats.PocketTopo, testPocketTopoU,
				"Test correct format is returned for PocketTopo DAT file (uppercase extn)");

		SurveyDataInputFormats testPocketTopoM = CaveConverter.inputFormatFromFileExtn("tXt");
		assertEquals(SurveyDataInputFormats.PocketTopo, testPocketTopoM,
				"Test correct format is returned for PocketTopo DAT file (mixedcase extn)");

		// Survex files
		SurveyDataInputFormats testSurvexL = CaveConverter.inputFormatFromFileExtn("svx");
		assertEquals(SurveyDataInputFormats.Survex, testSurvexL,
				"Test correct format is returned for Survex DAT file (lowercase extn)");

		SurveyDataInputFormats testSurvexU = CaveConverter.inputFormatFromFileExtn("SVX");
		assertEquals(SurveyDataInputFormats.Survex, testSurvexU,
				"Test correct format is returned for Survex DAT file (uppercase extn)");

		SurveyDataInputFormats testSurvexM = CaveConverter.inputFormatFromFileExtn("sVx");
		assertEquals(SurveyDataInputFormats.Survex, testSurvexM,
				"Test correct format is returned for Survex DAT file (mixedcase extn)");

		// Unknown
		SurveyDataInputFormats testUnknown = CaveConverter.inputFormatFromFileExtn("xml");
		assertEquals(null, testUnknown, "Test null returned for unsupported file extn");
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.converter.CaveConverter#decimalNumberStringEnglish(double)}.
	 */
	@Test
	public void testDecimalNumberStringEnglish() {
		double testNum1 = 0.0;
		String testFormated1 = CaveConverter.decimalNumberStringEnglish(testNum1);
		assertEquals("0.0", testFormated1, "Test zero double returned as 0.0");

		double testNum2 = 1.0;
		String testFormated2 = CaveConverter.decimalNumberStringEnglish(testNum2);
		assertEquals("1.0", testFormated2, "Test whole number double returned as x.y");

		double testNum3 = 1.4;
		String testFormated3 = CaveConverter.decimalNumberStringEnglish(testNum3);
		assertEquals("1.4", testFormated3, "Test 1dp double returned as x.y");

		double testNum4 = 1.0001;
		String testFormated4 = CaveConverter.decimalNumberStringEnglish(testNum4);
		assertEquals("1.0", testFormated4, "Test whole number when rounded to 2dp double returned as x.y");

		double testNum5 = 5.304;
		String testFormated5 = CaveConverter.decimalNumberStringEnglish(testNum5);
		assertEquals("5.3", testFormated5, "Test double number when rounded to 2dp ends with zero returned as x.y");

		double testNum6 = 1.006;
		String testFormated6 = CaveConverter.decimalNumberStringEnglish(testNum6);
		assertEquals("1.01", testFormated6, "Test rounded up to 2dp double returned as x.yz");

		double testNum7 = 1.23456;
		String testFormated7 = CaveConverter.decimalNumberStringEnglish(testNum7);
		assertEquals("1.23", testFormated7, "Test 5dp double returned as x.yz");

		double testNum8 = 12345.6;
		String testFormated8 = CaveConverter.decimalNumberStringEnglish(testNum8);
		assertEquals("12345.6", testFormated8, "Test 1dp double returned as xxxxx.yz");

		double testNum9 = 1234.56;
		String testFormated9 = CaveConverter.decimalNumberStringEnglish(testNum9);
		assertEquals("1234.56", testFormated9, "Test 2dp double returned as xxxx.yz");

		double testNum10 = 123.456;
		String testFormated10 = CaveConverter.decimalNumberStringEnglish(testNum10);
		assertEquals("123.46", testFormated10, "Test 3dp double returned as xxx.yz");

		double testNum11 = -0.0001;
		String testFormated11 = CaveConverter.decimalNumberStringEnglish(testNum11);
		assertEquals("-0.0", testFormated11, "Test negative number rounded to zero double returned as -0.0");

		double testNum12 = -4.0;
		String testFormated12 = CaveConverter.decimalNumberStringEnglish(testNum12);
		assertEquals("-4.0", testFormated12, "Test negative whole number double returned as -x.y");

		double testNum13 = -1.4;
		String testFormated13 = CaveConverter.decimalNumberStringEnglish(testNum13);
		assertEquals("-1.4", testFormated13, "Test negative 1dp double returned as -x.y");

		double testNum14 = -1.0001;
		String testFormated14 = CaveConverter.decimalNumberStringEnglish(testNum14);
		assertEquals("-1.0", testFormated14, "Test negative whole number when rounded to 2dp double returned as -x.y");

		double testNum15 = -5.304;
		String testFormated15 = CaveConverter.decimalNumberStringEnglish(testNum15);
		assertEquals("-5.3", testFormated15,
				"Test negative double number when rounded to 2dp ends with zero returned as -x.y");

		double testNum16 = -1.006;
		String testFormated16 = CaveConverter.decimalNumberStringEnglish(testNum16);
		assertEquals("-1.01", testFormated16, "Test negative rounded up to 2dp double returned as -x.yz");

		double testNum17 = -1.23456;
		String testFormated17 = CaveConverter.decimalNumberStringEnglish(testNum17);
		assertEquals("-1.23", testFormated17, "Test negative 5dp double returned as -x.yz");

		double testNum18 = -12345.6;
		String testFormated18 = CaveConverter.decimalNumberStringEnglish(testNum18);
		assertEquals("-12345.6", testFormated18, "Test negative 1dp double returned as -xxxxx.yz");

		double testNum19 = -1234.56;
		String testFormated19 = CaveConverter.decimalNumberStringEnglish(testNum19);
		assertEquals("-1234.56", testFormated19, "Test negative 2dp double returned as -xxxx.yz");

		double testNum20 = -123.456;
		String testFormated20 = CaveConverter.decimalNumberStringEnglish(testNum20);
		assertEquals("-123.46", testFormated20, "Test negative 3dp double returned as -xxx.yz");

	}

}
