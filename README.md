# README #

JRSRepair is a research program that attempts to repair a buggy program by performing random mutations. 

### Installation ###

JRSRepair is an Eclipse plugin.

* Clone the source into a new folder
* Import the project into Eclipse
* Ensure Eclipse plug-in tools are installed
* Create a directory called `lib\` and put the following libraries
    1. [commons-io-2.4.jar](http://commons.apache.org/proper/commons-io/download_io.cgi)

		### Configuring and Running ###

		JRSRepair comes with a sample program that will run JRSRepair (ca.uwaterloo.ece.qhanam.jrsrepair.test.SampleUse). This program can be executed from the command line and takes one argument -> the path to the configuration file. A sample configuration file can be found in scr/jsrepair.properties.

		Sample useage:
		`java ca.uwaterloo.ece.qhanam.jrsrepair.test.SampleUse /path/to/jrsrepair.properties`

		### Directory Structure ###

		* `src\`: The JRSRepair library
		* `test\`: Contains the sample program `SampleUse.java` which runs JRSRepair.
		* `scr\`: Ant and JRSRepair configuration files
		* `cov\`: The fault localization coverage files for the sample program
=======
# README #

JRSRepair is a research program that attempts to repair a buggy program by performing random mutations. 

### Installation ###

JRSRepair is an Eclipse plugin.

* Clone the source into a new folder
* Import the project into Eclipse
* Ensure Eclipse plug-in tools are installed
* Create a directory called `lib\` and put the following libraries
    1. [commons-io-2.4.jar](http://commons.apache.org/proper/commons-io/download_io.cgi)

### Configuring and Running ###

JRSRepair comes with a sample program that will run JRSRepair (ca.uwaterloo.ece.qhanam.jrsrepair.test.SampleUse). This program can be executed from the command line and takes one argument -> the path to the configuration file. A sample configuration file can be found in scr/jsrepair.properties.

Sample useage:
`java ca.uwaterloo.ece.qhanam.jrsrepair.test.SampleUse /path/to/jrsrepair.properties`

### Directory Structure ###

* `src\`: The JRSRepair library
* `test\`: Contains the sample program `SampleUse.java` which runs JRSRepair.
* `scr\`: Ant and JRSRepair configuration files
* `cov\`: The fault localization coverage files for the sample program
