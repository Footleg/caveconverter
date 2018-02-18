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
package footleg.cavesurvey.gui.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import footleg.cavesurvey.converter.CaveConverter;
import footleg.cavesurvey.converter.CaveConverter.BearingUnit;
import footleg.cavesurvey.converter.CaveConverter.GradientUnit;
import footleg.cavesurvey.converter.CaveConverter.LengthUnit;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataOutputFormats;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.SurveyLeg;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.data.writer.SurvexWriter;
import footleg.cavesurvey.data.writer.TopoRobotWriter;
import footleg.cavesurvey.tools.SurveyProcessing;

/**
 * Component to display the cave data model
 *  
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.7                                       (The Java version used)
 * 
 * @to.do
 * TODO Add proper visualisation of data model
 */
@SuppressWarnings("serial")
public class CaveModelVisualiser extends JSplitPane {
	
	public class SelectionListener implements TreeSelectionListener {

		public void valueChanged(TreeSelectionEvent se) {
			JTree jTree = (JTree) se.getSource();
			Object selectedNode = (Object) jTree.getLastSelectedPathComponent();
			boolean enableSeriesProcessing = false;

			if (selectedNode instanceof CaveSurvey) {
				enableSeriesProcessing = false;
				CaveSurvey survey = (CaveSurvey) selectedNode;
				taCaveLog.append( ReportCaveSurveyStats( survey ) );
			}
			else if (selectedNode instanceof SurveySeries ) {
				enableSeriesProcessing = true;
				SurveySeries selectedSeries  = (SurveySeries) selectedNode;
				taCaveLog.setText( ReportSeriesStats(selectedSeries) );
			}
			else if (selectedNode instanceof SurveyLeg) {
				SurveyLeg selectedLeg  = (SurveyLeg) selectedNode;
				taCaveLog.setText( ReportLegStats(selectedLeg) );
			}
			//Enable or disable survey series actions
			parent.setSurveySeriesActionsEnabled( enableSeriesProcessing );
		}
	}

	private MainForm parent;
	private JTextArea taCaveLog;
	private CaveSurvey caveModel;
	private JTree tree;
	private Logger logger;

	/**
	 * Create TextArea scrollable tab instance and display model summary. This is a very basic start for a 
	 * full visualiser for the cave data model.
	 * @param parentForm Object reference to the parent form owning this item (so actions can be accessed)
	 * @param model The cave survey model to build a visualisation of
	 * @param font The font to use in the text area pane
	 */
	public CaveModelVisualiser(MainForm parentForm, CaveSurvey model, Font font) {
		super();
		caveModel = model;
		parent = parentForm;
		logger = parentForm.getLogger();
		
		//Create scrollable text area for right pane
		taCaveLog = new JTextArea();
		taCaveLog.setFont( font );
		JScrollPane spCaveLog = new JScrollPane (taCaveLog, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		spCaveLog.setViewportView(taCaveLog);

		//Create tree for left pane, using the CaveSurvey class as the TreeModel
		tree = new JTree( caveModel );
		tree.addTreeSelectionListener( new SelectionListener() );
		JScrollPane spSurveyTree = new JScrollPane (tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		//Configure split pane
		spCaveLog.setPreferredSize(new Dimension(250, 400));
		spSurveyTree.setPreferredSize(new Dimension(250, 400));
		this.setTopComponent( spSurveyTree );
		this.setBottomComponent( spCaveLog );

		renderCaveModel();
	}

	public CaveSurvey getCaveModel() {
		return caveModel;
	}

	/**
	 * To pass a new cave model to an existing visualiser tab
	 * @param caveModel The cave survey model to build a visualisation of
	 */
	public void setCaveModel(CaveSurvey caveModel) {
		this.caveModel = caveModel;
		
		//Pass the new model to the jTree
		tree.setModel( getCaveModel() );

		renderCaveModel();
	}

	/**
	 * @return the split pane
	 */
	public JSplitPane getTabPaneComponent() {
		return this;
	}

	/**
	 * Generates a visualisation of the cave data (currently a bare bones text summary, but intended to be developed).
	 */
	private void renderCaveModel() {
		taCaveLog.setText("This is a bare bones implementation of the visualisation of the cave model." + CaveConverter.newline + 
				"Currently it just reports some stats to let you know your data was loaded successfully." + CaveConverter.newline + 
				"This visualisation will improve with later releases to enable you to view your cave model" + CaveConverter.newline + 
				"more effectively." + CaveConverter.newline + 
				"-----" + CaveConverter.newline + CaveConverter.newline );

		taCaveLog.append( ReportCaveSurveyStats( caveModel ) );
	}

	/**
	 * Generates LRUD data for stations from splay legs in each series
	 */
	public void generateLRUDFromSplays() {
		//Generate LRUD if model contains data
		if ( ( caveModel != null ) && ( caveModel.isEmpty() == false ) ) {
			caveModel.generateLRUDfromSplays();
			taCaveLog.append( "LRUD data generated from splays in cave model." + CaveConverter.newline);
		}
		else {
			taCaveLog.append( "No data in cave model, so no LRUD generation is possible." + CaveConverter.newline);
		}
	}
	
	/**
	 * Generates new survey series from the names of the stations in the selected series
	 */
	public void generateSeriesFromFullPathStationNames() {
		//Generate LRUD if model contains data
		if ( ( caveModel != null ) && ( caveModel.isEmpty() == false ) ) {
			//Get selected series
			SurveySeries selectedSeries = getSelectedSeries();

			//Process series
			 if ( SurveyProcessing.generateSeriesFromFullPathStationNames(selectedSeries) ) {;
				//Reload model
			 	CaveSurvey model = caveModel;
			 	setCaveModel( model );
			 }
			 else {
				 taCaveLog.append( "No changes were made to the series." + CaveConverter.newline);
			 }
		}
		else {
			taCaveLog.append( "No data in cave model, so no processing is possible." + CaveConverter.newline);
		}
	}

	/**
	 * Returns data file content for a specified survey format for the current cave model
	 * @param outputFormat The format to export
	 * @return Data file content for the model in the specified format
	 */
	public List<String> exportModel( CaveConverter.SurveyDataOutputFormats outputFormat ) {
		List<String> outputData = null;

		if ( outputFormat == SurveyDataOutputFormats.Survex ) {
			//Set options flag for splays (default to true)
			SurvexWriter.SplayFormats outputSplays = SurvexWriter.SplayFormats.Flagged;
			//Generate Survex format data
			SurvexWriter writer = new SurvexWriter(logger);
			outputData = writer.generateSurvexData( caveModel, outputSplays );
		}
		else if ( outputFormat == SurveyDataOutputFormats.Toporobot ) {
			//Set options flag for splays (default to false)
			boolean outputSplays = false;
			//Set date to todays datetime
			Calendar cal = Calendar.getInstance(TimeZone.getDefault());
			Date today = cal.getTime();
			//Generate Toporobot format data
			TopoRobotWriter writer = new TopoRobotWriter(logger);
			outputData = writer.generateToporobotData( caveModel, today, outputSplays );
		}

		return outputData;
	}

	/**
	 * Generates a summary report for the survey
	 * @param survey
	 * @return Report text
	 */
	private String ReportCaveSurveyStats(CaveSurvey survey) {
		String report = "Cave Survey: ";

		if ( survey == null ) {
			report = "No Survey set!";
		}
		else {
			report += survey.getSurveyName() + CaveConverter.newline;

			if ( caveModel.size() == 0 ) {
				report += "No data was loaded into the cave model!" + CaveConverter.newline;
			}
			else {
				report += "Cave model contains " + caveModel.size() + " top level series." + CaveConverter.newline;
				for (int i = 0; i < caveModel.size(); i++ ) {
					report += ReportSeriesStats( caveModel.get(i) );
				}
			}
		}
		
		return report;
	}

	/**
	 * Generates a summary report for the survey series
	 * @param series
	 * @return Report text
	 */
	private String ReportSeriesStats(SurveySeries series) {
		String report = "Series: " + series.getSeriesName() + CaveConverter.newline;
		report += "Date: " + series.getSurveyDate() + CaveConverter.newline;
		report += "Tape units: " + series.getLengthUnit() + CaveConverter.newline;
		report += "Declination: " + series.getDeclination() +  CaveConverter.newline;
		if ( series.getTapeCalibration(LengthUnit.Metres) != 0 ) {
			report += "Tape calibration: " + series.getTapeCalibration( series.getLengthUnit() ) + CaveConverter.newline;
		}
		if ( series.getCompassCalibration(BearingUnit.Degrees) != 0 ) {
			report += "Compass calibration: " + series.getCompassCalibration( series.getBearingUnit() ) + CaveConverter.newline;
		}
		if ( series.getClinoCalibration(GradientUnit.Degrees) != 0 ) {
			report += "Clino calibration: " + series.getClinoCalibration( series.getGradientUnit() ) + CaveConverter.newline;
		}

		if ( series.legCount() > 0 ) {
			report += "Contains " + series.legCount() + " legs. ";
		}
		if ( series.innerSeriesCount() > 0 ) {
			report += "(Contains " + series.innerSeriesCount() + " child series).";
		}
		report += CaveConverter.newline;
		
		//Recursively log details of inner series
		for (int i = 0; i < series.innerSeriesCount(); i++ ) {
			report += ReportSeriesStats( series.getInnerSeries(i) );
		}
		
		return report;
	}

	/**
	 * Generates a summary report for the survey leg
	 * @param leg
	 * @return Report text
	 */
	private String ReportLegStats(SurveyLeg leg) {
		String report = leg.toString();
		
		return report;
	}
	
	/**
	 * Gets the selected series in the tree (if multiple nodes are selected then it gets 
	 * the first one which was selected)
	 * @return The selected series, or null if selected node is not a SurveySeries
	 */
	private SurveySeries getSelectedSeries() {
		SurveySeries selectedSeries = null;

		Object selectedNode = tree.getSelectionPath().getLastPathComponent();

		if (selectedNode instanceof SurveySeries ) {
			selectedSeries  = (SurveySeries) selectedNode;
		}

		return selectedSeries;
	}
}
