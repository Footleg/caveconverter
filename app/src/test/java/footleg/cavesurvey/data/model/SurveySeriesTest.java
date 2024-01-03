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

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.tools.TestHelper;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Unit test class for {@link footleg.cavesurvey.data.model.SurveySeries}
 * 
 * @author Footleg
 * @version 2024.01.03 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class SurveySeriesTest {

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#constructor(String)},
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#getSeriesName(String)},
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setSeriesName(String)}.
	 */
	@Test
	public void testSurveySeriesName() {
		String seriesName1 = "Test Series";
		String seriesName2 = "Renamed Series";

		// Create series with a name
		SurveySeries series = new SurveySeries(seriesName1);

		// Check name returned matches the name given
		assertTrue(series.getSeriesName() == seriesName1, "Series name matches name given in contructor.");

		// Change the series name
		series.setSeriesName(seriesName2);

		// Check the name returned matches the updated name
		assertTrue(series.getSeriesName() == seriesName2, "Series name matches the updated name.");
	}

	@Test
	public void testSeriesDateGetSet() {
		SurveySeries series = new SurveySeries("Test");
		String dateString = "12/02/1982 01:15:53";
		String dateFormat = "dd/MM/yyyy HH:mm:ss";
		try {
			Date date = UtilityFunctions.stringToDate(dateString, dateFormat);
			series.setSurveyDate(date);
		} catch (ParseException e) {
			// Just print out error (will never happen with hard coded arguments used in
			// method call above)
			e.printStackTrace();
		}

		assertEquals(dateString, UtilityFunctions.dateToString(series.getSurveyDate(), dateFormat),
				"Date returned should match date set.");

	}

	@Test
	public void testSeriesCommentSetSet() {
		SurveySeries series = new SurveySeries("Test");
		String comment = "My surveying is perfect.";

		series.setComment(comment);

		assertEquals(comment, series.getComment(), "Comment returned should match comment set.");
	}

	@Test
	public void testSetStationIdFromName() {
		SurveySeries series = new SurveySeries("Test");
		SurveyStation stn = new SurveyStation(0);

		// Set name which is numeric
		stn.setName("42");
		series.setStationIdFromName(stn);
		assertTrue(stn.getId() == 42, "Station id matches name string as number 42.");

		stn.setName("text");
		series.setStationIdFromName(stn);
		assertTrue(stn.getId() == -1, "Station id from non-numeric name should be negative.");
	}

	@Test
	public void testAddLegSurveyLeg() {
		SurveySeries series = new SurveySeries("Test");
		double leg1Length = 1.0;
		double leg2Length = 2.0;
		double leg3Length = 3.0;

		assertEquals(0, series.legCount(), "Series leg count is zero when first created.");

		series.addLeg(TestHelper.createTestLeg(1, 2, leg1Length, 58, -14));
		assertEquals(1, series.legCount(), "Series leg count is one when one leg added.");

		// Add leg 3 next
		series.addLeg(TestHelper.createTestLeg(3, 4, leg3Length, 0, 0));
		assertEquals(2, series.legCount(), "Series leg count is 2 when 2 legs added.");
		assertEquals(series.getLegRaw(0).getLength(LengthUnit.Metres), leg1Length, 1e-9,
				"First leg added is at the start.");
		assertEquals(series.getLegRaw(series.legCount() - 1).getLength(LengthUnit.Metres), leg3Length, 1e-9,
				"Last leg added is at the end.");

		// Insert leg 2 into second position
		series.addLeg(TestHelper.createTestLeg(2, 3, leg2Length, 12, 34), 1);
		assertEquals(3, series.legCount(), "Series leg count is 3 when 3 legs added.");
		assertEquals(series.getLegRaw(0).getLength(LengthUnit.Metres), leg1Length, 1e-9,
				"First leg added is at the start.");
		assertEquals(series.getLegRaw(series.legCount() - 1).getLength(LengthUnit.Metres), leg3Length, 1e-9,
				"Leg 3 is at the end.");
		assertEquals(series.getLegRaw(1).getLength(LengthUnit.Metres), leg2Length, 1e-9, "Leg 2 is in the middle.");
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#getNumberToRepresentStnName(String)}.
	 */
	@Test
	public void testGetNumberToRepresentStnName() {
		SurveySeries series = new SurveySeries("Test");

		// Add first station name to get a mapped number
		String stn1A = "1A";
		int stn1 = series.getNumberToRepresentStnName(stn1A);

		// Check number is negative
		assertTrue(stn1 < 0, "Mapped station number should be negative.");

		// Add a second station name
		String stn2A = "2A";
		int stn2 = series.getNumberToRepresentStnName(stn2A);

		// Check number is negative
		assertTrue(stn2 < 0, "Mapped station number should be negative.");

		// Check number is not the same as previous station number
		assertFalse(stn1 == stn2, "Mapped station numbers should be unique.");

		// Add same mapped name again
		int stn1b = series.getNumberToRepresentStnName(stn1A);

		// Check same number was returned as last time this name was added
		assertTrue(stn1 == stn1b,
				"Mapped station number should be the same when the same string is passed in a second time.");

	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#getMappedStnName(String)}.
	 */
	@Test
	public void testGetMappedStnName() {
		SurveySeries series = new SurveySeries("Test");

		// Add first station name to get a mapped number
		String stn1A = "1A";
		int stn1 = series.getNumberToRepresentStnName(stn1A);
		assertTrue(stn1 < 0, "Station number should be negative for a numeric name.");

		// Add a second station name
		String stn2A = "2A";
		int stn2 = series.getNumberToRepresentStnName(stn2A);
		assertTrue(stn2 < 0, "Station number should be negative for a numeric name.");

		// Add a third station name with a name already used
		String stn3N = stn2A;
		int stn3 = series.getNumberToRepresentStnName(stn3N);
		assertEquals(stn2, stn3, "Station number should be same as last time this name was used.");

		// Check name returned for first number
		assertEquals(stn1A, series.getMappedStnName(stn1),
				"Returned station name should match name specified for number.");

		// Check name returned for 2nd number
		assertEquals(stn2A, series.getMappedStnName(stn2),
				"Returned station name should match name specified for number.");

		// Check name returned for 3rd number
		assertEquals(stn3N, series.getMappedStnName(stn3),
				"Returned station name should match name specified for number.");

		// Check that a positive numeric ID returns the name which is that number
		assertEquals("123", series.getMappedStnName(123), "Station name should match number for positive ids");

	}

	@Test
	public void testCalibrationGettersSetters() {
		SurveySeries series = new SurveySeries("Test");

		assertEquals(0.0, series.getTapeCalibration(LengthUnit.Metres), 1e-9,
				"Tape calibration should be zero by default.");
		assertEquals(0.0, series.getCompassCalibration(BearingUnit.Degrees), 1e-9,
				"Compass calibration should be zero by default.");
		assertEquals(0.0, series.getClinoCalibration(GradientUnit.Degrees), 1e-9,
				"Clino calibration should be zero by default.");
		assertEquals(0.0, series.getDeclination(), 1e-9, "Declination should be zero by default.");

		double tapeCal = 1.45;
		double compassCal = 0.52;
		double clinoCal = -3.74;
		double decl = 2.56;

		series.setTapeCalibration(tapeCal, LengthUnit.Metres);
		series.setCompassCalibration(compassCal, BearingUnit.Degrees);
		series.setClinoCalibration(clinoCal, GradientUnit.Degrees);
		series.setDeclination(decl);

		assertEquals(tapeCal, series.getTapeCalibration(LengthUnit.Metres), 1e-9,
				"Tape calibration should match value that was set.");
		assertEquals(compassCal, series.getCompassCalibration(BearingUnit.Degrees), 1e-9,
				"Compass calibration should match value that was set.");
		assertEquals(clinoCal, series.getClinoCalibration(GradientUnit.Degrees), 1e-9,
				"Clino calibration should match value that was set.");
		assertEquals(decl, series.getDeclination(), 1e-9, "Declination should match value that was set.");
	}

	@Test
	public void testAddLink() {
		SurveySeries series = new SurveySeries("Test");
		String series1 = "ser1";
		SurveyStation stn1 = new SurveyStation(1);
		String series2 = "ser2";
		SurveyStation stn2 = new SurveyStation(2);

		assertEquals(0, series.getLinks().size(), "Series link count is zero when first created.");
		series.addLink(series1, stn1, series2, stn2);
		assertEquals(1, series.getLinks().size(), "Series link count is one when first link has been added.");
		SeriesLink link = series.getLinks().get(0);
		assertEquals(series1, link.getSeries1(), "Link series 1 matches series set when adding link.");
		assertEquals(stn1, link.getStn1(), "Link station 1 matches station set when adding link.");
		assertEquals(series2, link.getSeries2(), "Link series 2 matches series set when adding link.");
		assertEquals(stn2, link.getStn2(), "Link station 2 matches station set when adding link.");
	}

	@Test
	public void testAddSeries() {
		SurveySeries series = new SurveySeries("Parent");

		assertEquals(0, series.innerSeriesCount(), "Inner series count is zero when first created.");

		String innerSeries1 = "inner1";
		SurveySeries inner1 = new SurveySeries(innerSeries1);
		series.addSeries(inner1);
		assertEquals(1, series.innerSeriesCount(), "Inner series count is one when first inner series is added.");

		String innerSeries2 = "inner2";
		SurveySeries inner2 = new SurveySeries(innerSeries2);
		series.addSeries(inner2);
		assertEquals(2, series.innerSeriesCount(), "Inner series count is two when 2nd inner series is added.");

		String innerSeries3 = "inner3";
		SurveySeries inner3 = new SurveySeries(innerSeries3);
		series.addSeries(inner3);
		assertEquals(3, series.innerSeriesCount(), "Inner series count is 3 when 3rd inner series is added.");

		SurveySeries inner3rd = series.getInnerSeries(2);
		assertEquals(innerSeries3, inner3rd.getSeriesName(), "Inner series at index 2 is 3rd inner series added.");

		SurveySeries innerSearchMatch = series.findInnerSeriesByName(innerSeries2);
		assertEquals(innerSeries2, innerSearchMatch.getSeriesName(), "Inner series matching name used to fin by name.");

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
		SurveySeries series = new SurveySeries("Test");
		double leg1Length = 1.0;
		double leg1Compass = 83.0;
		double leg1Clino = -2.0;

		double leg2Length = 2.0;
		double leg2Compass = 359.9;
		double leg2Clino = -90.0;

		double leg3Length = 3.0;
		double leg3Compass = 260.5;
		double leg3Clino = 0.0;

		// Add 3 test legs to series
		series.addLeg(TestHelper.createTestLeg(1, 2, leg1Length, leg1Compass, leg1Clino));
		series.addLeg(TestHelper.createTestLeg(2, 3, leg2Length, leg2Compass, leg2Clino));
		series.addLeg(TestHelper.createTestLeg(3, 4, leg3Length, leg3Compass, leg3Clino));

		// Check lengths are the same as set when no calibration
		assertEquals(leg1Length, series.getLegCorrected(0).getLength(LengthUnit.Metres), 1e-9,
				"First leg length with no calibration set.");
		assertEquals(leg2Length, series.getLegCorrected(1).getLength(LengthUnit.Metres), 1e-9,
				"Second leg length with no calibration set.");
		assertEquals(leg3Length, series.getLegCorrected(2).getLength(LengthUnit.Metres), 1e-9,
				"Third leg length with no calibration set.");

		// Check compass bearings are the same as set when no calibration
		assertEquals(leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9,
				"First leg bearing with no calibration set.");
		assertEquals(leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9,
				"Second leg bearing with no calibration set.");
		assertEquals(leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9,
				"Third leg bearing with no calibration set.");

		// Check clino readings are the same as set when no calibration
		assertEquals(leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9,
				"First leg clino with no calibration set.");
		assertEquals(leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9,
				"Second leg clino with no calibration set.");
		assertEquals(leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9,
				"Third leg clino with no calibration set.");

		// Set tape calibration
		double tapeCal = 0.1; // Tape is 10cm short
		series.setTapeCalibration(tapeCal, LengthUnit.Metres);
		// Lengths recorded in legs are now 10cm too long, so corrected lengths should
		// be shorter
		leg1Length -= tapeCal;
		leg2Length -= tapeCal;
		leg3Length -= tapeCal;

		// Check lengths are the same as set when no calibration
		assertEquals(leg1Length, series.getLegCorrected(0).getLength(LengthUnit.Metres), 1e-9,
				"First leg length with tape calibration set.");
		assertEquals(leg2Length, series.getLegCorrected(1).getLength(LengthUnit.Metres), 1e-9,
				"Second leg length with tape calibration set.");
		assertEquals(leg3Length, series.getLegCorrected(2).getLength(LengthUnit.Metres), 1e-9,
				"Third leg length with tape calibration set.");

		// Check compass bearings are the same as set when no calibration
		assertEquals(leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9,
				"First leg bearing with tape calibration set.");
		assertEquals(leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9,
				"Second leg bearing with tape calibration set.");
		assertEquals(leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9,
				"Third leg bearing with tape calibration set.");

		// Check clino readings are the same as set when no calibration
		assertEquals(leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9,
				"First leg clino with tape calibration set.");
		assertEquals(leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9,
				"Second leg clino with tape calibration set.");
		assertEquals(leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9,
				"Third leg clino with tape calibration set.");

		// Set compass calibration
		double compassCal = 0.5; // Compass reads 0.5 for North
		series.setCompassCalibration(compassCal, BearingUnit.Degrees);
		// Bearing of 0.5 should be zero degrees, so subtract cal value from actuals
		leg1Compass -= compassCal;
		leg2Compass -= compassCal;
		leg3Compass -= compassCal;

		// Check lengths are the same as set when no calibration
		assertEquals(leg1Length, series.getLegCorrected(0).getLength(LengthUnit.Metres), 1e-9,
				"First leg length with tape and compass calibration set.");
		assertEquals(leg2Length, series.getLegCorrected(1).getLength(LengthUnit.Metres), 1e-9,
				"Second leg length with tape and compass calibration set.");
		assertEquals(leg3Length, series.getLegCorrected(2).getLength(LengthUnit.Metres), 1e-9,
				"Third leg length with tape and compass calibration set.");

		// Check compass bearings are the same as set when no calibration
		assertEquals(leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9,
				"First leg bearing with tape and compass calibration set.");
		assertEquals(leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9,
				"Second leg bearing with tape and compass calibration set.");
		assertEquals(leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9,
				"Third leg bearing with tape and compass calibration set.");

		// Check clino readings are the same as set when no calibration
		assertEquals(leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9,
				"First leg clino with tape and compass calibration set.");
		assertEquals(leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9,
				"Second leg clino with tape and compass calibration set.");
		assertEquals(leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9,
				"Third leg clino with tape and compass calibration set.");

		// Set clino calibration
		double clinoCal = -1.2; // Clino reads -1.2 for level
		series.setClinoCalibration(clinoCal, GradientUnit.Degrees);
		// Bearing of 0.5 should be zero degrees, so subtract cal value from actuals
		leg1Clino -= clinoCal;
		leg2Clino -= clinoCal;
		leg3Clino -= clinoCal;

		// Check lengths are the same as set when no calibration
		assertEquals(leg1Length, series.getLegCorrected(0).getLength(LengthUnit.Metres), 1e-9,
				"First leg length with tape, compass and clino calibration set.");
		assertEquals(leg2Length, series.getLegCorrected(1).getLength(LengthUnit.Metres), 1e-9,
				"Second leg length with tape, compass and clino calibration set.");
		assertEquals(leg3Length, series.getLegCorrected(2).getLength(LengthUnit.Metres), 1e-9,
				"Third leg length with tape, compass and clino calibration set.");

		// Check compass bearings are the same as set when no calibration
		assertEquals(leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9,
				"First leg bearing with tape, compass and clino calibration set.");
		assertEquals(leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9,
				"Second leg bearing with tape, compass and clino calibration set.");
		assertEquals(leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9,
				"Third leg bearing with tape, compass and clino calibration set.");

		// Check clino readings are the same as set when no calibration
		assertEquals(leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9,
				"First leg clino with tape, compass and clino calibration set.");
		assertEquals(leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9,
				"Second leg clino with tape, compass and clino calibration set.");
		assertEquals(leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9,
				"Third leg clino with tape, compass and clino calibration set.");

		// Set declination adjustment
		double declination = 2.36; //
		series.setDeclination(declination);
		// Bearing of 0.5 should be zero degrees, so subtract cal value from actuals
		leg1Compass -= declination;
		leg2Compass -= declination;
		leg3Compass -= declination;

		// Check lengths are the same as set when no calibration
		assertEquals(leg1Length, series.getLegCorrected(0).getLength(LengthUnit.Metres), 1e-9,
				"First leg length with all calibrations set plus declination.");
		assertEquals(leg2Length, series.getLegCorrected(1).getLength(LengthUnit.Metres), 1e-9,
				"Second leg length with all calibrations set plus declination.");
		assertEquals(leg3Length, series.getLegCorrected(2).getLength(LengthUnit.Metres), 1e-9,
				"Third leg length with all calibrations set plus declination.");

		// Check compass bearings are the same as set when no calibration
		assertEquals(leg1Compass, series.getLegCorrected(0).getCompass(BearingUnit.Degrees), 1e-9,
				"First leg bearing with all calibrations set plus declination.");
		assertEquals(leg2Compass, series.getLegCorrected(1).getCompass(BearingUnit.Degrees), 1e-9,
				"Second leg bearing with all calibrations set plus declination.");
		assertEquals(leg3Compass, series.getLegCorrected(2).getCompass(BearingUnit.Degrees), 1e-9,
				"Third leg bearing with all calibrations set plus declination.");

		// Check clino readings are the same as set when no calibration
		assertEquals(leg1Clino, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9,
				"First leg clino with all calibrations set plus declination.");
		assertEquals(leg2Clino, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9,
				"Second leg clino with all calibrations set plus declination.");
		assertEquals(leg3Clino, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9,
				"Third leg clino with all calibrations set plus declination.");
	}

	/*
	 * On Vulcain topofil the clino is from 0 to 180 and
	 * 0 = +90
	 * 90 = 0
	 * 180 = -90
	 * 
	 * measured value = (read value - zero error) x scale
	 * calibrate clino 90 -1
	 * 
	 */
	@Test
	public void testClinoCalibrationsForTopofil() {
		SurveySeries series = new SurveySeries("Test");

		// Set clino calibration for topofil
		series.setClinoCalibration(90.0, GradientUnit.Degrees, -1);

		// Clino of 0 should be +90 degrees
		double clinoReading = 0.0;
		double clinoCorrected = 90.0;

		series.addLeg(TestHelper.createTestLeg(1, 2, 1.1, 158.0, clinoReading));
		assertEquals(clinoCorrected, series.getLegCorrected(0).getClino(GradientUnit.Degrees), 1e-9,
				"Clino corrected for topofil data.");

		// Clino of 90 should be 0 degrees
		clinoReading = 90.0;
		clinoCorrected = 0.0;

		series.addLeg(TestHelper.createTestLeg(2, 3, 2.2, 274.0, clinoReading));
		assertEquals(clinoCorrected, series.getLegCorrected(1).getClino(GradientUnit.Degrees), 1e-9,
				"Clino corrected for topofil data.");

		// Clino of 180 should be -90 degrees
		clinoReading = 180.0;
		clinoCorrected = -90.0;

		series.addLeg(TestHelper.createTestLeg(3, 4, 3.3, 64.0, clinoReading));
		assertEquals(clinoCorrected, series.getLegCorrected(2).getClino(GradientUnit.Degrees), 1e-9,
				"Clino corrected for topofil data.");
	}

	/*
	 * Reverse series method is no longer used, so is commented out in series class
	 * 
	 * @Test
	 * public void testReverseSeries() {
	 * SurveySeries series = new SurveySeries( "Test" );
	 * 
	 * double tape1 = 1.0;
	 * double tape2 = 2.0;
	 * double tape3 = 3.0;
	 * double tape4 = 4.0;
	 * 
	 * series.addLeg( TestHelper.createTestLeg( 1, 2, tape1, 10, 10 ) );
	 * series.addLeg( TestHelper.createTestLeg( 2, 3, tape2, 100, 20 ) );
	 * series.addLeg( TestHelper.createTestLeg( 3, 4, tape3, 190, 0 ) );
	 * series.addLeg( TestHelper.createTestLeg( 4, 5, tape4, 340, -5 ) );
	 * 
	 * series.reverseSeries();
	 * 
	 * SurveyLeg leg = series.getLegRaw(0);
	 * assertEquals(5, leg.getFromStn().getId(), "From stn of first leg");
	 * assertEquals(4, leg.getToStn().getId(), "To stn of first leg");
	 * assertEquals(tape4, leg.getLength( LengthUnit.Metres ), 1e-9,
	 * "Length of first leg");
	 * assertEquals(160, leg.getCompass(BearingUnit.Degrees), 1e-9,
	 * "Compass for first leg");
	 * assertEquals(5, leg.getClino(GradientUnit.Degrees), 1e-9,
	 * "Clino for first leg");
	 * 
	 * leg = series.getLegRaw(1);
	 * assertEquals(4, leg.getFromStn().getId(), "From stn of 2nd leg");
	 * assertEquals(3, leg.getToStn().getId(), "To stn of 2nd leg");
	 * assertEquals(tape3, leg.getLength( LengthUnit.Metres ), 1e-9,
	 * "Length of 2nd leg");
	 * assertEquals(10, leg.getCompass(BearingUnit.Degrees), 1e-9,
	 * "Compass for 2nd leg");
	 * assertEquals(0, leg.getClino(GradientUnit.Degrees), 1e-9,
	 * "Clino for 2nd leg");
	 * 
	 * leg = series.getLegRaw(2);
	 * assertEquals(3, leg.getFromStn().getId(), "From stn of 3rd leg");
	 * assertEquals(2, leg.getToStn().getId(), "To stn of 3rd leg");
	 * assertEquals(tape2, leg.getLength( LengthUnit.Metres ), 1e-9,
	 * "Length of 3rd leg");
	 * assertEquals(280, leg.getCompass(BearingUnit.Degrees), 1e-9,
	 * "Compass for 3rd leg");
	 * assertEquals(-20, leg.getClino(GradientUnit.Degrees), 1e-9,
	 * "Clino for 3rd leg");
	 * 
	 * leg = series.getLegRaw(3);
	 * assertEquals(2, leg.getFromStn().getId(), "From stn of 4th leg");
	 * assertEquals(1, leg.getToStn().getId(), "To stn of 4th leg");
	 * assertEquals(leg.getLength( LengthUnit.Metres ), 1e-9, "Length of 4th leg",
	 * tape1);
	 * assertEquals(190, leg.getCompass(BearingUnit.Degrees), 1e-9,
	 * "Compass for 4th leg");
	 * assertEquals(-10, leg.getClino(GradientUnit.Degrees), 1e-9,
	 * "Clino for 4th leg");
	 * }
	 */

}
