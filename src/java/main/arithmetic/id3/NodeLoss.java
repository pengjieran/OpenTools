package arithmetic.id3;
import java.io.BufferedWriter;

/** Class used for node accuracies. Stores the total weight of instances this node
 * is responsible for, and also the total losses incurred by this node.
 *
 */
public class NodeLoss {
    //added by JL
    
    /** The total weight of instances in this node.
     */
    public double totalWeight;
    /** Total losses incurred by this node.
     */
    public double totalLoss;
    /** The totalLoss value squared.
     */
    public double totalLossSquared;
    
    private double UNDEFINED_REAL = -1.7976931348623157E+308;
    
    /** Constructor. All values are set to the undefined value.
     */
    NodeLoss(){
        totalWeight = UNDEFINED_REAL;
        totalLoss = UNDEFINED_REAL;
        totalLossSquared = UNDEFINED_REAL;
    }
    
    /** Display this NodeLoss object to a BufferedWriter.
     * @param ostream The BufferedWriter to which this NodeLoss is displayed.
     */
    public void display(BufferedWriter ostream) {
        String output = new String(totalLoss + "/" + totalWeight + " (squared=" + totalLossSquared + ")");
        try{ostream.write(output,0,output.length());}
        catch(Exception e){}
    }
    
    /** Updates the total weight and total loss information.
     * @param weight The new total weight.
     * @param loss The new loss value.
     */
    public void update(double weight, double loss){
        totalWeight = totalWeight + weight;
        totalLoss = totalLoss + (weight * loss);
        totalLossSquared = totalLossSquared + (weight * loss * loss);
    }
}//End of NodeLoss class