/**
 * Copyright (C) 2015-2015 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
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

import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;

/**
 * Data processing class for functions which process survey data.
 * 
 * @author      Footleg
 * @version     2015.11.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public final class SurveyProcessing {

	/**
	 * Check the series for station names containing a dot separator, and if found then reorganise
	 * the series into a set of nested series using the full path name of the series to define the
	 * child series which the stations belong to. Only process series containing legs.
	 * @param series The survey series to rearrange
	 * @return True if series was rearranged
	 */
	public static boolean generateSeriesFromFullPathStationNames( SurveySeries series ) {
		boolean rearranged = false;
		
		if ( series.innerSeriesCount() == 0 ) {
			int matchingChildSeriesIdx = -1;
			
			//Loop through all legs in the series
			for (int legIdx = 0; legIdx < series.legCount(); legIdx++) {
				SurveyLeg leg = series.getLegRaw( legIdx );
				
				//Check fromStn name and check for dot separator
				String fromStnName = leg.getFromStn().getName();
				int sepPos = fromStnName.indexOf('.');
				if ( sepPos > 0 ) {
					String prefix = fromStnName.substring(0, sepPos);
					
					//Check for child series matching the name of the prefix
					if ( series.innerSeriesCount() > 0 ) {
						if ( series.getInnerSeries( matchingChildSeriesIdx ).getSeriesName().equals( prefix ) == false ) {
							//Need to search all inner series for match
							matchingChildSeriesIdx = -1;
							for ( int j = 0; j < series.innerSeriesCount(); j++ ) {
								if ( series.getInnerSeries( j ).getSeriesName().equals( prefix ) ) {
									//Store matching index
									matchingChildSeriesIdx = j;
									break;
								}
							}
						}
					}
					
					//Create new child series if matching series not found
					if ( matchingChildSeriesIdx == -1 ) {
						SurveySeries newSeries = new SurveySeries( prefix );
						series.addSeries(newSeries );
						matchingChildSeriesIdx = series.innerSeriesCount() - 1;
					}
					//Remove prefix from fromStn name
					String newFromName = fromStnName.substring( sepPos + 1 );
					leg.getFromStn().setName(newFromName);
					
					//If to station shares the same prefix, then remove prefix from that
					String toStnName = leg.getToStn().getName();
					int sepToPos = toStnName.indexOf('.');
					if ( sepToPos > 0 ) {
						if ( toStnName.substring(0, sepToPos).equals( prefix ) ) {
							String newToName = toStnName.substring( sepToPos + 1 );
							leg.getToStn().setName(newToName);
						}
					}
					else {
						//Otherwise, we need an equate from the toStn to the equivalent station in the series it matches
					}
					
					//Put leg into child series
					series.getInnerSeries( matchingChildSeriesIdx ).addLeg (leg );
					
				}
			}
			
			//Loop through all legs and remove them from the series
			for (int legIdx = series.legCount() - 1; legIdx >= 0; legIdx--) {
				series.removeLeg( legIdx );
			}
			rearranged = true;
		}
		
		return rearranged;
	}
}
