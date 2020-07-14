import java.io.*;
import java.util.*;
//general index class, inclused general definitions and methods
public abstract class  TecIndexG {
	
	Map<Integer,Set<Integer>> vtoSGN ;/*dictionary for original graph vertices to summary graph nodes;
	*key is original graph vertex , value is the super node set which include this vertex*/
	Map<Integer,SGN> idSGN;// dictionary for super nodes;  key is id and value is super node objet
	Map<Integer,Set<Integer>> SG;//index summary graph; key is the vertex, value is the edge set of  key vertex
	private Double size;
	public TecIndexG()
	{
		vtoSGN=new HashMap<Integer, Set<Integer>>();
		idSGN=new HashMap<Integer, SGN>();
		SG=new HashMap<Integer, Set<Integer>>();
		size=0.0;
	}
	public abstract  void constructIndex(Map<Integer, LinkedHashSet<MyEdge>> klistdict,Map<MyEdge,Integer> trussd,MyGraph g);
	public abstract LinkedList<LinkedList<MyEdge>> findkCommunityForQuery(int query,int k) throws IOException;

	public Double getSize()
	{
		return size;
	}
	public void computeSize()
	{
		size=0.0;
		for(Integer v:vtoSGN.keySet())
		{
			size+=(8+vtoSGN.get(v).size()*4);
		}
//		System.out.printf("visited %s\n",size);
//		size+=8*idSGN.keySet().size(); //4 for iv + 4 for truss value of index vertex iv(idSGN.get(iv))

		for(Integer iv:idSGN.keySet())
		{
			size+=4;// for iv
			size+=4;// for truss value of index vertex iv(idSGN.get(iv))
			size+=4;// for id of index vertex iv(idSGN.get(iv))
			size+=4*idSGN.get(iv).edgelist.size();
		}
//		System.out.printf("idSGN %s\n",size);
		size+=8*SG.keySet().size();//
		for(Integer v:SG.keySet())
		{
			size+=4*SG.get(v).size();
		}
	}
	
	public void writeIndex(String path) throws IOException
	{
		FileWriter fileWriter = new FileWriter(path.concat("/superNodes.txt"));
		BufferedWriter bw =  new BufferedWriter(fileWriter);
		for (Integer sid:idSGN.keySet())
		{
			SGN sg=idSGN.get(sid);
			bw.write("id,");
			bw.write(Integer.toString(sid));
			bw.write(",truss,");
			bw.write(Integer.toString(sg.truss));
			bw.write("\n");
			ListIterator<MyEdge> listIterator = sg.edgelist.listIterator();
			while (listIterator.hasNext()) {
				MyEdge e=listIterator.next();
				bw.write(Integer.toString(e.s));
				bw.write(",");
				bw.write(Integer.toString(e.t));
				bw.write("\n");
			}
		}
		bw.close();
		fileWriter = new FileWriter(path.concat("/ogn_ign_dic.txt"));//original graph nodes to index graph node dictionary
		bw =  new BufferedWriter(fileWriter);
		bw.write("original_node_id index_graph_node_id\n");
		for(Integer k:vtoSGN.keySet())
		{
			for (Integer ign:vtoSGN.get(k))
			{
				bw.write(Integer.toString(k));
				bw.write(" ");
				bw.write(Integer.toString(ign));
				bw.write("\n");
			}
		}
		bw.close();
		fileWriter = new FileWriter(path.concat("/summaryIndexGraph.txt"));//summary index graph 
		bw =  new BufferedWriter(fileWriter);
		for(Integer kid:SG.keySet())
		{
			for(Integer nid:SG.get(kid))
			{
				bw.write(Integer.toString(kid));
				bw.write(",");
				bw.write(Integer.toString(nid));
				bw.write("\n");
			}
		}
		bw.close();
	}
	public void read_Indexlj(MyGraph g,String path) throws IOException
	{
		FileReader reader=new FileReader(path.concat("/superNodes.txt"));
		BufferedReader br = new BufferedReader(reader);
		String line =br.readLine();
		String[] sr=line.trim().split(",");
		int id=Integer.parseInt(sr[1]);
		int truss=Integer.parseInt(sr[3]);
		SGN sg=new SGN(truss, id);
		while((line = br.readLine()) != null) {
			sr=line.trim().split(",");
			if(sr[0].equals("id"))
			{
				idSGN.put(id, sg);
				Set<Integer> nl=new HashSet<Integer>();
				SG.put(id, nl);
				id=Integer.parseInt(sr[1]);
				truss=Integer.parseInt(sr[3]);
				sg= new SGN(truss, id);
			}
			else
				sg.edgelist.add(g.getEdge(Integer.parseInt(sr[0]),Integer.parseInt(sr[1])));
		}
		idSGN.put(id, sg);
		Set<Integer> nl=new HashSet<Integer>();
		SG.put(id, nl);
		br.close();
		for(Integer ci:idSGN.keySet())
		{
			for(MyEdge e:idSGN.get(ci).edgelist)
			{
				if(!vtoSGN.containsKey(e.s))
					vtoSGN.put(e.s, new HashSet<>());
				vtoSGN.get(e.s).add(ci);
				if(!vtoSGN.containsKey(e.t))
					vtoSGN.put(e.t, new HashSet<>());
				vtoSGN.get(e.t).add(ci);
			}
		}
		reader=new FileReader(path.concat("/summaryIndexGraph.txt"));
		br = new BufferedReader(reader);
		while((line = br.readLine()) != null)
		{
			if(line.equals("vertex"))
				break;
			sr=line.trim().split(",");
			SG.get(Integer.parseInt(sr[0])).add(Integer.parseInt(sr[1]));
			SG.get(Integer.parseInt(sr[1])).add(Integer.parseInt(sr[0]));
		}
		br.close();
	}
}
