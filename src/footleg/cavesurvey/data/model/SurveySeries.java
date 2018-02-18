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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * This class represents a series of cave survey legs joined together by 
 * sharing stations in common. Branches are allowed, but all legs in a series 
 * are required to have the same instrument calibration values and have the 
 * same date. A series can also contain other series, and details of how they 
 * are linked together.
 * 
 * A survey series contains the survey legs in the series, the date, 
 * calibrations for instruments (these default to zero if not set) 
 * and magnetic declination (defaults to zero if not set).
 * A series name is required. Optionally a series can also indicate that stations
 * used in legs in the series are equivalent (i.e. Represent the same point in 
 * the cave). The series can also contain other series, and details of links 
 * between them.
 * Data is stored and calculations done in metric units (metres and degrees), but
 * conversion to other units on input/output is supported.
 * 
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 * @to.do
 * TODO Add support for calibration comments fields
 * TODO Add support for team fields
 * TODO Add support for instrument fields
 */
public class SurveySeries {

	/**
	 * Private data class to represent the LRUD data for the toStn of a leg.
	 * Used for stations which are not used as 'from' stations in any legs.
	 * This class enables both the LRUD dimensions and the vector normal to the plane
	 * of the LRUD dimensions to be stored.
	 * 
	 * @author      Footleg
	 *
	 */
	public class ToStnLRUD {
		private SurveyStation fromStn;
		
		//LRUD applies to fromStn
		private double left = 0.0;
		private double right = 0.0;
		private double up = 0.0;
		private double down = 0.0;

		//Getters and Setters
		public SurveyStation getFromStn() {
			return fromStn;
		}
		public void setFromStn(SurveyStation fromStn) {
			this.fromStn = fromStn;
		}
		public double getLeft() {
			return left;
		}
		public void setLeft(double left) {
			this.left = left;
		}
		public double getRight() {
			return right;
		}
		public void setRight(double right) {
			this.right = right;
		}
		public double getUp() {
			return up;
		}
		public void setUp(double up) {
			this.up = up;
		}
		public double getDown() {
			return down;
		}
		public void setDown(double down) {
			this.down = down;
		}
	}
	
	private String seriesName;
	private List<SurveyLeg> legs;
	private List<ToStnLRUD> toStnLRUDs;
	private List<SeriesLink> links;
	private List<SurveySeries> innerSeries;
	private int stnRenumberSequence;
	private List<String> stnRenameCache;
	private double declination = 0;
	private double tapeCalibration = 0;
	private double compassCalibration = 0;
	private double clinoCalibration = 0;
	private double clinoCalScaleFactor = 1;
	private Date surveyDate;
	private String comment = "";
	private static double LRUD_SPECIAL_FLAG = -999;
	private LengthUnit lengthUnit = LengthUnit.Metres;
	private LengthUnit depthUnit = LengthUnit.Metres;
	private BearingUnit bearingUnit = BearingUnit.Degrees;
	private GradientUnit gradientUnit = GradientUnit.Degrees;
	private List<String> dataOrder;
	private List<String> dataOrder2;

	/**
	 * Create a Survey Series with the name given
	 * @param name Name of the series
	 */
	public SurveySeries( String name ) {
		super();
		init();
		this.seriesName = name;
	}

	private void init() {
		legs = new ArrayList<SurveyLeg>();
		toStnLRUDs = new ArrayList<ToStnLRUD>();
		links = new ArrayList<SeriesLink>();
		innerSeries = new ArrayList<SurveySeries>();
		stnRenameCache = new ArrayList<String>();
		stnRenumberSequence = 0;
	}
	
	public LengthUnit getLengthUnit() {
		return lengthUnit;
	}

	public void setLengthUnit(LengthUnit lengthUnit) {
		this.lengthUnit = lengthUnit;
	}
	
	public LengthUnit getDepthUnit() {
		return depthUnit;
	}

	public void setDepthUnit(LengthUnit depthUnit) {
		this.depthUnit = depthUnit;
	}

	public BearingUnit getBearingUnit() {
		return bearingUnit;
	}

	public void setBearingUnit(BearingUnit bearingUnit) {
		this.bearingUnit = bearingUnit;
	}

	public GradientUnit getGradientUnit() {
		return gradientUnit;
	}

	public void setGradientUnit(GradientUnit gradientUnit) {
		this.gradientUnit = gradientUnit;
	}

	public void addLeg(SurveyLeg leg) {
		//Create duplicate of leg to break reference
		this.legs.add(leg.clone());
	}

	public void addLeg(SurveyLeg leg, int position) {
		//Create duplicate of leg to break reference
		this.legs.add( position, leg.clone() );
	}
	
	public int legCount() {
		return legs.size();
	}
	
	public List<ToStnLRUD> getToStnLRUDs() {
		return toStnLRUDs;
	}
	
	/**
	 * Assigns a survey station a unique Id for the station name. The survey  
	 * station name is mapped to an integer which is assigned as the station id. 
	 * This can be used for file formats which do not support text station names.
	 * If the name is a string which represents 
	 * a positive integer number already then that number is returned.
	 * For non-numeric station names the name is assigned 
	 * a unique negative integer number. Negative numbers are used so they will 
	 * not clash with any positive number station names in the series. The negative
	 * station ids also indicate which stations have names which are not numeric. 
	 * A map of names to numbers already added to the series is used so that the 
	 * same name is not translated into different numbers if passed in more than 
	 * once. When all legs have been added to a series, the negative numbered
	 * stations could be assigned new positive number IDs which are not already
	 * used by other stations in the series.
	 * 
	 * @param stn The survey station with a name property to be assigned an id
	 */
	public void setStationIdFromName( SurveyStation stn ) {
		int stnId = stnNameToNumber( stn.getName() );
		stn.setId( stnId );
	}

	/**
	 * @param stnName
	 * @return a station number linked to the text name for this series
	 */
	private int stnNameToNumber( String stnName ) {
		int stn;
		try {
			stn = Integer.parseInt( stnName );
		}
		catch (NumberFormatException nfe) {
			//Stn name not an integer, so translate name to a negative number
			stn = this.getNumberToRepresentStnName( stnName );
		}
		return stn;
	}
	
	/** 
	 * Returns a negative integer number for a station name string.
	 * 
	 * The number will be unique to this series for this station name.
	 * This enables station names to be converted to numbers for file formats
	 * which do not support strings. The name is mapped to a number in this
	 * series by looking up the name in a cache for the series. If the name
	 * is not found in the cache then a new negative number is returned, and
	 * the new name and number mapping is added to the cache for the series. 
	 * 
	 * @param  stnName Name of the survey station to map to a number.
	 * @return Number which can be used to represent this station.
	 */
	public int getNumberToRepresentStnName(String stnName) {
		int mappedStnNum = 0;
		
		//Look for station name in cache
		ListIterator<String> stnCacheIterator = stnRenameCache.listIterator();
		int stnIdx = 0;
		while ( ( mappedStnNum == 0 ) && ( stnCacheIterator.hasNext() ) ) {
			String cachedName = stnCacheIterator.next();
			stnIdx--;
			if ( stnName.compareToIgnoreCase(cachedName) == 0 ) {
				//Found matching stn in cache, so get number from index
				mappedStnNum = stnIdx;
			}
		}
		
		if ( mappedStnNum == 0 ) {
			//No match found in cache, so create a new mapping and cache it
			stnRenumberSequence--;
			mappedStnNum = stnRenumberSequence;
			/**
			 * As the mapped stns are added to the cache, the index in the 
			 * cache will correspond to the mapped number of the stn:
			 * idx 0 = map -1
			 * idx 1 = map -2
			 * etc.
			 */
			stnRenameCache.add(stnName);
		}
		
		return mappedStnNum;
	}

	public String getMappedStnName(int stn) {
		//If number is negative then lookup name in map, 
		//otherwise just return station number as the name
		if ( stn < 0 ) {
			return stnRenameCache.get( Math.abs(stn) - 1 );
		}
		else {
			return "" + stn;
		}
	}
	
	//Apply instrument calibration corrections to all measurements in returned leg
	public SurveyLeg getLegCorrected(int index) {
		SurveyLeg originalLeg = legs.get(index);
		SurveyLeg correctedLeg = originalLeg.clone();
		
		//Over-ride leg length,compass and clino with calibration corrected values
		correctedLeg.setLength( originalLeg.getLength(LengthUnit.Metres) - tapeCalibration, LengthUnit.Metres );
		double compass = originalLeg.getCompass(BearingUnit.Degrees);
		if ( compass >= 0 && compass <= 360 ) {
			//Apply calibration only to valid bearings
			compass -= (compassCalibration + declination);
		}
		correctedLeg.setCompass( compass, BearingUnit.Degrees );
		double clino = originalLeg.getClino(GradientUnit.Degrees);
		if ( clino >= -90 && clino <= 180 ) {
			//Apply calibration only to valid clino
			clino = (clino - clinoCalibration) * clinoCalScaleFactor;
		}
		correctedLeg.setClino( clino, GradientUnit.Degrees );
		
		//Don't correct LRUD using tape calibration as these are assumed to be estimated by eye
		return correctedLeg;
	}

	public SurveyLeg getLegRaw(int index) {
		return legs.get(index);
	}
	
	public void addSeries(SurveySeries series) {
		this.innerSeries.add(series);
	}
	
	public int innerSeriesCount() {
		return innerSeries.size();
	}

	public SurveySeries getInnerSeries(int index) {
		return innerSeries.get(index);
	}

	public List<SurveySeries> getInnerSeriesList() {
		return innerSeries;
	}

	public SurveySeries findInnerSeriesByName(String name) {
		SurveySeries match = null;
		//Look in inner series
		ListIterator<SurveySeries> innerIterator = innerSeries.listIterator();
		while ( innerIterator.hasNext() ) {
			SurveySeries innerSeries = innerIterator.next();
			if ( innerSeries.getSeriesName().equalsIgnoreCase( name ) ) {
				//Found series, so return this
				match = innerSeries;
				break;
			}
		}
		return match;
	}
	
	/**
	 * Look up the child object specified in the series, and return the index of the object
	 * @param child The child object to get the index of
	 * @return The index of the object in this series
	 */
	public int getIndexOfChild(Object child) {
		int matchIndex = -1;
		//Look for child in inner series
		ListIterator<SurveySeries> innerIterator = innerSeries.listIterator();
		int idx = -1;
		while ( innerIterator.hasNext() ) {
			SurveySeries innerSeries = innerIterator.next();
			idx++;
			if ( innerSeries.equals( child ) ) {
				//Found series, so return this
				matchIndex = idx;
				break;
			}
		}
		return matchIndex;
	}

	public void addLink(String series1Path, SurveyStation stn1, String series2Path, SurveyStation stn2 ) {
		//Add link to array
		SeriesLink link = new SeriesLink(series1Path, stn1, series2Path, stn2 );
		links.add(link);
	}

	public List<SeriesLink> getLinks() {
		return links;
	}

	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public List<String> getDataOrder() {
		//Make a copy to return, as we don't want a pointer to the existing list or parsers
		//can change it
		List<String> tmpList = new ArrayList<String>();
		if (dataOrder != null ) {
			tmpList.addAll(dataOrder);
		}
		
		return tmpList;
	}

	public List<String> getDataOrder2() {
		//Make a copy to return, as we don't want a pointer to the existing list or parsers
		//can change it
		List<String> tmpList = new ArrayList<String>();
		if (dataOrder2 != null ) {
			tmpList.addAll(dataOrder2);
		}
		
		return tmpList;
	}
	
	/**
	 * Indicates if series has a data order defined. Method provided to avoid copying data order
	 * list just to check this from outside the series, as getter clones the list to protect it 
	 * from being edited via a pointer.
	 * @return True if series has a field data order defined.
	 */
	public boolean hasDataOrder() {
		boolean hasIt = false;
		if ( dataOrder != null ) {
			hasIt = (dataOrder.size() > 0);
		}
		return hasIt;
	}

	/**
	 * Specifies the order of data items used in the file which was read to generate the series.
	 * Parsers use this to track the active data ordering (i.e. from to length, bearing, gradient)
	 * through nested series in files.
	 * @param dataOrder Field data order for the series
	 */
	public void setDataOrder(List<String> dataOrder) {
		//Make a copy to store, as we don't want a pointer to an existing list
		List<String> tmpList = new ArrayList<String>();
		tmpList.addAll(dataOrder);

		this.dataOrder = tmpList;
	}

	/**
	 * Secondary order of data items used in the series. This is used when a series contains a mix
	 * of normal and diving data to hold the data order for both formats of data.
	 * @param dataOrder Field data order for the series for alternate data format (e.g. For mixed diving/normal data in series)
	 */
	public void setDataOrder2(List<String> dataOrder) {
		//Make a copy to store, as we don't want a pointer to an existing list
		List<String> tmpList = new ArrayList<String>();
		tmpList.addAll(dataOrder);

		this.dataOrder2 = tmpList;
	}

	/**
	 * Provides a string representation of a cave survey series to display in a tree view of the data model
	 * @return String representation of survey series class 
	 */
	public String toString() {
		String text = getSeriesName();
		if ( ( text == null ) || ( text.equals("") ) ) {
			text = "Survey Series";
		}
		return text;
	}

	public double getTapeCalibration(LengthUnit units) {
		return UtilityFunctions.lengthFromMetres( tapeCalibration, units );
	}

	public void setTapeCalibration(double tapeCalibration, LengthUnit units) {
		this.tapeCalibration = UtilityFunctions.lengthToMetres(tapeCalibration, units);
	}

	public double getDeclination() {
		return declination;
	}

	public void setDeclination(double declination) {
		this.declination = declination;
	}

	public double getCompassCalibration(BearingUnit units) {
		return UtilityFunctions.bearingFromDegrees(compassCalibration, units) ;
	}

	public void setCompassCalibration(double compassCalibration, BearingUnit units) {
		this.compassCalibration = UtilityFunctions.bearingToDegrees(compassCalibration, units) ;
	}

	public double getClinoCalibration(GradientUnit units) {
		return UtilityFunctions.gradientFromDegrees(clinoCalibration, units) ;
	}

	public double getClinoScaleFactor() {
		return clinoCalScaleFactor;
	}

	public void setClinoCalibration(double clinoCalibration, GradientUnit units) {
		this.clinoCalibration = UtilityFunctions.gradientToDegrees(clinoCalibration, units);
		this.clinoCalScaleFactor = 1;
	}
	
	public void setClinoCalibration(double clinoCalibration, GradientUnit units, double clinoCalScaleFactor) {
		this.clinoCalibration = UtilityFunctions.gradientToDegrees(clinoCalibration, units);;
		this.clinoCalScaleFactor = clinoCalScaleFactor;
	}
	
	public Date getSurveyDate() {
		return surveyDate;
	}

	public void setSurveyDate(Date surveyDate) {
		this.surveyDate = surveyDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setCalibrationFromAnotherSeries(SurveySeries series) {
		setDeclination( series.getDeclination() );
		setTapeCalibration( series.getTapeCalibration( LengthUnit.Metres ), LengthUnit.Metres );
		setCompassCalibration( series.getCompassCalibration( BearingUnit.Degrees ), BearingUnit.Degrees );
		setClinoCalibration( series.getClinoCalibration( GradientUnit.Degrees ), GradientUnit.Degrees, series.getClinoScaleFactor() );
	}
	
	public SurveyLeg removeLeg(int index){
		return legs.remove(index);
	}
	
	public void removeSplaysUsedForLRUD() {
		int i = legs.size() - 1;
		while ( i >= 0 ) {
			SurveyLeg leg = legs.get(i);
			if ( leg.isSplay() && leg.getDown( LengthUnit.Metres ) == LRUD_SPECIAL_FLAG ) {
				//Splay leg used for LRUD generation
				removeLeg(i);
			}
			else {
				i--;
			}
		}
	}
	
	/**
	 * Examines all splay shots from the start station for each leg and generates LRUD data 
	 * from them for each leg. The algorithm examines all legs which meet at the station, and
	 * splays which are close to equivalent to any of the legs are not used (to prevent back shots and
	 * splays up side passages which were then surveyed being used to calculate passage dimensions).
	 * Up and down dimensions are calculated from the splay with the greatest vertical length
	 * up or down as appropriate. Splays which are less than 20 degrees from horizontal are
	 * ignored when determining up and down dimensions. Splays with an angle steeper than 70 degrees
	 * from horizontal are excluded from the splays used to determine horizontal passage dimensions.
	 * The passage dimension directions for left and right are calculated along the horizontal
	 * vector bisecting the angle between the forward survey leg, and the most in-line previous
	 * leg arriving at the station. The best matching splay is selected from the suitable
	 * splays by determining which splay gives the greatest distance along the left or right
	 * dimension vector.
	 * @param  logger Logging class to output information, warning and error messages to
	 */
	public void generateLRUDFromSplays( Logger logger ) {
		List<SurveyLeg> caveLegs = new ArrayList<SurveyLeg>();
		List<List<SurveyLeg>> splayLegGroups = new ArrayList<List<SurveyLeg>>();
		List<List<SurveyLeg>> previousLegGroups = new ArrayList<List<SurveyLeg>>();
		
		//Loop through all legs in series to build arrays of splays and non-splay underground legs
		ListIterator<SurveyLeg> legsIterator = legs.listIterator();
		while ( legsIterator.hasNext() ) {
			SurveyLeg leg = legsIterator.next();

			//Put all non-surface legs into either the cave legs (non-splays) or splay groups lists
			if ( leg.isSurface() == false ) {
				if ( leg.isSplay() ) {
					//Add to splays group identified by station name
					String stnName = leg.getFromStn().getName();
					//Look for a group matching this station
					int groupIdx = -1;
					for ( int i = 0; groupIdx < 0 && i < splayLegGroups.size(); i++ ) {
						if ( splayLegGroups.get(i).get(0).getFromStn().getName().compareTo(stnName) == 0 ) {
							//Found group, so get index
							groupIdx = i;
						}
					}
					if ( groupIdx >= 0 ) {
						//Add to this group
						splayLegGroups.get(groupIdx).add(leg);
					} 
					else {
						//Create new group and add leg to this
						List<SurveyLeg> splays = new ArrayList<SurveyLeg>();
						splays.add(leg);
						splayLegGroups.add(splays);
					}
				} 
				else {
					//Add to legs array to be processed
					caveLegs.add(leg);
				}
			}
		}

		//Loop through all legs non-splay legs to build arrays of legs radiating from the station of each leg
		for ( int caveLegIdx = 0; caveLegIdx < caveLegs.size(); caveLegIdx++ ) {
			SurveyLeg leg = caveLegs.get(caveLegIdx);
			String legStartStn = leg.getFromStn().getName();

			//Create list to hold legs radiating out from this leg fromStn
			List<SurveyLeg> legGrp = new ArrayList<SurveyLeg>();

			//Find all other legs which radiate out from the fromStn of this leg
			for ( int otherLegIdx = 0; otherLegIdx < caveLegs.size(); otherLegIdx++ ) {
				//Ignore ourself
				if ( otherLegIdx != caveLegIdx ) {
					SurveyLeg chkLeg = caveLegs.get(otherLegIdx);
					if ( legStartStn.compareToIgnoreCase( chkLeg.getToStn().getName() ) == 0 ) {
						//Add leg to group
						legGrp.add(chkLeg);
					} 
					else if ( legStartStn.compareToIgnoreCase( chkLeg.getFromStn().getName() ) == 0 ) {
						//Reverse leg so it leads to this station and add leg to group
						SurveyLeg revLeg = chkLeg.clone();
						revLeg.reverseDirection();
						legGrp.add( revLeg );
					}
				}
			}
			//Add group to list for this leg
			previousLegGroups.add( legGrp );
		}
		
		//Loop through splay groups and process all the legs with a from stn matching the from stn of the splays in each group
		ListIterator<List<SurveyLeg>> spGroupsIter = splayLegGroups.listIterator();
		List<String> stnsUsed = new ArrayList<String>();
		while ( spGroupsIter.hasNext() ) {
			List<SurveyLeg> splaysGroup = spGroupsIter.next();
			String stnName = splaysGroup.get(0).getFromStn().getName();
			
			ListIterator<SurveyLeg> legsIter = caveLegs.listIterator();
			int caveLegIdx = -1;
			while ( legsIter.hasNext() ) {
				SurveyLeg leg = legsIter.next();
				caveLegIdx++;
				if ( leg.getFromStn().getName().compareTo(stnName) == 0 ) {
					//Generate LRUD data for leg, using the splays starting from that leg from station
					generateLRUDForLeg( leg, splaysGroup, previousLegGroups.get(caveLegIdx), logger );
					//Store station name of splays group that was used
					stnsUsed.add(stnName);
					//Remove leg from list now it has been processed
					legsIter.remove();
					//Remove corresponding previous legs group so lists remain in sync
					previousLegGroups.remove(caveLegIdx);
					caveLegIdx--;
				}
			}
		}
		
		//Build list of splays groups which were not used, as these will be for terminal
		//stations in branches of the series. These splays will only be from the toStn of a leg.
		ListIterator<List<SurveyLeg>> spGroupsIter2 = splayLegGroups.listIterator();
		List<List<SurveyLeg>> unusedSplayGroups = new ArrayList<List<SurveyLeg>>();
		while ( spGroupsIter2.hasNext() ) {
			List<SurveyLeg> splaysGroup = spGroupsIter2.next();
			String stnName = splaysGroup.get(0).getFromStn().getName();
		
			//Check for this group in the used stations list
			boolean foundMatch = false;
			ListIterator<String> stnsUsedIter = stnsUsed.listIterator();
			while ( stnsUsedIter.hasNext() ) {
				String grpName = stnsUsedIter.next();
				if ( stnName.compareTo( grpName ) == 0 ) {
					//Set flag to indicate group was used
					foundMatch = true;
					break;
				}
			}
			
			if ( foundMatch == false ) {
				//Add group to unused groups list
				unusedSplayGroups.add( splaysGroup );
			}
		}
		
		//Loop through unused splays groups and find the leg where the toStn for these 
		//splays occur. Then add the LRUD data for the toStn to the toStnLrud cache.
		ListIterator<List<SurveyLeg>> spGroupsIter3 = unusedSplayGroups.listIterator();
		while ( spGroupsIter3.hasNext() ) {
			List<SurveyLeg> splaysGroup = spGroupsIter3.next();
			String stnName = splaysGroup.get(0).getFromStn().getName();
			
			//Find leg with a matching toStn for these splays
			for ( int legsIdx = 0; legsIdx < legs.size(); legsIdx++ ) {
				SurveyLeg leg = legs.get( legsIdx );
				if ( ( leg.isSplay() == false )
				&& ( leg.getToStn() != null )
				&& ( leg.getToStn().getName().compareTo(stnName) == 0 ) ) {
					//Create a temporary leg to hold the LRUD for this toStn
					SurveyLeg tempLrudLeg = new SurveyLeg();
					tempLrudLeg.setFromStn( leg.getToStn() );
					tempLrudLeg.setLength( -1, LengthUnit.Metres );
					tempLrudLeg.setCompass( leg.getCompass(BearingUnit.Degrees), BearingUnit.Degrees );
					tempLrudLeg.setClino( leg.getClino(GradientUnit.Degrees), GradientUnit.Degrees );
					//Check for any other legs terminating at this station, as if there are any then 
					//their bearings should be used to average with the bearing for the leg we are creating here.
					List<SurveyLeg> otherLegs = new ArrayList<SurveyLeg>();

					/* Find any other legs which terminate at this station. There may be more than
					 * one leg terminating at a station which is not a 'from' station in any leg
					 * e.g. In a leap-frog survey. So we need to hold the LRUD for the station which
					 * only appears as a 'to' station, but in more than one leg. If there were 3 legs all
					 * pointing 'to' a station then the first one in the series will be the one the LRUD data
					 * is associated with really, but this is not recorded.
					 */
					for ( int legsIdx2 = legsIdx + 1; legsIdx2 < legs.size(); legsIdx2++ ) {
						SurveyLeg chkLeg = legs.get( legsIdx2 );
						if ( ( chkLeg.isSplay() == false )
						&& ( chkLeg.getToStn() != null )
						&& ( chkLeg.getToStn().getName().compareTo(stnName) == 0 ) ) {
							//Found another leg to this station, we need to reverse it as
							//LRUD function takes group of legs from the station
							SurveyLeg revLeg = chkLeg.clone();
							revLeg.reverseDirection();
							otherLegs.add(revLeg);
						}
					}
					//Generate LRUD data for leg, using the splays starting from that leg from station
					generateLRUDForLeg( tempLrudLeg, splaysGroup, otherLegs, logger );
					//Create an LRUD object to hold the LRUD data and copy data into it
					ToStnLRUD newLrud = new ToStnLRUD();
					newLrud.setFromStn( tempLrudLeg.getFromStn() );
					newLrud.setLeft( tempLrudLeg.getLeft(LengthUnit.Metres) );
					newLrud.setRight( tempLrudLeg.getRight(LengthUnit.Metres) );
					newLrud.setUp( tempLrudLeg.getUp(LengthUnit.Metres) );
					newLrud.setDown( tempLrudLeg.getDown(LengthUnit.Metres) );
					//Add the new LRUD leg in the series 
					toStnLRUDs.add(newLrud);
					//Stop checking series once LRUD data has been accounted for or LRUD data will get
					//duplicated in cases where the station is the toStn in more than one leg (e.g. In
					//a leap-frog survey)
					legsIdx = legs.size();
				}
			}
		}
		
	}	

	/**
	 * Generates a Left, Right, Up and Down dimension at the from station for a survey leg
	 * using the splays recorded at that station, and stores them in the leg.
	 * @param leg The survey leg to generate LRUD data for (at the From station)
	 * @param splaysGroup A list of splay legs measured from the From station of the leg
	 * @param otherLegs A list of survey legs arriving at the same station as the From station of the leg
	 */
	private void generateLRUDForLeg( SurveyLeg masterLeg, List<SurveyLeg> splayShots, List<SurveyLeg> otherLegs, Logger logger ) {
		int bearingTolerance = 3;
		List<SurveyLeg> leftShots = new ArrayList<SurveyLeg>();
		List<SurveyLeg> rightShots = new ArrayList<SurveyLeg>();
		List<SurveyLeg> upShots = new ArrayList<SurveyLeg>();
		List<SurveyLeg> downShots = new ArrayList<SurveyLeg>();
		
		//Determine best previous leg (use leg with closest bearing to onward leg)
		double bestPrevBearing = 360.0;
		int bestPrevLegIdx = -1;
		for ( int i = 0; i < otherLegs.size(); i++ ) {
			//Looking for bearing closest to master leg bearing
			double testBearing = UtilityFunctions.bearingDifferenceDegrees( otherLegs.get(i).getCompass(BearingUnit.Degrees), masterLeg.getCompass(BearingUnit.Degrees) );
			if ( testBearing < bestPrevBearing ) {
				//Found better match, so store value and index
				bestPrevBearing = testBearing;
				bestPrevLegIdx = i;
			}
		}

		//If a previous leg was found then calculate average bearing between previous and onward legs
		double bearing = masterLeg.getCompass(BearingUnit.Degrees);
		if ( bestPrevLegIdx >= 0 ) {
			double[] bearings = new double[2];

			bearings[0] = masterLeg.getCompass(BearingUnit.Degrees);
			bearings[1] = otherLegs.get(bestPrevLegIdx).getCompass(BearingUnit.Degrees);

			bearing = UtilityFunctions.averageCompassBearings(bearings);
		}
		
		//Loop through all the splays and categorise each shot into groups of L,R,U,D
		for ( int i = 0; i < splayShots.size(); i++ ) {
			SurveyLeg splayLeg = splayShots.get(i);

			//Ignore any splays which are along the path of any legs to/from this station
			//This will remove backshots and splays fired up side passages which were then surveyed
			//as side passage splays are not going to represent passage dimensions.
			boolean backShot = false;
			for ( int olegsIdx = -1; olegsIdx < otherLegs.size(); olegsIdx++ ) {
				//First check against main leg
				SurveyLeg testLeg = masterLeg;
				if (olegsIdx > -1) {
					//Then loop through other legs reversed (as we need bearing from this station, not to it)
					testLeg = otherLegs.get(olegsIdx).clone();
					testLeg.reverseDirection();
				}
				//Check if splay is equivalent to leg shot
				double bearingDiff = UtilityFunctions.bearingDifferenceDegrees( testLeg.getCompass(BearingUnit.Degrees), splayLeg.getCompass(BearingUnit.Degrees) );
				if ( bearingDiff < bearingTolerance ) {
					//Within 3 degrees of leg, so check clino
					double clinoDiff = UtilityFunctions.bearingDifferenceDegrees( testLeg.getClino(GradientUnit.Degrees), splayLeg.getClino(GradientUnit.Degrees) );
					if ( clinoDiff < bearingTolerance ) {
						//Within 3 degrees of leg, so check length
						double lengthDiff = Math.abs( testLeg.getLength( LengthUnit.Metres ) - splayLeg.getLength( LengthUnit.Metres ) );
						if ( lengthDiff < 0.2 ) {
							//Under 20cm difference in length, assume a back shot for a leg
							backShot = true;
							logger.logMessage("Ignoring splay from " + getSeriesName() + "." + splayLeg.getFromStn().getName() + 
									" with length of " + splayLeg.getLength( LengthUnit.Metres ) + " as splay too closely matching a leg and so assumed to be a back-shot.");
						}
					}
				}
			}

			if ( backShot == false ) {
				double splayClino = splayLeg.getClino(GradientUnit.Degrees);
				if ( splayClino > 20 ) {
					//Up shot
					upShots.add(splayLeg);
				}
				else if ( splayClino < -20 ) {
					//Down shot
					downShots.add(splayLeg);
				}
				//As well as up or down, some splays may also be the best Left or Right
				if ( ( splayClino < 70 ) && ( splayClino > -70 ) ) {
					//Left or right shots (not steeper than 70 deg.)
					//Determine whether left or right shot
					double masterBearing = masterLeg.getCompass(BearingUnit.Degrees);
					//Normalise bearings to reference of zero degrees for onward leg
					double splayCorrected = splayLeg.getCompass(BearingUnit.Degrees) - masterBearing;
					if ( splayCorrected < 0 ) {
						splayCorrected += 360;
					}
					if ( bestPrevLegIdx >= 0 ) {
						//Two legs to consider
						double prevLegBearing = otherLegs.get(bestPrevLegIdx).getCompass(BearingUnit.Degrees);
						//Convert previous leg bearing to bearing from the station for the splay adjusted with respect to onward leg bearing
						double prevLegBackBearingCorrected = UtilityFunctions.adjustBearingWithinDegreesRange( 
								180 + prevLegBearing - masterBearing, 0, 360 ); 
						/* We now have three bearings. The onward leg is adjusted to zero deg. 
						 * The back bearing for the previous leg is adjusted relative to the zero bearing of the onward leg.
						 * The splay is also adjusted relative to the zero bearing of the onward leg.
						 * Splays which are aligned with any leg from the station should already have been discarded.
						 * So now we just need to determine if this splay as a left or right splay.
						 */
						if ( splayCorrected < prevLegBackBearingCorrected ) {
							//Right
							rightShots.add(splayLeg);
						}
						else {
							//Left
							leftShots.add(splayLeg);
						}
					}
					else {
						//Only the one leg to consider as first or last leg of chain of legs in series

						//Angle between splay and leg bearings is now in the range 0-360 deg.
						if ( ( splayCorrected > bearingTolerance ) || ( splayCorrected > (360 - bearingTolerance) ) ) {
							if ( splayCorrected < ( 180 - bearingTolerance) ) {
								//Right
								rightShots.add(splayLeg);
							}
							else if ( splayCorrected > ( 180 + bearingTolerance) ) {
								//Left
								leftShots.add(splayLeg);
							}
							else {
								//Splay is within tolerance of back bearing for leg, so too shallow an angle to use for LRUD calc.
								logger.logMessage("Ignoring splay from " + getSeriesName() + "." + splayLeg.getFromStn().getName() + 
										" with bearing of " + splayLeg.getCompass(BearingUnit.Degrees) + " as bearing is < " + bearingTolerance + " degrees off back bearing of leg.");
							}
						}
						else {
							//Splay is within tolerance of bearing for leg, so too shallow an angle to use for LRUD calc.
							logger.logMessage("Ignoring splay from " + getSeriesName() + "." + splayLeg.getFromStn().getName() + 
									" with bearing of " + splayLeg.getCompass(BearingUnit.Degrees) + " as bearing is < " + bearingTolerance + " degrees off bearing of leg.");
						}
					}

				}
			}
		}
		
		//Set flag for this station to say whether all splays should be output
		boolean keepAllSplays = false;
		if (( upShots.size() > 1 )
		|| ( downShots.size() > 1 ) 
		|| ( leftShots.size() > 1 )
		|| ( rightShots.size() > 1 )) {
			keepAllSplays = true;
		}
		
		//Find best up shot
		double bestValue = 0.0;
		int bestIdx = -1;
		for ( int i = 0; i < upShots.size(); i++ ) {
			//Looking for highest clino reading
			if ( upShots.get(i).getClino(GradientUnit.Degrees) > bestValue ) {
				//Found better match, so store value and index
				bestValue = upShots.get(i).getClino(GradientUnit.Degrees);
				bestIdx = i;
			}
		}
		//Add up shot if found
		if ( bestIdx >= 0  ) {
			//Found best up shot
			masterLeg.setUp( upShots.get(bestIdx).getVerticalLength(), LengthUnit.Metres );
			//Flag splay as used for Up
			if ( keepAllSplays == false ) {
				upShots.get(bestIdx).setDown( LRUD_SPECIAL_FLAG, LengthUnit.Metres );
			}
		}

		//Find best down shot
		bestValue = 0.0;
		bestIdx = -1;
		for ( int i = 0; i < downShots.size(); i++ ) {
			//Looking for lowest clino reading
			if ( downShots.get(i).getClino(GradientUnit.Degrees) < bestValue ) {
				//Found better match, so store value and index
				bestValue = downShots.get(i).getClino(GradientUnit.Degrees);
				bestIdx = i;
			}
		}
		//Add down shot if found
		if ( bestIdx >= 0  ) {
			//Found best down shot
			masterLeg.setDown( downShots.get(bestIdx).getVerticalLength(), LengthUnit.Metres );
			//Flag splay as used for Down
			if ( keepAllSplays == false ) {
				downShots.get(bestIdx).setDown( LRUD_SPECIAL_FLAG, LengthUnit.Metres );
			}
		}

		//Find best left shot
		bestIdx = findBestSplayForHorizontalDimension(leftShots, bearing, true);
		
		//Add left shot if found
		if ( bestIdx >= 0  ) {
			//Found best left shot
			double horizontalLength = leftShots.get(bestIdx).getHorizontalLength();
			double leftOrthoganal = bearing - 90;
			if ( leftOrthoganal < 0 ) {
				leftOrthoganal += 360;
			}
			masterLeg.setLeft( extentAlongBearing( leftOrthoganal,
					horizontalLength, leftShots.get(bestIdx).getCompass(BearingUnit.Degrees) ), LengthUnit.Metres );
			//Flag splay as used for Left
			if ( keepAllSplays == false ) {
				leftShots.get(bestIdx).setDown( LRUD_SPECIAL_FLAG, LengthUnit.Metres );
			}
		}

		//Find best right shot
		bestIdx = findBestSplayForHorizontalDimension(rightShots, bearing, false);
		
		//Add right shot if found
		if ( bestIdx >= 0  ) {
			//Found best right shot
			double horizontalLength = rightShots.get(bestIdx).getHorizontalLength();
			double rightOrthoganal = bearing + 90;
			if ( rightOrthoganal >= 360 ) {
				rightOrthoganal -= 360;
			}
			masterLeg.setRight( extentAlongBearing( rightOrthoganal, 
					horizontalLength, rightShots.get(bestIdx).getCompass(BearingUnit.Degrees) ), LengthUnit.Metres );
			//Flag splay as used for Right
			if ( keepAllSplays == false ) {
				rightShots.get(bestIdx).setDown( LRUD_SPECIAL_FLAG, LengthUnit.Metres );
			}
		}
	}

	/**
	 * Determines which of the splays is the best one to use to calculate the horizontal dimension for the
	 * cave passage at this station
	 * 
	 * @param splays Array of splay shots to pick the best shot from
	 * @param forwardBearing Bearing from the station to represent the passage direction
	 * @param left Flag to indicate if we want to find the best left shot or best right shot
	 * @return Index of the best matching splay shot from the input array of splays
	 */
	private int findBestSplayForHorizontalDimension(List<SurveyLeg> splays, double forwardBearing, boolean left) {
		//Find best shot (greatest horizontal extent along vector orthogonal to leg bearing)
		double bestValue = 0.0;
		int bestIdx = -1;
		for ( int i = 0; i < splays.size(); i++ ) {
			//Calculate corrected compass bearing for splay, normalised to a passage trending due North (zero degrees)
			double shotCorrected = splays.get(i).getCompass(BearingUnit.Degrees) - forwardBearing;
			if ( shotCorrected < 0 ) {
				shotCorrected += 360;
			}
			//Calculate horizontal extent of leg in direction orthogonal to corrected bearing (left or right)
			double orthogonal;
			if ( left ) {
				orthogonal = 270.0;
			}
			else {
				orthogonal = 90.0;
			}
			double horizontalLength = splays.get(i).getHorizontalLength();
			double orthogonalExtent = extentAlongBearing( orthogonal, 
					horizontalLength, shotCorrected);

			if ( orthogonalExtent > bestValue ) {
				//Found better match, so store value and index
				bestValue = orthogonalExtent;
				bestIdx = i;
			}
		}
		
		return bestIdx;
	}
	
	/**
	 * Calculates the distance a vector extends along a bearing. Used to work out the distance of the cave wall
	 * for a left or right passage dimension in a given direction from a station for a horizontal length measured
	 * along another bearing
	 * @param bearing The direction we need the distance to the wall for
	 * @param vectorLength The length of the horizontal measurement we have
	 * @param vectorBearing The direction of the horizontal measurement we have
	 * @return The calculated distance to the wall from the station in the direction of the bearing
	 */
	private double extentAlongBearing( double bearing, double vectorLength, 
			double vectorBearing) {
		double bearingDifference = UtilityFunctions.bearingDifferenceDegrees(bearing, vectorBearing);
		double distance = vectorLength * Math.cos(Math.PI * bearingDifference / 180);
		
		return distance;
	}
	
	/**
	 * Reverses the order of all legs in the series, so in a linear chain of stations the last
	 * station is placed first, and the first station is placed last.
	 * Was written to Toporobot export, but then was not needed. Leaving the code here but
	 * commented out as not currently used.
	public void reverseSeries() {
		//Loop through all legs, moving each one to the start of the series
		for (int idx = 0; idx < legs.size(); idx++) {
			SurveyLeg leg = legs.remove(idx);
			//Reverse leg direction
			SurveyStation stn = leg.getFromStn();
			leg.setFromStn( leg.getToStn() );
			leg.setToStn( stn );
			double compass = leg.getCompass();
			if ( compass > 180 ) {
				compass -= 180;
			}
			else {
				compass += 180;
			}
			leg.setCompass( compass );
			leg.setClino( -leg.getClino() );
			//Put leg at start of series
			legs.add(0, leg);
		}
	}
	 */
	
}
