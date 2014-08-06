package arithmetic.fss;

import java.io.Writer;

import arithmetic.shared.Globals;

public class FSSInfo extends PerfEstInfo{

   // Default Ctor is used by FSSInducer
/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(FSSInfo source){}

   public int lower_bound(int num)
   { return 0; }

   public int upper_bound(int num)
   { return 1; }

   public void display_values(int[] values)
   {
      display_values(values, Globals.Mcout);
   }

   public void display_values(int[] values, Writer out)
   {
//obs      (void)values;
//obs      (void)out;
   }

   public int class_id()
   {
      return FSS_INFO;
   }
}