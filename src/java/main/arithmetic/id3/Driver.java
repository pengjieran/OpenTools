package arithmetic.id3;
import arithmetic.shared.AugCategory;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.InstanceList;
/* Driver class, used to create,initialize,run inducers */

/** Basic Driver class used to interface the ID3Inducer.
 * @author James Louis Created implementation.
 */
public class Driver {
    
    /** The main driving function.
     * @param args Command line arguments. The first argument should be the path to the data
     * files to be used for training and testing. They should have the same path
     * name. The second and third options are optional. The second option sets the
     * log level to the given integer number. The default is 0. The third value is
     * a boolean variable associated with how categories are displayed.
     */
    public static void main(String[] args) {
        //try{
        
        ID3Inducer id3 = new ID3Inducer("ID3");
        if(args.length < 1) {
            System.err.print("Error - file base path required.");
            System.exit(1);
        }
        if(args.length == 2) {
            id3.set_log_level(Integer.parseInt(args[1]));
            GlobalOptions.logLevel = Integer.parseInt(args[1]);
        }
        else {
            id3.set_log_level(0);
            GlobalOptions.logLevel = 0;
        }
        if(args.length == 3) {
            AugCategory.MLCBinaryDisplay = Boolean.valueOf(args[2]).booleanValue();
        }
        if(args.length > 3) {
            System.err.print("Error - Too many arguments.");
            System.exit(1);
        }
        
        
        //}catch(Exception e){e.printStackTrace();}
        
        InstanceList traindata = new InstanceList(args[0],".names",".data");
        //   InstanceList traindata = new InstanceList(args[0]);
        InstanceList testdata = new InstanceList(args[0],".names",".test");
        //   InstanceList copy = new InstanceList(testdata);
        boolean[] bitstring = {false, true, false, false};
        //   InstanceList newtraindata = traindata.project(bitstring);
        //   InstanceList newtestdata = testdata.project(bitstring);
        //   System.out.println("Displaying projected data.");
        //   newtraindata.display(false);
        
        //Sets the unknown edge generation option. False indicates no unknown edges.
        id3.set_unknown_edges(false);
        
        System.out.println("The probability of error is: "+id3.train_and_test(traindata,testdata));
        
        //Testing of CHC functions.
        //   System.out.println("The probability of error is: "+id3.project_train_and_test(traindata,testdata,bitstring));
        //   System.out.println("The probability of error is: "+id3.project_train_and_test_files(args[0],bitstring));
        
        
        id3.display_struct();
        System.out.println("The number of nodes is: " + id3.num_nontrivial_nodes());
        System.out.println("The number of leaves is: " + id3.num_nontrivial_leaves());
        ID3Inducer blah = new ID3Inducer("Blah");
    }
    
}
