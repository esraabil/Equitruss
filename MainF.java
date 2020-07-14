/* Esra Akbas
main file to run the program
*/
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class MainF {
	public static void main(String[] args) throws IOException {
		String fileName;// graph edge list file 
		String pathtec;// index files folder path
		int c;// c=1 will create index for graph, otherwise it will upload index  from files in given folder pathtec
		if (args.length == 3) {
			c = Integer.parseInt(args[0]);
			fileName = args[1];
			pathtec = args[2];
		} else {
			c =1;
			System.out.println("path for graph file and path for index files is not given. Toy graph will be used");
			fileName = "data/toyg.txt";
			pathtec = "toy";
		}

		TecIndexG tec = new TecIndexSB();
//		System.out.println(fileName);
		MyGraph mg = new MyGraph();
		/*** Read Graph ***/
//		System.out.println("start");
		long startTime = System.nanoTime();
		mg.read_GraphEdgelist(fileName);

		long endTime = System.nanoTime();
		System.out.print("graph read time: ");
		System.out.println((endTime - startTime) / 1000000000.00);
		if (c == 1) {
			Map<Integer, LinkedHashSet<MyEdge>> klistdict;
			Map<MyEdge, Integer> trussd = new HashMap<MyEdge, Integer>();
			createDir(pathtec);

			/**** Truss Decomposition ****/
			startTime = System.nanoTime();
			klistdict = mg.computeTruss(pathtec, trussd);
			endTime = System.nanoTime();
			System.out.print("truss computation time: ");
			System.out.println((endTime - startTime) / 1000000000.00);
			mg.write_support(pathtec + "/truss.txt", trussd);
			/**** Create Index ****/
			startTime = System.nanoTime();
			tec.constructIndex(klistdict, trussd, mg);
			endTime = System.nanoTime();
			System.out.print("Index for given graph is created. Index creation time: ");
			System.out.println((endTime - startTime) / 1000000000.00);
			tec.writeIndex(pathtec);
		} else {
			File theDir = new File(pathtec);
			if (!theDir.exists()) {
				System.out.println("given path for index files does not exists");
				System.exit(0);
			}
			tec.read_Indexlj(mg, pathtec);
			System.out.println("Index files are read and index is created ");
			test(tec);
		}

	}

	public static void test(TecIndexG tec) throws IOException {
		Scanner s = new Scanner(System.in);
		System.out.println("enter query node id and k:truss value");
		Integer query = Integer.parseInt(s.next());
		do {
			int k = s.nextInt();
			long startTime = System.nanoTime();
			LinkedList<LinkedList<MyEdge>> com = tec.findkCommunityForQuery(query, k);
			long endTime = System.nanoTime();

			if (com.size() == 0) {
				System.out.println("There is no community for this query with given truss value");
			} else {
				for (LinkedList<MyEdge> c : com) {
					for (MyEdge e : c) {
						System.out.print("(" + e.s + "," + e.t + "), ");
					}
					System.out.println();
				}
			}
			System.out.print("query time:");
			System.out.println((endTime - startTime) / 1000000000.00);
			System.out.println("\n\nenter query node id and  k truss value, -1 to exit");
			query = Integer.parseInt(s.next());
		} while (query != -1);
		s.close();
	}

	public static void createDir(String path) {
		File theDir = new File(path);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			System.out.println("creating directory: " + path);
			boolean result = false;

			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				System.out.println("there is a problem with creating directory");
			}
		}
	}

}
