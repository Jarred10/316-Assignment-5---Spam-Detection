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
			Node n = new Node(line[0].substring(0, line[0].length() - 1));
			network.add(n);
			n.parents = new Node[line.length - 1];
			for(int i = 0; i < n.parents.length; i++){
				n.parents[i] = findNode(line[i+1], network);
			}
			n.count = new int[(int) Math.pow(2, n.parents.length + 1)];
			Arrays.fill(n.count, 1);
		}
		return network;
	}

	public static void trainNetwork(String trainingFilePath, ArrayList<Node> network) throws FileNotFoundException {
		s = new Scanner(new File(trainingFilePath));
		String headers[] = s.nextLine().split(",");

		//sets all the indexes of the network in our network
		for(int i = 0; i < headers.length; i++){
			Node n;
			if((n = findNode(headers[i], network)) != null)
				n.index = i;
		}

		while(s.hasNextLine()){
			String[] line = s.nextLine().split(",");
			for(Node n : network){
				int b = 0;
				for(int i = 0; i < n.parents.length; i++){
					b = b << 1;
					if(line[n.parents[i].index].equals("1"))
						b += 1;
				}
				//increase true count
				if(line[n.index].equals("1"))
					n.count[b]++;
				//else increase false count
				else{
					b += (int) Math.pow(2, n.parents.length);
					n.count[b]++;
				}
			}
		}

		//set probabilities
		for(Node n : network){
			n.probs = new double[(int) Math.pow(2, n.parents.length)];
			for(int i = 0; i < n.probs.length; i++)
				n.probs[i] = (double)n.count[i] / (double)(n.count[i] + n.count[i + (int)Math.pow(2, n.parents.length)]);
		}
	}

	public static void testNetwork(String testingFilePath, ArrayList<Node> network) throws FileNotFoundException {

		PrintStream stdout = System.out;
		System.setOut(new PrintStream("completedTest.csv"));

		s = new Scanner(new File(testingFilePath));
		String headers[] = s.nextLine().split(",");

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
				Node n = network.get(i);
				if(unknown == null && values[n.index].equals("?")) unknown = n;
				else {
					n.value = (values[n.index].equals("1"));
				}
			}
			
			//if the probability exactly inferred from network for unknown node is less than half
			if(unknown.exactInference(network) < 0.5) 
				unknown.value = false; //set unknown node to false
			else unknown.value = true; //else set to true
			

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

}
