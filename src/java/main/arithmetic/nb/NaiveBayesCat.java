package arithmetic.nb;

import java.io.BufferedWriter;
import arithmetic.shared.Error;
import java.io.IOException;

import arithmetic.shared.AttrInfo;
import arithmetic.shared.AugCategory;
import arithmetic.shared.BagCounters;
import arithmetic.shared.CatDist;
import arithmetic.shared.Categorizer;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Entropy;
import arithmetic.shared.Globals;
import arithmetic.shared.Instance;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;
import arithmetic.shared.NominalAttrInfo;
import arithmetic.shared.Schema;
import arithmetic.shared.StatData;

/** This categorizer returns the category (label) that had the
  * greatest relative probability of being correct, assuming
  * independence of attributes. Relative probability of a label
  * is calculated by multiplying the relative probability for
  * each attribute.  The calculation of relative probabity for a
  * label on a single attribute depends on whether the attribute
  * is descrete or continuous.
  * By Bayes Theorem, P(L=l | X1=x1, X2=x2, ... Xn=xn)
  * = P(X1=x1, X2=x2, ... Xn=xn | L=l)*P(L=l)/P(X)
  * where P(X) is P(X1=x1, ..., Xn=xn).
  * Since P(X) is constant independent of the classes, we
  * can ignore it.
  * The Naive Bayesian approach asssumes complete independence
  * of the attributes GIVEN the label, thus
  * P(X1=x1, X2=x2, ... Xn=xn | L=l) =
  * P(X1=x1|L=l)*P(X2=x2|L)*... P(Xn=xn|L)
  * and P(X1=x1|L=l) = P(X1=x1 ^ L=l)/P(L=l) where this
  * quantity is approximated form the data.
  * When the computed probabilities for two labels have the same
  * value, we break the tie in favor of the most prevalent label.
  * 
  * If the instance being categorized has the first attribute = 1,
  * and in the training set label A occured 20 times, 10 of
  * which had value 1 for the first attribute, then the
  * relative probability is 10/20 = 0.5.
  *
  * For continuous (real) attributes, the relative probability
  * is based on the Normal Distribution of the values of the
  * attribute on training instances with the label.  The actual
  * calculation is done with the Normal Density; constants,
  * which do not affect the relative probability between labels,
  * are ignored.  For example, say 3 training instances have
  * label 1 and these instances have the following values for a
  * continous attribute: 35, 50, 65.  The program would use the
  * mean and variance of this "sample" along with the attribute
  * value of the instance that is being categorized in the
  * Normal Density equation.  The evaluation of the Normal
  * Density equation, without constant factors, provides the
  * relative probability.
  *
  * Unknown attributes are skipped over.
  * 
  * Assumptions :  This method calculates the probability of a label as the
  * product of the probabilities of each attribute.
  * This is assuming that the attributes are
  * independent, a condition not likely corresponding to
  * reality.  Thus the "Naive" of the title.
  * This method assumes that all continous attributes have a
  * Normal distribution for each label value.
  * 
  * Comments :   For nominal attributes, if a label does not have
  * any occurences for a given attribute value
  * of the test instance, a probability of
  * noMatchesFactor * ( 1 / # instances in training set )
  * is used.
  *
  * For nominal attributes, if an attribute value does not
  * occur in the training set, the attribute is skipped
  * in the categorizer, since it does not serve to
  * differentiate the labels.
  *  
  * The code can handle dealing with unknowns as a special
  * value by doing the is_unknown only in the real attribute
  * case.
  *
  * Helper class NBNorm is a simple structure to hold the
  * parameters needed to calculate the Normal Distribution
  * of each Attribute,Label pair.  The NBNorms are stored in
  * a Array2 table "continNorm" which is indexed by attribute
  * number and label value.
  *
  * For continuous attributes the variance must not equal 0 since
  * it is in the denominator.  If the variance is undefined for
  * a label value (e.g. if a label only has only one instance
  * in the training set), NaiveBayesInd will declare the
  * variance to be defaultVariance, a static variable.  In
  * cases where the variance is defined but equal to 0,
  * NaiveBayesInd will declare the variance to be epsilon,
  * a very small static variable.
  *
  * For continous attributes, if a label does not occur in
  * the training set, a zero relative probability is
  * assigned.  If a label occurs in the training set but only
  * has unknown values for the attribute, noMatchesFactor is
  * used as in the nominal attribute case above.
  *
  * Complexity : categorize() is O(ln) where l = the number of categories
  * and n = the number of attributes.
  *
  * @author James Plummer 5/15/2001 Ported to Java
  * @author Eric Bauer and Clay Kunz 5/24/1996 Added L'aplace correction
  * @author Robert Allen 12/03/94 Initial revision
  */
public class NaiveBayesCat extends Categorizer {
  public final static String endl = new String("\n");
  // Member data (also see public data)
  private BagCounters nominCounts;		// hold data on nominal attributs
  private NBNorm[][] continNorm;		        // hold data on real attributes
  private double trainWeight;
  private int numAttributes;
  private boolean useLaplace;                     // turn on to activate Laplace correction
  private double mEstimateFactor;                // noise in Laplace correction
  private double[] attrImportance;               // importance values per attribute
  private boolean[] unkIsVal;               // should unknowns be special values? // decisions per attribute

  /** Ported from C++ > 
    *   enum UnknownIsValueEnum { unknownNo, unknownYes, unknownAuto }; //C++ equivalent
    */
  public static final int unknownNo = 1;
  public static final int unknownYes = 2;
  public static final int unknownAuto = 3;
  private int unknownIsValue; // 1, 2, 3.

  private double klThreshold;
   
  /** Fraction of a single occurence to use in cases when a label
    * has no occurences of a given nominal value in the training set:
    */
  private double noMatchesFactor;

  /** If true Evidence projection is used.
    */
  private boolean useEvidenceProjection;
  /** The scale factor to use with Evidence Projection.
    */ 
  private double evidenceFactor;

  /** Categorizer option defaults.
    */
  public static final double defaultMEstimateFactor = 1.0;
  public static final boolean defaultLaplaceCorrection = false;
  public static final int defaultUnknownIsValue = unknownNo;
  public static final double defaultKLThreshold = 0.1;
  public static final double defaultNoMatchesFactor = 0.0;
  public static final boolean defaultUseEvidenceProjection = false;
  public static final double defaultEvidenceFactor = 1.0;

  /** Value to use for Variance when actual variance = 0:
    */
  public static final double epsilon = .01;
  /** Value to use for Vaiance when actual variance is undefined becase there
    * is only one occurance.
    */
  public static final double defaultVariance = 1.0;

/** Constructor 
  * @param dscr - the description of this Inducer.
  * @param instList - training data.
  */
  public NaiveBayesCat(String dscr, InstanceList instList) {
    super(instList.num_categories(), dscr, instList.get_schema());
    nominCounts = instList.counters();
    trainWeight = instList.total_weight();
    numAttributes = instList.num_attr();
    logOptions.LOG(3, "NBC . . numAttributes = "+numAttributes);
    useLaplace = defaultLaplaceCorrection;
    mEstimateFactor = defaultMEstimateFactor;
    unkIsVal = null;
    unknownIsValue = defaultUnknownIsValue;
    klThreshold = defaultKLThreshold;
    noMatchesFactor = defaultNoMatchesFactor;
    useEvidenceProjection = defaultUseEvidenceProjection;
    evidenceFactor = defaultEvidenceFactor;

    attrImportance = this.compute_importance(instList);
    continNorm = this.compute_contin_norm(instList);
  }

  /** Copy Constructor.
    * @param source - the NaiveBayesCat to copy.
    */
  public NaiveBayesCat(NaiveBayesCat source) {
    super(source.num_categories(), source.description(), source.get_schema());
    nominCounts = new BagCounters(source.nominCounts);
    continNorm = source.copyContinNorm();
    attrImportance = source.copyAttrImportance();
    trainWeight = source.trainWeight;
    numAttributes = source.numAttributes;
    useLaplace = source.useLaplace;
    mEstimateFactor = source.mEstimateFactor;
    unkIsVal = null;
    unknownIsValue = source.unknownIsValue;
    klThreshold = source.klThreshold;
    noMatchesFactor = source.noMatchesFactor;
    useEvidenceProjection = source.useEvidenceProjection;
    evidenceFactor = source.evidenceFactor;
  }

  /** Categorizes a single instances based upon the training data.
    * @param instance - the instance to categorize.
    * @return the predicted category.
    */
  public AugCategory categorize(Instance instance) {
    CatDist cDist = score(instance);
    AugCategory cat = cDist.best_category();
    return cat;
  }
  /** Simple Method to return an ID.
    * @return - an int representing this Categorizer.
    * @deprecated CLASS_NB_CATEGORIZER has been deprecated
    */
  public int class_id() {return CLASS_NB_CATEGORIZER;}

  /** Returns a pointer to a deep copy of this NaiveBayesCat.
    * @return - the copy of this Categorizer.
    */
  public Object clone() {
    if ( !(this instanceof NaiveBayesCat) ) { 
      Error.fatalErr("NaiveBayesCat.clone: invoked for improper class");
    }
    return new NaiveBayesCat(this);
  }
  /** Compute the norms of the continuous attributes
    * @param instList - the instances to calculate.
    * @return the array[][] of NBNorms.
    */
  public static NBNorm[][] compute_contin_norm(InstanceList instList) {
    int contAttrCount = 0;
    int numCategories = instList.num_categories();
    Schema schema = instList.get_schema();
    int numAttributes = schema.num_attr();
   
    // start labels at -1 for unknown
    NBNorm[][] normDens = new NBNorm[numAttributes][numCategories + 1]; // no initial value
    for (int m=0; m<normDens.length;m++) {
      for (int n=0; n<normDens[m].length;n++) {
        normDens[m][n] = new NBNorm();
        normDens[m][n].set_mean_and_var(0,0);
      }
    }
      
    // loop through each attribute, and process all instances for each
    // continuous one
    for (int attrNum = 0; attrNum < numAttributes; attrNum++) {
      AttrInfo attrinfo = schema.attr_info(attrNum);
      if (attrinfo.can_cast_to_real()) {
	  // this is a continuous attribute
	  contAttrCount++;
	 
	  // read each occurance in the list and feed the stats for attribute
	  StatData[] continStats = new StatData[numCategories + 1];
        for (int j=0; j<continStats.length;j++) {
          continStats[j]=new StatData();
        }
//	  for (ILPix pix(instList); pix; ++pix) { //What?
        for (int i = 0; i < instList.num_instances(); i++) {
	    Instance inst = new Instance((Instance)instList.instance_list().get(i));
	    int labelVal = schema.label_info().cast_to_nominal().get_nominal_val(inst.get_label()); //for some reason the label values for the instances are one number higher than the actual value
	    MLJ.ASSERT(labelVal < numCategories, " NaiveBayesCat.compute_contin_norm()");

            // Ignore unknowns.
	    if ( !attrinfo.is_unknown(inst.get_value(attrNum))) {
	       double value = attrinfo.get_real_val(inst.get_value(attrNum));
	       continStats[labelVal].insert( value );
	    }
	  }

	  double mean;
        double var;
	  // extract Normal Density parameters into normDens table
	  for (int label = 0; label < numCategories; label++) {
	    if (continStats[label].size() == 0 ) {
	       mean = 0;
	       var = defaultVariance;
	    }
	    else {
	       mean = continStats[label].mean();
	       if (continStats[label].size() == 1 )
		  var = defaultVariance;
	       
	       else if ( (var = continStats[label].variance(0))<=0 )   // var == 0
		  var = epsilon;
	    }
	    normDens[attrNum][label].set_mean_and_var(mean,var);

	    //@@ pass in a log option?
	    //LOG(3, " Continuous Attribute # " << attrNum <<
	    //", Label " << label << ": Mean = " << mean <<
	    //", Variation = " << var << endl );
	  }
      } // end of handling this continous attribute
    }    // end of loop through all attributes

    if (contAttrCount==0) {  // no continous attributes found
      normDens = null;
    }
    return normDens;
  }

  /** Computes importance values for each nominal attribute using
    * the mutual_info (entropy).
    * Static function; used as helper by train() below.
    * @param instList - the instances to use.
    * @return - the array[] of importance values.
    */
  public static double[] compute_importance(InstanceList instList) {
    double[] attrImp = new double[instList.num_attr()];
    for (int i = 0; i < attrImp.length; i++) {
      attrImp[i] = 0;
    }
   
    double ent = Entropy.entropy(instList);
    if (ent == Globals.UNDEFINED_REAL) {
      Error.fatalErr("compute_importance: undefined entropy");
    }
    if(ent < 0 && -ent < MLJ.realEpsilon) {
      ent = 0;
    }
    for (int i=0; i<instList.num_attr(); i++) {
      if(instList.get_schema().attr_info(i).can_cast_to_real()) {
	  attrImp[i] = 0;
      }
      else if(instList.get_schema().attr_info(i).can_cast_to_nominal()) {
	  if(ent <= 0) {
	    attrImp[i] = 0;
        }
	  else {
	    double condEnt = Entropy.cond_entropy(instList.counters().value_counts()[i], instList.counters().attr_counts()[i], instList.total_weight());

	    if(condEnt < 0 && -condEnt < MLJ.realEpsilon) {
	       condEnt = 0;
          }
	    attrImp[i] = 100 - 100 * (condEnt / ent);
	    if(attrImp[i] < 0 && attrImp[i] >= -1000 * MLJ.realEpsilon) {
	       attrImp[i] = 0;  // avoid small negatives
          }
	    else if(attrImp[i] < 0) {
	       Error.fatalErr("compute_importance: attribute " + i +
		      " had importance " + attrImp[i] + "which is severly negative");
          }
	  }
      }
      else {
	 Error.fatalErr("compute_importance: attribute " + i + " has " +
	    "unsupported type.  Must be real or nominal.");
      }
    }
    return attrImp;
  }

  /** Computes the distance metrics for all attributes.
    * Should only be called once.
    */
  private void compute_kl_distances() {
    if(unkIsVal != null) {
      Error.fatalErr("NaiveBayesCat.compute_kl_distances: kl distances already computed");
    }
    unkIsVal = new boolean[get_schema().num_attr()];
    for(int i=0; i<get_schema().num_attr(); i++) {
      if(!get_schema().attr_info(i).can_cast_to_nominal()) {
	  Error.fatalErr("NaiveBayesCat.categorize: UNKNOWN_IS_VALUE is set and " +
	      get_schema().attr_name(i) + " is a real value with unknowns.  " +
            "UNKNOWN_IS_VALUE settings of " +
	      "yes and auto are not supported for undiscretized real values " +
	      "with unknowns.");
      }
      
      double dist = kl_distance(i);
      if(dist >= klThreshold) {
	  logOptions.LOG(1, "k-l distance for attribute " + get_schema().attr_name(i)
	      + " (" + dist + ") exceeds threshold" + endl);
	  unkIsVal[i] = true;
      }
      else {
        unkIsVal[i] = false;
      }
    }
  }

  /** copyAttrImportance copys the array of doubles stored in the attrImportance
    * Array and returns the new Array. This function is used to copy NaiveBayesCat
    * Objects.
    * @author James Plummer added to package for compatibility.
    * @return the new copy of attrImportance.
    */
  private double[] copyAttrImportance() {
    if ( this.attrImportance != null ) {
      double[] result = new double[attrImportance.length];
      for (int i = 0; i < attrImportance.length; i++) {
        result[i] = attrImportance[i];
      }
      return result;
    }
    else {
      return null;
    }    
  }

  /** copyContinNorm copys the array of NBNorms stored in the continNorm
    * Array and returns the new Array. This function is used to copy NaiveBayesCat
    * Objects.
    * @author James Plummer added for compatiblity.
    * @return the new copy of continNorm.
    */
  private NBNorm[][] copyContinNorm() {
    if ( this.continNorm != null ) {
      NBNorm[][] result = new NBNorm[continNorm.length][];
      for (int i = 0; i < continNorm.length; i++) {
        result[i] = new NBNorm[continNorm[i].length];
        for (int j = 0; j < continNorm[i].length; j++) {
          result[i][j] = new NBNorm(continNorm[i][j]);
        }
      }

      return result;
    }
    else {
      return null;
    }    
  }

  /** Prints a readable representation of the Cat to the
    *given stream.
    */
  public void display_struct(BufferedWriter stream, DisplayPref dp) {
    try {
    if (stream != null) {
      logOptions.set_log_stream(stream);
    }
    stream.write("Simple NaiveBayes Cat " + this.description() + 
                 " categorizing using prevalence data in BagCounter: "  + endl + 
                 nominCounts + endl);
   
    if ( continNorm != null ) {
      stream.write("Categorizing uses Normal Density to estimate probability" +
	  " of continuous attributes.  The mean, variance, and standard" +
	  " deviation of each attribute,label combination is: " + endl);
      for (int i = 0; i < numAttributes; i++) {
	  if ( nominCounts.value_counts()[i] != null )    // nominal attribute
	    stream.write("Attribute " + i + ":" + " Nominal Attribute." + endl);
	  else {
	    stream.write("Attribute " + i + ":" + endl);
	    for (int j = 0; j < num_categories(); j++) 
	       stream.write("  Label " + j + "\t\t" + continNorm[i][j].mean +
		              "\t" + continNorm[i][j].var + endl);
	  }
      }
    }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** findMax finds the largest value for an array of doubles
    * @author James Plummer added to match C++ functionality.
    * @param d - the array of doubles.
    * @return the maximum number.
    */
  public static double findMax(double[] d) {
    double result = d[0];
    for (int i = 1; i < d.length; i++) {
      if (result < d[i]) {
        result = d[i];
      }
    }
    return result;
  }

  /** findMin finds the smallest value for an array of doubles
    * @author James Plummer added to match C++ functionality.
    * @param d - the array of doubles.
    * @return the minimum number.
    */
  public static double findMin(double[] d) {
    double result = d[0];
    for (int i = 1; i < d.length; i++) {
      if (result > d[i]) {
        result = d[i];
      }
    }
    return result;
  }

  /** Helper function: generate a single probability using 
    * the Laplace correction.
    * Evidence projection is not used if there's no data
    * (labelCount == 0).
    */
  private double generate_cond_probability(double labelValCount, double labelCount,
					            int numAttrVals, int numAttr) {
    if(useEvidenceProjection && labelCount > 0) {
      double maxEvidence = MLJ.log_bin(1.0 + trainWeight*evidenceFactor);
      return CatDist.single_evidence_projection(labelValCount, labelCount, maxEvidence);
    }
    else if (useLaplace) {
      double effectiveMEstimate = mEstimateFactor;
      if(effectiveMEstimate == 0.0) {
	  effectiveMEstimate = 1.0 / trainWeight;
      }
      return (labelValCount + effectiveMEstimate) / (labelCount + numAttrVals * effectiveMEstimate);
    }
    else if (labelValCount == 0) {
      if(noMatchesFactor >= 0) {
	  return noMatchesFactor / trainWeight;
      }
      else if(noMatchesFactor == -1) {
	  return (double)(labelCount) / trainWeight / trainWeight;
      }
      else if(noMatchesFactor == -2) {
	 return (double)(labelCount) / trainWeight
	    / (trainWeight * this.get_schema().num_attr());
      }
      else {
	 Error.fatalErr("NaiveBayesCat.generate_cond_probability: noMatchesFactor has illegal value of " +
	    noMatchesFactor);
	 return 0;
      }
    }
    else {
      // if labelCount == 0, then labelValCount should also be 0 and we'll
      // choose the case above instead of this one.
      MLJ.ASSERT( (labelCount > 0), "NaiveBayesCat.generate_cond_probability()");
      return (double)(labelValCount / labelCount);
    }
  }

  /** Helper function: generate a single probability.  Allow
    * for Laplace correction.
    */
  private double generate_probability_prior(double labelCount, int numLabels) {
   if(useEvidenceProjection) {
      double maxEvidence = MLJ.log_bin(1.0 + trainWeight*evidenceFactor);
      return CatDist.single_evidence_projection(labelCount, trainWeight, maxEvidence);
   }
   else if(useLaplace) {
      double effectiveMEstimate = mEstimateFactor;
      if(effectiveMEstimate == 0.0) {
	 effectiveMEstimate = 1.0 / trainWeight;
      }
      return (labelCount + effectiveMEstimate) / (trainWeight + numLabels * effectiveMEstimate);
   }
   else if(labelCount == 0)
      return 0;

   else {
      // if labelCount == 0, then labelValCount should also be 0 and we'll
      // choose the case above instead of this one.
      MLJ.ASSERT( (labelCount > 0), "NaiveBayesCat.generate_probablility()");
      return labelCount / trainWeight;
   }
  }
  
  /** Removed function */
/*  public void generate_viz(BufferedWriter stream, boolean[] autoDiscVector, int evivizVersion) throws IOException {
  }*/

  /** fuctions for retrieving and setting optional variables. */
  public double get_evidence_factor() { return evidenceFactor; }
  public double get_kl_threshold() { return klThreshold; }
  public double get_m_estimate_factor() { return mEstimateFactor; }
  public double get_no_matches_factor() { return noMatchesFactor; }
  public int get_unknown_is_value() { return unknownIsValue; }
  public boolean get_use_evidence_projection() { return useEvidenceProjection; }
  public boolean get_use_laplace() { return useLaplace; }
  public void set_evidence_factor(double f) { evidenceFactor = f; }
  public void set_kl_threshold(double th) { klThreshold = th; }

  /** Initialize the probabilities to be the class probabilities
    * P(L = l) 
    * @param nominCoutns - the BagCounter to initilize.
    */
  public static void init_class_prob(BagCounters nominCounts,
                      double trainWeight,
                      double[] prob, boolean useLaplace,
                      boolean useEvidenceProjection,
                      double evidenceFactor)
  {
    if (useEvidenceProjection) {
      for (int labelVal = 0; labelVal < prob.length; labelVal++) {
        prob[labelVal] = nominCounts.label_count(labelVal);
      }
      CatDist.apply_evidence_projection(prob, evidenceFactor, true);
    }
    else if (useLaplace) {
      int numLabels = prob.length - 1;
  
      // No laplace correction for unknown label.  This fixes bug #526924
      MLJ.ASSERT(nominCounts.label_count(Globals.UNKNOWN_CATEGORY_VAL) == 0,"NaiveBayesCat.init_class_prob()");
      prob[Globals.UNKNOWN_CATEGORY_VAL] = 0;
      for (int labelVal = 1; labelVal < prob.length; labelVal++) {
        prob[labelVal] = (double)(nominCounts.label_count(labelVal) + 1)/(trainWeight + numLabels);
      }
    }
    else {
      for (int labelVal = 0; labelVal < prob.length; labelVal++) {
        prob[labelVal] = (double)(nominCounts.label_count(labelVal))/(trainWeight);
      }
    }
    
    // Check that probabilities sum to about 1.0
    double probSum = sumArray(prob);
    MLJ.verify_approx_equal(probSum,1,"NaiveBayesCat.init_class_prob: prob does not sum to one");
  }

  /** Compute the KL distance metric for a single attribute.
    * If we don't have minimum support, we always return 0
    * @param attrNum - the number of the attribute to compute distances.
    * @return the distance for the attribute.
    */
  private double kl_distance(int attrNum)
  {
    int numLabelVals = get_schema().num_label_values();
    double[] p = new double[numLabelVals];
    double[] q = new double[numLabelVals];
   
    if(!get_schema().attr_info(attrNum).can_cast_to_nominal()) {
      Error.fatalErr("NaiveBayesCat.kl_distance: this function does not work " +
	 "for real attributes");
    }
    double support = nominCounts.attr_count(attrNum, Globals.UNKNOWN_CATEGORY_VAL);
    MLJ.verify_strictly_greater(trainWeight,0.0,"NaiveBayesCat.kl_distance: " +
			       "total train weight is negative");
    if (support < 5) { // @@ make this an option
      return 0;         
    }
    int numLabelValues = get_schema().num_label_values();

    for(int i=0; i < numLabelValues; i++) {
      // Compute p(C) and p(C|?) with laplace correction so we
      //   avoid zeros and can do KL distance.
      q[i] = (nominCounts.label_count(i) + 1)/(trainWeight + numLabelValues);

      MLJ.ASSERT(support > 0,"NaiveBayesCat.kl_distance()");
      p[i]=(nominCounts.val_count(i, attrNum, Globals.UNKNOWN_CATEGORY_VAL) + 1)/(support + numLabelValues); 
    } 

    // now get the distance

    logOptions.LOG(3, "p=" + p + "\nq=" + q + endl);

    double dist = this.kullback_leibler_distance(p, q);
    logOptions.LOG(2, "k-l distance for attribute " + this.get_schema().attr_name(attrNum) +
        " (" + attrNum + "): " + dist + endl);
   
    return dist;
  }

  /** Removed function */
/*  boolean operator==(Categorizer rhs) {
  }
*/

  /** Removed function */
/*  public void make_persistent(PerCategorizer_ dat) {
*/

  /** Compute a Kullback Leibler distance metric given an array
    * of p(x) and q(x) for all x.
    * The Kullback Leibler distance is defined as:
    * sum over all x  p(x) log(p(x)/q(x))
    * We assume that no p(x) or q(x) are zero
    * @param p - the first array
    * @param q - the second array.
    * @return - the computer distance between p and q.
    */
  public static double kullback_leibler_distance(double[] p, double[] q) {
    if(p.length != q.length) {
      Error.fatalErr("kullback_leibler_distance: p and q arrays have different " +
	               "sizes");
    }
    // @@ The KL distance needs to be fixed so that p can be zero
    // @@ (real-world prob).  In that case p*log(p/q) = 0, which
    // @@ we need to special case.  Ronny

    double sum = 0;
    double sump = 0;
    double sumq = 0;
    for(int i=0; i<p.length; i++) {
	 if(p[i] <= 0) {
	    Error.fatalErr("p(" + i + ") <= zero");
       }
	 if(q[i] <= 0) {
	    Error.fatalErr("q(" + i + ") <= zero");
       }
	 sum += (p[i] * MLJ.log_bin(p[i] / q[i]));
	 sump += p[i];
	 sumq += q[i];
    }

    MLJ.verify_approx_equal(sump, (double)(1), "kullback_leibler_distance:: " +
			   "sum of p doesn't add to 1");

    MLJ.verify_approx_equal(sumq, (double)(1), "kullback_leibler_distance:: " +
			   "sum of q doesn't add to 1");

    if (sum < 0) {
      Error.err("kullback_leibler_distance: sum < 0: " + sum + endl);
    }
    return sum;
  }

  /** Prints detail probability values with attribute names.
    * Used when logging maximum detail in categorize().
    */
  private void log_prob(double[] prob, Schema instSchema) throws IOException {
    for (int i = 0; i <= prob.length; i++) {
      logOptions.get_log_stream().write("Prob=" + prob[i] +
                             " for label " + i + " (" +
	                       instSchema.category_to_label_string(i) + ')' + endl);
      logOptions.get_log_stream().write(endl);
    }// <--this might not go here

  }

  /** Check state of object after training.  Checks
    *    1) BagCounter is ok, and
    *    2) the number of test cases is > 0, and
    *    3) that there are no variances = 0.
    */
  public void OK(int level) {
//e55    nominCounts.OK(level);
   
    if (nominCounts.label_counts().length < 2 ) { // 1 comes free for unkn
      Error.fatalErr("NaiveBayesCat.OK: BagCounter has less than 2 labels");
    }
    MLJ.verify_strictly_greater(trainWeight, 0, "NaiveBayesCat.OK: total training weight " +
                                "must be strictly greater than zero");
    if ( continNorm != null ) {
      int labelNumVals = nominCounts.label_counts().length - 1;
      for (int attrVal = 0; attrVal < numAttributes; attrVal++) {
        if ( nominCounts.value_counts()[attrVal] == null ) { // continuous //JWP if this doesn't work use extra variable!!!
          for (int labelVal = -1; labelVal < labelNumVals; labelVal++) {
            NBNorm nbn = continNorm[attrVal][labelVal];
//            NBNorm nbn = (*continNorm)(attrVal,labelVal); //what the heck is this???
            if ( !nbn.hasData ) {
	        Error.fatalErr("NaiveBayesCat.OK: Normal Distribution data " +
	          "missing for (label, attribute) (" + labelVal + ", " + attrVal + "). ");
            }
            if ( nbn.var<=0 ) {
              Error.fatalErr("NaiveBayesCat.OK: Varience must be > 0 for continuous attributes.  Varience = for " +
                 "(label, attribute) (" + labelVal + ", " + attrVal + "). ");
            }
          }
        }
      }
    }
  }

  /** Rescale the probabilities so that the highest is 1
    * This is to avoid underflows.
    * @param prob - an array of probabilities to be scaled.
    * @report Possbile bug; function prob will not hold the values on return.
    */
  static void rescale_prob(double[] prob) {
    double maxVal = findMax(prob);
    // Performance shows this is time critical
    // Avoid division by zero.  Happens on shuttle-small.
    if (maxVal > 0) {
      // int numLabels = prob.length; moved inside for
      for (int labelVal = 0; labelVal < prob.length; labelVal++) {
        prob[labelVal] /= maxVal;
        MLJ.ASSERT( (prob[labelVal] >= 0), "NaiveBayesCat.rescale_prob()");
      }
    }
  }

  /** Returns a category given an instance by checking all 
    * attributes in schema and returning category with highest
    * relative probability.
    * The relative probability is being estimated for each label.
    * The label with the highest values is the category returned.
    * The probability for a given label is
    * P(Nominal Attributes)*P(Continuous Attributes)
    * Since the probability is a product,  we can factor out any
    * constants that will be multiplied times every label, since
    * this will not change the ordering of labels.
    * P(Continuous Attribute Value X) is caculated using the normal
    * density: 
    * Normal(X) = 1/(sqrt(2*pi)*std-dev)*exp((-1/2)*(X-mean)^2/var)
    * This calculation can be stripped of the constant
    * (sqrt(2*pi)) without changing the outcome.
    * P(Nominal Attributes) is calculated as the percentage of a
    * label's training set that had the test instance's value
    * for a each attribute.
    * The majority label is returned if all are equal.
    * See this file's header for more information.
    * @param instance - the instance to be scored.
    */
  public CatDist score(Instance instance) {
    int attrNum;
    int labelVal;
    Schema Schema = get_schema();
    int labelNumVals = get_schema().num_label_values();
    // starts at -1 for the unknown category
    double[] prob = new double[labelNumVals + 1];
    // Sanity check: the number of attributes and the number of labels of
    // the training set of the categorizer should correspond with the Schema
    // of the instance being categorized.

    //?OK();
    logOptions.LOG(3, "Instance to categorize: ");
    //IFLOG(3,instance.display_unlabelled(logOptions.get_log_stream())); 
    logOptions.LOG(3, endl);

    int trainLabelCount = nominCounts.label_counts().length - 1;
    if ( labelNumVals != trainLabelCount || numAttributes != get_schema().num_attr()) {
      Error.fatalErr("NaiveBayesCat.categorize: Schema of instance to be categorized does not match Schema of training set.");
    }
    init_class_prob(nominCounts, trainWeight, prob, useLaplace, useEvidenceProjection, evidenceFactor);
    logOptions.LOG(4, "Initial class probabilities" + endl);
    //IFLOG(4, this.log_prob(prob, schema));

    // compute kl distances here if needed.  Break constness to keep
    // categorize() a logically const function.
    // We compute these here instead of in the constructor because we don't
    // know if we're using unknown auto mode until we actually categorize.
    if(unknownIsValue == unknownAuto && unkIsVal == null) {
      (this).compute_kl_distances();
    }
    // loop through each attribute in instance:
    for (attrNum=0; attrNum < numAttributes; attrNum++) {
      AttrInfo ai = get_schema().attr_info(attrNum);
      if(!ai.can_cast_to_nominal() && unknownIsValue != unknownNo) {
	 Error.fatalErr("NaiveBayesCat.categorize: UNKNOWN_IS_VALUE is set and " +
	    ai.name() + " is a real value with unknowns.  UNKNOWN_IS_VALUE settings of " +
	    "yes and auto are not supported for undiscretized real values " +
	    "with unknowns.");
      }
	 
      // determine whether or not to treat unknowns as values for this
      // attribute.
      boolean useUnknowns = false;
      if(unknownIsValue == unknownYes) {
	  useUnknowns = true;
      }
      else if(unknownIsValue == unknownAuto) {
	  MLJ.ASSERT(unkIsVal[attrNum], "NaiveBayesCat.score(): unkIsVal["+attrNum+"]");
	  if(unkIsVal[attrNum]) {
	    useUnknowns = true;
        }
      }
      
      if (!useUnknowns && ai.is_unknown(instance.get_value(attrNum))) {
	  logOptions.LOG(4, "Skipping unknown value for attribute " + attrNum + endl);
      }
      else {
	 // continuous attr
	 if ( nominCounts.value_counts()[attrNum] == null ) {
	    logOptions.LOG(4, endl + "Continuous Attribute " + attrNum + endl);

	    MLJ.ASSERT( continNorm != null, "NaiveBayesCat.score(): continNorm");
	    if ( !ai.can_cast_to_real() ) {
	       Error.fatalErr("NaiveBayesCat.categorize: Schema of instance to be " +
		  "categorized does not match Schema of training set. " +
		  "Attribute Number " + attrNum + " is continuous in training " +
		  "set and nominal in instance schema.");
          }
	    double realVal = ai.get_real_val(instance.values[attrNum]);
	    for (labelVal = 0; labelVal < prob.length; labelVal++) {
	       NBNorm nbn = continNorm[attrNum][labelVal];
	       double distToMean = realVal - nbn.mean;
	       double stdDev = Math.sqrt(nbn.var);
	       double e2The = Math.exp( -1 * distToMean * distToMean / (2*nbn.var) );
	       prob[labelVal] *= e2The / stdDev;
		  
	       logOptions.LOG(5, " P(" + labelVal + "): times " + e2The / stdDev
		   + ", X = " + realVal + ",  Probability so far = " +
	         prob[labelVal] + endl);
	    }
	 }
	 else { // nominal attribute
	    logOptions.LOG(4, endl + "Nominal Attribute " + attrNum + endl);
	    if ( ! ai.can_cast_to_nominal() ) {
	       Error.fatalErr("NaiveBayesCat.categorize: Schema of instance to be " +
		  "categorized does not match Schema of training set. " +
		  "Attribute Number " + attrNum + " is nominal in training " +
		  "set and continuous in instance schema.");
          }
	    NominalAttrInfo nai = ai.cast_to_nominal();
	    int nomVal = nai.get_nominal_val(instance.get_value(attrNum));

	    double[] estProb = new double[prob.length];
          for (int i = 0; i < estProb.length; i++) {
            estProb[i] = 0.0;
          }	    

	    // The value should never be out of range of the BagCounter.
	    // Even in a non-fixed value set, this value should have
	    // been converted into an unknown during reading/assimilation.
	    if(nomVal > nominCounts.attr_counts()[attrNum].length-1 /*[nominCounts.attr_counts()[attrNum].length - 1]*/ ) {
	       Error.fatalErr("Value for attribute " + attrNum + " is out of " +
		  "range.  This indicates that this instance was not " +
		  "correctly assimilated into the training schema.");
          }
	    else {
	      // loop through each label val, updating cumulative vector
	      // The generate_cond_probability function provides several
	      // options for handling zero counts here.
	      for (labelVal = 0;labelVal < prob.length;labelVal++) {
		  // include unknowns in the label count: (langley didn't)
              double labelCount = nominCounts.label_count(labelVal);
              double labelMatch = nominCounts.val_count(labelVal, attrNum, nomVal);
              // If there are Nulls, we have to account for them
              //   in the laplace correction, or else the sum of
              //   the probabilities don't sum up to 1.  
              //   A failure will occur in t_DFInducer.c
              boolean hasNulls = (nominCounts.attr_count(attrNum, Globals.UNKNOWN_CATEGORY_VAL) > 0);
		  estProb[labelVal] = generate_cond_probability(labelMatch, labelCount, nai.num_values() + (hasNulls?1:0), numAttributes);
	      }

	      // The evidence projection algorithm produces unnormalized
	      // probabilities.  If we're using this algorithm, normalize
	      // here.
	      if(useEvidenceProjection) {
              double sum = sumArray(estProb);
              for(labelVal = 0; labelVal < prob.length; labelVal++) {
		    estProb[labelVal] /= sum;
              }
	      }
          }

	    // accumulate probabilities
	    for(int i=0; i < estProb.length; i++) {
	       prob[i] *= estProb[i];
	       logOptions.LOG(4, "P | L=" + i + " = " + estProb[i] + ".  Cumulative prob = " + prob[i] + endl);
	    }
	  }

	  // Since these are unscaled relative probabilities, we rescale them
	  //   to 0-1, so that we don't get underflows
	  this.rescale_prob(prob);
	  logOptions.LOG(4, "Relative probabilities after scaling: " + endl);
	  //IFLOG(4, this.log_prob(prob, schema));
      } // if unknown
    }

    // place the probabilities into a CatDist
    CatDist retDist = new CatDist(get_schema(), prob, CatDist.none, 1.0);

    // set the tiebreaking ordering to favor label values with more
    // instances (higher label counts)
    //JWP retDist.set_tiebreaking_by_values(nominCounts.label_counts());
   
    return retDist;
  }

  /** set m value for L'aplace correction.
    * @param m - the new m-estimate factor.
    */
  public void set_m_estimate_factor(double m) {
    if (m < 0) {
      Error.fatalErr("NaiveBayesCat.set_m_estimate_factor() : illegal m_estimate_" +
	 "factor value : " + m);
    }
    else {
      mEstimateFactor = m;
    }
  }

  public void set_no_matches_factor(double nm) { noMatchesFactor = nm; }

  /** set_unknown_is_value sets the value of unknownIsValue. the variable unknownIsValue must be either
    * 1, 2, or 3. Any other value fails and gives an Error.
    * @param unk - the new value of unknownIsValue
    */
  public void set_unknown_is_value(int unk) {
    if (unk >= 1 && unk <= 3) {
      unknownIsValue = unk;
    }
    else {
      Error.err("NaiveBayesCat.set_unknown_is_value(): unknownIsValue cannot be " + unk);
    }
  }

  public void set_use_evidence_projection(boolean b) { useEvidenceProjection = b;}
  public void set_use_laplace(boolean lap) { useLaplace = lap; }

  /** sumArray() adds the values of all the ellements in the given array
    * @param d[] and array of doubles to add
    * @return the sum of the doubles
    */
  public static double sumArray(double [] d) {
    double result = 0.0;
    for( int i = 0; i < d.length; i++) {
      result += d[i];
    }
    return result;
  }

  public boolean supports_backfit() { return false; }
  public double total_train_weight() { return trainWeight; }
}
