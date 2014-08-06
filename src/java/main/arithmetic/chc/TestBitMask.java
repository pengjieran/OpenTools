package arithmetic.chc;

/** used to test the bitmask during development of chc.
  */
public class TestBitMask {


  /** Constructed a new bit mask and then printed out the
    * size and the values of all the masks. The values should
    * correspond to those found in the MLJ-Options.file.
    */
  public static void main(String[] args) {
    BitMask bm = new BitMask();

    System.out.println(bm.size());

    System.out.println(bm.getDisplayString());

  }

}
