/**
 * Copyright (C) 2017 Paul Fretwell - aka 'Footleg' (drfootleg@gmail.com)
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
package footleg.cavesurvey.converter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Logging class for Swing GUI application. Displays error messages in dialog boxes.
 *  
 * @author      Footleg
 * @version     2017.01.10                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 */
public class DialogMsgLogger extends Logger {
	JFrame parent;

	/**
	 * 
	 * @param parentFrame Parent JFrame to display to error message over
	 */
	public DialogMsgLogger( JFrame parentFrame ) {
		super();
		parent = parentFrame;
	}

	/**
	 * Display error in dialog box over parent frame
	 * @param message The message to append to the log
	 */
	@Override
	public void logError(String message) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	@Override
	public void logMessage(String message) {
		//JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
		System.out.println(message);
	}

}
