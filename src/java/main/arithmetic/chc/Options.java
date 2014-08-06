package arithmetic.chc;

import java.util.StringTokenizer;

import arithmetic.shared.GetEnv;
import arithmetic.shared.LogOptions;

/** This class holds the run time options for the CHC program. It
  * also verifies options are not changed during the data run and
  * that all given options are viable. */
public class Options {
    /** The chc printing options are stored in this array. */
    private BitMask bitmask = new BitMask();
    /** The stoping generation. */
    public int finalgeneration;
    /** The maximum size of the population. */
    public int populationsize;
    /** The name of the inducer to use. */
    public String optioninducer;
    /** The dataDistributor to use during testing. */
    private DataDistributor dist = null;

    /** The printing object for all CHC classes to use. */
    public static LogOptions logOptions = new LogOptions("WRAPPER");

  public Options () {
    GetEnv getEnv = new GetEnv();
    finalgeneration = getEnv.get_option_int("finalgeneration");
    populationsize = getEnv.get_option_int("populationsize");
    optioninducer = getEnv.get_option_string("inducer");
    System.out.println(optioninducer);

    String temp = getEnv.get_option_string("FitnessDistribution");
    StringTokenizer token = new StringTokenizer(temp, ",");
    double[] fdist = new double[3];
    for (int i = 0; i < fdist.length; i++) {
      fdist[i] = Double.valueOf(token.nextToken()).doubleValue();
    }
    Hypothesis.setFDist(fdist);
  }

  public String getDisplayString() {
    return "";
  }

  public void setDistributor(String arg, boolean system) {
    if (dist == null) {
      dist = new DataDistributor(arg, system);
    }
    else {
      logOptions.LOG(0, "Cannot change dist. Distributor already set.");
    }
  }

  public DataDistributor getDist() {
    if (dist == null) {
      logOptions.LOG(0, "No Distributor set.");
      logOptions.LOG(0, "Exit(1)");
      System.exit(1);
    }
    return dist;
  }

  public void setBitMask(String o) {
    bitmask.setBitMask(o);
  }

  public boolean getMask(String o) {
    return bitmask.getMask(o);
  }
  
  public String getBitMaskDisplayString() {
    return bitmask.getDisplayString();
  }

  public static void LOG(int level, String message) {
    logOptions.LOG(level, message);
  }
  public static void ERR(int level, String message) {
    logOptions.ERR(level, message);
  }
  public void set_log_level(int level) {
    logOptions.set_log_level(level);
  }
  public int get_log_level() {
    return logOptions.get_log_level();
  }
  
  public int getPopArraySize() {
    if (!getMask("print_all_hypothesis")) {
      return 0;
    }
    else {
      return (finalgeneration*populationsize + populationsize*2);
    }
  }
}


