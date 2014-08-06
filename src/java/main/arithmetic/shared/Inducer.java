package arithmetic.shared;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/*****************************************************************************
  The Inducer class "induces" a concept from a labelled training set 
(supervised learning). An Inducer is really an "Internal inducer," that is, 
one that we have a categorizer for (as opposed to the BaseInducer which may 
be external. The main routines added to BaseInducer are train() predict() 
(for a single instance), and get_categorizer(). Train_and_test() is 
implemented here in terms of the above.

@author James Louis 5/21/2001 Java Implementation.
@author Yeogirl Yun 74/95 Added copy() and copy constructor.
@author Ronny Kohavi 10/7/94 Rewrote as subclass of BaseInducer.
@author Richard Long 8/17/93 Initial revision (.c)
@author Ronny Kohavi 6/23/93 Initial revision (.h)
*****************************************************************************/

abstract public class Inducer extends BaseInducer
{
/*****************************************************************************
  Constructor. Uses the constructor provided in BaseInducer.
@param description A String representing the description of the inducer.
*****************************************************************************/
   public Inducer(String description)
   {
      super(description);
   }

/*****************************************************************************
  Copy constructor. 
@param source The original inducer that is being copied.
*****************************************************************************/
   public Inducer(Inducer source)
   {
      super(source);
   }

/*****************************************************************************
  Trains this inducer on the data set.
*****************************************************************************/
   public abstract void train();

/*****************************************************************************
  Returns the categorizer created by this inducer.
@return The Categorizer class storing a categorizer created by this induction
        algorithm.
*****************************************************************************/
public abstract Categorizer get_categorizer(); 

/*****************************************************************************
  Displays the structure of the categorizer that is produced by the induction
algorithm. Uses default display preferences and System.out for displaying.
*****************************************************************************/
public void display_struct()
{
	DisplayPref dp = new DisplayPref(DisplayPref.ASCIIDisplay);
	BufferedWriter stream = new BufferedWriter(new OutputStreamWriter(System.out));
	display_struct(stream, dp);
}

/*****************************************************************************
  Displays the structure of the categorizer that is produced by the induction
algorithm.
@param stream The output writer used to display the categorizer.
@param dp     The display preferences specified by caller.
*****************************************************************************/
public void display_struct(BufferedWriter stream, DisplayPref dp)
{
   try{
      get_categorizer().display_struct(stream, dp);
      stream.flush();
   }catch(IOException e){e.printStackTrace();}
}



/*****************************************************************************
  Train and tests this inducer. Uses the train_and_perf method, so this method
will be specific to the induction algorithm of the subclass.
@param trainingSet The data set used to train this inducer.
@param testSet     The data set used to test this inducer.
@return The probability of incorrect responses obtained during testing. 
        Possible values range from 0.0 to 1.0.
*****************************************************************************/
public double train_and_test(InstanceList trainingSet,
			     InstanceList testSet) 
{
   if (!supports_full_testing())
      Error.fatalErr("Inducer::train_and_test: this Inducer (class id "
		+ this.getClass().getName()
	      +" does not support full testing. train_and_test and "
	      +"train_and_perf must be overridden in this class");
   CatTestResult results = train_and_perf(trainingSet, testSet);
   double error = results.error();
   results = null;
   return error;
}

/**
 * Train and measures performance on the inducer. Uses the train method, so
 * this method will be specific to the induction algorithm of the subclass.
 * @param trainingSet The data set used to train this inducer.
 * @param testSet     The data set used to test this inducer.
 * @return A CatTestResult class that represents the results after perfecting.
 */
public CatTestResult train_and_perf(InstanceList trainingSet,
				       InstanceList testSet) 
{
//   DBG_DECLARE(InstanceList* callersSet = trainingSet;)
      
   InstanceList oldList = assign_data(trainingSet);
   train();
   CatTestResult results =
      new CatTestResult(get_categorizer(), instance_list(), testSet);

   logOptions.LOG(1, "Inducer::train_and_test for inducer " +description()
       +'\n'+results.toString());

   trainingSet = assign_data(oldList); // get back ownership of user's set
//   DBG(ASSERT(trainingSet == callersSet));
   return results;
}

/*****************************************************************************
  Checks if this inducer can test itself.
@return True for the Inducer class.
*****************************************************************************/
   public boolean supports_full_testing() { return true; }

/*****************************************************************************
  Train and tests this inducer on the attributes specified. Uses the 
train_and_perf method, so this method will be specific to the induction 
algorithm of the subclass.
@param trainingSet	The data set used to train this inducer.
@param testSet		The data set used to test this inducer.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The probability of incorrect responses obtained during testing. 
        Possible values range from 0.0 to 1.0.
*****************************************************************************/
   public double project_train_and_test(InstanceList trainingSet, 
                                        InstanceList testSet, 
                                        boolean[] bitString)
   {
      return train_and_test(trainingSet.project(bitString),
                            testSet.project(bitString));
   }

/*****************************************************************************
  Reads the training and test files in, trains the inducer, and tests the 
inducer on the attributes specified by the bit string input.
@param fileStem The name of the names, data, and test files without the
                the file extensions.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The probability of incorrect test responses. Possible values
        are 0.0 to 1.0.
*****************************************************************************/
   public double project_train_and_test_files(String fileStem , 
                                              boolean[] bitString)
   {
      return project_train_and_test_files(fileStem,Globals.DEFAULT_NAMES_EXT,
                       Globals.DEFAULT_DATA_EXT,
                       Globals.DEFAULT_TEST_EXT,bitString);
   }

/*****************************************************************************
  Reads the training and test files in, trains the inducer, and tests the 
inducer on the attributes specified by the bit string input.
@param fileStem The name of the names, data, and test files without the
                the file extensions.
@param namesExtension	The extension used for the names file. Should
					begin with a period.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The probability of incorrect test responses. Possible values
        are 0.0 to 1.0.
*****************************************************************************/
   public double project_train_and_test_files(String fileStem, 
                                     String namesExtension, 
                                     boolean[] bitString)
   {
      return project_train_and_test_files(fileStem,namesExtension,
                       Globals.DEFAULT_DATA_EXT,
                       Globals.DEFAULT_TEST_EXT,bitString);
   }

/*****************************************************************************
  Reads the training and test files in, trains the inducer, and tests the 
inducer on the attributes specified by the bit string input.
@param fileStem The name of the names, data, and test files without the
                the file extensions.
@param namesExtension	The extension used for the names file. Should
					begin with a period.
@param dataExtension	The extension used for the data file. Should
					begin with a period.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The probability of incorrect test responses. Possible values
        are 0.0 to 1.0.
*****************************************************************************/
   public double project_train_and_test_files(String fileStem, 
                                     String namesExtension, 
                                     String dataExtension, 
                                     boolean[] bitString)
   {
      return project_train_and_test_files(fileStem,namesExtension,
                       dataExtension,
                       Globals.DEFAULT_TEST_EXT,bitString);
   }

/*****************************************************************************
  Reads the training and test files in, trains the inducer, and tests the 
inducer on the attributes specified by the bit string input.
@param fileStem The name of the names, data, and test files without the
                the file extensions.
@param namesExtension	The extension used for the names file. Should
					begin with a period.
@param dataExtension	The extension used for the data file. Should
					begin with a period.
@param testExtension	The extension used for the test file. Should
					begin with a period.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The probability of incorrect test responses. Possible values
        are 0.0 to 1.0.
*****************************************************************************/
   public double project_train_and_test_files(String fileStem, 
                                     String namesExtension, 
                                     String dataExtension, 
                                     String testExtension, 
                                     boolean[] bitString)
   {
      InstanceList trainList = new InstanceList(Globals.EMPTY_STRING, 
                                                fileStem + namesExtension,
						fileStem + dataExtension);
      InstanceList testList = new InstanceList(trainList.get_schema(),
         trainList.get_original_schema(),
         fileStem + testExtension);
      return train_and_test(trainList.project(bitString), 
                            testList.project(bitString));

   }

/*****************************************************************************
  Train and tests this inducer on the attributes specified. Uses the 
train_and_perf method, so this method will be specific to the induction 
algorithm of the subclass.
@param trainingSet	The data set used to train this inducer.
@param testSet		The data set used to test this inducer.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The CatTestResult object containing results for this inducer.
*****************************************************************************/
   public CatTestResult project_train_and_perf(InstanceList trainingSet, 
                                        InstanceList testSet, 
                                        boolean[] bitString)
   {
      return train_and_perf(trainingSet.project(bitString),
                            testSet.project(bitString));
   }

/*****************************************************************************
  Reads the training and test files in, trains the inducer, and tests the 
inducer on the attributes specified by the bit string input.
@param fileStem The name of the names, data, and test files without the
                the file extensions.
@param namesExtension	The extension used for the names file. Should
					begin with a period.
@param dataExtension	The extension used for the data file. Should
					begin with a period.
@param testExtension	The extension used for the test file. Should
					begin with a period.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The CatTestResult object containing results for this inducer.
*****************************************************************************/
   public CatTestResult project_train_and_perf_files(String fileStem, 
                                     String namesExtension, 
                                     String dataExtension, 
                                     String testExtension, 
                                     boolean[] bitString)
   {
      InstanceList trainList = new InstanceList(Globals.EMPTY_STRING, 
                                                fileStem + namesExtension,
						fileStem + dataExtension);
      InstanceList testList = new InstanceList(trainList.get_schema(),
         trainList.get_original_schema(),
         fileStem + testExtension);
      return train_and_perf(trainList.project(bitString), 
                            testList.project(bitString));

   }

/*****************************************************************************
  Reads the training and test files in, trains the inducer, and tests the 
inducer on the attributes specified by the bit string input.
@param fileStem The name of the names, data, and test files without the
                the file extensions.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The CatTestResult object containing results for this inducer.
*****************************************************************************/
   public CatTestResult project_train_and_perf_files(String fileStem , 
                                              boolean[] bitString)
   {
      return project_train_and_perf_files(fileStem,Globals.DEFAULT_NAMES_EXT,
                       Globals.DEFAULT_DATA_EXT,
                       Globals.DEFAULT_TEST_EXT,bitString);
   }

/*****************************************************************************
  Reads the training and test files in, trains the inducer, and tests the 
inducer on the attributes specified by the bit string input.
@param fileStem The name of the names, data, and test files without the
                the file extensions.
@param namesExtension	The extension used for the names file. Should
					begin with a period.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The CatTestResult object containing results for this inducer.
*****************************************************************************/
   public CatTestResult project_train_and_perf_files(String fileStem, 
                                     String namesExtension, 
                                     boolean[] bitString)
   {
      return project_train_and_perf_files(fileStem,namesExtension,
                       Globals.DEFAULT_DATA_EXT,
                       Globals.DEFAULT_TEST_EXT,bitString);
   }

/*****************************************************************************
  Reads the training and test files in, trains the inducer, and tests the 
inducer on the attributes specified by the bit string input.
@param fileStem The name of the names, data, and test files without the
                the file extensions.
@param namesExtension	The extension used for the names file. Should
					begin with a period.
@param dataExtension	The extension used for the data file. Should
					begin with a period.
@param bitString		A boolean array with the same number of values as 
					there are attributes. Each boolean element 
					corresponds to an attribute in the order they 
					were input. True values represent attributes 
					that are used.
@return The probability of incorrect test responses. Possible values
        are 0.0 to 1.0.
*****************************************************************************/
   public CatTestResult project_train_and_perf_files(String fileStem, 
                                     String namesExtension, 
                                     String dataExtension, 
                                     boolean[] bitString)
   {
      return project_train_and_perf_files(fileStem,namesExtension,
                       dataExtension,
                       Globals.DEFAULT_TEST_EXT,bitString);
   }


/*****************************************************************************
  Returns the Categorizer trained and removes ownership by this Inducer 
object.
@return The Categorizer trained.
*****************************************************************************/
   abstract public Categorizer release_categorizer();

/*****************************************************************************
  Checks if this Inducer object can be cast to an Inducer.
@return TRUE if this object can be cast to an Inducer, FALSE otherwise.
*****************************************************************************/
   public boolean can_cast_to_inducer()
   {
      return true;
   }

   /** Casts this object to an Inducer class.
    * @returns This object as an Inducer object.
    * @return Returns an Inducer reference to this object.
    */
   public Inducer cast_to_inducer() 
   {
   // Note that a class may override can_cast_to_inducer(), so this
   //   ensures consistency and avoids the need to override cast_to_inducer().
   //   This happens in wrapper inducers that may or may not wrap around
   //   a real inducer.
      if (!can_cast_to_inducer())
         Error.fatalErr("Inducer "+description()+" (class id " + class_id()
            +") cannot be cast into an inducer");
      return (Inducer)this;
   }

/*****************************************************************************
     These two methods are needed to extract information from tree inducers.
     Other inducers may only have to return 0 if they produce no tree.
*****************************************************************************/
     public abstract int num_nontrivial_nodes();
     public abstract int num_nontrivial_leaves();
}
