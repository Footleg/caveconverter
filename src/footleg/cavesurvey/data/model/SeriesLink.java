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

/**
 * Represents a link or join between two survey series. The link specifies a series and 
 * station for each of the two series being joined which are the same point in the cave.
 * The series link lives in a series which both the linked series are inside (or the
 * same as).
 * 
 * @author      Footleg
 * @version     2013.01.20                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 */
public class SeriesLink {
	private String series1;
	private String series2;
	private SurveyStation stn2;
	private SurveyStation stn1;
	
	public SeriesLink(String series1, SurveyStation stn1, String series2, SurveyStation stn2) {
		this.series1 = series1;
		this.series2 = series2;
		this.stn1 = stn1;
		this.stn2 = stn2;
	}

	public String getSeries1() {
		return series1;
	}

	public String getSeries2() {
		return series2;
	}

	public SurveyStation getStn2() {
		return stn2;
	}

	public SurveyStation getStn1() {
		return stn1;
	}

	public void setSeries2(String series2) {
		this.series2 = series2;
	}

}
