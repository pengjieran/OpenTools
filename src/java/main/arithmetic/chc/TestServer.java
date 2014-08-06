package arithmetic.chc;

import java.io.*;
import java.net.*;

public class TestServer {

  private ServerSocket server;
  private BufferedReader in;
  private PrintWriter out;

  public static void main(String[] args) {
    TestServer ts = new TestServer(CHC.sTI(args[0]));
    ts.go();
  }

  public TestServer(int port) {
    try {
      server = new ServerSocket(port);
    }
    catch (Exception e) {
      System.out.println("Could not connect");
    }
  }
  
  public void go() {
    Socket socket = null;
    while (true) {
      try {
        socket = server.accept();
        System.out.println("Socket Recieved");
      }
      catch (Exception e) {
      }
      String[] input = Farmer.readSocket(socket);
      for (int i = 0; i < input.length; i++) {
        System.out.println(input[i]);
      }
      try { Thread.sleep(5000); }
      catch (Exception e) {}
      //Farmer.writeSocket(socket, input);
    }
  }
}
