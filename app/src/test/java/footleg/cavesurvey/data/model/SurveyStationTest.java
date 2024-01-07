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

import org.junit.jupiter.api.Test;

import footleg.cavesurvey.data.model.SurveyStation.FixType;

/**
 * Unit test class for {@link footleg.cavesurvey.data.model.SurveyStation}
 * 
 * @author Footleg
 * @version 2024.01.03 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class SurveyStationTest {

	@Test
	public void testSetEntrance() {
		int stnID = 123;

		SurveyStation stn = new SurveyStation(stnID);

		assertFalse(stn.isEntrance(), "Station entrance should be false if not set.");

		stn.setEntrance(true);
		assertTrue(stn.isEntrance(), "Checking station was set to be an entrance.");

		stn.setEntrance(false);
		assertFalse(stn.isEntrance(), "Checking station was reset to not be an entrance");
	}

	@Test
	public void testSetFixed() {
		int stnID = 123;
		double easting = 4800456.0;
		double northing = 4800789.75;
		double altitude = 8900.45;
		double easting2 = 123.45;
		double northing2 = 678.00;
		double altitude2 = 0.0;

		SurveyStation stn = new SurveyStation(stnID);

		assertFalse(stn.isFixed(), "Station should not be fixed by default.");

		// Set GPS type fix
		stn.setFixed(FixType.GPS, easting, northing, altitude);
		assertTrue(stn.isFixed(), "Checking station was set to be fixed.");

		assertEquals(easting, stn.getEasting(), 0.0, "Getter for Easting");
		assertEquals(northing, stn.getNorthing(), 0.0, "Getter for Northing");
		assertEquals(altitude, stn.getAltitude(), 0.0, "Getter for Altitude");
		assertEquals(FixType.GPS, stn.getFixType(), "Getter for fix type GPS");

		// Set Other type fix
		stn.setFixed(FixType.OTHER, easting2, northing2, altitude2);
		assertTrue(stn.isFixed(), "Checking station was set to be fixed.");

		assertEquals(easting2, stn.getEasting(), 0.0, "Getter for Easting");
		assertEquals(northing2, stn.getNorthing(), 0.0, "Getter for Northing");
		assertEquals(altitude2, stn.getAltitude(), 0.0, "Getter for Altitude");
		assertEquals(FixType.OTHER, stn.getFixType(), "Getter for fix type Other");

		// Clear fix
		stn.clearFixedStn();
		assertFalse(stn.isFixed(), "Station fix was cleared");
	}

	@Test
	public void testSetId() {
		int stnID = 67899035;

		SurveyStation stn = new SurveyStation(stnID);
		assertEquals(stnID, stn.getId(), "Getter for Id");
	}

	@Test
	public void testGetName() {
		int stnID = 12;
		String defaultName = "12";
		int stnID2 = 345;
		String defaultName2 = "345";
		String newName = "ABC";

		SurveyStation stn = new SurveyStation(stnID);

		// If not set then name should be string of the Id
		assertEquals(defaultName, stn.getName(), "Getter for Name based on Id");

		// Change ID, check name changes
		stn.setId(stnID2);
		assertEquals(defaultName2, stn.getName(), "Getter for Name based on changed Id");

		// Set name to something else
		stn.setName(newName);
		assertEquals(newName, stn.getName(), "Getter for Name set explicitly");

		// Change ID after name was set, and check name is not changed
		stn.setId(92364);
		assertEquals(newName, stn.getName(), "Getter for Name set explicitly");
	}

	@Test
	public void testSetComment() {
		int stnID = 12;
		String commentText = "This is a station comment. It is quite long can contains\ra line break.";

		SurveyStation stn = new SurveyStation(stnID);

		stn.setComment(commentText);
		assertEquals(commentText, stn.getComment(), "Getter for Comment");
	}

	@Test
	public void testClone() {
		int stnID = 12;
		String newName = "ABC";
		String commentText = "This is a station comment. It is quite long can contains\ra line break.";
		double easting = 4800456.0;
		double northing = 4800789.75;
		double altitude = 8900.45;

		SurveyStation stn = new SurveyStation(stnID);
		stn.setName(newName);
		stn.setComment(commentText);
		stn.setEntrance(true);
		stn.setFixed(FixType.GPS, easting, northing, altitude);

		SurveyStation clone = stn.clone();

		assertEquals(stnID, clone.getId(), "Station ID from clone");
		assertEquals(newName, clone.getName(), "Station Name from clone");
		assertEquals(commentText, clone.getComment(), "Station Comment from clone");
		assertTrue(clone.isEntrance(), "Station is entrance from clone");
		assertTrue(clone.isFixed(), "Station is fixed from clone");
		assertEquals(FixType.GPS, clone.getFixType(), "Station is fix type from clone");
		assertEquals(easting, clone.getEasting(), 0.0, "Station easting from clone");
		assertEquals(northing, clone.getNorthing(), 0.0, "Station northing from clone");
		assertEquals(altitude, clone.getAltitude(), 0.0, "Station altitude from clone");

		// Change values of original station, and check clone was not changed
		// (checks clone is not a pointer to the same object)
		stn.setId(8765);
		stn.setName("iusdfhbs");
		stn.setComment("iadrkuKAJ RDFC J JGV JYG BHF HF NJH B CN HJC NGF XB HGF CBNJCFN JFC NJ CF");
		stn.setEntrance(false);
		stn.setFixed(FixType.OTHER, 1.2, 3.4, 5.6);

		assertEquals(stnID, clone.getId(), "Station ID from clone");
		assertEquals(newName, clone.getName(), "Station Name from clone");
		assertEquals(commentText, clone.getComment(), "Station Comment from clone");
		assertTrue(clone.isEntrance(), "Station is entrance from clone");
		assertTrue(clone.isFixed(), "Station is fixed from clone");
		assertEquals(FixType.GPS, clone.getFixType(), "Station is fix type from clone");
		assertEquals(easting, clone.getEasting(), 0.0, "Station easting from clone");
		assertEquals(northing, clone.getNorthing(), 0.0, "Station northing from clone");
		assertEquals(altitude, clone.getAltitude(), 0.0, "Station altitude from clone");

	}

}
