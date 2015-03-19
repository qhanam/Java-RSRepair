package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import ca.uwaterloo.ece.qhanam.jrsrepair.context.Context;
import ca.uwaterloo.ece.qhanam.jrsrepair.context.ContextFactory;

/**
 * This class implements a program that attempts to automatically fix a
 * faulty program using JRSRepair. It takes one argument, the path to the
 * configuration file that is used to configure JRSRepair.
 * 
 * @author qhanam
 */
public class JRSRepairMain {
	
	public static void main(String[] args) throws Exception {
		if(args.length > 0){
			JRSRepair repair = readConfigFile(new File(args[0]));
            repair.buildASTs();
            repair.repair();
		}
		else{
			System.out.println("Use: java SampleUse [/path/to/jrsrepair.properties]");
		}
	}
	
	/**
	 * Reads the configuration file, creates the context (environment) and 
	 * creates the JRSRepair instance.
	 * @param config The .properties file.
	 * @return An instance of JRSRepair configures using the settings in the .properties file.
	 * @throws Exception Throws an exception if a parameter is missing or incorrect.
	 */
	public static JRSRepair readConfigFile(File config) throws Exception{
		
		Properties properties = new Properties();
		properties.load(new FileReader(config));
		
        Context context = ContextFactory.buildContext(properties);
		
        JRSRepair repair = new JRSRepair(context);

		return repair;
	}
}