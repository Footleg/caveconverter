/**
 * Copyright (C) 2009-2017 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
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
package footleg.cavesurvey.tools;

import java.text.DecimalFormat;

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.data.model.SurveyStation;

/**
 * Test helper class containing methods to assist in building survey models for test scenarios.
 *  
 * @author      Footleg
 * @version     2017.01.07                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public final class TestHelper {

	/**
	 * Creates a survey leg from a set of measurements
	 * 
	 * @param from	From-station number
	 * @param to	To-station number
	 * @param tape	Length of leg (in metres)
	 * @param compass Bearing of leg
	 * @param clino	Inclination angle of leg
	 * @return SurveyLeg object
	 */
	public static SurveyLeg createTestLeg(int from, int to, double tape, double compass, double clino) {
		SurveyLeg leg = new SurveyLeg();
		leg.setFromStn(new SurveyStation(from));
		leg.setToStn(new SurveyStation(to));
		leg.setLength( tape, LengthUnit.Metres );
		leg.setCompass( compass, BearingUnit.Degrees );
		leg.setClino( clino, GradientUnit.Degrees );
		
		return leg;
	}

	/**
	 * Creates a survey splay leg from a set of measurements. The leg will have no to-station.
	 * 
	 * @param from	From-station number
	 * @param tape	Length of leg (in metres)
	 * @param compass Bearing of leg
	 * @param clino	Inclination angle of leg
	 * @return SurveyLeg object
	 */
	public static SurveyLeg createSplayLeg(int from, double tape, double compass, double clino) {
		SurveyLeg leg = new SurveyLeg();
		leg.setFromStn(new SurveyStation(from));
		leg.setLength( tape, LengthUnit.Metres );
		leg.setCompass( compass, BearingUnit.Degrees );
		leg.setClino( clino, GradientUnit.Degrees );
		leg.setSplay(true);
		
		return leg;
	}
	
	/**
	 * Creates a short linear survey series of 3 legs without splays. All legs are measured in the 
	 * forward direction for the survey. (e.g. 1-2, 2-3, 3-4, but starting at the specified number). 
	 * 
	 * @param startStnNumber Number of the first station in the series
	 * @return SurveySeries object
	 */
	public static SurveySeries createSimpleForward3SurveyingSeriesWithNoSplays(int startStnNumber) {
		SurveySeries series = new SurveySeries( "Test" );

		//Add test legs to series with splays for LRUD
		int fromStn = startStnNumber;
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 5.05, 279.35, -25.23 ) );

		fromStn++;
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 5.02, 336.36, -13.85 ) );

		fromStn++;
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.19, 303.08, -33.11 ) );

		return series;
	}
	
	
	/**
	 * Creates a short linear survey series of 3 legs with splays measured for LRUD passage
	 * dimension at each station. All legs are measured in the forward direction for the survey.
	 * (e.g. 1-2, 2-3, 3-4). All stations have all three splays for LRUD (only a L or R per station).
	 * 
	 * @return SurveySeries object
	 */
	public static SurveySeries createSimpleForward3SurveyingSeriesWith3Splays() {
		SurveySeries series = new SurveySeries( "Test" );

		//Add test legs to series with splays for LRUD
		int fromStn = 1;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.74, 236.15, -6.85 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.87, 46.77, -82.61 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.68, 227.27, 67.16 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 5.05, 279.35, -25.23 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.58, 34.79, 1.49 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.16, 111.26, -85.03 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 2.83, 16.27, 74.62 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 5.02, 336.36, -13.85 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.41, 231.77, -3.06 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 3.73, 254.43, 80.33 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.54, 182.41, -84.02 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.19, 303.08, -33.11 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.53, 23.07, -5.56 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 4.46, 353.35, 80.60 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.45, 224.40, -78.95 ) );

		return series;
	}
	
	/**
	 * Creates a short linear survey series of 5 legs with splays measured for LRUD passage
	 * dimension at each station. All legs are measured in the forward direction for the survey.
	 * (e.g. 1-2, 2-3, 3-4). All stations have all four splays for LRUD.
	 * 
	 * @return SurveySeries object
	 */
	public static SurveySeries createSimpleForward5SurveyingSeriesWith4Splays() {
		SurveySeries series = new SurveySeries( "Test" );

		//Add test legs to series with splays for LRUD
		int fromStn = 1;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.07, 194.15, 2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.74, 16.15, -6 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.28, 227.27, 87 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.87, 46.77, -82 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 5.05, 279.15, -5.23 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.15, 217.28, -3 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.78, 34.79, 1 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.83, 16.27, 84 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.16, 111.26, -85 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 5.02, 336.26, -3.85 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.21, 231.77, 2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.21, 45.17, -3 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.23, 254.43, 80 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.54, 182.41, -84 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.19, 303.38, -6 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.53, 228.49, -2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.62, 60.07, -5 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.46, 353.35, 80 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.45, 224.40, -89 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 5.02, 26.42, -5.85 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.21, 251.77, 2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.21, 45.17, -5 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.25, 254.45, 80 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.54, 182.41, -84 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.19, 297.57, -6 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.53, 228.69, -2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.62, 60.07, -5 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.66, 353.35, 80 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.65, 226.60, -89 ) );

		return series;
	}
		/**
	 * Creates a short linear survey series of 3 legs with splays measured for LRUD passage
	 * dimension at each station. All legs are measured in the backwards direction for the survey.
	 * (e.g. 2-1, 3-2, 4-3)
	 * 
	 * @return SurveySeries object
	 */
	public static SurveySeries createSimpleBackwardsSurveyingSeriesWithSplays() {
		SurveySeries series = new SurveySeries( "Test" );

		//Add test legs to series with splays for LRUD
		int fromStn = 1;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.74, 236.15, -6.85 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.87, 46.77, -82.61 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.68, 227.27, 67.16 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn + 1, fromStn, 5.05, 99.35, 25.23 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.58, 34.79, 1.49 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.16, 111.26, -85.03 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 2.83, 16.27, 74.62 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn + 1, fromStn, 5.02, 156.36, 13.85 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.41, 231.77, -3.06 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 3.73, 254.43, 80.33 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.54, 182.41, -84.02 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn + 1, fromStn, 3.19, 123.08, 33.11 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.53, 23.07, -5.56 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 4.46, 353.35, 80.60 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.45, 224.40, -78.95 ) );

		return series;
	}
	
	/**
	 * Creates a short t-shaped survey series of 6 legs with splays measured for LRUD passage
	 * dimension at each station. Branch lengths 2,2,2
	 * All legs are measured in the forward direction for the survey (e.g. 1-2, 2-3, 3-4, etc.)
	 * 
	 * @return SurveySeries object
	 */
	public static SurveySeries createBranched222ForwardSurveyingSeriesWithSplays() {
		SurveySeries series = new SurveySeries( "Test" );

		//Add test legs to series with splays for LRUD
		int fromStn = 1;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.5, 330, 3 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.5, 150, -6 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 2, 60, 88 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.4, 60, -89 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 1.43, 61.61, -13.25 ) );
		fromStn++;

		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.58, 327.33,  -0.57 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.94, 171.13,  -6.41 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.50, 258.56, -34.18 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.89,  73.29, -51.63 ) );
		fromStn++;

		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.91, 252.72,  -4.04 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.80, 250.31, -35.24 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 2.44, 239.99,  47.81 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 4.50, 170.72,  19.68 ) );
		fromStn++;

		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.46,  71.39,  -4.30 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.54, 358.12,  82.08 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.94, 170.44, -79.46 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.45, 220.38,  -6.59 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 1.63, 131.27,  20.13 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.37, 242.14, -5.73 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.36,  39.38, -82.79 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.17, 258.11,  84.02 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.5, 41, 2 ) );

		//Branch from 2 stations back
		series.addLeg( TestHelper.createTestLeg( fromStn - 2, fromStn + 1, 5.02, 336.36, -13.85 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.41, 231.77, -3.06 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 3.73, 254.43, 80.33 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.54, 182.41, -84.02 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.19, 303.08, -33.11 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.53, 23.07, -5.56 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 4.46, 353.35, 80.60 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.45, 224.40, -78.95 ) );

		return series;
	}
	
	/**
	 * Creates a short t-shaped survey series of 6 legs with splays measured for LRUD passage
	 * dimension at each station. Branch lengths 1,2,2
	 * All legs are measured in the forward direction for the survey (e.g. 1-2, 2-3, 3-4, etc.)
	 * 
	 * @return SurveySeries object
	 */
	public static SurveySeries createBranched122ForwardSurveyingSeriesWithSplays() {
		SurveySeries series = new SurveySeries( "Test" );

		//Add test legs to series with splays for LRUD
		int fromStn = 1;

		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.58, 327.33,  -0.57 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.94, 171.13,  -6.41 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.50, 258.56, -34.18 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.89,  73.29, -51.63 ) );
		fromStn++;

		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.91, 252.72,  -4.04 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.80, 250.31, -35.24 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 2.44, 239.99,  47.81 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 4.50, 170.72,  19.68 ) );
		fromStn++;

		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.46,  71.39,  -4.30 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.54, 358.12,  82.08 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.94, 170.44, -79.46 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.45, 220.38,  -6.59 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 1.63, 131.27,  20.13 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.37, 242.14, -5.73 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.36,  39.38, -82.79 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.17, 258.11,  84.02 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.5, 41, 2 ) );

		//Branch from 2 stations back
		series.addLeg( TestHelper.createTestLeg( fromStn - 2, fromStn + 1, 5.02, 336.36, -13.85 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.41, 231.77, -3.06 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 3.73, 254.43, 80.33 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.54, 182.41, -84.02 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.19, 303.08, -33.11 ) );

		fromStn++;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.53, 23.07, -5.56 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 4.46, 353.35, 80.60 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.45, 224.40, -78.95 ) );

		return series;
	}
	
	
	/**
	 * Creates a short linear survey series of 5 legs with splays measured for LRUD passage
	 * dimension at each station. Alternate legs are measured forwards then backwards as
	 * instrument reader leapfrogs station target setter (e.g. 1-2, 3-2, 3-4, 4-3 etc.)
	 * 
	 * @return SurveySeries object
	 */
	public static SurveySeries createLinearLeapfrogSurveyingSeriesWithSplays() {
		SurveySeries series = new SurveySeries( "Test" );

		//Add test legs to series with splays for LRUD
		int fromStn = 1;
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.07, 194.15, 2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.74, 16.15, -6 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.28, 227.27, 87 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.87, 46.77, -82 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 5.05, 279.15, -5.23 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.15, 217.28, -3 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.78, 34.79, 1 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.83, 16.27, 84 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.16, 111.26, -85 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn + 1, fromStn, 5.02, 156.26, 3.85 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.21, 231.77, 2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.21, 45.17, -3 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.23, 254.43, 80 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.54, 182.41, -84 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.19, 303.38, -6 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.53, 228.49, -2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.62, 60.07, -5 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.46, 353.35, 80 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.45, 224.40, -89 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn + 1, fromStn, 5.02, 206.42, 5.85 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.21, 251.77, 2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.21, 45.17, -5 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.25, 254.45, 80 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.54, 182.41, -84 ) );
		series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, 3.19, 297.57, -6 ) );
		fromStn++;
		
		series.addLeg( TestHelper.createSplayLeg( fromStn, 0.53, 228.69, -2 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.62, 60.07, -5 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.66, 353.35, 80 ) );
		series.addLeg( TestHelper.createSplayLeg( fromStn, 1.65, 226.60, -89 ) );

		return series;
	}

	/*
	int fromStn = 1;
	series.addLeg( TestHelper.createSplayLeg( fromStn, , ,  ) );
	series.addLeg( TestHelper.createSplayLeg( fromStn, , ,  ) );
	series.addLeg( TestHelper.createSplayLeg( fromStn, , ,  ) );
	series.addLeg( TestHelper.createTestLeg( fromStn, fromStn + 1, , ,  ) );
	fromStn++;
*FLAGS SPLAY
1	1-L	 0.07	194.15	 2
1	1-R	 0.74	 16.15	 -6
1	1-U	 1.28	227.27	 87
1	1-D	 0.87	 46.77	-82
*FLAGS NOT SPLAY
1	2	 5.05	279.35	-5.23
*FLAGS SPLAY
2	2-L	 0.15	217.28	  -3
2	2-R	 0.78	 34.79	  1
2	2-U	 0.83	 16.27	 84
2	2-D	 1.16	111.26	-85
*FLAGS NOT SPLAY
2	3	 5.02	336.36	-3.85
*FLAGS SPLAY
3	3-L	 1.21	231.77	 2
3	3-R	 0.21	45.17	 -3
3	3-U	 1.23	254.43	 80
3	3-D	 0.54	182.41	-84
*FLAGS NOT SPLAY
3	4	 3.19	303.08	-6
*FLAGS SPLAY
4	4-L	 0.53	228.49	 -2
4	4-R	 1.62	 60.07	 -5
4	4-U	 1.46	353.35	 80
4	4-D	 1.45	224.40	-89
*FLAGS NOT SPLAY
4	5	 5.02	26.56	-5.85
*FLAGS SPLAY
5	5-L	 1.21	251.77	 2
5	5-R	 0.21	45.17	 -5
5	5-U	 1.25	254.45	 80
5	5-D	 0.54	182.41	-84
*FLAGS NOT SPLAY
5	6	 3.19	303.08	-6
*FLAGS SPLAY
6	6-L	 0.53	228.69	 -2
6	6-R	 1.62	 60.07	 -5
6	6-U	 1.66	353.35	 80
6	6-D	 1.65	226.60	-89
*FLAGS NOT SPLAY
*/

	/**
	 * Test for older (pre v8) JRE double rounding bug. Used to direct tests to older
	 * results files to prevent tests failing on JREs older than v8
	 * 
	 * @return True if older JRE detected with double rounding bug present
	 */
	public static boolean jreRoundingBug() {
		boolean oldJRE = false;
		
		double testValue = 2.215;
		DecimalFormat formatter = new DecimalFormat("0.00");
		String testRounding = formatter.format(testValue);
		
		if (testRounding.equalsIgnoreCase( "2.22" ) ) {
			oldJRE = true;
		}
		
		return oldJRE;
	}
}
