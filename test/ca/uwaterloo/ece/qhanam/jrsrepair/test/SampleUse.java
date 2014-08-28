package ca.uwaterloo.ece.qhanam.jrsrepair.test;

import java.io.File;
import ca.uwaterloo.ece.qhanam.jrsrepair.*;

public class SampleUse {
	
	public static final String SOURCE_DIRECTORY = "/Users/qhanam/Documents/workspace_faultlocalization/ca.uwaterloo.ece.qhanam.localization/src";
	public static final String FAULTY_COVERAGE = "/Users/qhanam/Documents/workspace_repair/ca.uwaterloo.ece.qhanam.jrsrepair/cov/faulty.cov";
	public static final String SEED_COVERAGE = "/Users/qhanam/Documents/workspace_repair/ca.uwaterloo.ece.qhanam.jrsrepair/cov/seed.cov";

	public static void main(String[] args) throws Exception {
		JRSRepair repair = new JRSRepair(new File(SOURCE_DIRECTORY), new File(FAULTY_COVERAGE), new File(SEED_COVERAGE));
		repair.buildASTs();
	}
}
