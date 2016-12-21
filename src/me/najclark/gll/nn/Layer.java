package me.najclark.gll.nn;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Layer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7915683105488066923L;
	private ArrayList<Neuron> neurons;
	
	@Override
	public String toString(){
		String output = "{";
		for(Neuron n : neurons){
			output += n + ", ";
		}
		output += "}";
		return output;
	}
	
	public Neuron getHighest() {
		Neuron highest = new Neuron(Double.MIN_VALUE);
		for (Neuron n : neurons) {
			if (n.getValue() > n.getValue()) {
				highest = n;
			}
		}
		return highest;
	}

	public Neuron getLowest() {
		Neuron lowest = new Neuron(Double.MAX_VALUE);
		for (Neuron n : neurons) {
			if (n.getValue() < n.getValue()) {
				lowest = n;
			}
		}
		return lowest;
	}

	public ArrayList<Integer> getHighest2Lowest() {
		ArrayList<Neuron> copy = new ArrayList<Neuron>();
		copy.addAll(neurons);
		Collections.sort(copy);

		ArrayList<Integer> sorted = new ArrayList<Integer>();
		for (Neuron n : copy) {
			sorted.add(neurons.indexOf(n));
		}

		return sorted;
	}
	
	public void addNeuron(Neuron n){
		neurons.add(n);
	}
	
	/**
	 * {@code public Layer(int neurons)}
	 * @param neurons - The number of neurons in this layer.
	 */
	public Layer(int neurons){
		this.neurons = new ArrayList<Neuron>(neurons);
		for(int i = 0; i < neurons; i++){
			this.neurons.add(new Neuron());
		}
	}
	
	public Layer(){
		this(0);
	}
	
	public Layer(int neurons, ActivationFunction af){
		this.neurons = new ArrayList<Neuron>(neurons);
		for(int i = 0; i < neurons; i++){
			this.neurons.add(new Neuron(af));
		}
	}
	
	/**
	 * {@code public int size()}
	 * @return The number of Neurons in the layer.
	 */
	public int size(){
		return neurons.size();
	}
	
	/**
	 * {@code public Neuron[] getNeurons()}
	 * @return An array of Neurons in the layer.
	 */
	public Neuron[] getNeurons(){
		return neurons.toArray(new Neuron[neurons.size()]);
	}
	
	/**
	 * {@code public Neuron getNeuron(int index)}
	 * @param index - index of the Neuron to return
	 * @return The Neuron at a given index.
	 */
	public Neuron getNeuron(int index){
		return neurons.get(index);
	}
	
	/**
	 * {@code public void setNeuron(int index, Neuron n)}
	 * @param index - index of the Neuron to replace.
	 * @param n - the Neuron to replace the existing Neuron.
	 */
	public void setNeuron(int index, Neuron n){
		neurons.set(index, n);
	}
	
	
}