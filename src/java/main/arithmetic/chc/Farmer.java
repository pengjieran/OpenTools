package arithmetic.chc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import arithmetic.shared.Inducer;
import arithmetic.shared.InstanceList;
import arithmetic.shared.ScoringMetrics;


/** A Farmer is an object that tests an inducer on a set of
  * data. All that is needed for a Farmer to perform testing
  * is an inducer and a data set. This information is obtained
  * through a socket. Once testing is done the result is sent
  * back through a socket and the Farmer thread stops and is
  * recycled. 
  *
  * No information recovery methods are possible through this
  * type of Farming implementation but the communcation between
  * modules is simple and very reliable. */
public class Farmer extends Thread {
    /** Signifies the type of this Farmer.
      * 1 means the farmer is remote and uses a socket
      *   to get information
      * 2 means the farmer is local and the information
      *   is already given */
    private int type; // 1 is socket type; 2 is non socket type;

    /** the socket used for communication to the main program. */
    private Socket socket = null;

    private BufferedReader in;

    private PrintWriter out;

    private Inducer inducer;

    private DataDistributor dist;

//    private int stage;

    private Depot depot;
    /** the Hypothesis to test. */
    private Hypothesis hypo;
    /** the training data to be used during testing. */ 
    private InstanceList train = null;
    /** the testing data to be used during testing. */
    private InstanceList test = null;
    /** the genetic string of the Hypothesis. */
    private boolean[] genetics;

  public Farmer(Socket socket, Depot depot) {
    super();
    this.socket = socket;
    this.depot = depot;
    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream());
    }
    catch (IOException e) {
    }
    type = 1;
  }

  public Farmer(Hypothesis hypo, String ind, String filename, Depot depot) {
    this.hypo = hypo;
    this.depot = depot;
    inducer = depot.getInducer(ind);
    dist = depot.getData(filename);
    genetics = hypo.getGeneticMakeup("");
    int stage = hypo.stage();
      if (stage == 0) {
      }
      else if (stage == 1) {
        train = dist.getTrainData();
        test = dist.getValidationData();
      }
      else if (stage == 2) {
        train = dist.getFinalTrainer(); 
        test = dist.getTestData();
      }
      else {
      }
    type = 2;
  }
  
  public void run() {
    if (type == 1) {
      String[] input = readSocket(in);
      interpret(input);
      ScoringMetrics metrics = test();
      String[] output = interpret(metrics);
      writeSocket(out, output);
    }
    else if (type == 2) {
      ScoringMetrics metrics = 
        inducer.project_train_and_perf(train, test, genetics).get_metrics();
      metrics.treenodes = inducer.num_nontrivial_nodes();
      metrics.treeleaves = inducer.num_nontrivial_leaves();
      hypo.setFitness(metrics);
    } 
  }
  
  public static String[] interpret(ScoringMetrics met) {
    String[] result = new String[12];
    try {
      result[0] = CHC.iTS(met.numCorrect);
      result[1] = CHC.iTS(met.numIncorrect);
      result[2] = CHC.dTS(met.totalLoss);
      result[3] = CHC.dTS(met.minimumLoss);
      result[4] = CHC.dTS(met.maximumLoss);
      result[5] = CHC.dTS(met.weightCorrect);
      result[6] = CHC.dTS(met.weightIncorrect);
      result[7] = CHC.dTS(met.meanSquaredError);
      result[8] = CHC.dTS(met.meanAbsoluteError);
      result[9] = CHC.dTS(met.totalLogLoss);
      result[10] = CHC.iTS(met.treenodes);
      result[11] = CHC.iTS(met.treeleaves);
    }
    catch (Exception e) {
    }
    return result;
  }  

  public void interpret(String[] input) {
    try {
      inducer = depot.getInducer(input[0]);
      dist = depot.getData(input[1]);  
      int stage = CHC.sTI(input[2]);
      int iii = CHC.sTI(input[3]);
      genetics = new boolean[iii];
      if (input.length > (iii + 3) ) {
        for (int i = 0; i < genetics.length; i++) {
          genetics[i] = CHC.sTB(input[i + 3]);
        }
      }
      if (stage == 0) {
      }
      else if (stage == 1) {
        train = dist.getTrainData();
        test = dist.getValidationData();
      }
      else if (stage == 2) {
        train = dist.getFinalTrainer(); 
        test = dist.getTestData();
      }
      else {
      }
    }
    catch (Exception e) {
    }
  }

  public ScoringMetrics test() {
    try {
      ScoringMetrics result = 
          inducer.project_train_and_perf(train, test, genetics).get_metrics();
      result.treenodes = inducer.num_nontrivial_nodes();
      result.treeleaves = inducer.num_nontrivial_leaves();
      return result;
    }
    catch (Exception e) {
      return null;
    }
  }

  public static String[] readSocket(Socket sock) {
    BufferedReader inr = null;
    try {
      inr = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    }
    catch (Exception e) {
    }
    return readSocket(inr);
  }

  public static String[] readSocket(BufferedReader ins) {
//    System.out.println("readSocket()");
    String[] input = new String[0];
    try {
      String temp = null;
      while ( (temp = ins.readLine()) != null) {
//        System.out.println(temp);
        if (temp.equals("")) {
          break;
        }
        input = CHC.addString(input, temp, 1);
      }
    }
    catch (Exception e) {
      System.out.println("readSocket() Error");
    }
    return input;
  }

  public static void writeSocket(Socket sock, String[] output) {
    PrintWriter outr = null;
    try {
      outr = new PrintWriter(sock.getOutputStream());
    }
    catch (Exception e) {
    }
    writeSocket(outr, output);
  }

  public static void writeSocket(PrintWriter out, String[] output) {
    boolean result = true;
    try {
      for (int i = 0; i < output.length; i++) {
        out.println(output[i]);
        out.flush();
      }
        out.println();
        out.flush();
    }
    catch (Exception e) {
      result = false;
    }
  }

}
