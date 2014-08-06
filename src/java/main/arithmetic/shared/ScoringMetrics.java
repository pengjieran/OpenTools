package arithmetic.shared;

/** scoring device for CatTestResult **/
public class ScoringMetrics {
    // Public member data
    /** The number fo instances correctly classified.
     */
    public int numCorrect;
    /** The number fo instances incorrectly classified.
     */
    public int numIncorrect;
    /** The total amount of loss.
     */
    public double totalLoss;
    /** The minimum amount of loss.
     */
    public double minimumLoss;
    /** The maximum amount of loss.
     */
    public double maximumLoss;
    /** The weight of correctly classified instances.
     */
    public double weightCorrect;
    /** The weight of incorrectly classified instances.
     */
    public double weightIncorrect;
    /** The mean squared error value.
     */
    public double meanSquaredError;
    /** The mean absolute error value.
     */
    public double meanAbsoluteError;
    /** The total amount of log loss.
     */
    public double totalLogLoss;
    /** The number of none trivial leaves used by a tree inducer.
      */
    public int treeleaves;
    /** The number of none trivial nodes used by a tree inducer.
      */
    public int treenodes;
    
    // Methods
    /** Constructor.
     */
    public ScoringMetrics() {
        numCorrect = 0;
        numIncorrect = 0;
        totalLoss = 0;
        minimumLoss = 0;
        maximumLoss = 0;
        weightCorrect = 0;
        weightIncorrect = 0;
        meanSquaredError = 0;
        meanAbsoluteError = 0;
        totalLogLoss = 0;
        treeleaves = 0;
        treenodes = 0;
    }
    
    /** Returns the total weight of instances.
     * @return The total weight.
     */
    public double totalWeight() {
        return (weightCorrect + weightIncorrect);
    }
    
    /** Returns the total number of instances. Easier than retreiving both values
      * summing them external to this class.
      * @return The total number of instances.
      */  
    public int totalInstances() {
      return (numCorrect + numIncorrect);
    }

}
