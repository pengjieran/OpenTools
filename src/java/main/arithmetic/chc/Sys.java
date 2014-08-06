package arithmetic.chc;

import java.net.Socket;

public class Sys {
  private String name;
  private int port;
  private boolean occupied = false;
  public boolean checked = true; //this sys is not working if false
  public int strikes = 0;
  private String ID = "";
  
  public Sys(String name, int port) {
    this.name = name;
    this.port = port;
  }

  public String toString() {
    return (name + ":" + CHC.iTS(port));
  }

  public Socket setSocket() {
    Socket result;
    try {
      result = new Socket(name, port);
      ID = result.toString();
    }
    catch (Exception e) {
      result = null;
    }
    occupied = true;
    
    return result;
  }

  public boolean isOccupied() {
    return occupied;
  }  

  public synchronized boolean reset(Socket s) {
    if (s != null) {
      if ( s.toString().equals(ID) ) {
        ID = "";
        occupied = false;
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  public synchronized void overide() {
    ID = "";
    occupied = false;
  }

  public boolean equals(Sys sys) { 
    if (toString().equals(sys.toString())) {
      return true;
    }
    else {
      return false;
    }
  }
}
