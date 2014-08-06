package arithmetic.chc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import arithmetic.shared.LogOptions;

/** JobFarms are designed to run on remote systems to help 
  * chc test Hypothesis. It requires two parameters. The path
  * of the data directory and the port on which to listen.
  *
  * After started a JobFarm listens on the given port for
  * job calls from a active chc program. Upon recieving a socket
  * through the port the JobFarm hands the Socket to a Farmer and 
  * continues listening. The Farmer handles the the communication
  * through the socket and the testing.
  */
public class JobFarm {
  /** The reference of the JobFarm. */
  private static JobFarm jf;
  /** used as an internal flag. */ 
  private boolean good = true;
  /** Listens on the given port for Sockets and recieves them. */
  private ServerSocket server = null;
  
//  private PrintWriter out;
//  private BufferedReader in;
  /** Given to each new Farmer. */
  private Depot depot;
  /** just a counter to determine the number processed. */
  private int numproccessed = 0;

  /** Creates and starts the JobFarm giving error messages if
    * the port is occupied.
    */ 
  public static void main(String[] args) throws IOException {
    try {
      jf = new JobFarm( args[0], CHC.sTI(args[1]) );  
    }
    catch (Exception e) {
      LogOptions.GLOBLOG(0, "Error making JobFarm" + CHC.ENDL);
      LogOptions.GLOBLOG(0, "Correct syntax is:" + CHC.ENDL);
      LogOptions.GLOBLOG(0, "java [...] JobFarm filepath port" + CHC.ENDL);
      System.exit(1);
    }
    jf.go();
  }

  public JobFarm(String filepath, int port) {
    depot = new Depot(filepath);
    System.out.println("Trying port " + port); 
    try {
      server = new ServerSocket(port);
      System.out.println(server.toString());
      good = true;
    }
    catch (Exception e) {
      System.out.println("Cannot connect to port "+port);
      good = false;
    }
  }

  public void go() {
    Socket socket;
    while(good) {
      try {
        socket = server.accept();
        Farmer f = new Farmer(socket, depot);
        f.setDaemon(true);
        f.start();
        System.out.println("Recieved socket:  " + ++numproccessed);
      }
      catch (Exception e) {
        System.out.println("Stream not established");
      }
    }
    kill();
  }

  private void kill() {
    try {
      server.close();
      System.out.println("Server now disconected");
    }
    catch (Exception e) {
      System.out.println("Could not close server");
    }
  }
 
  /** converts an array of Strings to the equivalent array
    * of ints.
    * @param s - the array of Strings.
    * @return the array of ints converted from s.
    */ 
  public static int[] convertString(String[] s) {
    int[] temp = new int[s.length];
    for (int i = 0; i < s.length; i++) {
      temp[i] = Integer.valueOf(s[i]).intValue();
    }
    return temp;
  }
}

