package me.najclark.gll.test;

import me.najclark.gll.ga.GeneticAlgorithm;
import me.najclark.gll.ga.Individual;
import me.najclark.gll.nn.NeuralNetwork;
import me.najclark.gll.nn.Neuron;

public class PopulationTest extends GeneticAlgorithm{

	public PopulationTest(NeuralNetwork base, double mutateRate, int size) {
		super.initialize();
		for(int i = 0; i < size; i++){
			pool.add(new Individual(0, new NeuralNetwork(base), Individual.generateName()));
		}
	}

	@Override
	public double simulate(NeuralNetwork nn) {
		Neuron[] inputs = new Neuron[]{new Neuron(0), new Neuron(1)};
		nn.setInputs(inputs);
		
		nn.flush();
		
		double output = nn.getOutputs().getNeuron(0).getValue();
		return 100-output;
	}

	@Override
	public void clearStats() {
		// TODO Auto-generated method stub
		
	}

}
