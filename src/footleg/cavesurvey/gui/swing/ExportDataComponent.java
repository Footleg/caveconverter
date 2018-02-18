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

import footleg.cavesurvey.converter.CaveConverter;
import footleg.cavesurvey.converter.Logger;
import footleg.cavesurvey.converter.CaveConverter.SurveyDataOutputFormats;

/**
 * Component to display and hold cave survey data exported from the cave data model.
 *  
 * @author      Footleg
 * @version     2017.01.10                                (ISO 8601 YYYY.MM.DD)
 * @since       1.7                                       (The Java version used)
 * 
 * @to.do
 * TODO Allow data to be reused as input data to build a new cave model.
 */
@SuppressWarnings("serial")
public class ExportDataComponent extends SurveyDatafileTabPane {
	private CaveConverter.SurveyDataOutputFormats format;
	
	public ExportDataComponent(Font font, Logger logger, SurveyDataOutputFormats dataFormat) {
		super(font, logger);

		format = dataFormat;
		//Set file filter appropriate to file format
		if ( format == SurveyDataOutputFormats.Survex ) {
			fileSaveDialog.setFileFilter( survexFileFilter );
		}
		else if ( format == SurveyDataOutputFormats.Toporobot ) {
			fileSaveDialog.setFileFilter( toporobotFileFilter );
		}
	}

	/**
	 * @return the format
	 */
	public CaveConverter.SurveyDataOutputFormats getFormat() {
		return format;
	}

}
