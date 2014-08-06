package arithmetic.shared;
import java.util.LinkedList;
import java.util.ListIterator;

/*****************************************************************************
  InstanceHashTable is a clean interface to the user of hash table of 
instances. This HashTable handles collisions by placing the colliding instances
in an InstanceList at that position in the HashTable.<P>
Because the hashCode function does not gaurantee distinct values, it is 
possible for colliding instances to not be equivalent.
@author	James Louis	12/19/2000	Implemented using Java base classes.
*****************************************************************************/

public class InstanceHashTable {

/** The number of Instances stored in this InstanceHashTable. **/
   private int numInstances;
/** The Hashtable in which the Instances will be stored.**/
   private LinkedList table;

/*****************************************************************************
   Constructor with estimated number of instances in the hash table and 
an InstanceList. If a non-null InstanceList is given, all the instance in 
the InstanceList will be inserted into the hash table.
@param estimatedNum	The estimated number of Instances.
*****************************************************************************/
   public InstanceHashTable(int estimatedNum)
   {
      numInstances = 0;
      table = new LinkedList();
   }

/*****************************************************************************
   Constructor with estimated number of instances in the hash table and 
an InstanceList. If a non-null InstanceList is given, all the instance in 
the InstanceList will be inserted into the hash table.
@param estimatedNum	The estimated number of Instances.
@param ihl			The InstanceList of Instances to place in this 
					InstanceHashTable.
*****************************************************************************/
   public InstanceHashTable(int estimatedNum, InstanceList ihl)
   {
      numInstances = 0;
      table = new LinkedList();
      if (ihl != null) 
         for (ListIterator pix = ihl.instance_list().listIterator(); pix.hasNext();)
            insert((Instance)pix.next());
   }

/*****************************************************************************
   Creates an InstanceList to be placed in the Hashtable.
@return The InstanceList created.
@param instance	The first Instance to be placed in the InstanceList returned.
*****************************************************************************/
   private InstanceList createHashList(Instance instance)
   {
      InstanceList instHashList = new InstanceList(instance.get_schema());
      instance.is_labelled(true); // force that every instance has a label.
      instHashList.add_instance(instance);
	return instHashList;
   }

/*****************************************************************************
   Returns the reference to the InstanceList matching the given Instance. 
@return An InstanceList if found or NULL if there is no such element.
@param instance	The Instance used as a key in this Hashtable.
*****************************************************************************/
   public InstanceList find(Instance instance)
   {
      instance.is_labelled(true); // force every instance to have a label.
	ListIterator tableIndex = table.listIterator();
      while(tableIndex.hasNext())
      {
         InstanceList temp = ((InstanceList)tableIndex.next());
         if(((Instance)temp.instance_list().getFirst()).equals(instance)) return temp;
      }
      return null;
   }

/*****************************************************************************
  Adds an instance into the hash table by converting the instance into a 
InstanceListSameAttr of the one instance. Aborts if the instance is unlabelled.
If there exists such an instance, it merge the existing InstanceListSameAttr 
with the just created one.
@param instance The Instance that is to be inserted.
*****************************************************************************/
   public void insert(Instance instance)
   {
      instance.is_labelled(true);   // force that every instance has a
                                    // label.
      numInstances++;
      InstanceList temp = find(instance);
      if(temp == null){
         temp = createHashList(instance);
         table.add(temp);}
      else{
         temp.add_instance(instance);}
   }

};
