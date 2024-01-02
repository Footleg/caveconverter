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
package footleg.cavesurvey.data.model;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import footleg.cavesurvey.converter.Logger;

/**
 * Class representing a complete cave survey data model. This can consist of 
 * one or more cave surveys.
 *  
 * @author      Footleg
 * @version     2017.01.09                                (ISO 8601 YYYY.MM.DD)
 * @since       1.6                                       (The Java version used)
 */
public class CaveSurvey implements TreeModel {
	private String surveyName;
	private List<SurveySeries> survey;
	private EventListenerList listenerList = new EventListenerList();
	private Logger logger;
	
	/**
	 * Class constructor 
	 * @param  logger Logging class to output information, warning and error messages to
	 */
	public CaveSurvey( Logger logger ) {
		super();
		this.logger = logger;
		//Create new list of survey series to hold data
		survey = new ArrayList<SurveySeries>();
	}

	public int size() {
		return survey.size();
	}

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	public boolean isEmpty() {
		return survey.isEmpty();
	}

	public boolean contains(Object o) {
		return survey.contains(o);
	}

	public Iterator<SurveySeries> iterator() {
		return survey.listIterator();
	}

	public boolean add(SurveySeries e) {
		boolean result = survey.add(e);
		fireTreeStructureChanged( this );
		return result;
	}

	public boolean remove(SurveySeries o) {
		return survey.remove(o);
	}

	public void clear() {
		survey.clear();
	}

	public SurveySeries get(int index) {
		return survey.get(index);
	}

	public SurveySeries set(int index, SurveySeries element) {
		return survey.set(index, element);
	}

	public void add(int index, SurveySeries element) {
		survey.add(index, element);
	}

	public SurveySeries remove(int index) {
		return survey.remove(index);
	}

	public ListIterator<SurveySeries> listIterator() {
		return survey.listIterator();
	}

	public ListIterator<SurveySeries> listIterator(int index) {
		return survey.listIterator(index);
	}

	/**
	 * Generates LRUD data from splays for all legs in all series
	 */
	public void generateLRUDfromSplays() {
		//Loop through all series
		ListIterator<SurveySeries> seriesIterator = survey.listIterator();
		while ( seriesIterator.hasNext() ) {
			SurveySeries series = seriesIterator.next();

			//Process series and then recursively call inner series
			processLRUDfromSplays( series );
		}
	}

	/**
	 * Process the legs in this series to generate LRUD data from splays,
	 * then loop through all inner series recursively to process them too.
	 * @param series The survey series to process
	 */
	private void processLRUDfromSplays( SurveySeries series ) {
		//Process series and then recursively call inner series
		series.generateLRUDFromSplays( logger );
		
		//TODO Fix the way the option to remove splays used for LRUD is triggered, and don't do it by default.
		//series.removeSplaysUsedForLRUD(); 
		
		//Loop through all inner series
		ListIterator<SurveySeries> seriesIterator = series.getInnerSeriesList().listIterator();
		while ( seriesIterator.hasNext() ) {
			SurveySeries innerSeries = seriesIterator.next();
			processLRUDfromSplays( innerSeries );
		}
	}

	/*
	 * TreeModel interface methods
	 */
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {
		//Root is the cave survey class itself
		return this;
	}

	@Override
	public Object getChild(Object parent, int index) {
		//Return inner series at the index specified of the parent series passed in
		if ( parent instanceof CaveSurvey ) {
			return survey.get(index);
		}
		else if ( parent instanceof SurveySeries ) {
			SurveySeries series = (SurveySeries)parent;
			//Determine whether this index points to an inner series or a leg
			if ( index < series.innerSeriesCount() ) {
				//Return an inner series
				return series.getInnerSeries(index);
			}
			else {
				//Return a survey leg
				int legIdx = index - series.innerSeriesCount();
				return series.getLegRaw( legIdx );
			}
		}
		else {
			return null;
		}
	}

	@Override
	public int getChildCount(Object parent) {
		//Return count of inner series plus surveyed legs for the parent series passed in
		if ( parent instanceof CaveSurvey ) {
			return size();
		}
		else if ( parent instanceof SurveySeries ) {
			SurveySeries series = (SurveySeries)parent;
			return series.getInnerSeriesList().size() + series.legCount();
		}
		else {
			return 0;
		}
	}

	@Override
	public boolean isLeaf(Object node) {
		//Return true if node is a survey leg
		boolean leaf = false;
		
		if ( node instanceof SurveyLeg ) {
			leaf = true;
		}
		return leaf;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
		Object[] p = path.getPath();
		Object[] pp = p;
		Object node;
		int index;
		
		if ( p.length == 1 ) {
			//Editing root node
			setSurveyName( (String)newValue );
			node = this;
			index = -1;
		}
		else {
			//Editing a Survey series or leg inside a series
			node = p[p.length - 1];
			SurveySeries parent = (SurveySeries)p[p.length - 2];
			index = parent.getIndexOfChild( node );
			//EDIT HERE
			if ( node instanceof SurveySeries ) {
				((SurveySeries) node).setSeriesName( (String)newValue );
			}
		}
		
//		int[] ci = new int[] { index };
//		Object[] cc = new Object[] { node };
//		fireTreeNodesChanged();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		//Look up index of the specified child class instance in the specified parent instance
		if ( parent instanceof SurveySeries ) {
			return ((SurveySeries)parent).getIndexOfChild( child );
		}
		else {
			return -1;
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(TreeModelListener.class, l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(TreeModelListener.class, l);
	}

	protected void fireTreeStructureChanged(Object oldRoot) {
		TreeModelEvent event = new TreeModelEvent(this, new Object[] { oldRoot });
		EventListener[] listeners = listenerList.getListeners(TreeModelListener.class);
		for (int i = 0; i < listeners.length; i++)
			((TreeModelListener) listeners[i]).treeStructureChanged(event);
	}

	/**
	 * Provides a string representation of a cave survey to display in a tree view of the data model
	 * @return String representation of Cave Survey class 
	 */
	public String toString() {
		String text = getSurveyName();
		if ( ( text == null ) || ( text.equals("") ) ) {
			text = "Cave Survey";
		}
		return text;
	}
}
