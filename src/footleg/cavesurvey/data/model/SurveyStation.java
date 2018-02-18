/**
 * Copyright (C) 2009-2015 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
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

/**
 * Represents a survey station in a cave survey. A station is a specific location in 
 * the cave which might be marked, or a natural feature which can be described in a
 * comment, or it might be a temporary point which is not marked but was just used
 * to join two legs during the survey.
 * 
 * @author      Footleg
 * @version     2015.08.31                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 */
public class SurveyStation {
	/**
	 * Create station directly specifying an id number
	 * @param id ID number of the station
	 */
	public SurveyStation(int id) {
		super();
		this.id = id;
	}

	private int id = -1;
	private String name = "";
	private String comment = "";

	//FIXED POSITION
	private FixType fixType = FixType.NONE; 
	private double easting;
	private double northing;
	private double altitude;

	private boolean entrance = false;

	public enum FixType {
	    NONE   (0),
	    GPS (1),
	    OTHER   (2);
	    
	    FixType( int type ) {
	    }
	}
	
	public boolean isEntrance() {
		return ( entrance );
	}
	public void setEntrance(boolean entrance) {
		this.entrance = entrance;
	}
	public boolean isFixed() {
		return ( fixType != FixType.NONE );
	}
	public FixType getFixType() {
		return fixType;
	}
	public double getEasting() {
		return easting;
	}
	public double getNorthing() {
		return northing;
	}
	public double getAltitude() {
		return altitude;
	}
	
	/**
	 * Sets a fixed position for the station.
	 *
	 * @param  fixType The type of fix (GPS, other)
	 * @param  easting Grid position (in metres) of the fix position
	 * @param  northing Grid position (in metres) of the fix position
	 * @param  altitude Elevation (in metres) of the fix position
	 */
	public void setFixed(FixType fixType, double easting, double northing, double altitude) {
		this.fixType = fixType;
		this.easting = easting;
		this.northing = northing;
		this.altitude = altitude;
	}

	public void clearFixedStn() {
		this.fixType = FixType.NONE;
		this.easting = 0;
		this.northing = 0;
		this.altitude = 0;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		String stnName = this.name;
		//Return number as name if no name set
		if (stnName.length() == 0) {
			stnName = "" + this.id;
		}
		return stnName;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public SurveyStation clone() {
		SurveyStation clone = new SurveyStation( this.id );

		clone.name = this.name;
		clone.comment = this.comment;
		clone.entrance = this.entrance;
		clone.fixType = this.fixType;
		clone.easting = this.easting;
		clone.northing = this.northing;
		clone.altitude = this.altitude;

		return clone;
	}
	
}
