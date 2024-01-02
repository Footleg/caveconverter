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
package footleg.cavesurvey.data.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.converter.CmdLineLogger;
import footleg.cavesurvey.tools.TestHelper;

/**
 * Unit test class for {@link footleg.cavesurvey.data.model.CaveSurvey}
 * 
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class CaveSurveyTest {

	@Test
	public void testSetGetSurveyName() {
		CaveSurvey survey = new CaveSurvey( new CmdLineLogger() );
		
		String name = "test name";
		
		survey.setSurveyName( name );
		
		assertEquals("Name set for survey should match name retrieved for survey", name, survey.getSurveyName() );
	}

	@Test
	public void testIsEmptySetRemove() {
		String seriesName1 = "A Survey Series";
		String seriesName2 = "Another Series";
		
		//Create series with a name
		SurveySeries series1 = new SurveySeries( seriesName1 );
		SurveySeries series2 = new SurveySeries( seriesName2 );
		
		CaveSurvey survey = new CaveSurvey( new CmdLineLogger() );

		assertTrue("Survey created with no series should be empty", survey.isEmpty() );
		
		//Add a series and check again
		survey.add( series1 );
		assertFalse("Survey created with one series should not be empty", survey.isEmpty() );
		
		//Add a second series and check again
		survey.add( series2 );
		assertFalse("Survey created with two series should not be empty", survey.isEmpty() );
		assertEquals("Count series in survey", 2, survey.size() );
		
		//Remove a series
		survey.remove( 0 );
		assertEquals("Count series in survey", 1, survey.size() );
		assertEquals("First series should be series 2", seriesName2, survey.get(0).getSeriesName() );
		assertFalse("Survey after one of two series removed should not be empty", survey.isEmpty() );
		
		//Remove 2nd series
		survey.remove( 0 );
		assertEquals("Count series in survey", 0, survey.size() );
		assertTrue("Survey after two of two series removed should be empty", survey.isEmpty() );
	}

	@Test
	public void testClear() {
		String seriesName1 = "A Survey Series";
		String seriesName2 = "Another Series";
		
		//Create series with a name
		SurveySeries series1 = new SurveySeries( seriesName1 );
		SurveySeries series2 = new SurveySeries( seriesName2 );
		
		CaveSurvey survey = new CaveSurvey( new CmdLineLogger() );
		survey.add( series1 );
		survey.add( series2 );
		
		assertEquals("Count series in survey", 2, survey.size() );
		assertFalse("Survey should not be empty", survey.isEmpty() );
		
		//Clear survey and it should be empty
		survey.clear();
		assertEquals("Count series in survey", 0, survey.size() );
		assertTrue("Survey should be empty after clear.", survey.isEmpty() );
	}

	@Test
	public void testGetChild() {
		CaveSurvey survey = new CaveSurvey( new CmdLineLogger() );
		SurveySeries series = TestHelper.createLinearLeapfrogSurveyingSeriesWithSplays();
		survey.add( series );
		
		//Generate LRUD data from splays
		survey.generateLRUDfromSplays();
		
		//4 splays, then next item in series should be leg 1-2
		SurveyLeg leg1 = (SurveyLeg) survey.getChild( series, 4 );
		assertEquals("First leg should be from 1 - 2", "1", leg1.getFromStn().getName() );
		assertEquals("First leg should be from 1 - 2", "2", leg1.getToStn().getName() );
		//4 splays, then next item in series should be leg 2-3
		SurveyLeg leg2 = (SurveyLeg) survey.getChild( series, 9 );
		assertEquals("First leg should be from 3 - 2", "3", leg2.getFromStn().getName() );
		assertEquals("First leg should be from 3 - 2", "2", leg2.getToStn().getName() );
	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.CaveSurvey#getChild(Object,int)},
	 * {@link footleg.cavesurvey.data.model.CaveSurvey#getChildCount(Object)},
	 * {@link footleg.cavesurvey.data.model.CaveSurvey#isLeaf(Object)}.
	 * Tests that get child returns both the inner series and the legs for nested series.
	 */
	@Test
	public void testGetChild2() {
		CaveSurvey survey = new CaveSurvey( new CmdLineLogger() );
		String series1Name = "First Series";
		SurveySeries series1 = TestHelper.createSimpleForward3SurveyingSeriesWithNoSplays( 1 );
		series1.setSeriesName( series1Name );
		String series2Name = "2nd Series";
		SurveySeries series2 = TestHelper.createSimpleForward3SurveyingSeriesWithNoSplays( 4 );
		series2.setSeriesName( series2Name );
		String series3Name = "Third Series";
		SurveySeries series3 = TestHelper.createSimpleForward3SurveyingSeriesWithNoSplays( 7 );
		series3.setSeriesName( series3Name );
		
		//Put series 2 into series 1 as an inner series (giving mixed series with legs)
		series1.addSeries(series2);
		
		//Put series 3 into series 2 as an inner series (giving mixed series with legs)
		series2.addSeries(series3);
		
		//Add outer series to cave survey
		survey.add( series1 );
		
		//Should be only one item in survey
		int rootChildCount = survey.getChildCount( survey );
		assertEquals("Root should contain 1 child", 1, rootChildCount );
		SurveySeries testRoot = (SurveySeries) survey.getChild(survey, 0);
		assertEquals("First item returned should be 1st series", series1Name, testRoot.getSeriesName() );
		assertFalse("Series should not be a leaf", survey.isLeaf( testRoot ) );
		
		//Should only be 4 items in series 1
		int series1ChildCount = survey.getChildCount( series1 );
		assertEquals("Series 1 should contain 4 children", 4, series1ChildCount );
		
		//First item in series1 should be series2
		SurveySeries testSeries1 = (SurveySeries) survey.getChild( series1, 0 );
		assertEquals("First item returned should be 2nd series", series2Name, testSeries1.getSeriesName() );
		assertFalse("Series should not be a leaf", survey.isLeaf( testSeries1 ) );

		//2nd item in series1 should be leg 1-2
		SurveyLeg leg1 = (SurveyLeg) survey.getChild( series1, 1 );
		assertEquals("First leg in series 1 should be from 1", "1", leg1.getFromStn().getName() );
		assertEquals("First leg in series 1 should be to 2", "2", leg1.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg1 ) );

		//3rd item in series1 should be leg 2-3
		SurveyLeg leg2 = (SurveyLeg) survey.getChild( series1, 2 );
		assertEquals("Second leg in series 1 should be from 2", "2", leg2.getFromStn().getName() );
		assertEquals("Second leg in series 1 should be to 3", "3", leg2.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg2 ) );

		//4th item in series1 should be leg 3-4
		SurveyLeg leg3 = (SurveyLeg) survey.getChild( series1, 3 );
		assertEquals("Third leg in series 1 should be from 3", "3", leg3.getFromStn().getName() );
		assertEquals("Third leg in series 1 should be to 4", "4", leg3.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg3 ) );
		
		//Should only be 4 items in series 2
		int series2ChildCount = survey.getChildCount( series2 );
		assertEquals("Series 2 should contain 4 children", 4, series2ChildCount );
		
		//First item in series2 should be series3
		SurveySeries testSeries2 = (SurveySeries) survey.getChild( series2, 0 );
		assertEquals("First item returned should be 3rd series", series3Name, testSeries2.getSeriesName() );
		assertFalse("Series should not be a leaf", survey.isLeaf( testSeries2 ) );

		//2nd item in series2 should be leg 4-5
		SurveyLeg leg4 = (SurveyLeg) survey.getChild( series2, 1 );
		assertEquals("First leg in series 2 should be from 4", "4", leg4.getFromStn().getName() );
		assertEquals("First leg in series 2 should be to 5", "5", leg4.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg4 ) );

		//3rd item in series2 should be leg 5-6
		SurveyLeg leg5 = (SurveyLeg) survey.getChild( series2, 2 );
		assertEquals("Second leg in series 2 should be from 5", "5", leg5.getFromStn().getName() );
		assertEquals("Second leg in series 2 should be to 6", "6", leg5.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg5 ) );

		//4th item in series2 should be leg 6-7
		SurveyLeg leg6 = (SurveyLeg) survey.getChild( series2, 3 );
		assertEquals("Third leg in series 2 should be from 6", "6", leg6.getFromStn().getName() );
		assertEquals("Third leg in series 2 should be to 7", "7", leg6.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg6 ) );
		
		//Should only be 3 items in series 3
		int series3ChildCount = survey.getChildCount( series3 );
		assertEquals("series 3 should contain 3 children", 3, series3ChildCount );

		//1st item in series3 should be leg 7-8
		SurveyLeg leg7 = (SurveyLeg) survey.getChild( series3, 0 );
		assertEquals("First leg in series 3 should be from 7", "7", leg7.getFromStn().getName() );
		assertEquals("First leg in series 3 should be to 8", "8", leg7.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg7 ) );

		//2nd item in series3 should be leg 8-9
		SurveyLeg leg8 = (SurveyLeg) survey.getChild( series3, 1 );
		assertEquals("Second leg in series 3 should be from 8", "8", leg8.getFromStn().getName() );
		assertEquals("Second leg in series 3 should be to 9", "9", leg8.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg8 ) );

		//3rd item in series3 should be leg 9-10
		SurveyLeg leg9 = (SurveyLeg) survey.getChild( series3, 2 );
		assertEquals("Third leg in series 3 should be from 9", "9", leg9.getFromStn().getName() );
		assertEquals("Third leg in series 3 should be to 10", "10", leg9.getToStn().getName() );
		assertTrue("Leg should be a leaf", survey.isLeaf( leg9 ) );

	}

}
