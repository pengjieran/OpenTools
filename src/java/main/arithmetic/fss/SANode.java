package arithmetic.fss;

import java.util.Vector;

import arithmetic.shared.MLJ;
import arithmetic.shared.Node;

public class SANode{

   Node Node; // Hello!!! 'node' shadows a typedef!!! sigh...
   int numEvaluations;
   boolean isExpanded;
   double fitness;


/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private SANode(SANode source){}

   public SANode()
   {
      Node = null;
      numEvaluations = 1;
      isExpanded = false;
      fitness = 0;
   }

   public SANode(Node nodePtr, double initFitness)
   {
      MLJ.ASSERT(nodePtr != null,"SANode.SANode:nodePtr == null.");
      Node = nodePtr;
      numEvaluations = 1;
      isExpanded = false;
      fitness = initFitness;
   }

//obs SANode& SANode::operator=(const SANode& rhs)
   public SANode assign(SANode rhs)
   {
      if (this != rhs)
      {
         Node = rhs.Node;
         numEvaluations = rhs.numEvaluations;
         isExpanded = rhs.isExpanded;
         fitness = rhs.fitness;
      }
//   DBGSLOW(ASSERT((*this == rhs) && (rhs == this)));
      return this;
   }

//obs   Bool operator<(const SANode& rhs) const;
   public boolean lessThan(SANode rhs)
   {
      return fitness > rhs.fitness;
   }

//obs   Bool operator==(const SANode& rhs) const;
   public boolean equals(SANode rhs)
   {
      return fitness == rhs.fitness;
   }

//obs   Bool operator!=(const SANode& rhs) const
   boolean notEqual(SANode rhs)
   {
      return !((this)== rhs);
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
      SANode temp = (SANode)elements.get(Up);
      while(Up < Down)
      {
         for(Up = minpos + 1 ; Up <= maxpos ; Up++)
            if (((SANode)elements.get(minpos)).lessThan((SANode)elements.get(Up)))
               break;
         for(Down = maxpos ; Down >= minpos ; Down--)
            if (((SANode)elements.get(Down)).lessThan((SANode)elements.get(minpos)) ||
                ((SANode)elements.get(Down)).equals((SANode)elements.get(minpos)))
               break;
         if (Up < Down)
         {
            temp = (SANode)elements.set(Up,elements.get(Down));
            elements.setElementAt(temp,Down);
         }
      }
      temp = (SANode)elements.set(minpos,elements.get(Down));
      elements.setElementAt(temp,Down);

      sort(elements,minpos,Down - 1);
      sort(elements,Down + 1, maxpos);
   }

}