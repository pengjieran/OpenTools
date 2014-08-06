package arithmetic.shared;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.Writer;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

/** The InstanceList class provides basic functions for ordered lists of
 * instances. Instances may be labelled or unlabelled. Depending on
 * usage, the list may or may not keep counts about various data in the list.
 * These counts are kept in the BagCounters class.                          <P>
 * Assumptions  :                                                           <P>
 * File format follows Quinlan (pp. 81-83) EXCEPT:                          <BR>
 * 1)  , : | \ . do not appear in names                                     <BR>
 * 2) . at end of lists is optional                                         <BR>
 * 3) a word that appears before the labels are enumerated, that is preceded by
 * \ is interpreted as a modifier.  Currently, the only implemented modifier is
 * "weighted", which indicates that the list will be weighted. This means that
 * labels are assumed to be nominal type for read_names().                  <P>
 * Comments     :                                                           <P>
 * Line numbers given are the result of '\n', not wrapping of lines.        <P>
 * "continuous" is not a legal name for the first nominal attribute value; it
 * is reserved to indicate a continuous(RealAttrInfo) attribute.            <P>
 * "discrete" is not a legal name for the first nominal attribute value; it is
 * reserved to indicate a discrete (NominalAttrInfo) attribute but with a
 * dynamic set of values to be specified as they appear in the data file.   <P>
 * "discrete n" is supported, where n is an estimate of the number of values of
 * the attribute.                                                           <P>
 * "nolabel" may be specified as the label field ONLY. If specified, it
 * indicates an unlabelled list.                                            <P>
 *
 * Enhancements :                                                           <P>
 * Cause fatal_error() if read_names() is called by methods other than the
 * constructor or InstanceList.read_names()                                 <P>
 * Extend read_attributes to handle AttrInfo other than NominalAttrInfo and
 * RealAttrInfo.                                                            <P>
 * Expand capability of input function to:                                  <P>
 * 1) allow . in name if not followed by a space                            <P>
 * 2) allow , : | and \ in names if preceded by a backslash                 <P>
 * (this would mimic Quinlan)                                               <P>
 * Use lex to do the lexical analysis of the input file. This will be critical
 * if the syntax becomes more complicated.                                  <P>
 * Ideally impute_unknown_values would  handle both nominal and real values in
 * a single pass.  It should accept an array of operators allowing each
 * attribute to handle unknowns in a different way.  Obvious operators would be:
 * unique_value, mode, mean...                                              <P>
 *
 * @author James Louis 12/08/2000 Ported to Java.
 * @author Alex Kozlov 8/22/96 Added transpose() function.
 * @author Dan Sommerfield 2/22/96 Combined BagSet/InstList/CtrInstList
 * into one class.
 * @author Robert Allen 1/27/95 Modify project() after spreading work to
 * Instance & Schema.
 * @author Richard Long 7/29/93 Initial revision (.h, .c)
 */
public class InstanceList implements Cloneable{
    
    /** The maximum number of attributes allowable in an Instance.**/
    private static int maxAttrVals;
    /** The maximum number of labels allowed for an Instance.**/
    private static int maxLabelVals;
    /** Indicator of whether the Instances in this InstanceList are weighted.**/
    private boolean weighted;
    /** The total weight of all Instances in this InstanceList.**/
    private double totalWeight;
    /** Counts of each classification label found in the Instances stored in
     * this object. **/
    private BagCounters bagCounters;
    /** Schema for the data stored in the file from which data in this object
     * is produced. **/
    private FileSchema fileSchema;
    /** Schema of attriubtes that will actually be used in computation. **/
    private Schema schema;
    /** Indicator for removing all Instances for which there are unknown values
     * on attributes.**/
    private static boolean removeUnknownInstances;
    /** Indicator that Instances that have no weight should be removed from the
     * InstanceList.**/
    private static boolean removeZeroWeights;
    /** The rate at which Instances should have attribute values replaced
     * with unknown values.**/
    private static double corruptToUnknownRate;
    /** The seed for random placement of unknown values.**/
    private static int unknownSeed;
    /** The random number generator used for the placement of unknown values.**/
    private static Random mrandomForUnknowns;
    /** Indicator that this InstanceList has been initialized with Instances**/
    private static boolean initialized;
    /** TRUE if the MineSet program is being used.**/
    private static boolean mineset = false;
    /** The maximum number of warnings that will be logged for unknown labels.**/
    private static int MAX_UNKNOWN_LABEL_WARNING = 10;
    /** The maximum number of warnings that will be logged for Instances with
     * negative weight values.**/
    private static int MAX_NEG_WEIGHT_WARNINGS = 10;
    
    /** The list of Instances.**/
    private LinkedList instances; //list of Instance references
    
    /** LogOptions object containing information for logging purposes.
     */    
    public static LogOptions logOptions = new LogOptions();
    
    /** Constructor.
     * @param file The root name of the file to be loaded into the InstanceList.
     */
    public InstanceList(String file) {
        instances = new LinkedList();
        weighted = false;
        totalWeight = 0;
        bagCounters = null;
        init_max_vals();
        String namesFile = new String(file + Globals.DEFAULT_NAMES_EXT);
        fileSchema = new FileSchema(namesFile);
        schema = fileSchema.create_schema();  //SchemaRC->Schema
        
        fileSchema.display();
        String dataName = file + Globals.DEFAULT_DATA_EXT;
        read_data(dataName,false);
    }
    
    /** Constructor. InstanceList(String, String, String) takes complexity of
     * InstanceList.read_names() + complexity of InstanceList.read_data().
     * @param file The root name of the file to be loaded into the InstanceList.
     * @param namesExtension The file extension for the schema file.
     * @param dataExtension The file extension for the data file.
     */
    public InstanceList( String file,
    String namesExtension,
    String dataExtension) {
        instances = new LinkedList();
        weighted = false;
        totalWeight = 0;
        bagCounters = null;
        init_max_vals();
        String namesFile = new String(file + namesExtension);
        fileSchema = new FileSchema(namesFile);
        schema = fileSchema.create_schema();
        String dataName = file + dataExtension;
        read_data(dataName, false);
    }
    
    /** Constructor.
     * @param catSchema The schema of categories for these data sets.
     * @param file The root name of the file to be loaded into the InstanceList.
     * @param namesExtension The file extension for the schema file.
     * @param testExtension The file extension for the test file.
     */
    public InstanceList(Schema catSchema,
    String file,
    String namesExtension,
    String testExtension) {
        instances = new LinkedList();
        totalWeight = 0;
        try{
            schema = new Schema(catSchema);
        }catch(CloneNotSupportedException e){
            Error.err("InstanceList:constructor(Schema)):clone not"
            +" supported exception caught");}
        fileSchema = null;
        weighted = false;
        bagCounters = null;
        
        init_max_vals();
        String namesFile = file + namesExtension;
        fileSchema = new FileSchema(namesFile);
        read_data(file + testExtension, true);
    }
    
    /** Constructor.
     * @param catSchema The schema of categories for these data sets.
     */
    public InstanceList(Schema catSchema) {
        instances = new LinkedList();
        totalWeight = 0;
        try{
            schema = new Schema(catSchema);
        }catch(CloneNotSupportedException e){
            Error.err("InstanceList:constructor(Schema)):clone not"
            +" supported exception caught");}
        fileSchema = null;
        weighted = false;
        bagCounters = null;
        
        init_max_vals();
    }
    
    /** Constructor.
     * @param catSchema The schema of categories for these data sets.
     * @param names The schema of attributes for these data sets.
     * @param testName The file name for the test file.
     */
    public InstanceList(Schema catSchema,
    FileSchema names,
    String testName) {
        instances = new LinkedList();
        weighted = false;
        totalWeight = 0;
        bagCounters = null;
        init_max_vals();
        fileSchema = new FileSchema(names);
        try{
            schema = new Schema(catSchema);
        }catch(CloneNotSupportedException e){
            Error.err("InstanceList:copyConstructor:clone not"
            +" supported exception caught");}
        read_data(testName, true);
    }
    
    /** Constructor.
     * @param source The InstanceList that is being copied.
     */
    public InstanceList(InstanceList source) {
        boolean preserveCounters = false;
        
        instances = new LinkedList();
        totalWeight = 0;
        try{
            schema = new Schema(source.schema);
        }catch(CloneNotSupportedException e){
            Error.err("InstanceList:copyConstructor:clone not"
            +" supported exception caught");}
        fileSchema = null;
        weighted = source.weighted;
        bagCounters = null;
        
        //weight is accumulated into totalWeight as instances are added.
        init_max_vals();
        ListIterator pix = source.instance_list().listIterator();
        Instance inst = null;
        while(pix.hasNext()) {
            inst = (Instance)pix.next();
            add_instance(inst);
        }
        //If we have a fileSchema, copy it.
        if(source.fileSchema != null)
            fileSchema = new FileSchema(source.fileSchema);
        
        //if we have counters and we want to preserve them, the copy.
        if(source.has_counters() && preserveCounters)
            bagCounters = new BagCounters(source.counters() );
        
        //DBG(OK());
    }
    
    /** Copy constructor.
     * @param source The InstanceList object to be copied.
     * @param preserveCounters TRUE if counters of values should be copied, FALSE otherwise.
     */    
    public InstanceList(InstanceList source, boolean preserveCounters) {
        instances = new LinkedList();
        totalWeight = 0 ; // will get set when instances are added
        try{
            schema = new Schema(source.schema);
        }catch(CloneNotSupportedException e){
            Error.err("InstanceList:copyConstructor:clone not"
            +" supported exception caught");}
        fileSchema = null;
        weighted = source.weighted;
        bagCounters = null;
        
        
        // weight is accumulated into totalWeight as instances are
        // added.
        init_max_vals();
        
        for (ListIterator pix = source.instance_list().listIterator();
        pix.hasNext();)
            add_instance((Instance)pix.next());
        
        // If we have a fileSchema, copy it.
        if(source.fileSchema != null)
            fileSchema = new FileSchema(source.fileSchema);
        
        // If we have counters and we want to preserve them, then copy.
        if(source.has_counters() && preserveCounters)
            bagCounters = new BagCounters(source.counters());
        
        //   if(Globals.DBG(OK();)
    }
    
    /** Build an instance list which is designed to be a test list for some
     * other training set.  The training set must have been built with a
     * FileSchema which will now be used to interpret the test data.
     * @param trainList The training InstanceList that will be used to identify Schema for test data set.
     * @param testName The name of the file containing the test data set.
     */
    public InstanceList(InstanceList trainList,String testName) {
        instances = new LinkedList();
        totalWeight = 0;
        try{
            schema = new Schema(trainList.get_schema());
        }catch(CloneNotSupportedException e){e.printStackTrace();System.exit(1);}
        fileSchema = new FileSchema(trainList.get_original_schema());
        weighted = false;
        bagCounters = null;
        init_max_vals();
        read_data(testName, true);
    }
    
    
    
    /** Checks if this InstanceList has a set of bagcounters yet.
     * @return False if BagCounters is set to null, True otherwise.
     */
    public boolean has_counters() {
        return bagCounters != null;
    }
    
    /** Creates and fills bagCounters.
     * @return The BagCounters object created.
     */
    public BagCounters counters() {
        ensure_counters();
        return bagCounters;
    }
    
    /** Fills bagCounters by adding all instances into it.
     */
    public void ensure_counters() {
        if(bagCounters == null) {
            //Construct counters by adding each instance in turn
            bagCounters = new BagCounters(get_schema());
            if(!no_instances()) {
                //	    ListIterator pix = instances.listIterator();
                Instance inst = null;
                //	    for(;pix.hasNext();inst = (Instance)pix.next())
                for(ListIterator pix = instances.listIterator();
                pix.hasNext();){
                    inst =(Instance)pix.next();
                    bagCounters.add_instance(inst);
                }
            }
        }
    }
    
    /** Reads the data from the supplied file. InstanceList.read_data() takes
     * time proportional to the number of instances * the complexity of
     * read_data_line() + complexity of free_instances().
     * @param file		The name of the file containing the data set.
     * @param isTest	Indicator of whether this is a test data set. True
     * indicates this is a test data set, False otherwise.
     */
    public void read_data(String file, boolean isTest) {
        GetEnv getenv = new GetEnv();
        
        removeUnknownInstances = getenv.get_option_bool("REMOVE_UNKOWN_INST");
        corruptToUnknownRate = getenv.get_option_real_range("CORRUPT_UNKOWN_RATE");
        
        remove_all_instances();
        if(bagCounters!=null)
            bagCounters = null;
        try{
            BufferedReader dataFile = new BufferedReader(new FileReader(file));
            
            /*SECTION ADDED BY JL*/
            StreamTokenizer dataStream = new StreamTokenizer(dataFile);
            dataStream.eolIsSignificant(true);
            dataStream.commentChar((int)'|');
            dataStream.ordinaryChar((int)'?');
            dataStream.ordinaryChar((int)',');
            dataStream.ordinaryChar((int)'.');
            dataStream.wordChars((int)'_',(int)'_');
            dataStream.wordChars((int)' ',(int)' ');
            //	dataStream.parseNumbers();
            if(fileSchema.attrInfos[0] instanceof RealAttrInfo)
            {parseNumbers(dataStream,true);}
            else {parseNumbers(dataStream,false);}
            /*END OF SECTION ADDED BY JL*/
            
            InstanceList thisList = this;
            InstanceReader reader = new InstanceReader(thisList, maxAttrVals, isTest);
            
            fileSchema.skip_white_comments_same_line(dataFile);
            
            
            try{
                /*SECTION ADDED BY JL*/
                while(dataStream.nextToken() != StreamTokenizer.TT_EOF){
                    if(dataStream.ttype != StreamTokenizer.TT_EOL){
                        read_data_line(dataStream, isTest, reader);
                        if(num_instances() % 100 == 0)
                            ; //GLOBLOG(1,'.',flush);
                    }
                }
                /*END OF SECTION ADDED BY JL*/
                
/*REPLACES THIS SECTION
         while(dataFile.ready()){
            read_data_line(dataFile, isTest, reader);
            if(num_instances() % 100 == 0)
              ; //GLOBLOG(1,'.',flush);
         }
/*END OF SECTION REPLACED*/
                //done reading; release the list
                reader.release_list();
                
                if(!removeUnknownInstances)
                    ;//GLOBLOG(1," done.");
                else{
                    int num = num_instances();
                    //GLOBLOG(1,' '); //show we finished reading
                    remove_inst_with_unknown_attr();
                    int newNum = num_instances();
                    if(newNum < num)
                        ;//GLOBLOG(1,"Removed " + num-newNum +" instances.");
                    else
                        ;//GLOBLOG(1,"done.");
                }
                
                if(no_instances())
                    System.out.println("InstanceList.read_data WARNING: no"
                    + " instances in file");
                
                unknownSeed = -1;
                mrandomForUnknowns = null;
                if(corruptToUnknownRate > 0){
                    if(unknownSeed == -1) { //get seed first time
                        unknownSeed = getenv.get_option_int("UNKOWN_RATE_SEED");
                        mrandomForUnknowns = new Random(unknownSeed);
                    }
                    corrupt_values_to_unknown(corruptToUnknownRate, mrandomForUnknowns);
                }
                
                //remove any nominals which have no values other than unknowns here
                try{
                    remove_unknown_attributes();  //causes problems!
                }catch(CloneNotSupportedException e){
                    Error.err("Clone not supported exception caught");}
                
                
                //apply the loss matrix (from the FileSchema) now
                fileSchema.apply_loss_spec(schema);
                
                //some comments about next two lines
                Schema newSchema = schema;             //SchemaRC -> Schema
                try{
                    set_schema(newSchema);
                }catch(CloneNotSupportedException e){
                    Error.err("Clone not supported exception caught");}
            }catch(IOException e){Error.err("InstanceList.read_data"
            +" ERROR");}
        }catch(FileNotFoundException e){Error.err("-"
        +" Data file NOT found");}
    }
    
    /** Removes all instances that have unknown attributes from the data set.
     */
    
    //change for C45
    //   private void remove_inst_with_unknown_attr()
    public void remove_inst_with_unknown_attr() {
        ListIterator pix = instances.listIterator(0);
        while(pix.hasNext()) {
            boolean hasUnknownAttr = false;
            Instance instance = (Instance)pix.next();
            for(int attrNum=0;attrNum<num_attr() && !hasUnknownAttr;attrNum++) {
                AttrInfo attrInfo = attr_info(attrNum);
                AttrValue attrValue = instance.get_value(attrNum);
                if(attrInfo.is_unknown(attrValue))
                    hasUnknownAttr = true;
            }
            if(hasUnknownAttr)
                remove_instance(pix,instance);  //removes from list last element seen by next()
        }
    }
    
    /** Removes the specified Instance from the ListIterator of Instances
     * supplied.
     * @param pix		The ListIterator containing the Instance.
     * @param instance 	The Instance to be removed.
     */
    public void remove_instance(ListIterator pix,Instance instance) {
        if(instance==null)
            Error.err("InstanceList.remove_instance: tried "
            +"to dereference a null instance -->fatal_error");
        pix.remove();//instance_list().del(instance);
        //Remove from counters if we have them
        if(bagCounters!=null)
            bagCounters.del_instance(instance);
        
        //Update totalWeight cache
        totalWeight = instance.get_weight() -1 ;
        
    }
    
    /** Removes all Instance objects stored in this InstanceList object.
     */    
    public void remove_all_instances() {
        //drop_counters();
        MLJ.ASSERT(instances != null,"InstanceList.remove_all_instances: instance is null");
        while(!no_instances())
            instances.removeFirst();
        totalWeight = 0;
    }
    
    /** Returns the number of instances in the InstanceList.
     * InstanceList.num_instances() takes time proportional to the number of
     * instances in the List.
     * @return An integer value of the number of Instances contained in this list.
     */
    public int num_instances() {
        return instances.size();
    }
    
    /** Returns the number of categories that the instances in the List can have.
     * Only works if the Label is of a nominal attribute.
     * @return An integer value of the number of categories.
     */
    public int num_categories() {
        return nominal_label_info().num_values();
    }
    
    /** Returns the nominal label information contained in this InstanceList's
     * schema.
     * @return The information on the nominal labels contained in the schema.
     */
    public NominalAttrInfo nominal_label_info() {
        return label_info().cast_to_nominal();
    }
    
    /** Returns the label information contained in this InstanceList's
     * schema.
     * @return The information on the labels contained in the schema.
     */
    public AttrInfo label_info() {
        return get_schema().label_info();
    }
    
    /** Checks if this InstanceList contains Instances.
     * @return Returns True if there are no Instances in this InstanceList, False
     * otherwise.
     */
    public boolean no_instances() {
        return instances.size() == 0;
    }
    
    /** This function projects out any attributes which have only unknown values.
     *
     * @throws CloneNotSupportedException If InstanceList.project_in_place encounters an exception during cloning of the
     * Schema.
     */    
    private void remove_unknown_attributes() throws CloneNotSupportedException {
        boolean[] attrMask = new boolean[num_attr()];
        for(int i=0;i<attrMask.length;i++)attrMask[i] = true;
        for(int i=0;i<num_attr();i++)
            if(schema.attr_info(i).can_cast_to_nominal() &&
            schema.nominal_attr_info(i).num_values() == 0)
                attrMask[i] = false;
        project_in_place(attrMask);
    }
    
    /** Returns the list of Instances stored in this InstanceList.
     * @return A LinkedList containing the Instances sotred in this InstanceList.
     */
    public LinkedList instance_list() {
        return instances;
    }
    
    /** This function is very similar to project(), except that the list is
     * projected "in place"--attributes are removed directly from the list
     * and the schema is updated.
     * @param projMask An array of boolean values representing which attributes shall be use in this
     * InstanceList object. Values of projMask are related by order to the atributes.
     * Values of TRUE indicate that attribute will be used, FALSE indicates the
     * attribute will not be used.
     * @throws CloneNotSupportedException if the cloning process in Schema encounters an exception.
     */
    public void project_in_place(boolean[] projMask) throws CloneNotSupportedException {
        MLJ.ASSERT(schema != null,"InstanceList.project_in_place: schema is null");
        Schema newSchema = new Schema(schema.project(projMask));
        
        //Project all instances in the list "in place" --we cheat a bit
        // here because we have instances in the list with different
        // schemas.  However, we clean everything up at the end and check
        // the schemas carefully.
        
        int numInstBefore = num_instances();
        //ListIterator temp = instances.listIterator(0);
        int index = 0;
        for(int i=0;i<numInstBefore;i++) {
            //Work ona  temporary pix; otherwise we'll remove an instance
            //  before advancing the pix which is bad.
            
            //obs	 //ListIterator temp = pix;
            //obs	 //pix.next();
            
            //Change instance, add to end.  Add instance by hand so we can avoid
            // checking the schema (whch we know won't match at this point)
            //Don't touch the weight, because we're effectively adding
            // and removing the same instance here.
            
            Instance temp = (Instance)instance_list().get(index); //temp.next();
            Instance instance = temp.project(newSchema, projMask);
            MLJ.ASSERT(instance.get_weight() == temp.get_weight(),"InstanceList.project_in_place: ");
            MLJ.ASSERT(bagCounters == null,"InstanceList.project_in_place: bagCounters not equal to null.");
            instance_list().set(index++, instance);
            
            
            //obs	 Instance instance = (Instance)instance_list().get(index++); //temp.next();
            //obs	 instance.project(newSchema, projMask);
            //obs	 //MLJ.ASSERT(instance.get_weight == instance.get_weight()),"InstanceList.project_in_place: ");
            //obs	 MLJ.ASSERT(bagCounters == null,"InstanceList.project_in_place: bagCounters not equal to null.");
            //obs	 int place = instance_list().indexOf(instance);
            //obs         instance_list().set(place, instance);
            //obs         //instance_list().add(instance);
            //obs	 //instance_list().remove(instance);
        }
        
        //We should not have changed the number of instance
        if(numInstBefore != num_instances())
            Error.err("Assert Error, num instances changes");
        
        //swap schemas
        schema = null;
        schema = newSchema;
        if (Globals.DBG) newSchema = null;
        
        //DBG(OK())
    }
    
    /** Clones the supplied Schema and sets this InstanceList object to use it.
     * @param schemaRC The Schema object to be cloned into this InstanceList object.
     * @throws CloneNotSupportedException if the cloning process of the Schema object encounters an error.
     */    
    public void set_schema(Schema schemaRC) throws CloneNotSupportedException {
        //If we already have the schema set, save time.
        //This happens when normalizing assigns a get_unique_copy()
        //schema
        
        //This formerly recursive call has been collapsed here because
        //it used 'this' overloading, not sure how to do this in Java
        
        for(int i=0;i<instances.size();i++){
            Instance inst = (Instance)instances.get(i);
            
            if(schema == null || (schemaRC != schema) ||
            (inst.get_schema() != schema )) {
                if(schemaRC != schema) {
                    schema = null;
                    schema = new Schema(schemaRC);
                }
                
            }
            //DBG(OK());
        }
    }
    
    /** Change the value of an attribute value in each instance to unknown with the
     * given probability.
     * @param rate A number ranging from 0..1 indicating the probability of changing a specific
     * Instance's value to unknown.
     * @param mrandom Random number generator for randomly determining if Instance value should be
     * changed.
     */    
    private void corrupt_values_to_unknown(double rate, Random mrandom) {
        //mlc.clamp_to_range(rate, 0, 1,"InstanceList.corrupt_values_to_unknown"
        //   +": rate outside [0,1]");
        
        InstanceList tempInstList = new InstanceList(this);
        remove_all_instances();
        
        ListIterator pix = instance_list().listIterator();
        Instance instance = null;
        for(;pix.hasNext()==true;instance = (Instance)pix.next()) {
            for(int i=0;i<num_attr();i++)
                if(rate > mrandom.nextDouble()) //not equal for zero to work
                    attr_info(i).set_unknown(instance.get_value(i));
            add_instance(instance);
        }
    }
    
    /** Returns the information about a specific attribute stored in this
     * InstanceList.
     * @return An AttrInfo containing the information about the attribute.
     * @param attrNum	The number of the attribute about which information is
     * requested.
     */
    public AttrInfo attr_info(int attrNum){return get_schema().attr_info(attrNum);}
    
    /** Returns the number of attributes in the InstanceList.
     * @return An integer representing the number of attributes used for each
     * instance in this InstanceList.
     */
    public int num_attr(){return get_schema().num_attr();}
    
    /** Returns the maximum number of attributes that can be used for Instances
     * in this InstanceList.
     * @return The maximum number of attributes.
     */
    public static int get_max_attr_vals(){return maxAttrVals;}
    
    /** Returns the maximum number of labes that can be used to categorize
     * Instances in this InstanceList.
     * @return The maximum number of labels.
     */
    public static int get_max_label_vals(){return maxLabelVals;}
    
    /** Sets the maximum number of attributes for Instances in this InstanceList.
     * @param maxVals	The maximum number of attributes allowed for Instances
     * in this InstanceList.
     */
    public static void set_max_attr_vals(int maxVals){maxAttrVals = maxVals;}
    
    /** Sets the maximum number of labesl for Instances to be categorized as in
     * this InstanceList.
     * @param maxVals	The maximum number of labels that instances may be
     * categorized as.
     */
    public static void set_max_label_vals(int maxVals){maxLabelVals = maxVals;}
    
    /** Returns the Schema for this InstanceList.
     * @return The Schema of Instances in this InstanceList.
     */
    public Schema get_schema() {
        if(schema == null)
            Error.err("InstanceList.get_schema: schema "
            +"has not been set --> fatal_error");
        return schema;
    }
    
    /** Returns the FileSchema loaded into this InstanceList.
     * @return The FileSchema for this InstanceList.
     */
    public FileSchema get_original_schema() {
        if(fileSchema==null)
            Error.err("InstanceList.get_original_schema: there"
            +" is no FileSchema associated with this list-->fatal_error");
        return fileSchema;
    }
    
    /** InstanceList.read_data_line() takes time proportional to the number
     * of characters in the portion of the file that it reads + the total number
     * of possible attribute values for the _Instance.
     * @param dataFile BufferedReader that reads the file containing data.
     * @param isTest TRUE if this file contains testing data.
     * @param reader InstanceReader for reading Instances from the file.
     */
    
    private void read_data_line(BufferedReader dataFile, boolean isTest,InstanceReader reader) {
        
        //isTest;
        fileSchema.skip_white_comments_same_line(dataFile);
        try{
            MLJ.ASSERT(fileSchema != null,"InstanceList.read_data_line: fileSchema is null");
            for(int i=0;i<fileSchema.num_attr();i++){
                reader.set_from_file(i,dataFile);
                dataFile.mark(1);
                char c = (char)dataFile.read();
                dataFile.reset();
                if(c == ',')
                    dataFile.read();
            }
            
            dataFile.mark(1);
            char c = (char)dataFile.read();
            dataFile.reset();
            if(c == '.'){
                dataFile.read();
                if(fileSchema.skip_white_comments_same_line(dataFile))
                    Error.err("InstanceList.read_data_line: Illegal"
                    +" file format, Only comments or whitespace may follow a '.' "
                    +" on a line in data file");
            }
            else if (c != '\n' && c != '|')
                Error.err("InstanceList.read_data_line: Illegal"
                +" file format, Only comments or whitespace or a '.' may "
                +" follow the last value on a line in data file");
            else
                fileSchema.skip_white_comments_same_line(dataFile);
            reader.add_instance();
        }catch(IOException e){Error.err("InstanceList."
        +"read_data_line: can't read file");}
    }
    
    /** InstanceList.read_data_line() takes time proportional to the number
     * of characters in the portion of the file that it reads + the total number
     * of possible attribute values for the _Instance.
     * @param dataStream StreamTokenizer that reads data from the file.
     * @param isTest TRUE if this file contains testing data.
     * @param reader InstanceReader for reading Instances from the file.
     */    
    private void read_data_line(StreamTokenizer dataStream, boolean isTest,InstanceReader reader) {
        int h;
        //isTest;
        //      fileSchema.skip_white_comments_same_line(dataFile);
        try{
            MLJ.ASSERT(fileSchema != null,"InstanceList.read_data_line: fileSchema is null");
            for(int i=0;i<fileSchema.num_attr();i++){
                h = i+1;
                if (h == fileSchema.num_attr()) h = 0;
                if(fileSchema.attrInfos[h] instanceof RealAttrInfo)
                {parseNumbers(dataStream, true);}
                else {parseNumbers(dataStream, false);}
                reader.set_from_file(i,dataStream);
                dataStream.nextToken();
                if((char)dataStream.ttype == ',')
                    dataStream.nextToken();
                //         dataFile.mark(1);
                //         char c = (char)dataFile.read();
                //         dataFile.reset();
                //         if(c == ',')
                //            dataFile.read();
            }
            //      dataFile.mark(1);
            //      char c = (char)dataFile.read();
            //      dataFile.reset();
            //      if(c == '.'){
            //         dataFile.read();
            if((char)dataStream.ttype == '.')
                for( ;dataStream.nextToken() != StreamTokenizer.TT_EOL; )
                    if (dataStream.ttype != StreamTokenizer.TT_EOL)
                        Error.err("InstanceList.read_data_line: Illegal"
                        +" file format, Only comments or whitespace may follow a '.' "
                        +" on a line in data file");
            //         if(fileSchema.skip_white_comments_same_line(dataFile))
            //            Error.err("InstanceList.read_data_line: Illegal"
            //	       +" file format, Only comments or whitespace may follow a '.' "
            //	       +" on a line in data file");
            //      }
            //      else if (c != '\n' && c != '|')
                    else if(dataStream.ttype != StreamTokenizer.TT_EOL)
                        Error.err("InstanceList.read_data_line: Illegal"
                        +" file format, Only comments or whitespace or a '.' may "
                        +" follow the last value on a line in data file");
            //      else
            //         fileSchema.skip_white_comments_same_line(dataFile);
            reader.add_instance();
        }catch(IOException e){Error.err("InstanceList."
        +"read_data_line: can't read file");}
    }
    
    /** Sets the maximum values for attributes and labels in this InstanceList
     * according to the MLJ options stored in the MLJ-options file. These options
     * are MAX_ATTR_VALS and MAX_LABEL_VALS.
     */
    public void init_max_vals() {
        GetEnv getenv = new GetEnv();
        
        initialized = false;
        if(initialized) return;
        
        initialized = true;
        
        //changed the signature of these functions - possible adverse effects
        InstanceList.set_max_attr_vals(getenv.get_option_int("MAX_ATTR_VALS"));
        InstanceList.set_max_label_vals(getenv.get_option_int("MAX_LABEL_VALS"));
        
    }
    
    /** Displays the Instances stored in this InstanceList.
     * InstanceList.display() takes time proportional to the
     * number of instances * the number of attributes per instance.
     *
     * @param normalizeReal	TRUE if the Instances should be normalized according
     * to the min/max stored for real attributes. If
     * min equals max, values are normalized to .5.
     */
    public void display(boolean normalizeReal) {
        
        ListIterator pix = instances.listIterator(0);
        while(pix.hasNext()) {
            Instance inst = (Instance)pix.next();
            inst.display(is_weighted(), normalizeReal);
        }
        if(no_instances())
            System.out.println("InstanceList.display: No instances");
    }
    
    /** Checks if Instances stored in this InstanceList are weighted.
     * @return TRUE if the Instances are weighted, FALSE otherwise.
     */
    public boolean is_weighted() {
        return weighted;
    }
    
    /** Adds a new instance to the list, using the structures maintained by
     * InstanceReader.  Properly updates both schemas so that automatic instance
     * removal will work.
     * @param vals The values of the instance to be added.
     * @param labelVal The label value of the instance to be added.
     * @param weight The weight of the instance to be added.
     * @param allowUnknownLabels TRUE if unknown label values are allowed for the instance to be added.
     * @return A new Instance object containing the supplied information.
     */
    public Instance reader_add_instance(AttrValue[] vals, AttrValue labelVal, double weight, boolean allowUnknownLabels) {
        if(schema.num_attr() != vals.length)
            Error.err("InstanceList.reader_add_instance: "
            +"schema has "+schema.num_attr()+" attributes, while supplied "
            +"array has "+vals.length+" -->fatal_error");
        
        Instance newInst = new Instance(schema);
        for(int i=0;i<vals.length;i++)
            newInst.values[i] = vals[i];
        if(labelVal!=null)
            newInst.set_label(labelVal);
        
        //set the weight. if the weight is near zero, don't add the instance.
        //if the weight is negative, don't add the instance but warn as well.
        int numNegWeights = 0;
        
        if(MLJ.approx_equal(weight,0))
            weight = 0; //silent clamp
        else if(weight < 0) {
            newInst.set_weight(0.0);
            if(numNegWeights++ < MAX_NEG_WEIGHT_WARNINGS)
                System.out.println("Instance has a negative weight and will "
                +"be ignored");
            if(numNegWeights == MAX_NEG_WEIGHT_WARNINGS)
                System.out.println("There have been max amount of warning on "
                +"negative weights.  Further warnings will be suppressed");
            return newInst;
        }
        newInst.set_weight(weight);
        
        //Do not add the instance id the weight is near zero and
        //REMOVE_ZERO_WEIGHTS is set.  Note that negative weight instances
        //will never be added.
        //can use operator == because near zero weights were clamped above.
        if(weight == 0.0 && removeZeroWeights)
            return newInst;
        
        //simply add and return the instance if unlabelled.
        if(labelVal == null) {
            add_instance(newInst);
            return newInst;
        }
        
        //abort if too many label values
        if(schema.nominal_label_info().num_values() > get_max_label_vals()){
            if(mineset)
                System.out.println("MINESET clause encountered in InstanceList");
            else
                System.out.println("InstanceList.reader_add_instance: the"
                +" selected label '"+schema.label_info().name() +"' has more "
                +"than the current limit of "+get_max_label_vals() +" label "
                +"values.  It is highly recommeded that you do not use an "
                +"attribute with many label values, but you may increase "
                +"the paramter MAX_LAVEL_VALS to allow this operation "
                +"-->fatal_error");
        }
        
        //don't add(but warn) if label is unknown
        int numUnknownLabels = 0;
        if(schema.label_info().is_unknown(labelVal) && !allowUnknownLabels) {
            if(numUnknownLabels++ < MAX_UNKNOWN_LABEL_WARNING) {
                System.out.println("Warning: instance has an unknown label"
                +" value and will be ignored!");
                if(numUnknownLabels == MAX_UNKNOWN_LABEL_WARNING)
                    System.out.println("There have been max amount of label "
                    +"warnings on unknown labels, further warnings will be "
                    +"suppressed ");
            }
        }else
            add_instance(newInst);
        return newInst;
    }
    
    /** Adds the specified Instance to this InstanceList.
     * @return A ListIterator of all Instances in this InstanceList.
     * @param instance	The Instance to bo added.
     */
    public ListIterator add_instance(Instance instance) {
        
        //causes fatal_error if not equal
        //if (Globals.DBG) MLJ.ASSERT((schema != null)&&(schema.equal(instance.get_schema(), true)),"InstanceList.add_instance: schema not equal to instance.schema");
        
        //update totalWeight cache
        double wt = instance.get_weight();
        if(wt != 1.0)
            weighted = true;
        totalWeight += wt;
        
        //InstanceRC inst(instance);
        
        //set the instance's shema to match the schema for the list.
        //we're just making sure that the two schemas (which are equal)
        //are also equal in memory, so this operation is logically const
        //Failure to perform this step will cause the OK() function
        //to occasionally fail for this list.
        //instance.set_schema(schema);  //inst.set_schema(schema);
        
        //update the counters if we have them
        if(bagCounters != null)
            bagCounters.add_instance(instance);  //(inst);
        
        
        try{
            instances.add(instance.clone());
        }catch(CloneNotSupportedException e){
            Error.err("InstanceList.add_instance:CloneNotSupportedException caught");}
        //instances.add(instance);
        ListIterator pix = instances.listIterator(0);
      /*while(pix.hasNext())
      {
         Instance inst = (Instance)pix.next();
         inst.display(false,false);
      }*/
        return pix;
    }
    
    /** Updates the list by removing specified attributes. This is similar to
     * the project() call, except that it is designed to be used WHILE READING.
     * The size of the projMask may be larger than the number of attributes in
     * the schema. This is to allow InstanceReader to maintain a single copy of
     * the projMask even as the schema shrinks.
     * @param projMask A boolean array with the same number of values as there are
     * attributes. Each boolean element coresponds to an attribute
     * In the order they were input. True values represent
     * attributes that are used.
     */
    public void update_for_overflows(boolean[] projMask) {
        //determine if projection is needed
        boolean projNeeded = false;
        for(int i=0;i<schema.num_attr();i++)
            if(projMask[i] == false) {
                projNeeded = true;
                break;
            }
        
        //only act if attributes need projection
        if(projNeeded) {
            //build a projMask of the correct size
            //Projection happens rarely so this is not a big hit
            boolean[] truncProjMask = new boolean[schema.num_attr()];
            for(int i=0;i<schema.num_attr();i++)
                truncProjMask[i] = projMask[i];
            try{
                project_in_place(truncProjMask);
            }catch(CloneNotSupportedException e){
                Error.err("InstanceList.update_for_overflows:"
                +" clone not supported exception was caught");  }
        }
    }
    
    /** Returns the tiebreaking distribution order stored in the CatDist object
     * for this InstanceList.
     * @return The tiebreaking order.
     */
    public int[] get_distribution_order() {
        return CatDist.tiebreaking_order(counters().label_counts());
    }
    
    /** Returns the sum of the weights of all Instances in the InstanceList.
     * This value is cached for faster access.
     * @return The sum of weights for all Instances stored in this InstanceList.
     */
    public double total_weight(){return total_weight(false);}
    
    /** Returns the sum of the weights of all Instances in the InstanceList.
     * This value is cached for faster access, but can be recalculated to
     * avoid the numerical instabilities involved in weight updates.
     * @return The sum of weights for all Instances stored in this InstanceList.
     * @param recalculate	TRUE if the sum should be recalculated, FALSE if
     * the cached value should be used.
     */
    public double total_weight(boolean recalculate) {
        //Compute total_weight on the fly and compare to
        //the cached value.  This is a very slow test.
        //DBGSLOW(OK());
        
        if(recalculate){
            double newTotalWeight = 0;
            ListIterator pix = instances.listIterator();
            Instance inst = null;
            for(;pix.hasNext();inst = (Instance)pix.next())
                newTotalWeight += get_weight(inst);
            totalWeight = newTotalWeight;
        }
        return totalWeight;
    }
    
    /** Returns the weight for the specified Instance.
     * @return The weight for the Instance supplied.
     * @param instance	The Instance for which weight is questioned.
     */
    public double get_weight(Instance instance) {
        double wt = instance.get_weight();
        if (Globals.DBG) MLJ.ASSERT((weighted) || (wt == 1.0),"InstanceList.get_weight: InstanceList is not weighted");
        return wt;
    }
    
    /** Deletes the counters stored for Instances in this InstanceList.
     */
    public void drop_counters() {
        if(bagCounters != null) {
            InstanceList thisNC = this;//(InstanceList)this;
            //      delete thisNC->bagCounters;
            thisNC.bagCounters = null;
            //      thisNC->bagCounters = NULL;
        }
    }
    
    /** Normalize all weights by the number of instances in the list.
     * After this operation, totalWeight should equal the number of instances.
     * The normalization factor is 1 and zeros are allowed for Instance weights.
     */
    public void normalize_weights() {
        normalize_weights(1.0,true);
    }
    
    /** Normalize all weights by the number of instances in the list, times
     * an optional normalization factor. After this operation, totalWeight
     * should equal the number of instances * the normalization factor. Zeros are
     * allowed for Instance weights.
     * @param normFactor	The normalization factor.
     */
    public void normalize_weights(double normFactor) {
        normalize_weights(normFactor,true);
    }
    
    /** Normalize all weights by the number of instances in the list, times
     * an optional normalization factor. After this operation, totalWeight
     * should equal the number of instances * the normalization factor.
     * @param normFactor	The normalization factor.
     * @param allowZeros TRUE if zeros are allowed for Instance weights. If FALSE,
     * Instance weights that are approximately equal 0, the weight
     * is automatically reset to a lower bound.
     */
    public void normalize_weights(double normFactor,
    boolean allowZeros) {
        // drop counters when calling this--you get too many precision
        // errors otherwise.
        drop_counters();
        
        // Set the weighted flag here.  When we call get_weight, we check
        // if weighted is set whenever we find a nonzero weight.
        weighted = true;
        double newTotalWeight = 0;
        
        double r = normFactor * num_instances() / totalWeight;
        double lbound = MLJ.storedRealEpsilon * 2; //mlc.stored_clamping_epsilon()*2;
        
        // We can use Instance.set_weight() here because we'll be resetting
        // the weights at the end anyway.
        for(ListIterator li = instances.listIterator();
        li.hasNext(); ) {
            Instance p = (Instance)li.next();
            //   for(ILPix p(this); p; ++p) {
            double newWeight = get_weight(p) * r;
            if (MLJ.approx_equal(newWeight,0.0)
            && !allowZeros)
                newWeight = lbound;
            p.set_weight(newWeight);	//instance_list()(p).set_weight(newWeight);
            newTotalWeight += get_weight(p);
        }
        
        // float is used for comparison with coarser granularity.
        //@@ The OK() check here as well as the weight checks are disabled
        //@@ because of precision problems introduced in this function.
        //@@DBGSLOW(OK());
        //@@if (!mlc.approx_equal((float) totalWeight,
        //@@		 (float) (num_instances() * normFactor)))
        //@@   err << "InstanceList.normalize_weights: total weight, "
        //@@	  << (float) totalWeight << ", is not near number of instances "
        //@@	 "times normalization factor, "
        //@@	  << (float) (num_instances() * normFactor) << fatal_error;
        
        // Reset total weight here
        totalWeight = newTotalWeight;
        //   DBG(OK());
    }
    
    //PtrArray<RealAndLabelColumn*>*
    /** Splits the InstanceList into several RealAndLabelColumn structures for the
     * parallel discretization.
     *
     * @param mask Boolean array of the same length as the number of attributes. TRUE values
     * indicate that attribute should have a RealAndLabelColumn object created for it,
     * FALSE otherwise.
     * @return An array of RealAndLabelColumns generated from the attribute values for the
     * Instances stored in this InstanceList.
     */    
    public RealAndLabelColumn[] transpose(boolean[] mask) {
        // @@ it is inefficient to get the number of instances up front since we
        //   have to traverse the whole instance list, but allocating the array as
        //   a single piece will allow efficiency in splitting later.
        int numAttr = schema.num_attr();
        //obs   PtrArray<RealAndLabelColumn*>* columns =
        //obs      new PtrArray<RealAndLabelColumn*>(numAttr);
        RealAndLabelColumn[] columns = new RealAndLabelColumn[numAttr];
        
        // Select only continuous attributes
        int numInst = num_instances();
        int numColumns = 0;
        for (int k = 0; k < numAttr; k++){
            AttrInfo ai = schema.attr_info(k);
            if (ai.can_cast_to_real() && mask[k]) {
                if (schema.is_labelled())
                    columns[k] =
                    new RealAndLabelColumn(numInst,
                    schema.num_label_values(),
                    ai.cast_to_real(),
                    nominal_label_info());
                else
                    columns[k] =
                    new RealAndLabelColumn(numInst, ai.cast_to_real());
                numColumns++;
            }
        }
        // Store each column (attribute, label and weight) in a linear array
        if (schema.is_labelled()) {
            NominalAttrInfo labelInfo = schema.label_info().cast_to_nominal();
            for (ListIterator pix = instances.listIterator(); pix.hasNext();) {
                Instance instance =(Instance)pix.next();
                AttrValue labVal = instance.get_label();
                MLJ.ASSERT(!labelInfo.is_unknown(labVal),"InstanceList.transpose: label is unknown");
                int lab = labelInfo.get_nominal_val(labVal) + 1;//plus 1 added to offset schema -JL
                int iLab = lab - Globals.FIRST_CATEGORY_VAL - Globals.FIRST_NOMINAL_VAL;
                double weight = instance.get_weight();
                for(int k = 0; k < numAttr; k++) {
                    // Skip the NULL references (default in the constructor)
                    RealAndLabelColumn column = columns[k];
                    if (column == null) continue;
                    RealAttrInfo attrInfo = column.attr_info();
                    AttrValue attrValue = instance.get_value(k);
                    if (attrInfo.is_unknown(attrValue))
                        column.add_unknown(iLab, weight);
                    else
                        column.add_known(
                        (float)(attrInfo.get_real_val(attrValue)),
                        iLab, weight);
                }
            }
        } else {
            for (ListIterator pix = instances.listIterator(); pix.hasNext();) {
                Instance instance =(Instance)pix.next();
                double weight = instance.get_weight();
                for(int k = 0; k < numAttr; k++) {
                    // Skip the NULL references (default in the constructor)
                    RealAndLabelColumn column = columns[k];
                    if (column == null) continue;
                    RealAttrInfo attrInfo = column.attr_info();
                    AttrValue attrValue = instance.get_value(k);
                    if (attrInfo.is_unknown(attrValue))
                        column.add_unknown(weight);
                    else
                        column.add_known(
                        (float)(attrInfo.get_real_val(attrValue)), weight);
                }
            }
        }
        logOptions.LOG(6, "Instance list transposed into "+numColumns+" columns."
        +'\n');
        return columns;
    }
    
    /** Checks if the total weight of this InstanceList is approximately 0.
     * @return TRUE if the total weight is approximately equal to 0, FALSE
     * otherwise.
     */
    public boolean no_weight(){
        return MLJ.approx_equal((float)totalWeight,0.0);
    }
    
    /** Returns the Category corresponding to the label that occurs most
     * frequently in the InstanceList. In case of a tie, we prefer the
     * given tieBreaker if it is one of those tied. TieBreaker can be
     * UNKNOWN_CATEGORY_VAL if you prefer the earlier category to the
     * tied ones. The method used differs depending on whether or not we have
     * counters on this List.  It is considerably faster if the counters are
     * present. This method is only meaningful for labels with AttrInfo
     * derived from NominalAttrInfo.  This method will cause fatal_error
     * otherwise. In the case of a tie, returns the Category corresponding to
     * the label which occurs first in the NominalAttrInfo.
     * InstanceList.majority_category() takes time proportional to the number
     * of different categories + the number of instances.
     *
     * @return The category that occurs the most in this InstanceList or
     * UNKNOWN_CATEGORY_VAL if there are no instances.
     * @param tieBreakingOrder Array indicating the order in which ties are broken. The array should be the same
     * length as the number of attributes and each element corresponds to an attribute.
     * Lower number elements represent attributes that are more favorable in an tie
     * than higher number elements.
     */
    public int majority_category(int[] tieBreakingOrder) {
        double[] computedCounts = null;
        double[] labelCounts = null;
        if (bagCounters != null)
            labelCounts = bagCounters.label_counts();
        else {
            NominalAttrInfo nai = nominal_label_info();
            // +1 because of Globals.UNKNOWN_CATEGORY_VAL instead of Globals.FIRST_CATEGORY_VAL
            computedCounts =new double[nai.num_values()+1]; // (Globals.UNKNOWN_CATEGORY_VAL, nai.num_values() + 1, 0);
            double[] count = computedCounts;
            
            //      for (ILPix pix(*this); pix; ++pix)
            for(ListIterator pixLI = instance_list().listIterator();pixLI.hasNext();){
                Instance pix = (Instance)pixLI.next();
                count[nai.get_nominal_val(pix.get_label())] += pix.get_weight();
            }
            labelCounts = computedCounts;
        }
        
        int best = CatDist.majority_category(labelCounts, tieBreakingOrder);
        computedCounts = null;
        return best;
    }
    
    /** This function takes an attribute mask which is an array of booleans
     * indicating whether the corresponding attribute should be included in
     * the projection. InstanceList.project() takes
     * O(num attributes * (num instances + num attributes)) time.
     * @param attrMask A boolean array with the same number of values as there are
     * attributes. Each boolean element corresponds to an attribute
     * in the order they were input. True values represent
     * attributes that are used.
     * @return An InstanceList with a new Schema that includes only the attributes
     * with a mask value true. May return null if an exception occures.
     */
    public InstanceList project(boolean[] attrMask) {
        //   DBGSLOW(OK());
        MLJ.ASSERT(attrMask.length == num_attr(),"InstanceList.project: attrMask's length does not match number of attributes");
        try{
            Schema newSchema = get_schema().project(attrMask);
            InstanceList newInstList = new InstanceList(newSchema);
            for(ListIterator pix = instances.listIterator();pix.hasNext();)
                newInstList.add_instance(((Instance)pix.next()).project(newSchema, attrMask));
            return newInstList;
        }catch(CloneNotSupportedException e){e.printStackTrace();}
        return null;
    }
    
    /** Sets the given StreamTokenizer to parse/not parse numbers.
     * @param stream StreamTokenizer tokenizing information.
     * @param ifYes TRUE if numbers should be parsed as double values, FALSE otherwise.
     */    
    private void parseNumbers(StreamTokenizer stream, boolean ifYes) {
        if (ifYes) stream.parseNumbers();
        else {
            stream.ordinaryChars((int)'0', (int)'9');
            stream.wordChars((int)'0', (int)'9');
        }
    }
    
    /** Returns a String representation of this InstanceList object.
     * @param normalizeReal TRUE if real values in an Instance object should be normalized.
     * @return A String representation of this InstanceList object.
     */    
    public String out(boolean normalizeReal) {
        String rtrn = new String();
        ListIterator pix = instances.listIterator(0);
        while(pix.hasNext()) {
            Instance inst = (Instance)pix.next();
            rtrn = rtrn + inst.out(is_weighted(), normalizeReal);
        }
        if(no_instances())
            rtrn = rtrn + "InstanceList.display: No instances";
        return rtrn;
    }
    
    /** Returns a clone of this InstanceList object.
     * @param preserveCounters TRUE if counters of values should be copied, FALSE otherwise.
     * @return A new object with a copy of the data stored in the supplied InstanceList.
     */    
    public Object clone(boolean preserveCounters) {
        return new InstanceList(this,preserveCounters);
    }
    
    
    /** Returns a clone of this InstanceList object. Does not preserve counters.
     * @return A new object with a copy of the data stored in the supplied InstanceList.
     */    
    public Object clone() {
        return new InstanceList(this,false);
    }
    
    /** Checks integrity constraints. We verify that all instances have the
     * same schema at level 0
     * Comments    : Because the schema has attrinfo's that are updated,
     * everyone must share the EXACT representation, not
     * just logical equivalence.  Specifically, if the schema
     * is updated, we want to make sure all instances see
     * the exact same min/max for RealAttrInfo's. Level of
     * checking is automatically set to 0.
     *
     */    
    public void OK() {
        OK(0);
    }
    
    /** Checks integrity constraints. We verify that all instances have the
     * same schema at level 0
     * Comments    : Because the schema has attrinfo's that are updated,
     * everyone must share the EXACT representation, not
     * just logical equivalence.  Specifically, if the schema
     * is updated, we want to make sure all instances see
     * the exact same min/max for RealAttrInfo's
     *
     * @param level Level of checking done.
     */
    public void OK(int level) {
/*   if (level < 1 && schema != null && instances) {
      Schema schemaRep = get_schema().read_rep();
      for (ILPix pix(*this); pix; ++pix)
         if ((*pix).get_schema().read_rep() != schemaRep)
            err << "InstanceList.OK mismatch in schemas for list and "
               " instance.   Instance is:\n" << *pix <<
               " with schema " << (void *)(*pix).get_schema().read_rep() <<
               " = " << (*pix).get_schema() <<
               "\nList schema is: " << (void *)schemaRep <<
               " = " << *schemaRep << fatal_error;
   }
 
   // Check that the counters agree with the actual number of instances
   //   if we have counters
   // WARNING:  we need to use totalWeight here instead of total_weight().
   //   total_weight() calls OK() in DBG level 2, which would lead
   //   to infinite recursion!!!
   if(bagCounters) {
      double num = counters().OK(); // @@ Dan, change num to weight?
      MLJ.verify_approx_equal(StoredReal(num), StoredReal(totalWeight),
                              "InstanceList.OK: "
                              "Counters claim of weight does not match "
                              "list's total weight");
   }
   ASSERT(instances);
 
   // Check for numerical inaccuracy in the totalWeight cache.
   // DO NOT refresh the cache here because we don't want results
   //   to differ in high debug levels!
   // Check that totalWeight is correct (if we're weighted)
   // Passing StoredReals to approx_equal means using coarse granularity.
   if(is_weighted()) {
      Real compTotalWeight = 0;
      for (ILPix pix(*this); pix; ++pix)
         compTotalWeight += get_weight(pix);
      mlc.verify_approx_equal(StoredReal(compTotalWeight),
                              StoredReal(totalWeight),
                              "InstanceList.OK: computed weight "
                              "fails to match totalWeight");
   }
 
   // Check that totalWeight is close to number of instances
   //   (if we're not weighted)
   // Passing StoredReals to approx_equal means using coarse granularity.
   if(!is_weighted()) {
      mlc.verify_approx_equal((StoredReal) num_instances(),
                              (StoredReal) totalWeight,
                              "InstanceList.OK: List is unweighted, "
                              "but total weight fails to match whole "
                              "number of instances");
   }
 
   // Total weight may not be negative
   if (totalWeight < -MLC.stored_real_epsilon())
      err << "InstanceList.OK: total weight (" << totalWeight
          << ") is negative" << fatal_error;
 */
    }
    
    /** Displays the names file associated with the InstanceList.
     * @param stream Writer object to which the names file will be displayed.
     * @param protectChars TRUE if protected characters are used, FALSE otherwise.
     * @param header A String to use for the header to the display.
     */
    public void display_names(Writer stream,
    boolean protectChars,
    String header) {
        get_schema().display_names(stream, protectChars, header);
    }
    
    /** Split the InstanceList according to the labels. Each InstanceList
     * corresponds to all instances having one label value. Works only for nominal
     * labels. If a label value never appears in the training set, the
     * corresponding InstanceList will be empty.
     * @return An array of InstanceList where each InstanceList contains
     * Instances with a particular label value.
     */
    public InstanceList[] split_by_label() {
        NominalAttrInfo instance_label = nominal_label_info(); //name change -JL
        // The + 1 is for UNKNOWN_CATEGORY_VAL
        //obs   InstanceList[] ila = new InstanceListArray(UNKNOWN_CATEGORY_VAL,
        //obs				       instance.num_values() + 1);
        InstanceList[] ila = new InstanceList[instance_label.num_values() + 1];
        //obs   for (int i = ila.low(); i <= ila.high(); i++)
        for (int i = 0; i < ila.length; i++)
            ila[i] = new InstanceList(get_schema());
        
        NominalAttrInfo labelInfo = nominal_label_info();
        //obs   for (ILPix pix(*this); pix; ++pix) {
        //obs      InstanceRC& instance = *pix;
        for (ListIterator pix = instances.listIterator(); pix.hasNext();) {
            Instance instance = (Instance)pix.next();
            ila[labelInfo.get_nominal_val(instance.get_label())].add_instance(instance);
        }
        return ila;
    }
    
    /** Sample_with_replacement takes an independent sample of the instance list,
     * with replacement.  The parameter size is the number of samples to take, which
     * is generally equal to num_instances() for bootstrap. size must be greater
     * than 0, and can be greater than num_instances(). If restOfInstList is
     * non-null, the unused instances are inserted into it. If mrandom is non-null,
     * it is used as the random number generator, otherwise a new one is created,
     * used, and destroyed. The caller gains ownership of the returned list, and
     * should already own restOfInstList, if non-null.
     * @param size The size of the list of samples requested.
     * @param restOfInstList An InstanceList object containing the Instances not sampled.
     * @param mrandom Random number generator for randomly sampling Instances.
     * @return An InstanceList object containing randomly sampled Instances.
     */
    public InstanceList sample_with_replacement(int size, InstanceList restOfInstList,Random mrandom) {
        if (size <= 0)
            Error.fatalErr("InstanceList.sample_with_replacement: size "+size
            +" must not be negative");
        
        int i = 0;
        int numInst = num_instances();
        
        InstSampleElement[] sampleArray = new InstSampleElement[numInst];
        
        // initialize the array with pixes from our list
        for(ListIterator p = instances.listIterator(); p.hasNext(); i++) {
            sampleArray[i].pix = (Instance)p.next();
            sampleArray[i].flag = false;
        }
        
        Random mrand = mrandom!=null ? mrandom : new Random();
        
        // copy random pixes from our list into the sampleArray
        InstanceList sample = new InstanceList(get_schema());
        for(i = 0; i < size; i++) {
            int randomIndex = mrand.nextInt(numInst);
            sample.add_instance(sampleArray[randomIndex].pix);
            sampleArray[randomIndex].flag = true;
        }
        
        if (restOfInstList != null)
            for(i = 0; i < numInst; i++)
                if(sampleArray[i].flag == false)
                    restOfInstList.add_instance(sampleArray[i].pix);
        
        logOptions.LOG(2, "Total samples: "+size);
        if (restOfInstList != null)
            logOptions.LOG(2, "\tUnique Used Samples: "
            +(num_instances() - restOfInstList.num_instances())
            +"\tUnused Samples: " +restOfInstList.num_instances());
        logOptions.LOG(2,"\n");
        logOptions.LOG(3, "Selected sample:"+"\n"+ sample+"\n");
        if (restOfInstList != null)
            logOptions.LOG(3, "Unused sample:"+"\n"+restOfInstList+"\n");
        
        MLJ.ASSERT(sample.num_instances() == size,"InstanceList.sample_with_replacement:sample.num_instances() != size.");
        
        // clean up the array
        for(i = 0; i < numInst; i++) {
            MLJ.ASSERT(sampleArray[i].pix != null,"InstanceList.sample_with_replacement: sampleArray[i].pix == null");
            sampleArray[i].pix = null;
        }
        
        if (mrandom == null)
            mrand = null;
        
        return sample;
    }
    
    /** Returns a reference to an InstanceList that has the same contents as this
     * InstanceList, with a random ordering of the instances. The Random
     * parameter allows for duplication of results.
     * @param mrandom Random number generator used for shuffling.
     * @param index Array of Instances being shuffled.
     * @param keepFileSchema TRUE if the FileSchema for this InstanceList object should be copied to the
     * InstanceList with the shuffled Instances.
     * @return An InstanceList object with a shuffled order of Instances.
     */
    public InstanceList shuffle(Random mrandom,
    Instance[] index,//index was InstanceListIndex typedef -JL
    boolean keepFileSchema) {
        InstanceList instList = independent_sample(num_instances(), mrandom, index);
        
        // copy the FileSchema into the new list, if there is one
        if((fileSchema != null) && keepFileSchema && (instList.fileSchema == null))
            instList.fileSchema = new FileSchema(fileSchema);
        
        return instList;
    }
    
    /** Returns a reference to an InstanceList that has the same contents as this
     * InstanceList, with a random ordering of the instances. The Random
     * parameter allows for duplication of results. The FileSchema is not copied to
     * the new InstanceList object.
     * @param mrandom Random number generator used for shuffling.
     * @param index Array of Instances being shuffled.
     * @return An InstanceList object with a shuffled order of Instances.
     */    
    public InstanceList shuffle(Random mrandom,
    Instance[] index)//index was InstanceListIndex typedef -JL
    {
        return shuffle(mrandom,index,false);
    }
    
     /** Returns a reference to an InstanceList that has the same contents as this
      * InstanceList, with a random ordering of the instances. The Random
      * parameter allows for duplication of results. The FileSchema is not copied to
      * the new InstanceList.
      * @param mrandom Random number generator used for shuffling.
      * @return An InstanceList object with a shuffled order of Instances.
      */    
    public InstanceList shuffle(Random mrandom) {
        return shuffle(mrandom,null,false);
    }
    
    /** Returns a reference to an InstanceList that has the same contents as this
     * InstanceList, with a random ordering of the instances. The Random
     * parameter allows for duplication of results. The FileSchema is not copied to
     * the new InstanceList.
     * @return An InstanceList object with a shuffled order of Instances.
     */    
    public InstanceList shuffle() {
        return shuffle(null,null,false);
    }
    
    
    /** Returns a list with the first "numInSplit" instances removed from this list.
     * If keepFileSchema is TRUE, the FileSchema will be copied into the new list.
     * @param numInSplit The number of Instances to be split from this InstanceList object.
     * @param keepFileSchema TRUE if the FileSchema should be copied to the new InstanceList, FALSE otherwise.
     * @return Returns an InstanceList with the first "numInSplit" Instances.
     */
    public InstanceList split_prefix(int numInSplit,
    boolean keepFileSchema) {
        if (numInSplit < 0 || numInSplit > num_instances())
            Error.fatalErr("InstanceList.split_prefix: Cannot remove "
            +numInSplit +" instances from a list with "
            +num_instances() +" instances" );
        InstanceList instList;
        instList = new InstanceList(get_schema());
        
        for (int i = 0; i < numInSplit; i++) {
            Instance instance = remove_front();
            instList.add_instance(instance);
        }
        
        // copy the FileSchema into the new list, if there is one
        if((fileSchema != null) && keepFileSchema && (instList.fileSchema == null))
            instList.fileSchema = new FileSchema(fileSchema);
        
        MLJ.ASSERT(instList.num_instances() == numInSplit,"InstanceList.split_prefix: instList.num_instances() != numInSplit");
        return instList;
    }
    
    /** Returns a list with the first "numInSplit" instances removed from this list. The
     * FileSchema is not copied to the new InstanceList.
     * @param numInSplit The number of Instances to be split from this InstanceList object.
     * @return Returns an InstanceList with the first "numInSplit" Instances.
     */    
    public InstanceList split_prefix(int numInSplit) {
        return split_prefix(numInSplit,false);
    }
    
    /** Returns an InstanceRC corresponding to the first instance in the list.
     * The instance is deleted from the list.
     * @return The Instance removed from this InstanceList object.
     */
    public Instance remove_front() {
        if (no_instances())
            Error.fatalErr("InstanceList.remove_front: list is empty");
        //obs   ILPix pix(*this);
        //obs   return InstanceList.remove_instance(pix);
        return (Instance)instances.removeFirst();
    }
    
    /** Returns a reference to an InstanceList with "size" instances randomly
     * sampled (without replacement) from this InstanceList. The MRandom
     * parameter allows for duplication of results.
     * @param size The number of Instances requested in the sample.
     * @param mrandom Random number generator for randomly selecting Instances.
     * @param index Array of Instances to be sampled from.
     * @return An InstanceList object containing the randomly sampled Instances.
     */
    public InstanceList independent_sample(int size, Random mrandom,
    Instance[] index)// index is InstanceListIndex typedef -JL
    {
        return independent_sample(size, null,  mrandom, index);
    }
    
    /** Returns a reference to an InstanceList with "size" instances randomly
     * sampled (without replacement) from this InstanceList. The MRandom
     * parameter allows for duplication of results.
     * @param size The number of Instances requested in the sample.
     * @param mrandom Random number generator for randomly selecting Instances.
     * @return An InstanceList object containing the randomly sampled Instances.
     */    
    public InstanceList independent_sample(int size, Random mrandom) {
        return independent_sample(size, null,  mrandom, null);
    }
    
    /** Returns a reference to an InstanceList with "size" instances randomly
     * sampled (without replacement) from this InstanceList.
     * @param size The number of Instances requested in the sample.
     * @return An InstanceList object containing the randomly sampled Instances.
     */    
    public InstanceList independent_sample(int size) {
        return independent_sample(size, null,  null, null);
    }
    
    
    
    /** Returns a reference to an InstanceList with 'size' instances and another
     * reference to an InstanceList with the rest of the instances.
     * @param size The number of Instances requested in the sample.
     * @param restOfInstList The InstanceList containing Instances not contained in the sample.
     * @param mrandom Random number generator for randomly selecting Instances.
     * @param index Array of Instances to be sampled from.
     * @return An InstanceList object containing the randomly sampled Instances.
     */
    public InstanceList independent_sample(int size,
    InstanceList restOfInstList,
    Random mrandom,
    Instance[] index)// index is InstanceListIndex typedef -JL
    {
        
        if(Basics.DBG)
            if (size < 0 || size > num_instances())
                Error.fatalErr("InstanceList.independent_sample: size("+size
                +") must be greater than zero and less than or equal to "
                +num_instances());
        if(Basics.DBG)
            if (index != null && index.length != num_instances())
                Error.fatalErr("InstanceList.independent_sample: The index size("
                +index.length +") was not equal to the InstanceList size("
                +num_instances() +')');
        
        //obs   InstanceListIndex instListIndex =
        Instance[] instListIndex =
        index != null ? index : create_inst_list_index();
        InstanceList instList = new InstanceList(get_schema());
        Random mrand = mrandom == null ? mrandom : new Random(0);
        int maxRandom = instListIndex.length;
        for (int i = 0; i < size; i++) {
            int instNum = mrand.nextInt(maxRandom);
            maxRandom--;
            //obs      // ** is needed.  The first one gives us ILPix, the second gives
            //obs      //   the InstanceRC
            //obs      Instance instance = **instListIndex[instNum];
            Instance instance = instListIndex[instNum];
            instList.add_instance(instance);
            //obs      ILPix* temp = instListIndex[instNum];
            Instance temp = instListIndex[instNum];
            instListIndex[instNum] = instListIndex[maxRandom];
            instListIndex[maxRandom] = temp;
        }
        if (restOfInstList != null) {
            int numInstances = restOfInstList.num_instances();
            if (numInstances != 0)
                Error.fatalErr("InstanceList.independent_sample: restOfInstList is not "
                +"empty but has "+numInstances+" instances.");
            for (int i = 0; i < maxRandom; i++)
                restOfInstList.add_instance(instListIndex[i]);
            MLJ.ASSERT(restOfInstList.num_instances() == instListIndex.length - size,
            "InstanceList.independent_sample: restOfInstList.num_instances() != instListIndex.size() - size");
        }
        MLJ.ASSERT(instList.num_instances() == size,
        "InstanceList.independent_sample: instList.num_instances() != size.");
        
        if (mrandom == null)
            mrand = null;
        if (index == null)
            instListIndex = null;
        return instList;
    }
    
    /** Returns a reference to an InstanceList with 'size' instances and another
     * reference to an InstanceList with the rest of the instances.
     * @param size The number of Instances requested in the sample.
     * @param restOfInstList The InstanceList containing Instances not contained in the sample.
     * @param mrandom Random number generator for randomly selecting Instances.
     * @return An InstanceList object containing the randomly sampled Instances.
     */    
    public InstanceList independent_sample(int size,
    InstanceList restOfInstList,
    Random mrandom) {
        return independent_sample(size,restOfInstList,mrandom,null);
    }
    
    /** Returns a reference to an InstanceList with 'size' instances and another
     * reference to an InstanceList with the rest of the instances.
     * @param size The number of Instances requested in the sample.
     * @param restOfInstList The InstanceList containing Instances not contained in the sample.
     * @return An InstanceList object containing the randomly sampled Instances.
     */    
    public InstanceList independent_sample(int size,
    InstanceList restOfInstList) {
        return independent_sample(size,restOfInstList,null,null);
    }
    
    /** Returns a reference to an array of references to the instances in the list.
     * This is used for independent_sample() and shuffle().
     * @return An array of references to all Instance objects in this object.
     */
    public Instance[] create_inst_list_index() {
        
        //obs   int instListSize = num_instances();
        //obs   Instance[] index = new Instance[instListSize];//InstanceListIndex typedef -JL
        //obs   int i = 0;
        //obs   for (ILPix pix(*this); i < instListSize && pix; ++i, ++pix)
        //obs      index[i] = new ILPix(*this, pix);
        //obs   MLJ.ASSERT(i == instListSize && pix == null,"InstanceList.create_inst_list_index: i != instListSize || pix != null.");
        Instance[] index = new Instance[num_instances()];
        int i = 0;
        for(ListIterator pix = instances.listIterator(); pix.hasNext();++i)
            index[i] = (Instance)pix.next();
        return index;
    }
    
    /** Appends the instances from the given list to this list. Gets ownership
     * of and deletes the given list.
     * @param instList The supplied InstanceList object to be appended to this object.
     */
    public void unite(InstanceList instList) {
        if (instList == this)
            Error.fatalErr("InstanceList::unite: Cannot unite instList with itself");
        while (!instList.no_instances()) {
            Instance instance = instList.remove_front();
            add_instance(instance);
        }
        //obs   delete instList;
        instList = null;
    }
    
    /** Appends the instances from the given list to this list. Gets ownership
     * of and deletes the given list.
     * @return A ListIterator containing the Instance objects stored in this
     * InstanceList.
     */
    public ListIterator listIterator() {
        return instances.listIterator();
    }
    
    //END OF FILE
}

