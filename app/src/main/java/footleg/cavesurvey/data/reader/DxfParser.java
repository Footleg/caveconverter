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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.data.model.SurveyStation;
import footleg.cavesurvey.data.model.SurveyStation.FixType;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Parser for DXF format text data files.
 * 
 * @author Footleg
 * @version 2017.01.09 (ISO 8601 YYYY.MM.DD)
 * @since 1.6 (The Java version used)
 */
public class DxfParser {
	private Date seriesDate;
	private Logger logger;

	/**
	 * Class constructor
	 * 
	 * @param logger Logging class to output information, warning and error messages
	 *               to
	 */
	public DxfParser(Logger logger) {
		super();
		this.logger = logger;
	}

	/**
	 * Parse Autocad DXF file into the cave data model.
	 * 
	 * Converts polylines into series with fixed point at start.
	 * 
	 * @param surveyFileData ListArray of data lines from a DXF file
	 * @param parseMode      An enumeration to indicate whether to parse file for
	 *                       contour polylines, spot heights or survey legs
	 * @return Cave Survey object
	 * 
	 * @to.do
	 *        TODO Add option to round to nearest 10cm or 0.5 deg.
	 */
	public CaveSurvey parseFile(List<String> surveyFileData, int parseMode) {
		int legCount = 0;
		int iPolylineCount = 0;
		int iVertexCount = 0;
		int iPlainlineCount = 0;

		// Create new list of survey series to hold data
		CaveSurvey surveyData = new CaveSurvey(logger);
		SurveySeries outerSeries = null;

		// Create arrays to data while processing lines
		List<List<double[]>> allChains = new ArrayList<List<double[]>>();
		List<String[]> allChainLabels = new ArrayList<String[]>();
		List<double[]> arSurveyChain = new ArrayList<double[]>();
		List<double[]> arLines = new ArrayList<double[]>();
		List<double[]> arLabelPoints = new ArrayList<double[]>();
		List<String> arLabels = new ArrayList<String>();
		boolean mappedLabelsFound = false;

		// Define read state values
		final int stateParsingHeader = 0;
		final int stateFindPolyline = 1;
		final int stateFindVertex = 2;

		final int parseModeSurveyLegs = 0;
		final int parseModeSurfaceContours = 1;
		final int parseModeSpotHeights = 2;

		// Initialise read state
		int state = stateParsingHeader;

		// Local vars
		int iPointCount = 0;
		String sLineName = "";
		double dEastValue = 0;
		double dNorthValue = 0;
		double dElevValue = 0;
		double dLastEastValue = 0;
		double dLastNorthValue = 0;
		double dLastElevValue = 0;

		// Range bounds
		// TODO Promote these to parameters which can be specified as options
		int iNorthEdge = 999999999;
		int iSouthEdge = -999999999;
		int iEastEdge = 999999999;
		int iWestEdge = -999999999;
		int iMinElev = -999999999;

		// Write output file header
		logger.logMessage("Generating survey data from lines and polylines in DXF data file");
		outerSeries = new SurveySeries("SurveyFromDXF");

		// Loop through all data lines
		for (int i = 0; i < surveyFileData.size(); i++) {
			String dataLine = surveyFileData.get(i);

			// Proceed based on state
			switch (state) {
				case stateParsingHeader:
					// First look for line:
					// ENTITIES
					// This signifies start of data after header stuff which we ignore.
					if (dataLine.trim().compareToIgnoreCase("ENTITIES") == 0) {
						// Found end of header section, so change state to searching for new polyline
						state = stateFindPolyline;
					}
					break;
				case stateFindPolyline:
					if (dataLine.trim().compareToIgnoreCase("POLYLINE") == 0) {
						// Found polyline, indicated with header line:
						// POLYLINE
						// and ending with:
						// SEQEND

						// Check next 15 lines looking for AcDbEntity start
						int iSkipLoop = 1;
						do {
							dataLine = surveyFileData.get(++i);
						} while ((dataLine.trim().compareToIgnoreCase("AcDbEntity") != 0)
								|| (iSkipLoop > 15));
						// If found it then process
						if (dataLine.trim().compareToIgnoreCase("AcDbEntity") == 0) {
							// (skip 1 line)
							dataLine = surveyFileData.get(++i);
							// Read entity type from next line:
							dataLine = surveyFileData.get(++i);
							// if Strings.Left(Trim(dataLine), 4) = "1100" {
							// Found a contour line entity (they are all 1100x)
							// Increment line counter
							iPolylineCount += 1;
							// Start new section heading, removing spaces from station names
							sLineName = dataLine.replace(" ", "_");
							if (parseMode == parseModeSurfaceContours) {
								sLineName = sLineName + iPolylineCount;
							}
							// Set state to finding vertices
							state = stateFindVertex;
							// Reset point counter
							iPointCount = 0;
							// }
						}
					}
					// loop through all lines, each indicated with header line:
					// LINE
					// and ending after a fixed number of lines
					else if (dataLine.trim().compareToIgnoreCase("LINE") == 0) {
						// Found line
						iPlainlineCount++;
						// Check next 4 lines looking for AcDbEntity start
						int iSkipLoop = 0;
						do {
							dataLine = surveyFileData.get(++i);
							iSkipLoop++;
						} while ((dataLine.trim().compareToIgnoreCase("AcDbEntity") != 0
								&& dataLine.trim().compareToIgnoreCase("CentreLine") != 0)
								&& (iSkipLoop < 4));
						// Allow anything if 4 lines read
						boolean allowAnyLineName = false;
						if (iSkipLoop == 4) {
							allowAnyLineName = true;
						}
						// If found it then process
						if (dataLine.trim().compareToIgnoreCase("AcDbEntity") == 0
								|| dataLine.trim().compareToIgnoreCase("CentreLine") == 0
								|| allowAnyLineName == true) {
							if (dataLine.trim().compareToIgnoreCase("CentreLine") != 0 && allowAnyLineName == false) {
								// Check next 15 lines looking for AcDbLine start
								iSkipLoop = 1;
								do {
									dataLine = surveyFileData.get(++i);
								} while ((dataLine.trim().compareToIgnoreCase("AcDbLine") != 0)
										|| (iSkipLoop > 15));
							}
							// If found it then process
							if (dataLine.trim().compareToIgnoreCase("AcDbLine") == 0
									|| (dataLine.trim().compareToIgnoreCase("CentreLine") == 0)
									|| allowAnyLineName) {
								// Check for next line:
								// 10:
								dataLine = surveyFileData.get(++i);
								if (dataLine.trim().compareToIgnoreCase("10") == 0) {
									// Read Easting
									dataLine = surveyFileData.get(++i);
									dEastValue = roundedDataValue(dataLine);
									// Check for next line:
									// 20:
									dataLine = surveyFileData.get(++i);
									if (dataLine.trim().compareToIgnoreCase("20") == 0) {
										// Read Northing
										dataLine = surveyFileData.get(++i);
										dNorthValue = roundedDataValue(dataLine);
										// Check for next line:
										// 30:
										dataLine = surveyFileData.get(++i);
										if (dataLine.trim().compareToIgnoreCase("30") == 0) {
											// Read Elevation
											dataLine = surveyFileData.get(++i);
											dElevValue = roundedDataValue(dataLine);
											// Check for next line:
											// 11:
											dataLine = surveyFileData.get(++i);
											if (dataLine.trim().compareToIgnoreCase("11") == 0) {
												// Read Easting
												dataLine = surveyFileData.get(++i);
												dLastEastValue = roundedDataValue(dataLine);
												// Check for next line:
												// 21:
												dataLine = surveyFileData.get(++i);
												if (dataLine.trim().compareToIgnoreCase("21") == 0) {
													// Read Northing
													dataLine = surveyFileData.get(++i);
													dLastNorthValue = roundedDataValue(dataLine);
													// Check for next line:
													// 31:
													dataLine = surveyFileData.get(++i);
													if (dataLine.trim().compareToIgnoreCase("31") == 0) {
														// Read Elevation
														dataLine = surveyFileData.get(++i);
														dLastElevValue = roundedDataValue(dataLine);
														// Now generate data to output
														if ((dEastValue >= iWestEdge)
																&& (dEastValue <= iEastEdge)
																&& (dNorthValue <= iNorthEdge)
																&& (dNorthValue >= iSouthEdge)
																&& (dElevValue >= iMinElev)
																&& (dLastEastValue >= iWestEdge)
																&& (dLastEastValue <= iEastEdge)
																&& (dLastNorthValue <= iNorthEdge)
																&& (dLastNorthValue >= iSouthEdge)
																&& (dLastElevValue >= iMinElev)) {
															// Increment point counter
															iPointCount += 1;

															if (parseMode == parseModeSurveyLegs) {
																// Add points to line array
																double[] point = new double[6];
																point[0] = dEastValue;
																point[1] = dNorthValue;
																point[2] = dElevValue;
																point[3] = dLastEastValue;
																point[4] = dLastNorthValue;
																point[5] = dLastElevValue;
																arLines.add(point);
															}
														} else {
															// Record outside specified area
															dEastValue = dNorthValue;
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
					// loop through all text items, each indicated with header line:
					// TEXT
					// and ending after a fixed number of lines
					else if (dataLine.trim().compareToIgnoreCase("TEXT") == 0) {
						// Found TEXT item
						iPlainlineCount++;
						// Skip next line then check for Label start
						dataLine = surveyFileData.get(++i);
						dataLine = surveyFileData.get(++i);
						if (dataLine.trim().compareToIgnoreCase("Labels") == 0) {
							// Found label, so read position and name from following lines

							// Check for next line:
							// 10:
							dataLine = surveyFileData.get(++i);
							if (dataLine.trim().compareToIgnoreCase("10") == 0) {
								// Read Easting
								dataLine = surveyFileData.get(++i);
								dEastValue = roundedDataValue(dataLine);
								// Check for next line:
								// 20:
								dataLine = surveyFileData.get(++i);
								if (dataLine.trim().compareToIgnoreCase("20") == 0) {
									// Read Northing
									dataLine = surveyFileData.get(++i);
									dNorthValue = roundedDataValue(dataLine);
									// Check for next line:
									// 30:
									dataLine = surveyFileData.get(++i);
									if (dataLine.trim().compareToIgnoreCase("30") == 0) {
										// Read Elevation
										dataLine = surveyFileData.get(++i);
										dElevValue = roundedDataValue(dataLine);
										// Skip 3 lines
										for (int iskip = 0; iskip < 3; iskip++) {
											dataLine = surveyFileData.get(++i);
										}
										// Next line should be station name
										String possibleStnName = surveyFileData.get(++i);
										// skip a line
										dataLine = surveyFileData.get(++i);
										// Check for next line:
										// POINT:
										dataLine = surveyFileData.get(++i);
										if (dataLine.trim().compareToIgnoreCase("POINT") == 0) {
											// skip a line
											dataLine = surveyFileData.get(++i);
											// Check for next line:
											// Stations:
											dataLine = surveyFileData.get(++i);
											if (dataLine.trim().compareToIgnoreCase("Stations") == 0) {
												// Check for next line:
												// 10:
												dataLine = surveyFileData.get(++i);
												if (dataLine.trim().compareToIgnoreCase("10") == 0) {
													// Read Easting
													dataLine = surveyFileData.get(++i);
													dLastEastValue = roundedDataValue(dataLine);
													// Check for next line:
													// 20:
													dataLine = surveyFileData.get(++i);
													if (dataLine.trim().compareToIgnoreCase("20") == 0) {
														// Read Northing
														dataLine = surveyFileData.get(++i);
														dLastNorthValue = roundedDataValue(dataLine);
														// Check for next line:
														// 30:
														dataLine = surveyFileData.get(++i);
														if (dataLine.trim().compareToIgnoreCase("30") == 0) {
															// Read Elevation
															dataLine = surveyFileData.get(++i);
															dLastElevValue = roundedDataValue(dataLine);
															// Now generate data to output
															if ((dEastValue >= iWestEdge)
																	&& (dEastValue <= iEastEdge)
																	&& (dNorthValue <= iNorthEdge)
																	&& (dNorthValue >= iSouthEdge)
																	&& (dElevValue >= iMinElev)
																	&& (dLastEastValue >= iWestEdge)
																	&& (dLastEastValue <= iEastEdge)
																	&& (dLastNorthValue <= iNorthEdge)
																	&& (dLastNorthValue >= iSouthEdge)
																	&& (dLastElevValue >= iMinElev)) {
																// Check station point and label point matched
																if ((dEastValue == dLastEastValue)
																		&& (dNorthValue == dLastNorthValue)
																		&& (dElevValue == dLastElevValue)) {

																	// Station label identified
																	if (parseMode == parseModeSurveyLegs) {
																		// Add label and point to lists
																		double[] point = new double[3];
																		point[0] = dEastValue;
																		point[1] = dNorthValue;
																		point[2] = dElevValue;
																		arLabels.add(possibleStnName);
																		arLabelPoints.add(point);
																	}
																}
															} else {
																// Record outside specified area
																dEastValue = dNorthValue;
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
					break;
				case stateFindVertex:
					// Within each polyline, look for vertices indicated by lines:
					// VERTEX
					if (dataLine.trim().compareToIgnoreCase("VERTEX") == 0) {
						// Reset flag
						boolean bValidRecord = false;
						// Increment count
						iVertexCount += 1;
						// Check next 15 lines looking for AcDbEntity start
						int iSkipLoop = 1;
						do {
							dataLine = surveyFileData.get(++i);
						} while ((dataLine.trim().compareToIgnoreCase("AcDbEntity") != 0)
								|| (iSkipLoop > 15));
						if (dataLine.trim().compareToIgnoreCase("AcDbEntity") == 0) {
							// Check next 15 lines looking for AcDbVertex start
							iSkipLoop = 1;
							do {
								dataLine = surveyFileData.get(++i);
							} while ((dataLine.trim().compareToIgnoreCase("AcDbVertex") != 0)
									|| (iSkipLoop > 15));
							if (dataLine.trim().compareToIgnoreCase("AcDbVertex") == 0) {
								// Check next 15 lines looking for AcDb3dPolylineVertex start
								iSkipLoop = 1;
								do {
									dataLine = surveyFileData.get(++i);
								} while ((dataLine.trim().compareToIgnoreCase("AcDb3dPolylineVertex") != 0)
										|| (iSkipLoop > 15));
								if (dataLine.trim().compareToIgnoreCase("AcDb3dPolylineVertex") == 0) {
									// Check for next line:
									// 10:
									dataLine = surveyFileData.get(++i);
									if (dataLine.trim().compareToIgnoreCase("10") == 0) {
										// Read Easting
										dataLine = surveyFileData.get(++i);
										dEastValue = roundedDataValue(dataLine);
										// Check for next line:
										// 20:
										dataLine = surveyFileData.get(++i);
										if (dataLine.trim().compareToIgnoreCase("20") == 0) {
											// Read Northing
											dataLine = surveyFileData.get(++i);
											dNorthValue = roundedDataValue(dataLine);
											// Check for next line:
											// 30:
											dataLine = surveyFileData.get(++i);
											if (dataLine.trim().compareToIgnoreCase("30") == 0) {
												// Read Elevation
												dataLine = surveyFileData.get(++i);
												dElevValue = roundedDataValue(dataLine);
												// Now generate data to output
												if ((dEastValue >= iWestEdge)
														&& (dEastValue <= iEastEdge)
														&& (dNorthValue <= iNorthEdge)
														&& (dNorthValue >= iSouthEdge)
														&& (dElevValue >= iMinElev)) {
													// Increment point counter
													iPointCount += 1;

													if (parseMode == parseModeSurveyLegs) {
														// Add point to chain
														double[] point = new double[3];
														point[0] = dEastValue;
														point[1] = dNorthValue;
														point[2] = dElevValue;
														arSurveyChain.add(point);
													} else {
														// Generate station name
														String sStnName = "S" + iPolylineCount + "P" + iPointCount;
														// Reset station position string
														String sPointPosition = "";
														if (iPointCount == 1) {
															// First point, so fix it
															sPointPosition = "*fix " + sStnName + " " +
																	dEastValue + " " +
																	dNorthValue + " " +
																	dElevValue;
															if (parseMode == parseModeSurfaceContours) {
																// Write line header
																logger.logMessage("*begin " + sLineName);
															} else {
																// } point, so write differences for leg
																sPointPosition = "  " + (dEastValue - dLastEastValue)
																		+ " " +
																		(dNorthValue - dLastNorthValue) + " " +
																		(dElevValue - dLastElevValue);
															}
														}
														if (parseMode == parseModeSpotHeights) {
															logger.logMessage(
																	String.valueOf(Math.round(dEastValue) - iEastEdge));
															logger.logMessage(String.valueOf((iNorthEdge - iSouthEdge)
																	- (Math.round(dNorthValue) - iSouthEdge)));
															logger.logMessage(String.valueOf(dElevValue));
														} else {
															// Write position and station number
															logger.logMessage(sPointPosition);
															logger.logMessage(sStnName);
														}
													}
													// Update last positions
													dLastEastValue = dEastValue;
													dLastNorthValue = dNorthValue;
													dLastElevValue = dElevValue;
												} else {
													// Record outside specified area
													dEastValue = dNorthValue;
												}
												bValidRecord = true;
											}
										}
									}
								}
							}
						}
						// Check for invalid records
						if (bValidRecord == false) {
							// Invalid record
							logger.logMessage("Bad Line");
							// Set state back to searching for next vertex
							state = stateFindPolyline;
						}
					}
					// Find either next VERTEX or SEQEND
					else if (dataLine.trim().compareToIgnoreCase("SEQEND") == 0) {
						// End of polyline
						// Write final station name if any points were written
						if (parseMode == parseModeSurveyLegs) {
							// Check that line name is unique
							String sUniqueName = sLineName;
							int k = 1;
							boolean bUnique = false;
							while (bUnique == false) {
								// Search existing chains for matching name
								bUnique = true;
								for (int j = 0; j < outerSeries.innerSeriesCount(); j++) {
									// Check for matching series
									if (outerSeries.getInnerSeries(j).getSeriesName().compareTo(sUniqueName) == 0) {
										// Not unique, so add number to end
										sUniqueName = sLineName + k;
										k++;
										bUnique = false;
										// Exit loop
										j = outerSeries.innerSeriesCount();
									}
								}
							}

							// If no label points, then pass the unique name from the line in via the labels
							// array
							if (arLabelPoints.size() == 0) {
								arLabels.add(0, sUniqueName);
							}

							// Map points in chain to labels
							String[] stnLabels = mapLabelsToChainPoints(arSurveyChain, arLabelPoints, arLabels);

							// Add new series to list for this polyline
							outerSeries.addSeries(makeSeriesFromPolyline(arSurveyChain, sUniqueName, stnLabels));
							legCount += (arSurveyChain.size() - 1);

							// Store chain and labels for this chain for linking to other chains later
							allChains.add(arSurveyChain);
							allChainLabels.add(stnLabels);
							// Reset flag if station labels are null
							if (stnLabels[0] != null) {
								mappedLabelsFound = true;
							}

							// Reset series data array
							arSurveyChain = new ArrayList<double[]>();

						} else if (parseMode == parseModeSurfaceContours) {
							// End section if any points were in target area
							if (iPointCount > 0) {
								logger.logMessage("*end " + sLineName);
							}
						}
						// Set state back to searching for next vertex
						state = stateFindPolyline;
					}
					break;
				default:
			}
		}

		// Attempt to join up lines into connected polylines
		int newlines = 0;
		while (arLines.size() > 0) {
			// Get first line out
			double[] line = arLines.remove(0);
			// Start new polyline
			arSurveyChain = new ArrayList<double[]>();
			// Add points to chain
			double[] firstPoint = new double[3];
			firstPoint[0] = line[0];
			firstPoint[1] = line[1];
			firstPoint[2] = line[2];
			arSurveyChain.add(firstPoint);
			double[] lastPoint = new double[3];
			lastPoint[0] = line[3];
			lastPoint[1] = line[4];
			lastPoint[2] = line[5];
			arSurveyChain.add(lastPoint);

			// Search for adjoining lines
			boolean added = true;
			while (added == true) {
				added = false;
				for (int linesIdx = 0; linesIdx < arLines.size(); linesIdx++) {
					boolean match = false;
					line = arLines.get(linesIdx);
					if (firstPoint[0] == line[0]
							&& firstPoint[1] == line[1]
							&& firstPoint[2] == line[2]) {
						// Add new first point to start of polyline
						firstPoint = new double[3];
						firstPoint[0] = line[3];
						firstPoint[1] = line[4];
						firstPoint[2] = line[5];
						arSurveyChain.add(0, firstPoint);
						match = true;
					} else if (firstPoint[0] == line[3]
							&& firstPoint[1] == line[4]
							&& firstPoint[2] == line[5]) {
						// Add new first point to start of polyline
						firstPoint = new double[3];
						firstPoint[0] = line[0];
						firstPoint[1] = line[1];
						firstPoint[2] = line[2];
						arSurveyChain.add(0, firstPoint);
						match = true;
					} else if (lastPoint[0] == line[0]
							&& lastPoint[1] == line[1]
							&& lastPoint[2] == line[2]) {
						// Add new last point to end of polyline
						lastPoint = new double[3];
						lastPoint[0] = line[3];
						lastPoint[1] = line[4];
						lastPoint[2] = line[5];
						arSurveyChain.add(lastPoint);
						match = true;
					} else if (lastPoint[0] == line[3]
							&& lastPoint[1] == line[4]
							&& lastPoint[2] == line[5]) {
						// Add new last point to end of polyline
						lastPoint = new double[3];
						lastPoint[0] = line[0];
						lastPoint[1] = line[1];
						lastPoint[2] = line[2];
						arSurveyChain.add(lastPoint);
						match = true;
					}
					if (match == true) {
						// Remove line and decrement index so next item is picked up
						arLines.remove(linesIdx);
						linesIdx--;
						added = true;
					}
				}
			}

			// Map points in chain to labels
			String[] stnLabels = mapLabelsToChainPoints(arSurveyChain, arLabelPoints, arLabels);
			if (arLabels.size() > 0) {
				mappedLabelsFound = true;
			}

			// Add chain to array and create series
			newlines++;
			String sSeriesName = "SeriesFromLines" + newlines;
			SurveySeries newSeries = makeSeriesFromPolyline(arSurveyChain, sSeriesName, stnLabels);
			// Add series if it contains any legs (polylines where all points share the same
			// location do not generate any legs)
			if (newSeries.legCount() > 0) {
				allChains.add(arSurveyChain);
				allChainLabels.add(stnLabels);
				outerSeries.addSeries(newSeries);
				legCount += (arSurveyChain.size() - 1);
			}
		}

		if (mappedLabelsFound) {
			// DXF has mapped labels for all chains. Rebuild series based on full station
			// names.
			SurveySeries rebuiltOuterSeries = new SurveySeries("SurveyFromDXFExportedFrom3D");
			for (int seriesIdx = 0; seriesIdx < outerSeries.innerSeriesCount(); seriesIdx++) {
				// Get chain corresponding to this series
				List<double[]> srcChain = allChains.get(seriesIdx);
				// Search through all legs in the series and put each into a new series matching
				// the station name prefix
				SurveySeries inputSeries = outerSeries.getInnerSeries(seriesIdx);
				for (int legIdx = 0; legIdx < inputSeries.legCount(); legIdx++) {
					SurveyLeg inputLeg = inputSeries.getLegRaw(legIdx);

					// Find series for from stn in this leg
					String fromStnName = inputLeg.getFromStn().getName();

					int matchingFromSeries = findMatchingSeriesForStnName(rebuiltOuterSeries, fromStnName);
					if (matchingFromSeries > -1) {
						String toStnName = inputLeg.getToStn().getName();
						int matchingToSeries = findMatchingSeriesForStnName(rebuiltOuterSeries, toStnName);
						int legNewSeries = matchingFromSeries;
						if (matchingToSeries != matchingFromSeries) {
							// fromStn series does not match toStn series, so we need to determine which
							// series this leg belongs to
							boolean stnNameSwapped = false;
							// Check if toStn has more than one matching point in the all points array
							List<Integer> matchingToPoints = getListOfMatchingPoints(srcChain.get(legIdx + 1),
									arLabelPoints);
							if (matchingToPoints.size() > 1) {
								// Look for matching point label which does match the from station series
								String fromStnPrefix = getNamePrefix(fromStnName);
								if (fromStnPrefix.length() > 0) {
									String toStnPrefix = getNamePrefix(toStnName);
									if (toStnPrefix.length() > 0) {
										for (int matchingPointsIdx = 0; matchingPointsIdx < matchingToPoints
												.size(); matchingPointsIdx++) {
											String testLabel = arLabels.get(matchingToPoints.get(matchingPointsIdx));
											String testLabelPrefix = getNamePrefix(testLabel);
											if (testLabelPrefix.equals(fromStnPrefix)) {
												// Replace the toStnName with the matching point which does match the
												// from station prefix
												inputLeg.getToStn().setName(testLabel);
												// Add an equate for the alternate names being swapped for this stn
												SurveyStation linkStn1 = new SurveyStation(0);
												linkStn1.setName(getShortenedName(testLabel));
												SurveyStation linkStn2 = new SurveyStation(0);
												linkStn2.setName(getShortenedName(toStnName));
												// TODO Only add link if not already present
												rebuiltOuterSeries.addLink(
														rebuiltOuterSeries.getInnerSeries(matchingFromSeries)
																.getSeriesName(),
														linkStn1,
														rebuiltOuterSeries.getInnerSeries(matchingToSeries)
																.getSeriesName(),
														linkStn2);
												stnNameSwapped = true;
												break;
											}
										}
									}
								}
							}
							if (stnNameSwapped == false) {
								// No matching point for toStn was in the same series as the fromStn, so we need
								// to find
								// a matching label for the fromStn which is in the toStn series

								// Check if fromStn has more than one matching point in the all points array
								List<Integer> matchingFromPoints = getListOfMatchingPoints(srcChain.get(legIdx),
										arLabelPoints);
								if (matchingFromPoints.size() > 1) {
									// Look for matching point label which does match the from station series
									String fromStnPrefix = getNamePrefix(fromStnName);
									if (fromStnPrefix.length() > 0) {
										String toStnPrefix = getNamePrefix(toStnName);
										if (toStnPrefix.length() > 0) {
											for (int matchingPointsIdx = 0; matchingPointsIdx < matchingFromPoints
													.size(); matchingPointsIdx++) {
												String testLabel = arLabels
														.get(matchingFromPoints.get(matchingPointsIdx));
												String testLabelPrefix = getNamePrefix(testLabel);
												if (testLabelPrefix.equals(toStnPrefix)) {
													// Replace the fromStnName with the matching point which does match
													// the to station prefix
													inputLeg.getFromStn().setName(testLabel);
													// Add an equate for the alternate names being swapped for this stn
													SurveyStation linkStn1 = new SurveyStation(0);
													linkStn1.setName(getShortenedName(fromStnName));
													SurveyStation linkStn2 = new SurveyStation(0);
													linkStn2.setName(getShortenedName(testLabel));
													// TODO Only add link if not already present
													rebuiltOuterSeries.addLink(
															rebuiltOuterSeries.getInnerSeries(matchingFromSeries)
																	.getSeriesName(),
															linkStn1,
															rebuiltOuterSeries.getInnerSeries(matchingToSeries)
																	.getSeriesName(),
															linkStn2);
													legNewSeries = matchingToSeries;
													break;
												}
											}
										}
									}
								}

							}
						}

						// Remove the prefixes from both stations in each leg
						String shortenedFromName = getShortenedName(inputLeg.getFromStn().getName());
						String shortenedToName = getShortenedName(inputLeg.getToStn().getName());
						inputLeg.getFromStn().setName(shortenedFromName);
						inputLeg.getToStn().setName(shortenedToName);

						// Put leg into matching series in rebuilt survey after all changes to the
						// station names
						rebuiltOuterSeries.getInnerSeries(legNewSeries).addLeg(inputLeg);
					}
				}
			}

			// Replace original with rebuilt series set
			outerSeries = rebuiltOuterSeries;
		} else {
			// Search for connected stations for any station in each polyline
			for (int seriesIdx = 0; seriesIdx < outerSeries.innerSeriesCount(); seriesIdx++) {
				// Get chain corresponding to this series
				List<double[]> srcChain = allChains.get(seriesIdx);
				String[] srcChainLabels = allChainLabels.get(seriesIdx);
				for (int point1Idx = 0; point1Idx < srcChain.size(); point1Idx++) {
					// Get fixed position for this station in chain
					double fixX = srcChain.get(point1Idx)[0];
					double fixY = srcChain.get(point1Idx)[1];
					double fixZ = srcChain.get(point1Idx)[2];
					// Check all chains except self for a matching point
					for (int chainIdx = 0; chainIdx < allChains.size(); chainIdx++) {
						List<double[]> chain = allChains.get(chainIdx);
						String[] chainLabels = allChainLabels.get(chainIdx);
						for (int pointIdx = 0; pointIdx < chain.size(); pointIdx++) {
							if ((fixX == chain.get(pointIdx)[0])
									&& (fixY == chain.get(pointIdx)[1])
									&& (fixZ == chain.get(pointIdx)[2])) {
								// Found matching point
								boolean addEquate = true;
								if (chainIdx == seriesIdx) {
									// Matching point is in same series, so only add equate
									// when matching station occurs after test station to avoid adding
									// equate twice or equating stations onto themselves
									if (pointIdx <= point1Idx) {
										addEquate = false;
									}
								}
								if (addEquate == true) {
									// Replace fixed point in leg with equate
									SurveyStation linkStn1 = new SurveyStation(point1Idx);
									if (srcChainLabels[point1Idx] != null) {
										linkStn1.setName(srcChainLabels[point1Idx]);
									}
									SurveyStation linkStn2 = new SurveyStation(pointIdx);
									if (chainLabels[pointIdx] != null) {
										linkStn2.setName(chainLabels[pointIdx]);
									}
									outerSeries.addLink(outerSeries.getInnerSeries(seriesIdx).getSeriesName(), linkStn1,
											outerSeries.getInnerSeries(chainIdx).getSeriesName(), linkStn2);
								}
								// Clear fixed points apart from first one in first series
								if (seriesIdx > 0) {
									SurveyStation stn = outerSeries.getInnerSeries(seriesIdx).getLegRaw(0).getFromStn();
									// double easting = stn.getEasting();
									// double northing = stn.getNorthing();
									// double altitude = stn.getAltitude();
									// stn.setFixed(FixType.NONE, easting, northing, altitude);
									stn.clearFixedStn();
								}
							}
						}
					}
				}
			}
		}

		// Put generated data series into survey
		surveyData.add(outerSeries);

		// Debug dump
		UtilityFunctions.logSurveyDebugData(surveyData, logger);

		// Completed file parsing
		logger.logMessage("Processed " + legCount + " survey legs in " +
				outerSeries.innerSeriesCount() + " series.");
		logger.logMessage("Found:");
		logger.logMessage("Polylines: " + iPolylineCount + " containing " + iVertexCount + " line segments.");
		logger.logMessage("Lines: " + iPlainlineCount);
		logger.logMessage("Total line segments: " + (iPlainlineCount + iVertexCount));

		return surveyData;
	}

	/**
	 * Search for an inner series matching the name prefix on this station, and
	 * return it. If
	 * not found then create it and return it.
	 * 
	 * @param series  Series containing an inner series to match by name to the stn
	 *                name
	 * @param stnName Stn name to match
	 * @return Matching inner series index
	 */
	private int findMatchingSeriesForStnName(SurveySeries series, String stnName) {
		int matchingIdx = -1;

		// Determine prefix from station name
		String stnPrefix = getNamePrefix(stnName);
		if (stnPrefix.length() > 0) {
			for (int i = 0; i < series.innerSeriesCount(); i++) {
				String seriesName = series.getInnerSeries(i).getSeriesName();
				if (stnPrefix.equalsIgnoreCase(seriesName)) {
					matchingIdx = i;
					break;
				}
			}

			// If not match found then create a new series for this name
			if (matchingIdx == -1) {
				SurveySeries newSeries = new SurveySeries(stnPrefix);
				series.addSeries(newSeries);
				matchingIdx = series.innerSeriesCount() - 1;
			}
		}

		return matchingIdx;
	}

	/**
	 * Takes a survey chain array, label points array and station labels array, and
	 * generates an array
	 * of labels for all the points in the chain. The index of any point in the
	 * chain will be the same
	 * a the index of the corresponding label in the output array.
	 * 
	 * @param arSurveyChain Chain of points representing a survey chain (linear
	 *                      chain of legs)
	 * @param arLabelPoints Array of points which correspond the labels in the
	 *                      labels array
	 * @param stnLabels     Array of labels corresponding to the points in the label
	 *                      points array
	 * @return Array of labels corresponding to the points in the survey chain
	 */
	private String[] mapLabelsToChainPoints(List<double[]> arSurveyChain, List<double[]> arLabelPoints,
			List<String> stnLabels) {

		// Create an array of labels and set them to the matching points
		String[] labels = new String[arSurveyChain.size()];
		for (int i = 0; i < arSurveyChain.size(); i++) {
			double[] chainPoint = arSurveyChain.get(i);
			for (int j = 0; j < arLabelPoints.size(); j++) {
				// Check if point matches chain point
				if ((chainPoint[0] == arLabelPoints.get(j)[0])
						&& (chainPoint[1] == arLabelPoints.get(j)[1])
						&& (chainPoint[2] == arLabelPoints.get(j)[2])) {
					labels[i] = stnLabels.get(j);
					break;
				}
			}
		}

		return labels;

	}

	/**
	 * Converts a series of points in a polyline with absolute coordinates into a
	 * series
	 * of connected survey legs,calculating tape, compass and clino data.
	 * 
	 * @param List<double[]> List of all points in polyline as x,y,z coordinates
	 * @return SurveySeries of all the legs represented by the polyline. Fixed by
	 *         position of first stn.
	 */
	private SurveySeries makeSeriesFromPolyline(List<double[]> arSurveyChain, String seriesName, String[] labels) {
		// Create new series from this chain
		SurveySeries series = new SurveySeries(seriesName);

		// Set date
		series.setSurveyDate(seriesDate);

		// Loop through all points in chain
		for (int i = 1; i < arSurveyChain.size(); i++) {
			SurveyLeg leg = new SurveyLeg();
			SurveyStation fromStn = new SurveyStation(i - 1);
			SurveyStation toStn = new SurveyStation(i);
			double[] startPoint = arSurveyChain.get(i - 1);
			double[] endPoint = arSurveyChain.get(i);

			if (i == 1) {
				// First leg, so fix first station
				fromStn.setFixed(FixType.OTHER, startPoint[0], startPoint[1], startPoint[2]);
			}

			// Calculate leg data
			double x = endPoint[0] - startPoint[0];
			double y = endPoint[1] - startPoint[1];
			double z = endPoint[2] - startPoint[2];

			// Only add point if in a different position to previous point in polyline (or
			// series is only two points forming an equate)
			if (x != 0 || y != 0 || z != 0 || arSurveyChain.size() == 2) {
				double hori = Math.pow(Math.pow(x, 2) + Math.pow(y, 2), 0.5);
				double tape = Math.pow(Math.pow(hori, 2) + Math.pow(z, 2), 0.5);
				double compass = bearing(x, y);
				double clino = bearing(z, hori);
				if (clino > 90) {
					clino = clino - 360;
				}
				// Set stn names from labels array if we have them
				if (labels[i - 1] != null) {
					fromStn.setName(labels[i - 1]);
				}
				if (labels[i] != null) {
					toStn.setName(labels[i]);
				}

				leg.setFromStn(fromStn);
				leg.setToStn(toStn);
				leg.setLength(tape, LengthUnit.Metres);
				leg.setCompass(compass, BearingUnit.Degrees);
				leg.setClino(clino, GradientUnit.Degrees);

				series.addLeg(leg);
			}
		}

		// CleanSeries(series);

		return series;
	}

	/**
	 * bearing
	 * 
	 * Returns bearing for angle described by x and y lengths
	 */
	private double bearing(double x, double y) {
		double res = 0;

		if (x == 0) {
			if (y < 0) {
				res = 180;
			} else {
				res = 0;
			}
		} else if (x < 0) {
			if (y == 0) {
				res = 270;
			} else if (y < 0) {
				// 180-270
				res = 180 + (Math.atan(x / y) * 180 / Math.PI);
			} else {
				// 270-360
				res = 360 + (Math.atan(x / y) * 180 / Math.PI);
			}
		} else {
			if (y == 0) {
				res = 90;
			} else if (y < 0) {
				// 90-180
				res = 180 + (Math.atan(x / y) * 180 / Math.PI);
			} else {
				// 0-90
				res = (Math.atan(x / y) * 180 / Math.PI);
			}
		}

		return res;
	}

	private double roundedDataValue(String dataLine) {
		double roundedVal = ((double) Math.round(Double.valueOf(dataLine) * 10000)) / 10000;

		return roundedVal;
	}

	/**
	 * Clean up names in the survey series
	 * 
	 * @param series
	 */
	private void CleanSeries(SurveySeries series) {
		// Count occurrences of each unique prefix on station names
		List<String> stnNamePrefixes = new ArrayList<String>();
		List<Integer> stnNamePrefixCounts = new ArrayList<Integer>();

		for (int i = 0; i < series.legCount(); i++) {
			SurveyLeg leg = series.getLegRaw(i);

			String name = leg.getFromStn().getName();
			if (name.contains(".")) {
				String prefix = name.substring(0, name.lastIndexOf('.'));
				boolean found = false;
				for (int j = 0; j < stnNamePrefixes.size(); j++) {
					if (prefix.equals(stnNamePrefixes.get(j))) {
						found = true;
						int count = stnNamePrefixCounts.get(j);
						count++;
						stnNamePrefixCounts.set(j, count);
						break;
					}
				}
				if (found == false) {
					stnNamePrefixes.add(prefix);
					stnNamePrefixCounts.add(1);
				}
			}

			name = leg.getToStn().getName();
			if (name.contains(".")) {
				String prefix = name.substring(0, name.lastIndexOf('.'));
				boolean found = false;
				for (int j = 0; j < stnNamePrefixes.size(); j++) {
					if (prefix.equals(stnNamePrefixes.get(j))) {
						found = true;
						int count = stnNamePrefixCounts.get(j);
						count++;
						stnNamePrefixCounts.set(j, count);
						break;
					}
				}
				if (found == false) {
					stnNamePrefixes.add(prefix);
					stnNamePrefixCounts.add(1);
				}
			}
		}

		if (stnNamePrefixes.size() > 0) {
			// Find most common prefix then remove it from all station names and use as
			// series name
			int mostCommonPrefixIdx = 0;
			int highestOccurence = 0;
			for (int j = 0; j < stnNamePrefixCounts.size(); j++) {
				if (stnNamePrefixCounts.get(j) > highestOccurence) {
					mostCommonPrefixIdx = j;
					highestOccurence = stnNamePrefixCounts.get(j);
				}
			}

			// Rename series using most common prefix
			series.setSeriesName(stnNamePrefixes.get(mostCommonPrefixIdx));
		}

	}

	/**
	 * Determines the prefix for a station name (all the string up to the last dot
	 * separator)
	 * 
	 * @param name Station name to examine
	 * @return Prefix from the name, or an empty string if no dit separator in name.
	 */
	private String getNamePrefix(String name) {
		int lastDotPos = name.lastIndexOf('.');
		String prefix = "";
		if (lastDotPos > -1) {
			prefix = name.substring(0, lastDotPos);
		}
		return prefix;
	}

	/**
	 * Determines the station name after the prefix (all the string after the last
	 * dot separator)
	 * 
	 * @param name Station name to examine
	 * @return Shortened station name
	 */
	private String getShortenedName(String name) {
		int lastDotPos = name.lastIndexOf('.');
		String shortName = name;
		if (lastDotPos > -1) {
			shortName = name.substring(lastDotPos + 1);
		}
		return shortName;
	}

	/**
	 * Returns a list of the indices in the label points array of all points
	 * matching the position of a given point
	 * 
	 * @param matchingPoint The point to match
	 * @param labelPoints   Array of points to search for matching points in
	 * @return ArrayList of the indices of all points at the same coordinates as the
	 *         specified point
	 */
	private List<Integer> getListOfMatchingPoints(double[] matchingPoint, List<double[]> labelPoints) {
		List<Integer> matchingPoints = new ArrayList<Integer>();
		for (int allPointsIdx = 0; allPointsIdx < labelPoints.size(); allPointsIdx++) {
			double[] testPoint = labelPoints.get(allPointsIdx);
			if ((matchingPoint[0] == testPoint[0])
					&& (matchingPoint[1] == testPoint[1])
					&& (matchingPoint[2] == testPoint[2])) {
				// Add index of matching point to matches array
				matchingPoints.add(allPointsIdx);
			}
		}
		return matchingPoints;
	}
}
