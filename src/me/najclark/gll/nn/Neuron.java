package me.najclark.gll.nn;
import java.io.Serializable;

public class Neuron implements Serializable, Comparable<Neuron>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -798726926214283987L;
	private double input;
	
	@Override
	public String toString(){
		return "(" + input + ")";
	}
	
	public double getValue(){
		return input;
	}
	
	/**
	 * {@code public Neuron(double input)}
	 * @param input - the input of the Neuron.
	 */
	public Neuron(double input){
		this.input = input;
	}
	
	/**
	 * {@code public Neuron()}
	 */
	public Neuron(){
		this.input = 0;
	}
	
	/**
	 * {@code public void setInput()}
	 * @param input - the input of the Neuron.
	 */
	public void setInput(double input){
		this.input = input;
	}
	
	/**
	 * {@code public double getInput()}
	 * @return The given input of the Neuron.
	 */
	public double getInput(){
		return input;
	}

	/**
	 * {@code public double getOutput()}
	 * @return The result of the sigmoid function, based on the Neuron's set input.
	 */
	public double getOutput(){
		return (1/( 1 + Math.pow(Math.E,(-1*input))));
	}

	@Override
	public int compareTo(Neuron o) {
		return Double.compare(input, o.getValue());
	}
	
}