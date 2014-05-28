import java.util.ArrayList;

class Node {

	// The name of the node
	public String name;

	// The parent nodes
	public Node[] parents;
	
	// The index of the node in training file
	public int index;
	
	//counters for node when training
	public int[] count;

	// The probabilities for the CPT
	public double[] probs;

	// The current value of the node
	public boolean value;

	/**
	 * Initialize an empty node
	 * 
	 * @param n Name of the node
	 */
	Node(String n) {
		name = n;
	}

	/**
	 * Returns conditional probability of value "true" for the current node
	 * based on the values of the parent nodes.
	 * 
	 * @return The conditional probability of this node, given its parents.
	 */
	public double conditionalProbability() {

		int index = 0;
		for (int i = 0; i < parents.length; i++) {
			if (parents[i].value) {
				index += Math.pow(2, parents.length - i - 1);
			}
		}
		return probs[index];
	}
	
	/**
	 * Exactly infers the probability of a nodes value based on probabilities of all nodes. We have no unknown nodes so no summed over hidden variables required
	 * 
	 * @param network The current state of network
	 * @return The probability of this node being true
	 */
	public double exactInference(ArrayList<Node> network) {
		
		//variables to hold probabilities
		double trueProb = 1;
		double falseProb = 1;
		for(Node n : network){
			value = true; //set to true
			double nodeProb = n.conditionalProbability(); //find n's probability
			trueProb *= n.value? nodeProb : 1 - nodeProb; //time true prob by node prob or 1 - node prob, depending on nodes value
			value = false; //do the same for false
			nodeProb = n.conditionalProbability();
			falseProb *= n.value? nodeProb : 1 - nodeProb;
		}		
		
		//normalize true prob
		return (trueProb / (trueProb + falseProb));
	}

	//prints the current node and children
	public String toString(){
		String value = this.name + ": ";
		if(parents.length > 0) {
			for (Node n : parents){
				value += n.name + " ";
			}
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}
	
	//tests is a node has parents
	public boolean hasParents(){
		return parents.length > 0;
	}
}