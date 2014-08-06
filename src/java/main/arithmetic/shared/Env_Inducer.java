package arithmetic.shared;
import arithmetic.id3.ID3Inducer;
/*
 * Env_Inducer.java
 *
 * Created on January 23, 2002, 12:26 PM
 */

/** The Env_Inducer class provides functions for creating Inducer objects of any
 * specific type.
 * @author James Louis Ported to Java.
 */
public class Env_Inducer {

    /** GetEnv object used to access global variables.
     */    
    static GetEnv getEnv = new GetEnv();
    
    


    /** Help string for the INDUCER_TYPE option.
     */    
    static private String INDUCER_TYPE_HELP = "Use this option to select the type of "+
    "inducer to use.  Refer to inducer-specific documentation for more "+
    "details on what each of these inducers do.";
    
    /** List of Inducer names.
     */    
    static String[] InducerName  = {"null","ID3","HOODG","const","table-majority",
    "table-no-majority","IB","c4.5","c4.5-no-pruning","c4.5-rules","naive-bayes",
    "performance-estimator","oneR","disc-filter","EODG","lazyDT","bagging",
    "pebls","aha-ib","perceptron","winnow","disc-naive-bayes","oc1","CatDT",
    "cont-filter","list-hoodg","cn2","COODG","ListODG","T2","stacking","MC4",
    "SGIDT","nn-am","ripper","cart","c5.0","option-dt","booster","nbtree","ODT",
    "ddt","ddtC"};
    
    /** List of Inducer types associated with names.
     */    
    static String[] InducerType  = {"null","id3","hoodg","constInducer","tablemaj",
    "tablenomaj","ib","c45prune","c45nopruning","c45Rules","naiveBayes",
    "perfEst","oneR","dfInducer","eodg","lazyDT","baggingInd",
    "peblsInd","ahaIB","ptronInd","winnowInd","discNaiveBayes","oc1","catDT",
    "cfInducer","listHOODG","cn2","COODG","ListODG","t2Ind","stackingInd","mC4",
    "sgidt","nnAM","Ripper","cart","c50","optionDT","boosterInd","nbtree","ODT",
    "ddt","ddtC"};
    
    
    /** Creates a new instance of Env_Inducer. Should not be accessable. */
    public Env_Inducer() {
    }

    /** Returns an Inducer object of an unspecified type.
     * @return An Inducer object of an unspecified type.
     */    
    static public BaseInducer env_inducer() {
        return env_inducer(Globals.EMPTY_STRING);
    }
    
    /** Returns the name associated with the specified type.
     * @param inducerType Prefix for Inducer options.
     * @return The name of the Inducer type.
     */    
    static public String name_from_value(String inducerType) {
        int i;
        for(i = 0; i < InducerType.length && !InducerType[i].equals(inducerType); i++);
        if (i == InducerType.length) return "";
        else return InducerName[i];
    }
    
    /** Creates a new instance of Env_Inducer
     * @param prefix Prefix for Inducer options. Should correspond to the Inducer name.
     * @return An Inducer of the type corresponding to the name specified.
     */
    static public BaseInducer env_inducer(String prefix) {
        BaseInducer baseInd;
       
        String envVarType = prefix + "INDUCER";
        String envVarName = prefix + "INDUCER_NAME";
        
        String inducerType = getEnv.get_option_string(envVarType);
//        getEnv.get_option_enum_no_default(envVarType, envInducerEnum, INDUCER_TYPE_HELP, false, inducerType);
        String defaultInducerName = name_from_value(inducerType);
//        String inducerName = getEnv.get_option_string(envVarName, defaultInducerName, Globals.EMPTY_STRING, true);
        String inducerName = getEnv.get_option_string(envVarName,defaultInducerName,Globals.EMPTY_STRING,true);

        if (inducerType.equals("id3")){
                ID3Inducer inducer = new ID3Inducer(inducerName);
                inducer.set_user_options(prefix + "ID3_");
                return inducer;
            }
//            else if(inducerType.equals("naive-bayes")){
//                NaiveBayesInd inducer = new NaiveBayesInd(inducerName);
//                inducer.set_user_options(prefix + "NB_");
//                return inducer;
//            }
/*        switch (inducerType) {
            case "null":
                // create a null inducer that never aborts
                return new NullInducer(inducerName, false);
            case "MC4": {
                ID3Inducer inducer = new ID3Inducer(inducerName);
                inducer.set_c45_options();
                inducer.set_user_options(prefix + "MC4_");
                return inducer;
            }
            case "SGIDT": {
                SGIDTInducer inducer = new SGIDTInducer(inducerName);
                inducer.set_user_options(prefix + "SGIDT_");
                return inducer;
            }
            case "hoodg": {
                HOODGInducer inducer = new HOODGInducer(inducerName);
                BaseInducer inner = inducer;
                return make_disc_filter(prefix, inner);
            }
            case "constInducer": {
                ConstInducer inducer = new ConstInducer(inducerName);
                return inducer;
            }
            case "tablemaj": {
                TableInducer inducer = new TableInducer(inducerName, true);
                return inducer;
            }
            case "table-no-majority": {
                TableInducer inducer = new TableInducer(inducerName, false);
                return inducer;
            }
            case "IB": {
                IBInducer inducer = new IBInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "c4.5": {
                String c45Flags = get_option_string(prefix + "C45_FLAGS", C45Inducer.defaultPgmFlags);
                C45Inducer inducer = new C45Inducer(inducerName, c45Flags, true);
                return inducer;
            }
            case "c4.5-rules": {
                String c45Flags1 = get_option_string(prefix + "C45R_FLAGS1", C45RInducer.defaultPgmFlags1);
                String c45Flags2 = get_option_string(prefix + "C45R_FLAGS2", C45RInducer.defaultPgmFlags2);
                C45RInducer inducer = new C45RInducer(inducerName, c45Flags1, c45Flags2);
                return inducer;
            }
            case "c4.5-no-pruning": {
                String c45Flags = get_option_string(prefix + "C45_FLAGS", C45Inducer.defaultPgmFlags);
                C45Inducer inducer = new C45Inducer(inducerName, c45Flags, false);
                return inducer;
            }
            case "performance-estimator": {
                PerfEstInducer inducer = new PerfEstInducer(prefix, inducerName);
                return inducer;
            }
            case "oneR": {
                int minInstPerLabel = get_option_int(prefix + "MIN_INST", 6, "Small parameter", true);
                OneRInducer inducer = new OneRInducer(inducerName, minInstPerLabel);
                return inducer;
            }
            case "eodg": {
                EntropyODGInducer inducer = new EntropyODGInducer(inducerName);
                inducer.set_user_options(prefix + "ODG_");
                return inducer;
            }
            case "disc-filter": {
                DiscFilterInducer inducer = new DiscFilterInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "lazyDT": {
                LazyDTInducer inducer = new LazyDTInducer(inducerName);
                inducer.set_user_options(prefix + "LAZYDT_");
                BaseInducer *inner = inducer;
                return make_disc_filter(prefix, inner);
            }
            case "bagging": {
                BaggingInd inducer = new BaggingInd(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "pebls": {
                PeblsInducer inducer = new PeblsInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "aha-ib": {
                AhaIBInducer inducer = new AhaIBInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "oc1": {
                OC1Inducer inducer = new OC1Inducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "perceptron": {
                PerceptronInducer inducer = new PerceptronInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "winnow": {
                WinnowInducer inducer = new WinnowInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "disc-naive-bayes": {
                NaiveBayesInd inducer = new NaiveBayesInd(inducerName);
                inducer.set_user_options(prefix + "NB_");
                BaseInducer inner = inducer;
                return make_disc_filter(prefix, inner);
            }
            case "CatDT": {
                CatDTInducer inducer = new CatDTInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "cont-filter": {
                ContinFilterInducer inducer =
                new ContinFilterInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "list-hoodg": {
                ListHOODGInducer inducer =
                new ListHOODGInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "cn2": {
                CN2Inducer inducer = new CN2Inducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "COODG": {
                COODGInducer inducer = new COODGInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "ListODG": {
                ListODGInducer inducer = new ListODGInducer(inducerName, NULL);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "T2": {
                String t2Flags = get_option_string(prefix + "T2_FLAGS", T2Inducer.defaultPgmFlags);
                T2Inducer inducer = new T2Inducer(inducerName, t2Flags);
                return inducer;
            }
            case "stacking": {
                StackingInd inducer = new StackingInd(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "nn-am": {
                AMInducer inducer = new AMInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "ripper": {
                String ripperFlags = get_option_string(prefix + "RIPPER_FLAGS", RipperInducer.defaultPgmFlags);
                RipperInducer inducer = new RipperInducer(inducerName, ripperFlags);
                return inducer;
            }
            case "option-dt": {
                SGIDTInducer inducer = new SGIDTInducer(inducerName);
                inducer.set_split_decision(SGIDTInducer.multipleSplit);
                inducer.set_user_options(prefix + "OPTDT_");
                return inducer;
            }
            case "booster": {
                BoosterInducer inducer = new BoosterInducer(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            }
            case "cart": {
                CARTInducer inducer = new CARTInducer(inducerName);
                return inducer;
            }
            case "c5.0": {
                C50Inducer inducer = new C50Inducer(inducerName);
                return inducer;
            }
            case "ODT": {
                ODTInducer inducer = new ODTInducer(inducerName, NULL);
                inducer.set_user_options(prefix + "ODT_");
                return inducer;
            }
            case "ddt": {
                DDTInducer inducer = new DDTInducer(inducerName);
                inducer.set_user_options(prefix + "DDT_");
                inducer.set_default_functors();
                return inducer;
            }
            case "ddtC": {
                DDTInducer inducer = new DDTInducer(inducerName);
                inducer.set_c45_options();
                inducer.set_user_options(prefix + "DDTC_");
                inducer.set_default_functors();
                return inducer;
            }
            case "nbtree": {
                CatDTInducer inducer = new CatDTInducer(inducerName);
                NaiveBayesInd nbInducer = new NaiveBayesInd("inner " + inducerName);
                nbInducer.set_no_matches_factor(0.5);
                Inducer innerInducer = nbInducer;
                PerfEstDispatch perfEstDispatch = new PerfEstDispatch;
                perfEstDispatch.set_perf_estimator(PerfEstDispatch::cv);
                perfEstDispatch.set_cv_folds(5);
                perfEstDispatch.set_cv_times(1);
                DiscDispatch disc = NULL;
                inducer.set_leaf_ind_perf_est_disc(innerInducer, perfEstDispatch, disc);
                inducer.set_improve_ratio(0.05);
                inducer.set_lower_bound_min_split_weight(30.0);
                inducer.set_user_options("NBTREE_");
                return inducer;
            }
*/          else if ((baseInd = search_inducers(prefix, inducerType, inducerName)) != null) //default
                    return baseInd;
                else
                    Error.fatalErr("Env_Inducer.env_inducer:: invalid inducer type "+inducerType);
                
        return null;
    }
    
    /** Creates a discrete filter wrapper around a specified inducer.
     * @param prefix Prefix for Inducer options.
     * @param innerInducer The Inducer this wrapper contains.
     * @return A discretly filtered Inducer.
     */    
    public static BaseInducer make_disc_filter(String prefix, BaseInducer innerInducer) {
/*        DiscFilterInducer inducer = new DiscFilterInducer("automatic-filter");
        inducer.set_user_options_no_inducer(prefix);
        inducer.set_inducer(innerInducer);
        return inducer;
*/      return null;
    }
    
    /** Creates a search wrapper of the specified type.
     * @param prefix Prefix for Inducer options.
     * @param inducerType The type of search wrapping Inducer.
     * @param inducerName The name of the search wrapping Inducer.
     * @return The search wrapping Inducer.
     */    
    static public BaseInducer search_inducers(String prefix, String inducerType, String inducerName) {
//        if (inducerType.equals("FSS")) {
//            FSSInducer inducer = new FSSInducer(inducerName);
//            inducer.set_user_options(prefix + "FSS_");
//            return inducer;
/*            } else if (inducerType == "disc-search") {
                DiscSearchInducer inducer = new DiscSearchInducer(inducerName);
                inducer.set_user_options(prefix + "DISC_");
                return inducer;
            } else if (inducerType == "order-FSS") {
                OrderFSSInducer inducer = new OrderFSSInducer(inducerName);
                inducer.set_user_options(prefix + "OFSS_");
                return inducer;
            } else if (inducerType == "C4.5-auto-parm") {
                C45APInducer inducer = new C45APInducer(inducerName);
                inducer.set_user_options(prefix + "AP_");
                return inducer;
            } else if (inducerType == "table-cascaded") {
                TableCasInd inducer = new TableCasInd(inducerName);
                inducer.set_user_options(prefix);
                return inducer;
            } else if (inducerType == "weight-search") {
                WeightSearchInducer inducer = new WeightSearchInducer(inducerName);
                inducer.set_user_options(prefix + "WEIGHT_");
                return inducer;
            } else if (inducerType == "construct-filter") {
                ConstrFilterInducer inducer = new ConstrFilterInducer(inducerName);
                inducer.set_user_options(prefix + "CONSTR_");
                return inducer;
 */
//    } else
     return null;
    };
    
}
