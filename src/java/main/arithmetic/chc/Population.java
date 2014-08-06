package arithmetic.chc;

import java.util.StringTokenizer;

import arithmetic.shared.GetEnv;
import arithmetic.shared.Inducer;

public class Population {

  private static Inducer inducer;
  private static FitnessMachine machine;

  private Hypothesis[] population = new Hypothesis[0];
  private Hypothesis[] parents = new Hypothesis[0];
  private Hypothesis[] children = new Hypothesis[0];
  private int popmarker=0;
  private int parentpopmarker=0;
  private int childpopmarker=0;

  private MatingPair[] pairs;
  private int pairmarker;

  private int generationnumber;
  private int currentthreshhold;

  //statistic storage to be printed
  private double[] averagegenerationfitness;
  private Hypothesis[] bestspawnedofgeneration;

  private Options options;

  public Population(Inducer ind, Options options) {
    this.options = options;

    GetEnv getenv = new GetEnv();
    String temp = getenv.get_option_string("FitnessDistribution");
    StringTokenizer token = new StringTokenizer(temp, ",");
    double[] fdist = new double[3];
    for (int i = 0; i < fdist.length; i++) {
      fdist[i] = Double.valueOf(token.nextToken()).doubleValue();
    } 
    Hypothesis.setFDist(fdist);

    inducer = ind;
    machine = new FitnessMachine(ind, options.getDist());
    generationnumber = 0;
    currentthreshhold = 1 + options.getDist().getattrselectionsize()/4;
    averagegenerationfitness = new double[0];

    //Precalculate the size of the arrays
    population = new Hypothesis[options.getPopArraySize()]; 
    parents = new Hypothesis[options.populationsize];
    children = new Hypothesis[options.populationsize];
  }

  /** nextGeneration sequentially goes through the steps necessary to complete
    * one generation. It takes no arguments and returns nothing.
    */ 
  public void nextGeneration() {
    options.LOG(1, "Generation " + generationnumber + CHC.ENDL);
    children=new Hypothesis[options.populationsize];
    breed();
    calculateFitness();
    parents = CHC.sortDescendingFitness(parents);
    children = CHC.sortDescendingFitness(children);
    bestspawnedofgeneration = CHC.addHypo(bestspawnedofgeneration,
                                          children[0], 1);
    displayPopulation(CHC.GENERATIONPRINTING);
    parents = getFit();
    for (int i=0; i<parents.length;i++) {
      parents[i].nextGeneration();
    }
    pairs = null;
    generationnumber++;
  }
  /** getFit() returns an array with the most fit hypothesis from the 
    * parent and child population.
    */
  public Hypothesis[] getFit() {
    Hypothesis[] fithypo = new Hypothesis[parents.length+children.length];
    int tempmarker=0;
    int tempparentmarker = 0;                                //parents.length-1;
    int tempchildmarker = 0;                                 //children.length-1;
    for (int i=0; i<parents.length;i++) {
      fithypo[tempmarker++]=parents[i];
    }
    for (int i=0; i<children.length;i++) {
      fithypo[tempmarker++]=children[i];
    }
    fithypo=CHC.sortDescendingFitness(fithypo);

    double holdfitness = fithypo[options.populationsize-1].getFitness();
    int holdindex=options.populationsize;
    for (int i=options.populationsize;i<fithypo.length; i++) {
      if (holdindex==options.populationsize && holdfitness < fithypo[i].getFitness()) {
        holdindex = i;
      }
    }
    Hypothesis[] temphypo = new Hypothesis[holdindex];
    for (int i=0; i<holdindex;i++) {
      temphypo[i] = fithypo[i];
    }
    return temphypo;    
  }

  public Hypothesis[] sortDescendingHypothesis(Hypothesis[] hypo) {
     return hypo;
  }

  public void calculateFitness() {
    options.LOG(3, "Calculate Fitness Called"+CHC.ENDL);
    machine.add(children);
    machine.run();
//    options.LOG(3, " Done" + CHC.ENDL);
  }

  public void breed() {
    options.LOG(3, "Breeding "+CHC.ENDL);
    Breeder b = new Breeder(parents);
    children = b.breed(options.populationsize, currentthreshhold);
    if (options.getMask("print_all_hypothesis")) {
      population = CHC.addHypo(population, children, options.populationsize*10);
    }
    // Check to see if we need Cataclysmic Mutation
    if (children.length < options.populationsize) {
      options.LOG(3, "Using Cataclysimic Mutation to produce "+(options.populationsize-children.length)+" hypotheses"+CHC.ENDL);
      Hypothesis[] hypo = Mutator.cataclysmicMutation(parents[0],options.populationsize-children.length);
      for (int i=0;i<hypo.length;i++) {
        addChild(hypo[i]);
      }
    }
  }

  private void addParent(Hypothesis[] hypo) {
    for (int i = 0; i < hypo.length; i++) {
      addParent(hypo[i]);
    }
  }
  private void addParent(Hypothesis hypo) {
    parents = CHC.addHypo(parents, hypo, 1);
    if (options.getMask("print_all_hypothesis")) {
      population = CHC.addHypo(population, hypo, options.populationsize*10);
    }
  }

  private void addChild(Hypothesis hypo) {
    children = CHC.addHypo(children, hypo, 1);
    if (options.getMask("print_all_hypothesis")) {
      population = CHC.addHypo(population, hypo, options.populationsize*10);
    }
  }

  private void addMatingPair(MatingPair mp) {
    if (pairmarker == pairs.length) {
      MatingPair[] newpairs = new MatingPair[pairmarker + 1];
      for (int i = 0; i < pairmarker; i++) {
        newpairs[i] = pairs[i];
      }
      pairs = newpairs;
    }
    pairs[pairmarker++] = mp;
  }

  public void spawnPopulation() {
    Spawner spawner = new Spawner(options.getDist().getattrselectionsize());
    addParent(spawner.spawnPopulation(options.populationsize, generationnumber));
    machine.add(parents);
    machine.run();
  }

  public void displayPopulation(String depth) {
     if (depth.equals(CHC.GENERATIONPRINTING)) {
       options.LOG(4, "Generation "+generationnumber+".\nParents."+CHC.ENDL);
       options.LOG(4, getHypothesisString(parents, 0));
       options.LOG(4, "Children."+CHC.ENDL);
       options.LOG(4, getHypothesisString(children, 0));
     }
     else if (depth.equals(CHC.TOTALPRINTING)) {
       population = CHC.cleanHypo(population);
       printHypothesis(population, popmarker-1);
     }
     options.LOG(4, CHC.ENDL);
  }

  private void printHypothesis(Hypothesis[] hypo, int maxprint) {
    if (hypo == null) {
    }
    else {
      for (int i = 0; i<((maxprint<=0)||(hypo.length<maxprint)?hypo.length:maxprint); i++) {
        if (hypo[i] == null) {
        }
        else {
          hypo[i].displayHypothesis();
        }
      }
    }
  }

  public static String getHypothesisString(Hypothesis[] hypo, int maxprint) {
    String result = "";
    if (hypo == null) {
    }
    else {
      for (int i = 0; i<((maxprint<=0)||(hypo.length<maxprint)?hypo.length:maxprint); i++) {
        if (hypo[i] == null) {
        }
        else {
          result += hypo[i].getHypothesisString();
        }
      }
    }
    return result;
  }

  public int getGenerationNumber() { return generationnumber; }
/*  public void setmaxpopulation(int value) {
    if (value > 1) {
      int temp = value/2;
      maxpopulation = temp*2;
    }
  }
*/
  public void displayFinalHypothesisFitness(boolean bool) {
    if (bool) {
      Hypothesis[] newpopulation = CHC.cleanHypo(parents);
      newpopulation = CHC.sortDescendingFitness(newpopulation);
      int number = 0;
      options.LOG(7, "newpopulation.length = " + newpopulation.length+CHC.ENDL);
      if (newpopulation.length >= 10) {
        number = 10;
      }
      else {
        number = newpopulation.length;
      }
      for (int i = 0; i < number; i++ ) {
        options.LOG(7, "newpop: i = "+ i+CHC.ENDL);
        if (newpopulation[i] == null) {
        }
        else {
          machine.add(newpopulation[i]);
        }
      }
      machine.run();

      options.LOG(0, "\n|||||||||||||||||Final Hypothesis Fitness||||||||||||||||||||||||||||||"+CHC.ENDL);
      for (int i = 0; i < number; i++) {
        if (newpopulation[i] == null) {
        }
        else {
          options.LOG(0, newpopulation[i].getHypothesisString("|| ", " ||"+CHC.ENDL));
        }
      } 
      options.LOG(0, "|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||"+CHC.ENDL);
    }
  }

  public void displayAverageFitnessPerGeneration(boolean bool) {
    if (bool) {
      options.LOG(0, "\nAverage Fitness by Generation"+CHC.ENDL);
      for (int i = 0; i < averagegenerationfitness.length; i++) {
        options.LOG(0, "  " + i + ") " + averagegenerationfitness[i]
                       +CHC.ENDL);
      }
      options.LOG(0, CHC.ENDL);
    }
  }

  public void set_log_level(int level) {
    options.set_log_level(level);
  }

  public void displayBestSpawnedOfGeneration(boolean bool) {
    if (bool) {
      bestspawnedofgeneration = CHC.cleanHypo(bestspawnedofgeneration);
      machine.add(bestspawnedofgeneration);
      machine.run();

      System.out.println("Best Spawned Hypothesis of Generation");
      for (int i = 0 ; i < bestspawnedofgeneration.length; i++) {

        System.out.println(i
               + bestspawnedofgeneration[i].getHypothesisString(" ", ""));
      }
    }
  }

  public int get_log_level() {
    return options.get_log_level();
  }
}
