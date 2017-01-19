package me.najclark.gll.nn;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class NeuralNetwork implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1676836486914368417L;
	private ArrayList<Layer> layers;
	private ArrayList<WeightGroup> weights;

	public void clear() {
		for (Layer l : layers) {
			for (Neuron n : l.getNeurons()) {
				n.setInput(0);
			}
		}
	}

	public void setLayer(int index, Layer l) {
		layers.set(index, l);
	}

	public Layer getLayer(int index) {
		return layers.get(index);
	}

	public int getTotalNeurons() {
		int total = 0;
		for (Layer l : layers) {
			total += l.getNeurons().length;
		}
		return total;
	}

	public void removeLayer(int index) {
		layers.remove(index);
	}

	public void addLayerAt(int index, Layer l) {
		layers.add(index, l);
	}

	public NeuralNetwork(NeuralNetwork copy) {
		this();
		for (Layer l : copy.getLayers()) {
			Layer newLayer = new Layer(l.getNeurons().length);
			int i = 0;
			for (Neuron n : l.getNeurons()) {
				Neuron newN = new Neuron(n.getInput(), n.getActivationFunction());
				newLayer.setNeuron(i, newN);
				i++;
			}
			addLayer(newLayer);
		}
		makeWeightGroups();
	}

	@Override
	public String toString() {
		String output = "[";
		for (Layer l : layers) {
			output += l + ", ";
		}
		output += "]";
		return output;
	}

	public String getId() {
		return super.toString();
	}

	public void setWeightGroup(int index, WeightGroup wg) {
		weights.set(index, wg);
	}

	/**
	 * {@code public boolean saveNN(String path)}
	 * 
	 * @param path
	 *            - The path to where the NeuralNetwork will be saved.
	 * @return Whether or not the save was successful.
	 */
	public boolean saveNN(String path) {
		try {
			if (!path.contains(".nn")) {
				path += ".nn";
			}
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			return true;
		} catch (IOException i) {
			return false;
		}
	}

	/**
	 * {@code public static NeuralNetwork openNN(String path)}
	 * 
	 * @param path
	 *            - The path to the saved NeuralNetwork.
	 * @return The instance of the saved NeuralNetwork.
	 */
	public static NeuralNetwork openNN(String path) {
		NeuralNetwork nn = null;
		try {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			nn = (NeuralNetwork) in.readObject();
			in.close();
			fileIn.close();
			return nn;
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return null;
		}
	}

	/**
	 * {@code public ArrayList<Layer> getLayers()}
	 * 
	 * @return The ArrayList of layers.
	 */
	public ArrayList<Layer> getLayers() {
		return layers;
	}

	/**
	 * {@code public ArrayList<WeightGroup> getWeightGroups()}
	 * 
	 * @return The ArrayList of the WeightGroups.
	 */
	public ArrayList<WeightGroup> getWeightGroups() {
		return weights;
	}

	/**
	 * {@code public void setLayers(ArrayList<Layer> layers)}
	 * 
	 * @param layers
	 *            - the new ArrayList of Layers.
	 */
	public void setLayers(ArrayList<Layer> layers) {
		this.layers = layers;
	}

	/**
	 * {@code public void setWeightGroups(ArrayList<WeightGroup> weights)}
	 * 
	 * @param weights
	 *            - the new ArrayList of WeightGroups.
	 */
	public void setWeightGroups(ArrayList<WeightGroup> weights) {
		this.weights = weights;
	}

	public NeuralNetwork() {
		layers = new ArrayList<Layer>();
		weights = new ArrayList<WeightGroup>();
	}

	/**
	 * {@code public void addLayer(Layer layer)}
	 * 
	 * @param layer
	 *            - Layer to be added to the end of the neural network.
	 */
	public void addLayer(Layer layer) {
		layers.add(layer);
	}

	/**
	 * {@code public void makeWeightGroups()} <br>
	 * <br>
	 * Makes WeightGroups based on the existing layers.
	 * 
	 * @throws Exception
	 *             Cannot create weight groups for only 1 layer.
	 */
	public void makeWeightGroups() {
		if (layers.size() < 2) {
			try {
				throw new Exception("Cannot create weight groups for 1 layer.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < layers.size() - 1; i++) {
				weights.add(WeightGroup.connectLayers(layers.get(i), layers.get(i + 1)));
			}
		}
	}

	/**
	 * {@code public void setInputs(Layer l)}
	 * 
	 * @param l
	 *            - the Layer containing the inputs for the neural network.
	 * @throws ArrayIndexOutOfBoundsException
	 *             Thrown when the number of inputs and first layer neurons do
	 *             not match.
	 */
	public void setInputs(Layer l) {
		Layer first = layers.get(0);
		if (l.size() != first.size()) {
			throw new ArrayIndexOutOfBoundsException("The number of inputs and first " + "layer neurons don't match");
		} else {
			int i = 0;
			for(Neuron n : first.getNeurons()){
				n.setInput(l.getNeuron(i).getInput());
			}
			layers.set(0, first);
		}
	}

	/**
	 * {@code public void flush()} <br>
	 * <br>
	 * Runs the given inputs through the neural network.
	 */
	public void flush() {
		// iterate through the layers (not the last one)
		for (int i = 0; i < layers.size() - 1; i++) {
			Layer current = layers.get(i);
			Layer next = layers.get(i + 1);
			// iterate through the Neurons in the next layer
			for (int neuronNum = 0; neuronNum < next.size(); neuronNum++) {
				// iterate through the weights associated with that Neuron
				for (int weight = neuronNum * current.size(); weight < (neuronNum * current.size())
						+ current.size(); weight++) {
					// current value of the Neuron
					double neuronCur = next.getNeuron(neuronNum).getOutput();
					// the output of Neuron in the current layer associated with
					// the current weight
					double mult = current.getNeuron(weight % current.size()).getOutput();
					if (i == 0) { // input layer (no sigmoid activation)
						neuronCur = next.getNeuron(neuronNum).getInput();
						mult = current.getNeuron(weight % current.size()).getInput();
					}
					// sets the Neuron's input to the existing input + the new
					// input
					System.out.println(neuronNum);
					next.setNeuron(neuronNum, new Neuron(neuronCur + (weights.get(i).getWeights()[weight] * mult),
							next.getNeuron(neuronNum).getActivationFunction()));
				}
			}
		}
	}

	/**
	 * {@code public Layer getOutputs()}
	 * 
	 * @return The output layer.
	 */
	public Layer getOutputs() {
		return layers.get(layers.size() - 1);
	}

	public void setInputs(Neuron[] inputs) {
		Layer first = layers.get(0);
		if (inputs.length != first.size()) {
			throw new ArrayIndexOutOfBoundsException("The number of inputs and first " + "layer neurons don't match");
		} else {
			for (int i = 0; i < inputs.length; i++) {
				Neuron n = first.getNeuron(i);
				n.setInput(inputs[i].getInput());
				first.setNeuron(i, n);
			}
			layers.set(0, first);
		}
	}

}