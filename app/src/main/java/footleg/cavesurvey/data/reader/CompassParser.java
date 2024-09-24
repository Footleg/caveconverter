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
package footleg.cavesurvey.data.reader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.Equate;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Parser for Compass format text data files.
 * 
 * @author Footleg
 * @version 2024.09.24 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 * 
 */
public class CompassParser {
	private Logger logger;
	private static double nullReading = -999.0;

	/**
	 * Class constructor
	 * 
	 * @param logger Logging class to output information, warning and error messages
	 */
	public CompassParser(Logger logger) {
		super();
		this.logger = logger;
	}

	/**
	 * Parse Compass format data into the cave data model
	 * 
	 * @param surveyFileData ListArray of data lines from a Compass file
	 * @return Cave Survey object
	 * @throws ParseException Exception raised when information in a survey data
	 *                        file is not supported or valid for the format
	 */
	public CaveSurvey parseFile(List<String> surveyFileData) throws ParseException {
		/**
		 * Read state codes:
		 * 0=starting new file
		 * 1=header lines
		 * 2=calibration data
		 * 3=data line header
		 * 4=data lines
		 */
		int state = 0;

		if (surveyFileData.size() == 0) {
			throw new ParseException("Empty survey data passed to Compass File Parser.", 0);
		}

		// Create cave survey object to hold data
		CaveSurvey allSeries = new CaveSurvey(logger);

		// Create a series instance to use as a pointer to the active series data is
		// being read from
		SurveySeries liveSeries = null;

		// Declare flags to store status of legs being read
		boolean duplicateFlag = false;
		boolean ignoreLeg = false;
		boolean splayFlag = false;
		boolean surfaceFlag = false;
		boolean backSights = false;

		// Loop through all data lines
		for (int i = 0; i < surveyFileData.size(); i++) {
			int lineNo = i + 1;
			String dataLine = surveyFileData.get(i);

			// Trim whitespace off line ends (unless a single form feed character)
			if (dataLine.equalsIgnoreCase("\f") == false) {
				dataLine = dataLine.trim();
			}
			// Skip blank lines
			if (dataLine.length() > 0) {
				switch (state) {
					case 0:
						// First line of series should be cave name
						String caveName = dataLine.replace(' ', '_');
						// Create series for cave name
						liveSeries = new SurveySeries(caveName);
						// Add to cave survey
						allSeries.add(liveSeries);
						state++;
						break;
					case 1:
						// Header lines
						caveName = dataLine.replace(' ', '_');
						if ((dataLine.length() < 12) || (dataLine.substring(0, 12).compareTo("SURVEY NAME:") != 0)) {
							// Repeat of cave name, which we can ignore
							// (changed to allow any line which is not a SURVEY NAME labelled data line as
							// some files have multiple cave names)
						} else if ((dataLine.length() > 11)
								&& (dataLine.substring(0, 12).compareTo("SURVEY NAME:") == 0)) {
							// Create series for this survey name
							String seriesName = dataLine.substring(12).trim();
							liveSeries = new SurveySeries(seriesName);

							// Next line: Data and Comment
							i++;
							lineNo = i + 1;
							String surveyDataComment = surveyFileData.get(i).trim();
							int commentPos = surveyDataComment.indexOf("COMMENT:");
							if (surveyDataComment.substring(0, 12).compareTo("SURVEY DATE:") == 0) {
								// Check if comment was present on line
								int dateEnd = commentPos;
								if (commentPos == -1) {
									// No comment, so set commentPos to end of line
									commentPos = surveyDataComment.length();
									dateEnd = commentPos;
								} else {
									// Set comment start position to after COMMENT:
									commentPos = commentPos + 8;
								}
								if (commentPos > 18) {
									// Separate date and comment
									String dateString = surveyDataComment.substring(12, dateEnd).trim();
									String comment = surveyDataComment.substring(commentPos).trim();
									// Parse date
									Date value = UtilityFunctions.stringToDate(dateString, "M d yy");
									// Store date and comment in series
									liveSeries.setSurveyDate(value);
									liveSeries.setComment(comment);

									// Survey team on next pair of lines
									i++;
									lineNo = i + 1;
									String surveyTeam1 = surveyFileData.get(i).trim();
									if (surveyTeam1.substring(0, 12).compareTo("SURVEY TEAM:") == 0) {
										i++;
										lineNo = i + 1;
										// String surveyTeam2 = surveyFileData.get(i).trim();
										// TODO Store team details in survey series

										// Header section done
										state++;
									} else {
										throw new ParseException(UtilityFunctions.formatFileParserMsg(
												"Did not find expected 'SURVEY TEAM:' at start of line. Found: "
														+ dataLine,
												lineNo), lineNo);
									}
								} else {
									throw new ParseException(UtilityFunctions.formatFileParserMsg(
											"Did not find expected 'COMMENT:' or valid length date string on line. Found: "
													+ dataLine,
											lineNo), lineNo);
								}
							} else {
								throw new ParseException(UtilityFunctions.formatFileParserMsg(
										"Did not find expected 'SURVEY DATE:' at start of line. Found: " + dataLine,
										lineNo), lineNo);
							}
						} else {
							throw new ParseException(
									UtilityFunctions.formatFileParserMsg(
											"Did not find expected 'SURVEY NAME:' line. Found: " + dataLine, lineNo),
									lineNo);
						}

						break;
					case 2:
						// calibration data
						if (liveSeries != null && dataLine.substring(0, 12).compareTo("DECLINATION:") == 0) {
							int formatPos = dataLine.indexOf("FORMAT:");
							int corrections1Pos = dataLine.indexOf("CORRECTIONS:");
							int corrections1EndPos = dataLine.length();
							// int corrections2Pos = dataLine.indexOf("CORRECTIONS2:");
							// int corrections2EndPos = dataLine.length();
							int declinationEndPos = dataLine.length();
							// int formatEndPos = dataLine.length();
							// // Determine end positions of data items
							// if (corrections2Pos > 12) {
							// corrections1EndPos = corrections2Pos;
							// if (corrections1Pos > 12) {
							// formatEndPos = corrections1Pos;
							// } else {
							// formatEndPos = corrections2Pos;
							// }
							// } else {
							// if (corrections1Pos > 12) {
							// formatEndPos = corrections1Pos;
							// }
							// }
							// Process optional file format code
							if (formatPos > 12) {
								declinationEndPos = formatPos;
								// String formatCode = dataLine.substring(formatPos + 7, formatEndPos).trim();
								// If format string is 15 chars, then 14th is Backsights option and 15th is LRUD
								// association
								// If format string is 13 chars, then 12th is Backsights option and 13th is LRUD
								// association
								// If format string is 12 chars, then 12th is Backsights option and there is no
								// LRUD association
								// When there are backsights (option=B) then the header row has columns AZM2 and
								// INC2, which we detect further
								// down and so set backsights from these, ignoring the format code string here.
								// TODO Set series default units (once supported in series class)
								// TODO Set default data order for series (once supported in series class)
								// TODO Handle LRUD on toStns (currently assumes they are for fromStns)
							}
							// Process declination
							String declination = dataLine.substring(12, declinationEndPos).trim();
							liveSeries.setDeclination(-Double.parseDouble(declination));
							// Process optional corrections
							if (corrections1Pos > 12) {
								String corrections = dataLine.substring(corrections1Pos + 12, corrections1EndPos)
										.trim();
								String[] data = UtilityFunctions.cleanAndSplitDataLine(corrections);
								liveSeries.setCompassCalibration(-Double.parseDouble(data[0]), BearingUnit.Degrees);
								liveSeries.setClinoCalibration(-Double.parseDouble(data[1]), GradientUnit.Degrees);
								liveSeries.setTapeCalibration(-Double.parseDouble(data[2]), LengthUnit.Feet);
							}
							// Process optional corrections2
							// if (corrections2Pos > 12) {
							// String corrections2 = dataLine.substring(corrections2Pos + 13,
							// corrections2EndPos)
							// .trim();
							// String[] data = UtilityFunctions.cleanAndSplitDataLine(corrections2);
							// // TODO Handle corrections for second set of instruments used for back
							// bearings
							// }
							// Declination line done
							state++;
						} else {
							throw new ParseException(UtilityFunctions
									.formatFileParserMsg("Did not find expected 'DECLINATION:' line.", lineNo), lineNo);
						}

						break;
					case 3:
						// data line header
						// "FROM TO LENGTH BEARING INC LEFT UP DOWN RIGHT FLAGS COMMENTS"
						// "FROM TO LENGTH BEARING DIP LEFT UP DOWN RIGHT AZM2 INC2 FLAGS COMMENTS"
						// TODO Support different ordering of LRUD fields in Compass files
						String[] headerData = UtilityFunctions.cleanAndSplitDataLine(dataLine);
						if ((headerData[0].compareTo("FROM") == 0)
								&& (headerData[1].compareTo("TO") == 0)
								&& (headerData[2].compareTo("LENGTH") == 0)
								&& (headerData[3].compareTo("BEARING") == 0)
								&& ((headerData[4].compareTo("INC") == 0) || (headerData[4].compareTo("DIP") == 0))) {
							if ((headerData[9].compareTo("AZM2") == 0)
									&& (headerData[11].compareTo("FLAGS") == 0)
									&& (headerData[12].compareTo("COMMENTS") == 0)) {
								// Header for data with back bearings
								backSights = true;
								// Ready to read data lines
								state++;
							} else if ((headerData[9].compareTo("FLAGS") == 0)
									&& (headerData[10].compareTo("COMMENTS") == 0)) {
								// Header for data without back bearings
								backSights = false;
								// Ready to read data lines
								state++;
							}
							if ((headerData[5].compareTo("LEFT") == 0)
									&& (headerData[6].compareTo("UP") == 0)
									&& (headerData[7].compareTo("DOWN") == 0)
									&& (headerData[8].compareTo("RIGHT") == 0)) {
								// Header for data with back bearings
							} else {
								throw new ParseException(UtilityFunctions.formatFileParserMsg(
										"LRUD data heading indicates unsupported order. Currently only LEFT UP DOWN RIGHT field order is supported.",
										lineNo), lineNo);
							}
						} else {
							throw new ParseException(UtilityFunctions
									.formatFileParserMsg("Did not find expected data heading line.", lineNo), lineNo);
						}
						break;
					case 4:
						// data lines
						// " 1 2 85.00 45.00 0.00 10.00 3.00 0.00 10.00 #|L#"
						// " 7 8 17.00 201.00 0.00 0.00 5.00 0.00 4.00 tributary enters here on left "

						// Check for end of series (form feed character)
						if (dataLine.equalsIgnoreCase("\f")) {
							// Close series to cave name series and start searching for next one
							allSeries.get(0).addSeries(liveSeries);
							state = 1;
						} else {
							// Process data line
							String[] data = UtilityFunctions.cleanAndSplitDataLine(dataLine);
							SurveyLeg leg = new SurveyLeg();
							double backBearing = nullReading;
							double backClino = nullReading;

							// Create record from the items
							int index = 0;
							while (index < data.length) {
								String item = data[index];

								// Put item into appropriate value
								switch (index) {
									case 0:
										// TODO Add support for retaining station name when not a number
										leg.setFromStn(
												UtilityFunctions.createStationFromNameForSeries(item, liveSeries));
										break;
									case 1:
										// TODO Add support for retaining station name when not a number
										leg.setToStn(UtilityFunctions.createStationFromNameForSeries(item, liveSeries));
										break;
									case 2:
										leg.setLength(Double.parseDouble(item), LengthUnit.Feet);
										break;
									case 3:
										// if ( item.compareTo("-") == 0 ) {
										// leg.setCompass( 0, BearingUnit.Degrees );
										// }
										// else {
										leg.setCompass(Double.valueOf(item), BearingUnit.Degrees);
										// }
										break;
									case 4:
										String val = item;

										// Compass does not support all these Survex like clino values. So commenting
										// out.
										// double straightDown = -90;
										// double straightUp = 90;
										// double level = 0;
										// if ( val.compareToIgnoreCase("-V") == 0 ) {
										// leg.setClino( straightDown, GradientUnit.Degrees );
										// }
										// else if ( val.compareToIgnoreCase("down") == 0 ) {
										// leg.setClino( straightDown, GradientUnit.Degrees );
										// }
										// else if ( val.compareToIgnoreCase("d") == 0 ) {
										// leg.setClino( straightDown, GradientUnit.Degrees );
										// }
										// else if ( val.compareToIgnoreCase("+V") == 0 ) {
										// leg.setClino( straightUp, GradientUnit.Degrees );
										// }
										// else if ( val.compareToIgnoreCase("up") == 0 ) {
										// leg.setClino( straightUp, GradientUnit.Degrees );
										// }
										// else if ( val.compareToIgnoreCase("u") == 0 ) {
										// leg.setClino( straightUp, GradientUnit.Degrees );
										// }
										// else if ( val.compareToIgnoreCase("-") == 0 ) {
										// leg.setClino( level, GradientUnit.Degrees );
										// }
										// else if ( val.compareToIgnoreCase("level") == 0 ) {
										// leg.setClino( level, GradientUnit.Degrees );
										// }
										// else {
										leg.setClino(Double.valueOf(val), GradientUnit.Degrees);
										// }
										break;
									case 5:
									case 6:
									case 7:
									case 8:
										// "FROM TO LENGTH BEARING INC LEFT UP DOWN RIGHT FLAGS COMMENTS"
										// Ignore LRUD values which are -9999.00
										if ((item.equals("-9999.00") == false) && (item.equals("-9.90") == false)) {
											if (index == 5) {
												leg.setLeft(Double.parseDouble(item), LengthUnit.Feet);
											} else if (index == 6) {
												leg.setUp(Double.parseDouble(item), LengthUnit.Feet);
											} else if (index == 7) {
												leg.setDown(Double.parseDouble(item), LengthUnit.Feet);
											} else if (index == 8) {
												leg.setRight(Double.parseDouble(item), LengthUnit.Feet);
											}
										}
										break;
									case 9:
										if (backSights) {
											backBearing = Double.parseDouble(item);
										}
										break;
									case 10:
										if (backSights) {
											backClino = Double.parseDouble(item);
										}
										break;
								}
								index++;
							}

							// Process flags and comments off end of string from fixed position
							// (as comment will have been split into multiple data array items)
							String commentFlagsData = "";

							// Reinitialise all flags for each leg
							duplicateFlag = false;
							ignoreLeg = false;
							splayFlag = false;
							surfaceFlag = false;

							// Determine if any flags or comments data exists on this line
							int dataEndPos = surveyFileData.get(i).length();
							if (backSights) {
								if (data.length > 11) {
									String firstItem = data[11];
									dataEndPos = surveyFileData.get(i).indexOf(firstItem);
								}
							} else {
								if (data.length > 9) {
									String firstItem = data[9];
									dataEndPos = surveyFileData.get(i).indexOf(firstItem);
								}
							}

							if (surveyFileData.get(i).length() > dataEndPos) {
								commentFlagsData = surveyFileData.get(i).substring(dataEndPos);
							}

							// Initialise comment to entire flags/comments data item
							String comment = commentFlagsData;
							int commentStartPos = 0;
							if (commentFlagsData.startsWith("#|")) {
								commentStartPos = commentFlagsData.indexOf("#", 2) + 1;
							}
							if (commentStartPos > 3) {
								// Process flags
								String flags = commentFlagsData.substring(2, commentStartPos - 1);

								// Handle 'L' which means exclude from length calculations. Treat as duplicate.
								if (flags.indexOf("L") > -1) {
									duplicateFlag = true;
								}

								// Handle 'X' which means exclude from all processing
								if (flags.indexOf("X") > -1) {
									ignoreLeg = true;
								}

								// TODO Store flags 'P' and 'C' which mean do not plot, and do not apply loop
								// closure

								// Set comment to the text after the flags end character
								comment = commentFlagsData.substring(commentStartPos).trim();
							}

							// Set leg comment
							leg.setComment(comment);

							// Check leg was found
							if ((leg.getLength(LengthUnit.Metres) > -1)
									&& ((leg.getFromStn().getName().compareTo(leg.getToStn().getName()) == 0 &&
											leg.getLength(LengthUnit.Metres) == 0
											&& leg.getClino(GradientUnit.Degrees) == 0 &&
											leg.getCompass(BearingUnit.Degrees) == 0) == false)) {
								// Set flags for leg
								leg.setDuplicate(duplicateFlag);
								leg.setSplay(splayFlag);
								leg.setSurface(surfaceFlag);
								// Check if ignore leg flag was set
								if (ignoreLeg == false) {
									// Check if backsight was found
									if (backSights) {
										// Was the backsight the only sighting?
										if ((leg.getCompass(BearingUnit.Degrees) == nullReading
												|| leg.getClino(GradientUnit.Degrees) == nullReading) &&
												backBearing != nullReading && backClino != nullReading) {
											// Leg with backsight only, so reverse leg and use backsight readings
											leg.reverseDirection();
											leg.setCompass(backBearing, BearingUnit.Degrees);
											leg.setClino(backClino, GradientUnit.Degrees);
										} else if (leg.getCompass(BearingUnit.Degrees) != nullReading
												&& leg.getClino(GradientUnit.Degrees) == nullReading &&
												backBearing == nullReading && backClino != nullReading) {
											// Rear sighting for clino, but forward sighting for compass. Just reverse
											// clino sighting
											leg.setClino(-backClino, GradientUnit.Degrees);
										} else if (leg.getCompass(BearingUnit.Degrees) == nullReading
												&& leg.getClino(GradientUnit.Degrees) != nullReading &&
												backBearing != nullReading && backClino == nullReading) {
											// Rear sighting for compass, but forward sighting for clino. Reverse leg,
											// apply back bearing then reverse back
											SurveyLeg revLeg = leg.clone();
											revLeg.reverseDirection();
											revLeg.setCompass(backBearing, BearingUnit.Degrees);
											revLeg.reverseDirection();
											leg.setCompass(revLeg.getCompass(BearingUnit.Degrees), BearingUnit.Degrees);
										} else if (backBearing != nullReading && backClino != nullReading) {
											// Leg with both backsight and foresight, so duplicate it to create the
											// reverse leg
											SurveyLeg revLeg = leg.clone();
											revLeg.reverseDirection();
											revLeg.setCompass(backBearing, BearingUnit.Degrees);
											revLeg.setClino(backClino, GradientUnit.Degrees);
											// Add back leg to series
											// liveSeries.addLeg(revLeg); //Removed this as survex cavern hangs for
											// large caves with back legs.
											// Alternatively, get average for forward and back sights
											revLeg.reverseDirection();
											double[] bearings = new double[2];
											bearings[0] = leg.getCompass(BearingUnit.Degrees);
											bearings[1] = revLeg.getCompass(BearingUnit.Degrees);
											double average = UtilityFunctions.averageCompassBearings(bearings);
											leg.setCompass(average, BearingUnit.Degrees);
											average = (leg.getClino(GradientUnit.Degrees)
													+ revLeg.getClino(GradientUnit.Degrees)) / 2;
											leg.setClino(average, GradientUnit.Degrees);
										}
									}

									// Add leg to series
									if (liveSeries != null) {
										liveSeries.addLeg(leg);
									}
								}
							}
						}
						break;
				}
			}
		}

		// Generate equates by matching station names
		List<Equate> equates = new ArrayList<Equate>();
		// Loop through all series
		for (int idx1 = 0; idx1 < allSeries.get(0).getInnerSeriesList().size(); idx1++) {
			SurveySeries series = allSeries.get(0).getInnerSeries(idx1);

			logger.logMessage("Searching for equivalent stations in series " + (idx1 + 1) + " of " +
					allSeries.get(0).getInnerSeriesList().size() + "...");

			// Loop through all station names in series
			for (int idxLeg = 0; idxLeg < series.legCount(); idxLeg++) {
				for (int idxStn = 0; idxStn < 2; idxStn++) {
					String stn1Name = "";
					if (idxStn == 0) {
						stn1Name = series.getLegRaw(idxLeg).getFromStn().getName();
					} else {
						stn1Name = series.getLegRaw(idxLeg).getToStn().getName();
					}

					// Check for matching station name in all following series
					for (int idx2 = idx1 + 1; idx2 < allSeries.get(0).getInnerSeriesList().size(); idx2++) {
						SurveySeries series2 = allSeries.get(0).getInnerSeries(idx2);
						// Loop through all station names in series
						for (int idxLeg2 = 0; idxLeg2 < series2.legCount(); idxLeg2++) {
							for (int idxStn2 = 0; idxStn2 < 2; idxStn2++) {
								String stn2Name = "";
								if (idxStn2 == 0) {
									stn2Name = series2.getLegRaw(idxLeg2).getFromStn().getName();
								} else {
									stn2Name = series2.getLegRaw(idxLeg2).getToStn().getName();
								}

								// Check for matching station name in all following series
								if (stn1Name.compareTo(stn2Name) == 0) {
									// Matching stations found
									String series1Name = allSeries.get(0).getSeriesName() + "."
											+ series.getSeriesName();
									String series2Name = allSeries.get(0).getSeriesName() + "."
											+ series2.getSeriesName();

									// Check if equate already added
									boolean foundNewEquate = true;
									for (int idxEquates = 0; idxEquates < equates.size(); idxEquates++) {
										Equate testEquate = equates.get(idxEquates);
										if ((series1Name.compareTo(testEquate.getSeries1()) == 0)
												&& (series2Name.compareTo(testEquate.getSeries2()) == 0)
												&& (stn1Name.compareTo(testEquate.getStn1()) == 0)
												&& (stn2Name.compareTo(testEquate.getStn2()) == 0)) {
											foundNewEquate = false;
										} else if ((series1Name.compareTo(testEquate.getSeries2()) == 0)
												&& (series2Name.compareTo(testEquate.getSeries1()) == 0)
												&& (stn1Name.compareTo(testEquate.getStn2()) == 0)
												&& (stn2Name.compareTo(testEquate.getStn1()) == 0)) {
											foundNewEquate = false;
										}
									}

									if (foundNewEquate) {
										// Create and add equate
										Equate newEquate = new Equate(series1Name, stn1Name, series2Name, stn2Name);
										equates.add(newEquate);
									}
								}
							}
						}
					}
				}
			}
		}

		// Process equates
		UtilityFunctions.processEquates(equates, allSeries);

		// Debug dump
		UtilityFunctions.logSurveyDebugData(allSeries, logger);

		// Completed file parsing
		return allSeries;
	}

}
