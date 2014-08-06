package arithmetic.nb;

import arithmetic.shared.InstanceList;
import arithmetic.shared.LogOptions;

/** NDriver is provided as an example. This class creates and runs the
  * NaiveBayes Inducer for the specified options. 
  * Usage: java NDriver filename [options]
  * Options:
  *       l - if present NB uses Laplace Correction; default is off; 
  *       ep - if present Evidence Project is used; default is off;
  *       mest=# - the m estimate factor used in NB is set to #; default is 1.0;
*/
public class NDriver {
  /** an internal identifier used to find command line options. */ 
  public static final String useLaplaceToggle = new String("l");
  /** an internal identifier used to find command line options. */ 
  public static final String useEvidenceProjectionToggle = new String("ep");
  /** an internal identifier used to find command line options. */ 
  public static final String mEstimateFactorChip = new String("mest=");
  /** the main method which creates the NB inducer and prints the error.
    * @param args - an array with the command line arguments.
    */ 
  public static void main(String[] args) {
    NaiveBayesInd nbi = new NaiveBayesInd("NaiveBayesInd");
      nbi.set_log_options(new LogOptions("INDUCER"));
      nbi.set_m_estimate_factor(1.0);
    String[] toggles;
    if(args.length > 1) {
      toggles = new String[args.length - 1]; 
      for (int i = 1; i < args.length; i++) {
        if (args[i].toLowerCase().equals(useLaplaceToggle)) {
          nbi.set_use_laplace(true);
        }
        if (args[i].toLowerCase().equals(useEvidenceProjectionToggle)) {
          nbi.set_use_evidence_projection(true);
        }
        if (args[i].length() >= 5) {
          if (args[i].substring(0,5).toLowerCase().equals(mEstimateFactorChip) ) {
            double xyz = Double.parseDouble(args[i].substring(5)); 
            nbi.set_m_estimate_factor(xyz);
          }
        }
      }
    }
      System.out.println(" . . . . . . . . . . . . Options Set");

    InstanceList traindata = new InstanceList(args[0],".names",".data");
    InstanceList testdata = new InstanceList(args[0],".names",".test");

    System.out.println("Error for "+args[0]+" is "+nbi.train_and_test(traindata, testdata));
    System.out.println("  Use Laplace Correction :    " + nbi.get_use_laplace());
    System.out.println("  Use Evidence Projection:    " + nbi.get_use_evidence_projection());
    System.out.println("  M Estimate Factor      :    " + nbi.get_m_estimate_factor());

  }
}
