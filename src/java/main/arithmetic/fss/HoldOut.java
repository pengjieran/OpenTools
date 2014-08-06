package arithmetic.fss;

import arithmetic.shared.BaseInducer;
import arithmetic.shared.Error;
import arithmetic.shared.InstanceList;
import arithmetic.shared.MLJ;

public class HoldOut extends PerfEstimator {

   private int numTimes;          // number of times to run holdout
   private int numHoldOut;        // number of instances to hold out
   private double pctHoldOut;       // percentage of instances to hold out

   public static int defaultNumTimes = 1;
   public static int defaultNumber = 0;
   public static double defaultPercent = 0.67;


/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private HoldOut(HoldOut source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(HoldOut source){}

/***************************************************************************
  Constructor.
***************************************************************************/
   public HoldOut(int nTimes, int number, double pct)
   {
      set_times(nTimes);
      set_number(number);
      set_percent(pct);
   }

/***************************************************************************
  Constructor.
***************************************************************************/
   public HoldOut()
   {
      set_times(defaultNumTimes);
      set_number(defaultNumber);
      set_percent(defaultPercent);
   }

/***************************************************************************
  Constructor.
***************************************************************************/
   public HoldOut(int nTimes)
   {
      set_times(nTimes);
      set_number(defaultNumber);
      set_percent(defaultPercent);
   }

/***************************************************************************
  Constructor.
***************************************************************************/
   public HoldOut(int nTimes, int number)
   {
      set_times(nTimes);
      set_number(number);
      set_percent(defaultPercent);
   }


   protected void finalizer()
   {
   }

/***************************************************************************
  Returns a descriptive string for this performance estimator.
***************************************************************************/
public String description()
{
   String holdoutString = new String("");
   if(get_number() == 0)
//obs      holdoutString = "hold out " + MString(get_percent()*100, 0) + "%";
      holdoutString = "hold out " + (get_percent()*100) + "%";
   else
      holdoutString = "hold out " + get_number();
   return (get_times() + " x " + holdoutString);
}

/***************************************************************************
  Trains and tests the inducer. The Hold Out method involves holding a 
certain number of instances aside for testing.  The inducer is trained on 
the remaining instances.  The method is repeated a number of times and the 
results are averaged.
***************************************************************************/
public double estimate_performance(BaseInducer inducer, InstanceList dataList)
{
   logOptions.LOG(1, "Inducer: " +inducer.description() +"\n");
   logOptions.LOG(1, "Description: " +description() +"\n");
   logOptions.LOG(6, "Training list: " +dataList +"\n");

   // clear out statistical data
   perfData = null;
   perfData = new PerfData();

   // compute number of instances to hold out
   int num = get_number();
   int size = dataList.num_instances();
   if(num >= size)
      Error.fatalErr("HoldOut::estimate_performance: number to hold out for training "
	 +"(" +num +") must not exceed number of instances (" +
	 size +")");
   else if(num <= -size)
      Error.fatalErr("HoldOut::estimate_performance: number to hold out for testing "
	 +"(" +(-num)+") must not exceed number of instances (" + size +")");
   
   if(num < 0)
      num = size + num;
   else if(num == 0)
//obs      num = (int)(Mround(get_percent() * size, 0));
      num = (int)(Math.round(get_percent() * size));
   MLJ.ASSERT(num > 0 && num < size,"HoldOut.estimate_performance: num <= 0 || num >= size.");

   // run performance estimation
   MLJ.ASSERT(numTimes != 0,"HoldOut.estimate_performance: numTimes == 0.");
   for(int time = 0; time < numTimes; time++) {
      // shuffle the list
      InstanceList shuffledList = dataList.shuffle(rand_num_gen());

      // pull out the right number of instances from front of the list
      InstanceList sample = shuffledList.split_prefix(num);

      double err = train_and_test(inducer, sample, shuffledList,
		     "-" + time, perfData);
      logOptions.LOG(2, "Error for time "+time+": "+err+"\n");

      // clean up
      shuffledList = null;
      sample = null;
   }

   logOptions.LOG(1, "Error: "+this+"\n");
   perfData.insert_cost(numTimes);
   return error();
}

/***************************************************************************
  Trains and tests the inducer using files. Uses the files fileStem.names, 
and fileStem-#, where # refers to the #th run of HoldOut.
***************************************************************************/
public double estimate_performance(BaseInducer inducer, String fileStem)
{
   perfData = null;
   perfData = new PerfData();

   for (int time = 0; time < numTimes; time++) {
      double error =
	 single_file_performance(inducer, fileStem + "-" + time,
			      perfData);
      logOptions.LOG(2, "Error of time " +time +" is " +error+"\n");
   }

   logOptions.LOG(1, "Error: "+this+"\n");
   perfData.insert_cost(numTimes);
   return error();
}

/***************************************************************************
  Sets the number of times to run hold out.
***************************************************************************/
public void set_times(int nTimes)
{
   if(nTimes <= 0)
      Error.fatalErr("HoldOut::set_times: number of times (" +nTimes +") "
	 +"must be > 0");
   numTimes = nTimes;
}

   public int get_times(){ return numTimes; }

/***************************************************************************
  Sets the number of instances to hold out.
***************************************************************************/
public void set_number(int num)
{
   numHoldOut = num;
}

   public int get_number(){ return numHoldOut; }

/***************************************************************************
  Sets the percentage to hold out.
***************************************************************************/
public void set_percent(double pct)
{
   if(pct <= 0 || pct >= 1)
      Error.fatalErr("HoldOut::set_percent: percent to hold out (" +pct +") "
	 +"must by between zero and one");
   pctHoldOut = pct;
}

   public double get_percent(){ return pctHoldOut; }


}
