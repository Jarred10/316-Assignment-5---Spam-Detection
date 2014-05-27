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
	private boolean value;

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
	private double conditionalProbability() {

		int index = 0;
		for (int i = 0; i < parents.length; i++) {
			if (parents[i].value == false) {
				index += Math.pow(2, parents.length - i - 1);
			}
		}
		return probs[index];
	}

	public String toString(){
		String value = this.name + ": ";
		if(parents.length > 0) {
			for (Node n : parents){
				value += n.name + " ";
			}
			value = value.substring(0, value.length() - 1);
		}
		value += " count size: " + count.length;
		return value;
	}
	
	public boolean hasParents(){
		return parents.length > 0;
	}
}