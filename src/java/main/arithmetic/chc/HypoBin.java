package arithmetic.chc;

import java.util.LinkedList;

/** HypoBin is just a storage container made specifically for moving Hypothesis
  * between two threads. Its main pupose is to make Hypo passing in CHC easier. */
public class HypoBin {
  /** A LinkedList to store the Hypothesis being stored. */
  private LinkedList bin = new LinkedList();

  public HypoBin() {

  }

  public synchronized void add(Hypothesis hypo) {
    bin.add(hypo);
  }

  public synchronized Hypothesis retrieve() {
    if (bin.size() > 0) {
      return (Hypothesis)bin.removeFirst();
    }
    else { 
      return null;
    }
  }

}
  
