/**
 * Copyright (C) 2018 Paul Fretwell - https://github.com/Footleg/caveconverter
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import footleg.cavesurvey.converter.CaveConverter;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataOutputFormats;
import footleg.cavesurvey.converter.DialogMsgLogger;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.reader.CompassParser;
import footleg.cavesurvey.data.reader.DxfParser;
import footleg.cavesurvey.data.reader.PocketTopoParser;
import footleg.cavesurvey.data.reader.SurvexParser;

/**
 * GUI application built on top of CaveConverter library.
 * 
 * @author Footleg
 * @version 2024.09.24 (ISO 8601 YYYY.MM.DD)
 * @since 1.8 (The Java version used)
 * 
 * @to.do
 *        TODO Add option to output anonymous splay stations
 *        TODO Add selection of file character set encoding to preferences
 *        TODO In case of unknown file format on reading, ask user to select the
 *        survey format of the file
 *        TODO Prevent window position from preferences being beyond screen size
 *        when screen resolution is changed
 *        TODO Add selection of font in preferences
 *        TODO Add selection of look and feel in preferences
 */
@SuppressWarnings("serial")
public class MainForm extends JFrame {

	/**
	 * Private class to manage loading, saving and to hold user preferences
	 * 
	 * @author Footleg
	 */
	private class AppPrefs {
		private static final String HEIGHT_PROPERTY = "window.height";
		private static final String WIDTH_PROPERTY = "window.width";
		private static final String SCREENPOSX_PROPERTY = "window.posx";
		private static final String SCREENPOSY_PROPERTY = "window.posy";
		private static final String FONT_PROPERTY = "font.typeface";
		private static final String FONTSIZE_PROPERTY = "font.size";
		private static final String CHARSET_PROPERTY = "character.set";
		private static final String LOOKANDFEEL = "application.skin";
		private static final String LASTFILEOPENPATH = "fileopen.path";
		private Point position;
		private Dimension size;
		private String charset;
		private String fontTypeface;
		private int fontSize;
		private String lookAndFeel;
		private String lastFileOpenPath;

		/**
		 * Initialise preferences data to defaults and attempt to load user preferences
		 * from properties file
		 */
		public AppPrefs() {
			// Set defaults in case app preferences cannot be read from file
			position = new Point(-1, -1);
			size = new Dimension(600, 400);
			charset = "UTF8";
			fontTypeface = "courier";
			fontSize = 12;
			lookAndFeel = "System";
			lastFileOpenPath = ".";

			// Read user preferences from file (if properties file exists)
			try {
				File file = new File(".");
				URL[] urls = { file.toURI().toURL() };
				ClassLoader loader = new URLClassLoader(urls);
				ResourceBundle userPrefs = ResourceBundle.getBundle("app", Locale.getDefault(), loader);
				// Set prefs from properties read from file
				position.setLocation(Integer.parseInt(userPrefs.getString(SCREENPOSX_PROPERTY)),
						Integer.parseInt(userPrefs.getString(SCREENPOSY_PROPERTY)));
				size.setSize(Integer.parseInt(userPrefs.getString(WIDTH_PROPERTY)),
						Integer.parseInt(userPrefs.getString(HEIGHT_PROPERTY)));
				charset = userPrefs.getString(CHARSET_PROPERTY);
				fontTypeface = userPrefs.getString(FONT_PROPERTY);
				fontSize = Integer.valueOf(userPrefs.getString(FONTSIZE_PROPERTY));
				lookAndFeel = userPrefs.getString(LOOKANDFEEL);
				lastFileOpenPath = userPrefs.getString(LASTFILEOPENPATH);
			} catch (Exception e) {
				// Ignore errors in reading prefs
				// JOptionPane.showMessageDialog(null, "Error reading prefs: " + e.getMessage(),
				// "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		public Point getPosition() {
			return position;
		}

		public void setPosition(Point position) {
			this.position = position;
		}

		public Dimension getSize() {
			return size;
		}

		public void setSize(int width, int height) {
			size.setSize(width, height);
		}

		public String getCharset() {
			return charset;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}

		public String getFontTypeface() {
			return fontTypeface;
		}

		public void setFontTypeface(String fontTypeface) {
			this.fontTypeface = fontTypeface;
		}

		public int getFontSize() {
			return fontSize;
		}

		public void setFontSize(int fontSize) {
			this.fontSize = fontSize;
		}

		public String getLookAndFeel() {
			return lookAndFeel;
		}

		public void setLookAndFeel(String lookAndFeel) {
			this.lookAndFeel = lookAndFeel;
		}

		public String getLastFileOpenPath() {
			return lastFileOpenPath;
		}

		public void setLastFileOpenPath(String lastFileOpenPath) {
			this.lastFileOpenPath = lastFileOpenPath;
		}

		/**
		 * Write application preference data to a properties file
		 */
		public void writePrefsToFile() {
			// Create prefs file text
			String prefsData = "";
			prefsData += SCREENPOSX_PROPERTY + "=" + (int) position.getX() + CaveConverter.newline;
			prefsData += SCREENPOSY_PROPERTY + "=" + (int) position.getY() + CaveConverter.newline;
			prefsData += HEIGHT_PROPERTY + "=" + (int) size.getHeight() + CaveConverter.newline;
			prefsData += WIDTH_PROPERTY + "=" + (int) size.getWidth() + CaveConverter.newline;
			prefsData += CHARSET_PROPERTY + "=" + getCharset() + CaveConverter.newline;
			prefsData += FONT_PROPERTY + "=" + getFontTypeface() + CaveConverter.newline;
			prefsData += FONTSIZE_PROPERTY + "=" + getFontSize() + CaveConverter.newline;
			prefsData += LOOKANDFEEL + "=" + getLookAndFeel() + CaveConverter.newline;
			prefsData += LASTFILEOPENPATH + "=" + getLastFileOpenPath() + CaveConverter.newline;

			// Save File
			FileWriter textWriter = null;
			try {
				textWriter = new FileWriter("./app.properties");
				textWriter.write(prefsData);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error writing user preferences file: " + e.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				if (textWriter != null) {
					try {
						textWriter.close();
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(null, "Error closing user preferences file: " + ex.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	/**
	 * Private action class to handle file open operations
	 * 
	 * @author Footleg
	 */
	private class OpenFileAction extends AbstractAction {
		private JFileChooser fileOpenDialog;

		public OpenFileAction(String name, Icon icon) {
			super(name, icon);

			// Initialise file open dialog
			fileOpenDialog = new JFileChooser(appPrefs.getLastFileOpenPath());
			fileOpenDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileOpenDialog.addChoosableFileFilter(new FileNameExtensionFilter("Compass Data Files", "dat"));
			fileOpenDialog.addChoosableFileFilter(new FileNameExtensionFilter("DXF Files", "dxf"));
			fileOpenDialog.addChoosableFileFilter(new FileNameExtensionFilter("PocketTopo Text Files", "txt"));
			fileOpenDialog.addChoosableFileFilter(new FileNameExtensionFilter("Survex Files", "svx"));
		}

		@Override
		public void actionPerformed(ActionEvent evnt) {
			// Show file open dialog for user to pick input file
			fileOpenDialog.showOpenDialog(getMainFrame());
			if (fileOpenDialog.getSelectedFile() != null) {
				// Check if this is the first data file to be opened
				if (inputData.getFormat() == null) {
					// Add tab to tab pane
					inputData.title = "Survey Input Data";
					tabPane.addTab(inputData.title, inputData.getTabPaneComponent());
				}
				// Open file in inputData tab
				boolean res = inputData.openFile(fileOpenDialog.getSelectedFile(), appPrefs.getCharset());
				// If data was loaded successfully
				if (res) {
					// Enable actions which can act on input data
					saveFileAction.setEnabled(true);
					saveAsFileAction.setEnabled(true);
					parseInputDataAction.setEnabled(true);
					// Store the path of opened file
					String fileOpenPath = fileOpenDialog.getSelectedFile().getPath();
					fileOpenPath = fileOpenPath.replace("\\", "/");
					String folderOnlyPath = fileOpenPath.substring(0, fileOpenPath.lastIndexOf('/'));
					appPrefs.lastFileOpenPath = folderOnlyPath;
					// Automatically process the data
					buildCaveModelFromInputData();
				}
			}
		}
	}

	/**
	 * Private action class to handle file save operations
	 * 
	 * @author Footleg
	 */
	private class SaveFileAction extends AbstractAction {

		public SaveFileAction(String name, Icon icon) {
			super(name, icon);
			// Disabled by default (enables when data is loaded)
			enabled = false;
		}

		@Override
		public void actionPerformed(ActionEvent evnt) {
			// Get selected tab
			SurveyDatafileTabPane fileContentTab = getActiveSurveyDataTabPane();

			if (fileContentTab != null) {
				boolean saveAs = false;

				// Determine action to perform
				if (evnt.getActionCommand().equals(saveAsActionCmd)) {
					saveAs = true;
				}
				// Show file save dialog for user to specify file path
				fileContentTab.saveData(saveAs, appPrefs.getCharset());
			} else {
				JOptionPane.showMessageDialog(getMainFrame(),
						"No data to save. Select a survey data file tab before selecting save.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Private action class to read input survey data to generate a cave model in
	 * the visualiser
	 * 
	 * @author Footleg
	 */
	private class ParseInputDataAction extends AbstractAction {
		public ParseInputDataAction(String name, Icon icon) {
			super(name, icon);
			// Disabled by default (enables when data is loaded)
			enabled = false;
		}

		@Override
		public void actionPerformed(ActionEvent evnt) {
			// Process the data in the input data tab and generate a model
			buildCaveModelFromInputData();
		}
	}

	/**
	 * Private action class to generate LRUD data in cave model from splays
	 * 
	 * @author Footleg
	 */
	private class GenerateLRUDAction extends AbstractAction {
		public GenerateLRUDAction(String name, Icon icon) {
			super(name, icon);
			// Disabled by default (enables when data is loaded)
			enabled = false;
		}

		@Override
		public void actionPerformed(ActionEvent evnt) {
			// Process the data in the input data tab and generate a model
			if (visualiser != null) {
				visualiser.generateLRUDFromSplays();
			}
		}
	}

	/**
	 * Private action class to generate new nested series in cave model from
	 * stations named using full paths
	 * 
	 * @author Footleg
	 */
	private class GenerateSeriesFromStnNamesAction extends AbstractAction {
		public GenerateSeriesFromStnNamesAction(String name, Icon icon) {
			super(name, icon);
			// Disabled by default (enables when data is loaded)
			enabled = false;
		}

		@Override
		public void actionPerformed(ActionEvent evnt) {
			// Process selected series
			if (visualiser != null) {
				visualiser.generateSeriesFromFullPathStationNames();
			}
		}
	}

	/**
	 * Private action class to handle Survex export operation
	 * 
	 * @author Footleg
	 */
	private class ExportSurvexDataAction extends AbstractAction {
		public ExportSurvexDataAction(String name, Icon icon) {
			super(name, icon);
			// Disabled by default (enables when data is loaded)
			enabled = false;
		}

		@Override
		public void actionPerformed(ActionEvent evnt) {
			// Process the data in the input data tab and generate a model
			generateOutputData(SurveyDataOutputFormats.Survex);
		}
	}

	/**
	 * Private action class to handle Toporobot export operation
	 * 
	 * @author Footleg
	 */
	private class ExportToporobotDataAction extends AbstractAction {
		public ExportToporobotDataAction(String name, Icon icon) {
			super(name, icon);
			// Disabled by default (enables when data is loaded)
			enabled = false;
		}

		@Override
		public void actionPerformed(ActionEvent evnt) {
			// Process the data in the input data tab and generate a model
			generateOutputData(SurveyDataOutputFormats.Toporobot);
		}
	}

	// /**
	// * Private action class to handle operations
	// * @author Footleg
	// */
	// private class NewAction extends AbstractAction {
	// public NewAction(String name, Icon icon) {
	// super(name, icon);
	// //Disabled by default (enables when data is loaded)
	// enabled = false;
	// }
	//
	// @Override
	// public void actionPerformed(ActionEvent evnt) {
	//
	// }
	// }

	/**
	 * Private listener for SurveyDatafileTabPane modified events to indicate
	 * modified state in tab title
	 */
	private PropertyChangeListener dataModified = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			SurveyDatafileTabPane source = (SurveyDatafileTabPane) evt.getSource();
			// Indicate modified state in tab title
			int idx = getTabIndexForObject(source);
			if (idx >= 0) {
				if ((Boolean) evt.getNewValue()) {
					tabPane.setTitleAt(idx, source.title + "(*)");
				} else {
					tabPane.setTitleAt(idx, source.title);
				}
			}
		}
	};

	// Themes
	private static final String[][] THEMES = {
			{ "Metal", "javax.swing.plaf.metal.MetalLookAndFeel" },
			{ "Ocean", "javax.swing.plaf.metal.MetalLookAndFeel" },
			{ "Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel" },
			{ "System", UIManager.getSystemLookAndFeelClassName() },
			{ "CrossPlafrom", UIManager.getCrossPlatformLookAndFeelClassName() },
			{ "GTK", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel" } };

	// Action name constants
	private static final String saveActionCmd = "Save";
	private static final String saveAsActionCmd = "SaveAs";

	// UI Elements
	private JTabbedPane tabPane;
	private Font font;
	private OpenFileAction openFileAction;
	private SaveFileAction saveFileAction;
	private SaveFileAction saveAsFileAction;
	private ParseInputDataAction parseInputDataAction;
	private GenerateLRUDAction generateLRUDAction;
	private GenerateSeriesFromStnNamesAction genSeriesFromStnNamesAction;
	private ExportSurvexDataAction exportSurvexDataAction;
	private ExportToporobotDataAction exportToporobotDataAction;

	// Data Objects
	private AppPrefs appPrefs;
	private SourceDataComponent inputData;
	private CaveModelVisualiser visualiser;
	private DialogMsgLogger dialogLogger;

	/**
	 * Create main application window
	 */
	public MainForm() {
		// Create application preferences manager first, as initialisation of UI depends
		// on preference values
		appPrefs = new AppPrefs();

		// Set look and feel
		initLookAndFeel(appPrefs.getLookAndFeel());

		// Create dialog logger
		dialogLogger = new DialogMsgLogger(this);

		// Set up properties from prefs
		font = new Font(appPrefs.getFontTypeface(), Font.PLAIN, appPrefs.getFontSize());

		// Create action class instances here after look and feel is configured, or UI
		// elements (JFileChoosers)
		// do not pick up look and feel
		openFileAction = new OpenFileAction("Open", createImageIcon("images/fileopen.png"));
		saveFileAction = new SaveFileAction("Save", createImageIcon("images/filesave.png"));
		saveAsFileAction = new SaveFileAction("Save As", createImageIcon("images/filesave.png"));
		parseInputDataAction = new ParseInputDataAction("Build Cave Model", createImageIcon("images/process_data.png"));
		generateLRUDAction = new GenerateLRUDAction("Generate LRUD", createImageIcon("images/lrud.png"));
		genSeriesFromStnNamesAction = new GenerateSeriesFromStnNamesAction("Generate Series From Station Names",
				createImageIcon("images/expand-series.png"));
		exportSurvexDataAction = new ExportSurvexDataAction("Export to Survex",
				createImageIcon("images/export_survex_data.png"));
		exportToporobotDataAction = new ExportToporobotDataAction("Export to Toporobot",
				createImageIcon("images/export_toporobot_data.png"));

		// Create application UI
		initializeUI();
	}

	/**
	 * Getter for the main frame reference, for use in sub classes where 'this'
	 * cannot be used
	 * 
	 * @return Returns the main frame reference from any within any sub class
	 *         context
	 */
	public MainForm getMainFrame() {
		return this;
	}

	public DialogMsgLogger getLogger() {
		return dialogLogger;
	}

	/**
	 * Getter for selected survey data tab if one is active.
	 * 
	 * @return The active tab pane if it is a Survey data file tab, otherwise
	 *         returns null
	 */
	public SurveyDatafileTabPane getActiveSurveyDataTabPane() {
		// Get selected tab
		SurveyDatafileTabPane fileContentTab = null;

		if (tabPane.getTabCount() > 0) {
			try {
				JScrollPane selectedTab = (JScrollPane) tabPane.getComponentAt(tabPane.getSelectedIndex());
				fileContentTab = (SurveyDatafileTabPane) selectedTab.getViewport().getView();
			} catch (ClassCastException e) {
				fileContentTab = null;
			}
		}
		return fileContentTab;
	}

	/**
	 * Determines the index in the tabPane of the tab holding the specified object
	 * 
	 * @param matchObj Object to match in the tabs
	 * @return The index of the tab pane matching the object, or -1 if there is no
	 *         matching tab
	 */
	public int getTabIndexForObject(SurveyDatafileTabPane matchObj) {
		int idx = -1;

		for (int i = 0; i < tabPane.getTabCount(); i++) {
			if (tabPane.getComponentAt(i) == matchObj.getTabPaneComponent()) {
				idx = i;
				break;
			}
		}
		return idx;
	}

	/**
	 * Create UI elements
	 */
	private void initializeUI() {
		// Pick up version number and display in title bar
		ResourceBundle resourceString = ResourceBundle.getBundle("footleg.cavesurvey.gui.app");
		String version = resourceString.getString("build.version");
		setTitle("Footleg's Cave Converter - " + version);

		// Set window size and position from preferences
		setSize(appPrefs.getSize());
		if (appPrefs.getPosition().getX() < 0) {
			setLocationRelativeTo(null);
		} else {
			setLocation(appPrefs.getPosition());
		}

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(
				new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent windowEvent) {
						boolean close = true;

						// Prompt to save changes for any modified tab panes
						for (int i = 0; i < tabPane.getTabCount(); i++) {
							if (tabPane.getComponentAt(i) instanceof JScrollPane) {
								JScrollPane pane = (JScrollPane) tabPane.getComponentAt(i);
								if (pane.getViewport().getView() instanceof SurveyDatafileTabPane) {
									// Make this pane the selected one
									tabPane.setSelectedComponent(pane);
									// Call method to prompt to save changes if modified
									SurveyDatafileTabPane filePane = (SurveyDatafileTabPane) pane.getViewport()
											.getView();
									int res = filePane.promptToSaveChanges(appPrefs.getCharset());
									if (res == JFileChooser.CANCEL_OPTION) {
										// User cancelled, so abort closing of application
										close = false;
										break;
									}
								}
							}
						}

						if (close) {
							// Update preferences with window size and position
							Point pos = windowEvent.getWindow().getLocationOnScreen();
							appPrefs.setSize(windowEvent.getWindow().getWidth(), windowEvent.getWindow().getHeight());
							appPrefs.setPosition(pos);
							appPrefs.writePrefsToFile();

							// Close window to exit application
							dispose();
						} else {
							// Ensure window is made visible
							setVisible(true);
						}
					}
				});

		createMenuBar();

		// Set up tab pane
		tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		add(tabPane, BorderLayout.CENTER);
		tabPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Enable actions appropriate to selected tab type
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane tp = (JTabbedPane) e.getSource();
					int selIdx = tp.getSelectedIndex();
					if (tp.getComponentAt(selIdx) instanceof JScrollPane) {
						JScrollPane pane = (JScrollPane) tp.getComponentAt(selIdx);
						if (pane.getViewport().getView() instanceof SurveyDatafileTabPane) {
							saveFileAction.setEnabled(true);
							saveAsFileAction.setEnabled(true);
						} else {
							saveFileAction.setEnabled(false);
							saveAsFileAction.setEnabled(false);
						}
					} else if (tp.getComponentAt(selIdx) instanceof JSplitPane) {
						JSplitPane pane = (JSplitPane) tp.getComponentAt(selIdx);
						if (pane instanceof CaveModelVisualiser) {
							// Nothing to save directly from cave model pane
							saveFileAction.setEnabled(false);
							saveAsFileAction.setEnabled(false);
						} else {
							// No other pane types yet, so disable save for when there are
							saveFileAction.setEnabled(false);
							saveAsFileAction.setEnabled(false);
						}
					}
				}
			}
		});

		// Create input data tab pane (but do not add to tab pane here, that is done
		// when a file is opened
		inputData = new SourceDataComponent(font, this.getLogger());
		// Add modified listener to tab pane
		inputData.addPropertyChangeListener("modified", dataModified);
	}

	// JOptionPane.showMessageDialog(getMainFrame(), "Modified property changed to:
	// " + inputData.isModified(),
	// "Debug", JOptionPane.OK_OPTION);

	/**
	 * Create the main form menus and toolbar
	 */
	private void createMenuBar() {
		JMenuBar menubar = new JMenuBar();
		JToolBar toolbar = new JToolBar();

		// File Menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menubar.add(fileMenu);

		// Define Open item
		createMenuItemAndToolbarBtn(fileMenu, toolbar, openFileAction, KeyEvent.VK_O, "Open file", "");

		// Define Save item
		createMenuItemAndToolbarBtn(fileMenu, toolbar, saveFileAction, KeyEvent.VK_S, "Save file", saveActionCmd);
		saveFileAction.setEnabled(false);

		// Define Save As item
		createMenuItem(fileMenu, saveAsFileAction, KeyEvent.VK_A, "Save file with a new name", saveAsActionCmd);

		// Define Exit item
		JMenuItem eMenuItem = new JMenuItem("Exit", createImageIcon("images/exit.png"));
		eMenuItem.setMnemonic(KeyEvent.VK_X);
		eMenuItem.setToolTipText("Exit application");
		eMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Trigger closing of application
				closeApp();
			}
		});
		// Add to File menu
		fileMenu.add(eMenuItem);

		// Data Menu
		JMenu dataMenu = new JMenu("Data");
		dataMenu.setMnemonic(KeyEvent.VK_D);
		menubar.add(dataMenu);

		// Define Process Data item
		createMenuItemAndToolbarBtn(dataMenu, toolbar, parseInputDataAction, KeyEvent.VK_P, "Process Survey Data", "");

		// Define LRUD item
		createMenuItemAndToolbarBtn(dataMenu, toolbar, generateLRUDAction, KeyEvent.VK_L,
				"Generate LRUD data from splays", "");

		// Define Generate Series from Stn Names item
		createMenuItemAndToolbarBtn(dataMenu, toolbar, genSeriesFromStnNamesAction, KeyEvent.VK_G,
				"Generate new series using station names", "");

		// Define ConvertToSurvex item
		createMenuItemAndToolbarBtn(dataMenu, toolbar, exportSurvexDataAction, KeyEvent.VK_U,
				"Export data model to Survex format", "");

		// Define ConvertToToporobot item
		createMenuItemAndToolbarBtn(dataMenu, toolbar, exportToporobotDataAction, KeyEvent.VK_T,
				"Export data model to Toporobot format", "");

		// Add menu bar and toolbar to frame
		setJMenuBar(menubar);
		add(toolbar, BorderLayout.NORTH);
	}

	/**
	 * Sets the look and feel from one of the supported styles
	 * 
	 * @param lookAndFeel
	 * @param theme
	 */
	private static void initLookAndFeel(String lookAndFeel) {
		String themeClass = "";

		// Look up this LookAndFeel name in the themes array to get class name and theme
		for (int i = 0; i < THEMES.length; i++) {
			if (lookAndFeel.compareTo(THEMES[i][0]) == 0) {
				// Use this look and feel item
				themeClass = THEMES[i][1];
				break;
			}
		}

		// If look and feel class name was not found then use system default
		if (themeClass.equals("")) {
			themeClass = UIManager.getSystemLookAndFeelClassName();
		}

		// Set look and feel ;
		try {
			// Set the theme for the themed Default Metal look and feels
			if (lookAndFeel.equals("Metal")) {
				MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
			} else if (lookAndFeel.equals("Ocean")) {
				MetalLookAndFeel.setCurrentTheme(new OceanTheme());
			}
			UIManager.setLookAndFeel(themeClass);

			// Make sure we have nice window decorations.
			JFrame.setDefaultLookAndFeelDecorated(true);
		}

		catch (ClassNotFoundException e) {
			System.err.println("Couldn't find class for specified look and feel:"
					+ lookAndFeel);
			System.err.println("Did you include the L&F library in the class path?");
			System.err.println("Using the default look and feel.");
		}

		catch (UnsupportedLookAndFeelException e) {
			System.err.println("Can't use the specified look and feel ("
					+ lookAndFeel
					+ ") on this platform.");
			System.err.println("Using the default look and feel.");
		}

		catch (Exception e) {
			System.err.println("Couldn't get specified look and feel ("
					+ lookAndFeel
					+ "), for some reason.");
			System.err.println("Using the default look and feel.");
			e.printStackTrace();
		}
	}

	/**
	 * Helper method to add menu item and toolbar button for an action to the UI
	 * 
	 * @param parentMenu  Menu to create the new menu item under
	 * @param toolbar     Toolbar to create the new button on
	 * @param action      Action to associate with new menu item and toolbar button
	 * @param shortcutKey Keyboard shortcut to associate with the menu
	 * @param toolTipText Tooltop text to associate with the new menu item and
	 *                    toolbar button
	 */
	private void createMenuItemAndToolbarBtn(JMenu parentMenu, JToolBar toolbar, Action action, int shortcutKey,
			String toolTipText, String actionCommand) {
		createMenuItem(parentMenu, action, shortcutKey, toolTipText, actionCommand);
		JButton btn = toolbar.add(action);
		btn.setToolTipText(toolTipText);
		btn.setActionCommand(actionCommand);
	}

	/**
	 * Helper method to add menu item for an action to the UI
	 * 
	 * @param parentMenu  Menu to create the new menu item under
	 * @param action      Action to associate with new menu item
	 * @param shortcutKey Keyboard shortcut to associate with the menu
	 * @param toolTipText Tooltop text to associate with the new menu item
	 */
	private void createMenuItem(JMenu parentMenu, Action action, int shortcutKey, String toolTipText,
			String actionCommand) {
		JMenuItem menuItem = parentMenu.add(action);
		menuItem.setMnemonic(shortcutKey);
		menuItem.setToolTipText(toolTipText);
		menuItem.setActionCommand(actionCommand);
	}

	/**
	 * Close application in a controlled manner, allowing preferences and data to be
	 * saved
	 */
	private void closeApp() {
		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Generate survey data from the cave data model in the specified output format
	 */
	private void generateOutputData(CaveConverter.SurveyDataOutputFormats format) {
		if (visualiser != null) {
			List<String> outputData = visualiser.exportModel(format);

			// Create tabPane for this output data
			SurveyDatafileTabPane exportTab = new ExportDataComponent(font, this.getLogger(), format);
			if (format == SurveyDataOutputFormats.Survex) {
				exportTab.title = "Survex Export Data";
			} else if (format == SurveyDataOutputFormats.Toporobot) {
				exportTab.title = "Toporobot Export Data";
			}

			tabPane.addTab(exportTab.title, exportTab.getTabPaneComponent());
			exportTab.addPropertyChangeListener("modified", dataModified);

			// Write strings from list to the file
			exportTab.setText("");
			Iterator<String> iterator = outputData.listIterator();
			while (iterator.hasNext()) {
				String line = iterator.next();
				if (iterator.hasNext()) {
					exportTab.append(line + System.getProperty("line.separator"));
				} else {
					exportTab.append(line);
				}
			}
			// Bring to front
			tabPane.setSelectedComponent(exportTab.getTabPaneComponent());
		}
	}

	/**
	 * Process the data in the input data tab and generate a model
	 */
	private void buildCaveModelFromInputData() {
		CaveSurvey dataModel = processInputData();

		// Create or update model tab if data was loaded
		if ((dataModel != null) && (dataModel.isEmpty() == false)) {
			// Reuse tab if already exists
			if (visualiser == null) {
				visualiser = new CaveModelVisualiser(this, dataModel, font);
				tabPane.addTab("Survey Data Model", visualiser.getTabPaneComponent());
			} else {
				// Update cave model in visualiser
				visualiser.setCaveModel(dataModel);
			}
			// Bring to front
			tabPane.setSelectedComponent(visualiser.getTabPaneComponent());
			// Enable actions which can act on cave model
			generateLRUDAction.setEnabled(true);
			exportSurvexDataAction.setEnabled(true);
			exportToporobotDataAction.setEnabled(true);
		}
	}

	/**
	 * Sets the enabled state of all actions which require a survey series
	 * 
	 * @param enabled State to set the enabled status to
	 */
	public void setSurveySeriesActionsEnabled(boolean enabled) {
		genSeriesFromStnNamesAction.setEnabled(enabled);
	}

	/**
	 * Parse the cave survey data in an input data tab into a cave data model
	 * 
	 * @return Cave data model generated from parsed data file
	 */
	private CaveSurvey processInputData() {
		CaveSurvey surveyData = null;

		// Get text data from input tab
		List<String> surveyDataLines = getSurveyDataFromText(inputData.getText());

		// Determine data type from input tab
		if (inputData.getFormat() == null) {
			// TODO In case of unknown format, ask user to select the format
			JOptionPane.showMessageDialog(this,
					"Format of data unknown. Currently this is determined by the file extension. The option to let you specify the format is not written yet.",
					"Unknown Survey Data Format", JOptionPane.OK_OPTION);
		}
		if (inputData.getFormat() != null) {
			// Parse the data into the cave data model
			switch (inputData.getFormat()) {
				case Compass:
					// Parse Compass data
					CompassParser cParser = new CompassParser(dialogLogger);
					try {
						surveyData = cParser.parseFile(surveyDataLines);
					} catch (ParseException e) {
						// Display error message
						JOptionPane.showMessageDialog(this, "Error processing Compass file data: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this,
								"Unexpected error processing Compass file data: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					break;

				case DXF:
					// Parse DXF data
					DxfParser dParser = new DxfParser(dialogLogger);
					try {
						surveyData = dParser.parseFile(surveyDataLines, 0);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this,
								"Unexpected error processing DXF file data: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					break;

				case PocketTopo:
					// Parse PocketTopo data
					PocketTopoParser pParser = new PocketTopoParser(dialogLogger);
					try {
						surveyData = pParser.parseFile(surveyDataLines);
						// Generate LRUD from splays by default for PocketTopo
						// CaveConverter.logMessage("Generating LRUD data from splays...");
						surveyData.generateLRUDfromSplays();
					} catch (ParseException e) {
						// Display error message
						JOptionPane.showMessageDialog(this, "Error processing PocketTopo file data: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this,
								"Unexpected error processing PocketTopo file data: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					break;

				case Survex:
					// Parse Survex data
					SurvexParser sParser = new SurvexParser(dialogLogger);
					try {
						surveyData = sParser.parseFile(surveyDataLines, null);
					} catch (ParseException e) {
						// Display error message
						JOptionPane.showMessageDialog(this, "Error processing Survex file data: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(this,
								"Unexpected error processing Survex file data: " + e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					break;

			}

		}

		return surveyData;
	}

	/**
	 * Returns an ImageIcon, or null if the path was invalid
	 * 
	 * @param path Path to the icon image relative to this class
	 * @return ImageIcon using the image at the path specified
	 */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = MainForm.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Failed to find icon file: " + path);
			return null;
		}
	}

	/**
	 * @param args Command line arguments (ignored)
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				MainForm mainWindow = new MainForm();
				mainWindow.setVisible(true);
			}
		});
	}

	private List<String> getSurveyDataFromText(String text) {
		List<String> dataLines = new ArrayList<String>();
		String[] parts = text.split("\n");

		for (int i = 0; i < parts.length; i++) {
			dataLines.add(parts[i]);
		}

		return dataLines;
	}

}
