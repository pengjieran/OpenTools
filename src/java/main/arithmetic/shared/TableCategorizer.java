package arithmetic.shared;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ListIterator;

/** A TableCategorizer consists of a table of all possible instances.  Instances
 * are categorized according to label if they are found; otherwise the default
 * category is returned. The tiebreaking order is used to break ties if multiple
 * instances match. Assumes LabelledInstanceInfo has label of type NominalAttrInfo
 * and all instances must be labelled.
 * @author James Louis
 * @author Clay Kunz 10/15/97 Added full tiebreaking.
 * @author Yeogirl Yun 9/04/94 Provides faster operations with InstanceHash.
 * @author Richard Long 8/12/93 Initial Revision (.c)
 * @author Richard Long 8/05/93 Initial Revision (.h)
 */
public class TableCategorizer extends Categorizer {
    
    private int defaultCat;
    private int[] tieBreakingOrder;
    /** Hashtable of Instances used in this categorizer.
     */    
    protected InstanceHashTable hashTable;
    
    
    /** Initializes the table with the given InstanceList and relays the description
     * and number of categories to the Categorizer constructor.
     * @param instList InstanceList obect containing the Instances used to populate the
     * hash table of instances.
     * @param defaultCategory Integer indicating the default category for this Categorizer.
     * @param dscr String description of this Categprizer object.
     */    
    public TableCategorizer(InstanceList instList,
    int defaultCategory,
    String dscr) {
        super(instList.num_categories(), dscr, instList.get_schema());
        defaultCat = defaultCategory;
        //     tieBreakingOrder(UNKNOWN_CATEGORY_VAL, instList.num_categories() + 1, 0);
        tieBreakingOrder = new int[instList.num_categories() + 1];
        hashTable = new InstanceHashTable(instList.num_instances());
        
        for (ListIterator pix = instList.instance_list().listIterator(); pix.hasNext();)
            hashTable.insert((Instance)pix.next());
        
        int[] order = instList.get_distribution_order();
        set_tiebreaking_order(order);
        order = null;
    }
    
    
    /** Returns category if instance found in table, otherwise returns defaultCat. This
     * will return the category of the majority instances found.
     * @param instance Instance object to be categorized.
     * @return Category of the supplied Instance object.
     */    
    public AugCategory categorize(Instance instance) {
        int cat;
        InstanceList instList = hashTable.find(instance);
        Schema schema = instance.get_schema();
        if (instList == null)
            cat = defaultCat;
        else {
            cat = instList.majority_category(tieBreakingOrder);
            //      DBG(ILPix pix(*instList);
            //	  const InstanceRC& inst = *pix;
            //	  ASSERT(inst.get_schema() == schema));
        }
        return new AugCategory(cat, schema.category_to_label_string(cat));
/*
Error.fatalErr("TableCategorizer.categorizer(Instance) is used ");
return null;
 */
    }
    
    /** Prints a readable representation of the categorizer to the given stream.
     * @param stream The BufferedStream to which the representation of this Categorizer will be
     * printed.
     * @param dp The preferences for display.
     */    
    public void display_struct(BufferedWriter stream, DisplayPref dp) {
        try{
            //   switch (dp.preference_type) {
            //      case DisplayPref.ASCIIDisplay :
            stream.write("Table Categorizer " +description()
            +" with default class " +defaultCat
            +" and tiebreaking order " +tieBreakingOrder +'\n');
            //	 IFLOG(3, stream << "and the following labelled instances in"
            //	       << " the table:" << endl);
            //	 IFLOG(3, hashTable.display(stream));
            //	 break;
            //      default :
            //	 Error.err("TableCategorizer::display_struct unsupported display "
            //	       +"preference "+dp.preference_type+'\n');
            //	 break;
            //   }
        }catch(IOException e){e.printStackTrace();}
    }
    
    /** Resets the tiebreaking order. The tiebreaking order must follow the rules for
     * tiebreaking orders, as set in CatDist.java
     *
     * @param newOrder The new order for tiebreaking.
     */    
    public void set_tiebreaking_order(int[] newOrder) {
        //   DBG(CatDist::check_tiebreaking_order(newOrder));
        tieBreakingOrder = newOrder;
    }
    
}