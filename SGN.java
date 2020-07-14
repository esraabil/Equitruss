import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//super node which is the node of index graph
public class SGN  {
	int truss;
	Integer idd;
	List<MyEdge> edgelist;
	public SGN(int truss,int tnid) {
		
		this.truss=truss;
		this.idd=tnid;
		edgelist=new ArrayList<MyEdge>();
	}
	public void addEdge(MyEdge e)
	{
		edgelist.add(e);
	}

}
