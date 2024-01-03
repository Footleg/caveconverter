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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;

/**
 * Unit test class for {@link footleg.cavesurvey.data.model.SurveyLeg}
 * 
 * @author Footleg
 * @version 2024.01.02 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class SurveyLegTest {

	// double easting = 4800456.0;
	// double northing = 4800789.75;
	// double altitude = 8900.45;

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setFromStn(int)}.
	 */
	@Test
	public void testSetFromStn() {
		SurveyLeg leg = new SurveyLeg();

		int stnID = 543;
		SurveyStation stn = new SurveyStation(stnID);
		leg.setFromStn(stn);

		assertEquals(stnID, leg.getFromStn().getId());
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setToStn(int)}.
	 */
	@Test
	public void testSetToStn() {
		SurveyLeg leg = new SurveyLeg();
		int stnID = 12345;
		SurveyStation stn = new SurveyStation(stnID);
		leg.setToStn(stn);

		assertEquals(stnID, leg.getToStn().getId());
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setLength(double)}.
	 */
	@Test
	public void testSetLength() {
		SurveyLeg leg = new SurveyLeg();
		double length = 123.45;

		leg.setLength(length, LengthUnit.Metres);

		assertEquals(length, leg.getLength(LengthUnit.Metres), 0.0);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setCompass(double)}.
	 */
	@Test
	public void testSetCompass() {
		SurveyLeg leg = new SurveyLeg();
		double compass = 123.45;

		leg.setCompass(compass, BearingUnit.Degrees);

		assertEquals(compass, leg.getCompass(BearingUnit.Degrees), 0.0);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setClino(double)}.
	 */
	@Test
	public void testSetClino() {
		SurveyLeg leg = new SurveyLeg();
		double clino = 123.45;

		leg.setClino(clino, GradientUnit.Degrees);

		assertEquals(clino, leg.getClino(GradientUnit.Degrees), 0.0);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setLeft(double)}.
	 */
	@Test
	public void testSetLeft() {
		SurveyLeg leg = new SurveyLeg();
		double length = 0.45;
		leg.setLeft(length, LengthUnit.Metres);
		assertEquals(length, leg.getLeft(LengthUnit.Metres), 0.0);

		length = 0.0;
		leg.setLeft(length, LengthUnit.Metres);
		assertEquals(length, leg.getLeft(LengthUnit.Metres), 0.0);

		// TODO: Should error
		length = -0.1;
		leg.setLeft(length, LengthUnit.Metres);
		assertEquals(length, leg.getLeft(LengthUnit.Metres), 0.0);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setRight(double)}.
	 */
	@Test
	public void testSetRight() {
		SurveyLeg leg = new SurveyLeg();
		double length = 0.45;
		leg.setRight(length, LengthUnit.Metres);
		assertEquals(length, leg.getRight(LengthUnit.Metres), 0.0);

		length = 0.0;
		leg.setRight(length, LengthUnit.Metres);
		assertEquals(length, leg.getRight(LengthUnit.Metres), 0.0);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setUp(double)}.
	 */
	@Test
	public void testSetUp() {
		SurveyLeg leg = new SurveyLeg();
		double length = 0.45;
		leg.setUp(length, LengthUnit.Metres);
		assertEquals(length, leg.getUp(LengthUnit.Metres), 0.0);

		length = 0.0;
		leg.setUp(length, LengthUnit.Metres);
		assertEquals(length, leg.getUp(LengthUnit.Metres), 0.0);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setDown(double)}.
	 */
	@Test
	public void testSetDown() {
		SurveyLeg leg = new SurveyLeg();
		double length = 0.45;
		leg.setDown(length, LengthUnit.Metres);
		assertEquals(length, leg.getDown(LengthUnit.Metres), 0.0);

		length = 0.0;
		leg.setDown(length, LengthUnit.Metres);
		assertEquals(length, leg.getDown(LengthUnit.Metres), 0.0);
	}

	// /**
	// * Test method for {@link
	// footleg.cavesurvey.data.model.SurveyLeg#setFixedStn(boolean, double, double,
	// double)},
	// * {@link footleg.cavesurvey.data.model.SurveyLeg#clearFixedStn()} and
	// associated getters.
	// */
	// @Test
	// public void testSetFixedStnFrom() {
	// SurveyLeg leg = new SurveyLeg();
	//
	// leg.setFixedStn(true, easting, northing, altitude);
	//
	// assertEquals( easting, leg.getEasting(), 0.0 );
	// assertEquals( northing, leg.getNorthing(), 0.0 );
	// assertEquals( altitude, leg.getAltitude(), 0.0 );
	// assertTrue( leg.hasFixedStn() );
	// assertTrue( leg.isFromStnFixed() );
	// assertTrue( leg.isToStnFixed() == false );
	//
	// leg.clearFixedStn();
	// assertTrue( leg.hasFixedStn() == false );
	// assertTrue( leg.isFromStnFixed() == false );
	// assertTrue( leg.isToStnFixed() == false );
	// }
	//
	// /**
	// * Test method for {@link
	// footleg.cavesurvey.data.model.SurveyLeg#setFixedStn(boolean, double, double,
	// double)},
	// * {@link footleg.cavesurvey.data.model.SurveyLeg#clearFixedStn()} and
	// associated getters.
	// */
	// @Test
	// public void testSetFixedStnTo() {
	// SurveyLeg leg = new SurveyLeg();
	//
	// leg.setFixedStn(false, easting, northing, altitude);
	//
	// assertEquals( easting, leg.getEasting(), 0.0 );
	// assertEquals( northing, leg.getNorthing(), 0.0 );
	// assertEquals( altitude, leg.getAltitude(), 0.0 );
	// assertTrue( leg.hasFixedStn() );
	// assertTrue( leg.isFromStnFixed() == false );
	// assertTrue( leg.isToStnFixed() );
	//
	// leg.clearFixedStn();
	// assertTrue( leg.hasFixedStn() == false );
	// assertTrue( leg.isFromStnFixed() == false );
	// assertTrue( leg.isToStnFixed() == false );
	// }

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#setSplay(boolean)}.
	 */
	@Test
	public void testSetSplay() {
		SurveyLeg leg = new SurveyLeg();

		leg.setSplay(true);
		assertTrue(leg.isSplay());

		leg.setSplay(false);
		assertTrue(leg.isSplay() == false);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#compareTo(footleg.cavesurvey.data.model.SurveyLeg)}.
	 */
	@Test
	public void testCompareTo() {
		SurveyLeg leg1 = new SurveyLeg();
		SurveyLeg leg2 = new SurveyLeg();
		SurveyLeg leg3 = new SurveyLeg();
		SurveyLeg leg4 = new SurveyLeg();

		int stnID1 = 1;
		int stnID2 = 2;
		int stnID3 = 3;
		int stnID4 = 4;

		SurveyStation stn1 = new SurveyStation(stnID1);
		SurveyStation stn2 = new SurveyStation(stnID2);
		SurveyStation stn3 = new SurveyStation(stnID3);
		SurveyStation stn4 = new SurveyStation(stnID4);

		leg1.setFromStn(stn1);
		leg1.setToStn(stn2);
		leg2.setFromStn(stn2);
		leg2.setToStn(stn3);
		leg3.setFromStn(stn2);
		leg3.setToStn(stn4);
		leg4.setFromStn(stn2);
		leg4.setToStn(stn1);

		assertTrue(leg1.compareTo(leg2) < 0);
		assertTrue(leg2.compareTo(leg1) > 0);
		assertTrue(leg2.compareTo(leg3) < 0);
		assertTrue(leg3.compareTo(leg2) > 0);
		assertTrue(leg2.compareTo(leg4) > 0);
		assertTrue(leg4.compareTo(leg2) < 0);

	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg#clone()}.
	 */
	@Test
	public void testCloneNormalLeg() {
		SurveyLeg leg = new SurveyLeg();
		int fromStnId = 123;
		int toStnId = 456;
		SurveyStation fromStn = new SurveyStation(fromStnId);
		SurveyStation toStn = new SurveyStation(toStnId);
		double length = 123.45;
		double compass = 123.45;
		double clino = 123.45;
		double left = 1.2;
		double right = 0.1;
		double up = 0.23;
		double down = 0.92;
		boolean splay = true;
		boolean duplicate = true;
		boolean surface = true;

		// Create leg fully populated with test data
		leg.setFromStn(fromStn);
		leg.setToStn(toStn);
		leg.setLength(length, LengthUnit.Metres);
		leg.setCompass(compass, BearingUnit.Degrees);
		leg.setClino(clino, GradientUnit.Degrees);
		leg.setLeft(left, LengthUnit.Metres);
		leg.setRight(right, LengthUnit.Metres);
		leg.setUp(up, LengthUnit.Metres);
		leg.setDown(down, LengthUnit.Metres);
		leg.setSplay(splay);
		leg.setDuplicate(duplicate);
		leg.setSurface(surface);

		boolean nosurvey = leg.isNosurvey();

		// Clone leg
		SurveyLeg legClone = leg.clone();

		// Set clone has the correct data values
		assertEquals(fromStnId, legClone.getFromStn().getId());
		assertEquals(toStnId, legClone.getToStn().getId());
		assertEquals(length, legClone.getLength(LengthUnit.Metres), 0.0);
		assertEquals(compass, legClone.getCompass(BearingUnit.Degrees), 0.0);
		assertEquals(clino, legClone.getClino(GradientUnit.Degrees), 0.0);
		assertEquals(left, legClone.getLeft(LengthUnit.Metres), 0.0);
		assertEquals(right, legClone.getRight(LengthUnit.Metres), 0.0);
		assertEquals(up, legClone.getUp(LengthUnit.Metres), 0.0);
		assertEquals(down, legClone.getDown(LengthUnit.Metres), 0.0);
		assertTrue(legClone.isSplay() == splay);
		assertTrue(legClone.isDuplicate() == duplicate);
		assertTrue(legClone.isSurface() == surface);
		assertTrue(legClone.isDiving() == false);
		assertTrue(legClone.isDepthChangeLeg() == false);
		assertTrue(legClone.isNosurvey() == nosurvey);

	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg#clone()}.
	 */
	@Test
	public void testCloneNosurveyLeg() {
		SurveyLeg leg = new SurveyLeg();
		int fromStnId = 222;
		int toStnId = 333;
		SurveyStation fromStn = new SurveyStation(fromStnId);
		SurveyStation toStn = new SurveyStation(toStnId);
		boolean splay = false;
		boolean duplicate = false;
		boolean surface = false;
		boolean nosurvey = true;

		// Create leg fully populated with test data
		leg.setFromStn(fromStn);
		leg.setToStn(toStn);
		leg.setNosurvey(nosurvey);
		leg.setSplay(splay);
		leg.setDuplicate(duplicate);
		leg.setSurface(surface);

		// Clone leg
		SurveyLeg legClone = leg.clone();

		// Set clone has the correct data values
		assertEquals(fromStnId, legClone.getFromStn().getId());
		assertEquals(toStnId, legClone.getToStn().getId());
		assertTrue(legClone.isSplay() == splay);
		assertTrue(legClone.isDuplicate() == duplicate);
		assertTrue(legClone.isSurface() == surface);
		assertTrue(legClone.isDiving() == false);
		assertTrue(legClone.isDepthChangeLeg() == false);
		assertTrue(legClone.isNosurvey() == nosurvey);

	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg#clone()}.
	 */
	@Test
	public void testCloneDivingTrueDepthsLeg() {
		SurveyLeg leg = new SurveyLeg();
		int fromStnId = 876;
		int toStnId = 543;
		SurveyStation fromStn = new SurveyStation(fromStnId);
		SurveyStation toStn = new SurveyStation(toStnId);
		double length = 9.87;
		double compass = 6.54;
		double fromDepth = 3.21;
		double toDepth = 4.56;
		double left = 9.8;
		double right = 7.0;
		double up = 6.5;
		double down = 4;
		boolean splay = false;
		boolean duplicate = false;
		boolean surface = false;

		// Create leg fully populated with test data
		leg.setFromStn(fromStn);
		leg.setToStn(toStn);
		leg.setLength(length, LengthUnit.Metres);
		leg.setCompass(compass, BearingUnit.Degrees);
		leg.setDepths(fromDepth, toDepth, LengthUnit.Metres);
		leg.setLeft(left, LengthUnit.Metres);
		leg.setRight(right, LengthUnit.Metres);
		leg.setUp(up, LengthUnit.Metres);
		leg.setDown(down, LengthUnit.Metres);
		leg.setSplay(splay);
		leg.setDuplicate(duplicate);
		leg.setSurface(surface);

		// Clone leg
		SurveyLeg legClone = leg.clone();

		// Set clone has the correct data values
		assertEquals(fromStnId, legClone.getFromStn().getId());
		assertEquals(toStnId, legClone.getToStn().getId());
		assertEquals(length, legClone.getLength(LengthUnit.Metres), 0.0);
		assertEquals(compass, legClone.getCompass(BearingUnit.Degrees), 0.0);
		assertEquals(fromDepth, legClone.getFromDepth(LengthUnit.Metres), 0.0);
		assertEquals(toDepth, legClone.getToDepth(LengthUnit.Metres), 0.0);
		assertEquals(left, legClone.getLeft(LengthUnit.Metres), 0.0);
		assertEquals(right, legClone.getRight(LengthUnit.Metres), 0.0);
		assertEquals(up, legClone.getUp(LengthUnit.Metres), 0.0);
		assertEquals(down, legClone.getDown(LengthUnit.Metres), 0.0);
		assertTrue(legClone.isSplay() == splay);
		assertTrue(legClone.isDuplicate() == duplicate);
		assertTrue(legClone.isSurface() == surface);
		assertTrue(legClone.isDiving() == true);
		assertTrue(legClone.isDepthChangeLeg() == false);
		assertTrue(legClone.isNosurvey() == false);

	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg#clone()}.
	 */
	@Test
	public void testCloneDivingDepthChangeLeg() {
		SurveyLeg leg = new SurveyLeg();
		int fromStnId = 77;
		int toStnId = 66;
		SurveyStation fromStn = new SurveyStation(fromStnId);
		SurveyStation toStn = new SurveyStation(toStnId);
		double length = 1.1;
		double compass = 2.2;
		double depthChange = 3.3;
		double left = 0.5;
		double right = 0.6;
		double up = 0.7;
		double down = 0;
		boolean splay = false;
		boolean duplicate = false;
		boolean surface = false;

		// Create leg fully populated with test data
		leg.setFromStn(fromStn);
		leg.setToStn(toStn);
		leg.setLength(length, LengthUnit.Metres);
		leg.setCompass(compass, BearingUnit.Degrees);
		leg.setDepthChange(depthChange, LengthUnit.Metres);
		leg.setLeft(left, LengthUnit.Metres);
		leg.setRight(right, LengthUnit.Metres);
		leg.setUp(up, LengthUnit.Metres);
		leg.setDown(down, LengthUnit.Metres);
		leg.setSplay(splay);
		leg.setDuplicate(duplicate);
		leg.setSurface(surface);

		// Clone leg
		SurveyLeg legClone = leg.clone();

		// Set clone has the correct data values
		assertEquals(fromStnId, legClone.getFromStn().getId());
		assertEquals(toStnId, legClone.getToStn().getId());
		assertEquals(length, legClone.getLength(LengthUnit.Metres), 0.0);
		assertEquals(compass, legClone.getCompass(BearingUnit.Degrees), 0.0);
		assertEquals(depthChange, legClone.getDepthChange(LengthUnit.Metres), 0.0);
		assertEquals(left, legClone.getLeft(LengthUnit.Metres), 0.0);
		assertEquals(right, legClone.getRight(LengthUnit.Metres), 0.0);
		assertEquals(up, legClone.getUp(LengthUnit.Metres), 0.0);
		assertEquals(down, legClone.getDown(LengthUnit.Metres), 0.0);
		assertTrue(legClone.isSplay() == splay);
		assertTrue(legClone.isDuplicate() == duplicate);
		assertTrue(legClone.isSurface() == surface);
		assertTrue(legClone.isDiving() == true);
		assertTrue(legClone.isDepthChangeLeg() == true);
		assertTrue(legClone.isNosurvey() == false);

	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg}.
	 */
	@Test
	public void testReadDefaultValues() {
		SurveyLeg leg = new SurveyLeg();

		int fromStnId = 543;
		SurveyStation stn = new SurveyStation(fromStnId);
		leg.setFromStn(stn);

		// Test default values can be read without errors
		assertEquals(fromStnId, leg.getFromStn().getId());
		assertTrue(leg.getToStn() == null);
		assertEquals(-1.0, leg.getLength(LengthUnit.Metres), 0.0);
		assertEquals(-1.0, leg.getCompass(BearingUnit.Degrees), 0.0);
		assertEquals(-99.0, leg.getClino(GradientUnit.Degrees), 0.0);
		assertEquals(0.0, leg.getLeft(LengthUnit.Metres), 0.0);
		assertEquals(0.0, leg.getRight(LengthUnit.Metres), 0.0);
		assertEquals(0.0, leg.getUp(LengthUnit.Metres), 0.0);
		assertEquals(0.0, leg.getDown(LengthUnit.Metres), 0.0);
		assertTrue(leg.isSplay() == false);
		assertTrue(leg.isDuplicate() == false);
		assertTrue(leg.isSurface() == false);
		assertTrue(leg.isDiving() == false);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#reverseDirection()}.
	 */
	@Test
	public void testReverseLeg() {
		SurveyLeg leg = new SurveyLeg();
		int fromStnId = 123;
		int toStnId = 456;
		SurveyStation fromStn = new SurveyStation(fromStnId);
		SurveyStation toStn = new SurveyStation(toStnId);
		double length = 123.45;
		double compass = 123.45;
		double clino = 123.45;
		double left = 1.2;
		double right = 0.1;
		double up = 0.23;
		double down = 0.92;
		boolean splay = true;
		boolean duplicate = true;
		boolean surface = true;

		// Create leg fully populated with test data
		leg.setFromStn(fromStn);
		leg.setToStn(toStn);
		leg.setLength(length, LengthUnit.Metres);
		leg.setCompass(compass, BearingUnit.Degrees);
		leg.setClino(clino, GradientUnit.Degrees);
		leg.setLeft(left, LengthUnit.Metres);
		leg.setRight(right, LengthUnit.Metres);
		leg.setUp(up, LengthUnit.Metres);
		leg.setDown(down, LengthUnit.Metres);
		leg.setSplay(splay);
		leg.setDuplicate(duplicate);
		leg.setSurface(surface);

		leg.reverseDirection();

		// Test default values can be read without errors
		assertEquals(toStnId, leg.getFromStn().getId());
		assertEquals(fromStnId, leg.getToStn().getId());
		assertEquals(length, leg.getLength(LengthUnit.Metres), 1e-8);
		assertEquals(compass + 180, leg.getCompass(BearingUnit.Degrees), 1e-8);
		assertEquals(-clino, leg.getClino(GradientUnit.Degrees), 1e-8);
		assertEquals(left, leg.getLeft(LengthUnit.Metres), 1e-8);
		assertEquals(right, leg.getRight(LengthUnit.Metres), 1e-8);
		assertEquals(up, leg.getUp(LengthUnit.Metres), 1e-8);
		assertEquals(down, leg.getDown(LengthUnit.Metres), 1e-8);
		assertTrue(leg.isSplay() == splay);
		assertTrue(leg.isDuplicate() == duplicate);
		assertTrue(leg.isSurface() == surface);
		assertTrue(leg.isDiving() == false);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#reverseDirection()}.
	 */
	@Test
	public void testReverseDivingTrueDepthsLeg() {
		SurveyLeg leg = new SurveyLeg();
		int fromStnId = 77;
		int toStnId = 66;
		SurveyStation fromStn = new SurveyStation(fromStnId);
		SurveyStation toStn = new SurveyStation(toStnId);
		double length = 1.1;
		double compass = 220.2;
		double fromDepth = 9.7;
		double toDepth = 7.89;
		double left = 0.0;
		double right = 0.60;
		double up = 0.0;
		double down = 0.99;
		boolean splay = false;
		boolean duplicate = true;
		boolean surface = false;

		// Create leg fully populated with test data
		leg.setFromStn(fromStn);
		leg.setToStn(toStn);
		leg.setLength(length, LengthUnit.Metres);
		leg.setCompass(compass, BearingUnit.Degrees);
		leg.setDepths(fromDepth, toDepth, LengthUnit.Metres);
		leg.setLeft(left, LengthUnit.Metres);
		leg.setRight(right, LengthUnit.Metres);
		leg.setUp(up, LengthUnit.Metres);
		leg.setDown(down, LengthUnit.Metres);
		leg.setSplay(splay);
		leg.setDuplicate(duplicate);
		leg.setSurface(surface);

		leg.reverseDirection();

		// Test default values can be read without errors
		assertEquals(toStnId, leg.getFromStn().getId());
		assertEquals(fromStnId, leg.getToStn().getId());
		assertEquals(length, leg.getLength(LengthUnit.Metres), 1e-8);
		assertEquals(compass - 180, leg.getCompass(BearingUnit.Degrees), 1e-8);
		assertEquals(toDepth, leg.getFromDepth(LengthUnit.Metres), 0.0);
		assertEquals(fromDepth, leg.getToDepth(LengthUnit.Metres), 0.0);
		assertEquals(left, leg.getLeft(LengthUnit.Metres), 1e-8);
		assertEquals(right, leg.getRight(LengthUnit.Metres), 1e-8);
		assertEquals(up, leg.getUp(LengthUnit.Metres), 1e-8);
		assertEquals(down, leg.getDown(LengthUnit.Metres), 1e-8);
		assertTrue(leg.isSplay() == splay);
		assertTrue(leg.isDuplicate() == duplicate);
		assertTrue(leg.isSurface() == surface);
		assertTrue(leg.isDiving() == true);
		assertTrue(leg.isDepthChangeLeg() == false);
	}

	/**
	 * Test method for
	 * {@link footleg.cavesurvey.data.model.SurveyLeg#reverseDirection()}.
	 */
	@Test
	public void testReverseDivingDepthChangeLeg() {
		SurveyLeg leg = new SurveyLeg();
		int fromStnId = 77;
		int toStnId = 66;
		SurveyStation fromStn = new SurveyStation(fromStnId);
		SurveyStation toStn = new SurveyStation(toStnId);
		double length = 1.1;
		double compass = 220.2;
		double depthChange = 3.3;
		double left = 0.5;
		double right = 0.6;
		double up = 0.7;
		double down = 0;
		boolean splay = false;
		boolean duplicate = false;
		boolean surface = false;

		// Create leg fully populated with test data
		leg.setFromStn(fromStn);
		leg.setToStn(toStn);
		leg.setLength(length, LengthUnit.Metres);
		leg.setCompass(compass, BearingUnit.Degrees);
		leg.setDepthChange(depthChange, LengthUnit.Metres);
		leg.setLeft(left, LengthUnit.Metres);
		leg.setRight(right, LengthUnit.Metres);
		leg.setUp(up, LengthUnit.Metres);
		leg.setDown(down, LengthUnit.Metres);
		leg.setSplay(splay);
		leg.setDuplicate(duplicate);
		leg.setSurface(surface);

		leg.reverseDirection();

		// Test default values can be read without errors
		assertEquals(toStnId, leg.getFromStn().getId());
		assertEquals(fromStnId, leg.getToStn().getId());
		assertEquals(length, leg.getLength(LengthUnit.Metres), 1e-8);
		assertEquals(compass - 180, leg.getCompass(BearingUnit.Degrees), 1e-8);
		assertEquals(-depthChange, leg.getDepthChange(LengthUnit.Metres), 1e-8);
		assertEquals(left, leg.getLeft(LengthUnit.Metres), 1e-8);
		assertEquals(right, leg.getRight(LengthUnit.Metres), 1e-8);
		assertEquals(up, leg.getUp(LengthUnit.Metres), 1e-8);
		assertEquals(down, leg.getDown(LengthUnit.Metres), 1e-8);
		assertTrue(leg.isSplay() == splay);
		assertTrue(leg.isDuplicate() == duplicate);
		assertTrue(leg.isSurface() == surface);
		assertTrue(leg.isDiving() == true);
		assertTrue(leg.isDepthChangeLeg() == true);
	}

	/**
	 * Test method for {@link footleg.cavesurvey.data.model.SurveyLeg}.
	 */
	@Test
	public void testUnitsSupport() {
		SurveyLeg leg = new SurveyLeg();

		int fromStnId = 543;
		SurveyStation stn = new SurveyStation(fromStnId);
		leg.setFromStn(stn);

		// Test returned value in feet matches expected
		double lengthMetres = 12.34;
		double lengthFeet = 3.280839895 * lengthMetres;
		leg.setLength(lengthMetres, LengthUnit.Metres);
		assertEquals(lengthFeet, leg.getLength(LengthUnit.Feet), 1e-8);

		// Test returned bearing in degrees matches expected
		double angleDegrees = 123.45;
		double angleGrads = 400 * angleDegrees / 360;
		leg.setCompass(angleDegrees, BearingUnit.Degrees);
		assertEquals(angleGrads, leg.getCompass(BearingUnit.Grads), 1e-8);
		/*
		 * //Test returned bearing in degrees matches expected
		 * double clinoDegrees = 123.45;
		 * double clinoPercent = ;
		 * leg.setCompass( Measure.valueOf( angleDegrees, NonSI.DEGREE_ANGLE ) );
		 * assertEquals( angleGrads, leg.getCompass().doubleValue(NonSI.GRADE), 1e-8 );
		 */
	}

}
