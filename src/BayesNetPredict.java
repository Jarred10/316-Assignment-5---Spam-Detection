import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


public class BayesNetPredict {

	static Scanner s;
	
	public static void main(String[] args) throws FileNotFoundException {

		//creates the network from the file passed in as first argument, the layout of the network
		ArrayList<Node> network = createNetwork(args[0]);

		//trains the network based on the file passed in as second argument, the training data
		trainNetwork(args[1], network);

		//tests the network based on the file passed in as third argument, the testing data
		testNetwork(args[2], network);
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

	public static void testNetwork(String testingFilePath, ArrayList<Node> network) throws FileNotFoundException {

		PrintStream stdout = System.out; //prints to a file
		System.setOut(new PrintStream("completedTest.csv"));

		s = new Scanner(new File(testingFilePath));
		String headers[] = s.nextLine().split(",");
		
		//sets the indexes for nodes in the network based on testing data headers
		for(int i = 0; i < headers.length; i++){
			Node n;
			if((n = findNode(headers[i], network)) != null)
				n.index = i;
			System.out.print(headers[i] + ", ");
		}
		System.out.println();

		//assumes there is only one unknown column in whole file
		Node unknown = null;
		
		while(s.hasNextLine()){
			String values[] = s.nextLine().split(",");
			for(int i = 0; i < network.size(); i++){
				Node nd = network.get(i);
				//if we havent found the unknown yet and the value of the current node is unknown, set unknown to current node
				if(unknown == null && values[nd.index].equals("?")) unknown = nd;
				else {
					//set node value to true if the values at the nodes index is 1, else false
					nd.value = (values[nd.index].equals("1"));
				}
			}
			
			//unknown nodes value is equal to condition: 
			//is the probability exactly inferred from network for unknown node is not less than half
			unknown.value = !(unknown.exactInference(network) < 0.5);
			
			//prints all the headers and the unknown value as our inferred value
			for(int i = 0; i < headers.length; i++){
				if(unknown.index == i){
					System.out.print((unknown.value? 1 : 0) + ", ");
				}
				else System.out.print(values[i] + ", ");
			}
			System.out.println();
		}
		System.setOut(stdout);
	}

	/**
	 * 	finds a node with a given name in the network
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

}
