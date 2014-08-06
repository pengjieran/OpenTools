package arithmetic.fss;

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

public class CompElem {

   public int num;
   public int value;
   public double eval;

//obs   public int operator<(const CompElem& other)
   public boolean lessThan(CompElem other)
   {
      // tie breakers are just an attempt to achieve consistency
      //   across machines because without it there was a difference
      //   on suns and SGI (probably the sort).
      return (eval < other.eval || 
         (eval == other.eval && num < other.num) ||
         (eval == other.eval && num == other.num && value < other.value));
   }

//obs   public int operator==(const CompElem& other)
   public boolean Equals(CompElem other)
   {
      return (eval == other.eval && num == other.num &&
         value == other.value);
   }

//obs public void display(MLCOStream& out = Mcout, PerfEstInfo *info = NULL)

   public void display(Writer out)
   {
      display(out,null);
   }

   public void display(Writer out, PerfEstInfo info)
   {
//obs      (void)info;
      try{
         out.write(num+" ("+value+"): "+eval);
      }catch(IOException e){e.printStackTrace();System.exit(1);}
   }

   static public void sort(Vector elements)
   {
      sort(elements,0,elements.size()-1);
   }

   static public void sort(Vector elements, int minpos, int maxpos)
   {
      if (minpos >= maxpos)
         return;
      int Up = 0;
      int Down = 1;
      CompElem temp = (CompElem)elements.get(Up);
      while(Up < Down)
      {
         for(Up = minpos + 1 ; Up <= maxpos ; Up++)
            if (((CompElem)elements.get(minpos)).lessThan((CompElem)elements.get(Up)))
               break;
         for(Down = maxpos ; Down >= minpos ; Down--)
            if (((CompElem)elements.get(Down)).lessThan((CompElem)elements.get(minpos)) ||
                ((CompElem)elements.get(Down)).equals((CompElem)elements.get(minpos)))
               break;
         if (Up < Down)
         {
            temp = (CompElem)elements.set(Up,elements.get(Down));
            elements.setElementAt(temp,Down);
         }
      }
      temp = (CompElem)elements.set(minpos,elements.get(Down));
      elements.setElementAt(temp,Down);

      sort(elements,minpos,Down - 1);
      sort(elements,Down + 1, maxpos);
   }



}