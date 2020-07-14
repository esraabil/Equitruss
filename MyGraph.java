import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
//My graph structure 
//g is the edge list of the vertices ; key is a vertex value is the edge list of that vertex
public class MyGraph {
	Map<Integer, HashMap<Integer, MyEdge>> g;
	int numberOfEdge;

	public MyGraph() {
		g = new HashMap<Integer, HashMap<Integer, MyEdge>>();
	}

	// compute truss values of the edges and create edge, truss dictinary and
	// truss,edgelist with same truss value dictionary
	public Map<Integer, LinkedHashSet<MyEdge>> computeTruss(String path, Map<MyEdge, Integer> trussd)
			throws IOException {
		Map<Integer, LinkedHashSet<MyEdge>> klistdict = new HashMap<Integer, LinkedHashSet<MyEdge>>();
		LinkedHashSet<MyEdge> kedgelist = new LinkedHashSet<MyEdge>();
		HashMap<MyEdge, Integer> sp = new HashMap<>();
		int kmax = computeSupport(sp);
		// write_support(path+"support.txt",sp);
		int k = 2;
		MyEdge[] sorted_elbys = new MyEdge[sp.size()];

		Map<MyEdge, Integer> sorted_ep = new HashMap<MyEdge, Integer>();
		Map<Integer, Integer> svp = new HashMap<Integer, Integer>();
		bucketSortedgeList(kmax, sp, sorted_elbys, svp, sorted_ep);
//		System.out.println("sorted support");
		for (int i = 0; i < sorted_elbys.length; i++) {
			MyEdge e = sorted_elbys[i];
			int val = sp.get(e);
			if (val > (k - 2)) {
//				System.out.println("finished truss" + k);
				klistdict.put(k, kedgelist);
				k = val + 2;
				kedgelist = new LinkedHashSet<MyEdge>();

			}

			Integer src = e.s;
			Integer dst = e.t;
			Set<Integer> nls = g.get(src).keySet();
			if (nls.size() > g.get(dst).size()) {
				dst = e.s;
				src = e.t;
				nls = g.get(src).keySet();
			}

			for (Integer v : nls) {
				if (g.get(v).containsKey(dst)) {
					MyEdge e1 = getEdge(v, src);
					MyEdge e2 = g.get(v).get(dst);
					if (!(trussd.containsKey(e1) || trussd.containsKey(e2))) {
						if (sp.get(e1) > (k - 2))
							reorderEL(sorted_elbys, sorted_ep, sp, svp, e1);

						if (sp.get(e2) > (k - 2))
							reorderEL(sorted_elbys, sorted_ep, sp, svp, e2);
					}
				}
			}

			kedgelist.add(e);
			trussd.put(e, k);

		}
		klistdict.put(k, kedgelist);
		return klistdict;
	}

	private static void reorderEL(MyEdge[] sorted_elbys, Map<MyEdge, Integer> sorted_ep, Map<MyEdge, Integer> supd,
			Map<Integer, Integer> svp, MyEdge e1) {
		int val = supd.get(e1);
		int pos1 = sorted_ep.get(e1);
		int cp = svp.get(val);
		if (cp != pos1) {
			MyEdge tmp2 = sorted_elbys[cp];
			sorted_ep.put(e1, cp);
			sorted_ep.put(tmp2, pos1);
			sorted_elbys[pos1] = tmp2;
			svp.put(val, cp + 1);
			sorted_elbys[cp] = e1;
		} else {
			if (sorted_elbys.length > cp + 1 && supd.get(sorted_elbys[cp + 1]) == val)
				svp.put(val, cp + 1);
			else
				svp.put(val, -1);
		}
		if (!svp.containsKey(val - 1) || svp.get(val - 1) == -1)
			svp.put(val - 1, cp);
		supd.put(e1, val - 1);
	}

	private static void bucketSortedgeList(int kmax, HashMap<MyEdge, Integer> sp, MyEdge[] sorted_elbys,
			Map<Integer, Integer> svp, Map<MyEdge, Integer> sorted_ep) {
		int[] bucket = new int[kmax + 1];
		for (int i = 0; i < bucket.length; i++)
			bucket[i] = 0;
		for (MyEdge e : sp.keySet())
			bucket[sp.get(e)]++;
		int tmp;
		int p = 0;
		for (int j = 0; j < kmax + 1; j++) {
			tmp = bucket[j];
			bucket[j] = p;
			p = p + tmp;
		}
		for (int i = 0; i < sorted_elbys.length; i++)
			sorted_elbys[i] = null;

		for (Entry<MyEdge, Integer> e : sp.entrySet()) {
			sorted_elbys[bucket[e.getValue()]] = e.getKey();
			sorted_ep.put(e.getKey(), bucket[e.getValue()]);
			if (!svp.containsKey(e.getValue()))
				svp.put(e.getValue(), bucket[e.getValue()]);
			bucket[e.getValue()] = bucket[e.getValue()] + 1;
		}
	}

	public void write_support(String filename, Map<MyEdge, Integer> trussd) throws IOException {
		FileWriter fileWriter = new FileWriter(filename);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		for (MyEdge e : trussd.keySet()) {
			bw.write(Integer.toString(e.s));
			bw.write(",");
			bw.write(Integer.toString(e.t));
			bw.write(",");
			bw.write(Integer.toString(trussd.get(e)));
			bw.write("\n");

		}
		bw.close();
	}

	// compute support of the edges and return a edge,support dictionary
	public int computeSupport(HashMap<MyEdge, Integer> sp) {
		int s = 0;
		int maxs = 0;
		for (int v : g.keySet()) {
			for (int v2 : g.get(v).keySet()) {
				if (!sp.containsKey(g.get(v).get(v2))) {
					s = 0;
					for (int v3 : g.get(v).keySet()) {
						if (v2 != v3) {
							if (g.get(v2).containsKey(v3))
								s++;
						}
					}
					if (s > maxs)
						maxs = s;
					sp.put(getEdge(v, v2), s);
				}
			}
		}
		return maxs;
	}

	// read graph from file
	public void read_GraphEdgelist(String fileName) throws IOException {
		g = new HashMap<Integer, HashMap<Integer, MyEdge>>();
		File f = new File(fileName);
		if(!f.exists()) { 
		   System.out.println("File does not exist");
		   System.exit(0);
		}
		FileReader reader = new FileReader(fileName);
		BufferedReader br = new BufferedReader(reader);
		String line;
		int noe = 0;
		int a;
		while ((line = br.readLine()) != null) {
			a = processLine(line);
			if (a == 1)
				noe++;
		}
		numberOfEdge = noe;
		br.close();
		System.out.println("# of vertices: " + g.size());
		System.out.println("# of edges: " + numberOfEdge);
		// int size = g.size() * 4 + numberOfEdge * 8;
	}

	// proess graph file line and create an edge for it
	protected int processLine(String aLine) {
		StringTokenizer st;
		int id1, id2;
		st = new StringTokenizer(aLine, "	");
		id1 = Integer.parseInt(st.nextToken().trim());
		id2 = Integer.parseInt(st.nextToken().trim());
		if (id1 == id2)
			return 0;
		HashMap<Integer, MyEdge> nl;
		MyEdge me = new MyEdge(id1, id2);

		if (!g.containsKey(id1)) {
			nl = new HashMap<Integer, MyEdge>();
			nl.put(id2, me);
			g.put(id1, nl);
		} else {
			if (g.get(id1).containsKey(id2))
				return 0;
			g.get(id1).put(id2, me);
		}
		if (!g.containsKey(id2)) {
			nl = new HashMap<Integer, MyEdge>();
			nl.put(id1, me);
			g.put(id2, nl);
		} else
			g.get(id2).put(id1, me);
		return 1;
	}

	MyEdge getEdge(int u, int v) {
		return g.get(u).get(v);
	}

	MyEdge removeEdge(int u, int v) {
		if (g.get(u).containsKey(v)) {
			MyEdge re = g.get(u).get(v);
			g.get(u).remove(v);
			g.get(v).remove(u);

			return re;
		}
		return null;
	}

	int removeEdge(MyEdge e) {
		if (g.get(e.s).containsKey(e.t)) {
			g.get(e.s).remove(e.t);
			g.get(e.t).remove(e.s);
			return 1;
		}
		return 0;
	}

	public int addEdge(MyEdge e) {
		if (g.containsKey(e.s)) {
			if (g.get(e.s).containsKey(e.t))
				return 0;
			else
				g.get(e.s).put(e.t, e);
		} else {
			HashMap<Integer, MyEdge> nl = new HashMap<>();
			nl.put(e.t, e);
			g.put(e.s, nl);
		}
		if (g.containsKey(e.t)) {
			if (g.get(e.t).containsKey(e.s))
				return 0;
			else
				g.get(e.t).put(e.s, e);
		} else {
			HashMap<Integer, MyEdge> nl = new HashMap<>();
			nl.put(e.s, e);
			g.put(e.t, nl);
		}
		return 1;
	}

	public MyEdge addEdge(int x, int y) {
		MyEdge me = new MyEdge(x, y);
		if (!g.containsKey(x)) {
			HashMap<Integer, MyEdge> nl = new HashMap<Integer, MyEdge>();
			nl.put(y, me);
			g.put(x, nl);
		} else {
			if (g.get(x).containsKey(y))
				return g.get(x).get(y);
			g.get(x).put(y, me);
		}
		if (!g.containsKey(y)) {
			HashMap<Integer, MyEdge> nl = new HashMap<Integer, MyEdge>();
			nl.put(x, me);
			g.put(y, nl);
		} else {
			if (g.get(y).containsKey(x))
				return g.get(y).get(x);
			;
			g.get(y).put(x, me);
		}
		return me;
	}

	public Set<Integer> getAddjList(int x) {
		return g.get(x).keySet();
	}

	public boolean containsEdge(int u, int v) {
		return g.get(u).containsKey(v);
	}

	// read graph and truss values and create both graph and truss dictionary
	public void read_GraphEdgelistWt(String fileName, Map<MyEdge, Integer> trussd) throws IOException {
		g = new HashMap<Integer, HashMap<Integer, MyEdge>>((int) (18483186.0 / 0.75 + 100));
		FileReader reader = new FileReader(fileName);
		BufferedReader br = new BufferedReader(reader);
		String line;

		// String[] sr;
		int noe = 0;
		int a;
		while ((line = br.readLine()) != null) {
			a = processLineWT(line, trussd);
			if (a == 1)
				noe++;
		}
		numberOfEdge = noe;
		br.close();
		System.out.println("v" + g.size());
		System.out.println("e" + numberOfEdge);
		// int size = g.size() * 4 + numberOfEdge * 8;
	}

	protected int processLineWT(String aLine, Map<MyEdge, Integer> trussd) {
		// StringTokenizer st;
		int id1, id2, t;
		// st = new StringTokenizer(aLine, ",");
		String[] ls = aLine.split(",");
		id1 = Integer.parseInt(ls[0].trim());
		id2 = Integer.parseInt(ls[1].trim());
		t = Integer.parseInt(ls[2].trim());
		// id1 = Integer.parseInt(st.nextToken().trim());
		// id2 = Integer.parseInt(st.nextToken().trim());
		// t = Integer.parseInt(st.nextToken().trim());
		if (id1 == id2)
			return 0;
		HashMap<Integer, MyEdge> nl;
		MyEdge me = new MyEdge(id1, id2);

		if (!g.containsKey(id1)) {
			nl = new HashMap<Integer, MyEdge>();
			nl.put(id2, me);
			g.put(id1, nl);
		} else {
			if (g.get(id1).containsKey(id2))
				return 0;
			g.get(id1).put(id2, me);
		}
		if (!g.containsKey(id2)) {
			nl = new HashMap<Integer, MyEdge>();
			nl.put(id1, me);
			g.put(id2, nl);
		} else
			g.get(id2).put(id1, me);
		trussd.put(me, t);
		return 1;
	}

	public Map<MyEdge, Integer> readTrussf(String path) throws IOException {
		StringTokenizer st;
		Map<MyEdge, Integer> trussd = new HashMap<MyEdge, Integer>(numberOfEdge);
		FileReader reader = new FileReader(path.concat("trussd.txt"));
		BufferedReader br = new BufferedReader(reader);
		String line = "";
		while ((line = br.readLine()) != null) {
			st = new StringTokenizer(line, ",");
			trussd.put(getEdge(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())),
					Integer.parseInt(st.nextToken()));
		}
		br.close();
		return trussd;
	}

	public static Map<Integer, LinkedHashSet<MyEdge>> createKedgeList(Map<MyEdge, Integer> trussd) {
		int kmax = Collections.max(trussd.values());
		Map<Integer, LinkedHashSet<MyEdge>> klistdict = new HashMap<>(kmax);
		// Queue<DefaultWeightedEdge> kedgelist;
		LinkedHashSet<MyEdge> kedgelist;
		for (MyEdge e : trussd.keySet()) {
			if (!klistdict.containsKey(trussd.get(e))) {
				kedgelist = new LinkedHashSet<MyEdge>();
				kedgelist.add(e);
				klistdict.put(trussd.get(e), kedgelist);
			} else {
				klistdict.get(trussd.get(e)).add(e);
			}
		}
		return klistdict;
	}
}

class VertexDegreeComparator implements Comparator<Integer> {
	MyGraph gg;
	int asc;

	public VertexDegreeComparator(MyGraph mg, boolean as) {
		gg = mg;
		if (as)
			asc = 1;
		else
			asc = -1;
	}

	@Override
	public int compare(Integer v1, Integer v2) {
		return gg.g.get(v1).size() < gg.g.get(v2).size() ? (asc * -1)
				: gg.g.get(v1).size() == gg.g.get(v2).size() ? 0 : (asc * 1);

	}
}

//my edge class to keep the vertices of it
 class MyEdge {
int s;
int t;
//int sp;
	public MyEdge(int ss,int tt)
	{
		s=ss;
		t=tt;
		//sp=-1;
	}
}
