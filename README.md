=======
## README ##

JRSRepair is a research tool that attempts to repair a buggy program by randomly mutating it. It is essentially a Java implementation of the [RSRepair](http://qiyuhua.github.io/projects/rsrepair/) tool, which is a variation of the [GenProg](http://dijkstra.cs.virginia.edu/genprog/) automated program repair tool.

### Installation ###

JRSRepair is an Eclipse plugin.

* Clone the source into a new folder
* Import the project into Eclipse
* Ensure Eclipse plug-in tools are installed
* Create a directory called `lib\` and put the following libraries in it:
    1. [commons-io-2.4.jar](http://commons.apache.org/proper/commons-io/download_io.cgi)
    2. [commons-lang3-3.3.2.jar](http://commons.apache.org/proper/commons-lang/download_lang.cgi)

### Configuring and Running ###

JRSRepair comes with a sample program that will run JRSRepair (ca.uwaterloo.ece.qhanam.jrsrepair.test.SampleUse). This program can be executed from the command line and takes one argument -> the path to the configuration file. A sample configuration file can be found in `sample\config\jsrepair.properties`.

Sample useage:
```bash
java ca.uwaterloo.ece.qhanam.jrsrepair.test.SampleUse /path/to/jrsrepair.properties
```

### Directory Structure ###

* `src\`: The JRSRepair library
* `test\`: Contains the sample program `SampleUse.java` which runs JRSRepair.
* `sample\`: A sample program for trying out JRSRepair
* `sample\src`: The sample program under test (an LRU cache implementation)
* `sample\test`: The JUnit test cases for the sample program under test
* `sample\config`: The JRSRepair configuration files for repairing the sample program. This includes:
    * `jrsrepair.properties`: The configuration file
    * `faulty.cov`: The code coverage file for potentially faulty statements to mutate.
    * `seed.cov`: The code coverage file for seed statements to perform the mutations with.
    * `build.xml`: The Apache Ant build script that runs the JUnit tests for the sample program.
