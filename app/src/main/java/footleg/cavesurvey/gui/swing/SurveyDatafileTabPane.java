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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import footleg.cavesurvey.converter.Logger;

/**
 * Component to display and hold cave survey data.
 *  
 * @author      Footleg
 * @version     2017.01.10                                (ISO 8601 YYYY.MM.DD)
 * @since       1.7                                       (The Java version used)
 * 
 * @to.do
 * TODO The tab should indicate it's role (as the source data) by colour or other visual indicator.
 */
@SuppressWarnings("serial")
public class SurveyDatafileTabPane extends JTextArea {
	private JScrollPane scrollPane;
	protected JFileChooser fileSaveDialog;
	protected FileNameExtensionFilter compassFileFilter = new FileNameExtensionFilter("Compass Data Files","dat");
	protected FileNameExtensionFilter dxfFileFilter = new FileNameExtensionFilter("DXF Files","dxf");
	protected FileNameExtensionFilter pocketTopoFileFilter = new FileNameExtensionFilter("PocketTopo Text Files","txt");
	protected FileNameExtensionFilter survexFileFilter = new FileNameExtensionFilter("Survex Files","svx");
	protected FileNameExtensionFilter toporobotFileFilter = new FileNameExtensionFilter("Toporobot Data Files","text");
	private boolean modified;
	protected String title = "";
	protected File loadedFile;
	protected Logger logger;
	
	public boolean isModified() {
		return modified;
	}

	protected void setModified(boolean modified) {
		boolean oldModified = this.modified;
		this.modified = modified;
        firePropertyChange( "modified", oldModified, modified );		
	}

	/**
	 * Create TextArea scrollable tab instance with specified font
	 * @param font The font to use to display survey data file contents
	 * @param  logger Logging class to output information, warning and error messages to
	 */
	public SurveyDatafileTabPane(Font font, Logger logger) {
		super();
		this.setFont(font);
		this.logger = logger;
		modified = false;
		scrollPane = new JScrollPane (this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(this);

		//Add listener for document change events
		this.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				//Update modified state
				setModified(true);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				//Update modified state
				setModified(true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				//Update modified state
				setModified(true);
			}
		});
		
		//Initialise file save dialog
		fileSaveDialog = new JFileChooser(".");
		fileSaveDialog.setFileSelectionMode( JFileChooser.FILES_ONLY );
		fileSaveDialog.addChoosableFileFilter( compassFileFilter );
		fileSaveDialog.addChoosableFileFilter( dxfFileFilter );
		fileSaveDialog.addChoosableFileFilter( pocketTopoFileFilter );
		fileSaveDialog.addChoosableFileFilter( survexFileFilter );
		fileSaveDialog.addChoosableFileFilter( toporobotFileFilter );
	}

	/**
	 * @return the scrollPane
	 */
	public JScrollPane getTabPaneComponent() {
		return scrollPane;
	}

	/**
	 * Prompts user to save changes if data has been modified. Returns a JFileChooser option value.
	 * @param characterSetEncoding The character set encoding to use for the file being written (e.g. UTF8, Cp1252 (for ANSI) )
	 * @return APPROVE_OPTION if saved, ERROR_OPTION if failed, CANCEL_OPTION if user aborted save
	 */
	public int promptToSaveChanges(String characterSetEncoding) {
		int action = JOptionPane.NO_OPTION;
		int saveRes = JFileChooser.APPROVE_OPTION;
		
		//Check whether currently loaded data has been modified
		if ( isModified() ) {
			//Ask whether to save before loading new data
			action = JOptionPane.showConfirmDialog( this.getParent(), "Save changes to survey data file " + title + "?",
                    "Save changes?", JOptionPane.YES_NO_CANCEL_OPTION);
            if ( action == JOptionPane.YES_OPTION ) {
            	//Save file before proceeding
            	saveRes = saveData( false, characterSetEncoding );
            }
            else if ( action == JOptionPane.CANCEL_OPTION ) {
            	//Change result to Cancel if user cancelled save 
            	saveRes = JFileChooser.CANCEL_OPTION;
            }
		}
		
		return saveRes;
	}
	
	/**
	 * Save survey data to file. Returns JFileChooser Option responses (APPROVE_OPTION, ERROR_OPTION, CANCEL_OPTION)
	 * @param saveAs If true then show file chooser even if a filename is already set
	 * @param characterSetEncoding The character set encoding to use for the file being written (e.g. UTF8, Cp1252 (for ANSI) )
	 * @return Returns JFileChooser Option responses: APPROVE_OPTION if saved, ERROR_OPTION if failed, CANCEL_OPTION if user aborted save
	 */
	public int saveData( boolean saveAs, String characterSetEncoding ) {
		int action = JFileChooser.APPROVE_OPTION;
		File saveFile = loadedFile;
		
		if ( saveAs || loadedFile == null ) {
			//Set file chooser to current filename
			fileSaveDialog.setSelectedFile( loadedFile );
			//Enable new filename to be specified
			boolean overwriteCheckDone = false;
			while ( overwriteCheckDone == false ) {
		    	//Show file save dialog for user to specify file path
				action = fileSaveDialog.showSaveDialog( this );
				if ( action == JFileChooser.APPROVE_OPTION ) {
					//Check whether specified file already exists and warn of overwrite
					int overwriteConfirm = JOptionPane.YES_OPTION;
					if ( fileSaveDialog.getSelectedFile().exists() ) {
						overwriteConfirm = JOptionPane.showConfirmDialog( this.getParent(), 
								"File " + fileSaveDialog.getSelectedFile().getPath() + " already exists. Do you want to replace this file?",
			                    "Overwrite file?", JOptionPane.YES_NO_OPTION);
					}
					if ( overwriteConfirm == JOptionPane.YES_OPTION ) {
						saveFile = fileSaveDialog.getSelectedFile();
						overwriteCheckDone = true;
					}
				}
				else {
					overwriteCheckDone = true;
				}
			}
		}
		
    	if ( action == JFileChooser.APPROVE_OPTION ) {
 			//Save File
    		BufferedWriter textWriter = null;
            try {
    			FileOutputStream fos = new FileOutputStream( saveFile );
    	        Writer out = new OutputStreamWriter(fos, characterSetEncoding);
    	        textWriter = new BufferedWriter(out);
                textWriter.write( this.getText() );
                //Update file reference for new saved file
                loadedFile = saveFile;
                //Reset modified status
                setModified( false );
             } 
            catch (Exception e) {
                JOptionPane.showMessageDialog( this, "Error writing file: " + e.getMessage(),
                      "Error", JOptionPane.ERROR_MESSAGE);
        		action = JFileChooser.ERROR_OPTION; 
             }
    		finally {
    			if ( textWriter != null ) {
    				try {
    					textWriter.close();
    				}
    				catch (IOException ex) {
    	                JOptionPane.showMessageDialog( this, "Error closing file.",
    	                        "Error", JOptionPane.ERROR_MESSAGE);
    	        		action = JFileChooser.ERROR_OPTION; 
    	            }
    			}
    		}
    	}
    	return action;
	}
	
}
