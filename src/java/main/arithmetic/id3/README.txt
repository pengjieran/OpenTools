To compile:
   Set the sourcepath option for the Java compiler to the shared and id3 directories. 
      Windows Example:

	javac -sourcepath ../shared;../id3;. *.java

      Linux Example:

        javac -sourcepath ../shared:../id3:. *.java

To run ID3 Driver:

   The ID3 Driver is run from either the src directory or one of its subdirectories. If run from a subdirectory, the MLJ-Options.file file should be present in that directory.

   Data, test, and names files should have the same name and path base. This name and path base is the first argument into Driver. Example:

	java Driver ../data/vote

This will run the Driver over vote.names, vote.test, and vote.data in the ../data directory.

   A second argument can be supplied to change the loglevel and global loglevel to a different setting. Example:

	java Driver ../data/vote 4

This will change the loglevel to 4 and produce more output. Not supplying a value results in a default value of 0.

   A third option allows switching between tree display specifications as they are in the MLC binary and specifications as they are in the MLC code. To switch between these settings, add true or false as a third setting. Example:

	java Driver ../data/vote 4 false

False sets to the source specifications and true sets to the binary specifications. The default is true.
