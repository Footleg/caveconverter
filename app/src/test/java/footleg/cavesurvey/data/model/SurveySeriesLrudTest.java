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
package footleg.cavesurvey.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.converter.CmdLineLogger;
import footleg.cavesurvey.data.writer.SurvexWriter;
import footleg.cavesurvey.tools.TestHelper;

/**
 * Unit test class which tests the generation of LRUD passage dimension data
 * from splays.
 * Various test sets of data in a survey series are set up and tested.
 * 
 * @author Footleg
 * @version 2024.01.03 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class SurveySeriesLrudTest {
	final String PASSAGE_DATA_HEADER = "*data passage station left right up down";

	@Test
	public void testLrudGenerationsStandardLinearSeriesOfForwardShots() {
		SurveySeries series = TestHelper.createSimpleForward3SurveyingSeriesWith3Splays();

		// Generate LRUD data from splays
		series.generateLRUDFromSplays(new CmdLineLogger());

		// Generate Survex format data
		List<String> outputData = survexDataFromSeries(series);

		/*
		 * data passage station left right up down
		 * 1 0.51 0.00 1.55 0.86
		 * 2 0.00 0.58 2.73 1.16
		 * 3 0.41 0.00 3.68 0.54
		 * 4 0.00 0.52 4.40 1.42
		 */

		// Check LRUD data block from Survex Writer
		assertEquals(34, outputData.size(), "File Length check.");
		int line = 27;
		assertEquals(PASSAGE_DATA_HEADER, outputData.get(line++));
		assertEquals("1	 0.51	 0.00	 1.55	 0.86", outputData.get(line++));
		assertEquals("2	 0.00	 0.58	 2.73	 1.16", outputData.get(line++));
		assertEquals("3	 0.41	 0.00	 3.68	 0.54", outputData.get(line++));
		assertEquals("4	 0.00	 0.52	 4.40	 1.42", outputData.get(line++));
	}

	@Test
	public void testLrudGenerationsStandardLinearSeriesOfBackwardShots() {
		SurveySeries series = TestHelper.createSimpleBackwardsSurveyingSeriesWithSplays();

		// Generate LRUD data from splays
		series.generateLRUDFromSplays(new CmdLineLogger());

		// Generate Survex format data
		List<String> outputData = survexDataFromSeries(series);

		/*
		 * data passage station left right up down
		 * 4 0.52 0.00 4.40 1.42
		 * 3 0.00 0.41 3.68 0.54
		 * 2 0.58 0.00 2.73 1.16
		 * 1 0.00 0.51 1.55 0.86
		 */

		// Check LRUD data block from Survex Writer
		assertEquals(34, outputData.size(), "File Length check.");
		int line = 27;
		assertEquals(PASSAGE_DATA_HEADER, outputData.get(line++));
		assertEquals("4	 0.52	 0.00	 4.40	 1.42", outputData.get(line++));
		assertEquals("3	 0.00	 0.41	 3.68	 0.54", outputData.get(line++));
		assertEquals("2	 0.58	 0.00	 2.73	 1.16", outputData.get(line++));
		assertEquals("1	 0.00	 0.51	 1.55	 0.86", outputData.get(line++));
	}

	@Test
	public void testLrudGenerationsBranchedSeriesOfForwardShots() {
		SurveySeries series = TestHelper.createBranched222ForwardSurveyingSeriesWithSplays();

		// Generate LRUD data from splays
		series.generateLRUDFromSplays(new CmdLineLogger());

		// Generate Survex format data
		List<String> outputData = survexDataFromSeries(series);

		/*
		 * data passage station left right up down
		 * 1 0.50 1.49 2.00 0.40
		 * 2 0.57 0.91 0.00 0.28
		 * 3 0.00 1.59 1.81 0.46
		 * 4 0.45 0.42 1.53 0.92
		 * 5 0.50 0.34 1.16 0.36
		 * data passage station left right up down
		 * 3 1.59 0.00 1.81 0.46
		 * 6 0.41 0.00 3.68 0.54
		 * 7 0.00 0.52 4.40 1.42
		 */

		// Check LRUD data block from Survex Writer
		assertEquals(60, outputData.size(), "File Length check.");
		int line = 48;
		assertEquals(PASSAGE_DATA_HEADER, outputData.get(line++));
		assertEquals("1	 0.50	 1.49	 2.00	 0.40", outputData.get(line++));
		assertEquals("2	 0.57	 0.91	 0.00	 0.28", outputData.get(line++));
		assertEquals("3	 0.00	 1.59	 1.81	 0.46", outputData.get(line++));
		assertEquals("4	 0.45	 0.42	 1.53	 0.92", outputData.get(line++));
		assertEquals("5	 0.50	 0.34	 1.16	 0.36", outputData.get(line++));
		assertEquals(PASSAGE_DATA_HEADER, outputData.get(line++));
		assertEquals("3	 1.59	 0.00	 1.81	 0.46", outputData.get(line++));
		assertEquals("6	 0.41	 0.00	 3.68	 0.54", outputData.get(line++));
		assertEquals("7	 0.00	 0.52	 4.40	 1.42", outputData.get(line++));
	}

	@Test
	public void testLrudGenerationsBranched122SeriesOfForwardShots() {
		SurveySeries series = TestHelper.createBranched122ForwardSurveyingSeriesWithSplays();

		// Generate LRUD data from splays
		series.generateLRUDFromSplays(new CmdLineLogger());

		// Generate Survex format data
		List<String> outputData = survexDataFromSeries(series);

		/*
		 * data passage station left right up down
		 * 1 0.56 0.93 0.00 0.28
		 * 2 0.00 1.59 1.81 0.46
		 * 3 0.45 0.42 1.53 0.92
		 * 4 0.50 0.34 1.16 0.36
		 * data passage station left right up down
		 * 2 1.59 0.00 1.81 0.46
		 * 5 0.41 0.00 3.68 0.54
		 * 6 0.00 0.52 4.40 1.42
		 */

		// Check LRUD data block from Survex Writer
		assertEquals(52, outputData.size(), "File Length check.");
		int line = 41;
		assertEquals(PASSAGE_DATA_HEADER, outputData.get(line++));
		assertEquals("1	 0.56	 0.93	 0.00	 0.28", outputData.get(line++));
		assertEquals("2	 0.00	 1.59	 1.81	 0.46", outputData.get(line++));
		assertEquals("3	 0.45	 0.42	 1.53	 0.92", outputData.get(line++));
		assertEquals("4	 0.50	 0.34	 1.16	 0.36", outputData.get(line++));
		assertEquals(PASSAGE_DATA_HEADER, outputData.get(line++));
		assertEquals("2	 1.59	 0.00	 1.81	 0.46", outputData.get(line++));
		assertEquals("5	 0.41	 0.00	 3.68	 0.54", outputData.get(line++));
		assertEquals("6	 0.00	 0.52	 4.40	 1.42", outputData.get(line++));
	}

	@Test
	public void testLrudGenerationsLinearSeriesOfForwardShots4Splays() {
		SurveySeries series = TestHelper.createSimpleForward5SurveyingSeriesWith4Splays();

		// Generate LRUD data from splays
		series.generateLRUDFromSplays(new CmdLineLogger());

		// Generate Survex format data
		List<String> outputData = survexDataFromSeries(series);

		/*
		 * data passage station left right up down
		 * 1 0.07 0.73 1.28 0.86
		 * 2 0.15 0.78 0.83 1.16
		 * 3 1.21 0.21 1.21 0.54
		 * 4 0.47 1.56 1.44 1.45
		 * 5 1.21 0.19 1.23 0.54
		 * 6 0.49 1.36 1.63 1.65
		 */

		// Check LRUD data block from Survex Writer
		assertEquals(54, outputData.size(), "File Length check.");
		int line = 45;
		assertEquals(PASSAGE_DATA_HEADER, outputData.get(line++));
		assertEquals("1	 0.07	 0.73	 1.28	 0.86", outputData.get(line++));
		assertEquals("2	 0.15	 0.78	 0.83	 1.16", outputData.get(line++));
		assertEquals("3	 1.21	 0.21	 1.21	 0.54", outputData.get(line++));
		assertEquals("4	 0.47	 1.56	 1.44	 1.45", outputData.get(line++));
		assertEquals("5	 1.21	 0.19	 1.23	 0.54", outputData.get(line++));
		assertEquals("6	 0.49	 1.36	 1.63	 1.65", outputData.get(line++));
	}

	@Test
	public void testLrudGenerationsLinearSeriesOfLeapfrogShots() {
		SurveySeries series = TestHelper.createLinearLeapfrogSurveyingSeriesWithSplays();

		// Generate LRUD data from splays
		series.generateLRUDFromSplays(new CmdLineLogger());

		// Generate Survex format data
		List<String> outputData = survexDataFromSeries(series);

		/*
		 * data passage station left right up down
		 * 1 0.07 0.73 1.28 0.86
		 * 2 0.15 0.78 0.83 1.16
		 * 3 1.21 0.21 1.21 0.54
		 * 4 0.47 1.56 1.44 1.45
		 * 5 1.21 0.19 1.23 0.54
		 * 6 0.49 1.36 1.63 1.65
		 */

		// Check LRUD data block from Survex Writer
		assertEquals(54, outputData.size(), "File Length check.");
		int line = 45;
		assertEquals(PASSAGE_DATA_HEADER, outputData.get(line++));
		assertEquals("1	 0.07	 0.73	 1.28	 0.86", outputData.get(line++));
		assertEquals("2	 0.15	 0.78	 0.83	 1.16", outputData.get(line++));
		assertEquals("3	 1.21	 0.21	 1.21	 0.54", outputData.get(line++));
		assertEquals("4	 0.47	 1.56	 1.44	 1.45", outputData.get(line++));
		assertEquals("5	 1.21	 0.19	 1.23	 0.54", outputData.get(line++));
		assertEquals("6	 0.49	 1.36	 1.63	 1.65", outputData.get(line++));
	}

	private List<String> survexDataFromSeries(SurveySeries series) {
		CmdLineLogger logger = new CmdLineLogger();
		// Declare structure to hold survey data
		CaveSurvey surveyData = new CaveSurvey(logger);
		surveyData.add(series);
		SurvexWriter writer = new SurvexWriter(logger);
		List<String> outputData = writer.generateSurvexData(surveyData, SurvexWriter.SplayFormats.Flagged);

		return outputData;
	}

}
