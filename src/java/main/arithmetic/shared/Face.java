package arithmetic.shared;

/** Face object for the Graph class.
 */
public class Face extends GraphObject{
    
    /** The head Edge for this face of the Graph.
     */
    public Edge head;
    
    /** The size of the Graph for this Face.
     */
    public int sz;

    /** The Graph to which this Face belongs.
     */
    public Graph owner;
    
    /** Constructor.
     * @param x Data to be stored in this Face.
     */
    Face(Object x){
        data = x ;
        id = 0;
        owner = null;
        head = null;
        sz = 0;
    }
    
    /** Returns the index value of this Face.
     * @return The index value of this Face.
     */
    public int index(){ return id & 0x7fffffff;  }
    
    /** Returns the Graph to which this Face belongs.
     * @return The Graph to which this Face belongs.
     */
    public Graph graph_of(){ return owner; }
}//End of Face class