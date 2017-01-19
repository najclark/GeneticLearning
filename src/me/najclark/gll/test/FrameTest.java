package me.najclark.gll.test;

import me.najclark.gll.ga.GeneticFrame;
import me.najclark.gll.nn.Layer;
import me.najclark.gll.nn.NeuralNetwork;

public class FrameTest {

	public static void main(String[] args) {
		new FrameTest().run();
	}

	public void run() {
		NeuralNetwork base = new NeuralNetwork();
		base.addLayer(new Layer(2));
		base.addLayer(new Layer(2));
		base.addLayer(new Layer(1));
		
		base.makeWeightGroups();
		GeneticFrame frame = new GeneticFrame("Frame Test", 1000, 800);
		PopulationTest pop = new PopulationTest(base, 0.01, 100);
		pop.setIsMultiThreaded(false);

		while (pop.getAvgFitness() < 100) {
			pop.runGeneration();
			frame.update(pop);
		}
	}

}
