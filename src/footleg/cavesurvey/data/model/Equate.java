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
 * Class to represent an equate between two survey stations in different series
 *
 * @author      Footleg
 * @version     2015.08.31                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 */
public class Equate {
	private String series1;
	private String stn1;
	private String series2;
	private String stn2;
	
	/**
	 * Create an equate defining the link between two stations in two series
	 * 
	 * @param seriesPrefix1 Full path to the first series
	 * @param stnName1      Name of the station in the first series joining to the second series
	 * @param seriesPrefix2 Full path to the second series
	 * @param stnName2      Name of the station in the second series joining to the first series
	 */
	public Equate(String seriesPrefix1, String stnName1, String seriesPrefix2, String stnName2) {
		//Need to combine series prefix with station name and then split off 
		//the station name from full series name
		String[] resplitStnRef1 = combineAndResplitFullStnRef( seriesPrefix1, stnName1 );
		String[] resplitStnRef2 = combineAndResplitFullStnRef( seriesPrefix2, stnName2 );
		
		this.series1 = resplitStnRef1[0];
		this.stn1 = resplitStnRef1[1];
		this.series2 = resplitStnRef2[0];
		this.stn2 = resplitStnRef2[1];
	}
	
	public String getSeries1() {
		return series1;
	}
	
	public String getStn1() {
		return stn1;
	}
	
	public String getSeries2() {
		return series2;
	}
	
	public String getStn2() {
		return stn2;
	}
	
	private String[] combineAndResplitFullStnRef( String seriesPrefix, String stnName ) {
		String[] results = new String[2];
		
		//Combine prefix and station name into fully qualified station reference
		String fullStnRef = seriesPrefix + "." + stnName;
		
		//Split series name from station name
		int pos = fullStnRef.lastIndexOf('.');
		if ( pos > 0 ) {
			//Split at this position
			results[0] = fullStnRef.substring( 0, pos );
			results[1] = fullStnRef.substring( pos + 1 );
		}
		else {
			//No '.' separator in equate, so throw error
			throw new RuntimeException( "Equate does not contain a series name and station." );
		}
		
		return results;
	}
	
}
