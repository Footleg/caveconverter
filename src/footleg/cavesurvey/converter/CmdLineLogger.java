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
package footleg.cavesurvey.converter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import footleg.cavesurvey.tools.UtilityFunctions;

/**
 * Logging class for command line application.
 *  
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 * 
 */
public class CmdLineLogger extends Logger {
	private static List<String> log = new ArrayList<String>();

	/**
	 * Logs as message
	 * @param message The message to append to the log
	 */
	@Override
	public void logError(String message) {
		logMessage(message);
	}

	/**
	 * Formats message with date time stamp, then outputs to console and log
	 * @param message The message to append to the log
	 */
	@Override
	public void logMessage(String message) {
		String logLine;

		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);	
		sdf.setTimeZone(TimeZone.getDefault());          

		logLine = sdf.format(cal.getTime()) + ": " + message;
		log.add( logLine );
		System.out.println(logLine);

	}	

	/**
	 * Writes the contents of the log to a file
	 * @param fileName Full path to the file to write
	 * @param characterSetEncoding Characterset encoding to use in writing the file
	 */
	public void writeLogToFile(String fileName, String characterSetEncoding) {
		UtilityFunctions.writeTextFile( log, fileName, characterSetEncoding );
	}
	
}
