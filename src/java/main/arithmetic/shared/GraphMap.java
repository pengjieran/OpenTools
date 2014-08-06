package arithmetic.shared;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class GraphMap implements ParamTypes{
//added by JL
//based on LEDA

	public int           kind;  // node/edge/face
	public Object[]      table;
	public int           table_size;
	public Graph         g;
	public int           g_index;
	public Object        g_loc;	//location in Graph class's map_list  //list_item     g_loc;
	public Object        def_entry; 
	public ArrayOrd	   array_ord_node;	//these are internal classes
	public ArrayOrd	   array_ord_edge;	//these are internal classes
	public ArrayCmp	   array_cmp;		//these are internal classes


	protected class ArrayOrd implements OrderingFunction {
		//needed for the bucket_sort_nodes(GraphMap) function in Graph :JL
		//needed for the bucket_sort_edges(GraphMap) function in Graph :JL
		public int order(Object the_item){
			return ((Integer)array_read((GraphObject)the_item)).intValue();}
	}

	protected class ArrayCmp implements SortFunction {
		//needed for sort_edges(GraphMap) and sort_nodes(GraphMap) in Graph :JL
		public boolean is_less_than(Object num1, Object num2){
			Object o1 = array_read((GraphObject)num1);
			Object o2 = array_read((GraphObject)num2);
			switch (elem_type_id()){
				case INT_TYPE_ID:	//objects are ints
					return (((Integer)o1).intValue() < ((Integer)o2).intValue());
				case FLOAT_TYPE_ID: //objects are floats
					return (((Float)o1).floatValue() < ((Float)o2).floatValue());
				default:		//objects are doubles
							//all other types are caught in Graph
					return (((Double)o1).doubleValue() < ((Double)o2).doubleValue());
			}
		}
	}

	public GraphMap(Graph G, int k){
		kind = k; 
		g = G;
		g_index = 0;
		table = null;
		table_size = 0;
	}

	public GraphMap(Graph G, int sz, int k){
		kind = k; 
		g = G;
		g_index = -1;

		if (g != null) g_index = g.register_map(this);

		if (g_index > -1){
			table = null;	//lose reference to table
			table_size = 0;	//set the table size to empty
			return; 
		}

		def_entry = null;
		table = null;		//lose reference to table
		table_size = next_power(sz);	//get next larger size for table
		if (table_size > 0){ 
			table = new Object[table_size];
			if (table == null) 
				System.err.println(" GraphMap: out of memory"); //error_handler 1
		}
	}

	public GraphMap(GraphMap M) {
		kind = M.kind;
		g = M.g;
		if (M.g_index == 0) {
			g_index = 0;
			table = null;
			return;
		}
		g_index = -1;
		if (g != null) g_index = g.register_map(this);
		def_entry = null;
		table = null;
		table_size = M.table_size;
		if (table_size > 0){
			table = new Object[table_size];
			if (table == null)
				System.err.println(" GraphMap: out of memory");	//error_handler 1

			for(int p = 0; p < table_size; p++){
				table[p] = M.table[p];	//At the moment this only re-assigns the table
				copy_entry(table[p]);
			}

//obs			Object p = table;
//obs			Object stop = M.table+M.table_size;
//obs			for(Object q=M.table; q < stop; q++) {
//obs				p = q;
//obs				M.copy_entry(p); 
//obs				p++;
//obs			}
		}
	}

	int next_power(int s){
		if (s == 0) return 0;
		int p = 1;
		while (p < s) p<<= 1;
		return p;
	}

	public void assign(GraphMap M){
		if (M == this) return /*this*/;
//		clear_table();
		if (table_size > 0) table = null;	//delete table if table contains nothing
		if (g != null && g_index != 0) g.unregister_map(this);
		table = null;	//lose reference to table
		kind = M.kind;
		g = M.g;
		if (M.g_index == 0){
			g_index = 0;
			table = null;	//lose reference to table
			return;
		}
		g_index = -1;
		if (g != null) g_index = g.register_map(this);
		table_size = M.table_size;
		if (table_size > 0){
			table = new Object[table_size];
			if (table == null)
				System.err.println(" GraphMap: out of memory"); //error_handler 1
			Object p = table;

			for(int q = 0; q < table_size; q++){
				table[q] = M.table[q];		//At the moment this only re-assigns the table
				copy_entry(table[q]);
			}

//obs			Object stop = M.table+M.table_size;
//obs			for(Object q=M.table; q < stop; q++){
//obs				p = q;
//obs				copy_entry(p); 
//obs				p++;
//obs			}
		}
//obs		return this;
	}

	public void re_init_entry(GraphObject theobject){
		if (g_index > -1) 
			init_entry(theobject.data);
//obs			init_entry(theobject.data[g_index]);
		else {
			int i = theobject.index();
			if (i < table_size) { 
//obs				clear_entry(table);
//obs				init_entry(table);
				clear_entry(table[i]);
				init_entry(table[i]);
			}
		}
	}

	private void init_entry(Object entry){}
	private void clear_entry(Object entry){}
	private void copy_entry(Object entry){}

	private void read_entry(BufferedReader Input, Object entry){}
	private void write_entry(BufferedWriter Output, Object entry){}
//obs	private void read_entry(istream Input, Object entry){}
//obs	private void write_entry(ostream Output, Object entry){}

	protected void init_def_entry(){ init_entry(def_entry); }
	protected void clear_def_entry(){ clear_entry(def_entry); }

	public int     size()  { return table_size; }
	public Object access(int i)     { return table[i]; }
	public Object  read(int i)  { return table[i]; }

	private void resize_table(int sz){
		Object[] old_table = table;		//creating reference to old table
		int old_size = table_size;
//obs		Object old_stop  = table + table_size;
		table_size = sz;				//creating new table of the new size
		table = new Object[sz];
		if (table == null)
			System.err.println("GraphMap: out of memory"); //error_handler 1

//obs		Object p = old_table; 
//obs		Object q = table;
//obs		while (p < old_stop) q++ = p++;

		int p = 0;					//loop to assign the references in the
		while (p < old_size){			//old table to the corresponding elements
			table[p] = old_table[p];	//in the new table
			p++;
		}

//obs		init_table(q,table+sz);			//needs to be implemented

//obs		if (old_table != old_stop) delete[] old_table;
		old_table = null;				//explicitly losing reference to old array
	}

	public void init(Graph G, int sz, int k){
		if (g != G){
			if (g != null && g_index != 0) g.unregister_map(this);
			kind = k;
			g = G;
			if (g != null) g_index = g.register_map(this);
		}

		if (g_index > -1){ 
			table = null;
			table_size = 0;
			return; 
		}

		clear_table();
		if (table_size > 0) table = null;

		table = null;

		table_size = next_power(sz);
		if (table_size > 0){ 
			table = new Object[table_size];
			if (table == null) 
				System.err.println(" GraphMap: out of memory");	//error_handler 1
		}
	}


	protected void init_table(int start, int stop){
/*		if (g_index == -1)
			for(int q = start; q < stop; q++) init_entry(table[q]);
		else
			if (g != null && g_index > 0){ 
				switch (kind) {
					case 0 : {
						Node v;
						forall_nodes(v,g) init_entry(v.data[g_index]);
						break;
					}
					case 1 : {
						Edge e;
						forall_edges(e,g) init_entry(e.data[g_index]);
						break;
					}
					case 2 : {
						Face f;
						forall_faces(f,g) init_entry(f.data[g_index]);
						break;
					}
				}
			}
*/	}

	protected void init_table(){
		init_table(0,table_size);	//init_table(table,table+table_size);
	}

	protected void clear_table(){
/*		if (g_index == -1)
			{ Object stop = table + table_size;
			for(Object q=table; q < stop; q++) clear_entry(q);
		}
		else
			if (g && g_index > 0){
				switch (kind) {
					case 0 : {
						Node v;
						forall_nodes(v,g) clear_entry(v.data[g_index]);
						break;
					}
					case 1 : {
						Edge e;
						forall_edges(e,g) clear_entry(e.data[g_index]);
						break;
					}
					case 2 : {
						Face f;
						forall_faces(f,g) clear_entry(f.data[g_index]);
						break;
					}
				}
		}
*/	}



	public int cmp_entry(Object entry1, Object entry2)   { return 0; }

	public int elem_type_id()  {return UNKNOWN_TYPE_ID;}

public Object map_access(GraphObject the_item){return the_item.data;}
public Object array_access(GraphObject the_item){return the_item.data;}
public Object array_read(GraphObject the_item){return array_access(the_item);}
public Object map_read(GraphObject the_item){ return map_access(the_item); }

//obs	public Object map_access(Node v){return v.data[g_index]; }
//obs	public Object map_access(Edge e){return e.data[g_index]; }
//obs	public Object map_access(Face f){return f.data[g_index]; }

//obs GenPtr& array_access(node v) const { return v->data[g_index]; }
//obs GenPtr& array_access(edge e) const { return e->data[g_index]; }
//obs GenPtr& array_access(face f) const { return f->data[g_index]; }

//obs const GenPtr& array_read(node v) const { return array_access(v); }
//obs const GenPtr& array_read(edge e) const { return array_access(e); }
//obs const GenPtr& array_read(face f) const { return array_access(f); }

//obs GenPtr& map_access(node v) const { return v->data[g_index]; }
//obs GenPtr& map_access(edge e) const { return e->data[g_index]; }
//obs GenPtr& map_access(face f) const { return f->data[g_index]; }

//obs const GenPtr& map_read(node v) const { return map_access(v); }
//obs const GenPtr& map_read(edge e) const { return map_access(e); }
//obs const GenPtr& map_read(face f) const { return map_access(f); }

}//End of GraphMap class