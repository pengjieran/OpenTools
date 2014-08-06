package arithmetic.shared;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

/** Schema defines the attribute information. Most effort was directed at labelled
 * instances for supervised learning, and on discrete attributes.
 * @author James Louis Java Implementation.
 * @author Dan Sommerfield 2/21/97 Added loss functions.
 * @author Robert Allen Added project(). 1/27/95
 * @author Chia-Hsin Li Added display_names(). 9/29/94
 * @author Yeogirl Yun Merged LabelledInstanceInfo and InstanceInfo into Schema
 * and made it reference-count class. 6/12/94
 * @author Richard Long 5/27/94 AttrInfo stored as Array instead of linked list.
 * @author Richard Long 1/21/94 Added weighted option.
 * @author Svetlozar Nestorov 1/8/94 Added copy constructors.
 * @author Ronny Kohavi 8/18/93 Change equal to non-virtual, eliminated
 * combinations of LabelledInstanceInfo and InstanceInfo for equal and operator=.
 * See coding standards for explanations.
 * @author Richard Long 7/14/93 Initial revision (.c)
 * @author Ronny Kohavi 7/13/93 Initial revision (.h)
 *
 */
public class Schema
{
    /** Array of information on attributes.
     */    
   AttrInfo[] attr;
   /** Information on labels.
    */   
   AttrInfo labelInfo;
   /** The number of references to this Schema.
    */   
   int refCount;
   /** Loss matrix for this schema.
    */   
   double[][] lossMatrix;
   /** TRUE if the Lossmatrix should be transposed with the attribute information.
    */   
   boolean transposeLossMatrix = false;
   /** Constructor.
    * @param attrInfos	The information about each attribute to be
    * stored in this Schema.
    */
   public Schema(LinkedList attrInfos)
   {
      attr = new AttrInfo[attrInfos.size()];
      lossMatrix = null;
      refCount = 1;
  
       
      labelInfo = null;
      ListIterator pix = attrInfos.listIterator(0);
      for (int i=0; i < attrInfos.size(); i++){
         if(pix.hasNext())attr[i] = (AttrInfo)pix.next();  
         if(attr[i] == null)
            Error.err("Schema::Schema:attribute info "
               + i + "is NULL --> fatal_error");
      }
      attrInfos = null;
      OK(); 
   }
 
   /** Constructor.
    * @param attrInfos	The information about the each attribute to be
    * stored in this Schema.
    * @param lInfo		The information about the labels for each
    * catagory.
    */
   public Schema(LinkedList attrInfos,AttrInfo lInfo)
   {  
      attr = new AttrInfo[attrInfos.size()];
      lossMatrix = null;
      refCount = 1;
   
      if(lInfo == null)
         Error.err("Error-->Schema::Schema: Null labelInfo not"
            +" not allowed.  Use the constructor which does not require"
            +" a labelInfo argument, fatal_error");

      labelInfo = lInfo;
      ListIterator pix = attrInfos.listIterator(0); //(1) CHANGE
      for(int i=0; i< attrInfos.size();i++) {
         attr[i] = (AttrInfo)pix.next();
         if(attr[i] == null)
            Error.err("Schema::Schema: attribute info "
               +i+"is Null, fatal_error");
      }

      attrInfos = null;
      lInfo = null;
      
      OK(); 
   }

   /** Copy Constructor.
    * @param source The Schema object to be copied.
    * @throws CloneNotSupportedException if the cloning process for the new Schema experiences an Exception.
    */
   public Schema(Schema source) throws CloneNotSupportedException 
   {
      attr = new AttrInfo[source.num_attr()];
      //instanceOp = null;
      lossMatrix = null;
      refCount = 1;

      for(int i=0;i < source.attr.length; i++) {
         AttrInfo nextAttr = (AttrInfo)source.attr_info(i).clone(); //clone();
	 attr[i] = nextAttr;
      }
      
      if(source.is_labelled())
         labelInfo = (AttrInfo)source.labelInfo.clone(); //clone();
      else
         labelInfo = null;
   
      if(source.lossMatrix != null)
         lossMatrix = source.lossMatrix;

      //copy instance operation, if there is one
      //if(source.instanceOp)
      //   instanceOp = source.instanceOp; //clone();
	 
      //DBG(OK());
   }

   /** Checks if this Schema object has a loss matrix.
    * @return TRUE if there is a loss matrix, FALSE otherwise.
    */
   public boolean has_loss_matrix(){return lossMatrix != null;}
   
   /** Returns the loss matrix for this Schema object.
    * @return The loss matrix.
    */
   public double[][] get_loss_matrix()
   {
      return lossMatrix;
   }
   
   /** Sets the loss matrix to the matrix specified.
    * @param aLossMatrix	The new matrix to be stored in this Schema
    * object.
    */
   public void set_loss_matrix(double[][] aLossMatrix)
   {
      establish_loss_matrix();
      lossMatrix = aLossMatrix;

      //some error checking here !
   }

   /** Allocates the loss matrix for this Schema object. The Schema must have
    * label information or an error message will be displayed.
    */
   public void establish_loss_matrix()
   {
      if(!is_labelled())
         Error.err("Schema::establish_loss_matrix: the"
	    +" data must be labelled -->fatal_error");

      //The dimensions of the lossmatrix in MLC++ are [-1..num_label_values]
      //[0..num_label_values + 1].  So we will make these [0..nlv][0..nlv+1]
      if(lossMatrix == null)
         lossMatrix = new double[num_label_values()][num_label_values()+1];
   
      //left out some stuff about dimensions of 2D array.  See Schema.c 
   }

   /** Creates a new Schema object which contains only the specified attributes.
    * @return The new Schema created.
    * @param attrMask A boolean array where each element represents an attribute
    * in the Schema. It should have have the same number of
    * elements as attributes. Elements set to true will result in
    * the corresponding attribute being included in the new
    * Schema.
    * @throws CloneNotSupportedException if the cloning process for the new Schema experiences an Exception.
    */
   public Schema project(boolean[] attrMask) throws CloneNotSupportedException
   {
      //DBG(OK());
      //ASSERT(attrMask.size() == num_attr());
      LinkedList attrInfos = new LinkedList();
      int newnum = 0;
      for(int i=0;i<num_attr();i++) {
         if(attrMask[i]) {
	    AttrInfo ai = (AttrInfo)attr_info(i).clone();  //clone()
	    newnum++;
	    attrInfos.add(ai);
	 }
      }

      if(is_labelled()) {
         AttrInfo labelInfo = label_info(); //clone()
	 Schema newSchema = new Schema(attrInfos, labelInfo);

	 //if the previous schema had a loss matrix, set it inside the new
	 //shema too
	 if(has_loss_matrix())
	    newSchema.set_loss_matrix(get_loss_matrix());

	 //newSchema gets ownership
	 //ASSERT(attrInfos == null); ASSERT(labelInfo == null);
	 return newSchema;
      }
      else{
         Schema newSchema = new Schema(attrInfos);
	 //ASSERT(attrInfos == null);
	 return newSchema;
      }
   }

   /** Casts the specified attribute to a nominal attribute.
    * @return The NominalAttrInfo object of the specified attribute.
    * @param attrNum	The index number of the specified attribute.
    */
   public NominalAttrInfo nominal_attr_info(int attrNum)
   {
      return attr_info(attrNum).cast_to_nominal();
   }

   /** Validate assertions.
    */
   private void OK()
   {
      int level = 0; 
      // ASSERT refCount >= 0
      check_duplicate_attrs();

      if (level <1) {
         for(int i=0; i< attr.length; i++)
            if (attr[i]==null)
               Error.err("Shema::OK: attribute info "
                  + i + "is NULL --> fatal_error");
      }

      if(lossMatrix != null){
         //ASSERT is_labelled() == true 
         int numLabelVals = num_label_values();
         //ASSERT(lossMatrix->start_row() == FIRST_CATEGORY_VAL);
         //ASSERT(lossMatrix->start_col() == UNKNOWN_CATEGORY_VAL);
         //ASSERT(lossMatrix->num_rows() == numLabelVals);
         //ASSERT(lossMatrix->num_cols() == numLabelVals + 1);
      } 
   }

   /** Returns the number of values possible for the specified attribute.
    * @return The number of values.
    * @param attrNum	The index number of the specified attribute.
    */
   public int num_attr_values(int attrNum)
   {
      return nominal_attr_info(attrNum).num_values();
   }


   /** Returns the number of classification labels possible for this Schema.
    * @return The number of labels.
    */
   public int num_label_values()
   {
      return nominal_label_info().num_values();
   }
   
   /** Casts the label information to a nominal attribute.
    * @return The NominalAttrInfo containing the label information.
    */
   public NominalAttrInfo nominal_label_info()
   {
      return label_info().cast_to_nominal();
   }

   /** Returns the label information.
    * @return The AttrInfo containing the label information.
    */
   public AttrInfo label_info()
   {
      if (!is_labelled())
         Error.err("Schema::label_info() : labelInfo is "
            + "NULL -->fatal_error");
      return labelInfo; 
   }
   
   /** Checks if this Schema has label information.
    * @return TRUE if label information is present, FALSE otherwise.
    */
   public boolean is_labelled()
   {
      return is_labelled(false);
   }

   /** Checks if this Schema has label information.
    * @return TRUE if label information is present, FALSE otherwise.
    * @param fatalOnFalse	If set to TRUE, an error message will be displayed if
    * there is no label information in this Schema.
    */
   public boolean is_labelled(boolean fatalOnFalse)  
   {
      if(labelInfo == null)
      {   if(fatalOnFalse==true){
            Error.err("Schema::is_labelled(): labelInfo "
               + "is NULL --> fatal_error");
            return false;
         } else return false;
      }else
         return true;
   } 

   /** Checks if there are any duplicate attributes in this Schema and displays
    * an error message listing them if they do exist.
    * @return TRUE if duplicate attributes exist, FALSE otherwise.
    */
   private boolean check_duplicate_attrs()
   {  boolean fatalOnTrue = true;

      String[] dups = find_duplicate_attrs(); 
 
      //if we found any dup names, display them here in an error message
      boolean hasDups = (dups.length > 0);  
      if(fatalOnTrue == true && hasDups == true){
         Error.err("Schema::check_duplicate_attrs: Duplicate"
            + " attributes found... fatal_error");
         // dups->display(err);
      }
      return hasDups; 
   }

   /** Finds any duplicate attributes in this Schema.
    * @return The names of the duplicate attributes.
    */
   private String[] find_duplicate_attrs()
   {
      //place all att names in an array of String, and sort
      String[] nameArray = new String[num_attr()];
      for(int i=0;i<num_attr();i++)
         nameArray[i] = attr_name(i);
      Arrays.sort(nameArray);  // not sure about this, may need a Comparator

      // Any dups will now be adjacent. Check for duplicity.
      // Accumulate all dups found into a dups array.
      String[] dupsArray = new String[0];  
      for(int i=1;i<num_attr();i++){
         int size = dupsArray.length;
         if(nameArray[i].equals(nameArray[i-1])){
            if(size>0 && nameArray[i-1].equals(dupsArray[size-1])) 
               ; //don't add again if more than two copies 
            else {
               // Hacked Dynamic Array version 
               String[] temp = new String[size+1];
               for(int j=0;j<size;j++)temp[j]=dupsArray[j];
               dupsArray = temp;
               dupsArray[size] = nameArray[i];
            } 
         }
      }
     
      //Log full schema if duplicates were found
      if(dupsArray.length > 0)
         Error.err("GLOBALLOG-->Schema::find_diplicate_attrs : "
             + " Schema containing duplicates");
    
      return dupsArray; 
   }
   
   /** Returns the name of the specified attribute.
    * @return The name of the specified attribute.
    * @param attrNum	The index number of the specified attribute.
    */
   public String attr_name(int attrNum)
   {
      return attr_info(attrNum).name();
   } 

   /** Returns the number of attributes in this Schema object.
    * @return The number of attributes in this Schema object.
    */
   public int num_attr()
   {
      return attr.length;
   }  

   /** Returns the AttrInfo object containing the specified attribute's
    * information.
    * @return The AttrInfo object containing the specified attribute's
    * information.
    * @param num	The index of the specified attribute.
    */
   public AttrInfo attr_info(int num)
   {
      return attr[num];
   } 

   /** Returns the name of the specified category label.
    * @return The name of the specified category label.
    * @param cat	The specified category.
    */
   public String category_to_label_string(int cat)
   {
      return nominal_label_info().get_value(cat);
   }

   /** Displays the names file associated with the instances. The reason we
    * don't call labelInfo.display_values() is to avoid printing the name of the
    * label since a label doesn't have a name.
    * @param stream The Writer to which the Schema will be displayed.
    * @param protectChars TRUE if characters should be protected for display.
    * @param header The String to use as a header to the display.
    */
   public void display_names(Writer stream, 
         boolean protectChars, String header)
   {
      try{
         stream.write("|"+header+"\n");
         if(is_labelled())
            label_info().display_attr_values(stream, protectChars);
         else stream.write("nolabel\n");
         stream.write("\n"); // Have an extra blank line for clarity

   // display attribute values
         for (int attrNum = 0; attrNum < num_attr(); attrNum++) {
            stream.write(attr[attrNum].name()+": ");
            attr[attrNum].display_attr_values(stream, protectChars);
         }
      }catch(IOException e){e.printStackTrace(); System.exit(1);}
   }

   /** Returns TRUE if all attributes in the schema are nominals }
    * (or can be cast to nominals).
    * @return TRUE if all of the attributes are nominal, FALSE otherwise.
    */
  public boolean is_nominal() {
    boolean result = true; 
    for(int i=0; i<num_attr(); i++) {
      if(!attr_info(i).can_cast_to_nominal()) {
        result = false; 
      }
    }
    return result;
  }

  /** Create permutation array for labels if sorting is requested
   * The array is owned by the caller and must be deleted.
   * @return A array of index values of label inromation after sorting.
   */
  public int[] sort_labels() {
    NominalAttrInfo nai = nominal_label_info(); 
    int numLabelValues = nai.num_values(); 
    int[] permutation = new int[numLabelValues]; 
   
    // Find the label permuation for sorted labels
    boolean sortLabels = true;  //get_option_bool("SORT_LABELS", TRUE,
					         //"Sort the labels", TRUE); 
    StringNum[] labels = new StringNum[numLabelValues]; 
    for (int i = 0; i < numLabelValues; i++) { 
//      labels[i].assign_str(nai.get_value(i));
//      labels[i].num = i; 
      labels[i].str = nai.get_value(i); 
      labels[i].num = i; 
      labels[i].convert(); 
    }
    if (sortLabels && !nai.is_linear()) {
      labels = sort(labels); 
    }
    for (int i = 0; i < numLabelValues; i++) {
      permutation[i] = labels[i].num; 
    }
    return permutation; 
  }



  /** String to Double transformation device.
   */  
public class StringNum {
    /** The Integer value of the transformed String.
     */    
  public int num; 
  /** The String to be transformed.
   */  
  public String str; 
  /** The Double value of the String.
   */  
  public double dstr; 
  /** TRUE if this String is transformed, FALSE otherwise.
   */  
  public boolean isconverted = false; 
  /** Constructor.
   * @param s The string to be transformed.
   */  
  public StringNum(String s) {
    str = s; 
  }

  /** Converts the stored String value to a Double value and stores the value in dstr.
   */  
  public void convert() {
    if (!isconverted) {
      dstr = Double.valueOf(str).doubleValue(); 
      isconverted = true; 
    }
  }
}; 

/** Sorts an array of StringNums from smallest to greatest value.
 * @param sn An array of StringNum values to be sorted.
 * @return The sorted array of StringNum values.
 */
  public static StringNum[] sort(StringNum[] sn) {
    for(int e = 0; e < sn.length; e++) {
      int indexofmin = findindexofmin(sn, e); 
      StringNum temp = sn[e]; 
      sn[e] = sn[indexofmin]; 
      sn[indexofmin] = temp; 
    }
    for (int e = 0; e < sn.length-1; e++) {
      if (sn[e].dstr > sn[e+1].dstr) {
        Error.fatalErr("Schema.sort(): Sort error at index "+e+"."); 
      }
    }
    return sn; 
  }

  /** Finds the index of the minimum value in an array of StringNum values.
   * @param sn The array of StringNum values to be searched.
   * @param start The start index.
   * @return The index of the smallest value after the start index.
   */  
  public static int findindexofmin(StringNum[] sn, int start) {
    int indexofmin = start; 
    double mindoublevalue = sn[start].dstr; 
    for (int e = start+1; e < sn.length; e++) {
      if(mindoublevalue > sn[e].dstr) {
        mindoublevalue = sn[e].dstr; 
        indexofmin = e; 
      }
    }
    return indexofmin; 
  }

  /** Removes a loss matrix from this Schema object. This occurs during reading if any
   * extra values get added to the label
   */  
  public void remove_loss_matrix() {
      lossMatrix = null;
  }
  
  /** Returns a Schema with the given AttrInfo removed and the infos renumbered to be
   * sequential starting at 0.
   *
   * @param attrNum The number of the attribute to be removed.
   * @return A copy of this Schema with the specified attribute removed.
   */  
  public Schema remove_attr(int attrNum) {
      Schema newSchema = null;
      try{
          if ( attrNum < 0 || attrNum >= num_attr() )
              Error.fatalErr("Schema.remove_attr(const int): illegal attrNum \n"
              +attrNum+" is passed but the proper range is \n 0 to "+(num_attr() - 1)
              +".");
          
          LinkedList attrInfos = new LinkedList();
          // Add AttrInfos that keep the same attrNum
          for (int i = 0; i < attrNum; i++) {
              AttrInfo ai = (AttrInfo)attr_info(i).clone();
              attrInfos.add(ai);
          }
          // Add AttrInfos that need to have their attrNums subtracted by 1
          for (int i = attrNum + 1; i < num_attr(); i++) {
              AttrInfo ai = (AttrInfo)attr_info(i).clone();
              attrInfos.add(ai);
          }
          
          if (is_labelled()) {
              
              AttrInfo label = (AttrInfo)label_info().clone();
              newSchema = new Schema(attrInfos, label);
              //      ASSERT(label == null);
          }
          else
              newSchema = new Schema(attrInfos);
          //   ASSERT(attrInfos == NULL);
      }catch(CloneNotSupportedException e){
          e.printStackTrace();
      }
      return newSchema;
  }

}
