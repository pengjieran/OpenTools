package arithmetic.chc;

import java.net.*;
import java.io.*;

public class TestFarm {

  public static boolean send = false;
  public static boolean go = false;
 
  public static int[] values;
  public static Socket socket = null;
  public static PrintWriter out;
  public static BufferedReader in;

  public static void main(String[] args) {
    
    values = JobFarm.convertString(args);
    try {
      socket = new Socket("samwise.user.cis.ksu.edu", values[0]);
      go = true;
    }
    catch (Exception e) {
      System.out.println("No Connection");
    }
    
    if (go) {
      Farmer.writeSocket(socket, args);
      String[] echo;
      try { Thread.sleep(1); } 
      catch (Exception e) {}
      echo = Farmer.readSocket(socket);
      for (int i = 0; i < echo.length; i++) {
        System.out.println(echo[i]);
      }
    }  
  }

}
