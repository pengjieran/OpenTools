package arithmetic.shared;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ListIterator;

/**
 * The CatTestResult class provides summaries of running categorizers on test
 * data. This includes the option of loading the test data from a file (or
 * giving an existing InstanceList), running the categorizer on all instances,
 * and storing the results. Information can then be extracted quickly.
 * <P>
 * The training set and test set (if given as opposed to loading it here) must
 * not be altered as long as calls to this class are being made, because
 * references are kept to those structures.
 * <P>
 * The complexity for construction of the CatTestResult is O(n1 n2), where n1 is
 * the size of the training-set InstanceList and n2 is the size of the test set.
 * All display routines take time proportional to the number of displayed
 * numbers.
 * <P>
 * The CatTestResult class has been enhanced to compute the log-evidence metric.
 * The log evidence metric is equal to the total evidence against the correct
 * category.
 * <P>
 * 
 * @author James Louis 12/06/2000 Java Implementations JavaDocumentation
 * @author Jim Kelly 11/08/96 Further strengthening of
 *         display_confusion_matrix(): now can display matrix in scatterviz.
 * @author Yeogirl Yun 12/27/94 Implemented NOT_IMPLEMENTED parts. Strengthened
 *         display_confusion_matrix()
 * @author Robert Allen 12/10/94 Add generalized vs memorized error
 * @author Richard Long 10/01/93 Initial revision (.c)
 * @author Ronny Kohavi 9/13/93 Initial revision (.h)
 */
public class CatTestResult {

	// LOG_OPTIONS;

	/* ENUM ErrorType */
	/** The Normal partition for error reporting. **/
	static final public int Normal = 0; /* ENUM ErrorType */
	/** The Generalized partition for error reporting. **/
	static final public int Generalized = 1; /* ENUM ErrorType */
	/** The Memorized partition for error reporting. **/
	static final public int Memorized = 2; /* ENUM ErrorType */
	/* ENUM ErrorType */

	// Member data
	/** The InstanceList containing the training data set. **/
	InstanceList trainIL;
	/** The InstanceList containing the testing data set. **/
	InstanceList testIL;
	/** The number of Instances on which a Categorizer is trained. **/
	int numOnTrain;
	/** The total weight of the Instances on which a Categorizer is trained. **/
	double weightOnTrain;
	/** The scores of a Categorizer's test. **/
	ScoringMetrics metrics;
	/** Statistical data on a Categorizer. **/
	StatData lossStats;
	/** The results of a Categorizer's test. **/
	CatOneTestResult[] results;
	/** The distribution of categories over a data set. **/
	CatCounters[] catDistrib;
	/** The confusion matrix produced by a test of a Categorizer. **/
	double[][] confusionMatrix;
	/**
	 * The indicator that this CatTestResult owns the test data set. True if
	 * this CatTestResult does own the set.
	 **/
	boolean ownsTestIL;
	/**
	 * The indicator that this CatTestResult owns the training data set. True if
	 * this CatTestResult does own the set.
	 **/
	boolean ownsTrainIL;
	/** The indicator that the training data set is initialized with instances. **/
	boolean inTrainingSetInitialized;
	/**
	 * The indicator that this CatTestResult should compute LogLoss. True
	 * indicates this CatTestResult should do so.
	 **/
	static boolean computeLogLoss;

	/** Logging options for this class. **/
	protected LogOptions logOptions = new LogOptions();

	/**
	 * Sets the logging level for this object.
	 * 
	 * @param level
	 *            The new logging level.
	 */
	public void set_log_level(int level) {
		logOptions.set_log_level(level);
	}

	/**
	 * Returns the logging level for this object.
	 * 
	 * @return The log level for this object.
	 */
	public int get_log_level() {
		return logOptions.get_log_level();
	}

	/**
	 * Sets the stream to which logging options are displayed.
	 * 
	 * @param strm
	 *            The stream to which logs will be written.
	 */
	public void set_log_stream(Writer strm) {
		logOptions.set_log_stream(strm);
	}

	/**
	 * Returns the stream to which logs for this object are written.
	 * 
	 * @return The stream to which logs for this object are written.
	 */
	public Writer get_log_stream() {
		return logOptions.get_log_stream();
	}

	/**
	 * Returns the LogOptions object for this object.
	 * 
	 * @return The LogOptions object for this object.
	 */
	public LogOptions get_log_options() {
		return logOptions;
	}

	/**
	 * Sets the LogOptions object for this object.
	 * 
	 * @param opt
	 *            The new LogOptions object.
	 */
	public void set_log_options(LogOptions opt) {
		logOptions.set_log_options(opt);
	}

	/**
	 * Sets the logging message prefix for this object.
	 * 
	 * @param file
	 *            The file name to be displayed in the prefix of log messages.
	 * @param line
	 *            The line number to be displayed in the prefix of log messages.
	 * @param lvl1
	 *            The log level of the statement being logged.
	 * @param lvl2
	 *            The level of log messages being displayed.
	 */
	public void set_log_prefixes(String file, int line, int lvl1, int lvl2) {
		logOptions.set_log_prefixes(file, line, lvl1, lvl2);
	}

	/**
	 * This class has no access to a copy constructor.
	 * 
	 * @param source
	 *            The CatTestResult object to be copied.
	 */
	private CatTestResult(CatTestResult source) {
	}

	/**
	 * This class has no access to an assign method.
	 * 
	 * @param source
	 *            The CatTestResult containing data to be copied into this
	 *            CatTestResult object.
	 */
	private void assign(CatTestResult source) {
	}

	/**
	 * Constructor.
	 * 
	 * @param cat
	 *            The Categorizer used to create this CatTestResult.
	 * @param trainILSource
	 *            The training data set.
	 * @param testILSource
	 *            The test data set.
	 */
	public CatTestResult(Categorizer cat, InstanceList trainILSource,
			InstanceList testILSource) {
		logOptions = new LogOptions("CTR");
		trainIL = trainILSource;
		testIL = testILSource;
		results = new CatOneTestResult[testIL.num_instances()];
		for (int y = 0; y < results.length; y++)
			results[y] = new CatOneTestResult();
		catDistrib = new CatCounters[testIL.num_categories() + 1];// (Globals.UNKNOWN_CATEGORY_VAL,
																	// testIL.num_categories()
																	// + 1);
		for (int z = 0; z < catDistrib.length; z++)
			catDistrib[z] = new CatCounters();
		confusionMatrix = new double[testILSource.num_categories() + 1][testILSource
				.num_categories() + 1];
		// (Globals.UNKNOWN_CATEGORY_VAL, Globals.UNKNOWN_CATEGORY_VAL,
		// testILSource.num_categories()+1,
		// testILSource.num_categories()+1,
		// 0);
		metrics = new ScoringMetrics();
		lossStats = new StatData();

		ownsTestIL = false;
		ownsTrainIL = false;
		initialize(cat);
		inTrainingSetInitialized = false;
	}

	/**
	 * Prune the tree for the given pruning factor. Pruning is based on C4.5's
	 * pruning / Quinlan. We return the pessimistic number of errors on the
	 * training set. We use the standard normal distribution approximation from
	 * CatTestResult. Here's a derivation that shows that this happens to be the
	 * same as C4.5, at least for errors >= 1. <BR>
	 * err = (2ne+z^2+z*sqrt(4ne+z^2-4ne^2))/(2*(n+z^2)) <BR>
	 * where n is the number of records, e is the prob of error, and z is the
	 * z-value. Let E = count of errors, i.e., ne.<BR>
	 * err = (2E + z^2 + z*sqrt(4E+z^2-4E^2/n))/(2*(n+z^2)) <BR>
	 * err = (E + z^2/2 + z*sqrt(E-E^2/n+z^2/4))/(n+z^2) <BR>
	 * err = (E + z^2/2 + z*sqrt(E(1-E/n)+z^2/4))/(n+z^2) <BR>
	 *
	 * @return The pessimistic number of errors on the training set.
	 * @param numErrors
	 *            The number of errors produced in a test run of this
	 *            categorizer.
	 * @param totalWeight
	 *            The total weight of all Instances tested.
	 * @param zValue
	 *            The half of the interval width for confidence evaluation.
	 */
	static public double pessimistic_error_correction(double numErrors,
			double totalWeight, double zValue) {
		MLJ.verify_strictly_greater(totalWeight, 0, "CatTestResult::"
				+ "pessimistic_error_correction: " + "zero total weight");

		if (zValue == 0)
			return numErrors;

		// This can be strictly less than if we're guaranteed to have majority,
		// but when classifying instances in another subtree, this may not
		// hold any more (e.g., when asserting whether to replace a node
		// with one of its subtrees).
		// @@ this check may go away with loss functions
		// @@ Dan, add maximum loss here under DBG
		// ASSERT(numErrors <= totalWeight);
		double probError = (numErrors + 0.5) / totalWeight;
		if (probError > 1)
			probError = 1;
		DoubleRef optimisticProb = new DoubleRef(0);
		DoubleRef pessimisticProb = new DoubleRef(0);
		confidence(optimisticProb, pessimisticProb, probError, totalWeight,
				zValue);
		MLJ.clamp_below(pessimisticProb, 1, "CatTestResult::"
				+ "pessimistic_error_correction: too many errors");

		// ASSERT(pessimisticProb >= 0);
		return pessimisticProb.value * totalWeight;
	}

	/**
	 * Compute the confidence interval according to the binomial model. Source
	 * is Devijver and Kittler.
	 * 
	 * @param confLow
	 *            Low bound of confidence interval. This value is altered.
	 * @param confHigh
	 *            High bound of confidence interval. This value is altered.
	 * @param error
	 *            The error value for which the confidence interval is
	 *            requested.
	 * @param n
	 *            Number of samples.
	 * @param z
	 *            The confidence coefficient.
	 */
	static void confidence(DoubleRef confLow, DoubleRef confHigh, double error,
			double n, double z) {
		double z2 = z * z;
		double sqrtTerm = z
				* Math.sqrt(4 * n * error + z2 - 4 * n * error * error);
		double numer = 2 * n * error + z2;
		double denom = 2 * (n + z2);

		confLow.value = (numer - sqrtTerm) / denom;
		confHigh.value = (numer + sqrtTerm) / denom;
	}

	/**
	 * Compute the confidence interval according to the binomial model. Source
	 * is Devijver and Kittler.
	 * 
	 * @param confLow
	 *            Low bound of confidence interval. This value is altered.
	 * @param confHigh
	 *            High bound of confidence interval. This value is altered.
	 * @param error
	 *            The error value for which the confidence interval is
	 *            requested.
	 * @param n
	 *            Number of samples.
	 */
	public static void confidence(DoubleRef confLow, DoubleRef confHigh,
			double error, double n) {
		confidence(confLow, confHigh, error, n, Globals.CONFIDENCE_INTERVAL_Z);
	}

	/**
	 * Returns ratio number of test instances incorrectly categorized / number
	 * of test instances. Test instance set defaults to all test instances.
	 * 
	 * @return The ratio number of incorrectly classified instances without
	 *         partitioning.
	 */
	public double error() {
		return error(Normal);
	}

	/**
	 * Returns ratio number of test instances incorrectly categorized / number
	 * of test instances. Test instance set defaults to all test instance.
	 * ErrorType argument can be used to partition test cases into those
	 * occuring in the training set or not.
	 * 
	 * @return The ratio number of incorrectly classified instances.
	 * @param errType
	 *            The type of error used to partition test cases. Possible
	 *            values are CatTestResult.Normal, CatTestResult.Generalized,
	 *            CatTestResult.Memorized.
	 */
	public double error(int errType) {
		if (testIL.no_instances())
			Error.fatalErr("CatTestResult::error: No test instances.  This causes "
					+ "division by 0");

		switch (errType) {
		case Normal:
			return total_incorrect_weight() / total_test_weight();

		case Generalized: {
			if (total_weight_off_train() == 0)
				Error.fatalErr("CatTestResult::error(Generalized): All test instances "
						+ "are also in training set.  This causes division by 0");

			double weightOffTrainIncorrect = 0;
			// for (int i = results.low(); i <= results.high(); i++)
			for (int i = 0; i < results.length; i++)
				if ((!results[i].inTrainIL)
						&& (results[i].augCat.num() != results[i].correctCat))
					weightOffTrainIncorrect += results[i].instance.get_weight();
			return weightOffTrainIncorrect / total_weight_off_train();
		}

		case Memorized: {
			if (total_weight_on_train() == 0)
				Error.fatalErr("CatTestResult::error(Memorized): No test instances "
						+ "are in the training set.  This causes division by 0");

			double weightOnTrainIncorrect = 0;
			// for (int i = results.low(); i <= results.high(); i++)
			for (int i = 0; i < results.length; i++)
				if ((results[i].inTrainIL)
						&& (results[i].augCat.num() != results[i].correctCat))
					weightOnTrainIncorrect += results[i].instance.get_weight();
			return weightOnTrainIncorrect / total_weight_on_train();
		}
		default:
			Error.fatalErr("CatTestResult::error unexpected error type"
					+ (int) errType);
			return 1.0;
		} // end switch
	}

	/**
	 * Weight of test instances appearing in appearing in the training data.
	 * Initializes flag for each test instance if not already done.
	 * 
	 * @return The total weight of the instances found in the training and test
	 *         data sets.
	 */
	public double total_weight_on_train() {
		if (!inTrainingSetInitialized)
			initializeTrainTable();
		return weightOnTrain;
	}

	/**
	 * Weight of test instances appearing not appearing in the training data.
	 * Initializes flag for each test instance if not already done.
	 * 
	 * @return The total weight of the instances not found in the training and
	 *         test data sets.
	 */
	public double total_weight_off_train() {
		return total_test_weight() - total_weight_on_train();
	}

	/**
	 * Uses TableCategorizer as an interface to hash table to do quick lookup on
	 * whether a test instance occurs in the training set. Only called when
	 * inTrainIL data is needed. Initializes class variable numOffTrain to
	 * number of test cases found in training set.
	 */
	protected void initializeTrainTable() {
		int numTestInTrain = 0;
		double weightTestInTrain = 0;

		// @@ Due to the MString leak compiler bug, this temporary is allocated
		// here
		String tableName = "Training Set Lookup Table";

		TableCategorizer trainTable = new TableCategorizer(trainIL,
				Globals.UNKNOWN_CATEGORY_VAL, tableName);

		int i = 0;
		// for (ILPix pix(*testIL); pix; ++pix, ++i) {
		// InstanceRC instance = *pix;
		for (ListIterator pixI = testIL.instance_list().listIterator(); pixI
				.hasNext(); i++) {
			Instance instance = (Instance) pixI.next();
			if (trainTable.categorize(instance).Category() != Globals.UNKNOWN_CATEGORY_VAL) {
				results[i].inTrainIL = true; // constructor sets to FALSE
				numTestInTrain++;
				weightTestInTrain += instance.get_weight();
			}
		}
		numOnTrain = numTestInTrain;
		weightOnTrain = weightTestInTrain;
		inTrainingSetInitialized = true;
	}

	/**
	 * Returns the total weight in the test list.
	 * 
	 * @return The total weight of the test data set.
	 */
	public double total_test_weight() {
		return testIL.total_weight();
	}

	/**
	 * Returns the total weight of instances which were incorrectly classified.
	 * 
	 * @return The total weight of incorrectly classified instances.
	 */
	public double total_incorrect_weight() {
		return metrics.weightIncorrect;
	}

	/**
	 * Initializes this CatTestResult by categorizing the test data set with the
	 * given Categorizer.
	 * 
	 * @param cat
	 *            The categorizer with which the test data set will be
	 *            categorized.
	 */
	protected void initialize(Categorizer cat) {
		// DBG(trainIL.get_schema().compatible_with(testIL.get_schema(), true));
		int i = 0;

		int numProcessed = 0;
		int numTotal = testIL.num_instances();
		int tenthsDone = 0;
		// just for now JWP logOptions.DRIBBLE("Classifying (% done): ");
		// for (ILPix pix(*testIL); pix; ++pix, ++i) {
		for (ListIterator pixIL = testIL.instance_list().listIterator(); pixIL
				.hasNext(); ++i) {
			Instance instance = (Instance) pixIL.next();
			int newTenthsDone = (10 * ++numProcessed) / numTotal;

			// If the outer loop is not in an IFDRIBBLE, we get into
			// an infinite loop here!
			// IFDRIBBLE(while (newTenthsDone > tenthsDone)
			// logOptions.DRIBBLE(10 * ++tenthsDone + "%  " + flush));
			// Instance instance = pix;
			logOptions.LOG(2, "Instance: " + instance);
			// logOptions.LOG(2, "Instance: ");
			// instance.display(false,false);
			int correctCat = instance.label_info().get_nominal_val(
					instance.get_label());
			results[i].correctCat = correctCat;
			results[i].instance = new Instance(instance);

			// It is an error to encounter unknown labels at this point.
			if (instance.label_info().is_unknown(instance.get_label()))
				Error.fatalErr("Test instance (" + instance
						+ ") has an unknown " + "label value");

			// Change label to UNKNOWN, so that categorizers don't cheat.
			// DBG(AttrValue_ av;
			// instance.label_info().set_unknown(av);
			// instance.set_label(av));

			// Categorize the instance. If the categorizer is capable of
			// scoring, compute the probability distribution of the label
			// as well. If the categorizer is NOT capable of scoring,
			// build all-or-nothing distributions.
			if (cat.supports_scoring()) {
				results[i].predDist = cat.score(instance);
				results[i].correctDist = new CatDist(cat.get_schema(),
						correctCat);
				results[i].augCat = new AugCategory(
						results[i].predDist.best_category());
			} else {
				AugCategory predCat = cat.categorize(instance);
				results[i].augCat = new AugCategory(predCat);
				results[i].predDist = new CatDist(cat.get_schema(), predCat);
				results[i].correctDist = new CatDist(cat.get_schema(),
						correctCat);
			}

			// Accumulate all results into a ScoringMetric structure.
			ScoringMetrics metric = new ScoringMetrics();
			compute_scoring_metrics(metric, results[i].augCat, new AugCategory(
					correctCat, "correct"), results[i].predDist,
					results[i].correctDist, instance.get_weight(),
					testIL.get_schema(), computeLogLoss);

			AugCategory predCat = results[i].augCat;
			catDistrib[predCat.num()].numTestSet++;
			accumulate_scoring_metrics(catDistrib[predCat.num()].metrics,
					metric);
			confusionMatrix[correctCat][predCat.num()] += instance.get_weight();
			accumulate_scoring_metrics(metrics, metric);

			// accumulate the loss into the loss statistics statData
			lossStats.insert(metric.totalLoss);

			logOptions.LOG(2, "Correct label: "
					+ instance.get_schema()
							.category_to_label_string(correctCat)
					+ " Predicted label: " + predCat.description() + ". ");

			if (predCat.num() == correctCat) {
				// checks that strings match too
				// DBG(if (predCat.description() !=
				// instance.get_schema().category_to_label_string(correctCat))
				// err + "CatTestResult::initialize: labels match but not in "
				// " name. Categorizer=" + predCat.description()
				// + ". Instance="
				// + instance.get_schema().
				// category_to_label_string(correctCat)+ fatal_error);
				logOptions.LOG(2, "Correct. ");
			} else {
				logOptions.LOG(2, "Incorrect. ");
			}

			logOptions.LOG(2, "No correct: " + metrics.numCorrect + '/'
					+ (i + 1) + '\n');

		}
		// IFDRIBBLE(while (tenthsDone < 10)
		// logOptions.DRIBBLE(10 * ++tenthsDone +"%  " + flush));
		// just for now JWP logOptions.DRIBBLE("done." +'\n');
	}

	/**
	 * Accumulates the given ScoringMetrics by the increment of another
	 * ScoringMetrics.
	 * 
	 * @param dest
	 *            The ScoringMetrics which is incremented.
	 * @param src
	 *            The ScoringMetrics which provides the step for incrementation.
	 */
	private void accumulate_scoring_metrics(ScoringMetrics dest,
			ScoringMetrics src) {
		dest.numCorrect += src.numCorrect;
		dest.numIncorrect += src.numIncorrect;
		dest.totalLoss += src.totalLoss;
		dest.weightCorrect += src.weightCorrect;
		dest.weightIncorrect += src.weightIncorrect;
		dest.meanSquaredError += src.meanSquaredError;
		dest.meanAbsoluteError += src.meanAbsoluteError;
		dest.minimumLoss += src.minimumLoss;
		dest.maximumLoss += src.maximumLoss;
		dest.totalLogLoss += src.totalLogLoss;
	}

	/**
	 * Useful functions for computing scoring metrics. All metrics
	 * (probabilistic or normal) are computed within this function.
	 * 
	 * @param metrics
	 *            The ScoringMetrics
	 * @param predictedCat
	 *            The category determined by a Categorizer.
	 * @param correctCat
	 *            The correct category for a test Instance.
	 * @param predictedDist
	 *            The distribution of categories determined by a Categorizer.
	 * @param correctDist
	 *            The distribution of the correct categories for a test data
	 *            set.
	 * @param weight
	 *            The weight of the instance currently being added.
	 * @param testSchema
	 *            The Schema for the test Instance.
	 * @param computeLogLoss
	 *            Indicator of whether LogLoss should be computed. True
	 *            indicates LogLoss should be computed.
	 */
	private void compute_scoring_metrics(ScoringMetrics metrics,
			AugCategory predictedCat, AugCategory correctCat,
			CatDist predictedDist, CatDist correctDist, double weight,
			Schema testSchema, boolean computeLogLoss) {
		double[] predScores = predictedDist.get_scores();
		double[] corrScores = correctDist.get_scores();
		if (predScores.length != corrScores.length)
			Error.fatalErr("compute_scoring_metrics: correct and predicted "
					+ "distributions have different sizes");

		// Compare only the categories, because the predictedCat and correctCat
		// may get different descriptions if one was created by a CatDist
		// based on a scoring inducer.
		if (predictedCat.Category() == correctCat.Category()) {
			metrics.weightCorrect += weight;
			metrics.numCorrect++;
		} else {
			metrics.weightIncorrect += weight;
			metrics.numIncorrect++;
		}

		// If a loss matrix is defined for the TEST set, look up the loss
		// and add it to our totalLoss. Then find the minimum and maximum
		// losses given this correctCat. Add these to the minimum and
		// maximum loss metrics respectively.
		// All losses must be scaled by the instance's weight.
		if (testSchema.has_loss_matrix()) {
			metrics.totalLoss += testSchema.get_loss_matrix()[correctCat.num()][predictedCat
					.num()] * weight;
			int index = 0;
			metrics.minimumLoss += Matrix.min_in_row(correctCat.num(), index,
					testSchema.get_loss_matrix()) * weight;
			metrics.maximumLoss += Matrix.max_in_row(correctCat.num(), index,
					testSchema.get_loss_matrix()) * weight;
		} else {
			if (predictedCat.notequal(correctCat))
				metrics.totalLoss += weight;
			metrics.maximumLoss += weight;
		}

		// Compute the mean squared and mean absolute error between the
		// predicted and correct distributions.
		// The correct distribution will generally be an
		// all-or-nothing distribution in favor of the correct category.
		// Normalize the mean squared/mean absolute errors to be always
		// between 0 and 1. This involves dividing by 2.
		double mse = 0;
		double mae = 0;
		for (int i = 0; i < predScores.length; i++) {
			double diff = predScores[i] - corrScores[i];
			mse += diff * diff;
			mae += Math.abs(diff);
		}
		mse /= 2;
		mae /= 2;

		metrics.meanSquaredError += weight * mse;
		metrics.meanAbsoluteError += weight * mae;

		if (computeLogLoss) {
			double logLoss = log_loss(predictedDist, correctCat) * weight;
			metrics.totalLogLoss += logLoss;
		} else
			metrics.totalLogLoss = 0;
	}

	/**
	 * Computes the log-loss of the given distribution and correct category.
	 * Aborts if the correct category is assigned probability 0 (infinite log
	 * loss).
	 * 
	 * @return
	 * @param predDist
	 * @param correctCat
	 */
	private double log_loss(CatDist predDist, AugCategory correctCat) {
		double predProb = predDist.get_scores()[correctCat.num()];

		if (MLJ.approx_equal(predProb, 0.0))
			Error.fatalErr("CatTestResult::log_loss: the correct category "
					+ correctCat
					+ " was assigned probability zero by the predicted distribution "
					+ predDist);

		return -MLJ.log_bin(predProb);
	}

	/**
	 * Return the number of instances in the test InstanceList that were
	 * correctly categorized.
	 * 
	 * @return An integer representing the number of instances that were
	 *         correctly categorized by an inducer during a test run.
	 */
	public int num_correct() {
		return metrics.numCorrect;
	}

	/**
	 * Return the number of instances in the test InstanceList that were
	 * incorrectly categorized.
	 * 
	 * @return An integer representing the number of instances that were
	 *         incorrectly categorized by an inducer during a test run.
	 */
	public int num_incorrect() {
		return num_test_instances() - num_correct();
	}

	/**
	 * Returns the number of test instances appearing in appearing in the
	 * training data. Initializes flag for each test instance if not already
	 * done.
	 * 
	 * @return The number of test instances also in the training data.
	 */
	public int num_on_train() {
		if (!inTrainingSetInitialized)
			initializeTrainTable();
		return numOnTrain;
	}

	/**
	 * Returns the number of test instances not appearing in appearing in the
	 * training data. Initializes flag for each test instance if not already
	 * done.
	 * 
	 * @return The number of test instances not in the training data.
	 */
	public int num_off_train() {
		return num_test_instances() - num_on_train();
	}

	/**
	 * Returns the number of instances in the training set. This function
	 * ignores weights of instances.
	 * 
	 * @return The number of training instances.
	 */
	public int num_train_instances() {
		return trainIL.num_instances();
	}

	/**
	 * Returns the number of instances in the testing set. This function ignores
	 * weights of instances.
	 * 
	 * @return The number of test instances.
	 */
	public int num_test_instances() {
		return testIL.num_instances();
	}

	/**
	 * Returns the total weight of instances which were correctly classified.
	 * 
	 * @return The total weight of correct instances.
	 */
	public double total_correct_weight() {
		return metrics.weightCorrect;
	}

	/**
	 * Returns the total weight in the training list.
	 * 
	 * @return The total weight in the training list.
	 */
	public double total_train_weight() {
		return trainIL.total_weight();
	}

	// Returns various measures of scoring performance. These metrics attempt
	// to validate the probability distribution returned by a the Categorizer's
	// score() function.

	/**
	 * Returns the total mean squared error value.
	 * 
	 * @return The total mean squared error value.
	 */
	public double total_mean_squared_error() {
		return metrics.meanSquaredError;
	}

	/**
	 * Returns the total mean absolute error value.
	 * 
	 * @return The total mean absolute error value.
	 */
	public double total_mean_absolute_error() {
		return metrics.meanAbsoluteError;
	}

	/**
	 * Computes the estimated standard deviation according to the binomial
	 * model, which assumes every test instance is a Bernoulli trial, thus
	 * std-dev=sqrt(acc*(1-acc)/(n-1))
	 * 
	 * @return The estimated standard deviation.
	 * @param error
	 *            Error value for which deviation measure is requested.
	 * @param n
	 *            The number of samples over which the deviation is requested.
	 */
	public static double theoretical_std_dev(double error, double n) {
		// StoredReal storedRealWeight = (StoredReal)n;
		// mlc.clamp_above(storedRealWeight, (StoredReal)2.0,
		// "CatTestResult:: std-dev of less than "
		// "2.0 total weight is undefined");

		DoubleRef storedRealWeight = new DoubleRef(n);
		MLJ.clamp_above(storedRealWeight, (double) 2.0,
				"CatTestResult:: std-dev of less than "
						+ "2.0 total weight is undefined");

		return Math.sqrt(error * (1 - error) / (storedRealWeight.value - 1));
	}

	/*
	 * Standardized display for performances (expected to be in percentages)
	 * public BufferedWriter perf_display(BufferedWriter stream) {
	 * stream.write(setprecision(GlobalOptions::printPerfPrecision) +
	 * fixed_reals); return stream; }
	 */

	/**
	 * Returns the total log loss recorded for this Inducer run.
	 * 
	 * @return The total log loss.
	 */
	public double total_log_loss() {
		return metrics.totalLogLoss;
	}

	/**
	 * Determines whether unknown classes are used. or example, some test
	 * instance was classified as 'unknown' or there are some test instances
	 * that is of 'unknown' class. Result is to set step and start values for
	 * use in display_ascii_confusion_matrix and
	 * display_scatterviz_confusion_matrix.
	 * 
	 * @param step
	 *            The step value of where to begin in the confusion matrix.
	 * @param start
	 *            The start value of where to begin in the confusion matrix.
	 * @param confusionMatrix
	 *            The confusion matrix which is being checked for unknown
	 *            values.
	 */
	static public void check_for_unknown_classes(int step, int start,
			double[][] confusionMatrix) {
		boolean hasUnknowns = false;
		int i;
		for (i = 0; i < confusionMatrix.length; i++)
			if (confusionMatrix[i][0] > 0) {
				hasUnknowns = true;
				break;
			}

		if (!hasUnknowns)
			for (i = 0 + 1; i < confusionMatrix[0].length; i++)
				if (confusionMatrix[0][i] > 0) {
					hasUnknowns = true;
					break;
				}

		// set depending variables.
		if (hasUnknowns) {
			step = 0;
			start = Globals.UNKNOWN_CATEGORY_VAL;
		} else {
			step = 1;
			start = Globals.FIRST_CATEGORY_VAL;
		}
	}

	/**
	 * Displays confusion matrices in ascii format for this CatTestResult
	 * object.
	 * 
	 * @return The String containing the display of the confusion matrix.
	 * @param stream
	 *            Stream to which display is shown.
	 */
	public String display_ascii_confusion_matrix(String stream) {
		return display_ascii_confusion_matrix(stream, confusionMatrix,
				testIL.get_schema());
	}

	/**
	 * Displays confusion matrices in ascii format.
	 * 
	 * @return The String containing the display of the confusion matrix.
	 * @param display
	 *            The String containing any previous items to be included in the
	 *            display.
	 * @param confusionMatrix
	 *            The confusion matrix to be displayed.
	 * @param schema
	 *            The Schema of the categories that Instances can be classified
	 *            as.
	 */
	static public String display_ascii_confusion_matrix(String display,
			double[][] confusionMatrix, Schema schema) {
		int numCategories = schema.num_label_values() + 1;
		if (
		// confusionMatrix.start_row() != Globals.UNKNOWN_CATEGORY_VAL ||
		// confusionMatrix.start_col() != Globals.UNKNOWN_CATEGORY_VAL ||
		confusionMatrix.length != numCategories
				|| confusionMatrix[0].length != numCategories)
			Error.fatalErr("CatTestResult::display_ascii_confusion_matrix: the given "
					+ "confusion matrix, of dimension (0"
					+ ".."
					+ confusionMatrix.length
					+ ") x (0"
					+ ".."
					+ confusionMatrix[0].length
					+ ") is not compatible with the given schema, which has "
					+ numCategories + " label values (including UNKNOWN)");

		display = display + "\nDisplaying confusion matrix... " + "\n";
		// display = display + push_opts + setprecision(3);
		int row, col;
		int i;

		// int step = 0 + Globals.FIRST_CATEGORY_VAL;
		// int start = 0 + Globals.FIRST_CATEGORY_VAL;

		int step = 0;
		int start = 0;

		check_for_unknown_classes(step, start, confusionMatrix);

		for (i = start, row = 0 + step; row < confusionMatrix.length; row++, i++)
			if (i == Globals.UNKNOWN_CATEGORY_VAL)
				display = display + "   (?)   ";
			else
				display = display + "   ("
						+ (char) ('a' + i - Globals.FIRST_CATEGORY_VAL)
						+ ")   ";
		display = display + "   <-- classified as " + "\n";
		for (row = 0 + step; row < confusionMatrix.length; row++)
			display = display + "-------- ";
		display = display + "\n";

		for (i = start, row = 0 + step; row < confusionMatrix.length /*- Globals.FIRST_CATEGORY_VAL*/; i++, row++) {
			for (col = 0 + step; col < confusionMatrix[0].length /*- Globals.FIRST_CATEGORY_VAL*/; col++) {
				// String confStr(confusionMatrix(row, col), 2, 0,
				// MString::mlcFixed);
				// display = display + "        " + confusionMatrix[row][col] +
				// " ";
				display = display
						+ MLJ.numberToString(confusionMatrix[row][col], 8, 2)
						+ " ";
			}
			AttrValue val = new AttrValue();
			schema.nominal_label_info().set_nominal_val(val, i);
			// Globals.FIRST_CATEGORY_VAL + i);

			if (i == Globals.UNKNOWN_CATEGORY_VAL)
				display = display + "   (?): unknown class " + "\n";
			else
				display = display + "   ("
						+ (char) ('a' + i - Globals.FIRST_CATEGORY_VAL)
						+ "): class "
						+ schema.nominal_label_info().attrValue_to_string(val)
						+ "\n";
		}
		/*
		 * display = display + pop_opts;
		 */
		return display;
	}

	/**
	 * Displays the confusion matrix. The confusion matrix displays for row i
	 * column j, the number of instances classified as j that should have been
	 * classified as i.
	 * 
	 * @param stream
	 *            Stream to which display is shown.
	 * @return String containing the display.
	 */
	public String display_confusion_matrix(String stream) {
		// DispConfMatType defaultDispConfMat = no;
		String dispConfMatHelp = "Display confusion matrix when displaying results on test";
		// static DispConfMatType dispConfusionMatrix =
		// get_option_enum("DISP_CONFUSION_MAT", dispConfMatEnum,
		// defaultDispConfMat, dispConfMatHelp, TRUE);

		// switch (dispConfusionMatrix) {
		// case no:
		// break;
		// case ascii:
		return display_ascii_confusion_matrix(stream);
		// break;
		// case both:
		// display_ascii_confusion_matrix(stream);
		// case scatterviz:
		// MString outputSVizfile =
		// get_option_string("SCATTERVIZ_FILE", EMPTY_STRING,
		// "File name for ScatterViz confusion matrix config file", TRUE);
		//
		// // if the name is provided, use it to generate a permanent file.
		// if(outputSVizfile != EMPTY_STRING) {
		// outputSVizfile += ".conf_matrix.scatterviz";
		// MLCOStream out(outputSVizfile);
		// MString dataName = out.description() + ".data";
		// MLCOStream data(dataName);
		// display_scatterviz_confusion_matrix(out, data);
		// }

		// otherwise, use a TEMPORARY scatterviz file only.
		// else {
		// Array<MString> suffixes(2);
		// suffixes[0] = ".scatterviz";
		// suffixes[1] = ".scatterviz.data";
		// PtrArray<TmpFileName *> *tempNames = gen_temp_file_names(suffixes);
		// const TmpFileName& tmpSVizConf = *tempNames->index(0);
		// const TmpFileName& tmpSVizData = *tempNames->index(1);
		// MLCOStream SVizConf(tmpSVizConf);
		// MLCOStream SVizData(tmpSVizData);
		// display_scatterviz_confusion_matrix(SVizConf, SVizData);
		// SVizConf.close();
		// SVizData.close();
		// if(system(*GlobalOptions::scattervizUtil + " " +
		// tmpSVizConf))
		// Mcerr << "CatTestResult::display: "
		// "Call to ScatterViz returns an error." << endl;
		// delete tempNames;
		// }
		// break;
		// }
	}

	/**
	 * Gives all available statistics (not displays)
	 * 
	 * @param stream
	 *            The writer to which the statistics will be displayed.
	 */
	public void display(BufferedWriter stream) {
		try {
			stream.write(toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts information in this CatTestResult object to a string for
	 * display.
	 * 
	 * @return The String containing the display.
	 */
	public String toString() {
		String rtrn = new String();
		GetEnv getenv = new GetEnv();
		boolean dispMisclassifications = getenv
				.get_option_bool("DISP_MISCLASS", false,
						"Display misclassified instances", true);

		boolean isWeighted = trainIL.is_weighted() || testIL.is_weighted();

		// No perf_display method yet -JL
		// rtrn = rtrn + push_opts + perf_display;
		if (isWeighted) {
			rtrn = rtrn + "Total training weight: "
					+ (float) total_train_weight() + " ("
					+ num_train_instances() + " instances)" + "\n";
			rtrn = rtrn + "Total test weight: " + (float) total_test_weight()
					+ " (" + num_test_instances() + " instances)" + "\n";
			rtrn = rtrn + "  seen: " + (float) total_weight_on_train() + " ("
					+ num_on_train() + " instances)" + "\n";
			rtrn = rtrn + "  unseen: " + (float) total_weight_off_train()
					+ " (" + num_off_train() + " instances)" + "\n";
			rtrn = rtrn + "  correct: " + (float) total_correct_weight() + " ("
					+ num_correct() + " instances)" + "\n";
			rtrn = rtrn + "  incorrect: " + (float) total_incorrect_weight()
					+ " (" + num_incorrect() + " instances)" + "\n";
		} else {
			rtrn = rtrn + "Number of training instances: "
					+ num_train_instances() + "\n";
			rtrn = rtrn + "Number of test instances: " + num_test_instances()
					+ ".  Unseen: " + num_off_train() + ",  seen "
					+ num_on_train() + ".\n";
			rtrn = rtrn + "Number correct: " + num_correct()
					+ ".  Number incorrect: " + num_incorrect() + "\n";
		}

		// DBG(ASSERT(num_off_train() + num_on_train() ==
		// num_test_instances()));
		DoubleRef confLow = new DoubleRef();
		DoubleRef confHigh = new DoubleRef();
		confidence(confLow, confHigh, error(), num_test_instances());
		rtrn = rtrn + "Generalization error: ";

		if (num_off_train() > 0)
			rtrn = rtrn + (error(Generalized) * 100) + '%';
		else
			rtrn = rtrn + "unknown";
		rtrn = rtrn + ".  Memorization error: ";
		if (num_on_train() > 0)
			rtrn = rtrn + (error(Memorized) * 100) + '%' + "\n";
		else
			rtrn = rtrn + "unknown" + "\n";

		rtrn = rtrn + "Error: " + error() * 100 + "% +- ";
		if (total_test_weight() > 1)
			rtrn = rtrn
					+ (theoretical_std_dev(error(), total_test_weight()) * 100)
					+ "%";
		else
			rtrn = rtrn + "Undefined";

		rtrn = rtrn + " [" + confLow.value * 100 + "% - " + confHigh.value
				* 100 + "%]" + "\n";

		// display scoring metrics if the categorizer supports scoring
		rtrn = rtrn + "Average Normalized Mean Squared Error: "
				+ (100 * total_mean_squared_error() / total_test_weight())
				+ "%" + "\n";
		rtrn = rtrn + "Average Normalized Mean Absolute Error: "
				+ (100 * total_mean_absolute_error() / total_test_weight())
				+ "%" + "\n";

		if (computeLogLoss)
			rtrn = rtrn + "Average log-loss: " + total_log_loss()
					/ total_test_weight() + "\n";

		// display the total and average loss if the test set had a loss matrix
		if (testIL.get_schema().has_loss_matrix()) {
			rtrn = rtrn + "Total Loss: " + metrics.totalLoss + "\n";
			rtrn = rtrn + "Average Loss: "
					+ (metrics.totalLoss / total_test_weight()) + "\n";
		}
		/* stream.write(pop_opts); */

		rtrn = display_confusion_matrix(rtrn);
		/*
		 * if (dispMisclassifications) display_incorrect_instances();
		 * 
		 * // dispay a confusion scattergram in VRML if requested
		 * if(get_option_bool("DISP_CONF_SCATTERGRAM", false, "", true)) {
		 * MString fileName =
		 * get_option_string_no_default("CONF_SCATTERGRAM_NAME", "", false) +
		 * ".wrl"; display_vrml_scattergram(fileName); }
		 */
		return rtrn;
	}

	/**
	 * Returns the InstanceList used for training.
	 * 
	 * @return The InstanceList used for training.
	 */
	public InstanceList get_training_instance_list() {
		return trainIL;
	}

	/**
	 * Returns the InstanceList used for testing.
	 * 
	 * @return The InstanceList used for testing.
	 */
	public InstanceList get_testing_instance_list() {
		return testIL;
	}

	/**
	 * Returns the individual results from testing.
	 * 
	 * @return The individual results from testing.
	 */
	public CatOneTestResult[] get_results() {
		return results;
	}

	/**
	 * Returns the scoring metrics collected from the test results.
	 * 
	 * @return The scoring metrics collected from the test results.
	 */
	public ScoringMetrics get_metrics() {
		return metrics;
	}

	/**
	 * Returns the confusion matrix of the results of testing.
	 * 
	 * @return The confusion matrix of the results of testing.
	 */
	public double[][] get_confusion_matrix() {
		return confusionMatrix;
	}

	/**
	 * Returns the total loss value from the scoring metrics.
	 * 
	 * @return The total loss value from the scoring metrics.
	 */
	public double total_loss() {
		return metrics.totalLoss;
	}

	/**
	 * Calculates a normalized loss value.
	 * 
	 * @return The loss value normalized by the loss value range.
	 */
	public double normalized_loss() {
		return (metrics.totalLoss - metrics.minimumLoss)
				/ (metrics.maximumLoss - metrics.minimumLoss);
	}

	/**
	 * Sets the computation of log loss option.
	 * 
	 * @param b
	 *            The new setting of the log loss option.
	 */
	public static void set_compute_log_loss(boolean b) {
		computeLogLoss = b;
	}

	/**
	 * Returns TRUE if the log loss option is set, or FALSE otherwise.
	 * 
	 * @return TRUE if the log loss option is set, FALSE otherwise.
	 */
	public static boolean get_compute_log_loss() {
		return computeLogLoss;
	}

	/*
	 * protected: // Protected methods void initialize();
	 * 
	 * public: void OK(int level = 1) const; CatTestResult(const Categorizer&
	 * cat, const InstanceList& trainILSource, const MString& testFile, const
	 * MString& namesExtension = DEFAULT_NAMES_EXT, const MString& testExtension
	 * = DEFAULT_TEST_EXT); CatTestResult(const Categorizer& cat, const
	 * InstanceList& trainILSource, const InstanceList& testILSource);
	 * CatTestResult(const Array<CatOneTestResult>& resultsArray, const
	 * InstanceList& trainILSource, const InstanceList& testILSource); virtual
	 * ~CatTestResult();
	 * 
	 * void assign_instance_lists(InstanceList *& train, InstanceList *& test);
	 * 
	 * 
	 * // metrics Real total_label_weight(Category label) const; Real
	 * true_label_weight(Category label) const; Real false_label_weight(Category
	 * label) const;
	 * 
	 * Real accuracy(ErrorType errType = Normal) const; Real minimum_loss()
	 * const; Real maximum_loss() const;
	 * 
	 * const StatData& loss_stats() const { return lossStats; } Real mean_loss()
	 * const { return total_loss() / total_test_weight(); } Real std_dev_loss()
	 * const { return loss_stats().std_dev(); } void confidence_loss(Real&
	 * confLow, Real& confHigh, Real z = CONFIDENCE_INTERVAL_Z) const {
	 * loss_stats().percentile(z, confLow, confHigh); }
	 * 
	 * 
	 * virtual InstanceRC get_instance(int num) const; virtual const
	 * AugCategory& label(int num) const; virtual const AugCategory&
	 * predicted_label(int num) const; // display_* show the instance and both
	 * labels (except for // display_correct_instances() which shows only one
	 * label). // Instances which were in the training set say "(In TS)" on //
	 * the display line. virtual void display_all_instances(MLCOStream& stream =
	 * Mcout) const; virtual void display_incorrect_instances(MLCOStream& stream
	 * = Mcout) const; virtual void display_correct_instances(MLCOStream& stream
	 * = Mcout) const; // category distribution displays an array where each
	 * cell i // consists of: (1) number of test instances in class i, // (2)
	 * number of test instances correctly predicted as class i, // (3) number of
	 * test instances incorrectly predicted as class i. virtual void
	 * display_category_distrib(MLCOStream& stream = Mcout) const;
	 * 
	 * virtual void display_scatterviz_confusion_matrix(MLCOStream& stream,
	 * MLCOStream& data) const; // display_all dumps everything (display +
	 * display_all_instances). virtual void display_all(MLCOStream& stream =
	 * Mcout) const; virtual void display_scatterviz_lift_curve(const Category&
	 * labelValue, MString configFileName) const; virtual void
	 * display_vrml_scattergram(const MString& fileName) const;
	 */

}
