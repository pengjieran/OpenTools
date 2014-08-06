package arithmetic.chc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

/** This class is designed to handle System operations for CHC. Operations
  * include getting a list of specified systems from the Systems.file, checking   * to see if systems are working correctly, and compiling lists of registered
  * Systems.
  */
public class SysOp {

  public static final String sysfile = "Systems.file";
  private static String filepath = "./chc/";

  public static void main(String[] args) throws IOException {
    Sys[] systems = getSys();
    for (int i = 0; i < systems.length; i++) {
      System.out.println(systems[i].toString());
    }
    systems = trySys(systems);
    for (int i = 0; i < systems.length; i++) {
      System.out.println(systems[i].toString());
    }
  }


  public SysOp() {

  }


  public static Sys[] getSys() {
    Sys[] result = new Sys[0];
    BufferedReader filein;
    try {
      filein = new BufferedReader(
                         new FileReader(filepath + sysfile));
    }
    catch (IOException e) {
      Options.LOG(0, "Error Reading " + filepath + sysfile + CHC.ENDL);
      return result;
    }
    try {
    while (filein.ready()) {
      String temp = "";
      try {
        temp = filein.readLine();
      }
      catch (IOException e) {
      }
      StringTokenizer token = new StringTokenizer(temp, ":");
      Sys newsys = null;
      try {
        newsys = new Sys(token.nextToken(), CHC.sTI(token.nextToken()));
      }
      catch (Exception e) {
      }
      result = addSys(result, newsys, 1);
    }
    }
    catch (IOException e) { 
    }
    return result;
  }

  public static Sys[] trySys(Sys[] sys) {
    Sys[] result = new Sys[0];
    Socket socket = null;
    for (int i = 0; i < sys.length; i++) {
      try {
        socket =  sys[i].setSocket();
      }
      catch (Exception e) {
        socket = null;
      }
      if (socket != null) {
        try {
          Farmer.writeSocket(socket, new String[0]);
          result = addSys(result, sys[i], 1);
        }
        catch (Exception e) {
        }
      }
      sys[i].overide();
    }
    return result;
  }

  public static Sys[] addSys(Sys[] sysarray, Sys addition, int increment) {
    boolean hasnulls = false;
    int firstnull = -1;
      if ( increment < 1) { increment = 1; }
    if (addition == null) {
      return sysarray;
    }
    if (sysarray == null) {
      Sys[] result = new Sys[increment];
      result[0] = addition;
      return result;
    }
    else {
      for (int i = 0; i < sysarray.length; i++) {
        if ( (sysarray[i] == null) && (firstnull < 0) ) {
          firstnull = i;
        }
      }
        if (firstnull >= 0) {
          sysarray[firstnull] = addition;
          return sysarray;
        }
        else {
          Sys[] temparray = new Sys[sysarray.length + increment];
          for (int j = 0; j < sysarray.length; j++) {
            temparray[j] = sysarray[j];
          }
          temparray[sysarray.length] = addition;
          return temparray;
        }
    }
  }
}
