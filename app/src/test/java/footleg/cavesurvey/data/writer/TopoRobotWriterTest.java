package footleg.cavesurvey.data.writer;

import static org.junit.Assert.*;

import org.junit.Test;

import footleg.cavesurvey.converter.CmdLineLogger;
import footleg.cavesurvey.data.model.CaveSurvey;
import footleg.cavesurvey.data.model.SurveySeries;
import footleg.cavesurvey.data.writer.TopoRobotWriter;
import footleg.cavesurvey.tools.TestHelper;
import footleg.cavesurvey.tools.UtilityFunctions;

public class TopoRobotWriterTest {

	@Test
	public void testConvertToSingleSeries() {
		CmdLineLogger logger = new CmdLineLogger();
		TopoRobotWriter writer = new TopoRobotWriter(logger);
		
		//Create test survey
		CaveSurvey surveyData = new CaveSurvey(logger);
		
		//Create master series
		SurveySeries masterSeries = new SurveySeries("outer");
		//Add 3 test legs to series
		masterSeries.addLeg( TestHelper.createTestLeg(1,2,1.0,0.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(2,3,1.0,20.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(3,4,1.0,40.0,0.0) );
		
		//Create inner series
		SurveySeries innerSeries = new SurveySeries("inner");
		//Add a couple of test legs
		innerSeries.addLeg( TestHelper.createTestLeg(1,2,1.0,100.0,0.0) );
		innerSeries.addLeg( TestHelper.createTestLeg(2,3,1.0,120.0,0.0) );
		
		//Link station in inner series to a station in the outer series
		masterSeries.addLink("", masterSeries.getLegRaw(2).getToStn(), "inner", innerSeries.getLegRaw(0).getFromStn());
		
		//Add inner series to master series
		masterSeries.addSeries(innerSeries);
		
		//Add master series to survey
		surveyData.add(masterSeries);
		
		//Convert to single series
		SurveySeries convertedSeries = writer.convertToSingleSeries(surveyData, false);
		
		assertEquals("root", convertedSeries.getSeriesName() );
	}

	@Test
	public void testConvertToLinearSeriesFig8() {
		//Create master series
		SurveySeries masterSeries = new SurveySeries("test");
		//Add 3 test legs to series
		masterSeries.addLeg( TestHelper.createTestLeg(1,2,1.0,010.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(2,3,1.0,100.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(3,4,1.0,190.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(4,1,1.0,280.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(1,5,1.0,270.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(5,6,1.0,180.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(6,7,1.0,090.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(7,1,1.0,000.0,0.0) );
		
		//Convert to single series
		SurveySeries convertedSeries = UtilityFunctions.convertToLinearSeries( masterSeries, new CmdLineLogger() );
		
		assertEquals("Expect 2 series from figure of 8 loop.", 2, convertedSeries.innerSeriesCount() );
	}

	@Test
	public void testConvertToLinearSeriesOrderedT() {
		//Create master series
		SurveySeries masterSeries = new SurveySeries("test");
		//Add 3 test legs to series
		masterSeries.addLeg( TestHelper.createTestLeg(1,2,1.0,110.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(2,3,1.0,100.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(3,4,1.0,190.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(4,5,1.0,180.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(3,6,1.0,270.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(6,7,1.0,290.0,0.0) );
		
		//Convert to single series
		SurveySeries convertedSeries = UtilityFunctions.convertToLinearSeries( masterSeries, new CmdLineLogger() );
		
		assertEquals("Expect 2 series from t-shaped single series.", 2, convertedSeries.innerSeriesCount() );
	}

	@Test
	public void testConvertToLinearSeriesScrambledT() {
		//Create master series
		SurveySeries masterSeries = new SurveySeries("test");
		//Add 3 test legs to series
		masterSeries.addLeg( TestHelper.createTestLeg(4,5,1.0,180.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(3,6,1.0,270.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(2,3,1.0,100.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(6,7,1.0,290.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(3,4,1.0,190.0,0.0) );
		masterSeries.addLeg( TestHelper.createTestLeg(1,2,1.0,110.0,0.0) );
		
		//Convert to single series
		SurveySeries convertedSeries = UtilityFunctions.convertToLinearSeries( masterSeries, new CmdLineLogger() );
		
		assertEquals("Expect 2 series from t-shaped single series.", 2, convertedSeries.innerSeriesCount() );
	}

}
