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

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.converter.CmdLineLogger;
import footleg.cavesurvey.data.model.SeriesLink;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Regression test class for UtilityFunctions static class methods.
 *  
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class UtilityFunctionsTest {
	private static final double meanBearingTolerance = 1e-3;

	/**
	 * Test helper method to call the UtilityFunctions.averageCompassBearings method for
	 * two given bearings
	 */
	private double calcMeanBearing( double bearing1, double bearing2 ) {
		
		double[] bearings = new double[2];

		bearings[0] = bearing1;
		bearings[1] = bearing2;

		double meanBearing = UtilityFunctions.averageCompassBearings(bearings);
		if ( meanBearing > 359.999) {
			meanBearing = 0;
		}
		
		return meanBearing;
	}
	
	/**
	 * Test helper method to call the UtilityFunctions.averageCompassBearings method for
	 * three given bearings
	 */
	private double calcMeanBearing( double bearing1, double bearing2, double bearing3 ) {
		
		double[] bearings = new double[3];

		bearings[0] = bearing1;
		bearings[1] = bearing2;
		bearings[2] = bearing3;

		double meanBearing = UtilityFunctions.averageCompassBearings(bearings);
		
		return meanBearing;
	}
	
	/**
	 * Test helper method to call the UtilityFunctions.averageCompassBearings method for
	 * two given bearings and an expected result, which formats a useful assertion message
	 * on test failure.
	 */
	private void testMean2(double a, double b, double expectedMean) {
		double mean = calcMeanBearing(a, b);
		assertEquals("Mean bearing for values of " + a + " and " + b + "." , expectedMean, mean, meanBearingTolerance);
	}
	
	/**
	 * Test helper method to call the UtilityFunctions.averageCompassBearings method for
	 * three given bearings and an expected result, which formats a useful assertion message
	 * on test failure.
	 */
	private void testMean3(double a, double b, double c, double expected) {
		double expectedMean = expected;
		double mean = calcMeanBearing(a, b, c);
		if ( ( expectedMean > 359.5 ) && ( mean < 0.5 ) ) {
			mean += 360;
		} else if ( ( mean > 359.5 ) && ( expectedMean < 0.5 ) ) {
			expectedMean += 360;
		}
		assertEquals("Mean bearing for values of " + a + ", " + b + ", and " + c + "." , expectedMean, mean, meanBearingTolerance);
	}
	
	/**
	 * Test helper method to compare the result from UtilityFunctions.averageCompassBearings method 
	 * for three given bearings with the result from the numerical average method
	 */
	private void testMean3VerusNumericalAverage(double a, double b, double c) {
		double expectedMean = numericalMeanBearing3(a, b, c);
		testMean3(a, b, c, expectedMean);
	}
	
	/**
	 * Test method which calculates the numerical average of two bearings using logical cases 
	 * for relative positions of the two legs. This method essentially adds the two bearings 
	 * together and divides by two to get an average but also handles the case where the bearings 
	 * are either side of North (i.e. 359 & 1 should average to 0 and not 180).
	 * This method works for any pair of bearings in the range 0 - 360 degrees with any
	 * angle between them.
	 */
	private double numericalMeanBearing2( double bearing1, double bearing2 ) {
		
		double smallerBearing;
		double largerBearing;
		double meanBearing;
		
		//If bearings are the same then average is the value of either bearing
		if ( bearing1 == bearing2 ) {
			meanBearing = bearing1;
		}
		else {
			//Determine which is the larger value of the two bearings
			if ( bearing1 > bearing2 ) {
				largerBearing = bearing1;
				smallerBearing = bearing2;
			}
			else {
				largerBearing = bearing2;
				smallerBearing = bearing1;
			}
			
			//Adjust bearings so smaller is zero, and larger is relative to smaller
			double adjustedBearing = largerBearing - smallerBearing;
			if (adjustedBearing > 180) {
				//Larger bearing is on West side of compass dial, so average will be half way
				//between this and 360 deg.
				meanBearing = UtilityFunctions.adjustBearingWithinDegreesRange( 360 - ((360 - adjustedBearing) / 2) + smallerBearing, 0, 360);
			}
			else {
				//Larger bearing is on East side of compass dial, so average will be half way
				//between this and 0 deg.
				meanBearing = (adjustedBearing / 2) + smallerBearing;
			}
		}
		
		return meanBearing;
	}
	
	/**
	 * Test method which calculates the numerical average of three bearings using logical cases 
	 * for relative positions of the three legs when all within 180 segment of each other. This
	 * method essentially adds the three bearings together and divides by three to get an average
	 * but also handles the case where the bearings are either side of North (i.e. 359, 357, 1
	 * should average to 359 and not 239)
	 */
	private double numericalMeanBearing3( double bearing1, double bearing2, double bearing3 ) {
		
		double bearingA = bearing1;
		double bearingB = bearing2;
		double bearingC = bearing3;
		double meanBearing;
		
		//If bearings are the same then average is the value of any bearing
		if ( ( bearingA == bearingB ) && ( bearingA == bearingC ) ) {
			meanBearing = bearingA;
		}
		else {
			//Check for case where all bearings are in the northern half of the compass dial
			//and split either side of North
			if ( ( ( bearingA > 270 ) ||  ( bearingA < 90 ) )
			&& ( ( bearingB > 270 ) ||  ( bearingB < 90 ) )
			&& ( ( bearingC > 270 ) ||  ( bearingC < 90 ) ) ) {
				if ( ( bearingA < 180 )	|| ( bearingB < 180 ) || ( bearingC < 180 ) ) {
					//At least one bearing on East side of dial, make any West side bearings negative
					if ( bearingA > 180 ) {
						bearingA -= 360;
					}
					if ( bearingB > 180 ) {
						bearingB -= 360;
					}
					if ( bearingC > 180 ) {
						bearingC -= 360;
					}
				}
			}
			//Determine which is the smallest and largest value of the three bearings
			if ( bearingA > bearingB ) {
				double park = bearingA;
				bearingA = bearingB;
				bearingB = park;
			}
			if ( bearingA > bearingC ) {
				double park = bearingA;
				bearingA = bearingC;
				bearingC = park;
			}
			if ( bearingB > bearingC ) {
				double park = bearingB;
				bearingB = bearingC;
				bearingC = park;
			}
			
			//Adjust bearings so smallest is zero, and larger are relative to smallest
			double adjustedBearing2 = bearingB - bearingA;
			double adjustedBearing3 = bearingC - bearingA;
			if (adjustedBearing3 > 180) {
				//Bearings span more than 180 degree range. Not supported by this function.
				meanBearing = -999;
			}
			else {
				//All bearings between 0-180 degrees, so safe to average
				meanBearing = ( (adjustedBearing2 + adjustedBearing3) / 3) + bearingA;
			}
		}
		
		return UtilityFunctions.adjustBearingWithinDegreesRange(meanBearing, 0, 360);
	}

	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#averageCompassBearings(double[] bearings)}.
	 */
	@Test
	public void testMeanBearings() {
		//Test average trend of a pair of bearings using vector averaging (atan2 method) is the same as
		//the numerical average of the two angles, for values in all possible ranges
		for (int diff = 0; diff < 180; diff += 4) {
			for (int start = 0; start < 360 - diff; start += 1) {
				double a = start;
				double b = a + diff + 0.07;
				
				double expectedMean = numericalMeanBearing2(a, b);
				testMean2( a, b, expectedMean);
				testMean2( b, a, expectedMean);
			}
		}

		for (int diff = 0; diff < 180; diff += 4) {
			for (int start = 1; start < 90; start += 1) {
				double a = start;
				double b = 360 - a - diff + 0.02;
				
				double expectedMean = numericalMeanBearing2(a, b);
				testMean2( a, b, expectedMean);
				testMean2( b, a, expectedMean);
			}
		}
	}
	
	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#averageCompassBearings(double[] bearings)}.
	 */
	@Test
	public void testMeanCompassBearing() {
		
		double expectedMean = 0.2;
		double mean;
		
		mean = calcMeanBearing(0.1, 0.3);
		assertEquals("Mean bearing for 2 similar values just over zero.", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 180.0;
		mean = calcMeanBearing(179, 181);
		assertEquals("Mean bearing for 2 similar values around 180 deg.", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 180.0;
		mean = calcMeanBearing(90, 270);
		assertEquals("Mean bearing for 2 values exactly East and West", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 180.0;
		mean = calcMeanBearing(270, 90);
		assertEquals("Mean bearing for 2 values exactly West and East", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 0.0;
		mean = calcMeanBearing(271, 89);
		assertEquals("Mean bearing for 2 values just North of West and East", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 320.0;
		mean = calcMeanBearing(20, 260);
		assertEquals("Mean bearing for 2 values over 180 degrees different", expectedMean, mean, meanBearingTolerance);

		//Some pairs to survey legs and expected average trend of passage
		testMean2( 0.0, 0.0, 0.0);
		testMean2( 0.0, 24.0, 12.0);
		testMean2( 90.0, 0.0, 45.0);
		testMean2( 120.0, 0.0, 60.0);
		testMean2( 180.0, 180.0, 180.0);
		testMean2( 180.0, 359.0, 269.5);
		
	}

	@Test
	public void testMeanBearings3() {
		boolean rigourousTests = false;
		
		//Default increments for fast testing (test takes approx. 1 sec. with these values)
		double mainBearingIncrement = 7.3; //Bearing 1 tested from 0-360 deg in steps of this size
		double bearingSpreadIncrement = 0.3; //Maximum difference between all 3 bearings varies from 0-5 deg in steps of this size
		double bearingSpreadDiffIncrement = 0.07; //Difference between bearing 1 and 2 varies in steps of this size between min and max.
		
		if ( rigourousTests ) {
			//Use smaller increments for better test coverage of numeric space at cost of running time
			// (test takes approx. 1 minute with these values)
			mainBearingIncrement = 0.37;
			bearingSpreadIncrement = 0.13;
			bearingSpreadDiffIncrement = 0.07;
		}

		//Full revolution for bearing 1
		for (double start = 0; start < 360; start += mainBearingIncrement) {
			double bearing1 = start;
			//Vary maximum bearing spread from 0 - 5 degrees
			for (double diff = 0; diff < 5; diff += bearingSpreadIncrement) {
				double bearing3 = start + diff;
				//Vary difference between 0 and max spread
				for (double mid = 0; mid < diff; mid += bearingSpreadDiffIncrement) {
					double bearing2 = start + mid;
					//Test all ways
					double a = bearing1;
					double b = UtilityFunctions.adjustBearingWithinDegreesRange(bearing2, 0, 360);
					double c = UtilityFunctions.adjustBearingWithinDegreesRange(bearing3, 0, 360);
					testMean3VerusNumericalAverage( a, b, c);
					testMean3VerusNumericalAverage( a, c, b);
					testMean3VerusNumericalAverage( b, a, c);
					testMean3VerusNumericalAverage( b, c, a);
					testMean3VerusNumericalAverage( c, a, b);
					testMean3VerusNumericalAverage( c, b, a);
				}
			}
		}
	}

	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#averageCompassBearings(double[] bearings)}.
	 */
	@Test
	public void testAverageCompassBearings() {
		
		double expectedMean;
		double mean;
		
		expectedMean = 0.2;
		mean = calcMeanBearing(0.1, 0.2, 0.3);
		assertEquals("Mean bearing for 3 similar values just over zero.", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 180.0;
		mean = calcMeanBearing(179, 180, 181);
		assertEquals("Mean bearing for 3 similar values around 180 deg.", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 359.5;
		mean = calcMeanBearing(359.9, 359.2, 359.4);
		assertEquals("Mean bearing for 3 similar values around 359.5 deg.", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 0.09;
		mean = calcMeanBearing(359.94, 0.21, 0.12);
		assertEquals("Mean bearing for 3 values either side of North", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 359.9966667;
		mean = calcMeanBearing(359.94, 0.03, 0.02);
		assertEquals("Mean bearing for 3 values either side of North", expectedMean, mean, meanBearingTolerance);
		
		//These bearings spread over 10 degree range show difference between mean vector angle and 
		//straight numerical averaging methods of calculating. So the correct answer is not 2.000
		expectedMean = 2.00244;
		mean = calcMeanBearing(356, 4, 6);
		assertEquals("Mean bearing for 3 values around 0", expectedMean, mean, meanBearingTolerance);
		
		//These bearings spread over 10 degree range show difference between mean vector angle and 
		//straight numerical averaging methods of calculating. So the correct answer is not 358.000
		expectedMean = 358.00244;
		mean = calcMeanBearing(2, 352, 0);
		assertEquals("Mean bearing for 3 values around 0", expectedMean, mean, meanBearingTolerance);
		
		//These bearings spread over 10 degree range show difference between mean vector angle and 
		//straight numerical averaging methods of calculating. So the correct answer is not 43.000
		expectedMean = 43.002;
		mean = calcMeanBearing(45, 37, 47);
		assertEquals("Mean bearing for 3 values around 45", expectedMean, mean, meanBearingTolerance);
		
		//These bearings spread over 10 degree range show difference between mean vector angle and 
		//straight numerical averaging methods of calculating. So the correct answer is not 88.000
		expectedMean = 88.002;
		mean = calcMeanBearing(90,92,82);
		assertEquals("Mean bearing for 3 values around 90", expectedMean, mean, meanBearingTolerance);
		
		//These bearings spread over 10 degree range show difference between mean vector angle and 
		//straight numerical averaging methods of calculating. So the correct answer is not 133.000
		expectedMean = 133.002;
		mean = calcMeanBearing(135,137,127);
		assertEquals("Mean bearing for 3 values around 135", expectedMean, mean, meanBearingTolerance);
		
		//These bearings spread over 10 degree range show difference between mean vector angle and 
		//straight numerical averaging methods of calculating. So the correct answer is not 178.000
		expectedMean = 178.002;
		mean = calcMeanBearing(180,182,172);
		assertEquals("Mean bearing for 3 values around 180", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 223.002;
		mean = calcMeanBearing(225,227,217);
		assertEquals("Mean bearing for 3 values around 225", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 268.002;
		mean = calcMeanBearing(270,272,262);
		assertEquals("Mean bearing for 3 values around 270", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 313.002;
		mean = calcMeanBearing(315,317,307);
		assertEquals("Mean bearing for 3 values around 315", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 357.002;
		mean = calcMeanBearing(359,1,351);
		assertEquals("Mean bearing for 3 values around 360", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 4.610;
		mean = calcMeanBearing(0.0, 0.0, 13.86);
		assertEquals("Mean bearing for 3 values around 360", expectedMean, mean, meanBearingTolerance);
		
		//These bearings spread over 22.5 degree range show difference between mean vector angle and 
		//straight numerical averaging methods of calculating. So the correct answer is not 116.533
		expectedMean = 116.548;
		testMean3(104.06, 118.87, 126.67, expectedMean);
		
		//These bearings spread over 160 degree range show difference between mean vector angle and 
		//straight numerical averaging methods of calculating. So the correct answer is not 126.667
		//as clearly with bearings pulling almost North or South the average trend would be in the 
		//North-South direction, and pulling Eastwards nearly as much as a numerical average suggests.
		expectedMean = 162.122;
		testMean3(180, 180, 20, expectedMean);
		
	}
	
	/**
	 * Test method for helper method in this test file.
	 */
	@Test
	public void testNumericalMeanBearing3() {
		
		double expectedMean = 0.2;
		double mean;
		
		mean = numericalMeanBearing3(0.1, 0.2, 0.3);
		assertEquals("Mean bearing for 3 similar values just over zero.", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 180.0;
		mean = numericalMeanBearing3(179, 180, 181);
		assertEquals("Mean bearing for 3 similar values around 180 deg.", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 359.5;
		mean = numericalMeanBearing3(359.9, 359.2, 359.4);
		assertEquals("Mean bearing for 3 similar values around 359.5 deg.", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 0.09;
		mean = numericalMeanBearing3(359.94, 0.21, 0.12);
		assertEquals("Mean bearing for 3 values either side of North", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 359.9966667;
		mean = numericalMeanBearing3(359.94, 0.03, 0.02);
		assertEquals("Mean bearing for 3 values either side of North", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 2;
		mean = numericalMeanBearing3(356, 4, 6);
		assertEquals("Mean bearing for 3 values around 0", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 358;
		mean = numericalMeanBearing3(2, 352, 0);
		assertEquals("Mean bearing for 3 values around 0", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 43;
		mean = numericalMeanBearing3(45, 37, 47);
		assertEquals("Mean bearing for 3 values around 45", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 88;
		mean = numericalMeanBearing3(90,92,82);
		assertEquals("Mean bearing for 3 values around 90", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 133;
		mean = numericalMeanBearing3(135,137,127);
		assertEquals("Mean bearing for 3 values around 135", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 178;
		mean = numericalMeanBearing3(180,182,172);
		assertEquals("Mean bearing for 3 values around 180", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 223;
		mean = numericalMeanBearing3(225,227,217);
		assertEquals("Mean bearing for 3 values around 225", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 268;
		mean = numericalMeanBearing3(270,272,262);
		assertEquals("Mean bearing for 3 values around 270", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 313;
		mean = numericalMeanBearing3(315,317,307);
		assertEquals("Mean bearing for 3 values around 315", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 357;
		mean = numericalMeanBearing3(359,1,351);
		assertEquals("Mean bearing for 3 values around 360", expectedMean, mean, meanBearingTolerance);
		
		expectedMean = 116.533333;
		mean = numericalMeanBearing3(104.06, 118.87, 126.67);
		assertEquals("Mean bearing for 3 values around 360", expectedMean, mean, meanBearingTolerance);
		
	}	

	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#bearingDifferenceDegrees(double angle1, double angle2)}.
	 */
	@Test
	public void testBearingDifference() {
		
		double expectedDiff = 10.0;
		double calcDiff;
		
		calcDiff = UtilityFunctions.bearingDifferenceDegrees( 5.0, 355.0 );
		assertEquals("Difference between two bearings either side of North", expectedDiff, calcDiff, meanBearingTolerance);
		
		expectedDiff = 180.0;
		calcDiff = UtilityFunctions.bearingDifferenceDegrees( 90.0, 270.0 );
		assertEquals("Difference between two bearings of exactly East and West", expectedDiff, calcDiff, meanBearingTolerance);
		
		expectedDiff = 4.8;
		calcDiff = UtilityFunctions.bearingDifferenceDegrees( 120.4, 125.2 );
		assertEquals("Difference between two bearings of similar direction", expectedDiff, calcDiff, meanBearingTolerance);
	}
	
	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#stringToDate( String dateString, String dateFormat )},
	 *                 {@link footleg.cavesurvey.tools.UtilityFunctions#dateToString( Date date, String dateFormat )}.
	 */
	@Test
	public void testDateStringConversionPocketTopo() {
		String dateOutFormat = UtilityFunctions.POCKETTOPO_DATE_FORMAT;
		String dateInFormat = "dd-MM-yyyy";
		String dateIn = "27-12-1984";
		String dateExpectedOut = "1984/12/27";
		
		doDateStringConversion(dateIn, dateInFormat, dateExpectedOut, dateOutFormat);

		dateIn = "01-02-1947";
		dateExpectedOut = "1947/02/01";
		doDateStringConversion(dateIn, dateInFormat, dateExpectedOut, dateOutFormat);

		dateIn = "31-12-2012";
		dateExpectedOut = "2012/12/31";
		doDateStringConversion(dateIn, dateInFormat, dateExpectedOut, dateOutFormat);

		dateInFormat = "MM-dd-yy";
		dateIn = "03-05-07";
		dateExpectedOut = "2007/03/05";
		doDateStringConversion(dateIn, dateInFormat, dateExpectedOut, dateOutFormat);
}
	
	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#stringToDate( String dateString, String dateFormat )},
	 *                 {@link footleg.cavesurvey.tools.UtilityFunctions#dateToString( Date date, String dateFormat )}.
	 */
	@Test
	public void testDateStringConversionSurvex() {
		String dateOutFormat = UtilityFunctions.SURVEXDATE_FORMAT;
		String dateInFormat = "dd-MM-yyyy";
		String dateIn = "27-12-1984";
		String dateExpectedOut = "1984.12.27";
		
		doDateStringConversion(dateIn, dateInFormat, dateExpectedOut, dateOutFormat);

		dateIn = "01-02-1947";
		dateExpectedOut = "1947.02.01";
		doDateStringConversion(dateIn, dateInFormat, dateExpectedOut, dateOutFormat);

		dateIn = "31-12-2012";
		dateExpectedOut = "2012.12.31";
		doDateStringConversion(dateIn, dateInFormat, dateExpectedOut, dateOutFormat);

		dateInFormat = "MM-dd-yy";
		dateIn = "03-05-07";
		dateExpectedOut = "2007.03.05";
		doDateStringConversion(dateIn, dateInFormat, dateExpectedOut, dateOutFormat);
	}

	/**
	 * Converts a string from one data format to another, with parse exception handling.
	 * Tests that the string generated matches the specified expected string.
	 */
	private void doDateStringConversion( String dateIn, String dateInFormat, 
			                             String dateExpectedOut, String dateOutFormat ) {
		try {
			Date testDate = UtilityFunctions.stringToDate( dateIn, dateInFormat);
			String dateOut = UtilityFunctions.dateToString( testDate, dateOutFormat );
			assertEquals("Date converted from one string format to another", dateExpectedOut, dateOut);
		} catch (ParseException e) {
			fail("Date string '" + dateIn + "' raised parse exception using format '" + dateInFormat + "'.");
		} 
	}

	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#parseDataStringIntoDataItems( String dataIn )}.
	 */
	@Test
	public void testParseDataLineIntoDataItems() {
		String dataLine = "";
		String[] dataItems = new String[6];
		
		dataItems[0] = "Item1";
		dataItems[1] = "2";
		dataItems[2] = "Item3";
		dataItems[3] = "";
		dataItems[4] = "Item 5";
		dataItems[5] = "Item6";
		
		dataLine = dataItems[0] + " " + dataItems[1] + "  " + dataItems[2] + "\t\"" + dataItems[3] + "\" \t\"" + dataItems[4] + "\"\t " + dataItems[5];
		splitStringIntoItemsTest( dataLine, dataItems );

		dataLine = dataItems[0] + "      " + dataItems[1] + "   " + dataItems[2] + " \t \"" + dataItems[3] + "\" \t   \"" + dataItems[4] + "\"   \t   \t " + dataItems[5];
		splitStringIntoItemsTest( dataLine, dataItems );
		
		dataItems = new String[1];
		dataItems[0] = "Item1";
		dataLine = dataItems[0];
		splitStringIntoItemsTest( dataLine, dataItems );

		dataLine = "\"" + dataItems[0] + "\"";
		splitStringIntoItemsTest( dataLine, dataItems );

		dataItems[0] = "A long sentance of misc text";
		dataLine = "\"" + dataItems[0] + "\"";
		splitStringIntoItemsTest( dataLine, dataItems );

		dataItems[0] = "Some\ttext\ncontaining\rtabs and    new        line chars     ";
		dataLine = "\"" + dataItems[0] + "\"";
		splitStringIntoItemsTest( dataLine, dataItems );

		dataItems = new String[2];
		
		dataItems[0] = "Item\t1";
		dataItems[1] = "Item 2";
		dataLine = "\"" + dataItems[0] + "\"" + "\"" + dataItems[1] + "\"";
		splitStringIntoItemsTest( dataLine, dataItems );
	}
		
	private void splitStringIntoItemsTest( String dataLine, String[] items ) {
		//Parse data string into items
		List<String> parsedData = UtilityFunctions.parseDataStringIntoDataItems( dataLine );
		
		//Check expected number of items were returned
		assertEquals( "Data string '" + dataLine + "' returns expected number of items.", items.length, parsedData.size() );
		
		//Check each item returned matches expected data item
		for (int i = 0; i < items.length; i++) {
			assertEquals( "Item " + (i + 1) + " in '" + dataLine + "'.", items[i], parsedData.get(i) );
		}
	}
	
	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#convertToLinearSeries( SurveySeries seriesIn )}.
	 */
	@Test
	public void testConvertToLinearSeriesOrderedT() {
		CmdLineLogger logger = new CmdLineLogger();
		//Create series of test data
		SurveySeries masterSeries = new SurveySeries("test");
		//Add legs to series
		masterSeries.addLeg( TestHelper.createTestLeg(1,2,1.0,110.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(2,3,1.0,100.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(3,4,1.0,190.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(4,5,1.0,180.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(3,6,1.0,270.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(6,7,1.0,290.0,0.0) );
		
		//Convert to single series
		SurveySeries convertedSeries = UtilityFunctions.convertToLinearSeries( masterSeries, logger );
		
		assertEquals("Expect 2 series from t-shaped single series.", 2, convertedSeries.innerSeriesCount() );
		assertEquals("Expect links between all series.", 1, convertedSeries.getLinks().size() );
		SeriesLink firstLink = convertedSeries.getLinks().get(0);
		assertEquals("Expect link from series 1.", convertedSeries.getInnerSeries(0).getSeriesName(), firstLink.getSeries2() );
		assertEquals("Expect link to series 2.", convertedSeries.getInnerSeries(1).getSeriesName(), firstLink.getSeries1() );
		
	}

	/**
	 * Test method for {@link footleg.cavesurvey.tools.UtilityFunctions#compareStringLists( List<String> list1, List<String> list2 )}.
	 */
	@Test
	public void testCompareStringLists() {
		List<String> list1 =  new ArrayList<String>();
		List<String> list2 =  new ArrayList<String>();
		
		//Test empty lists are the same
		assertTrue( "Expect pair of empty string lists to be considered identical.", UtilityFunctions.compareStringLists( list1, list2 ) );
		
		list1.add("A String");
		
		//Test empty list against list with data are not the same
		assertFalse( "Expect an empty string list not to be considered identical to one containing data.", UtilityFunctions.compareStringLists( list1, list2 ) );
		
		list1.add("Another");
		list1.add("More Data");
		
		//Test empty list against list with data are not the same
		assertFalse( "Expect an empty string list to not to be considered identical to one containing data.", UtilityFunctions.compareStringLists( list1, list2 ) );
		
		list2.add("A String");
		
		//Test lists for different sizes are not the same
		assertFalse( "Expect string lists not to be considered identical when containing different numbers of items.", UtilityFunctions.compareStringLists( list1, list2 ) );
		
		list2.add("Another");
		
		//Test lists for different sizes are not the same
		assertFalse( "Expect string lists not to be considered identical when containing different numbers of items.", UtilityFunctions.compareStringLists( list1, list2 ) );
		
		list2.add("More Data");
		
		//Test lists containing the same data are the same
		assertTrue( "Expect string lists to be considered identical when containing the same items.", UtilityFunctions.compareStringLists( list1, list2 ) );
		
		list2.clear();
		list2.add("Another");
		list2.add("A String");
		list2.add("More Data");
		
		//Test lists containing the same data are the same
		assertFalse( "Expect string lists not to be considered identical when containing the same items in a different order.", UtilityFunctions.compareStringLists( list1, list2 ) );
		
	}
	
	/**
	 * Test method for 
	 * {@link footleg.cavesurvey.tools.UtilityFunctions#lengthToMetres(double length, LengthUnit units)},
	 * {@link footleg.cavesurvey.tools.UtilityFunctions#lengthFromMetres(double metrelength, LengthUnit units)}.
	 * Tests conversion from feet and yards to metres and back.
	 */
	@Test
	public void testLengthUnitsConversions() {
		double feet = 100;
		double metres = 30.48;
		
		assertEquals("Converting feet to metres", metres, 
				UtilityFunctions.lengthToMetres(feet, LengthUnit.Feet), 1e-6);
		assertEquals("Converting inclincation in degrees to percentage slope", feet , 
				UtilityFunctions.lengthFromMetres(metres, LengthUnit.Feet), 1e-6);
		
		double yards = 40;
		metres = 36.576;
		assertEquals("Converting metres to yards", metres, 
				UtilityFunctions.lengthToMetres(yards, LengthUnit.Yards), 1e-6);
		assertEquals("Converting yards to metres", yards , 
				UtilityFunctions.lengthFromMetres(metres, LengthUnit.Yards), 1e-6);
	}
	
	/**
	 * Test method for 
	 * {@link footleg.cavesurvey.tools.UtilityFunctions#bearingFromDegrees(double bearingDegrees, BearingUnit units)},
	 * {@link footleg.cavesurvey.tools.UtilityFunctions#bearingToDegrees(double bearing, BearingUnit units)}.
	 * Tests conversion from percent to degrees and back. Tests conversion from grads to degrees and back.
	 */
	@Test
	public void testBearingToDegreesConversions() {
		double minutes = 30;
		double degrees = 0.5;
		
		assertEquals("Converting bearing in minutes to degrees", degrees, 
				UtilityFunctions.bearingToDegrees(minutes, BearingUnit.Minutes), 1e-6);
		assertEquals("Converting bearing in degrees to minutes", minutes , 
				UtilityFunctions.bearingFromDegrees(degrees, BearingUnit.Minutes), 1e-6);
		
		double grads = 400;
		degrees = 360;
		assertEquals("Converting bearing in grads to degrees", degrees, 
				UtilityFunctions.bearingToDegrees(grads, BearingUnit.Grads), 1e-6);
		assertEquals("Converting bearing in degrees to grads", grads , 
				UtilityFunctions.bearingFromDegrees(degrees, BearingUnit.Grads), 1e-6);
	}
	
	/**
	 * Test method for 
	 * {@link footleg.cavesurvey.tools.UtilityFunctions#gradientFromDegrees(double gradientDegrees, GradientUnit units)},
	 * {@link footleg.cavesurvey.tools.UtilityFunctions#gradientToDegrees(double gradient, GradientUnit units)}.
	 * Tests conversion from percent to degrees and back. Tests conversion from grads to degrees and back.
	 */
	@Test
	public void testGradientToDegreesConversions() {
		double percent = 100;
		double degrees = 45;
		
		assertEquals("Converting percentage slope to inclincation degrees", degrees, 
				UtilityFunctions.gradientToDegrees(percent, GradientUnit.Percent), 1e-6);
		assertEquals("Converting inclincation in degrees to percentage slope", percent , 
				UtilityFunctions.gradientFromDegrees(degrees, GradientUnit.Percent), 1e-6);
		
		percent = 12.5;
		degrees = 7.125016349;
		assertEquals("Converting percentage slope to inclincation degrees", degrees, 
				UtilityFunctions.gradientToDegrees(percent, GradientUnit.Percent), 1e-6);
		assertEquals("Converting inclincation in degrees to percentage slope", percent , 
				UtilityFunctions.gradientFromDegrees(degrees, GradientUnit.Percent), 1e-6);
		
		double grads = 400;
		degrees = 360;
		assertEquals("Converting inclincation grads to degrees", degrees, 
				UtilityFunctions.gradientToDegrees(grads, GradientUnit.Grads), 1e-6);
		assertEquals("Converting inclincation in degrees to grads", grads , 
				UtilityFunctions.gradientFromDegrees(degrees, GradientUnit.Grads), 1e-6);
		
		grads = 100;
		degrees = 90;
		assertEquals("Converting inclincation grads to degrees", degrees, 
				UtilityFunctions.gradientToDegrees(grads, GradientUnit.Grads), 1e-6);
		assertEquals("Converting inclincation in degrees to grads", grads , 
				UtilityFunctions.gradientFromDegrees(degrees, GradientUnit.Grads), 1e-6);
	}

}