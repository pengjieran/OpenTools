package arithmetic.shared;
import java.io.BufferedReader;
import java.io.StreamTokenizer;

/** Provide a set of functions for reading a list of instances from a source which
 * provides a single instance at a time, attribute by attribute. Supports the
 * exclusion of nominal attributes which have more than a set limit on the number
 * of values.
 * @author James Louis Java Implmentation.
 * @author Dan Sommerfield 5/03/96 Initial revision (.h, .c)
 *
 */
public class InstanceReader {
    /** The InstanceList in which are stored Instances that are read.
     */
    private InstanceList instList;
    /** TRUE if unknown values for attributes are possible, FALSE otherwise.
     */
    private boolean makeUnknowns;
    /** TRUE if unknown labels are possible, FALSE otherwise.
     */
    private boolean allowUnknownLabels;
    /** The FileSchema detailing the data being read by this InstanceReader.
     */
    private FileSchema fileSchema;
    
    /** Values possible for the attributes.
     */
    private AttrValue[] vals;
    private boolean[] setAttr;
    private boolean anySet;
    private int attrValueLimit;
    /** The total weight of Instances.
     */
    private double weight;
    private boolean warnOnSetComplete;
    private int[] assimMap;
    private boolean[] projMap;
    /**
     */
    private boolean[] listProjMap;
    //private QuarkTable[] quarkTables;
    
    /** Special value for mapping operations. Any integer value is valid.
     */
    public static final int unmapped = -1;
    /** Special value for mapping operations. Any integer value is valid.
     */
    public static final int mapToLabel = -2;
    
    /** Constructor. Builds an InstanceReader which can be used to construct instances
     * for ownerList. OwnerList MUST have a FileSchema associated with it; this
     * defines the form of all incoming data. The data will be ASSIMILATED to the form
     * of ownerList's schema as it is read.                                        <BR>
     * The limit parameter specifies an optional limit on the number of distinct
     * attribute values which are allowed on any given attribute.  If this limit is
     * exceeded, the attribute in question will be projected out, and future incoming
     * data for that attribute will be ignored.                                    <BR>
     * The makeUnknown parameter, if TRUE, will cause all attribute values not present
     * in ownerList's schema to be converted to UNKNOWN.                           <BR>
     * NOTE: for reading test data, limit should be set to 0 and makeUnknown should be
     * TRUE.
     * @param ownerList The InstaceList in which Instances will be stored.
     */
    public InstanceReader(InstanceList ownerList){	//ADDED BY JL
        this(ownerList,0,false,false);}
    /** Constructor. Builds an InstanceReader which can be used to construct instances
     * for ownerList. OwnerList MUST have a FileSchema associated with it; this
     * defines the form of all incoming data. The data will be ASSIMILATED to the form
     * of ownerList's schema as it is read.                                        <BR>
     * The limit parameter specifies an optional limit on the number of distinct
     * attribute values which are allowed on any given attribute.  If this limit is
     * exceeded, the attribute in question will be projected out, and future incoming
     * data for that attribute will be ignored.                                    <BR>
     * The makeUnknown parameter, if TRUE, will cause all attribute values not present
     * in ownerList's schema to be converted to UNKNOWN.                           <BR>
     * NOTE: for reading test data, limit should be set to 0 and makeUnknown should be
     * TRUE.
     * @param ownerList The InstaceList in which Instances will be stored.
     * @param limit The limit number of how many attribute values are possible.
     */
    public InstanceReader(InstanceList ownerList, int limit){	//ADDED BY JL
        this(ownerList,limit,false,false);}
    /** Constructor. Builds an InstanceReader which can be used to construct instances
     * for ownerList. OwnerList MUST have a FileSchema associated with it; this
     * defines the form of all incoming data. The data will be ASSIMILATED to the form
     * of ownerList's schema as it is read.                                        <BR>
     * The limit parameter specifies an optional limit on the number of distinct
     * attribute values which are allowed on any given attribute.  If this limit is
     * exceeded, the attribute in question will be projected out, and future incoming
     * data for that attribute will be ignored.                                    <BR>
     * The makeUnknown parameter, if TRUE, will cause all attribute values not present
     * in ownerList's schema to be converted to UNKNOWN.                           <BR>
     * NOTE: for reading test data, limit should be set to 0 and makeUnknown should be
     * TRUE.
     * @param ownerList The InstaceList in which Instances will be stored.
     * @param limit The limit number of how many attribute values are possible.
     * @param makeUnknown TRUE if unknown values for attributes are possible, FALSE otherwise.
     */
    public InstanceReader(InstanceList ownerList, int limit, boolean makeUnknown){	//ADDED BY JL
        this(ownerList,limit,makeUnknown,false);}
    
    /** Constructor. Builds an InstanceReader which can be used to construct instances
     * for ownerList. OwnerList MUST have a FileSchema associated with it; this
     * defines the form of all incoming data. The data will be ASSIMILATED to the form
     * of ownerList's schema as it is read.                                        <BR>
     * The limit parameter specifies an optional limit on the number of distinct
     * attribute values which are allowed on any given attribute.  If this limit is
     * exceeded, the attribute in question will be projected out, and future incoming
     * data for that attribute will be ignored.                                    <BR>
     * The makeUnknown parameter, if TRUE, will cause all attribute values not present
     * in ownerList's schema to be converted to UNKNOWN.                           <BR>
     * NOTE: for reading test data, limit should be set to 0 and makeUnknown should be
     * TRUE.
     * @param ownerList The InstaceList in which Instances will be stored.
     * @param limit The limit number of how many attribute values are possible.
     * @param makeUnknown TRUE if unknown values for attributes are possible, FALSE otherwise.
     * @param allowUnknownLab TRUE if unknown labels are possible, FALSE otherwise.
     */
    public InstanceReader(InstanceList ownerList, int limit, boolean makeUnknown, boolean allowUnknownLab) {
        instList = ownerList;
        makeUnknowns = makeUnknown;
        allowUnknownLabels = allowUnknownLab;
        
        setAttr = new boolean[ownerList.get_original_schema().num_attr()];
        for(int i=0;i<setAttr.length;i++)setAttr[i]=false;
        
        anySet = false;
        attrValueLimit = limit;
        weight = 1.0;
        fileSchema = ownerList.get_original_schema();
        
        vals = new AttrValue[ownerList.get_original_schema().num_attr()];
        for(int j=0;j<vals.length;j++)vals[j]=new AttrValue();///ADDED BY JL
        
        assimMap = new int[ownerList.get_original_schema().num_attr()];
        for(int i=0;i<assimMap.length;i++)assimMap[i]=-1;
        
        projMap = new boolean[ownerList.get_original_schema().num_attr()];
        listProjMap = new boolean[ownerList.get_original_schema().num_attr()];
        //quarkTables(0,ownerList.get_original_schema().num_attr(),null)
        warnOnSetComplete = true;
        
        //fileSchema.OK();
        if(attrValueLimit < 0)
            Error.err("InstanceReader::InstanceReader: negative"
            + " value is not allowed for attrValueLimit->fatal_error");
        
        //construct the assimilation map to be used during set functions
        construct_assim_map();
        
        //take ownership of the list we're building
        ownerList = null;
        //OK();
    }
    
    /** Attempts to match values for two fixed value set nominals. Prints an error
     * message on failure.
     * @param name The name of the attribute.
     * @param a1 The first nominal being compared.
     * @param a2 The second nominal being compared.
     */
    public void match_values(String name, NominalAttrInfo a1, NominalAttrInfo a2) {
        boolean error = false;
        //ASSERT(a1.is_fixed());
        //ASSERT(a2.is_fixed());
        if(a1.num_values() != a2.num_values())
            error = true;
        else{
            //         for(int i = 0;i<a1.num_values();i++) //CHANGED FOR ZOO TESTSET -JL
            for(int i = Globals.FIRST_NOMINAL_VAL; i < a1.num_values();i++)
                if(a1.get_value(i) != a2.get_value(i))
                    error = true;
        }
        if(error){
            Error.err("InstanceReader::match_values: mismatch"
            +" in fixed nominals for attribute \"" + name + "\": ");
            Error.err("taining version: ");
            a1.display_attr_values();
            Error.err("testing version: ");
            a2.display_attr_values();
            Error.err(" -->fatal_error");
        }
    }
    
    /** Constructs the assimilation map used to map attribute numbers used in the
     * assimilation schema (set functions) into numbers used in the list's schema.
     *
     */
    private void construct_assim_map() {
        //mark the label column as mapped to the label
        if(fileSchema.get_label_column() != unmapped)
            assimMap[fileSchema.get_label_column()] = mapToLabel;
        
        //for each attribute name in the file schema (test data), find the
        //same name in the list's schema(training data) and establish the mapping
        //No attributes in the list's schema may be left unaccounted for.
        
        int numDestAttr = get_schema().num_attr();
        boolean[] checklist = new boolean[numDestAttr];
        for(int i=0;i<checklist.length;i++)checklist[i]=false;
        int checkCount = 0;
        for(int i=0;i<fileSchema.num_attr();i++){
            for(int j=0;j<numDestAttr;j++){
                String name = fileSchema.attrInfos[i].name();
                if(name.equals(get_schema().attr_name(j))) {
                    //make sure the column is not mapped to some other column.
                    //if it is mapped to the label or weight, then ignore it.
                    //ASSERT(assimMap[i] != false);
                    if(assimMap[i] == unmapped){
                        assimMap[i] = j;
                        checkCount++;
                        checklist[j] = true;
                        
                        //assimilate attribute infos. Thre are some rules here:
                        // 1. if the types don't match, it is an error.
                        // 2. if both are fixed nominals, the exact values must match
                        // 3. if the list's schema is an unfixed nominal, use it
                        // 4. if the list's schema is a fixed nominl, but the file
                        //    schema specifies an unfixed noinal, create an unfixed
                        //    nominal with the values from the list's schema's
                        //    fixed nominal.
                        AttrInfo testAI = fileSchema.attrInfos[i];
                        AttrInfo trainAI = get_schema().attr_info(j);
                        if(trainAI.can_cast_to_nominal()) {
                            //make sure the nominal types match
                            if(!testAI.can_cast_to_nominal())
                                Error.err("InstanceReader::constuct_"
                                + "assim_map: training schema requires a nominal "
                                + "for attribute \"" +name+"\" -->fatal_error");
                            
                            // other nominal checks
                            NominalAttrInfo testNAI = testAI.cast_to_nominal();
                            NominalAttrInfo trainNAI = trainAI.cast_to_nominal();
                            
                            //check fixed/unfixed status
                            if(trainNAI.is_fixed()) {
                                if(testNAI.is_fixed()) {
                                    //by rule#2, the exact values must match
                                    match_values(name, trainNAI, testNAI);
                                }
                                else{
                                    //replace attribute info for test data with the
                                    //training version, but make unfixed(rule #4)
                                    fileSchema.set_attr_info(i,trainAI);
                                    fileSchema.attrInfos[i].cast_to_nominal().fix_values(false);
                                }
                            }
                            else {
                                //just use the training version (rule #3)
                                fileSchema.set_attr_info(i,trainAI);
                            }
                        }
                        else if(trainAI.can_cast_to_real()) {
                            if(!testAI.can_cast_to_real())
                                Error.err("InstanceReader::construct_"
                                +"assim_map: training schema requires a numerical "
                                +"value for attribute \"" +name+"\" -->fatal_error");
                        }
                        else
                            Error.err("InstanceReader::construct_"
                            +"assim_map: training schema contains an attribute \""
                            +name+"\" which is neither real nor nominal-->fatal_error");
                    }
                }
            }
        }
        
        //if the weight is set and ignoreWeightColumn is specified, the weight
        //column MUST be unmapped
        if(fileSchema.get_weight_column() != unmapped &&
        assimMap[fileSchema.get_weight_column()] != unmapped &&
        fileSchema.get_weight_column() != 0) {
            Error.err("InstanceReader::constuct_assim_map: the "
            +"column "+fileSchema.attrInfos[fileSchema.get_weight_column()].name() + " is mapped "
            +"to both the weight and attribute "
            + assimMap[fileSchema.get_weight_column()] +" yet WEIGHT_IS"
            +"_ATTRIBUTE is falst -->fatal_error");
        }
        
        //if the label exists. replace with the list's label schema
        if(fileSchema.get_label_column() != unmapped){
            if(is_labelled() == false)
                Error.err("InstanceReader::construct_assim_map:"
                +" attempting to assimilate labelled data to unlabelled data "
                +" -->fatal_error");
            fileSchema.set_attr_info(fileSchema.get_label_column(),
            get_schema().label_info());
        }
        
        //make sure all attributes in the destination schema were accounted for.
        //Its ok to leave the lavel or weight unmapped, in this case you can use
        //the set_..._label() and set_weight() functions.
        if(checkCount < checklist.length){
            Error.err("InstanceReader::constuct_assim_map: the"
            +" following required attributes were unaccounted for: ");
            for(int i =0;i<checklist.length;i++){
                if(!checklist[i]){
                    System.out.print("\"" + get_schema().attr_info(i).name() + "\" ");
                }
            }
            Error.err("fatal_error");
        }
    }
    
    /** Releases the list we're building.
     * @return The InstanceList being built by this InstanceReader.
     */
    public InstanceList release_list() {
        //if we're not making extra values into unknowns, warn about
        //projected columns
        if(!makeUnknowns)
            warn_projected_columns();
        //it is an error to release a list when an instance is partially
        //added
        if(anySet)
            Error.err("InstanceReader::release_list: cannot"
            +" release a list with a partially built instance.  Use "
            +"set_complete() to finish off the instance with unknown"
            +" values -->fatal_error");
        
        //release ownership and return
        InstanceList retList = instList;
        instList = null;
        fileSchema = null;
        return retList;
    }
    
    /** Sets the value of an attribute from an MLJ format data file.
     *
     * @param attrNum The number of the attribute being read.
     * @param dataFile The BufferedReader reading the file.
     */
    public void set_from_file(int attrNum, BufferedReader dataFile) {
        //check range on the incoming attrNum
        if(attrNum < 0 || attrNum > fileSchema.num_attr())
            Error.err("InstanceReader::set_from_file: "
            +"attribute number "+attrNum+" is out of range-->fatal_error");
        
        //map attribute number, but keep original for later calls
        int mapNum = assimMap[attrNum];
        
        //read the attribute value from the file
        AttrValue attrVal = fileSchema.attrInfos[attrNum].read_attr_value(dataFile, makeUnknowns, fileSchema);
        if(mapNum==unmapped){
            if(fileSchema.get_ignore_weight_column() &&
            attrNum==fileSchema.get_weight_column()){
                //set the weight
                double val = fileSchema.attrInfos[attrNum].get_real_val(attrVal);
                weight = val;
            }
            return; //ignore unmapped attributes from here on
        }
        
        //determine type of attribute, and call the appropriate function instead.
        AttrInfo ai = fileSchema.attrInfos[attrNum];
        
        if(ai.is_unknown(attrVal)){
            set_unknown(attrNum);
        }
        else if(ai.can_cast_to_real()){
            
            double val = ai.get_real_val(attrVal);
            set_real(attrNum, val);
        }
        else if(ai.can_cast_to_nominal()){
            String strVal = ai.attrValue_to_string(attrVal);
            set_nominal(attrNum, strVal);
        }
        else
            Error.err("InstanceReader::set_from_file: reader "
            +"only supports real and nominal types -->fatal_error");
    }
    
    /** Sets the value of an attribute from an MLJ format data file.
     *
     * @param attrNum The number of the attribute being read.
     * @param dataFile The StreamTokenizer reading from the file.
     */
    public void set_from_file(int attrNum, StreamTokenizer dataFile) {
        //check range on the incoming attrNum
        if(attrNum < 0 || attrNum > fileSchema.num_attr())
            Error.err("InstanceReader::set_from_file: "
            +"attribute number "+attrNum+" is out of range-->fatal_error");
        
        //map attribute number, but keep original for later calls
        int mapNum = assimMap[attrNum];
        
        //read the attribute value from the file
        AttrValue attrVal = fileSchema.attrInfos[attrNum].read_attr_value(dataFile, makeUnknowns, fileSchema);
        if(mapNum==unmapped){
            if(fileSchema.get_ignore_weight_column() &&
            attrNum==fileSchema.get_weight_column()){
                //set the weight
                double val = fileSchema.attrInfos[attrNum].get_real_val(attrVal);
                weight = val;
            }
            return; //ignore unmapped attributes from here on
        }
        
        //determine type of attribute, and call the appropriate function instead.
        AttrInfo ai = fileSchema.attrInfos[attrNum];
        
        if(ai.is_unknown(attrVal)){
            set_unknown(attrNum);
        }
        else if(ai.can_cast_to_real()){
            
            double val = ai.get_real_val(attrVal);
            set_real(attrNum, val);
        }
        else if(ai.can_cast_to_nominal()){
            String strVal = ai.attrValue_to_string(attrVal);
            set_nominal(attrNum, strVal);
        }
        else
            Error.err("InstanceReader::set_from_file: reader "
            +"only supports real and nominal types -->fatal_error");
    }
    
    /** Adds the instance to the list. The instance must be fully constructed and must
     * have its label set. Also, you may not add the same instance twice.
     *
     * @return The Instance being added.
     */
    public Instance add_instance() {
        for(int i=0;i<setAttr.length;i++)
            if(assimMap[i]!=unmapped && !setAttr[i])
                Error.err("InstanceReader::add_instance: "
                +"you forgot to set attribute "+i+" ("
                +fileSchema.attrInfos[i].name()+")\n Use the set_complete() "
                +"to give unknown values to extra attributes -->fatal_Error");
        
        //set up small array of values for list
        AttrValue labelVal = null;
        AttrValue[] listVals = new AttrValue[get_schema().num_attr()];
        for(int i=0;i<assimMap.length;i++) {
            int mapNum = assimMap[i];
            if(mapNum>=0)
                listVals[mapNum] = vals[i];
            else if(mapNum == mapToLabel){
                //ASSERT(labelVal == null);
                labelVal = vals[i];
            }
        }
        
        //reset status bits for the next add
        //can't reset the weight before its used, so we have to
        //reset it independently for each branch of the if below.
        
        for(int i=0;i<setAttr.length;i++)
            setAttr[i] = false;
        anySet = false;
        
        if(is_labelled()){
            //ASSERT(labelVal);
            Instance inst =
            instList.reader_add_instance(listVals,labelVal,weight,
            allowUnknownLabels);
            if(attrValueLimit!=0)
                update_for_overflows();
            weight = 1.0;
            return inst;
        }
        else {
            //ASSERT(labelVal==null)
            Instance inst =
            instList.reader_add_instance(listVals,null,weight,false);
            if(attrValueLimit!=0)
                update_for_overflows();
            weight = 1.0;
            return inst;
        }
    }
    
    private void build_proj_maps(boolean[] readerProjMap, boolean[] listProjMap) {
        //ASSERT(readerProjMap.lenght == assimMap.length);
        Schema schema = get_schema();
        int numAttr = schema.num_attr();
        //ASSERT(listProjMap.length >= numAttr);
        
        //Both projection maps begin as all true;
        for(int i=0;i<readerProjMap.length;i++)
            readerProjMap[i] = true;
        for(int i=0;i<numAttr;i++)
            listProjMap[i] = true;
        
        //Run through the attributes in the names file. Check the number of
        //values for each nominal. If anything exceeds the limit, add it to the
        //map.  Also add attributes which are being deliberately ignored.
        for(int i=0;i<fileSchema.num_attr();i++){
            int numVals = 0;
            if(assimMap[i] >= 0){
                if(fileSchema.attrInfos[i].can_cast_to_nominal()) {
                    numVals = fileSchema.attrInfos[i].cast_to_nominal().num_values();
                    
                    //check the number of values here against the counterpart attr
                    // info in the list.  If these don't match, we have failed
                    //to update our attr infos correctly
                    int listNumVals = schema.attr_info(assimMap[i]).cast_to_nominal().num_values();
                    if(numVals != listNumVals)
                        Error.err("InstanceReader::build_proj_maps:"
                        +" number of values for attribute" + schema.attr_name(i)
                        +" is inconsistent; reader's FileSchema has "+numVals
                        +" while list has "+listNumVals+" -->fatal_error");
                    
                    //if we've exceeded the number of values, set entries in
                    // each map to false;
                    if(numVals > attrValueLimit) {
                        readerProjMap[i] = false;
                        listProjMap[assimMap[i]] = false;
                    }
                    
                    //if the attribute is marked to be ignored, set to false
                    if(fileSchema.attrInfos[i].cast_to_nominal().get_ignore()) {
                        readerProjMap[i] = false;
                        listProjMap[assimMap[i]] = false;
                    }
                }
            }
        }
    }
    
    private void update_assim_map(boolean[] projMap) {
        int displacement = 0;
        
        //ASSERT(projMap.length == asimMap.length);
        
        //the following algorithm is based on identifying "newly removed"
        //attributes.  A newly removed attribute is one which has a valid
        //(i.e. >= 0) entry in the assim map, but a false in the projMap.
        //For each newly removed attribute, set it to unmapped in the
        //assim map and increment displacement.
        //Also, throughout the loop decrement the assimilation values of
        //any valid attribtes by displacement.  This will correct for
        //the newly removed attributes.
        for(int i=0; i< projMap.length;i++) {
            if(assimMap[i] >= 0) {
                if(projMap[i] == false) {
                    //newly removed
                    displacement++;
                    assimMap[i] = unmapped;
                }
                else {
                    //decrement by displacement to correct for other removed attrs
                    assimMap[i] -= displacement;
                    //ASSERT(assimMap[i] >= 0);
                }
            }
        }
    }
    
    private void update_for_overflows() {
        build_proj_maps(projMap, listProjMap);
        instList.update_for_overflows(listProjMap);
        update_assim_map(projMap);
    }
    
    private int warn_projected_columns() {
        //LOG(5, "Checking schema againts names-file schema);
        //ASSERT(fileSchema);
        
        int attrNum = 0;
        int origAttrNum = 0;
        int numIgnored = 0;
        
        Schema schema = get_schema();
        while(origAttrNum <  fileSchema.num_attr()) {
            if(attrNum < schema.num_attr())
                ;//LOG(5, "Schema: attr "+attrNum+" ("
            //+ schema.attr_name(attrNum) +").");
            else
                ;//LOG(5, "Schema: no more attrs.");
            
            //LOG(5, "Original: attr " + origAttrNum +" ("
            //   + fileSchema.attrInfos[origAttrNum].name() + ").");
            
            //If the left out column is a weight or label, don't do anything
            //Only leave out the weight if ignoreWeightColumn is set.
            if(fileSchema.get_label_column() == origAttrNum ||
            (fileSchema.get_weight_column() == origAttrNum &&
            fileSchema.get_ignore_weight_column()))
                ; //ignore this case
            
            //Check names.  If they match, we're ok and we increment
            // out index.  If not, print a warning
            else if(attrNum < schema.num_attr() &&
            schema.attr_name(attrNum) ==
            fileSchema.attrInfos[origAttrNum].name() )
                attrNum++;
            else{
                //Only nominals should get left out.  If it's not nominal,
                // then it's an error
                AttrInfo ai = fileSchema.attrInfos[origAttrNum];
                if(!ai.can_cast_to_nominal())
                    Error.err("InstanceList::diff_original"
                    +"_schema: non-nominal attribute "+ai.name()+" was "
                    +"projected out -->fatal_error");
                NominalAttrInfo nai = ai.cast_to_nominal();
                if(nai.num_values() >= attrValueLimit)
                    System.out.println("Warning-->attribute "+ai.name()
                    +" ignored: exceeded "+attrValueLimit+ " values.");
                else if(nai.num_values() == 0)
                    System.out.println("Warning-->attribute "+ai.name()
                    +" this attribtue has no values except unknowns.");
                else if(nai.get_ignore())
                    System.out.println("Warning-->attribute "+ai.name()
                    +"ignored: ignore flag set.");
                else
                    Error.err("InstanceReader::warn_on...:"
                    +"attribute "+ai.name() +" was poject out for an "
                    +"invalid reason -->fatal_error");
                
                numIgnored++;
            }
            
            origAttrNum++;
        }
        
        if(numIgnored > 0)
            ;//LOG(1, "\nWarning: some attributes are ignored.  The default"
        //+" limit of +"attrValueLimit+" may be increased\n"
        //+" by changing the MAX_ATTR_VALS paramter");
        
        //if no attributes remain, post a warning
        if(schema.num_attr()==0)
            Error.err("InstanceReader::warn_projected_columns"
            +": all attributes have been ignored!  This example is probably"
            +" no useful for learnign -->fatal_error");
        
        //Make sure we've accounted for all attributes in this schema
        //ASSERT(origAttrNum == fileSchema.num_attr());
        //ASSERT(attrNum == schema.num_attr());
        return numIgnored;
        
    }
    
    /** Checks if the Instance being read is labelled.
     * @return TRUE if the Instance is labelled, FALSE otherwise.
     */
    public boolean is_labelled(){return get_schema().is_labelled();}
    
    private void prepare_to_set(int attrNum, boolean isNominal, boolean isReal) {
        //check range on the incoming attrNum
        if(attrNum < 0 || attrNum > fileSchema.num_attr())
            Error.err("InstanceReader::prepare_to_set: "
            +"attribute number "+attrNum+" is out of range -->fatal_error");
        
        if(isNominal && isReal)
            Error.err("InstanceReader::prepare_to_set: "
            +"isNominal and isReal may not both be set -->fatal_error");
        
        if(isNominal && !fileSchema.attrInfos[attrNum].can_cast_to_nominal())
            Error.err("InstanceReader::prepare_to_set: "
            +"attempted to call a nominal setting function on a non-nominal"
            +" attribute -->fatal_error");
        
        if(isReal && !fileSchema.attrInfos[attrNum].can_cast_to_real())
            Error.err("InstanceReader::prepare_to_set: "
            +"attempted to call a real setting function on a non-real"
            +" attribute -->fatal_error");
        
        if(setAttr[attrNum])
            Error.err("InstanceReader::prepare_to_set: "
            +"attribute "+attrNum+" was already set -->fatal_Error");
        
        setAttr[attrNum] = true;
        anySet = true;
    }
    
    /** Returns the Schema being used to read data.
     * @return The Schema containing details about the file beinig read.
     */
    public Schema get_schema() {
        check_has_list();
        return instList.get_schema();
    }
    
    private void check_has_list() {
        if(!has_list())
            Error.err("InstanceReader::check_has_list: "
            +"this reader has had its list released -->fatal_error");
    }
    
    /** Checks if this InstanceReader has an InstanceList to store Instances in.
     * @return TRUE if there is an InstanceList present, FALSE otherwise.
     */
    public boolean has_list() {
        return instList != null;
    }
    
    /** Explicitly sets a nominal value. The attribute's type must support nominal.
     *
     * @param attrNum The number of the attribute containing the nominal value.
     * @param attrVal The value to be set as a nominal value.
     */
    public void set_nominal(int attrNum, String attrVal) {
        prepare_to_set(attrNum, true, false);
        //map attribute number if needed. If it doesn't map, return
        int mapNum = assimMap[attrNum];
        
        //map to label if requested. Do nothing if unmapped.  You cannot
        //map a nominal to the weight so abort if this is requested.
        if(mapNum==unmapped) {
            return;
        }
        else if(mapNum==mapToLabel){
            //ASSERT(fileSchema.get_label_column() != unmapped);
            if(vals[attrNum]==null)vals[attrNum]=new AttrValue();
            get_schema().label_info().cast_to_nominal().set_nominal_string(vals[attrNum], attrVal,makeUnknowns);
            
        }
        else {
            //set the value in vals from the list's attribute info.  If
            //makeUnknowns is set, we need to check if the value exists.
            //if not, don't add it but rather set an unknown value.
            if(vals[attrNum]==null)vals[attrNum]=new AttrValue();
            get_schema().attr_info(mapNum).cast_to_nominal().set_nominal_string(vals[attrNum],attrVal, makeUnknowns);
            
        }
        
        //force an update on the attrInfo in fileSchema if the nominal
        //belongs to a non-fixed attribute.
        //Only do this if makeUnknowns is not set
        if(!makeUnknowns &&
        !fileSchema.attrInfos[attrNum].cast_to_nominal().is_fixed()){
            AttrValue dummyAttrVal = new AttrValue();
            fileSchema.attrInfos[attrNum].set_nominal_string(dummyAttrVal, attrVal,false);
        }
        
    }
    
    /** Explicitly sets a real value. The attribute's type must support real.
     *
     * @param attrNum The number of the attribute containing the real value.
     * @param attrVal The value to be set as a real value.
     */
    public void set_real(int attrNum, double attrVal) {
        //System.out.println("Gets to InstanceReader::set_real--0");//DEBUG BY JL
        prepare_to_set(attrNum, false, true);
        //if this column is mapped to the weight, set the weight.
        if(attrNum==fileSchema.get_weight_column())
            weight = attrVal;
        //System.out.println("Gets to InstanceReader::set_real--1");//DEBUG BY JL
        
        //map attribute number if needed,  if it doesn't map, then just return
        int mapNum = assimMap[attrNum];
        //System.out.println("Gets to InstanceReader::set_real--2");//DEBUG BY JL
        if (vals == null)System.out.println("InstanceReader::set_real--vals == null");//DEBUG BY JL
        if (vals[attrNum] == null)System.out.println("InstanceReader::set_real--vals["+attrNum+"] == null");//DEBUG BY JL
        
        if(mapNum == unmapped)
            return;
        else if(mapNum==mapToLabel)
            //label is a nominal column--should have failed in check above
            Error.err("ABORT_IF_REACHED");
        else
            get_schema().attr_info(mapNum).cast_to_real().set_real_val(vals[attrNum], attrVal);
        //System.out.println("Gets to InstanceReader::set_real--3");//DEBUG BY JL
        
    }
    
    /** Sets an attribute's value to unknown. Works on any type.
     *
     * @param attrNum The number of the attribute for which the unknown value will be inserted.
     */
    public void set_unknown(int attrNum) {
        prepare_to_set(attrNum,false,false);
        if(attrNum == fileSchema.get_weight_column()){
            Error.err("Mcerr:WARNING:setting an unknown weight value ("
            + fileSchema.attrInfos[attrNum].name()+").");
            weight = 0.0;
        }
        
        //map attribute number if needed.  If it doesn't map, then just return
        int mapNum = assimMap[attrNum];
        
        if(mapNum == unmapped)
            return;
        else if(mapNum == mapToLabel)
            //a later process will remove this instance
            get_schema().label_info().set_unknown(vals[attrNum]);
        else
            get_schema().attr_info(mapNum).set_unknown(vals[attrNum]);
    }
    
}
