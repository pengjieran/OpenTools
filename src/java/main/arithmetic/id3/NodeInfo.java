package arithmetic.id3;
import java.util.LinkedList;
import arithmetic.shared.Error;

import arithmetic.shared.Categorizer;
import arithmetic.shared.Globals;

/** Stores the information in a Graph Node.
 */
public class NodeInfo implements Cloneable{
    private int lev;
    private NodeCategorizer cat;
    private LinkedList parents;
    
    /** Constructor.
     * @param levl The level in the graph this information will be stored.
     */
    public NodeInfo(int levl) {
        lev = levl;
        cat = null;
        Categorizer bad = Globals.badCategorizer;
        cat = new LeafCategorizer(bad);
    }
    
    /** Returns the level in the graph this information is stored.
     * @return The level in the graph this information is stored.
     */
    public int level() {return lev;}
    /** Sets the level at which this NodeInfo is stored in the graph.
     * @param newlev The level at which this NodeInfo is stored.
     */
    public void set_level(int newlev) {lev = newlev;}
    
    /** Creates a new NodeInfo at the given level.
     * @param level The level of the new NodeInfo.
     * @return The NodeInfo created.
     */
    public NodeInfo create_my_type(int level){return new NodeInfo(level);}
    
    /** Assigns the categorizer stored in the NodeInfo to this NodeInfo.
     * @param newInfo The NodeInfo containing the categorizer to be assigned.
     */
    public void assign_categorizer(NodeInfo newInfo) {
        if (newInfo == null)
            Error.err("NodeInfo::assign_categorizer: NULL info-->fatal_error");
        if (newInfo.cat == null)
            Error.err("NodeInfo::assign_categorizer: NULL cat-->fatal_error");
        cat = null;
        //   ASSERT(newInfo.cat.can_cast_to_node_categorizer());
        //		cat = (NodeCategorizer)((NodeCategorizer)newInfo.cat).clone();
        
        // @@ the previous two lines should be:
        cat = (NodeCategorizer)((NodeCategorizer)newInfo.cat).clone();
        //      ASSERT(cat);
        
    }
    
    //used by Node.print_node_entry() for the Inducer.display_struct function.
    /** Converts this object to a String representation.
     * @return A String representation of this NodeInfo.
     */
    public String toString() {
        return cat.toString() + ", level "+ lev;
    }
    
    /** Sets node info to point to given categorizer.
     * @param categorizer The categorizer to be stored in this NodeInfo.
     */
    public void set_categorizer(NodeCategorizer[] categorizer) {
        if (categorizer[0] == null)
            Error.err("NodeInfo::set_categorizer: Cannot set node to NULL categorizer-->fatal_error");
        //		cat = null;
        cat = categorizer[0];
        categorizer[0] = null; //This NodeInfo takes possesion of the categorizer
    }
    
    /** Returns the categorizer stored in this NodeInfo.
     * @return The categorizer stored in this NodeInfo.
     */
    public NodeCategorizer get_categorizer() {return cat;}
}