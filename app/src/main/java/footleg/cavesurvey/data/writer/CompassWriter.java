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
package footleg.cavesurvey.data.writer;

import java.util.ArrayList;
import java.util.List;

import footleg.cavesurvey.converter.CaveConverter;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;

/**
 * Writer for Compass file format text data.
 * 
 * @author Footleg
 * @version 2024.09.24 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class CompassWriter {
	private List<String> shortNames = new ArrayList<String>();
	private Logger logger;

	public CompassWriter(Logger logger) {
		super();
		this.logger = logger;
	}

	/**
	 * Generates Compass format data from a cave survey
	 * 
	 * TODO: Compass Writer CLASS IS NOT FULLY IMPLEMENTED YET AND WILL NOT GENERATE
	 * VALID DATA
	 * 
	 * @param surveyData The cave survey model to generate Compass data for
	 * @return Text lines of Compass format data
	 */
	public List<String> generateCompassData(CaveSurvey surveyData) {
		List<String> outputData = new ArrayList<String>();
		String caveName = "cave";

		// See if we can determine cave name from series name of first series
		SurveySeries series1 = surveyData.get(0);
		if (series1.getSeriesName().length() > 0) {
			// Check for dot separator
			int pos = series1.getSeriesName().indexOf('.');
			if (pos > 0) {
				// Take name before first separator
				caveName = series1.getSeriesName().substring(0, pos);
			} else {
				// Work backwards until first non-numeric character found
				for (int nameIdx = series1.getSeriesName().length() - 1; nameIdx > -1; nameIdx--) {
					// Check for non-numerical character
					int charCode = series1.getSeriesName().charAt(nameIdx);
					if (charCode < 48 || charCode > 57) {
						// Take name prior to this point
						caveName = series1.getSeriesName().substring(0, nameIdx + 1);
						nameIdx = 0;
					}
				}
			}
		}

		// Generate short names for all series
		for (int seriesIdx = 0; seriesIdx < surveyData.size(); seriesIdx++) {
			String seriesName = surveyData.get(seriesIdx).getSeriesName();
			String reducedName = "";

			/*
			 * //Remove cave name prefix if found
			 * if ( seriesName.substring(0, caveName.length() + 1 ).compareTo( caveName +
			 * "." ) == 0 ) {
			 * seriesName = seriesName.substring(caveName.length() + 1);
			 * }
			 */

			// Look for last part of multi-part names
			String nameLastPart = seriesName;
			int lstIdx = seriesName.lastIndexOf('.');
			if (lstIdx > -1 && lstIdx < (seriesName.length() - 1)) {
				// Take just last part of name
				nameLastPart = seriesName.substring(lstIdx + 1);
			}

			// Remove illegal characters
			for (int idx = 0; reducedName.length() < 4 && idx < nameLastPart.length(); idx++) {
				if (nameLastPart.charAt(idx) != '.') {
					reducedName += nameLastPart.charAt(idx);
				}
			}

			int trys = 0;
			while (trys < 110) {
				trys++;
				String testName = "    ";

				switch (trys) {
					case 1:
						// Try using first 4 chars of name padded with leading spaces
						testName = reducedName;
						break;
					case 2:
						// Try removing vowels
						reducedName = "";
						for (int idx = 0; reducedName.length() < 4 && idx < nameLastPart.length(); idx++) {
							if (nameLastPart.charAt(idx) != '.'
									&& nameLastPart.charAt(idx) != 'a'
									&& nameLastPart.charAt(idx) != 'e'
									&& nameLastPart.charAt(idx) != 'i'
									&& nameLastPart.charAt(idx) != 'o'
									&& nameLastPart.charAt(idx) != 'u'
									&& nameLastPart.charAt(idx) != 'A'
									&& nameLastPart.charAt(idx) != 'E'
									&& nameLastPart.charAt(idx) != 'I'
									&& nameLastPart.charAt(idx) != 'O'
									&& nameLastPart.charAt(idx) != 'U') {
								reducedName += nameLastPart.charAt(idx);
							}
						}
						testName += reducedName;
						break;
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
						// Try adding increment number to end of 3 char name
						testName += reducedName.substring(0, 3) + (trys - 2);
						break;
					default:
						// Try adding increment number to end of 2 char name
						testName += reducedName.substring(0, 2) + (trys - 11);
				}

				// Trim to 4 chars
				testName = testName.substring(testName.length() - 4);

				// Check if name already used
				if (nameAlreadyUsed(testName) == false) {
					// Use this name
					shortNames.add(testName);
					trys = 999;
				}
			}
			// Check name was added
			if (trys != 999) {
				RuntimeException ex = new RuntimeException(
						"Failed to generate unique short name for series name: " + seriesName);
				throw ex;
			}
		}

		// Loop through all series
		for (int seriesIdx = 0; seriesIdx < surveyData.size(); seriesIdx++) {
			SurveySeries series = surveyData.get(seriesIdx);
			List<String> legsData = new ArrayList<String>();
			List<String> fixedStnsData = new ArrayList<String>();
			List<String> passageData = new ArrayList<String>();

			// Write header lines for series
			outputData.add(caveName);
			outputData.add("SURVEY NAME: " + series.getSeriesName());
			outputData.add("SURVEY DATE: " + series.getSurveyDate());
			outputData.add("SURVEY TEAM: ");
			outputData.add(" ");
			outputData.add("DECLINATION:    " + series.getCompassCalibration(BearingUnit.Degrees)
					+ "  FORMAT: DMMDLRUDLADN  CORRECTIONS: 0.00 0.00 0.00");
			outputData.add(" ");
			outputData.add(
					"FROM           TO   LENGTH  BEARING      INC     LEFT       UP     DOWN    RIGHT   FLAGS  COMMENTS");
			/*
			 * //Add equates to output
			 * for ( int linkIdx = 0; linkIdx < series.getLinks().size(); linkIdx++ ) {
			 * SeriesLink link = series.getLinks().get(linkIdx);
			 * String equate = "*EQUATE ";
			 * if ( link.getLinkedSeries() == -1 ) {
			 * //Link to series not in current data file, so create 'false' equate
			 * placeholder in file
			 * equate = ";" + equate + series.getSeriesName() + "." +
			 * link.getLocalStation().getName() + " " +
			 * "CAVE" + "." +
			 * link.getLocalStation();
			 * }
			 * else {
			 * equate += series.getSeriesName() + "." + link.getLocalStation().getName() +
			 * " " +
			 * surveyData.get( link.getLinkedSeries() ).getSeriesName() + "." +
			 * link.getLinkedStation().getName();
			 * }
			 * outputData.add( equate );
			 * }
			 * outputData.add( "");
			 */
			// Start series block
			outputData.add("*BEGIN " + series.getSeriesName());

			// TODO Write date if present
			// if ( series.getSurveyDate().length() > 0 ) {
			// compassData.add( ";Date " + series.getSurveyDate() );
			// }

			// Write calibration lines
			if (series.getTapeCalibration(LengthUnit.Metres) != 0) {
				outputData.add("*CALIBRATE tape " + series.getTapeCalibration(LengthUnit.Feet));
			}
			if (series.getCompassCalibration(BearingUnit.Degrees) != 0) {
				outputData.add("*CALIBRATE declination " + series.getCompassCalibration(BearingUnit.Degrees));
			}
			if (series.getClinoCalibration(GradientUnit.Degrees) != 0) {
				outputData.add("*CALIBRATE clino " + series.getClinoCalibration(GradientUnit.Degrees));
			}
			outputData.add("");

			// Loop through the series legs writing details of each leg found
			for (int legIdx = 0; legIdx < series.legCount(); legIdx++) {
				SurveyLeg leg = series.getLegRaw(legIdx);

				// Check for valid leg
				String fromStn = leg.getFromStn().getName();
				String toStn = leg.getToStn().getName();
				if (leg.getLength(LengthUnit.Metres) > 0) {
					// Write leg data
					legsData.add(fromStn + "\t" + toStn + "\t" +
							CaveConverter.padNumber(leg.getLength(LengthUnit.Feet), 2, 5) + "\t" +
							CaveConverter.padNumber(leg.getCompass(BearingUnit.Degrees), 2, 6) + "\t" +
							CaveConverter.padNumber(leg.getClino(GradientUnit.Degrees), 2, 6) + "\t");
				} else {
					// Zero length leg
					// Get to stn name if there is one
					if (leg.isSplay() == false) {
						// Valid leg, so write as equate
						legsData.add("*EQUATE " + fromStn + "\t" + toStn);
					}
				}

				// Add FIXed points to fixed stns block for series
				// if (leg.hasFixedStn() == true) {
				// String fixedStnLine = "*FIX ";
				// if ( leg.isFromStnFixed() == true ) {
				// fixedStnLine += fromStn;
				// }
				// else {
				// fixedStnLine += getActualStationName( series, leg.getToStn() );
				// }
				// fixedStnLine += "\t" + leg.getEasting() + "\t" + leg.getNorthing() + "\t" +
				// leg.getAltitude();
				// fixedStnsData.add( fixedStnLine );
				// }

				// Add LRUD to passages block for series
				if (leg.getLeft(LengthUnit.Metres) + leg.getRight(LengthUnit.Metres) +
						leg.getUp(LengthUnit.Metres) + leg.getDown(LengthUnit.Metres) > 0.0) {
					passageData.add(fromStn + "\t" + leg.getLeft(LengthUnit.Feet) + "\t" +
							leg.getRight(LengthUnit.Feet) + "\t" + leg.getUp(LengthUnit.Feet) + "\t" +
							leg.getDown(LengthUnit.Feet) + "\t");
				}
			}
			// Write fixes data block
			if (fixedStnsData.size() > 0) {
				outputData.addAll(fixedStnsData);
			}
			// Write legs data block
			outputData.addAll(legsData);
			// Write passage data block
			if (passageData.size() > 0) {
				outputData.add("");
				outputData.add("*data passage station left right up down");
				outputData.addAll(passageData);
			}
			// Close the series
			outputData.add("*END " + series.getSeriesName() + "\n");
		}

		// Close the cave name
		outputData.add("*END " + caveName);

		// Use the logger just to stop the compiler warning that it is never used
		logger.logMessage("Finished writing Compass file (it won't be valid as this class is still in development).");

		return outputData;
	}

	/**
	 * Looks up short series name for series from array of names created previously
	 * 
	 * @param seriesIdx
	 * @return short (4 character) name for series
	 */
	protected String getShortSeriesName(int seriesIdx) {
		return shortNames.get(seriesIdx);
	}

	/**
	 * Determines if a name has already been used for a short series name
	 * 
	 * @param name
	 * @return true if name has already been used for another series
	 */
	private boolean nameAlreadyUsed(String name) {
		boolean found = false;
		for (int i = 0; i < shortNames.size(); i++) {
			if (shortNames.get(i).compareToIgnoreCase(name) == 0) {
				found = true;
				i = shortNames.size();
			}
		}
		return found;
	}

}
