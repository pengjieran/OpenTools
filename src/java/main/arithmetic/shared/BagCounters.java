package arithmetic.shared;
import java.io.BufferedWriter;
import java.io.IOException;

/** The BagCounters class provides support for instance list counters.
 * Counters are provided only for nominal attributes. It is an error to call
 * counters with a non-nominal attribute. All counters are real-valued to
 * provide instance weighting. Unlabelled lists are now supported.           <P>
 * It is assumed the list is labelled and the label must be of nominal type.
 * All non-nominal attributes contain a NULL value, instead of an array of
 * values.
 * @author James Louis      2/25/2001   Ported to Java.
 * @author Clay Kunz        9/26/97     Upgraded to allow unlabelled lists,
 *                                      removed virtual.
 * @author Dan Sommerfield  2/04/97     Upgraded to use instance weighting.
 * @author Brian Frasca     4/13/94     Changed valueCounts to be an array of
 *                                      pointers to matrices.
 * @author Richard Long     10/21/93    Converted to use MLC++ Array template
 *                                      class.
 * @author Ronny Kohavi     8/29/93     Initial revision (.c and .h)
 */
public class BagCounters {
    /** Counting bags for labels. **/
    private double[] labelCounts;
    
    /** 3D matrix of bags for attributes, values, and value counts. The first
     * dimension size should equal the number of attributes. The first dimension
     * is the number of attributes present. The second dimension is the number
     * of possible labels for each instance to be classified as. The third
     * dimension is the number of values this attribute can have. The number
     * stored at a specified position is the number of instances that have the
     * specified label classification and specified value number for the
     * specified attribute number.**/
    private double[][][] valueCounts;

    /** Counting bag for attribute counts. **/
    private double[][] attrCounts;
    
    /** The Schema for the Instances to be counted. **/
    private Schema schema;
    
    /** Constructor. The basic form shouldn't be used.
     */
    private BagCounters()
    {}
    
    /** Constructor.
     * @param aSchema Schema of the Instances for which counting will be done.
     */
    public BagCounters(Schema aSchema) {
        schema = aSchema;
        allocate_all(aSchema);
    }
    
    /** Copy constructor.
     * @param source	The BagCounters to be copied.
     */
    public BagCounters(BagCounters source) {
        
    }
    
    /** For each attribute, memory is allocated for the value array, and step
     * through all label values and allocate the value array. A NULL is assigned
     * in cases with non-nominal attributes. Label arrays are only allocated if the
     * schema is labelled.
     * @param aSchema	The Schema for Instances to be counted.
     */
    private void allocate_all(Schema aSchema) {
        attrCounts = new double[aSchema.num_attr() ][0];
        boolean isLabelled = aSchema.is_labelled();
        if (isLabelled) {
            valueCounts = new double[aSchema.num_attr() ][0][0];
            labelCounts = new double[aSchema.num_label_values() +1];
            MLJArray.init_values(0,labelCounts);
        }
        for(int attrNum = 0 ; attrNum < aSchema.num_attr() ; attrNum++) {
            AttrInfo ai = aSchema.attr_info(attrNum);
            if (!ai.can_cast_to_nominal()) {
                attrCounts[attrNum] = null;
                if (isLabelled)
                    valueCounts[attrNum] = null;
            }
            else {
                int numAttrVals = ai.cast_to_nominal().num_values();
                attrCounts[attrNum] =new double[numAttrVals+1];
                MLJArray.init_values(0,attrCounts[attrNum]);
                if (isLabelled)
                    valueCounts[attrNum] =new double[aSchema.num_label_values() +1][numAttrVals+1];
            }
        }
        
    }
    
    /** Adds the given Instance to the counting bags. The Instance's weight is
     * taken into account.
     * @param instance	The Instance to be added. The Instance should be labelled.
     */
    public void add_instance(Instance instance) {
        update_counters(instance, +1);
    }
    
    /** Deleted the given Instance to the counting bags. The Instance's weight is
     * taken into account.
     * @param instance	The Instance to be deleted. The Instance should be
     * labelled.
     */
    public void del_instance(Instance instance) {
        update_counters(instance, -1);
    }
    
    /** Returns the array of counting bags.
     * @return The label counting bags.
     */
    public double[] label_counts() {
        if ((Globals.DBG)&&(labelCounts == null))
            Error.fatalErr("BagCounters::label_counts: the counters are not labelled");
        return labelCounts;
    }
    
    /** Returns the 3D matrix of attribute value counting bags.
     * @return The attribute value counting bags.
     */
    public double[][][] value_counts() {
        if ((Globals.DBG)&&(valueCounts == null))
            Error.fatalErr("BagCounters::value_counts: the counters are not labelled");
        return valueCounts;
    }
    
    /** Return the value of the attribute counter.
     * @return The number of occurances of a specific value for a specific
     * attribute.
     * @param attrNum	The number of the attribute for which a count is
     * requested.
     * @param attrVal	The number of the value of the attribute for which
     * a count is requested.
     */
    public double attr_count(int attrNum, int attrVal) {
        return attrCounts[attrNum][attrVal];
    }    
    
    /** Returns the matrix of attribute counts.
     * @return The matrix of attribute counts.
     */
    public double[][] attr_counts() {
        return attrCounts;
    }
    
    /** Returns the number of label values that are actually found in the counted
     * Instances.
     * @return The number of label values that have been found.
     */
    public int label_num_vals() {
        if (labelCounts == null)
            Error.err("BagCounters::label_num_vals: these counters do not have labels-->fatal_error");
        int num_vals = 0;
        for(int i = 0 ; i < labelCounts.length ; i++)
            if (labelCounts[i] != 0)
                num_vals++;
        return num_vals;
    }
    
    /** Returns the number of attribute values that are actually found in the
     * counted Instances for a specified attribute.
     * @return The number of attribute values found for an attribute.
     * @param attrNum	The number of the attribute to be checked.
     */
    public int attr_num_vals(int attrNum) {
        //      if (Globals.DBG){check_nominal(attrNum)};
        double[] ac = attrCounts[attrNum];
        int num_vals = 0;
        for(int i = 0 ; i < ac.length ; i++)
            if (ac[i] != 0)
                num_vals++;
        return num_vals;
    }
    
    /** Returns the count of a specific label.
     * @return The count of a specific label.
     * @param labelVal	The specific value to be checked.
     */
    public double label_count(int labelVal) {
        if ((Globals.DBG)&&(labelCounts == null))
            Error.fatalErr("BagCounters::label_count: the counters are not labelled");
        return labelCounts[labelVal];
    }
    
    /** Update counters (either increment or decrement) according to the given
     * labelled Instance. We assume the label is Nominal here (checked in the
     * constructor). Update_instance is for use by add_instance and del_instance.
     * Update counters takes Instance weight into account during addition and
     * deletion.
     * @param instance	The Instance to be added/deleted from the BagCounters.
     * @param delta	Indicates whether given Instance is to be added or
     * deleted. 1 indicates addition. -1 indicates
     * deletion.
     */
    protected void update_counters(Instance instance, int delta) {
        Schema schemaRC = instance.get_schema();
        // Check that the LabelledInstanceInfo matches the original one.
        //      if (Globals.DBGSLOW)
        //      {
        //           schema.equal(instance.get_schema(), true);
        //           MLJ.ASSERT(delta == -1 || delta == +1,"BagCounters::update_counters: delta is not equal to -1 or +1.");
        //      }
        boolean updateLabel = labelCounts != null;
        
        int labelVal = Globals.UNKNOWN_CATEGORY_VAL - 1;
        
        double change = delta * instance.get_weight();
        
        if (updateLabel) {
            labelVal = schema.label_info().get_nominal_val(instance.get_label());
            labelCounts[labelVal] += change;
        }
        int numAttr = schemaRC.num_attr();
        
        //      for(int attrNum = 0 ; attrNum < numAttr - Globals.FIRST_NOMINAL_VAL ; attrNum++)
        for(int attrNum = 0 ; attrNum < numAttr; attrNum++)
            
        {
            // valueCounts is NULL iff attrCounts is NULL
            //         if (Globals.DBG)
            //              MLJ.ASSERT(valueCounts == null ||
            //               (valueCounts[attrNum] == null) ==
            //               (attrCounts[attrNum] == null),"BagCounters::update_counters: "
            //                  + "Either valueCounts, valueCounts[attrNum], or attrCounts[attrNum] is null);
            if (attrCounts[attrNum] != null) {
                int attrVal = schemaRC.attr_info(attrNum).get_nominal_val(instance.values[attrNum]);
                attrCounts[attrNum][attrVal] += change;
                if (updateLabel)
                    valueCounts[attrNum][labelVal][attrVal] += change;
            }
        }
    }
    
    /** Displays this BagCounters to the specified output stream.
     * @param stream	The output stream to be written to.
     */
    public void display(BufferedWriter stream) {
        try {
            stream.write(this.toString());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }    
    
    /** Convert the counter bag to a string.
     * @return A string containing the displayed output.
     */
    public String toString() {
        String output = new String();
        
        if (valueCounts == null)
            output = output +"Unlabelled counters" +'\n';
        else {
            output = output +"Value counters:" +'\n';
            for(int attrNum = 0 ; attrNum < valueCounts.length ;
            attrNum++) {
                output = output +"Attribute " +attrNum+":";
                if (valueCounts[attrNum] == null)
                    output = output +"  is not nominal." +'\n';
                else {
                    double[][] vc = valueCounts[attrNum];
                    
                    for(int labelInt = 0 ; labelInt < vc.length ;
                    labelInt++) {  //The output cannot be cast to int because weighting requires double values. -JL
                        output = output +"\n  Label " +(labelInt - 1)
                        +", label count=" +(int)label_count(labelInt)
                        +"\n  Value counts:  ";
                        for(int attrVal = 0 ; attrVal < vc[labelInt].length ;
                        attrVal++)
                            output = output + (int)vc[labelInt][attrVal]+", ";
                        output = output +'\n';
                    }
                }
            }
        }
        output = output +"Attribute counts" +'\n';
        for(int attr = 0 ; attr < attrCounts.length ; attr++) {
            output = output +"Attribute " +attr+"  ";
            if (attrCounts[attr] == null)
                output = output +"NULL" +'\n';
            else {
                double[] ac = attrCounts[attr];
                for(int attrVal = 0 ; attrVal < ac.length ; attrVal++)
                    output = output + (int)ac[attrVal]+", ";
                output = output+'\n';
            }
        }
        return output;
    }
    
    /** Returns the value of a value counter.
     * @return The number of values a specified attribute and label value has.
     * @param labelVal	The value of the label classification for which counters
     * are requested.
     * @param attrNum	The number of the attribute for which counters are
     * requested.
     * @param attrVal	The number of the value of the attribute for which
     * counters are requested.
     */
    public double val_count(int labelVal,int attrNum,int attrVal) {
        // Checking for valueCounts[attrNum] turns out to be very expensive
        //   in Naive-Bayes (nettalk) because it's done for every test
        //   instance 300*200 = 60000 times.
        //DBG(check_nominal(attrNum);
        if (valueCounts == null) {
            Error.fatalErr("BagCounters.val_count: the counters are not labelled");
        }
        MLJ.ASSERT(valueCounts[attrNum] != null,"BagCounters.val_count: valueCounts["+attrNum+"] == null");
        return (valueCounts[attrNum][labelVal][attrVal]);
    }    
}
