package arithmetic.fss;

/***************************************************************************
***************************************************************************/

abstract public class StateGlobalInfo {

/**  **/
   public int numNodesInGraph;
/**  **/
   public int numNodesExpanded;

/***************************************************************************
  This class has no access to a copy constructor.
***************************************************************************/
   private StateGlobalInfo(StateGlobalInfo source){}

/***************************************************************************
  This class has no access to an assign method.
***************************************************************************/
   private void assign(StateGlobalInfo source){}

/***************************************************************************
***************************************************************************/
   public StateGlobalInfo(){
      numNodesInGraph = 0;
      numNodesExpanded = 0;
   }

/***************************************************************************
***************************************************************************/
//   public virtual ~StateGlobalInfo() { }

//   abstract public int class_id();

};
