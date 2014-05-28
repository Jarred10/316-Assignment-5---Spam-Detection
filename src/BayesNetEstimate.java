import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class BayesNetEstimate {

	static Scanner s;

	public static void main(String[] args) throws FileNotFoundException {

		//creates the network from the file passed in as first argument
		ArrayList<Node> network = createNetwork(args[0]);

		//trains the network based on the file passed in as second argument
		trainNetwork(args[1], network);

		//prints the probabilities in given format to output.csv

		// Node
		// Parent1, Parent2, ... ParentN
		// 0, 0, ... 0, P(N|-P1 -P2)
		// 0, 1, P(N|-P1 P2)
		// 1, 0, P(N|P1 -P2)
		// 1, 1, ... 1, P(N|P1 P2)

		PrintStream stdout = System.out;
		System.setOut(new PrintStream("output.csv"));
		printProbabilities(network);
		System.setOut(stdout);
	}

	/**
	 * 		generates a bayesian network from a file
	 * 
	 * @param networkFilePath 
	 * 		a string pointing to the file on the system
	 * @return 
	 * 		the arraylist of network representing the network
	 * @throws FileNotFoundException
	 */
	public static ArrayList<Node> createNetwork(String networkFilePath) throws FileNotFoundException {
	
		s = new Scanner(new File(networkFilePath));
		ArrayList<Node> network = new ArrayList<Node>();
		
		while(s.hasNextLine()){
			String[] line = s.nextLine().split(" ");
			//creates new node, passing in string at first index of line array, with comma taken out
			Node n = new Node(line[0].substring(0, line[0].length() - 1));
			network.add(n); //adds node to network in order
			n.parents = new Node[line.length - 1]; //makes all parents for node, using rest of line
			for(int i = 0; i < n.parents.length; i++){
				n.parents[i] = findNode(line[i+1], network);
			}
			//create array large enough to count true and false for each child and the node itself
			n.count = new int[(int) Math.pow(2, n.parents.length + 1)];
			Arrays.fill(n.count, 1); //starts counts at 1 to avoid zero frequency problem
		}
		return network;
	}

	public static void trainNetwork(String trainingFilePath, ArrayList<Node> network) throws FileNotFoundException {
		s = new Scanner(new File(trainingFilePath));
		String headers[] = s.nextLine().split(",");
	
		//sets all the indexes of the network in our network using headers read in and there respective index
		for(int i = 0; i < headers.length; i++){
			Node n;
			if((n = findNode(headers[i], network)) != null)
				n.index = i;
		}
		
		//for all training data
		while(s.hasNextLine()){
			String[] line = s.nextLine().split(",");
			//for all nodes, read the value of their parents and create the mask based on parents values
			for(Node n : network){
				int parentMask = 0;
				for(int i = 0; i < n.parents.length; i++){
					parentMask = parentMask << 1; //bitshift the mask over 1
					if(line[n.parents[i].index].equals("1"))
						parentMask += 1; //if parent was true, set the bit
				}
				//increase true count
				if(line[n.index].equals("1"))
					n.count[parentMask]++;
				//else increase false count, the mask for this has the most significant bit set
				else{
					parentMask += (int) Math.pow(2, n.parents.length);
					n.count[parentMask]++;
				}
			}
		}
	
		//set probabilities
		for(Node n : network){
			n.probs = new double[(int) Math.pow(2, n.parents.length)];
			for(int i = 0; i < n.probs.length; i++)
				//probability is equal to true count over true plus false count
				n.probs[i] = (double)n.count[i] / (double)(n.count[i] + n.count[i + (int)Math.pow(2, n.parents.length)]);
		}
	}

	/**
	 * 		finds a node with a given name in the network
	 * 
	 * @param name
	 * 		the name of node to find
	 * @param network
	 * 		the arraylist containing all network in the netowrk
	 * @return
	 * 		the pointer to node if found, else null
	 */
	public static Node findNode(String name, ArrayList<Node> network){
		for(Node n : network){
			if(n.name.equals(name)) return n;
		}
		return null;
	}

	public static void printProbabilities(ArrayList<Node> network){
		for(Node n : network){
			//prints name of node
			System.out.println(n.name + ": ");

			if(n.hasParents()){
				String parents = "";
				//prints parents of node
				for(Node p : n.parents){
					parents += (p.name + " ");
				}
				//spacing
				System.out.println(parents);
			}

			//mask to use to index into probabilies, also printed before probility
			int mask = 0;
			while(mask < (int) Math.pow(2, n.parents.length)){

				//loops through bits from (length of parents - 1) to 0, print value of i at that bit
				int j = n.parents.length - 1;
				while(j >= 0){
					System.out.print(((mask >>> j) & 1) + ", ");
					j--;
				}
				System.out.println(n.probs[mask]);
				mask++;
			}
			System.out.println();
		}

	}
}
