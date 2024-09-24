/**
 * Copyright (C) 2023 Paul Fretwell - https://github.com/Footleg/caveconverter
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

import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.Equate;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.data.model.SurveyStation;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Parser for PocketTopo Text export files.
 * 
 * @author Footleg
 * @version 2024.09.24 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 */
public class PocketTopoParser {
	private Logger logger;

	// Constants used to indicate what type of data is encoded on a data line in the
	// file being parsed
	private final int LINE_TYPE_LEG = 1;
	private final int LINE_TYPE_SPLAY = 2;
	private final int LINE_TYPE_EQUATE = 3;
	private final int LINE_TYPE_COMMENT = 4;

	/**
	 * Class constructor
	 * 
	 * @param logger Logging class to output information, warning and error messages
	 *               to
	 */
	public PocketTopoParser(Logger logger) {
		super();
		this.logger = logger;
	}

	/**
	 * Class to hold data parsed from a line in a PocketTopo exported text data file
	 */
	private class TopoDataLine {
		// start stepping through the array from the beginning
		private String fromStn = "";
		private String toStn = "";
		private double tape = 0;
		private double compass = 0;
		private double clino = 0;
		private String trip = "";
		private String comment = "";
		private int lineType = -1;

		public String getFromStn() {
			return fromStn;
		}

		public void setFromStn(String fromStn) {
			this.fromStn = fromStn;
		}

		public String getToStn() {
			return toStn;
		}

		public void setToStn(String toStn) {
			this.toStn = toStn;
		}

		public double getTape() {
			return tape;
		}

		public void setTape(double tape) {
			this.tape = tape;
		}

		public double getCompass() {
			return compass;
		}

		public void setCompass(double compass) {
			this.compass = compass;
		}

		public double getClino() {
			return clino;
		}

		public void setClino(double clino) {
			this.clino = clino;
		}

		public String getTrip() {
			return trip;
		}

		public void setTrip(String trip) {
			this.trip = trip;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public int getLineType() {
			return lineType;
		}

		public void setLineType(int lineType) {
			this.lineType = lineType;
		}
	}

	/**
	 * Parse PocketTopo exported text data file into the cave data model
	 * 
	 * @param surveyFileData ListArray of data lines from a PocketTopo text file
	 * @return Cave Survey object
	 * @throws ParseException Exception raised when information in a survey data
	 *                        file is not supported or valid for the format
	 */
	public CaveSurvey parseFile(List<String> surveyFileData) throws ParseException {

		// Create new list of survey series to hold data
		CaveSurvey surveyData = new CaveSurvey(logger);
		SurveySeries outerSeries = null;

		String caveName = "";

		// Create cache for trip settings (contains list of arrays each
		// containing 3 strings for the id, date and magnetic declination)
		List<String[]> trips = new ArrayList<String[]>();

		// Create list for equates
		List<Equate> equates = new ArrayList<Equate>();

		// Initialise read state
		int state = 0;

		// Create arrays to cache shots while processing each leg
		List<SurveyLeg> legShots = new ArrayList<SurveyLeg>();
		List<SurveyLeg> splayShots = new ArrayList<SurveyLeg>();

		// Cache vars for last leg processed
		String lastShotStart = "";
		int lastToStn = -1;
		int lastSeriesNo = -1;
		String lastTripCode = "";
		boolean newLeg = false; // Gets set to true when a new leg is started
		// Create vars for active series refs
		SurveySeries series = null;
		int activeSeries = -1;

		// Loop through all data lines
		for (int lineIdx = 0; lineIdx <= surveyFileData.size(); lineIdx++) {
			int lineNo = lineIdx + 1;

			// Need to allow final pass through loop after all lines processed
			String dataLine = null;
			if (lineIdx < surveyFileData.size()) {
				dataLine = surveyFileData.get(lineIdx);
			}

			// Skip blank lines but not null line as this indicates end of file
			// and data cached from previous lines needs to be output
			if ((dataLine == null) || (dataLine.length() > 0)) {
				// Process data
				switch (state) {
					case 0: // Header line
						// My Cave (m, 360)
						// Check for triple space separator with bracket
						int pos = dataLine.indexOf("   (");
						if (pos > 0) {
							String part1 = dataLine.substring(0, pos);
							String part2 = dataLine.substring(pos + 3);
							if (part2.compareTo("(m, 360)") != 0) {
								// Not a valid header
								ParseException ex = new ParseException(
										"Invalid file header. Unsupported units: " + part2, lineNo);
								throw ex;
							} else {
								// Take cave name from header, and create a new series for the cave
								caveName = part1.replace(' ', '_');
								outerSeries = new SurveySeries(caveName);
								surveyData.add(outerSeries);
								logger.logMessage("Cave name: " + caveName);
							}
						}

						state = 1;
						break;
					case 1: // Trip settings in Header
						if (dataLine.substring(0, 1).compareTo("[") == 0) {
							// Trip settings
							List<String> tripData = UtilityFunctions.parseDataStringIntoDataItems(dataLine);
							// Copy into string array as rest of code uses a simple string array to store
							// trip details
							String[] trip = new String[tripData.size()];
							if (tripData.size() >= 3) {
								if (tripData.size() <= 4) {
									// Write first 3 items into trip data record
									for (int i = 0; i < tripData.size(); i++) {
										trip[i] = tripData.get(i);
									}
								} else {
									// Invalid trip line (more than 4 items)
									ParseException ex = new ParseException(
											"Invalid file header. Trip data contain more than 4 items in line: "
													+ dataLine,
											lineNo);
									throw ex;
								}
							} else {
								// Invalid trip line (less than 3 items)
								ParseException ex = new ParseException(
										"Invalid file header. Trip data does not contain at least 3 items in line: "
												+ dataLine,
										lineNo);
								throw ex;
							}
							// Remove colon from trip code
							if (trip[0].charAt(trip[0].length() - 1) == ':') {
								trip[0] = trip[0].substring(0, trip[0].length() - 1);
							} else {
								// Invalid trip code
								ParseException ex = new ParseException("Invalid file header. Unexpected trip code: "
										+ trip[0] + " in line: " + dataLine, lineNo);
								throw ex;
							}
							trips.add(trip);
							logger.logMessage("Trip settings: " + trip[0] + " " + trip[1] + " " + trip[2]);
							break;
						} else {
							// Start of legs, so don't break, just update state
							state = 2;
						}
					case 2: // Data lines
						// Create temporary survey leg to parse the data line into
						SurveyLeg shot = new SurveyLeg();
						int seriesNo = -1;
						String trip = "";
						TopoDataLine data = null;
						// Process data line if not at end of file
						if (dataLine != null) {
							// logger.logMessage("Data line " + CaveConverter.padNumber(lineNo, 4) + ": " +
							// dataLine);
							// Parse items into Topoline object
							data = parseDataLine(UtilityFunctions.parseDataStringIntoDataItems(dataLine));
							// Skip blank or all whitespace lines
							if (data.getLineType() > 0) {
								// Add fields in common to all types of data line
								int[] seriesStn = splitSeriesFromStn(data.getFromStn());
								seriesNo = seriesStn[0];
								shot.setFromStn(new SurveyStation(seriesStn[1]));
								shot.setLength(data.getTape(), LengthUnit.Metres);
								shot.setCompass(data.getCompass(), BearingUnit.Degrees);
								shot.setClino(data.getClino(), GradientUnit.Degrees);
								shot.setComment(data.getComment());
								trip = data.getTrip();
								if ((data.getLineType() == LINE_TYPE_LEG) || (data.getLineType() == LINE_TYPE_EQUATE)) {
									// Survey Leg or equate also have a To Stn
									int[] seriesStn2 = splitSeriesFromStn(data.getToStn());
									shot.setToStn(new SurveyStation(seriesStn2[1]));

									// Check whether both stations are in same series
									if (seriesNo != seriesStn2[0]) {
										if (data.getLineType() != LINE_TYPE_EQUATE) {
											// Leg changes series. Only allowed for equates.
											ParseException ex = new ParseException(
													"Legs linking different series must be zero length.", lineNo);
											throw ex;
										} else {
											// Series equate
											Equate equate = new Equate(outerSeries.getSeriesName() + "." + seriesNo,
													shot.getFromStn().getName(),
													outerSeries.getSeriesName() + "." + seriesStn2[0],
													shot.getToStn().getName());
											// Add to cache
											equates.add(equate);
										}
									} else if (data.getLineType() == LINE_TYPE_EQUATE) {
										// Equate within series
										Equate equate = new Equate(outerSeries.getSeriesName() + "." + seriesNo,
												shot.getFromStn().getName(),
												outerSeries.getSeriesName() + "." + seriesStn2[0],
												shot.getToStn().getName());
										// Add to cache
										equates.add(equate);
									}

								} else {
									// Splays do not have a to station, but the code relies on their being
									// a dummy station, so create one with id = -1
									shot.setToStn(new SurveyStation(-1));
								}
							}
						} else {
							// No more data, so just cached data to output from previous lines
							// Initialise leg to dummy data so conditions below enable cached data to be
							// processed
							data = parseDataLine(UtilityFunctions
									.parseDataStringIntoDataItems("DUMMY      DUMMY      42.0   0.0   0.0  [1]"));
							shot.setLength(data.getTape(), LengthUnit.Metres); // Set dummy value so cached legs get
																				// processed
						}
						// Process shot if not an equate
						if (shot.getLength(LengthUnit.Metres) > 0) {
							/**
							 * Check if this data is part of same leg already being processed.
							 * If the start station is different to the last one, or there is
							 * a to station which is different to the last to station then it
							 * is a new leg.
							 */
							if (lastShotStart.length() == 0) {
								// First shot in file, so store start station
								lastShotStart = data.getFromStn();
							} else if (data.getFromStn().compareTo(lastShotStart) != 0) {
								// Start of new leg (start stn is different to last data line)
								newLeg = true;
							} else if ((shot.getToStn().getId() != -1)
									&& ((lastToStn != -1))
									&& (shot.getToStn().getId() != lastToStn)) {
								// Start of new leg (end stn is different to to stn on last data line where last
								// data line had a to stn)
								newLeg = true;
							} else if ((lastToStn != -1) && (shot.getToStn().getId() == -1)) {
								// New leg (no to stn on current data line, but last line had one)
								newLeg = true;
							}

							if (newLeg) {
								// Starting new leg, so process data for last one before
								// storing this data

								// Create new leg to store store processed data
								SurveyLeg masterLeg = new SurveyLeg();

								if (legShots.size() > 0) {
									// Set stations using first leg
									masterLeg.setFromStn(legShots.get(0).getFromStn());
									masterLeg.setToStn(legShots.get(0).getToStn());

									// Calculate averages for leg shots
									double tape = 0.0;
									double clino = 0.0;
									String comments = "";
									// Use utility function to average bearings so values either side of north
									// average correctly
									double[] bearings = new double[legShots.size()];
									for (int i = 0; i < legShots.size(); i++) {
										tape += legShots.get(i).getLength(LengthUnit.Metres);
										bearings[i] = legShots.get(i).getCompass(BearingUnit.Degrees);
										clino += legShots.get(i).getClino(GradientUnit.Degrees);
										// Append comments from multiple shots into one comment
										if (legShots.get(i).getComment().length() > 0) {
											if (comments.length() > 0) {
												comments += "; " + legShots.get(i).getComment();
											} else {
												comments = legShots.get(i).getComment();
											}
										}

									}

									double aveCompass = UtilityFunctions.averageCompassBearings(bearings);
									// Update master leg
									masterLeg.setLength(tape / legShots.size(), LengthUnit.Metres);
									masterLeg.setCompass(aveCompass, BearingUnit.Degrees);
									masterLeg.setClino(clino / legShots.size(), GradientUnit.Degrees);
									masterLeg.setComment(comments);
								}

								// Determine if this leg belongs to the active series
								if (activeSeries != lastSeriesNo) {
									// Different series, see if this series already exists
									boolean gotSeries = false;
									for (int j = 0; j < outerSeries.innerSeriesCount(); j++) {
										// Check for matching series
										if (outerSeries.getInnerSeries(j).getSeriesName()
												.compareTo("" + lastSeriesNo) == 0) {
											// Found match, so get reference to this one
											series = outerSeries.getInnerSeries(j);
											gotSeries = true;
											// Exit loop
											j = outerSeries.innerSeriesCount();
										}
									}

									if (gotSeries == false) {
										// No series found, so need to create one
										series = new SurveySeries("" + lastSeriesNo);
										// Find compass calibration for this new series
										for (int j = 0; j < trips.size(); j++) {
											if (lastTripCode.compareTo(trips.get(j)[0]) == 0) {
												// Use calibration for this trip
												series.setDeclination(Double.valueOf(trips.get(j)[2]));
												// Set date for trip (will be string of format yyyy/mm/dd)
												String tripDateString = trips.get(j)[1];
												Date tripDate = UtilityFunctions.stringToDate(tripDateString,
														"yyyy/MM/dd");
												series.setSurveyDate(tripDate);
												// Add comment to series if a trip comment exists
												if (trips.get(j).length == 4) {
													series.setComment(trips.get(j)[3]);
												}
											}
										}
										// Add new series to list
										outerSeries.addSeries(series);
									}
									// Set active series number to match new active series
									activeSeries = lastSeriesNo;
								}

								// Process Splays for this leg
								int stnSplayCount = 0;
								String lastSplayFromStn = "";
								for (int i = 0; i < splayShots.size(); i++) {
									// Need to increment splay count for number of splays already added to series
									// from this station
									// unless we already know this due to last splay being from the same station
									if ((i == 0) || (lastSplayFromStn
											.equals(splayShots.get(i).getFromStn().getName()) == false)) {
										// Count previous splays matching this fromStn in series
										stnSplayCount = 0;
										for (int legIdx = 0; legIdx < series.legCount(); legIdx++) {
											SurveyLeg chkLeg = series.getLegRaw(legIdx);
											if ((chkLeg.isSplay())
													&& (chkLeg.getFromStn().getName()
															.equals(splayShots.get(i).getFromStn().getName()))) {
												stnSplayCount++;
											}
										}
									}
									// Add shot as a splay
									stnSplayCount++;
									addSplayShot(series, splayShots.get(i), stnSplayCount);
								}

								// Add master leg to series unless a dummy leg (will have negative length)
								if (masterLeg.getLength(LengthUnit.Metres) >= 0) {
									series.addLeg(masterLeg);
								}

								// Clear caches
								legShots = new ArrayList<SurveyLeg>();
								splayShots = new ArrayList<SurveyLeg>();

								// Update stn names for this new leg
								lastShotStart = data.getFromStn();

								// Reset flags
								newLeg = false;
							}

							// Process current leg unless a dummy end of file line
							if (dataLine != null) {
								// Determine whether a splay shot or a leg shot
								if (shot.getToStn().getId() == -1) {
									// Passage dimension shot
									splayShots.add(shot);
								} else {
									// Survey Leg, add to list
									legShots.add(shot);
								}

								// Cache series no for this leg to be used next loop
								lastSeriesNo = seriesNo;
								lastTripCode = trip;

								// Store to stn for comparison on next pass
								lastToStn = shot.getToStn().getId();
							}
						}

						break;
					default:
				}
			}
		}

		// Process equates
		UtilityFunctions.processEquates(equates, surveyData);

		// Debug dump
		UtilityFunctions.logSurveyDebugData(surveyData, logger);

		// Completed file parsing
		// logger.logMessage("Processed " + legCount + " survey legs in " +
		// surveyData.size() + " series.");
		return surveyData;
	}

	/**
	 * Splits a series.station name string into the component integers
	 * 
	 * @param dataIn String consisting of a numeric series name and numeric station
	 *               name separated with a dot
	 * @return An array of two integers, the series number and the station number
	 */
	private int[] splitSeriesFromStn(String dataIn) {
		// Split a series.station into the component integers
		int[] result = new int[2];

		int pos = dataIn.indexOf('.');
		if (pos > 0) {
			result[0] = Integer.valueOf(dataIn.substring(0, pos));
			result[1] = Integer.valueOf(dataIn.substring(pos + 1));
		} else {
			// Invalid series+station name
			RuntimeException ex = new RuntimeException("Invalid dot separated series and station name : " + dataIn);
			throw ex;
		}

		return result;
	}

	/**
	 * Determines what type of data a line of the file is representing
	 */
	private TopoDataLine parseDataLine(List<String> dataItems) {
		// Equate lines do not always have trip numbers ( [1] )
		// Lines may be and equate with an optional trip and optional comment
		// e.g. 1.0 1.1 0.000 0.00 0.00
		// e.g. 1.10 3.0 0.000 0.00 0.00 [1]
		// e.g. 1.0 1.1 0.000 0.00 0.00 "Equate with a comment."
		// e.g. 1.10 3.0 0.000 0.00 0.00 [1] "Equate with a comment."

		// Lines can define a station with no other data, or just a comment
		// e.g. 1.0 0.00 0.00 0.00
		// e.g. 1.0 0.000 0.00 0.00 "Just a comment."

		// Lines can represent legs, with or without comments
		// e.g. 1.1 1.2 2.050 260.84 -56.40 [1]
		// e.g. 1.0 1.2 5.234 3.18 -5.07 [1] "1.2=rawl plug on rear wall of blockhouse "

		// Lines can represent splay shots, with or without comments
		// e.g. 1.1 0.670 344.92 4.15 [1]
		// e.g. 1.1 0.670 344.92 4.15 [1] "Splay with a comment."

		TopoDataLine result = new TopoDataLine();

		// Check line is valid (has at least 4 items)
		if (dataItems.size() > 4) {
			// There will be either 4 or 5 data items in any line, followed by optional trip
			// and comment items
			int tripIdx = -1;
			int commentIdx = -1;

			String lastItem = dataItems.get(dataItems.size() - 1);
			String lastButOneItem = dataItems.get(dataItems.size() - 2);

			// Check if last item is a trip
			if (itemIsTrip(lastItem)) {
				// Last item is trip
				tripIdx = dataItems.size() - 1;
			} else if (itemIsTrip(lastButOneItem)) {
				// Last item is trip
				tripIdx = dataItems.size() - 2;
				// Last item in line has to be a comment
				commentIdx = dataItems.size() - 1;
			} else {
				// No trip, but is last item a comment?
				if (dataItems.size() > 5) {
					// Last item in line has to be a comment
					commentIdx = dataItems.size() - 1;
				} else if (dataItems.size() == 5) {
					// Last item in line is either a comment or a zero value
					if (itemIsZeroValue(lastItem) == false) {
						// Last item in line has to be a comment
						commentIdx = dataItems.size() - 1;
					}
				}
			}

			// All items have a from station
			result.setFromStn(dataItems.get(0));

			if ((tripIdx == 4) || (commentIdx == 4)) {
				// If trip or comment idx=4 then line is a splay leg
				result.setTape(Double.valueOf(dataItems.get(1)));
				result.setCompass(Double.valueOf(dataItems.get(2)));
				result.setClino(Double.valueOf(dataItems.get(3)));
				// Check whether line is really a splay, null data, or just a comment
				if ((result.getTape() == 0.0)
						&& (result.getCompass() == 0.0)
						&& (result.getClino() == 0.0)) {
					if (commentIdx > 0) {
						result.setLineType(LINE_TYPE_COMMENT);
					} else {
						// No data on line apart from from stn. Null line.
						result.setLineType(0);
					}
				} else {
					result.setLineType(LINE_TYPE_SPLAY);
				}
			} else if ((tripIdx == 5) || (commentIdx == 5) || ((tripIdx == -1) && (commentIdx == -1))) {
				// If trip or comment idx=5, or there is neither a trip or comment then line is
				// a survey leg
				// logger.logMessage(dataItems.size() + ";" + tripIdx + ";" + commentIdx + ";" +
				// dataItems.get(0) + ";" + dataItems.get(1) + ";" + dataItems.get(2) + ";" +
				// dataItems.get(3) + ";" + dataItems.get(4) + ";" + dataItems.get(5));
				result.setToStn(dataItems.get(1));
				result.setTape(Double.valueOf(dataItems.get(2)));
				result.setCompass(Double.valueOf(dataItems.get(3)));
				result.setClino(Double.valueOf(dataItems.get(4)));
				// Check whether line is an equate or a leg
				if ((result.getTape() == 0.0)
						&& (result.getCompass() == 0.0)
						&& (result.getClino() == 0.0)) {
					result.setLineType(LINE_TYPE_EQUATE);
				} else {
					result.setLineType(LINE_TYPE_LEG);
				}
			}

			// Add trip and comment if present
			if (tripIdx > 0) {
				result.setTrip(dataItems.get(tripIdx));
			}
			if (commentIdx > 0) {
				result.setComment(dataItems.get(commentIdx));
			}

		}

		return result;
	}

	private boolean itemIsTrip(String dataItem) {
		boolean isTrip = false;
		if ((dataItem.length() > 2)
				&& (dataItem.charAt(0) == '[') && (dataItem.charAt(dataItem.length() - 1) == ']')) {
			// Found trip item
			isTrip = true;
		}
		return isTrip;
	}

	private boolean itemIsZeroValue(String dataItem) {
		boolean isZero = false;

		// Check items starts with '0.0'
		if (dataItem.length() > 2) {
			if ((dataItem.charAt(0) == '0') && (dataItem.charAt(1) == '.') && (dataItem.charAt(2) == '0')) {
				// Potentially a zero value string
				isZero = true;
				// Loop through remaining characters checking they are all zeros
				for (int i = 3; i < dataItem.length(); i++) {
					if (dataItem.charAt(i) != '0') {
						// Item is not a zero value
						isZero = false;
						i = dataItem.length();
					}
				}
			}
		} else {
			// Allow '0' and '00'
			// Potentially a zero value string
			isZero = true;
			// Loop through remaining characters checking they are all zeros
			for (int i = 0; i < dataItem.length(); i++) {
				if (dataItem.charAt(i) != '0') {
					// Item is not a zero value
					isZero = false;
					i = dataItem.length();
				}
			}
		}

		return isZero;
	}

	/*
	 * processSplaysArray
	 * 
	 * Processes an array of splay shots. Will return an LRUD dimension or
	 * private double processSplaysArray( SurveyLeg splays[], int bestIdx,
	 * SurveySeries series ) {
	 * for ( int i = 0; i < upShots.size(); i++ ) {
	 * if ( i == bestIdx ) {
	 * //Found best up shot
	 * masterLeg.setUp( upShots.get(i).getLength() );
	 * }
	 * else {
	 * //Other shots, add as splay
	 * SurveyLeg splayShot = new SurveyLeg();
	 * splayShot.setFromStn( upShots.get(i).getFromStn() );
	 * String toStn = upShots.get(i).getFromStn() + new Character(
	 * (char)stnLetterCode++ ).toString();
	 * splayShot.setToStn( CaveConverter.stnNameToNumber( toStn, series ) );
	 * splayShot.setLength( upShots.get(i).getLength() );
	 * splayShot.setCompass( upShots.get(i).getCompass() );
	 * splayShot.setClino( upShots.get(i).getClino() );
	 * splayShot.setSplay( true );
	 * series.addLeg( splayShot );
	 * legCount++;
	 * }
	 * }
	 * }
	 */

	private void addSplayShot(SurveySeries series, SurveyLeg shot, int splayCount) {
		// Generate station name suffix for splay (97 = letter 'a')
		int increment = splayCount - 1; // Zero based
		int stnLetterCode = 97; // 97 = letter 'a'
		String stnSuffix = "";
		if (splayCount < 27) {
			stnSuffix = new Character((char) (stnLetterCode + increment)).toString();
		} else {
			int primaryCount = (increment - 26) / 26;
			int secondaryCount = (increment % 26);
			stnSuffix = new Character((char) (stnLetterCode + primaryCount)).toString();
			stnSuffix += new Character((char) (stnLetterCode + secondaryCount)).toString();
		}

		// Add shot as a splay
		// TODO Stns names should be screened against legal character list, wherever it
		// is that they are set
		SurveyLeg splayShot = shot.clone();
		String toStn = shot.getFromStn().getName() + stnSuffix;
		splayShot.setToStn(UtilityFunctions.createStationFromNameForSeries(toStn, series));
		splayShot.setLength(shot.getLength(LengthUnit.Metres), LengthUnit.Metres);
		splayShot.setCompass(shot.getCompass(BearingUnit.Degrees), BearingUnit.Degrees);
		splayShot.setClino(shot.getClino(GradientUnit.Degrees), GradientUnit.Degrees);
		splayShot.setSplay(true);
		series.addLeg(splayShot);
	}

}
