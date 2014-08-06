package arithmetic.id3;
import java.io.BufferedWriter;
import java.util.LinkedList;

import arithmetic.shared.AugCategory;
import arithmetic.shared.CatDist;
import arithmetic.shared.Categorizer;
import arithmetic.shared.DisplayPref;
import arithmetic.shared.Error;
import arithmetic.shared.Instance;
import arithmetic.shared.InstanceList;
import arithmetic.shared.Schema;

/** NodeCategorizer for categorizers that don't need to ask other
 * categorizers for help scoring (i.e. leaves in the decision process). Since
 * any categorizer can sit at a leaf in a decision tree / graph, this class
 * serves as a wrapper for any Categorizer that doesn't care about whether or
 * not it sits in a graph. This class is a pure wrapper to the inner
 * categorizer. That is, it doesn't maintain any information at all except for
 * the reference to the wrapped categorizer. The OK function should check that
 * none of the local variables ever change from their dummy variables.
 * @author James Louis 08/21/2001 Ported to Java.
 * @author Clay Kunz 08/13/97 Initial revision (.h,.c)
 */
public class LeafCategorizer extends NodeCategorizer
{
    /** Attributes for this Categorizer.
     */
   private static LinkedList dummyAttr = new LinkedList();
   /** Schema for the attributes and labels of this Categorizer.
    */
   private static Schema dummySchema = new Schema(dummyAttr);
   /** Description of this Categorizer.
    */
   private static String dummyDescription = "Leaf"; // can't be empty
   /** The Categorizer stored in this LeafCategorizer object.
    */
   private Categorizer categorizer;

   /** Constructor.
    * @param aCategorizer The Categorizer that will be stored in this LeafCategorizer.
    */
   public LeafCategorizer(Categorizer aCategorizer)
   {
     super(check_num_categories(aCategorizer), dummyDescription, dummySchema);
     categorizer = aCategorizer;
     aCategorizer = null;
   }

   /** Returns the class id of this of this categorizer.
    * @deprecated This method should be replaced with Java's instanceof operator.
    * @return Integer assigned to this inducer.
    */
   public int class_id(){return -1;}

   /** Returns the number of categories in the given LeafCategorizer object.
    * @return Always 1.
    * @param cat The Categorizer for which the number of categories is requested.
    */
   private static int check_num_categories(Categorizer cat)
   {
      if (cat == null)
         Error.fatalErr("LeafCategorizer::LeafCategorizer: the given categorizer was "+
            "NULL");

      return 1;
   }

   /** Returns the category for the given Instance. Leaf categorizers have no
    * children, so this function aborts.
    * @return Always returns the category "bad branch" with the category number 0.
    * @param inst	The instance to be checked.
    */
   public AugCategory branch(Instance inst)
   {
      Error.fatalErr("LeafCategorizer::branch: leaf categorizers have no children");
      return new AugCategory(0, "bad branch");
   }

   /** Returns the Categorizer stored in this LeafCategorizer.
    * @return The Categorizer stored in this LeafCategorizer.
    */
   public Categorizer get_categorizer(){ return categorizer; }

   /** Clone function.
    * @return The clone of this object.
    */
   public Object clone()
   {
      return super.clone();
   }

   /** Sets which attributes have been used for the attributes in the
    * Categorizer stored in this LeafCategorizer.
    * @param used The attributes used. TRUE indicates that attribute is used, FALSE otherwise.
    */
   public void set_used_attr(boolean[] used)
   { categorizer.set_used_attr(used); }

   /** Displays the structure of the Categorizer stored in this LeafCategorizer.
    * @param stream The BufferedWriter to which this LeafCategorizer will be displayed.
    * @param pref The preferences for display.
    */
   public void display_struct(BufferedWriter stream,DisplayPref pref)
   { categorizer.display_struct(stream, pref); }

   /** Returns the number of categories for the categorizer stored in this
    * LeafCategorizer.
    * @return The number of categories for the categorizer stored in this
    * LeafCategorizer.
    */
   public int num_categories(){return categorizer.num_categories();}

   /** Returns the description of the Categorizer stored in this LeafCategorizer.
    * @return The description of the Categorizer stored in this LeafCategorizer.
    */
   public String description(){ return categorizer.description(); }

   /** Scores the given Instance using the categorizer stored in this
    * LeafCategorizer.
    * @return The category distribution of the supplied instance.
    * @param inst The Instance given for categorization.
    */
   public CatDist score(Instance inst)
   { return super.score(inst); }

   /** Scores the given Instance using the categorizer stored in this
    * LeafCategorizer.
    * @param inst The Instance given for categorization.
    * @param addLoss TRUE if Instance loss is to be added, FALSE otherwise.
    * @return The category distribution of the supplied instance.
    */
   public CatDist score(Instance inst, boolean addLoss)
   {
      CatDist dist = categorizer.score(inst);

      if (addLoss)
         add_instance_loss(inst, dist);

      return dist;
   }

   /** builds an Instance distribution from the InstanceList.
    * @param instList The list of Instances from which a distribution is to be built.
    */
   public void build_distr(InstanceList instList)
   { categorizer.build_distr(instList); }

   /** Sets the description of this LeafCategorizer to the given value.
    * @param val The new description for this Node Categorizer.
    */
   public void set_description(String val)
   { categorizer.set_description(val); }

   /** Returns whether this Categorizer subclass supports scoring.
    * @return TRUE.
    */
   public boolean supports_scoring(){ return true; }

   /** Sets the distribution to the given values.
    * @param val An array containing the new distribution values.
    */
   public void set_distr(double[] val)
   { categorizer.set_distr(val); }

   /** Returns the total weight of instances that reach this LeafCategorizer.
    * @return The total weight of instances that reach this LeafCategorizer.
    */
   public double total_weight(){ return categorizer.total_weight(); }


/***************************************************************************
***************************************************************************
   public void set_schema(Schema sch) { categorizer.set_schema(sch); }
*/

/***************************************************************************
***************************************************************************
   public void add_distr(double[] val)
   { categorizer.add_distr(val); }
*/

/***************************************************************************
***************************************************************************
   public void add_distr(int label, double delta)
   { categorizer.add_distr(label, delta); }
*/
}