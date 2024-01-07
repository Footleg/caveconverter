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
package footleg.cavesurvey.data.writer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.converter.CmdLineLogger;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.tools.TestHelper;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Unit test class for {@link footleg.cavesurvey.data.writer.TopoRobotWriter}
 * 
 * @author Footleg
 * @version 2024.01.03 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class TopoRobotWriterTest {

	@Test
	public void testConvertToSingleSeries() {
		CmdLineLogger logger = new CmdLineLogger();
		TopoRobotWriter writer = new TopoRobotWriter(logger);

		// Create test survey
		CaveSurvey surveyData = new CaveSurvey(logger);

		// Create master series
		SurveySeries masterSeries = new SurveySeries("outer");
		// Add 3 test legs to series
		masterSeries.addLeg(TestHelper.createTestLeg(1, 2, 1.0, 0.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(2, 3, 1.0, 20.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(3, 4, 1.0, 40.0, 0.0));

		// Create inner series
		SurveySeries innerSeries = new SurveySeries("inner");
		// Add a couple of test legs
		innerSeries.addLeg(TestHelper.createTestLeg(1, 2, 1.0, 100.0, 0.0));
		innerSeries.addLeg(TestHelper.createTestLeg(2, 3, 1.0, 120.0, 0.0));

		// Link station in inner series to a station in the outer series
		masterSeries.addLink("", masterSeries.getLegRaw(2).getToStn(), "inner", innerSeries.getLegRaw(0).getFromStn());

		// Add inner series to master series
		masterSeries.addSeries(innerSeries);

		// Add master series to survey
		surveyData.add(masterSeries);

		// Convert to single series
		SurveySeries convertedSeries = writer.convertToSingleSeries(surveyData, false);

		assertEquals("root", convertedSeries.getSeriesName());
	}

	@Test
	public void testConvertToLinearSeriesFig8() {
		// Create master series
		SurveySeries masterSeries = new SurveySeries("test");
		// Add 3 test legs to series
		masterSeries.addLeg(TestHelper.createTestLeg(1, 2, 1.0, 010.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(2, 3, 1.0, 100.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(3, 4, 1.0, 190.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(4, 1, 1.0, 280.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(1, 5, 1.0, 270.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(5, 6, 1.0, 180.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(6, 7, 1.0, 090.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(7, 1, 1.0, 000.0, 0.0));

		// Convert to single series
		SurveySeries convertedSeries = UtilityFunctions.convertToLinearSeries(masterSeries, new CmdLineLogger());

		assertEquals(2, convertedSeries.innerSeriesCount(), "Expect 2 series from figure of 8 loop.");
	}

	@Test
	public void testConvertToLinearSeriesOrderedT() {
		// Create master series
		SurveySeries masterSeries = new SurveySeries("test");
		// Add 3 test legs to series
		masterSeries.addLeg(TestHelper.createTestLeg(1, 2, 1.0, 110.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(2, 3, 1.0, 100.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(3, 4, 1.0, 190.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(4, 5, 1.0, 180.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(3, 6, 1.0, 270.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(6, 7, 1.0, 290.0, 0.0));

		// Convert to single series
		SurveySeries convertedSeries = UtilityFunctions.convertToLinearSeries(masterSeries, new CmdLineLogger());

		assertEquals(2, convertedSeries.innerSeriesCount(), "Expect 2 series from t-shaped single series.");
	}

	@Test
	public void testConvertToLinearSeriesScrambledT() {
		// Create master series
		SurveySeries masterSeries = new SurveySeries("test");
		// Add 3 test legs to series
		masterSeries.addLeg(TestHelper.createTestLeg(4, 5, 1.0, 180.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(3, 6, 1.0, 270.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(2, 3, 1.0, 100.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(6, 7, 1.0, 290.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(3, 4, 1.0, 190.0, 0.0));
		masterSeries.addLeg(TestHelper.createTestLeg(1, 2, 1.0, 110.0, 0.0));

		// Convert to single series
		SurveySeries convertedSeries = UtilityFunctions.convertToLinearSeries(masterSeries, new CmdLineLogger());

		assertEquals(2, convertedSeries.innerSeriesCount(), "Expect 2 series from t-shaped single series.");
	}

}
