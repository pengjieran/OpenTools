package arithmetic.c45;

import arithmetic.id3.ID3Inducer;
import arithmetic.shared.AugCategory;
import arithmetic.shared.GlobalOptions;
import arithmetic.shared.InstanceList;

/* Driver class, used to create,initialize,run inducers */

public class C45Driver
{

public static void main(String[] args)
{
   ID3Inducer id3 = new ID3Inducer("ID3");
   id3.prune(true);
   if(args.length < 1) 
   {
      System.err.print("Error - file base path required.");
      System.exit(1);
   }
   if(args.length == 2)
   {
      id3.set_log_level(Integer.parseInt(args[1]));
      GlobalOptions.logLevel = Integer.parseInt(args[1]);
   }
   else
   {
      id3.set_log_level(0);
      GlobalOptions.logLevel = 0;
   }
   if(args.length == 3)
   {
      AugCategory.MLCBinaryDisplay = Boolean.valueOf(args[2]).booleanValue();
   }
   if(args.length > 3)
   {
      System.err.print("Error - Too many arguments.");
      System.exit(1);
   }

   InstanceList traindata = new InstanceList(args[0],".names",".data");
   InstanceList testdata = new InstanceList(args[0],".names",".test");
   boolean[] bitstring = {false, true, false, false};

   System.out.println("The probability of error is: "+id3.train_and_test(traindata,testdata));

   id3.display_struct();
   System.out.println("The number of nodes is: " + id3.num_nontrivial_nodes());
   System.out.println("The number of leaves is: " + id3.num_nontrivial_leaves());
}

}
