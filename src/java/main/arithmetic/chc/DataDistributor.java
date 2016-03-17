package arithmetic.chc;

import java.io.File;
import java.util.LinkedList;

import arithmetic.shared.Instance;
import arithmetic.shared.InstanceList;

/**
 * Class take care of the training and testing data used for chc data trials.
 * Data 'instances' are held in InstanceLists and inducers recieve the instances
 * from the lists.
 * 
 * Chc uses data in .name, ,.all, .train, .val, .data, and .test files .all -
 * has all data instances for a data set. .train - has data instance used for
 * training the innerloop inducer. .val - data for testing on the inner loop
 * inducer. .data - trains for the outer loop inducer. .text - testing data for
 * outer loop inducer.
 */
public class DataDistributor {
	/**
	 * holds the name of the data file without any extention. File object was
	 * used becuase of its functions to parse the name and path of the file.
	 */
	public File file;
	/** holds the .train file data for inner loop training. */
	private InstanceList traindata;
	/** hold the .val file data for inner loop testing. */
	private InstanceList validationdata;
	/**
	 * holds .data which is a combination of .train and .val for outer loop
	 * training.
	 */
	private InstanceList finaltrainer;
	/** holds the .test data for outer loop testing. */
	private InstanceList testdata;

	// private double trainingdistribution = .6;
	// private double validationdistribution = .2;
	// private double testingdistribution = .2;

	// private int attrselectionsize = 0;
	/**
	 * General constructor. Determines the file system and makes all the
	 * InstancesLists needed for chc trials.
	 * 
	 * @param filename
	 *            - the base name of the file with file path included.
	 * @param system
	 *            - if true the new system is used; if false the old.
	 */
	public DataDistributor(String filename, boolean system) {
		file = new File(filename);
		if (system) {
			traindata = new InstanceList(filename, ".names", ".train");
			validationdata = new InstanceList(filename, ".names", ".val");
			testdata = new InstanceList(filename, ".names", ".test");
			finaltrainer = new InstanceList(filename, ".names", ".data");
		} else {
			distributedata(filename);
		}
	}

	/**
	 * distribute data is used when a single data file must be broken into 3
	 * testable parts, Training Data, Validation Data, and Testing Data. If the
	 * data is already separated distribute data is not needed.
	 * 
	 * This method is technically not needed since this system is never used in
	 * chc. The code is included but the method is disabled.
	 * 
	 * @param arg
	 *            - a string with the data file name root.
	 */
	private void distributedata(String arg) {
		/*
		 * InstanceList templist = new InstanceList(arg,".names",".all");
		 * attrselectionsize = templist.num_attr(); int totalinstances =
		 * templist.num_instances();
		 * System.out.println("    Number of Instances:  " + totalinstances);
		 * int traininstances = (int)(totalinstances*trainingdistribution); int
		 * validateinstances = (int)(totalinstances*validationdistribution); int
		 * testinstances = (int)(totalinstances*testingdistribution);
		 * 
		 * int sumofinstances = traininstances+validateinstances+testinstances;
		 * if (sumofinstances < totalinstances) {
		 * System.out.println("Adding "+(totalinstances
		 * -sumofinstances)+" to validation data"); validateinstances +=
		 * (totalinstances-sumofinstances); } else if (sumofinstances <
		 * totalinstances) {
		 * System.out.println("Removing "+(sumofinstances-totalinstances
		 * )+" from training data"); traininstances -=
		 * (sumofinstances-totalinstances); } sumofinstances =
		 * traininstances+validateinstances+testinstances;
		 * System.out.println(" |||||||||||||||||||||||||||||||||||||||||||||||||"
		 * );
		 * System.out.println(" ||                      Number    Percent      ||"
		 * ); System.out.print(" || Training Data:   ");
		 * Hypothesis.printInt(traininstances, 10); System.out.print("    ");
		 * Hypothesis.printDouble((double)traininstances/sumofinstances, 5);
		 * System.out.print("       ||");
		 * System.out.print("\n || Validation Data: ");
		 * Hypothesis.printInt(validateinstances, 10); System.out.print("    ");
		 * Hypothesis.printDouble((double)validateinstances/sumofinstances, 5);
		 * System.out.print("       ||");
		 * System.out.print("\n || Testing Data:    ");
		 * Hypothesis.printInt(testinstances, 10); System.out.print("    ");
		 * Hypothesis.printDouble((double)testinstances/sumofinstances, 5);
		 * System.out.print("       ||");
		 * System.out.print("\n || Total:           ");
		 * Hypothesis.printInt(traininstances+validateinstances+testinstances,
		 * 10); System.out.print("                 ||");
		 * System.out.println("\n |||||||||||||||||||||||||||||||||||||||||||||||||"
		 * );
		 * 
		 * traindata = templist.split(traininstances); validationdata =
		 * templist.split(validateinstances); testdata =
		 * templist.split(testinstances); finaltrainer = makeFinalTrainer();
		 */
	}

	/**
	 * Makes the outer loop trainer by adding all inner loop instances into one
	 * InstanceList.
	 * 
	 * Function is not used because it is used with a different file system.
	 * 
	 * @return - a list with the outer loop training data.
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	private InstanceList makeFinalTrainer() {
		InstanceList newlist = new InstanceList(traindata, false);
		InstanceList newlist2 = new InstanceList(validationdata, false);
		LinkedList instances = newlist2.instance_list();
		while (!newlist2.no_instances()) {
			newlist.add_instance((Instance) instances.removeFirst());
		}
		return newlist;
	}

	/**
	 * returns a copy of the .train data. It is copied so no changes occur to
	 * the original data copy.
	 * 
	 * @return the list of the data.
	 */
	public InstanceList getTrainData() {
		return new InstanceList(traindata, false);
	}

	/**
	 * returns a copy of the .val data. It is copied so no changes occur to the
	 * original data copy.
	 * 
	 * @return the list of the data.
	 */
	public InstanceList getValidationData() {
		return new InstanceList(validationdata, false);
	}

	/**
	 * returns a copy of the .test data. It is copied so no changes occur to the
	 * original data copy.
	 * 
	 * @return the list of the data.
	 */
	public InstanceList getTestData() {
		return new InstanceList(testdata, false);
	}

	/**
	 * returns a copy of the .data data. It is copied so no changes occur to the
	 * original data copy.
	 * 
	 * @return the list of the data.
	 */
	public InstanceList getFinalTrainer() {
		return new InstanceList(finaltrainer, false);
	}

	/**
	 * Gets the number of attribute in the data set.
	 * 
	 * @return - the number of attributes.
	 */
	public int getattrselectionsize() {
		return testdata.num_attr();
	}

}
