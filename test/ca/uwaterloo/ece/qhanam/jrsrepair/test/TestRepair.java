package ca.uwaterloo.ece.qhanam.jrsrepair.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import ca.uwaterloo.ece.qhanam.jrsrepair.JRSRepair;
import ca.uwaterloo.ece.qhanam.jrsrepair.JRSRepairMain;
import junit.framework.TestCase;

public class TestRepair extends TestCase {
	
	@Test
	public void testSampleProgram () throws Exception {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut(new PrintStream(baos));
		
        JRSRepair repair = JRSRepairMain.readConfigFile(new File("./sample/config/jrsrepair.properties"));
        repair.buildASTs();
        repair.repair();
        
        String output = baos.toString();
        Assert.assertThat(output, CoreMatchers.containsString("Compiled! Failed."));
        Assert.assertThat(output, CoreMatchers.containsString("Compiled! Passed!"));

	}

}
