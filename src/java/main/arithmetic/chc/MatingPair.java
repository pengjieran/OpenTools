package arithmetic.chc;


public class MatingPair {
  private Hypothesis mate;
  private Hypothesis match;
  private Hypothesis brother;
  private Hypothesis sister;
  public int geneticdifference;
  public int generationnumber;

  public MatingPair(Hypothesis one, Hypothesis two) {
    if (one.length() == two.length()) {
      mate = one;
      match = two;
      geneticdifference = one.geneticDifference(two);
    }
    generationnumber = one.getGeneration();
  }

  public Hypothesis mate() {
    int[] crossindex = new int[geneticdifference];
    int marker = 0;
    int[] one = mate.getGeneticMakeup();
    int[] two = match.getGeneticMakeup();
//    mate.displayHypothesis();
//    System.out.println(geneticdifference);
//    match.displayHypothesis();

    for (int i = 0; i < mate.length(); i++) {
      if (mate.getChromosome(i)!=match.getChromosome(i)) {
        crossindex[marker]=i;
        marker++;
      }
    }
    Shuffler shuffle = new Shuffler(crossindex);
    int newint;
    for (int i=0; i < geneticdifference/2; i++) {
      newint = shuffle.selectint();
      int temp = one[newint];
      one[newint] = two[newint];
      two[newint] = temp;
    }
    brother = new Hypothesis(generationnumber,one);
    sister = new Hypothesis(generationnumber,two);    
    return brother;
  }

  public void display() {
    
  }

  public Hypothesis getSister() { return sister; }
}
