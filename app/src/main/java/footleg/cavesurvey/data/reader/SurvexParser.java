/**
 * Copyright (C) 2009-2024 Paul Fretwell - https://github.com/Footleg/caveconverter
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
import java.util.Iterator;
import java.util.List;

import footleg.cavesurvey.converter.CaveConverter;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.Equate;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Parser for Survex format text data files.
 * 
 * @author Footleg
 * @version 2024.06.09 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 * 
 * @to.do
 *        TODO Parse entrance flags
 *        TODO Parse fix flags
 *        TODO Parse calibration comments fields
 *        TODO Parse team fields
 *        TODO Parse instrument fields
 *        TODO Parse anonymous station legs as splays
 */
public class SurvexParser {
	private Logger logger;

	/**
	 * Class constructor
	 * 
	 * @param logger Logging class to output information, warning and error messages
	 *               to
	 */
	public SurvexParser(Logger logger) {
		super();
		this.logger = logger;
	}

	private class DataBlockProperties {

		public DataBlockProperties(boolean duplicateFlag, boolean splayFlag, boolean surfaceFlag, boolean noSurvey) {
			super();
			this.duplicateFlag = duplicateFlag;
			this.splayFlag = splayFlag;
			this.surfaceFlag = surfaceFlag;
			this.noSurvey = noSurvey;
		}

		public DataBlockProperties(DataBlockProperties parentBlock) {
			super();
			this.duplicateFlag = parentBlock.duplicateFlag;
			this.splayFlag = parentBlock.splayFlag;
			this.surfaceFlag = parentBlock.surfaceFlag;
			this.noSurvey = parentBlock.noSurvey;
			this.parentBlock = parentBlock;
		}

		private boolean duplicateFlag = false;
		private boolean splayFlag = false;
		private boolean surfaceFlag = false;
		private boolean noSurvey = false;
		private DataBlockProperties parentBlock;

	}

	/**
	 * Parse survex format data into the cave data model
	 * 
	 * @param surveyFileData ListArray of data lines from a survex file
	 * @param multifileRefs  ArrayList of strings indicating the original file and
	 *                       line number for each data line in the
	 *                       surveyFileData ArrayList passed in. Used to report
	 *                       errors in logger for original survey data files when
	 *                       parsing multi-file Survex projects.
	 * @return Cave Survey object
	 * @throws ParseException Exception raised when information in a survey data
	 *                        file is not supported or valid for the format
	 */
	public CaveSurvey parseFile(List<String> surveyFileData, List<String> multifileRefs) throws ParseException {
		/**
		 * Read state codes:
		 * 0=starting new file
		 * 1=inside begin/end block
		 * 2=LRUD block
		 */

		int state = 0;

		// Create cave survey object to hold data
		CaveSurvey allSeries = new CaveSurvey(logger);

		// Create stack to hold open series while processing data lines
		List<SurveySeries> seriesStack = new ArrayList<SurveySeries>();

		// Create stack to hold open series names while processing data lines
		List<String> nameStack = new ArrayList<String>();

		// Create list for equates
		List<Equate> equates = new ArrayList<Equate>();

		// Create a series instance to use as a pointer to the active series data is
		// being read from
		SurveySeries liveSeries = null;

		// Create list to hold data order for active data block
		List<String> dataOrder = new ArrayList<String>();

		// Variable to hold values for parent block when inside an anonymous block
		DataBlockProperties blockProps = new DataBlockProperties(false, false, false, false);

		// Loop through all data lines
		for (int fileDataIdx = 0; fileDataIdx < surveyFileData.size(); fileDataIdx++) {
			// Get data line, file line number and line reference for the next data line
			// from the survey data array list
			int lineNo = fileDataIdx + 1;
			String dataLine = surveyFileData.get(fileDataIdx);
			String lineRef = "" + lineNo;
			if (multifileRefs != null) {
				lineRef = multifileRefs.get(fileDataIdx);
			}

			// Discard text after comment character
			// TODO Keep comments and add into data model for legs, series and file heading
			int commentPos = dataLine.indexOf(';');
			if (commentPos > -1) {
				dataLine = dataLine.substring(0, commentPos);
			}
			// Trim whitespace off line ends
			dataLine = dataLine.trim();
			// Skip blank lines
			if (dataLine.length() > 0) {
				// Check line for commands
				if (dataLine.charAt(0) == '*') {
					// Process line into individual items trimming off initial '*'
					String[] data = UtilityFunctions.cleanAndSplitDataLine(dataLine.substring(1));
					// Get command keyword
					String cmd = data[0];

					// Check for expected commands
					if (cmd.compareToIgnoreCase("BEGIN") == 0) {
						// Start of new block
						state = 1;
						if (data.length == 2) {
							// Create series
							liveSeries = new SurveySeries(data[1]);
							// Add name to stack
							nameStack.add(data[1]);
							// Get calibrations from last series and apply to new child series (if present)
							double tapeCal = 0.0;
							double compassCal = 0.0;
							double clinoCal = 0.0;
							double declinationCal = 0.0;
							// Check if a parent series exists
							if (seriesStack.size() > 0) {
								SurveySeries series = seriesStack.get(seriesStack.size() - 1);
								tapeCal = series.getTapeCalibration(LengthUnit.Metres);
								compassCal = series.getCompassCalibration(BearingUnit.Degrees);
								clinoCal = series.getClinoCalibration(GradientUnit.Degrees);
								declinationCal = series.getDeclination();
								dataOrder = series.getDataOrder();
							}
							// Put new series onto stack
							seriesStack.add(liveSeries);
							// Apply calibrations from parent series.
							// These will get overwritten if this series has it's own calibrations.
							liveSeries.setTapeCalibration(tapeCal, LengthUnit.Metres);
							liveSeries.setCompassCalibration(compassCal, BearingUnit.Degrees);
							liveSeries.setClinoCalibration(clinoCal, GradientUnit.Degrees);
							liveSeries.setDeclination(declinationCal);
							liveSeries.setDataOrder(dataOrder);
							// Copy flag settings into new child series and store parent settings to
							// retrieve at series end
							DataBlockProperties newBlock = new DataBlockProperties(blockProps);
							// Make new properties block the current one
							blockProps = newBlock;
						} else if (data.length < 2) {
							// Anonymous block begin. Put properties from last block into new block as
							// parent
							DataBlockProperties newBlock = new DataBlockProperties(blockProps);
							// then swap new block to current
							blockProps = newBlock;
							// throw new ParseException( UtilityFunctions.formatFileParserMsg("BEGIN/END
							// blocks without names are not supported.", lineRef ), lineNo );
						} else {
							// Do not support begin/end blocks names with white space
							throw new ParseException(
									UtilityFunctions.formatFileParserMsg(
											"BEGIN/END blocks names containing spaces are not supported.", lineRef),
									lineNo);
						}
					} else if (cmd.compareToIgnoreCase("END") == 0) {
						// End block
						if (data.length == 1) {
							// Anonymous block ended, so just restore parent block flags
							blockProps = blockProps.parentBlock;
						} else {
							// Named block ended
							String blockEndName = data[1];
							// Check end matches end of section name
							String currentBlockName = nameStack.get(nameStack.size() - 1);
							if (currentBlockName.compareToIgnoreCase(blockEndName) == 0) {
								// Found matching end block, so close series
								// Remove live series from stack, as it is closed
								SurveySeries endedSeries = seriesStack.remove(seriesStack.size() - 1);
								nameStack.remove(nameStack.size() - 1);
								// Restore block properties from parent series block
								blockProps = blockProps.parentBlock;
								if (seriesStack.size() > 0) {
									// Series is inside another, so make that live and add finished series to it
									liveSeries = seriesStack.get(seriesStack.size() - 1);
									liveSeries.addSeries(endedSeries);
									// Return state to 1
									state = 1;
								} else {
									// No other series on stack, so add to main cave survey
									allSeries.add(endedSeries);
									// Clear reference to live series
									liveSeries = null;
									// Return state to 0
									state = 0;
								}
							} else {
								// Names of begin end blocks do not match
								throw new ParseException(
										UtilityFunctions
												.formatFileParserMsg("Names of begin end blocks do not match. Begin=" +
														currentBlockName + " End=" + blockEndName + ".", lineRef),
										lineNo);
							}
						}
					} else if (cmd.compareToIgnoreCase("EQUATE") == 0) {
						// Get two parts of the equate, expanding to full nested series names
						String fullSeriesPrefix = fullNestedSeriesName(nameStack);
						Equate equate = new Equate(fullSeriesPrefix, data[1], fullSeriesPrefix, data[2]);
						// Add to cache
						equates.add(equate);
					} else if (cmd.compareToIgnoreCase("DATA") == 0) {
						// Reset nosurvey flag
						blockProps.noSurvey = false;
						// Check data command type
						if (data[1].compareToIgnoreCase("PASSAGE") == 0) {
							// LRUD data block
							state = 2;
						} else if (data[1].compareToIgnoreCase("NORMAL") == 0) {
							state = 1;
							// Check data order
							// TODO Add support for backcompass and backclino in normal data
							logger.logMessage("Found normal data header at line " + lineNo + ". Checking format.");

							// Build array of expected categories here, and then loop through items picking
							// off those that match until done.
							// We will then know if all were found and the format is valid.
							dataOrder.clear();
							List<String> allOrderKeys = new ArrayList<String>();
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_FROMSTN);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_TOSTN);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_LENGTH);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_BEARING);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_CLINO);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_IGNOREALL);

							for (int dataIdx = 2; dataIdx < data.length; dataIdx++) {
								// Get next item (including translating alternative terms)
								String item = data[dataIdx].toUpperCase();
								if (item.compareTo("TAPE") == 0) {
									item = CaveConverter.DATA_ORDER_CAT_LENGTH;
								} else if (item.compareTo("COMPASS") == 0) {
									item = CaveConverter.DATA_ORDER_CAT_BEARING;
								} else if (item.compareTo("CLINO") == 0) {
									item = CaveConverter.DATA_ORDER_CAT_CLINO;
								}
								// Look for item in remaining unused keys list
								int foundIdx = allOrderKeys.indexOf(item);
								if (foundIdx > -1) {
									// Add this item to dataOrder list, and remove from unused keys list
									dataOrder.add(item);
									allOrderKeys.remove(foundIdx);
								} else {
									// Item not found, so unsupported data normal block
									throw new ParseException(UtilityFunctions
											.formatFileParserMsg("Unsupported survex normal data order. Term '" + item
													+ "' is not supported.", lineRef),
											lineNo);
								}
							}
							// Add to live series, as primary unless that is already set to diving, in which
							// case set as secondary
							List<String> checkDataOrder = liveSeries.getDataOrder();
							if (checkDataOrder.size() > 0
									&& (UtilityFunctions.dataOrderIsDiving(checkDataOrder))) {
								liveSeries.setDataOrder2(dataOrder);
							} else {
								liveSeries.setDataOrder(dataOrder);
							}
						} else if (data[1].compareToIgnoreCase("DIVING") == 0) {
							state = 1;
							// Check data order
							// TODO Add support for backcompass and backclino in diving data
							logger.logMessage("Found diving data header at line " + lineNo + ". Checking format.");

							// Build array of expected categories here, and then loop through items picking
							// off those that match until done.
							// We will then know if all were found and the format is valid.
							dataOrder.clear();
							List<String> allOrderKeys = new ArrayList<String>();
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_FROMSTN);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_TOSTN);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_LENGTH);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_BEARING);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_FROMDEPTH);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_TODEPTH);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_DEPTHCHANGE);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_IGNOREALL);

							for (int dataIdx = 2; dataIdx < data.length; dataIdx++) {
								// Get next item (including translating alternative terms)
								String item = data[dataIdx].toUpperCase();
								if (item.compareTo("TAPE") == 0) {
									item = CaveConverter.DATA_ORDER_CAT_LENGTH;
								} else if (item.compareTo("COMPASS") == 0) {
									item = CaveConverter.DATA_ORDER_CAT_BEARING;
								}
								// Look for item in remaining unused keys list
								int foundIdx = allOrderKeys.indexOf(item);
								if (foundIdx > -1) {
									// Add this item to dataOrder list, and remove from unused keys list
									dataOrder.add(item);
									allOrderKeys.remove(foundIdx);
									// If a diving depth term, then remove alternative form
									if (item.compareTo(CaveConverter.DATA_ORDER_CAT_DEPTHCHANGE) == 0) {
										// Remove from and to depth items from list of remaining valid terms
										int altIdx = allOrderKeys.indexOf(CaveConverter.DATA_ORDER_CAT_FROMDEPTH);
										if (altIdx > -1) {
											allOrderKeys.remove(altIdx);
										}
										altIdx = allOrderKeys.indexOf(CaveConverter.DATA_ORDER_CAT_TODEPTH);
										if (altIdx > -1) {
											allOrderKeys.remove(altIdx);
										}
									} else if (item.compareTo(CaveConverter.DATA_ORDER_CAT_FROMDEPTH) == 0) {
										// Remove depth change item from list of remaining valid terms
										int altIdx = allOrderKeys.indexOf(CaveConverter.DATA_ORDER_CAT_DEPTHCHANGE);
										if (altIdx > -1) {
											allOrderKeys.remove(altIdx);
										}
										// Check toDepth comes after fromDepth as parser only supports this ordering of
										// depths in diving data
										altIdx = allOrderKeys.indexOf(CaveConverter.DATA_ORDER_CAT_TODEPTH);
										if (altIdx == -1) {
											throw new ParseException(UtilityFunctions.formatFileParserMsg(
													"Unsupported survex diving data order. todepth before fromdepth is not supported.",
													lineRef), lineNo);
										}
									}
								} else {
									// Item not found, so unsupported data normal block
									throw new ParseException(UtilityFunctions
											.formatFileParserMsg("Unsupported survex diving data order. Term '" + item
													+ "' is not supported.", lineRef),
											lineNo);
								}
							}
							// Add to live series, as primary unless that is already normal,
							// in which case set as secondary
							List<String> checkDataOrder = liveSeries.getDataOrder();
							if (checkDataOrder.size() > 0
									&& (UtilityFunctions.dataOrderIsDiving(checkDataOrder) == false)) {
								liveSeries.setDataOrder2(dataOrder);
							} else {
								liveSeries.setDataOrder(dataOrder);
							}
						} else if (data[1].compareToIgnoreCase("NOSURVEY") == 0) {
							state = 1;
							// Check data order
							// TODO Add support for nosurvey station format
							logger.logMessage("Found nosurvey data header at line " + lineNo + ". Checking format.");

							// Build array of expected categories here, and then loop through items picking
							// off those that match until done.
							// We will then know if all were found and the format is valid.
							dataOrder.clear();
							List<String> allOrderKeys = new ArrayList<String>();
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_FROMSTN);
							allOrderKeys.add(CaveConverter.DATA_ORDER_CAT_TOSTN);

							for (int dataIdx = 2; dataIdx < data.length; dataIdx++) {
								// Get next item
								String item = data[dataIdx].toUpperCase();
								// Look for item in remaining unused keys list
								int foundIdx = allOrderKeys.indexOf(item);
								if (foundIdx > -1) {
									// Add this item to dataOrder list, and remove from unused keys list
									dataOrder.add(item);
									allOrderKeys.remove(foundIdx);
								} else {
									// Item not found, so unsupported data normal block
									throw new ParseException(UtilityFunctions
											.formatFileParserMsg("Unsupported survex nosurvey data order. Term '" + item
													+ "' is not supported.", lineRef),
											lineNo);
								}
							}
							// Set nosurvey flag
							blockProps.noSurvey = true;
						} else {
							// Other data settings not currently supported (assumes file use default order)
							throw new ParseException(UtilityFunctions.formatFileParserMsg(
									"Unsupported survex data command: " + data[1], lineRef), lineNo);
						}
					} else if (cmd.compareToIgnoreCase("CALIBRATE") == 0) {
						// Process calibration command
						if (data.length == 3) {
							String type = data[1];
							double value = Double.parseDouble(data[2]);
							if (type.compareToIgnoreCase("tape") == 0) {
								// Set tape calibration in active series
								liveSeries.setTapeCalibration(value, liveSeries.getLengthUnit());
							} else if (type.compareToIgnoreCase("declination") == 0) {
								// Set declination calibration in active series
								liveSeries.setDeclination(value);
							} else if (type.compareToIgnoreCase("compass") == 0) {
								// Set compass calibration in active series
								liveSeries.setCompassCalibration(value, liveSeries.getBearingUnit());
							} else if (type.compareToIgnoreCase("clino") == 0) {
								// Set compass calibration in active series
								liveSeries.setClinoCalibration(value, liveSeries.getGradientUnit());
							}
							// TODO Add support for calibration scale factors
						} else {
							// Invalid calibration lie
							throw new ParseException(UtilityFunctions.formatFileParserMsg(
									"CALIBRATE command did not contain a single instrument type plus value.", lineRef),
									lineNo);
						}
					} else if (cmd.compareToIgnoreCase("DATE") == 0) {
						// Process date
						if (data.length > 1) {
							Date value = UtilityFunctions.stringToDate(data[1], "yyyy.MM.dd");
							liveSeries.setSurveyDate(value);
						} else {
							logger.logMessage("DATE command without further data skipped for line: " + dataLine);
						}
					} else if (cmd.compareToIgnoreCase("UNITS") == 0) {
						// Determine unit being set
						if (data.length > 2) {
							// Find unit in line
							int unitIdx = 0;
							for (int i = 2; i < data.length; i++) {
								if ((data[i].compareToIgnoreCase("METRES") == 0)
										|| (data[i].compareToIgnoreCase("METERS") == 0)
										|| (data[i].compareToIgnoreCase("METRIC") == 0)
										|| (data[i].compareToIgnoreCase("YARDS") == 0)
										|| (data[i].compareToIgnoreCase("FEET") == 0)
										|| (data[i].compareToIgnoreCase("DEGS") == 0)
										|| (data[i].compareToIgnoreCase("DEGREES") == 0)
										|| (data[i].compareToIgnoreCase("GRADS") == 0)
										|| (data[i].compareToIgnoreCase("MILS") == 0)
										|| (data[i].compareToIgnoreCase("MINUTES") == 0)
										|| (data[i].compareToIgnoreCase("PERCENT") == 0)
										|| (data[i].compareToIgnoreCase("PERCENTAGE") == 0)) {
									// Found unit, store index and then look back through items to set for this unit
									unitIdx = i;
									break;
								}
							}

							if (unitIdx > 0) {
								for (int i = 1; i < unitIdx; i++) {
									if (data[i].compareToIgnoreCase("TAPE") == 0
											|| data[i].compareToIgnoreCase("LENGTH") == 0) {
										// TAPE/LENGTH, BACKTAPE/BACKLENGTH, COUNTER/COUNT, DEPTH, DX/EASTING,
										// DY/NORTHING, DZ/ALTITUDE in YARDS|FEET|METRIC|METRES|METERS (default: METRES)
										if (data[unitIdx].compareToIgnoreCase("METRES") == 0
												|| data[unitIdx].compareToIgnoreCase("METERS") == 0
												|| data[unitIdx].compareToIgnoreCase("METRIC") == 0) {
											liveSeries.setLengthUnit(LengthUnit.Metres);
										} else if (data[unitIdx].compareToIgnoreCase("FEET") == 0) {
											liveSeries.setLengthUnit(LengthUnit.Feet);
										} else if (data[unitIdx].compareToIgnoreCase("YARDS") == 0) {
											liveSeries.setLengthUnit(LengthUnit.Yards);
										} else {
											throw new ParseException(UtilityFunctions.formatFileParserMsg(
													"Unsupported length unit '" + data[unitIdx] + "'.", lineRef),
													lineNo);
										}
									} else if (data[i].compareToIgnoreCase("DEPTH") == 0) {
										// TAPE/LENGTH, BACKTAPE/BACKLENGTH, COUNTER/COUNT, DEPTH, DX/EASTING,
										// DY/NORTHING, DZ/ALTITUDE in YARDS|FEET|METRIC|METRES|METERS (default: METRES)
										if (data[unitIdx].compareToIgnoreCase("METRES") == 0
												|| data[unitIdx].compareToIgnoreCase("METERS") == 0
												|| data[unitIdx].compareToIgnoreCase("METRIC") == 0) {
											liveSeries.setDepthUnit(LengthUnit.Metres);
										} else if (data[unitIdx].compareToIgnoreCase("FEET") == 0) {
											liveSeries.setDepthUnit(LengthUnit.Feet);
										} else if (data[unitIdx].compareToIgnoreCase("YARDS") == 0) {
											liveSeries.setDepthUnit(LengthUnit.Yards);
										} else {
											throw new ParseException(
													UtilityFunctions.formatFileParserMsg(
															"Unsupported depth unit '" + data[unitIdx] + "'.", lineRef),
													lineNo);
										}
									} else if (data[i].compareToIgnoreCase("COMPASS") == 0
											|| data[i].compareToIgnoreCase("BEARING") == 0) {
										// COMPASS/BEARING, BACKCOMPASS/BACKBEARING, DECLINATION in
										// DEG|DEGREES|GRADS|MILS|MINUTES (default: DEGREES)
										if (data[unitIdx].compareToIgnoreCase("DEGS") == 0
												|| data[unitIdx].compareToIgnoreCase("DEGREES") == 0) {
											liveSeries.setBearingUnit(BearingUnit.Degrees);
										} else if (data[unitIdx].compareToIgnoreCase("GRADS") == 0
												|| data[unitIdx].compareToIgnoreCase("MILS") == 0) {
											liveSeries.setBearingUnit(BearingUnit.Grads);
										} else if (data[unitIdx].compareToIgnoreCase("MINUTES") == 0) {
											liveSeries.setBearingUnit(BearingUnit.Minutes);
										} else {
											throw new ParseException(UtilityFunctions.formatFileParserMsg(
													"Unsupported bearing unit '" + data[unitIdx] + "'.", lineRef),
													lineNo);
										}
									} else if (data[i].compareToIgnoreCase("CLINO") == 0
											|| data[i].compareToIgnoreCase("GRADIENT") == 0) {
										// CLINO/GRADIENT, BACKCLINO/BACKGRADIENT in
										// DEG|DEGREES|GRADS|MILS|PERCENT|PERCENTAGE (default: DEGREES)
										if (data[unitIdx].compareToIgnoreCase("DEGS") == 0
												|| data[unitIdx].compareToIgnoreCase("DEGREES") == 0) {
											liveSeries.setGradientUnit(GradientUnit.Degrees);
										} else if (data[unitIdx].compareToIgnoreCase("GRADS") == 0
												|| data[unitIdx].compareToIgnoreCase("MILS") == 0) {
											liveSeries.setGradientUnit(GradientUnit.Grads);
										} else if (data[unitIdx].compareToIgnoreCase("PERCENT") == 0
												|| data[unitIdx].compareToIgnoreCase("PERCENTAGE") == 0) {
											liveSeries.setGradientUnit(GradientUnit.Percent);
										} else if (data[unitIdx].compareToIgnoreCase("MINUTES") == 0) {
											liveSeries.setGradientUnit(GradientUnit.Minutes);
										} else {
											throw new ParseException(UtilityFunctions.formatFileParserMsg(
													"Unsupported gradient unit '" + data[unitIdx] + "'.", lineRef),
													lineNo);
										}
									} else {
										throw new ParseException(UtilityFunctions.formatFileParserMsg(
												"Unsupported unit type '" + data[i] + "'.", lineRef), lineNo);
									}
								}
							} else {
								throw new ParseException(UtilityFunctions.formatFileParserMsg(
										"UNITS command did not contain a category of measurement plus value.", lineRef),
										lineNo);
							}

						}

					} else if (cmd.compareToIgnoreCase("FLAGS") == 0) {
						// Process flags
						boolean notPrefixed = false;
						for (int iFlags = 1; iFlags < data.length; iFlags++) {
							// Read all flags settings to determine what is being turned on or off
							if (data[iFlags].compareToIgnoreCase("NOT") == 0) {
								notPrefixed = true;
							} else if (data[iFlags].compareToIgnoreCase("DUPLICATE") == 0) {
								blockProps.duplicateFlag = (notPrefixed == false);
								notPrefixed = false;
							} else if (data[iFlags].compareToIgnoreCase("SPLAY") == 0) {
								blockProps.splayFlag = (notPrefixed == false);
								notPrefixed = false;
							} else if (data[iFlags].compareToIgnoreCase("SURFACE") == 0) {
								blockProps.surfaceFlag = (notPrefixed == false);
								notPrefixed = false;
							} else {
								// Reset notPrefixed flag if any other value
								notPrefixed = false;
							}
						}
					} else {
						// Ignore other commands inside begin end block
						// TODO Add support for FIX stations
						// TODO Add support for ENTRANCE stations
						// TODO Add support for topofil clino calibration scale factor
						logger.logMessage("Unsupported Survex command ignored: " + cmd);
					}
				} else {
					// Data line
					// logger.logMessage("Data line " + CaveConverter.padNumber(lineNo, 4) + ": " +
					// dataLine);

					if (liveSeries != null) {
						// Process line into individual items
						String[] data = UtilityFunctions.cleanAndSplitDataLine(dataLine);

						switch (state) {
							case 1:
								// Create new survey leg
								SurveyLeg leg = new SurveyLeg();
								double fromDepth = 0;

								// Create record from the items
								int index = 0;
								while (index < data.length) {
									String item = data[index];
									// Check for end of line
									if (item.charAt(0) == ';') {
										// Comments, so ignore rest of line
										// TODO Add support for leg comments
										index = data.length;
									} else {
										// Check data order was determined
										if (dataOrder.size() == 0) {
											// No data order specified, so set to default
											dataOrder.add(CaveConverter.DATA_ORDER_CAT_FROMSTN);
											dataOrder.add(CaveConverter.DATA_ORDER_CAT_TOSTN);
											dataOrder.add(CaveConverter.DATA_ORDER_CAT_LENGTH);
											dataOrder.add(CaveConverter.DATA_ORDER_CAT_BEARING);
											dataOrder.add(CaveConverter.DATA_ORDER_CAT_CLINO);
										} else if (blockProps.noSurvey == false && dataOrder.size() < 5) {
											throw new ParseException(UtilityFunctions.formatFileParserMsg(
													"Last data order command did not contain enough items for a survey leg.",
													lineRef), lineNo);
										} else if ((dataOrder.size() < index + 1)
												&& (dataOrder.get(dataOrder.size() - 1)
														.equals(CaveConverter.DATA_ORDER_CAT_IGNOREALL)) == false) {
											throw new ParseException(UtilityFunctions.formatFileParserMsg(
													"Last data order command did not contain enough items for data line.",
													lineRef), lineNo);
										}
										// Put item into appropriate value
										String itemCat = CaveConverter.DATA_ORDER_CAT_IGNOREALL;
										if (index < dataOrder.size()) {
											itemCat = dataOrder.get(index);
										}
										if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_FROMSTN)) {
											// TODO Add support for retaining station name when not a number
											leg.setFromStn(UtilityFunctions.createStationFromNameForSeries(data[index],
													liveSeries));
											// Set nosurvey legs flag for nosurvey leg when setting fromStn
											if (blockProps.noSurvey) {
												leg.setNosurvey(true);
											}
										} else if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_TOSTN)) {
											// TODO Add support for retaining station name when not a number
											leg.setToStn(UtilityFunctions.createStationFromNameForSeries(data[index],
													liveSeries));
										} else if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_LENGTH)) {
											double length = Double.parseDouble(data[index]);
											if (length < 0) {
												logger.logMessage("Warning: Negative leg length (" + data[index] +
														") read from Survex file at line " + lineNo + ".");
											}
											leg.setLength(length, liveSeries.getLengthUnit());
										} else if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_BEARING)) {
											if (data[index].compareTo("-") == 0) {
												leg.setCompass(0, BearingUnit.Degrees);
											} else {
												leg.setCompass(Double.valueOf(data[index]),
														liveSeries.getBearingUnit());
											}
										} else if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_CLINO)) {
											// Trim data item after ';' if present
											String val = data[index];
											int commentCharPos = val.indexOf(';');
											if (commentCharPos > 0) {
												val = val.substring(0, commentCharPos);
											}
											double straightDown = -90;
											double straightUp = 90;
											double level = 0;
											if (val.compareToIgnoreCase("-V") == 0) {
												leg.setClino(straightDown, GradientUnit.Degrees);
											} else if (val.compareToIgnoreCase("down") == 0) {
												leg.setClino(straightDown, GradientUnit.Degrees);
											} else if (val.compareToIgnoreCase("d") == 0) {
												leg.setClino(straightDown, GradientUnit.Degrees);
											} else if (val.compareToIgnoreCase("+V") == 0) {
												leg.setClino(straightUp, GradientUnit.Degrees);
											} else if (val.compareToIgnoreCase("up") == 0) {
												leg.setClino(straightUp, GradientUnit.Degrees);
											} else if (val.compareToIgnoreCase("u") == 0) {
												leg.setClino(straightUp, GradientUnit.Degrees);
											} else if (val.compareToIgnoreCase("-") == 0) {
												leg.setClino(level, GradientUnit.Degrees);
											} else if (val.compareToIgnoreCase("level") == 0) {
												leg.setClino(level, GradientUnit.Degrees);
											} else {
												leg.setClino(Double.valueOf(val), liveSeries.getGradientUnit());
											}
										} else if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_FROMDEPTH)) {
											// Store fromDepth, it will be added to leg when toDepth is read (parser
											// only supports fromDepth before toDepth in data ordering)
											fromDepth = Double.parseDouble(data[index]);
										} else if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_TODEPTH)) {
											double toDepth = Double.parseDouble(data[index]);
											leg.setDepths(fromDepth, toDepth, liveSeries.getDepthUnit());
										} else if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_DEPTHCHANGE)) {
											double depthChange = Double.parseDouble(data[index]);
											leg.setDepthChange(depthChange, liveSeries.getDepthUnit());
										} else if (itemCat.equals(CaveConverter.DATA_ORDER_CAT_IGNOREALL)) {
											// TODO Comment on line end?
										}
									}

									index++;
								}

								// Check leg was found
								if (blockProps.noSurvey || leg.getLength(LengthUnit.Metres) > -1) {
									// Set flags for leg
									leg.setDuplicate(blockProps.duplicateFlag);
									if (leg.getToStn().getName().compareTo("-") == 0) {
										// Legs to anonymous stations are always treated as splays
										leg.setSplay(true);
									} else {
										leg.setSplay(blockProps.splayFlag);
									}
									leg.setSurface(blockProps.surfaceFlag);
									// Add leg to series
									liveSeries.addLeg(leg);
								}
								break;
							case 2:
								// Add data to LRUD cache
								/**
								 * TODO Need to store all the LRUD lines in groups to match with
								 * the legs once the series is complete. Need to match legs to lrud
								 * from two consecutive lines to be sure the LRUD is for that leg, and
								 * not another leg from the same station. Create the LRUD group in
								 * the command parsing switch, as that is where we know a new LRUD group has
								 * been started.
								 */
								break;
						}

					} else {
						// Data line outside of series
						throw new ParseException(UtilityFunctions.formatFileParserMsg(
								"Data line found outside of any begin/end block.", lineRef), lineNo);
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

	// Get series names in stack to generate full series name
	private String fullNestedSeriesName(List<String> stack) {
		String name = "";

		Iterator<String> stackIterator = stack.listIterator();
		while (stackIterator.hasNext()) {
			name += "." + stackIterator.next();
		}
		// Remove initial dot
		return name.substring(1);
	}

}
