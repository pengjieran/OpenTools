package arithmetic.shared;

/** This class contains features that are used in the Node and Edge classes.
 * @author James Louis 4/17/2002 Java Implementation
 */
public class GraphObject{
    //added by JL
    //based on LEDA
    
    /** The successor GraphObject in the list of GraphObjects maintained by the Graph
     * class.
     */    
    protected GraphObject obj_list_succ;
    /** The predicessor GraphObject in the list of GraphObjects maintained by the Graph
     * class.
     */    
    protected GraphObject obj_list_pred;
    /** An integer identification number.
     */    
    protected int id;
    /** The data stored in this GraphObject.
     */    
    public Object data;
    
    /** Sets the ID number to the given number.
     * @param newid The new ID number.
     */    
    public void set_id(int newid){id = newid;}
    /** Returns the ID number masked with 0x7fffffff.
     * @return The ID number masked with 0x7fffffff.
     */    
    public int index(){return id & 0x7fffffff;}
    
}//End of GraphObject class