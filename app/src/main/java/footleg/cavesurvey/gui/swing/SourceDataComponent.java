/**
 * Copyright (C) 2015-2017 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
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

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JFileChooser;

import footleg.cavesurvey.converter.CaveConverter;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataInputFormats;
import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Component to display and hold cave survey source data for conversions and editing.
 *  
 * @author      Footleg
 * @version     2017.01.10                                (ISO 8601 YYYY.MM.DD)
 * @since       1.7                                       (The Java version used)
 * 
 * @to.do
 * TODO Add highlighting of lines in file which parser could not interpret
 */
@SuppressWarnings("serial")
public class SourceDataComponent extends SurveyDatafileTabPane {
	private CaveConverter.SurveyDataInputFormats format;
	private List<String> multifileLineRefs;
	
	/**
	 * Create TextArea scrollable tab instance with specified font
	 * @param font The font to use to display survey data file contents
	 * @param  logger Logging class to output information, warning and error messages to
	 */
	public SourceDataComponent(Font font, Logger logger) {
        super( font, logger );
	}

	/**
	 * @return the format
	 */
	public CaveConverter.SurveyDataInputFormats getFormat() {
		return format;
	}

	/**
	 * Opens a new survey data input file and loads the contents into a source data tab pane
	 * @param dataFile The file to open
	 * @param characterSetEncoding The character set encoding to use for the file being read (e.g. UTF8, Cp1252 (for ANSI) )
	 * @return Returns true if file was opened successfully
	 */
	public boolean openFile(File dataFile, String characterSetEncoding ) {
		boolean res = false;
		//Check whether currently loaded data has been modified
		int action = promptToSaveChanges( characterSetEncoding );
  		
		//Load new data if prompt check passed
		if ( action == JFileChooser.APPROVE_OPTION ) {
			//Reset fields before attempting to load new data
			setText("");
			format = null;

			//Determine file type from file extension
			boolean multiFile = false;
			String fileName = dataFile.getName();
			int extnStart = fileName.lastIndexOf('.') + 1;
			String fileExtn = "";
			if ( extnStart > 0 ) {
				fileExtn = fileName.substring( extnStart );
			}
			format = CaveConverter.inputFormatFromFileExtn( fileExtn );
			//Set file filter appropriate to file format (this instance of the file 
			//save dialog is specific to this class instance, so this can be set here)
			if ( format == SurveyDataInputFormats.Compass ) {
				fileSaveDialog.setFileFilter( compassFileFilter );
			}
			else if ( format == SurveyDataInputFormats.DXF ) {
				fileSaveDialog.setFileFilter( dxfFileFilter );
			}
			else if ( format == SurveyDataInputFormats.PocketTopo ) {
				fileSaveDialog.setFileFilter( pocketTopoFileFilter );
			}
			else if ( format == SurveyDataInputFormats.Survex ) {
				fileSaveDialog.setFileFilter( survexFileFilter );
				multiFile = true;
				multifileLineRefs = new ArrayList<String>();
			}

			//Read data from file
			List<String> fileData = 
					UtilityFunctions.readTextFile(dataFile, characterSetEncoding, multiFile, multifileLineRefs, logger);

			//Put data into tab
			ListIterator<String> dataIter = fileData.listIterator();
			while ( dataIter.hasNext() ) {
				append(dataIter.next() + "\n");
			}

			//Store filename in class property
			loadedFile = dataFile;
			//Reset modified flag now file loading is complete
			setModified( false );
			res = true;

		}
		return res;
	}
	
}
