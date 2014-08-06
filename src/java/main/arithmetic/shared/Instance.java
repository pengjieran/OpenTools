package arithmetic.shared;

/** The class Instance provides instances with or without label and
 * support for describing instances, iterating through values, and looking at
 * the instance label. Most effort was directed at categorization of labelled
 * instances for supervised learning. Has been extended to tree-structured
 * attributes.
 */

public class Instance {
    
    /** The Schema for the data stored in this Instance. **/
    private Schema schema;
    /** The values for each Attribute stored in this Instance. **/
    public AttrValue[] values;
    /** The weight assigned to this Instance of data. **/
    private double weight;
    /** Count of the references to this Instance object.
     */
    private int refCount;
    /** The label this Instance is categorized as. **/
    private AttrValue labelValue;
    
    /** A string used for displaying purposes. **/
    private static final String INSTANCE_WRAP_INDENT = "   ";
    /** A string used for displaying purposes. **/
    private static final char END_OF_INSTANCE_LINE = '.';
    
    /** Cloning function for the Instance class. Provides a deep copy.
     * @return A copy of the Object.
     * @throws CloneNotSupportedException if the cloning process for contained data members encounters a
     * CloneNotSupportedException.
     */
    public Object clone() throws CloneNotSupportedException {
        Instance newClone = new Instance(schema);
        newClone.set_weight(weight);
        newClone.set_label((AttrValue)labelValue.clone());
        for(int i=0;i<schema.num_attr();i++)
            newClone.values[i] = (AttrValue)get_value(i).clone();
        
        return newClone;
    }
    
    /** Constructor for the Instance class.
     * @param newSchema	The Schema for the data stored in this Instance.
     */
    public Instance(Schema newSchema) {
        values = new AttrValue[newSchema.num_attr()];
        for(int i = 0; i < values.length; values[i++] = new AttrValue());
        refCount = 1;
        weight = 1.0; //default value
        schema = newSchema;
        //DBG(init_values())
        //DBG(OK())
    }
    
    /** Copy constructor for the Instance class.
     * @param source	The original Instance to be copied.
     */
    public Instance(Instance source) {
        schema = source.schema;
        values = source.values;
        labelValue = source.labelValue;
        refCount = 1;
        
        weight = source.weight;
        //      DBG(OK());
    }
    
    
    /** Returns the Schema of attributes for this Instance.
     * @return The Schema for this Instance.
     */
    public Schema get_schema(){return schema;}
    
    /** Returns the number of attributes for data in this Instance.
     * @return The number of attributes in this Instance.
     */
    public int num_attr(){return schema.num_attr();}
    
    /** Returns the label this Instance is categorized as.
     * @return The labelValue for this Instance.
     */
    public AttrValue get_label(){return labelValue;}
    
    /** Returns a copy with only the attributes included that are are indicated
     * by the mask. Schema.project() should be called to create the new Schema
     * for the Instance.
     * @return The new Instance with the attributes specified.
     * @param shortSchema	The Schema for the new Instance created.
     * @param attrMask		An array of boolean values equal in length to the
     * original number of attributes. Each element
     * relates to an attribute of the original
     * instance. If the element is set TRUE, the
     * corresponding attribute is included in the new
     * Instance.
     */
    public Instance project(Schema shortSchema, boolean[] attrMask) {
        //DBG(OK());
        //ASSERT(attrMask.size() == num_attr());
        
        if(get_schema().is_labelled() != shortSchema.is_labelled())
            Error.err("Instance.project: short schema "
            +" has different labelled status from this schema-->"
            +"fatal_error");
        
        Instance newInst = new Instance(shortSchema);
        int newnum = 0;
        for(int i=0;i<num_attr();i++) {
            if(attrMask[i]) {
                newInst.values[newnum] = values[i];
                newnum++;
            }
        }
        
        if(get_schema().is_labelled())
            newInst.set_label(get_label());
        
        //set the weight for this instance
        newInst.set_weight(get_weight());
        
        return newInst;
    }
    
    // reimplementation of overrided [] method
    /** Returns the attribute value at the specified attribute.
     * @return The attribute value stored for the attribute at that index.
     * @param index	The index number for the specified attribute.
     */
    public AttrValue get_value(int index) {
        return values[index];
    }
    
    /** Sets the Schema for this Instance.
     * @param schemaRC	The new Schema for this Instance.
     */
    public void set_schema(Schema schemaRC) //SchemaRC
    {
        if(schemaRC.num_attr() != schema.num_attr())
            Error.err("Instance::set_schema(): attribute "
            +"count does not match -->fatal_error");
        schema = schemaRC;
    }
    
    /** Sets the weight for this Instance.
     * @param wt	The new weight for this Instance.
     */
    public void set_weight(double wt) {
        weight = wt;
    }
    
    /** Sets the label for this Instance.
     * @param lvalue	The new label value for this Instance.
     */
    public void set_label(AttrValue lvalue) {
        if(!schema.is_labelled())
            System.out.println("Instance::set_label():labelInfo is not set ->fe");
        schema.label_info().check_in_range(lvalue);
        labelValue = lvalue;
    }
    
    /** Returns the weight for this Instance.
     * @return The weight for this Instance.
     */
    public double get_weight(){return weight;}
    
    /** Displays the Instance with labels.
     * @param displayWeight	TRUE if the weight for this Instance should be shown,
     * FALSE otherwise.
     * @param normalizeReals TRUE if the attribute values should be normalized,
     * FALSE otherwise.
     */
    public void display(boolean displayWeight, boolean normalizeReals) {
        display_unlabelled(displayWeight, normalizeReals);
        if(is_labelled()){
            if(schema.num_attr() > 0)
                System.out.print(", ");
            AttrInfo ai = schema.label_info();
            String data = ai.attrValue_to_string(labelValue);
            //if(protectChars)
            //   data = protect_chars(data);
            System.out.print(data);
        }
        System.out.println();
        
    }
    
    /** Displays the Instance without labels.
     * @param normalizeReal TRUE if the attribute values should be normalized,
     * FALSE otherwise.
     * @param displayWeight TRUE if the weight for this Instance should be shown,
     * FALSE otherwise.
     */
    public void display_unlabelled(boolean displayWeight, boolean normalizeReal) {
        String separator =  new String(", ");
        if(displayWeight)
            System.out.println(weight+separator);
        for(int attrNum = 0;attrNum<schema.num_attr();attrNum++){
            AttrInfo ai = schema.attr_info(attrNum);
            if(normalizeReal && ai.can_cast_to_real()){
                RealAttrInfo rai = ai.cast_to_real();
                if(rai.is_unknown(values[attrNum]))
                    System.out.print(AttrInfo.UNKNOWN_VAL_STR);
                else
                    System.out.print(rai.normalized_value(values[attrNum]));
                if(attrNum < schema.num_attr() -1)
                    System.out.print(separator);
            }
            else{
                //System.out.println("Values.length = " + values.length);
                //System.out.println("attrNum = "+attrNum);
                if(values[attrNum] == null)System.out.println("Error");
                String data = ai.attrValue_to_string(values[attrNum]);
                if(ai.can_cast_to_nominal())// && protectChars)
                    ;// data = protect_chars(data);
                System.out.print(data);
                if((attrNum < (schema.num_attr()-1))&&(!(data.equals(""))))
                    System.out.print(separator);
                
            }
        }
    }
    
    /** Checks if this Instance is labelled.
     * @returns TRUE if the Instance is labelled, FALSE otherwise.
     * @return TRUE if the Instance contains a label value, FALSE otherwise.
     */
    public boolean is_labelled() {
        boolean fatalOnFalse = false;
        return schema.is_labelled();
    }
    
    /** Checks if this Instance is labelled.
     * @return TRUE if the Instance is labelled, FALSE otherwise.
     * @param fatalOnFalse TRUE if an Error message should be displayed if there
     * is no label for this Instance, FALSE otherwise.
     */
    public boolean is_labelled(boolean fatalOnFalse) {
        return schema.is_labelled(fatalOnFalse);
    }
    
    
    /** Returns the information about the label this Instance is categorized as.
     * @return The information about the label value.
     */
    public AttrInfo label_info() {
        if (!schema.is_labelled())
            Error.fatalErr("Instance::label_info(): label is not set ");
        return schema.label_info();
    }
    
    /** Transfers this Instance to a String value.
     * @return A String with representations of the data stored in this Instance.
     */
    public String toString() {
        boolean displayWeight = false;
        boolean normalizeReals = false;
        String rtrn = "";
        String separator =  new String(", ");
        if(displayWeight)
            rtrn = rtrn + weight + separator;
        for(int attrNum = 0;attrNum<schema.num_attr();attrNum++){
            AttrInfo ai = schema.attr_info(attrNum);
            if(normalizeReals && ai.can_cast_to_real()){
                RealAttrInfo rai = ai.cast_to_real();
                if(rai.is_unknown(values[attrNum]))
                    rtrn = rtrn + "?";//Globals.UNKNOWN_VAL_STR;
                else
                    rtrn = rtrn + rai.normalized_value(values[attrNum]);
                if(attrNum < schema.num_attr() -1)
                    rtrn = rtrn + separator;
            }
            else{
                //System.out.println("Values.length = " + values.length);
                //System.out.println("attrNum = "+attrNum);
                if(values[attrNum] == null)System.out.println("Error");
                String data = ai.attrValue_to_string(values[attrNum]);
                if(ai.can_cast_to_nominal())// && protectChars)
                    ;// data = protect_chars(data);
                rtrn = rtrn + data;
                if(data.equals("")) rtrn = rtrn + "?";
                if(attrNum < (schema.num_attr()-1))
                    rtrn = rtrn + separator;
                
            }
        }
        if(is_labelled()){
            if(schema.num_attr() > 0)
                rtrn = rtrn + ", ";
            AttrInfo ai = schema.label_info();
            String data = ai.attrValue_to_string(labelValue);
            //if(protectChars)
            //   data = protect_chars(data);
            rtrn = rtrn + data;
        }
        rtrn = rtrn + "\n";
        return(rtrn);
        
    }
    
    /** Returns the information about the attribute specified.
     * @return The information about the attribute.
     * @param attrNum	The index of the attribute specified.
     */
    public AttrInfo attr_info(int attrNum) {
        return schema.attr_info(attrNum);
    }
    
    /** Checks if this Instance is equal to the specified Instance. Compares the
     * attributes values, the weight, and the label of this Instance.
     * @return TRUE if these Instances are equal, FALSE otherwise.
     * @param i_instance	The Instance to be compared to.
     */
    public boolean equals(Instance i_instance) {
        return equal(i_instance,true,true,false);
    }
    
    /** Compares this Instance to the specified Instance. This function may
     * optionally compare the label and the weight as part of this process. Setting
     * fatalOnFalse will cause equal() to abort with a clear error message
     * if the equality test fails.
     * @return TRUE if these Instances are equal, FALSE otherwise.
     * @param instance		The Instance to be compared to.
     * @param compLabel		TRUE if labels are to be compared, FALSE otherwise.
     * @param compWeight		TRUE if weights are to be compared, FALSE otherwise.
     * @param fatalOnFalse	TRUE if an Error message should be displayed if
     * the Instances are not equal, FALSE otherwise.
     */
    boolean equal(Instance instance,
    boolean compLabel, boolean compWeight,
    boolean fatalOnFalse) {
        // if one instance is labelled and the other is not AND we're
        // comparing labels, its a mismatch.
        if( compLabel && (is_labelled() != instance.is_labelled())) {
            if (fatalOnFalse) {
                Error.fatalErr("Instance::equal: equalilty failed (only one of the"
                +"two instances has label).");
            }
            else
                return false;
        }
        
        // compare labels if compLabel is set
        else if(compLabel && is_labelled()) {
            //      DBGSLOW(schema.label_info().equal(instance.get_schema().label_info(),
            //					TRUE));
            if(!schema.label_info().equal_value(labelValue,instance.get_label())) {
                if(fatalOnFalse) {
                    Error.fatalErr("Instance::equal: equality failed (label values "
                    +"differ).");
                }
                return false;
            }
        }
        
        // compare weights if compWeight is set
        if(compWeight) {
            if(!MLJ.approx_equal((float)get_weight(),
            (float)(instance.get_weight()))) {
                if(fatalOnFalse) {
                    Error.fatalErr("Instance::equal: equality failed (weights do not match: "
                    +get_weight() +" vs " +instance.get_weight()
                    +").");
                }
                return false;
            }
        }
        
        // check schemas and number of values (DBG only)
        //   DBGSLOW(get_schema().equal_no_label(instance.get_schema(), TRUE));
        //   DBG(if (instance.values.size() != values.size())
        //       err << "Instance::equal: number of values in instance "
        //       "do not match.  class=" << values.size()
        //       << " and rhs=" << instance.values.size() << fatal_error);
        
        // compare attribute values
        for (int i = 0; i < schema.num_attr(); i++) {
            if (!attr_info(i).equal_value( values[i], instance.values[i] )) {
                if( fatalOnFalse ) {
                    Error.fatalErr("Instance::equal: equality failed (differed on "
                    +attr_info(i).name() +").");
                }
                return false;
            }
        }
        
        // comparison succeeded
        return true;
    }
    
    /** Returns a String representation of the Instance.
     * @param displayWeight TRUE if the Instance weight is to be displayed, FALSE otherwise.
     * @param normalizeReals TRUE if the real values in the Instance are to be normalized, FALSE otherwise.
     * @return A String representation of the Instance.
     */
    public String out(boolean displayWeight, boolean normalizeReals) {
        String rtrn = new String();
        rtrn = rtrn + display_unlabelled_out(displayWeight, normalizeReals);
        if(is_labelled()){
            if(schema.num_attr() > 0)
                rtrn = rtrn + ", ";
            AttrInfo ai = schema.label_info();
            String data = ai.attrValue_to_string(labelValue);
            //if(protectChars)
            //   data = protect_chars(data);
            rtrn = rtrn + data;
        }
        rtrn = rtrn + "\n";
        return rtrn;
    }
    
    /** Returns a String representation of the Instance.
     * @param displayWeight TRUE if the Instance weight is to be displayed, FALSE otherwise.
     * @param normalizeReal TRUE if the real values in the Instance are to be normalized, FALSE otherwise.
     * @return A String representation of the Instance.
     */
    public String display_unlabelled_out(boolean displayWeight, boolean normalizeReal) {
        String rtrn = new String();
        String separator =  new String(", ");
        if(displayWeight)
            rtrn = rtrn + (weight+separator);
        for(int attrNum = 0;attrNum<schema.num_attr();attrNum++){
            AttrInfo ai = schema.attr_info(attrNum);
            if(normalizeReal && ai.can_cast_to_real()){
                RealAttrInfo rai = ai.cast_to_real();
                if(rai.is_unknown(values[attrNum]))
                    rtrn = rtrn + AttrInfo.UNKNOWN_VAL_STR;
                else
                    rtrn = rtrn + rai.normalized_value(values[attrNum]);
                if(attrNum < schema.num_attr() -1)
                    rtrn = rtrn + separator;
            }
            else{
                //System.out.println("Values.length = " + values.length);
                //System.out.println("attrNum = "+attrNum);
                if(values[attrNum] == null)System.out.println("Error");
                String data = ai.attrValue_to_string(values[attrNum]);
                if(ai.can_cast_to_nominal())// && protectChars)
                    ;// data = protect_chars(data);
                rtrn = rtrn + data;
                if((attrNum < (schema.num_attr()-1))&&(!(data.equals(""))))
                    rtrn = rtrn + separator;
            }
        }
        return rtrn;
    }
    
    /** Returns the AttrValue at the specified index number.
     * @return The AttrValue at the specified index.
     * @param index The index specified.
     */
    public AttrValue index(int index) {
        return values[index];
    }
    
    /** Copies the supplied Instance object into this Instance object.
     * @param other The Instance object to be copied.
     * @throws CloneNotSupportedException if the cloning process for contained data members encounters a
     * CloneNotSupportedException.
     */
    public void copy(Instance other) throws CloneNotSupportedException{
        values = new AttrValue[other.schema.num_attr()];
        for(int i = 0; i < values.length; values[i++] = new AttrValue());
        schema = other.schema;
        refCount = other.refCount;
        set_weight(weight);
        set_label((AttrValue)labelValue.clone());
        for(int i=0;i<values.length;i++)
            values[i] = (AttrValue)other.get_value(i).clone();
    }
    
    /** Remove an Attribute from an Instance. When doing many projections, it is more
     * efficient to supply a Schema with the deleted attribute.
     * @param attrNum The number of the attribute to be removed from this InstanceObject.
     * @param schemaWithDelAttr Schema with the attribute deleted.
     * @return The modified Instance.
     */
    public Instance remove_attr(int attrNum, Schema schemaWithDelAttr) {
        if ( attrNum < 0 || attrNum >= num_attr() )
            Error.fatalErr("Instance.remove_attr(int,Schema): illegal attrNum \n"
            +attrNum+" is passed but the proper range is \n"+" 0 to "+(num_attr() - 1)
            +".");
        
        Instance instance = new Instance(schemaWithDelAttr);
        for (int i = 0; i < schema.num_attr(); i++)
            if (i != attrNum){
                index(i - ((i > attrNum)?1:0)).copy(index(i));
            }
        if (schema.is_labelled()) // both are labelled becauseof
            // equal_except_del_attr
            instance.set_label(get_label());
        return instance;
    }
    
    /** Remove an Attribute from an Instance. When doing many projections, it is more
     * efficient to supply a Schema with the deleted attribute.
     * @param attrNum The number of the attribute to be removed from this InstanceObject.
     * @return The modified Instance.
     */
    public Instance remove_attr(int attrNum) {
        if ( attrNum < 0 || attrNum >= num_attr() )
            Error.fatalErr("Instance.remove_attr(int): illegal attrNum \n"
            +attrNum+" is passed but the proper range is \n"+" 0 to "+(num_attr() - 1)
            +".");
        
        Schema schemaNew = schema.remove_attr(attrNum);
        return remove_attr(attrNum, schemaNew);
    }
    
    
    /** Returns the number of values for the specified attribute.
     * @param attrNum The number of the attribute for which the number of values is requested.
     * @return The number of attribute values.
     */
    public int num_attr_values(int attrNum){
        return schema.num_attr_values(attrNum);
    }
    
    /** Returns the number of possible label values in the Schema of this Instance.
     * @return The number of possible label values.
     */
    public int num_label_values() {
        return nominal_label_info().num_values();
    }
    
    /** Returns the information on the label attribute.
     * @return Information on the label attribute.
     */
    public NominalAttrInfo nominal_label_info() {
        return label_info().cast_to_nominal();
    }
    
    /** Returns the attribute name.
     * @param attrNum The number of the attribute.
     * @return The name of the attribute.
     */
    public String attr_name(int attrNum){
        return schema.attr_name(attrNum);
    }
    
}
