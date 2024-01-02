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

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.data.model.SeriesLink;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.data.model.SurveyStation;
import footleg.cavesurvey.tools.TestHelper;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Unit test class for {@link footleg.cavesurvey.data.model.SurveySeries}
 * 
 * @author      Footleg
 * @version     2017.01.07                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class SurveySeriesTest {

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg#constructor(String)},
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#getSeriesName(String)},
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setSeriesName(String)}.
	 */
	@Test
	public void testSurveySeriesName() {
		String seriesName1 = "Test Series";
		String seriesName2 = "Renamed Series";
		
		//Create series with a name
		SurveySeries series = new SurveySeries( seriesName1 );
		
		//Check name returned matches the name given
		assertTrue("Series name matches name given in contructor.", series.getSeriesName() == seriesName1 );
		
		//Change the series name
		series.setSeriesName( seriesName2 );
		
		//Check the name returned matches the updated name
		assertTrue("Series name matches the updated name.", series.getSeriesName() == seriesName2 );
	}

	@Test
	public void testSeriesDateGetSet() {
		SurveySeries series = new SurveySeries( "Test" );
		String dateString = "12/02/1982 01:15:53";
		String dateFormat = "dd/MM/yyyy HH:mm:ss";
		try {
			Date date = UtilityFunctions.stringToDate(dateString, dateFormat);
			series.setSurveyDate(date);
		} catch (ParseException e) {
			//Just print out error (will never happen with hard coded arguments used in method call above)
			e.printStackTrace();
		}
		
		assertEquals("Date returned should match date set.", 
			dateString, UtilityFunctions.dateToString(series.getSurveyDate(), dateFormat) );
		
	}

	@Test
	public void testSeriesCommentSetSet() {
		SurveySeries series = new SurveySeries( "Test" );
		String comment = "My surveying is perfect.";
		
		series.setComment(comment);
		
		assertEquals("Comment returned should match comment set.", comment, series.getComment() );
	}

	@Test
	public void testSetStationIdFromName() {
		SurveySeries series = new SurveySeries( "Test" );
		SurveyStation stn = new SurveyStation(0);
		
		//Set name which is numeric
		stn.setName("42");
		series.setStationIdFromName( stn );
		assertTrue("Station id matches name string as number 42.", stn.getId() == 42 );
		
		stn.setName("text");
		series.setStationIdFromName( stn );
		assertTrue("Station id from non-numeric name should be negative.", stn.getId() == -1 );
	}

	@Test
	public void testAddLegSurveyLeg() {
		SurveySeries series = new SurveySeries( "Test" );
		double leg1Length = 1.0;
		double leg2Length = 2.0;
		double leg3Length = 3.0;
		
		assertEquals("Series leg count is zero when first created.", 0, series.legCount() );
		
		series.addLeg( TestHelper.createTestLeg(1,2,leg1Length,58,-14) );
		assertEquals("Series leg count is one when one leg added.", 1, series.legCount() );
		
		//Add leg 3 next
		series.addLeg( TestHelper.createTestLeg(3,4,leg3Length,0,0) );
		assertEquals("Series leg count is 2 when 2 legs added.", 2, series.legCount() );
		assertEquals("First leg added is at the start.", series.getLegRaw(0).getLength( LengthUnit.Metres ), leg1Length, 1e-9 );
		assertEquals("Last leg added is at the end.", series.getLegRaw(series.legCount() - 1).getLength( LengthUnit.Metres ), leg3Length, 1e-9 );
		
		//Insert leg 2 into second position
		series.addLeg( TestHelper.createTestLeg(2,3,leg2Length,12,34), 1 );
		assertEquals("Series leg count is 3 when 3 legs added.", 3, series.legCount() );
		assertEquals("First leg added is at the start.", series.getLegRaw(0).getLength( LengthUnit.Metres ), leg1Length, 1e-9 );
		assertEquals("Leg 3 is at the end.", series.getLegRaw(series.legCount() - 1).getLength( LengthUnit.Metres ), leg3Length, 1e-9 );
		assertEquals("Leg 2 is in the middle.", series.getLegRaw(1).getLength( LengthUnit.Metres ), leg2Length, 1e-9 );
	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg#getNumberToRepresentStnName(String)}.
	 */
	@Test
	public void testGetNumberToRepresentStnName() {
		SurveySeries series = new SurveySeries( "Test" );
		
		//Add first station name to get a mapped number
		String stn1A = "1A";
		int stn1 = series.getNumberToRepresentStnName(stn1A);
		
		//Check number is negative
		assertTrue("Mapped station number should be negative.", stn1 < 0 );
		
		//Add a second station name
		String stn2A = "2A";
		int stn2 = series.getNumberToRepresentStnName(stn2A);
		
		//Check number is negative
		assertTrue("Mapped station number should be negative.", stn2 < 0 );
		
		//Check number is not the same as previous station number
		assertFalse("Mapped station numbers should be unique.", stn1 == stn2 );
		
		//Add same mapped name again
		int stn1b = series.getNumberToRepresentStnName(stn1A);
		
		//Check same number was returned as last time this name was added
		assertTrue("Mapped station number should be the same when the same string is passed in a second time.", stn1 == stn1b );
		
	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg#getMappedStnName(String)}.
	 */
	@Test
	public void testGetMappedStnName() {
		SurveySeries series = new SurveySeries( "Test" );
		
		//Add first station name to get a mapped number
		String stn1A = "1A";
		int stn1 = series.getNumberToRepresentStnName(stn1A);
		assertTrue("Station number should be negative for a numeric name.", stn1 < 0);
		
		//Add a second station name
		String stn2A = "2A";
		int stn2 = series.getNumberToRepresentStnName(stn2A);
		assertTrue("Station number should be negative for a numeric name.", stn2 < 0);
		
		//Add a third station name with a name already used
		String stn3N = stn2A;
		int stn3 = series.getNumberToRepresentStnName(stn3N);
		assertEquals("Station number should be same as last time this name was used.", stn2, stn3);
		
		//Check name returned for first number
		assertEquals("Returned station name should match name specified for number.", stn1A, series.getMappedStnName( stn1 ) );
		
		//Check name returned for 2nd number
		assertEquals("Returned station name should match name specified for number.", stn2A, series.getMappedStnName( stn2 ) );
		
		//Check name returned for 3rd number
		assertEquals("Returned station name should match name specified for number.", stn3N, series.getMappedStnName( stn3 ) );

		//Check that a positive numeric ID returns the name which is that number
		assertEquals("Station name should match number for positive ids", "123", series.getMappedStnName( 123 ));
	
	}

	@Test
	public void testCalibrationGettersSetters() {
		SurveySeries series = new SurveySeries( "Test" );
		
		assertEquals("Tape calibration should be zero by default.", 0.0, series.getTapeCalibration( LengthUnit.Metres ), 1e-9);
		assertEquals("Compass calibration should be zero by default.", 0.0, series.getCompassCalibration( BearingUnit.Degrees ), 1e-9);
		assertEquals("Clino calibration should be zero by default.", 0.0, series.getClinoCalibration( GradientUnit.Degrees ), 1e-9);
		assertEquals("Declination should be zero by default.", 0.0, series.getDeclination(), 1e-9);
		
		double tapeCal = 1.45;
		double compassCal = 0.52;
		double clinoCal = -3.74;
		double decl = 2.56;
		
		series.setTapeCalibration( tapeCal, LengthUnit.Metres );
		series.setCompassCalibration(compassCal, BearingUnit.Degrees);
		series.setClinoCalibration(clinoCal, GradientUnit.Degrees );
		series.setDeclination(decl);
		
		assertEquals("Tape calibration should match value that was set.", tapeCal, series.getTapeCalibration( LengthUnit.Metres ), 1e-9);
		assertEquals("Compass calibration should match value that was set.", compassCal, series.getCompassCalibration( BearingUnit.Degrees ), 1e-9);
		assertEquals("Clino calibration should match value that was set.", clinoCal, series.getClinoCalibration( GradientUnit.Degrees ), 1e-9);
		assertEquals("Declination should match value that was set.", decl, series.getDeclination(), 1e-9);
	}
	
	@Test
	public void testAddLink() {
		SurveySeries series = new SurveySeries( "Test" );
		String series1 = "ser1";
		SurveyStation stn1 = new SurveyStation(1);
		String series2 = "ser2";
		SurveyStation stn2 = new SurveyStation(2);
		

		assertEquals("Series link count is zero when first created.", 0, series.getLinks().size() );
		series.addLink(series1, stn1, series2, stn2);
		assertEquals("Series link count is one when first link has been added.", 1, series.getLinks().size() );
		SeriesLink link = series.getLinks().get(0);
		assertEquals("Link series 1 matches series set when adding link.", series1, link.getSeries1() );
		assertEquals("Link station 1 matches station set when adding link.", stn1, link.getStn1() );
		assertEquals("Link series 2 matches series set when adding link.", series2, link.getSeries2() );
		assertEquals("Link station 2 matches station set when adding link.", stn2, link.getStn2() );
	}

	@Test
	public void testAddSeries() {
		SurveySeries series = new SurveySeries( "Parent" );
		
		assertEquals("Inner series count is zero when first created.", 0, series.innerSeriesCount() );

		String innerSeries1 = "inner1";
		SurveySeries inner1 = new SurveySeries( innerSeries1 );
		series.addSeries(inner1);
		assertEquals("Inner series count is one when first inner series is added.", 1, series.innerSeriesCount() );

		String innerSeries2 = "inner2";
		SurveySeries inner2 = new SurveySeries( innerSeries2 );
		series.addSeries(inner2);
		assertEquals("Inner series count is two when 2nd inner series is added.", 2, series.innerSeriesCount() );

		String innerSeries3 = "inner3";
		SurveySeries inner3 = new SurveySeries( innerSeries3 );
		series.addSeries(inner3);
		assertEquals("Inner series count is 3 when 3rd inner series is added.", 3, series.innerSeriesCount() );

		SurveySeries inner3rd = series.getInnerSeries(2);
		assertEquals("Inner series at index 2 is 3rd inner series added.", innerSeries3, inner3rd.getSeriesName() );

		SurveySeries innerSearchMatch = series.findInnerSeriesByName(innerSeries2);
		assertEquals("Inner series matching name used to fin by name.", innerSeries2, innerSearchMatch.getSeriesName() );

	}
	
	/*
	 * TODO Test support for true vertical legs and true horizontal legs
	 * 
	 * TODO Test that calibrations are applied somehow to inner series
	 * 
	 * TODO Test that clino readings from 0 to 180 work with appropriate calibration
	 */
	@Test
	public void testGetLegCorrectedWithCalibrations() {
		SurveySeries series = new SurveySeries( "Test" );
		double leg1Length = 1.0;
		double leg1Compass = 83.0;
		double leg1Clino = -2.0;
		
		double leg2Length = 2.0;
		double leg2Compass = 359.9;
		double leg2Clino = -90.0;
		
		double leg3Length = 3.0;
		double leg3Compass = 260.5;
		double leg3Clino = 0.0;
		
		//Add 3 test legs to series
		series.addLeg( TestHelper.createTestLeg(1,2,leg1Length,leg1Compass,leg1Clino) );
		series.addLeg( TestHelper.createTestLeg(2,3,leg2Length,leg2Compass,leg2Clino) );
		series.addLeg( TestHelper.createTestLeg(3,4,leg3Length,leg3Compass,leg3Clino) );
		
		//Check lengths are the same as set when no calibration
		assertEquals("First leg length with no calibration set.", leg1Length, series.getLegCorrected(0).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Second leg length with no calibration set.", leg2Length, series.getLegCorrected(1).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Third leg length with no calibration set.", leg3Length, series.getLegCorrected(2).getLength( LengthUnit.Metres ), 1e-9 );
		
		//Check compass bearings are the same as set when no calibration
		assertEquals("First leg bearing with no calibration set.", leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Second leg bearing with no calibration set.", leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Third leg bearing with no calibration set.", leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9 );
		
		//Check clino readings are the same as set when no calibration
		assertEquals("First leg clino with no calibration set.", leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Second leg clino with no calibration set.", leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Third leg clino with no calibration set.", leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9 );
		
		//Set tape calibration
		double tapeCal = 0.1; //Tape is 10cm short
		series.setTapeCalibration( tapeCal, LengthUnit.Metres );
		//Lengths recorded in legs are now 10cm too long, so corrected lengths should be shorter
		leg1Length -= tapeCal;
		leg2Length -= tapeCal;
		leg3Length -= tapeCal;
		
		//Check lengths are the same as set when no calibration
		assertEquals("First leg length with tape calibration set.", leg1Length, series.getLegCorrected(0).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Second leg length with tape calibration set.", leg2Length, series.getLegCorrected(1).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Third leg length with tape calibration set.", leg3Length, series.getLegCorrected(2).getLength( LengthUnit.Metres ), 1e-9 );
		
		//Check compass bearings are the same as set when no calibration
		assertEquals("First leg bearing with tape calibration set.", leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Second leg bearing with tape calibration set.", leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Third leg bearing with tape calibration set.", leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9 );
		
		//Check clino readings are the same as set when no calibration
		assertEquals("First leg clino with tape calibration set.", leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Second leg clino with tape calibration set.", leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Third leg clino with tape calibration set.", leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9 );
		
		//Set compass calibration
		double compassCal = 0.5; //Compass reads 0.5 for North
		series.setCompassCalibration(compassCal, BearingUnit.Degrees);
		//Bearing of 0.5 should be zero degrees, so subtract cal value from actuals
		leg1Compass -= compassCal;
		leg2Compass -= compassCal;
		leg3Compass -= compassCal;
		
		//Check lengths are the same as set when no calibration
		assertEquals("First leg length with tape and compass calibration set.", leg1Length, series.getLegCorrected(0).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Second leg length with tape and compass calibration set.", leg2Length, series.getLegCorrected(1).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Third leg length with tape and compass calibration set.", leg3Length, series.getLegCorrected(2).getLength( LengthUnit.Metres ), 1e-9 );
		
		//Check compass bearings are the same as set when no calibration
		assertEquals("First leg bearing with tape and compass calibration set.", leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Second leg bearing with tape and compass calibration set.", leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Third leg bearing with tape and compass calibration set.", leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9 );
		
		//Check clino readings are the same as set when no calibration
		assertEquals("First leg clino with tape and compass calibration set.", leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Second leg clino with tape and compass calibration set.", leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Third leg clino with tape and compass calibration set.", leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9 );
		
		//Set clino calibration
		double clinoCal = -1.2; //Clino reads -1.2 for level
		series.setClinoCalibration(clinoCal, GradientUnit.Degrees);
		//Bearing of 0.5 should be zero degrees, so subtract cal value from actuals
		leg1Clino -= clinoCal;
		leg2Clino -= clinoCal;
		leg3Clino -= clinoCal;
		
		//Check lengths are the same as set when no calibration
		assertEquals("First leg length with tape, compass and clino calibration set.", leg1Length, series.getLegCorrected(0).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Second leg length with tape, compass and clino calibration set.", leg2Length, series.getLegCorrected(1).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Third leg length with tape, compass and clino calibration set.", leg3Length, series.getLegCorrected(2).getLength( LengthUnit.Metres ), 1e-9 );
		
		//Check compass bearings are the same as set when no calibration
		assertEquals("First leg bearing with tape, compass and clino calibration set.", leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Second leg bearing with tape, compass and clino calibration set.", leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Third leg bearing with tape, compass and clino calibration set.", leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9 );
		
		//Check clino readings are the same as set when no calibration
		assertEquals("First leg clino with tape, compass and clino calibration set.", leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Second leg clino with tape, compass and clino calibration set.", leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Third leg clino with tape, compass and clino calibration set.", leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9 );

		//Set declination adjustment
		double declination = 2.36; //
		series.setDeclination(declination);
		//Bearing of 0.5 should be zero degrees, so subtract cal value from actuals
		leg1Compass -= declination;
		leg2Compass -= declination;
		leg3Compass -= declination;
		
		//Check lengths are the same as set when no calibration
		assertEquals("First leg length with all calibrations set plus declination.", leg1Length, series.getLegCorrected(0).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Second leg length with all calibrations set plus declination.", leg2Length, series.getLegCorrected(1).getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Third leg length with all calibrations set plus declination.", leg3Length, series.getLegCorrected(2).getLength( LengthUnit.Metres ), 1e-9 );
		
		//Check compass bearings are the same as set when no calibration
		assertEquals("First leg bearing with all calibrations set plus declination.", leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Second leg bearing with all calibrations set plus declination.", leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Third leg bearing with all calibrations set plus declination.", leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9 );
		
		//Check clino readings are the same as set when no calibration
		assertEquals("First leg clino with all calibrations set plus declination.", leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Second leg clino with all calibrations set plus declination.", leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9 );
		assertEquals("Third leg clino with all calibrations set plus declination.", leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9 );
	}

	/*
	 * On Vulcain topofil the clino is from 0 to 180 and
	 * 0 =  +90
	 * 90 = 0
	 * 180 = -90
	 * 
	 * measured value = (read value - zero error) x scale
	 * calibrate clino 90 -1
	 * 
	 */
	@Test
	public void testClinoCalibrationsForTopofil() {
		SurveySeries series = new SurveySeries( "Test" );
		
		//Set clino calibration for topofil
		series.setClinoCalibration(90.0, GradientUnit.Degrees, -1);
		
		//Clino of 0 should be +90 degrees
		double clinoReading = 0.0;
		double clinoCorrected = 90.0;
		
		series.addLeg( TestHelper.createTestLeg(1,2,1.1,158.0,clinoReading) );
		assertEquals("Clino corrected for topofil data.", clinoCorrected, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9 );
		
		//Clino of 90 should be 0 degrees
		clinoReading = 90.0;
		clinoCorrected = 0.0;
		
		series.addLeg( TestHelper.createTestLeg(2,3,2.2,274.0,clinoReading) );
		assertEquals("Clino corrected for topofil data.", clinoCorrected, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9 );
		
		//Clino of 180 should be -90 degrees
		clinoReading = 180.0;
		clinoCorrected = -90.0;
		
		series.addLeg( TestHelper.createTestLeg(3,4,3.3,64.0,clinoReading) );
		assertEquals("Clino corrected for topofil data.", clinoCorrected, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9 );
	}

/*  Reverse series method is no longer used, so is commented out in series class
	@Test
	public void testReverseSeries() {
		SurveySeries series = new SurveySeries( "Test" );
		
		double tape1 = 1.0;
		double tape2 = 2.0;
		double tape3 = 3.0;
		double tape4 = 4.0;
		
		series.addLeg( TestHelper.createTestLeg( 1, 2, tape1, 10, 10 ) );
		series.addLeg( TestHelper.createTestLeg( 2, 3, tape2, 100, 20 ) );
		series.addLeg( TestHelper.createTestLeg( 3, 4, tape3, 190, 0 ) );
		series.addLeg( TestHelper.createTestLeg( 4, 5, tape4, 340, -5 ) );
		
		series.reverseSeries();
		
		SurveyLeg leg = series.getLegRaw(0);
		assertEquals("From stn of first leg", 5, leg.getFromStn().getId() );
		assertEquals("To stn of first leg", 4, leg.getToStn().getId() );
		assertEquals("Length of first leg", tape4, leg.getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Compass for first leg", 160, leg.getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Clino for first leg", 5, leg.getClino(GradientUnit.Degrees), 1e-9 );
		
		leg = series.getLegRaw(1);
		assertEquals("From stn of 2nd leg", 4, leg.getFromStn().getId() );
		assertEquals("To stn of 2nd leg", 3, leg.getToStn().getId() );
		assertEquals("Length of 2nd leg", tape3, leg.getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Compass for 2nd leg", 10, leg.getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Clino for 2nd leg", 0, leg.getClino(GradientUnit.Degrees), 1e-9 );
		
		leg = series.getLegRaw(2);
		assertEquals("From stn of 3rd leg", 3, leg.getFromStn().getId() );
		assertEquals("To stn of 3rd leg", 2, leg.getToStn().getId() );
		assertEquals("Length of 3rd leg", tape2, leg.getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Compass for 3rd leg", 280, leg.getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Clino for 3rd leg", -20, leg.getClino(GradientUnit.Degrees), 1e-9 );
		
		leg = series.getLegRaw(3);
		assertEquals("From stn of 4th leg", 2, leg.getFromStn().getId() );
		assertEquals("To stn of 4th leg", 1, leg.getToStn().getId() );
		assertEquals("Length of 4th leg", tape1, leg.getLength( LengthUnit.Metres ), 1e-9 );
		assertEquals("Compass for 4th leg", 190, leg.getCompass(BearingUnit.Degrees), 1e-9 );
		assertEquals("Clino for 4th leg", -10, leg.getClino(GradientUnit.Degrees), 1e-9 );
	}
*/
	
}
