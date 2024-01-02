/**
 * Copyright (C) 2009-2013 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
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

import footleg.cavesurvey.data.model.SurveyStation;
import footleg.cavesurvey.data.model.SurveyStation.FixType;

/**
 * Unit test class for {@link footleg.cavesurvey.data.model.SurveyStation}
 * 
 * @author      Footleg
 * @version     2012.12.26                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class SurveyStationTest {

	@Test
	public void testSetEntrance() {
		int stnID = 123;
		
		SurveyStation stn = new SurveyStation( stnID );
		
		assertFalse( "Station entrance should be false if not set.", stn.isEntrance() );
		
		stn.setEntrance(true);
		assertTrue( "Checking station was set to be an entrance.", stn.isEntrance() );
		
		stn.setEntrance(false);
		assertFalse( "Checking station was reset to not be an entrance", stn.isEntrance() );
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
		
		SurveyStation stn = new SurveyStation( stnID );
		
		assertFalse( "Station should not be fixed by default.", stn.isFixed() );
		
		//Set GPS type fix
		stn.setFixed(FixType.GPS, easting, northing, altitude);
		assertTrue( "Checking station was set to be fixed.", stn.isFixed() );
		
		assertEquals("Getter for Easting", easting, stn.getEasting(), 0.0 );
		assertEquals("Getter for Northing", northing, stn.getNorthing(), 0.0 );
		assertEquals("Getter for Altitude", altitude, stn.getAltitude(), 0.0 );
		assertEquals("Getter for fix type GPS", FixType.GPS, stn.getFixType() );
		
		//Set Other type fix
		stn.setFixed(FixType.OTHER, easting2, northing2, altitude2);
		assertTrue( "Checking station was set to be fixed.", stn.isFixed() );
		
		assertEquals("Getter for Easting", easting2, stn.getEasting(), 0.0 );
		assertEquals("Getter for Northing", northing2, stn.getNorthing(), 0.0 );
		assertEquals("Getter for Altitude", altitude2, stn.getAltitude(), 0.0 );
		assertEquals("Getter for fix type Other", FixType.OTHER, stn.getFixType() );
		
		//Clear fix
		stn.clearFixedStn();
		assertFalse( "Station fix was cleared", stn.isFixed() );
	}

	@Test
	public void testSetId() {
		int stnID = 67899035;
		
		SurveyStation stn = new SurveyStation( stnID );
		assertEquals("Getter for Id", stnID, stn.getId() );
	}

	@Test
	public void testGetName() {
		int stnID = 12;
		String defaultName = "12";
		int stnID2 = 345;
		String defaultName2 = "345";
		String newName = "ABC";
		
		SurveyStation stn = new SurveyStation( stnID );
		
		//If not set then name should be string of the Id
		assertEquals("Getter for Name based on Id", defaultName, stn.getName() );
		
		//Change ID, check name changes
		stn.setId(stnID2);
		assertEquals("Getter for Name based on changed Id", defaultName2, stn.getName() );
		
		//Set name to something else
		stn.setName(newName);
		assertEquals("Getter for Name set explicitly", newName, stn.getName() );
		
		//Change ID after name was set, and check name is not changed
		stn.setId(92364);
		assertEquals("Getter for Name set explicitly", newName, stn.getName() );
	}

	@Test
	public void testSetComment() {
		int stnID = 12;
		String commentText = "This is a station comment. It is quite long can contains\ra line break.";
		
		SurveyStation stn = new SurveyStation( stnID );
		
		stn.setComment(commentText);
		assertEquals("Getter for Comment", commentText, stn.getComment() );
	}

	@Test
	public void testClone() {
		int stnID = 12;
		String newName = "ABC";
		String commentText = "This is a station comment. It is quite long can contains\ra line break.";
		double easting = 4800456.0;
		double northing = 4800789.75;
		double altitude = 8900.45;

		SurveyStation stn = new SurveyStation( stnID );
		stn.setName(newName);
		stn.setComment(commentText);
		stn.setEntrance(true);
		stn.setFixed(FixType.GPS, easting, northing, altitude);
		
		SurveyStation clone = stn.clone();
		
		assertEquals( "Station ID from clone", stnID, clone.getId() );
		assertEquals( "Station Name from clone", newName, clone.getName() );
		assertEquals( "Station Comment from clone", commentText, clone.getComment() );
		assertTrue( "Station is entrance from clone", clone.isEntrance() );
		assertTrue( "Station is fixed from clone", clone.isFixed() );
		assertEquals( "Station is fix type from clone", FixType.GPS, clone.getFixType() );
		assertEquals( "Station easting from clone", easting, clone.getEasting(), 0.0 );
		assertEquals( "Station northing from clone", northing, clone.getNorthing(), 0.0 );
		assertEquals( "Station altitude from clone", altitude, clone.getAltitude(), 0.0 );
		
		//Change values of original station, and check clone was not changed 
		//(checks clone is not a pointer to the same object)
		stn.setId(8765);
		stn.setName("iusdfhbs");
		stn.setComment("iadrkuKAJ RDFC J JGV JYG BHF HF NJH B CN HJC NGF XB HGF CBNJCFN JFC NJ CF");
		stn.setEntrance(false);
		stn.setFixed(FixType.OTHER, 1.2, 3.4, 5.6);

		assertEquals( "Station ID from clone", stnID, clone.getId() );
		assertEquals( "Station Name from clone", newName, clone.getName() );
		assertEquals( "Station Comment from clone", commentText, clone.getComment() );
		assertTrue( "Station is entrance from clone", clone.isEntrance() );
		assertTrue( "Station is fixed from clone", clone.isFixed() );
		assertEquals( "Station is fix type from clone", FixType.GPS, clone.getFixType() );
		assertEquals( "Station easting from clone", easting, clone.getEasting(), 0.0 );
		assertEquals( "Station northing from clone", northing, clone.getNorthing(), 0.0 );
		assertEquals( "Station altitude from clone", altitude, clone.getAltitude(), 0.0 );

	}

}
