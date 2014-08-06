package arithmetic.nb;

import java.io.BufferedWriter;
import arithmetic.shared.Error;
import java.io.IOException;

import arithmetic.shared.Categorizer;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Inducer;

public class NaiveBayesInd extends Inducer {
  public String endl = new String("/n");

public final String LAPLACE_HELP =
  "Use the L'aplace correction for frequency counts " +
  "in probability estimates.";

public final String M_FACTOR_HELP =
  "Used in laplace correction only; denotes the amount of noise " +
  "expected in the conditional probability correction.";

public final String UNKNOWN_IS_VALUE_HELP =
  "When this option is true, unknowns will be treated as full-fledged " +
  "values.  When this option is false, unknowns will be treated as missing " +
  "information.  When this option is auto, the algorithm will attempt to " +
  "automatically determine how to treat unknowns.";

public final String KL_THREHSOLD_HELP =
  "This option determines the threshold value of kl-distance for " +
  "an unknown to be treated as a full value in auto mode.";

public final String USE_EVIDENCE_PROJECTION_HELP =
  "This option allows selection of the evidence projection algorithm " +
  "to correct probabilities during categorization.";

public final String EVIDENCE_FACTOR_HELP =
  "This option allows scaling of the total weight of input instances " +
  "for the purposes of computing total evidence available during " +
  "evidence projection.";

   // Member data
   /** Stores the categorizer this Inducer will train. */
   private NaiveBayesCat categorizer;

  /** set whether to use laplace correction. 
    */ 
  private boolean useLaplace;
  /** noise in laplace correction.
    */
  private double mEstimateFactor;
  private double klThreshold;
  private double noMatchesFactor;
  private boolean useEvidenceProjection;
  private double evidenceFactor;   

  //How to handle the Enums
  //private UnknownIsValueEnum unknownIsValue;
  /* the value for unknownNo in the enum. */
  public static final int unknownNo = 1;
  /* the value for unknownYes in the enum. */
  public static final int unknownYes = 2;
  /* the value for unknownAuto in the enum. */
  public static final int unknownAuto = 3;
  /** used to produce the effect of enums in C++. */
  private int unknownIsValue; // 1, 2, 3.

  /** returns the Id of this inducer
    */
  public int class_id() {return NAIVE_BAYES_INDUCER;}


  /** Constructor with description String.
    * @param description - the description of this inducer.
    */
  public NaiveBayesInd(String description) {
    super(description);
    categorizer = null;
    useLaplace = NaiveBayesCat.defaultLaplaceCorrection;
    mEstimateFactor = NaiveBayesCat.defaultMEstimateFactor;
    unknownIsValue = NaiveBayesCat.defaultUnknownIsValue;
    klThreshold = NaiveBayesCat.defaultKLThreshold;
    noMatchesFactor = NaiveBayesCat.defaultNoMatchesFactor;
    useEvidenceProjection = NaiveBayesCat.defaultUseEvidenceProjection;
    evidenceFactor = NaiveBayesCat.defaultEvidenceFactor;
  }


  /** Copy constructor
    * @param source - the Inducer to copy.
    */
  public NaiveBayesInd(NaiveBayesInd source) {
    super(source);
    categorizer = null;
    useLaplace = source.useLaplace;
    mEstimateFactor = source.mEstimateFactor;
    klThreshold = source.klThreshold;
    noMatchesFactor = source.noMatchesFactor;
    unknownIsValue = source.unknownIsValue;
    useEvidenceProjection = source.useEvidenceProjection;
    evidenceFactor = source.evidenceFactor;
  }


  /** Tests to see if this inducer was properly trained by
    * checking for a categorizer. Return TRUE iff the class
    * has a valid categorizer.
    * @param fatal_on_false - a value which tells the system a
    *         fatal error has occurred if this method should return
    *         false.
    */
  public boolean was_trained(boolean fatal_on_false) {
   if( fatal_on_false && categorizer == null ) {
      Error.err("NaiveBayesInd.was_trained(): No categorizer, " +
	 "Call train() to create categorizer");
   }
   return ( categorizer != null );
  }

  /** Returns the categorizer that the inducer has generated.
    * @return the categorizer trained by this inducer.
    */
  public Categorizer get_categorizer() {
    if (was_trained(true)) {
      return categorizer;
    }
    else {
      return null;
    }
  }

  /** Gives ownership of the generated categorizer to the
    * caller, reverting the Inducer to untrained state.
    * @return the categorizer trained by this inducer.
    */
  public Categorizer release_categorizer() {
    if (was_trained(true)) {
      Categorizer retCat = categorizer;
      categorizer = null;
      return retCat;
    }
    else {
      return null;
    }
  }

  /** Method no available at this time.
    * Sets the options from environment variables.
    * @param preFix - a String prefix to be added to all the options.
    */
  public void set_user_options(String preFix) {
/*    boolean laplaceOption = get_option_bool(preFix + "LAPLACE_CORRECTION",
				useLaplace,
				LAPLACE_HELP, true);
    set_use_laplace(laplaceOption);
    if (laplaceOption) {
      set_m_estimate_factor(get_option_real(preFix + "M_ESTIMATE_FACTOR",
					    mEstimateFactor,
					    M_FACTOR_HELP, true));
    }

    // set noMatchesFactor option
    noMatchesFactor = 
      get_option_real(preFix + "NO_MATCHES_FACTOR", noMatchesFactor,
		     "", true);

    // set unknown is value option
    unknownIsValue =
      get_option_enum(preFix + "UNKNOWN_IS_VALUE", unknownIsValueMEnum,
		     unknownIsValue, UNKNOWN_IS_VALUE_HELP, true);
    if(unknownIsValue == unknownAuto)
      klThreshold = get_option_real(preFix + "KL_THRESHOLD",
				    klThreshold, KL_THREHSOLD_HELP, true);

    // set evidence projection option.  Always suppress Laplace and
    // no matches factor if this is activated
    boolean projOption = get_option_bool(preFix + "EVIDENCE_PROJECTION",
				     useEvidenceProjection,
				     USE_EVIDENCE_PROJECTION_HELP, true);
    if(projOption) {
      evidenceFactor = get_option_real(preFix + "EVIDENCE_FACTOR",
				       evidenceFactor,
				       EVIDENCE_FACTOR_HELP, true);
    }
    useEvidenceProjection = projOption;
*/
  }


  /** Trains Naive Bayes Categorizer.  Descrete attributes can
    * be handled by simply passing occurance counts in the
    * BagCounter.  Continuous attributes are fed into
    * StatDatas to get mean, variance, and standard deviation.
    *
    * It is possible that some label is not in the training set, or
    * it has unknown for a continous attribute.  In this case,
    * statData::size() = 0, and the loop below will not write
    * any data into the NBNorm structure.  Since the NBNorm
    * structure is initialized to HasData = FALSE, doing nothing
    * will correctly indicate that there is no data.
    */
  public void train() {
//?    has_data();
//?    DBG(OK());
    categorizer = new NaiveBayesCat( this.description(), TS);
    categorizer.set_log_level(get_log_level());
    categorizer.set_use_laplace(useLaplace);
    categorizer.set_m_estimate_factor(mEstimateFactor);
    categorizer.set_unknown_is_value(unknownIsValue);
    categorizer.set_kl_threshold(klThreshold);
    categorizer.set_no_matches_factor(noMatchesFactor);
    categorizer.set_use_evidence_projection(useEvidenceProjection);
    categorizer.set_evidence_factor(evidenceFactor);
  }

  /** Prints a readable representation of the Cat to the
    * given stream.
    * @param stream - the stream to print to.
    * @param dp - the display preferences.
    */
  public void display(BufferedWriter stream, DisplayPref dp) throws IOException {
    display_struct(stream, dp);
    if (dp.preference_type() != DisplayPref.ASCIIDisplay) {
      Error.fatalErr("NaiveBayesInd.display_struct: Only ASCIIDisplay is " +
                    "valid for this display_struct");
    }
    stream.write("NaiveBayes Inducer " + description() + endl);
    if ( was_trained(false) ) {
      stream.write("Current Categorizer " + endl);
      get_categorizer().display_struct(stream, dp);
    }
  
  }

  /* Returns the pointer to the copy of this.
   */
  public Inducer copy() {
    Inducer ind = new NaiveBayesInd(this);
    return ind;
  }

  /** set whether to use L'aplace correction.
    * @param laplace - if true use laplace correction.
    *                  if false don't use laplace.
    */
  public void set_use_laplace(boolean laplace) {
    useLaplace = laplace;
  }

  /** set m value for L'aplace correction.
    * @param m - the value to set the m estimate factor.
    */
  public void set_m_estimate_factor(double m) {
    if (m < 0) {
      Error.fatalErr("NaiveBayesInd.set_m_estimate_factor() : illegal m_estimate_" +
	  "factor value : " + m);
    }
    else {
      mEstimateFactor = m;
    }
  }

  /** gets the value for the use laplace option.
    * @return true if laplace is on; false if off.
    */
  public boolean get_use_laplace() {
    return categorizer.get_use_laplace();
  }

  /** gets the value for the m estimate factor used in the
    * categorizer.
    * @return the value for the factor.
    */
  public double get_m_estimate_factor() {
    return categorizer.get_m_estimate_factor();
  }

  /** get the value for the use evidence projection used in the
    * categorizer.
    * @return true if use evidence projection is on; false otherwise.
    */
  public boolean get_use_evidence_projection() {
    return categorizer.get_use_evidence_projection();
  }

  /** sets the value for use evidence projection. The effect is not
    * perpetuated to the categorizer until train is called.
    * @param projection - true turns the evidence projection on; false
    *                     turns if off.
    */
  public void set_use_evidence_projection(boolean projection) {
    useEvidenceProjection = projection;
  } 

  /** This method returns 0 because NaiveBayes is not a tree inducer.
    * There is no tree for which to count nodes.
    * @return 0;
    */
  public int num_nontrivial_nodes() {
    return 0;
  }
  /** This method returns 0 because NaiveBayes is not a tree inducer.
    * There is not tree for which to count leaves
    * @return 0
    */
  public int num_nontrivial_leaves() {
    return 0;
  }
}
