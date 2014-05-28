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
	 * Initializes the node with empty parents array
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
	
	public double exactInference(ArrayList<Node> network) {
		
		double trueProb = 1;
		double falseProb = 1;
		for(Node n : network){
			value = true;
			double nodeProb = n.conditionalProbability();
			trueProb *= n.value? nodeProb : 1 - nodeProb;
			value = false;
			nodeProb = n.conditionalProbability();
			falseProb *= n.value? nodeProb : 1 - nodeProb;
		}		
		
		return (trueProb / (trueProb + falseProb));
	}

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
	
	public boolean hasParents(){
		return parents.length > 0;
	}
}