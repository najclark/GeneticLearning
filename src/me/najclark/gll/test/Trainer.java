package me.najclark.gll.test;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import me.najclark.gll.ga.NNGA;
import me.najclark.gll.ga.Phenotype;
import me.najclark.gll.nn.ActivationFunction;
import me.najclark.gll.nn.Layer;
import me.najclark.gll.nn.NetworkPicture;
import me.najclark.gll.nn.NeuralNetwork;
import me.najclark.gll.nn.Neuron;

public class Trainer extends NNGA {

	public static void main(String[] args) {
		new Trainer().run();
	}

	public void run() {
		NeuralNetwork base = new NeuralNetwork();
		base.addLayer(new Layer(5, ActivationFunction.sigmoid));
		base.addLayer(new Layer(7, ActivationFunction.sigmoid));
		base.addLayer(new Layer(1, ActivationFunction.linear));

		base.makeWeightGroups();
		initialize(0.01);
		setIsMultiThreaded(true);

		for (int i = 0; i < 100; i++) {
			NeuralNetwork nn = new NeuralNetwork(base);
			nn.makeWeightGroups();

			Phenotype ind = new Phenotype(0.0, nn, Phenotype.generateName());
			pool.add(ind);
		}

		NetworkPicture np = new NetworkPicture(base);
		np.setNodes(Color.blue);
		np.setLines(Color.blue);
		np.setNegativeLines(Color.red);

		JFrame frame = new JFrame();
		frame.setSize(1000, 800);
		frame.getContentPane().setLayout(new FlowLayout());
		JLabel label = new JLabel(new ImageIcon(np.getNetworkImage(frame.getWidth(), frame.getHeight(), 60)));
		frame.getContentPane().add(label);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		while (avgFitness < 95 && generation < 1000) {
			selection();

			Phenotype ind = getBestIndividual();
			np.update((NeuralNetwork)ind.gt);
			BufferedImage img = np.getNetworkImage(frame.getWidth(), frame.getHeight(), 60);
			Graphics g = img.getGraphics();
			g.setColor(Color.black);
			g.drawString(output, 50, 25);
			// g.drawString("Best Fitness: " + ind.fitness + " / " +
			// pop.getMaxTiedFitness(), 50, 50);
			g.drawString("Best Individual Name: " + ind.name, 50, 50);
			label.setIcon(new ImageIcon(img));
			// System.out.println(this.avgFitness);

			ArrayList<String> startingLetters = new ArrayList<String>();
			for (Phenotype i : getPopulation()) {
				startingLetters.add(i.name.substring(0, 1));
			}
			g.drawString("Highest Starting Letter: " + calculateElectionWinner(startingLetters), 50, 75);

			makeNewGeneration();
		}
		System.out.println("done");
	}

	@Override
	public void clearStats() {
	}

	@Override
	public void makeNewGeneration() {
		ArrayList<Phenotype> newPool = new ArrayList<Phenotype>();

		populateMatingPool(pool);
		for (int i = 0; i < pool.size() * 0.5; i++) {
			Phenotype p1 = pickParent(null, 0);
			Phenotype p2 = pickParent(p1, 0);
			Phenotype crossed = crossover(p1, p2);
			Phenotype mutated = mutate(crossed, mutateRate);
			newPool.add(mutated);
		}
		newPool.addAll(getHighestHalf(pool));
		avgFitness = 0;
		avgNeurons = 0;
		for (int i = 0; i < pool.size(); i++) {
			avgFitness += pool.get(i).fitness;
			avgNeurons += ((NeuralNetwork)pool.get(i).gt).getTotalNeurons();
		}

		avgFitness /= pool.size();
		avgNeurons /= pool.size();
		output = "Generation: " + generation + ". Average Fitness: " + avgFitness + ". Average Neurons: " + avgNeurons;

		pool.clear();
		pool.addAll(newPool);

		generation++;
	}

	@Override
	public double simulate(NeuralNetwork nn) {
		double avgDist = 0;
		for (int a = 0; a < 3; a++) {
			nn.clear();

			Layer input = new Layer(nn.getLayer(0).size());
			input.setNeuron(0, new Neuron(1));
			for (int i = 1; i < input.size(); i++) {
				 input.setNeuron(i, new Neuron(random.nextInt(10) - 5));
				//input.setNeuron(i, new Neuron(i + 1));
			}

			nn.setInputs(input);

			nn.flush();

			avgDist += Math.abs(1 - nn.getOutputs().getNeuron(0).getValue());
		}
		avgDist /= 3;
		return (1 / (Math.pow(10, avgDist))) * 100;
	}

	public String calculateElectionWinner(ArrayList<String> votes) {
		HashMap<String, Integer> people = new HashMap<String, Integer>();

		for (String s : votes) {
			s = s.toLowerCase();
			if (people.containsKey(s)) {
				people.put(s, people.get(s) + 1);
			} else {
				people.put(s, 1);
			}
		}

		int largest = 0;
		String key = "";
		for (String p : people.keySet()) {
			if (people.get(p) > largest) {
				key = p;
				largest = people.get(p);
			} else if (people.get(p) == largest)
				key += ", " + p;
		}

		return key;
	}

}
