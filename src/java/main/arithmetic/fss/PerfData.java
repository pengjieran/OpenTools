package arithmetic.fss;

import java.io.Writer;

import arithmetic.shared.Basics;
import arithmetic.shared.CatTestResult;
import arithmetic.shared.Error;
import arithmetic.shared.Globals;
import arithmetic.shared.MLJ;
import arithmetic.shared.Matrix;

public class PerfData{

   private ErrorData error;
   private ErrorData mse;
   private ErrorData mae;
   private ErrorData normalizedLoss;
   private ErrorData loss;
   private ErrorData logLoss = new ErrorData();
   
   private double[][] confusionMatrix;

   // cost is number of runs of inducer needed to get result
   private int evalCost;

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private PerfData(PerfData source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(PerfData source){}

   public void OK(int level){OK();}
   public void OK()
   {
      MLJ.ASSERT(mae.size() == mse.size(),"PerfData.OK: mae.size() != mse.size()");
      MLJ.ASSERT(normalizedLoss.size() == mse.size(),"PerfData.OK: normalizedLoss.size() != mse.size()");
      MLJ.ASSERT(loss.size() == mse.size(),"PerfData.OK: loss.size() != mse.size()");
      MLJ.ASSERT(logLoss.size() == mse.size(),"PerfData.OK: logLoss.size() != mse.size()");
   
   if (mse.size() > 0) {
      MLJ.ASSERT(error.size() == mse.size(),"PerfData.OK: error.size() != mse.size()");
      MLJ.ASSERT(confusionMatrix != null,"PerfData.OK: confusionMatrix == null");
   } else
      MLJ.ASSERT(confusionMatrix == null,"PerfData.OK: confusionMatrix != null");
   }


   public PerfData()
   {
      error = new ErrorData(true);
      mse = new ErrorData(true);
      mae = new ErrorData(true);
      normalizedLoss = new ErrorData(true);
      loss = new ErrorData(false);
      confusionMatrix = null;
      evalCost = 0;
   }


   protected void finalize()
   {
//obs   DBG(OK());
      confusionMatrix = null;
   }

   public void insert(CatTestResult item)
   {
      if (Basics.DBG) 
      {
         if (error.size() != mse.size())
            Error.fatalErr("PerfData::insert: cannot be called on the same object that "
               +"insert_error is called upon");
      }

      if (size() == 0)
         initialize_confusion_matrix(item.get_confusion_matrix());
      else
         accumulate_confusion_matrix(item.get_confusion_matrix());
   
      double totalTestWeight = item.total_test_weight();
      error.insert(item.error());
      mse.insert(item.total_mean_squared_error() / totalTestWeight);
      mae.insert(item.total_mean_absolute_error() / totalTestWeight);
      logLoss.insert(item.total_log_loss() / totalTestWeight);
      loss.insert(item.total_loss() / totalTestWeight);
      normalizedLoss.insert(item.normalized_loss());
      if (Basics.DBG) OK();
   }

//obs private void initialize_confusion_matrix(const Array2<Real>& confMatrix)
   private void initialize_confusion_matrix(double[][] confMatrix)
   {
      MLJ.ASSERT(confusionMatrix == null,"PerfData.initialize_confusion_matrix: confusionMatrix != null");
//obs   confusionMatrix = new Array2<Real>(confMatrix, ctorDummy);
      confusionMatrix = Matrix.copy(confMatrix);
   }

//obs private void accumulate_confusion_matrix(const Array2<Real>& confMatrix)
   private void accumulate_confusion_matrix(double[][] confMatrix)
   {
      MLJ.ASSERT(confusionMatrix != null,"PerfData.initialize_confusion_matrix: confusionMatrix == null");

//obs   for(int i = 0; i < confMatrix.size(); i++)
//obs      confusionMatrix->index(i) += confMatrix.index(i);
      for(int i = 0; i < confMatrix.length; i++)
         for(int j = 0; j < confMatrix[i].length; j++)
//obs         confusionMatrix->index(i) += confMatrix.index(i);
            confusionMatrix[i][j] += confMatrix[i][j];
   }

   public int size()
   {
      if (Basics.DBG) OK();
      return error.size();
   }

   public void insert_error(double errorRate)
   {
      if (Basics.DBG)
         if (mse.size() > 0)
            Error.fatalErr("PerfData::insert_error: can only be called if insert has "
               +"not been called");
      error.insert(errorRate);
      if (Basics.DBG) OK();
   }

   public void insert_cost(int cost)
   {
      evalCost += cost;
   }

   public void set_test_set(CatTestResult item)
   {
   // it is not legal to call this function if only the error has test
   // set data established.
      if(error.has_test_set() && !mse.has_test_set())
         Error.fatalErr("PerfData::set_test_set: attempting to set full test set "
            +"information for a PerfData which is using only error");

   // set the metrics
      double totalTestWeight = item.total_test_weight();
      error.set_test_set(item.error(), totalTestWeight);
      mse.set_test_set(item.total_mean_squared_error() / totalTestWeight,
         totalTestWeight);
      mae.set_test_set(item.total_mean_absolute_error() / totalTestWeight,
         totalTestWeight);
      logLoss.set_test_set(item.total_log_loss() / totalTestWeight,
         totalTestWeight);
      loss.set_test_set(item.total_loss() / totalTestWeight,
         totalTestWeight);
      normalizedLoss.set_test_set(item.normalized_loss(), totalTestWeight);
   }

   public void set_test_set(double testSetError, double totalTestWeight)
   {
   // it is not legal to call this function if all metrics have test
   // set data established.
      if(mse.has_test_set())
         Error.fatalErr("PerfData::set_test_set: attempting to set error only "
            +"for a PerfData which has full test set information");
      error.set_test_set(testSetError, totalTestWeight);
   }

   public boolean has_test_set(){ return error.has_test_set(); }
   public boolean has_perf_test_set(){ return mse.has_test_set(); }
   public boolean perf_empty()
   {
      if (Basics.DBG) OK();
      return mse.size() == 0;
   }

   public boolean empty(){ return error.size() == 0; }

   public void append(PerfData other)
   {
      error.append(other.error);
      mse.append(other.mse);
      mae.append(other.mae);
      logLoss.append(other.logLoss);
      normalizedLoss.append(other.normalizedLoss);
      loss.append(other.loss);
      if (other.confusionMatrix != null)
         if (confusionMatrix == null)
            initialize_confusion_matrix(other.confusionMatrix);
         else
            accumulate_confusion_matrix(other.confusionMatrix);
      evalCost += other.evalCost;
   }

   public void clear()
   {
      error.clear();
      mse.clear();
      mae.clear();
      logLoss.clear();
      normalizedLoss.clear();
      loss.clear();
//obs      delete confusionMatrix;
//already done      confusionMatrix = null;
      confusionMatrix = null;
      evalCost = 0;
   }

   public ErrorData get_error_data(){ return error; }
   public ErrorData get_mean_squared_error_data(){ return mse; }
   public ErrorData get_mean_absolute_error_data(){ return mae; }
   public ErrorData get_normalized_loss_data(){ return normalizedLoss; }
   public ErrorData get_loss_data(){ return loss; }

   public int get_cost(){ return evalCost; }
   public void set_cost(int cost) { evalCost = cost; }

   public double[][] get_confusion_matrix()
   {
      if (confusionMatrix == null)
         Error.fatalErr("PerfData::get_confusion_matrix: No confusion matrix");
      return confusionMatrix;
   }

/***************************************************************************
  Displays the error information.
***************************************************************************/
   public void display_error(Writer stream, double trim, int precision)
   {
      error.display(stream, trim, precision);
   }

   public void display_error()
   {display_error(Globals.Mcout,0,ErrorData.defaultPrecision);}
   public void display_error(Writer stream)
   {display_error(stream,0,ErrorData.defaultPrecision);}
   public void display_error(Writer stream, double trim)
   {display_error(stream,trim,ErrorData.defaultPrecision);}


/***************************************************************************
  Displays all statistics.
***************************************************************************/
public void display(Writer stream, boolean dispLogLoss, double trim, int precision)
{
/*   if (Basics.DBG)OK();
   stream.write("Error: ");
   display_error(stream, trim, precision);
   stream.write("\n");

   // display other metrics if available
   if(mse.size() >= 1)
      display_non_error(stream, dispLogLoss, trim, precision);
   stream.write("\n");

   // display cost information
   stream.write("Evaluation cost: "+evalCost+"\n");
*/
}

/*
   
public:
   

   // set test set error information
   
   void display_conf_matrix(MLCOStream& stream, const SchemaRC& schema) const;
   void display(MLCOStream& stream = Mcout, Bool dispLogLoss = FALSE,
		Real trim = 0, int precision = ErrorData::defaultPrecision)
      const;
   void display_non_error(MLCOStream& stream = Mcout,
			  Bool dispLogLoss = FALSE,
			  Real trim = 0,
			  int precision = ErrorData::defaultPrecision) const;
};
*/

}