package arithmetic.shared;
/*
 * u_Inducer.java
 *
 * Created on January 29, 2002, 1:39 PM
 */

/**
 *
 * @author  User
 */
import java.io.IOException;

public class u_Inducer {
    
    static String TRAIN_WITHOUT_LOSS_HELP = "Set if you want to disable "+
    "any loss matrix which is specified in the .names file.";

    static boolean DEFAULT_COMPUTE_LOG_LOSS = false;
    static String COMPUTE_LOG_LOSS_HELP = "Set to compute the log-loss of the "+
    "classifier. This will only work if the classifier never assigns a "+
    "probability of zero to the correct class.";
    

    static GetEnv getenv = new GetEnv();

    
    /** Creates a new instance of u_Inducer */
    public u_Inducer() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        GlobalOptions.printPerfPrecision = 3; // We want higher precision
        
        CmdLine.process_mlc_cmdline(args);
        try{
            BaseInducer baseInducer = Env_Inducer.env_inducer();
            FileNames files = new FileNames();
            
            InstanceList trainList = new InstanceList(Globals.EMPTY_STRING, files.names_file(), files.data_file());
            
            boolean suppressLoss = getenv.get_option_bool("TRAIN_WITHOUT_LOSS", false, TRAIN_WITHOUT_LOSS_HELP, true);
            
            if (suppressLoss && trainList.get_schema().has_loss_matrix()) {
                // remove the loss matrix from the inducer's schema
                Globals.Mcout.write("Suppressing the loss matrix for training\n");
                Schema schema = trainList.get_schema();
                schema.remove_loss_matrix();
                trainList.set_schema(schema);
                
            }
            
            if (baseInducer.can_cast_to_inducer()) {
                Inducer inducer = baseInducer.cast_to_inducer();
                
                inducer.assign_data(trainList);
                //MLJ.ASSERT(trainList == null,"u_inducer.main(): trainList != null");
                inducer.train();
                
                InstanceList testList = null;
                
                if (files.test_file(false) != Globals.EMPTY_STRING) {
                    InstanceList il = inducer.instance_list();
                    testList = new InstanceList(il.get_schema(),
                    il.get_original_schema(),
                    files.test_file());
                    if (suppressLoss && testList.get_schema().has_loss_matrix()) {
                        Globals.Mcout.write("Suppressing the loss matrix for testing\n");
                        Schema schema = testList.get_schema();
                        schema.remove_loss_matrix();
                        testList.set_schema(schema);
                    }
                    
                    boolean computeLogLoss = getenv.get_option_bool("COMPUTE_LOG_LOSS", DEFAULT_COMPUTE_LOG_LOSS, COMPUTE_LOG_LOSS_HELP);
                    CatTestResult.set_compute_log_loss(computeLogLoss);
                    CatTestResult result = new CatTestResult(inducer.get_categorizer(), il, testList);
                    Globals.Mcout.write(result + "\n");
                    //                display_lift_curve(result);
                }
                
                Categorizer categorizer = inducer.release_categorizer();
                
                //            backfit_categorizer(categorizer, inducer.instance_list(), testList);
                //            display_categorizer(categorizer, testList);
                //            make_persistent_categorizer(categorizer);
                
                categorizer = null;
                testList = null;
                
            } else {
                
                if (files.test_file(false) == Globals.EMPTY_STRING)
                    Error.fatalErr("Inducer: external inducers must use a test file");
                
                InstanceList testSet = new InstanceList(trainList.get_schema(),
                trainList.get_original_schema(),
                files.test_file());

            if (suppressLoss && trainList.get_schema().has_loss_matrix()) {
                Globals.Mcout.write("Supressing the loss matrix for testing\n");
                Schema schema = testSet.get_schema();
                schema.remove_loss_matrix();
                testSet.set_schema(schema);
            }
 
            if (baseInducer.supports_full_testing()) {
                boolean computeLogLoss =
                getenv.get_option_bool("COMPUTE_LOG_LOSS", DEFAULT_COMPUTE_LOG_LOSS, COMPUTE_LOG_LOSS_HELP);
                CatTestResult.set_compute_log_loss(computeLogLoss);
                CatTestResult result = baseInducer.train_and_perf(trainList, testSet);
 
//                display_lift_curve(result);
//                display_tableviz_from_inducer(baseInducer);
                Globals.Mcout.write(result+"\n");
                result = null;
            } else {
                double error = baseInducer.train_and_test(trainList, testSet);
//                display_tableviz_from_inducer(baseInducer);
 
                DoubleRef confLow = new DoubleRef();
                DoubleRef confHigh = new DoubleRef();
                CatTestResult.confidence(confLow, confHigh, error,
                testSet.total_weight());
                Globals.Mcout.write("Error: "
                + MLJ.numberToString(error*100, GlobalOptions.printPerfPrecision)
                + "% +- "
                + MLJ.numberToString(CatTestResult.theoretical_std_dev(error,
                testSet.total_weight())*100,
                GlobalOptions.printPerfPrecision) + "% ["
                + MLJ.numberToString(confLow.value*100,
                GlobalOptions.printPerfPrecision) +"% - "
                + MLJ.numberToString(confHigh.value*100,
                GlobalOptions.printPerfPrecision) +"%]" + "\n");
            }
        }
            
            trainList = null;
            baseInducer = null;
            
            System.exit(0); // return success to shell
        }catch(CloneNotSupportedException e){
            Error.err("u_inducer.main(): CloneNotSupportedException occurs "
            +"during removal of loss matrix from inducer's schema.");
        }catch(IOException e){
            Error.err("u_inducer.main(): IOException occurs "
            +"during removal of loss matrix from inducer's schema.");
        }
    }

}
