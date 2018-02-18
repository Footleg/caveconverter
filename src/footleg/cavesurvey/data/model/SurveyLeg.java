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

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Represents a leg in a cave survey. A leg is a set of measurements which define the 
 * location of a point in the cave relative to another point (a survey station). The
 * second point defined by the leg can be another survey station or may just be a point
 * in the cave on the wall or used to measure the position of a significant feature.
 * Legs can be normal (defined in terms of length, bearing and inclination), or diving.
 * Diving legs are defined in terms of a length, bearing and change in depth. The change
 * in depth can be defined explicitly using the depths of both the from and to stations,
 * or in terms of just a change in depth. If the leg is defined as a 'depth change' leg
 * then this is stored in the toDepth property (the fromDepth property is set to a 
 * value of 999999m).
 * Data is stored and calculations done in metric units (metres and degrees), but
 * conversion to other units on input/output is supported.
 * 
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 * @to.do
 * TODO Add method to convert diving leg into conventional measurements (length,bearing,gradient)
 */
public class SurveyLeg implements Comparable<SurveyLeg> {
	private static final int nullDepth = 999999;
	private SurveyStation fromStn;
	private SurveyStation toStn;
	private double length = -1;
	private double fromDepth = nullDepth;
	private double toDepth = nullDepth;
	private double compass = -1;
	private double clino = -99;
	private String comment = "";
	
	//LRUD applies to fromStn
	private double left = 0.0;
	private double right = 0.0;
	private double up = 0.0;
	private double down = 0.0;
	
	//Leg flags
	private boolean splay = false;
	private boolean duplicate = false;
	private boolean surface = false;
	private boolean diving = false;
	private boolean nosurvey = false;
	
	/**
	 * Support sorting by from station name, then to station name
	 */
	public int compareTo(SurveyLeg anotherLeg) {
		SurveyLeg leg = (SurveyLeg)anotherLeg;
		int fromTest = this.fromStn.getId() - leg.getFromStn().getId();
		if ( fromTest == 0 ) {
			//Same from station, so use toStn to sort
			return this.toStn.getId() - leg.getToStn().getId();
		}
		else {
			return fromTest;
		}
	}

	/**
	 * Provides a string representation of a cave survey leg to display in a tree view of the data model
	 * @return String representation of survey leg class 
	 */
	public String toString() {
		String text = getFromStn().getName();
		if ( (toStn != null) && ( toStn.getName() != null ) ) {
			text += " - " + getToStn().getName();
		}
		return text;
	}
	
	/**
	 * @return A copy of the survey leg
	 */
	public SurveyLeg clone() {
		SurveyLeg clone = new SurveyLeg();

		if (fromStn != null){
			clone.fromStn = fromStn.clone();
		}
		if (toStn != null){
			clone.toStn = toStn.clone();
		}
		clone.length = length;
		clone.compass = compass;
		clone.clino = clino;
		clone.fromDepth = fromDepth;
		clone.toDepth = toDepth;
		clone.comment = comment;
		clone.left = left;
		clone.right = right;
		clone.up = up;
		clone.down = down;
		clone.splay = splay;
		clone.duplicate = duplicate;
		clone.surface = surface;
		clone.diving = diving;
		clone.nosurvey = nosurvey;
		
		return clone;
	}
	
	/**
	 * Reverses the direction of a survey leg
	 */
	public void reverseDirection() {
		//Reverse leg direction
		SurveyStation stn = getFromStn();
		setFromStn( getToStn() );
		setToStn( stn );
		double compass = getCompass(BearingUnit.Degrees);
		if ( compass > 180 ) {
			compass -= 180;
		}
		else {
			compass += 180;
		}
		setCompass( compass, BearingUnit.Degrees );
		
		if ( isDiving() ) {
			if ( isDepthChangeLeg() ) {
				//Depth change, so change sign on toDepth to reverse depth change
				toDepth = -toDepth;
			}
			else {
				//Specific depths, so swap them
				double tmpDepth = fromDepth;
				fromDepth = toDepth;
				toDepth = tmpDepth;
			}
			//Swap clino value avoiding setter so isDiving flag is not unset, just in case it had a value
			this.clino = -getClino(GradientUnit.Degrees);
		}
		else {
			setClino( -getClino(GradientUnit.Degrees), GradientUnit.Degrees );
			//Reset depths to null depth in case they had values
			this.fromDepth = nullDepth;
			this.toDepth = nullDepth;
		}
	}
	
	/**
	 * Calculates the horizontal length of the leg
	 * @return calculated horizontal length
	 */
	public double getHorizontalLength() {
		double hLength = Math.abs( length * Math.cos( Math.toRadians( clino ) ) );
		return hLength;
	}
	
	/**
	 * Calculates the vertical height of the leg
	 * @return calculated vertical height
	 */
	public double getVerticalLength() {
		double vLength = Math.abs( length * Math.sin( Math.toRadians( clino ) ) );
		return vLength;
	}

	//Getters and Setters
	public SurveyStation getFromStn() {
		return fromStn;
	}
	
	public void setFromStn(SurveyStation fromStn) {
		this.fromStn = fromStn;
	}
	
	public SurveyStation getToStn() {
		return toStn;
	}
	
	public void setToStn(SurveyStation toStn) {
		this.toStn = toStn;
	}
	
	public double getLength(LengthUnit units) {
		return UtilityFunctions.lengthFromMetres( length, units );
	}
	
	public void setLength(double length, LengthUnit units) {
		this.length = UtilityFunctions.lengthToMetres(length, units);
	}
	
	public double getCompass(BearingUnit units) {
		return UtilityFunctions.bearingFromDegrees(compass, units);
	}
	
	public void setCompass(double compass, BearingUnit units) {
		this.compass = UtilityFunctions.bearingToDegrees(compass, units);
	}

	/**
	 * TODO Calculate angle for diving legs 
	 * @param units Units to return the value in
	 * @return Gradient of leg in the specified units
	 */
	public double getClino(GradientUnit units) {
		return UtilityFunctions.gradientFromDegrees(clino, units);
	}

	/**
	 * Setting this sets the leg to a non-diving (normal) type leg.
	 * @param clino Gradient value to set for the leg, in the units specified
	 * @param units Units of the specified value
	 */
	public void setClino(double clino, GradientUnit units) {
		this.clino = UtilityFunctions.gradientToDegrees(clino, units);
		diving = false;
	}
	
	public double getLeft(LengthUnit units) {
		return UtilityFunctions.lengthFromMetres( left, units );
	}
	
	public void setLeft(double left, LengthUnit units) {
		this.left = UtilityFunctions.lengthToMetres(left, units);
	}
	
	public double getRight(LengthUnit units) {
		return UtilityFunctions.lengthFromMetres( right, units );
	}
	
	public void setRight(double right, LengthUnit units) {
		this.right = UtilityFunctions.lengthToMetres(right, units);
	}
	
	public double getUp(LengthUnit units) {
		return UtilityFunctions.lengthFromMetres( up, units );
	}
	
	public void setUp(double up, LengthUnit units) {
		this.up = UtilityFunctions.lengthToMetres(up, units);
	}
	
	public double getDown(LengthUnit units) {
		return UtilityFunctions.lengthFromMetres( down, units );
	}
	
	public void setDown(double down, LengthUnit units) {
		this.down = UtilityFunctions.lengthToMetres(down, units);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isSplay() {
		return splay;
	}

	public void setSplay(boolean splay) {
		this.splay = splay;
		if ( splay == true ) {
			this.nosurvey = false;
		}
	}

	public boolean isDuplicate() {
		return duplicate;
	}

	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}

	public boolean isSurface() {
		return surface;
	}

	public void setSurface(boolean surface) {
		this.surface = surface;
	}
	
	/**
	 * @param units Length units to return the value in
	 * @return The depth change for the diving leg
	 */
	public double getDepthChange(LengthUnit units) {
		if ( isDepthChangeLeg() ) {
			return UtilityFunctions.lengthFromMetres( toDepth, units );
		}
		else {
			return UtilityFunctions.lengthFromMetres( toDepth - fromDepth, units );
		}
	}

	/**
	 * Setting this sets the leg to a diving type leg.
	 * @param depthChange The depth change to set
	 * @param units Units of the specified depth change
	 */
	public void setDepthChange(double depthChange, LengthUnit units) {
		this.fromDepth = nullDepth;
		this.toDepth = UtilityFunctions.lengthToMetres(depthChange, units);
		this.diving = true;
		this.nosurvey = false;
	}
	
	/**
	 * @param units Length units to return the value in
	 * @return The from Depth for a diving leg
	 */
	public double getFromDepth(LengthUnit units) {
		return UtilityFunctions.lengthFromMetres( fromDepth, units );
	}

	/**
	 * @param units Length units to return the value in
	 * @return The to Depth for a diving leg
	 */
	public double getToDepth(LengthUnit units) {
		return UtilityFunctions.lengthFromMetres( toDepth, units );
	}

	/**
	 * Setting this sets the leg to a diving type leg.
	 * @param fromDepth the depth of the from station
	 * @param toDepth the depth of the to station
	 * @param units Units of the specified depth measurements
	 */
	public void setDepths(double fromDepth, double toDepth, LengthUnit units) {
		this.fromDepth = UtilityFunctions.lengthToMetres(fromDepth, units);
		this.toDepth = UtilityFunctions.lengthToMetres(toDepth, units);
		this.diving = true;
		this.nosurvey = false;
	}

	public boolean isDiving() {
		return diving;
	}

	public boolean isDepthChangeLeg() {
		return ( diving && fromDepth == nullDepth );
	}

	public boolean isNosurvey() {
		return nosurvey;
	}

	public void setNosurvey(boolean nosurvey) {
		this.nosurvey = nosurvey;
		if ( nosurvey == true) {
			this.splay = false;
		}
	}
}
