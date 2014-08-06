package arithmetic.chc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import arithmetic.shared.ScoringMetrics;

  /** THSS is a class that provides a set of protocol for sending the
    * information needed to test a hypothesis through a socket in the
    * form of Strings.
    */
  public class THSS extends Thread { //Test Hypothesis Socket System
    public static int maxwaittime = 5000; // milliseconds

    private Hypothesis hypo = null;
    private Socket socket = null;
    private Sys sys = null;
    private String inducer = null;
    private String filename = null;
    private PrintWriter out = null;    
    private BufferedReader in = null;
    private long starttime;

    public boolean recovered = false;

    public THSS(Socket socket, Hypothesis hypo, String ind, String filename) {
      System.out.println("New THSS");
      this.socket = socket;
      this.hypo = hypo;
      inducer = ind;
      this.filename = filename;
    }
    
    public THSS(Sys sys, Hypothesis hypo, String ind, String filename) {
      this.sys = sys;
      this.hypo = hypo;
      inducer = ind;
      this.filename = filename;
      socket = sys.setSocket();
    }

    public void run() {
      starttime = System.currentTimeMillis();
      String[] info = getTestInfo();
      Farmer.writeSocket(socket, info);
      String[] result = Farmer.readSocket(socket);
      try {
        ScoringMetrics sm = format(result);
        hypo.setFitness(sm);
        hypo = null;
      }
      catch (Exception e) {}
      while ( hypo != null ) { 
        try { Thread.sleep(1000); }
        catch (Exception e) {}
      }
      if (!sys.reset(socket)) {
        sys.overide();
      }
      sys = null;
      socket = null;
    }

    public boolean ready() {
      boolean result = true;
      if (!recovered) {
        if (socket == null) {
          result = false;
        }
      }
      else {
        result = false;
      }
      return result;
    }

/*    public boolean isTesting() {
      boolean temp = (this.hypo != null);
      return temp;
    }
*/
    public String[] getTestInfo() {
      String[] result = new String[0];
      if (!recovered) {
        result = CHC.addString(result, inducer, 1);
        result = CHC.addString(result, filename, 1);
        result = CHC.addString(result, CHC.iTS(hypo.stage()), 1);
        boolean[] bool = hypo.getGeneticMakeup("");
        result = CHC.addString(result, CHC.iTS(bool.length), 1);
        for (int i = 0; i < bool.length; i++) {
          result = CHC.addString(result, CHC.bTS(bool[i]), 1);
        }
      } 
      return result;
    }

    public Hypothesis recover() {
      Hypothesis temp = hypo;
      hypo = null;
      sys.overide();
      recovered = true;
      return temp;
    }

    public boolean timedOut() {
      if ( (System.currentTimeMillis() - starttime) > maxwaittime ) {
        sys.strikes++;
        return true;
      }
      else {
        return false;
      }
    }

    private static ScoringMetrics format(String[] dat) throws Exception {
      ScoringMetrics metrics = new ScoringMetrics();
//      try {
        metrics.numCorrect = CHC.sTI(dat[0]);
        metrics.numIncorrect = CHC.sTI(dat[1]);
        metrics.totalLoss = CHC.sTD(dat[2]);
        metrics.minimumLoss = CHC.sTD(dat[3]);
        metrics.maximumLoss = CHC.sTD(dat[4]);
        metrics.weightCorrect = CHC.sTD(dat[5]);
        metrics.weightIncorrect = CHC.sTD(dat[6]);
        metrics.meanSquaredError = CHC.sTD(dat[7]);
        metrics.meanAbsoluteError = CHC.sTD(dat[8]);
        metrics.totalLogLoss = CHC.sTD(dat[9]);
        metrics.treenodes = CHC.sTI(dat[10]);
        metrics.treeleaves = CHC.sTI(dat[11]);
//      }
//      catch (Exception e) {
//        metrics = null;
//      }
      return metrics;
    }
  }
