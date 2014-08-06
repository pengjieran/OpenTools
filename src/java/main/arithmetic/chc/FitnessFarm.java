package arithmetic.chc;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** Replaced by class JobFarm for all chc purposes. */
public class FitnessFarm {

  private static boolean go = true;

  private static ServerSocket server = null;
  private static PrintWriter out;
  private static BufferedReader in;
  private static Socket socket;

  public static void main(String[] args) throws IOException {
    int[] ports = convertString(args);
    int i = 0;
    while ( (server == null || server.getInetAddress() == null) 
             && (i < ports.length) )  {
      int port = ports[i++];
      System.out.println("Trying port " + port); 
      try {
        server = new ServerSocket(port);
        System.out.println(server.toString());
        go = true;
      }
      catch (Exception e) {
        System.out.println("Cannot connect to port "+port);
        go = false;
      }
    }
    while(go) {
      try {
        socket = server.accept();
        out = new PrintWriter(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Recieved socket");
      }
      catch (Exception e) {
        System.out.println("Stream not established");
      }

      String input;
      while ((input = in.readLine()) != null) {
        System.out.println(input);
      }
      out.close();
      in.close();
    }
    kill();
  }

  private static void kill() {
    try {
      server.close();
      System.out.println("Server now disconected");
    }
    catch (Exception e) {
      System.out.println("Could not close server");
    }
  }
  
  public static int[] convertString(String[] s) {
    int[] temp = new int[s.length];
    for (int i = 0; i < s.length; i++) {
      temp[i] = Integer.valueOf(s[i]).intValue();
    }
    return temp;
  }
}

