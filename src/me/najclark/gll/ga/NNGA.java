package me.najclark.gll.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import me.najclark.gll.nn.ActivationFunction;
import me.najclark.gll.nn.Layer;
import me.najclark.gll.nn.NeuralNetwork;
import me.najclark.gll.nn.Neuron;
import me.najclark.gll.nn.WeightGroup;

public abstract class NNGA{

	private ArrayList<Phenotype> matingPool = new ArrayList<Phenotype>();
	protected ArrayList<Phenotype> pool;
	protected Random random = new Random();
	protected int maxFitness = 0;
	protected double mutateRate = 0.01;
	protected int generation = 0;
	protected double avgFitness = 0;
	protected double avgNeurons = 0;
	protected String output = "";
	protected boolean specialMutation = true;
	protected boolean isMultiThreaded = true;

	public void initialize(double mutateRate) {
		pool = new ArrayList<Phenotype>();
		this.mutateRate = mutateRate;
	}

	public void initialize() {
		initialize(mutateRate);
	}

	public Phenotype mutate(Phenotype ind, double mutateRate) {
		NeuralNetwork nn = (NeuralNetwork) ind.gt;
		NeuralNetwork mutated = new NeuralNetwork(nn);
		mutated.makeWeightGroups();
		boolean changed = false;

		for (int i = 0; i < nn.getWeightGroups().size(); i++) {
			WeightGroup wg = mutated.getWeightGroups().get(i);
			for (int d = 0; d < wg.getWeights().length; d++) {
				if (random.nextDouble() < mutateRate / nn.getTotalNeurons()) {
					changed = true;
					wg.setWeight(d, (random.nextDouble() - 0.5));
				} else {
					wg.setWeight(d, nn.getWeightGroups().get(i).getWeights()[d]);
				}
			}
			mutated.setWeightGroup(i, wg);
		}
		if (random.nextDouble() < mutateRate && specialMutation) { // special
																	// mutation
			changed = true;
			if (random.nextDouble() < mutateRate / 10) { // Add or remove Layer
															// to
				// NeuralNetwork
				NeuralNetwork changedDesign = new NeuralNetwork(nn);
				int index;
				if (nn.getLayers().size() <= 2) {
					index = 1;
				} else {
					index = random.nextInt(nn.getLayers().size() - 2) + 1;
				}
				if (random.nextBoolean() || nn.getLayers().size() == 2) { // Add
																			// a
																			// layer
					int neurons = nn.getTotalNeurons() / nn.getLayers().size();
					changedDesign.addLayerAt(index, new Layer(neurons));
				} else { // Remove a layer
					changedDesign.removeLayer(index);
				}
				changedDesign.makeWeightGroups();
				return new Phenotype(0, changedDesign, ind.name);
			} else { // Add or remove a neuron from a layer
				NeuralNetwork changedDesign = new NeuralNetwork(nn);
				if (nn.getLayers().size() > 2) {

					int changeLayer = random.nextInt(nn.getLayers().size() - 2) + 1;

					Layer l = nn.getLayers().get(changeLayer);
					int deltaNeurons = pickMutationLevel();
					if (random.nextBoolean()) {
						for (int i = 0; i < deltaNeurons; i++) {
							l.addNeuron(new Neuron(ActivationFunction.sigmoid));
						}
					} else {
						l = new Layer();
						for (int i = 0; i < Math.max(1,
								nn.getLayers().get(changeLayer).getNeurons().length - deltaNeurons); i++) {
							l.addNeuron(nn.getLayers().get(changeLayer).getNeuron(i));
						}
					}

					changedDesign.setLayer(changeLayer, l);
					changedDesign.makeWeightGroups();

					for (int w = 0; w < nn.getWeightGroups().size(); w++) {
						WeightGroup wg = nn.getWeightGroups().get(w);
						WeightGroup newWg = changedDesign.getWeightGroups().get(w);
						int index = 0;
						while (index < wg.getWeights().length && index < newWg.getWeights().length) {
							newWg.setWeight(index, wg.getWeight(index));
							index++;
						}
						changedDesign.setWeightGroup(w, newWg);
					}
					return new Phenotype(0, changedDesign, ind.name);
				}
			}
		}

		String mutatedName = ind.name;
		if (changed) {
			mutatedName = Phenotype.mutateName(ind.name, 1, Phenotype.SKIP_FIRST);
		}
		return new Phenotype(0, mutated, mutatedName);
	}

	private int pickMutationLevel() {
		int[] bin = { 1, 1, 1, 1, 1, 1, 2, 2, 2, 3 }; // 1 60%, 2 30%, 3 10%
		return bin[random.nextInt(bin.length)];
	}

	public Phenotype crossover(Phenotype ind, Phenotype ind2) {
		NeuralNetwork nn = (NeuralNetwork) ind.gt;
		NeuralNetwork nn2 = (NeuralNetwork) ind2.gt;
		NeuralNetwork crossed;
		NeuralNetwork copied;
		if (random.nextBoolean()) {
			crossed = new NeuralNetwork(nn);
			copied = nn;
		} else {
			crossed = new NeuralNetwork(nn2);
			copied = nn2;
		}

		crossed.makeWeightGroups();
		crossed.setWeightGroups(copied.getWeightGroups());

		int i = 0;
		while (i < nn.getWeightGroups().size() && i < nn2.getWeightGroups().size()) {
			WeightGroup wg = nn.getWeightGroups().get(i);
			WeightGroup wg2 = nn2.getWeightGroups().get(i);
			WeightGroup newWg = crossed.getWeightGroups().get(i);

			if (wg.getWeights().length == newWg.getWeights().length) {
				newWg.setWeights(wg.getWeights());
			} else {
				newWg.setWeights(wg2.getWeights());
			}

			int index = 0;
			while (index < wg.getWeights().length && index < wg2.getWeights().length) {
				if (random.nextBoolean()) {
					newWg.setWeight(index, wg.getWeight(index));
				} else {
					newWg.setWeight(index, wg2.getWeight(index));
				}
				index++;
			}
			crossed.setWeightGroup(i, newWg);
			i++;
		}
		return new Phenotype(0, crossed, Phenotype.mixNames(ind.name, ind2.name));
	}

	public NeuralNetwork acceptReject(ArrayList<Phenotype> pool, int maxFitness) {
		Random random = new Random();
		int besafe = 0;
		while (besafe < 10000) {
			int index = random.nextInt(pool.size());
			Phenotype partner = pool.get(index);
			int r = random.nextInt(maxFitness);
			double key = partner.fitness;

			if (r < key) {
				return (NeuralNetwork) partner.gt;
			}
			besafe++;
		}
		return (NeuralNetwork) pool.get(0).gt;

	}

	public Phenotype pickParent(Phenotype not, int iteration) {
		// System.out.println(matingPool.size());
		Phenotype parent = matingPool.get(random.nextInt(matingPool.size()));
		if (iteration > 100) {
			return parent;
		} else if (parent == not) {
			return pickParent(not, iteration + 1);
		} else {
			return parent;
		}
	}

	public void populateMatingPool(ArrayList<Phenotype> pool) {
		matingPool.clear();
		double lowest = Double.MAX_VALUE; // So lowest fitness = 0
		for (Phenotype hash : pool) {
			double fitness = hash.fitness;
			if (fitness < lowest) {
				lowest = fitness;
			}
		}

		for (Phenotype hash : pool) {
			double fitness = hash.fitness;
			for (int i = 0; i < fitness + Math.abs(lowest) + 1; i++) {
				matingPool.add(hash);
			}
		}
	}

	public ArrayList<Phenotype> getHighestHalf(ArrayList<Phenotype> pool) {
		Collections.sort(pool);
		ArrayList<Phenotype> newPool = new ArrayList<Phenotype>();
		for (int i = 0; i < pool.size() / 2; i++) {
			newPool.add(pool.get(i));
		}
		return newPool;
	}

	public void setSpecialMutation(boolean sm) {
		this.specialMutation = sm;
	}

	public boolean getSpecialMutation() {
		return specialMutation;
	}

	public ArrayList<Phenotype> getSorted(ArrayList<Phenotype> pool) {
		Collections.sort(pool);
		return pool;
	}

	public ArrayList<Phenotype> getMatingPool() {
		return matingPool;
	}

	public Phenotype getBestIndividual() {
		return getSorted(pool).get(0);
	}

	public ArrayList<Phenotype> getPopulation() {
		return pool;
	}

	public double getAvgFitness() {
		return avgFitness;
	}

	public double getAvgNeurons() {
		return avgNeurons;
	}

	public void setIsMultiThreaded(boolean isMultiThreaded) {
		this.isMultiThreaded = isMultiThreaded;
	}

	public boolean getIsMultiThreaded() {
		return isMultiThreaded;
	}

	public int getGeneration() {
		return generation;
	}

	public String getOutput() {
		return output;
	}

	public String progressBar(int progress) {
		String percent = "|";
		for (int i = 0; i < 10; i++) {
			if (progress >= 10) {
				percent += "=";
			} else {
				percent += "-";
			}

			progress -= 10;
		}
		percent += "|\r";
		return percent;
	}

	public void runGeneration() {
		selection();

		makeNewGeneration();

		clearStats();
	}

	public void selection() {

		if (isMultiThreaded) {
			class SimThread implements Runnable {
				private int poolId;

				public SimThread(int poolId) {
					this.poolId = poolId;
				}

				public void run() {
					Phenotype ind = pool.get(this.poolId);
					pool.set(this.poolId,
							new Phenotype(simulate((NeuralNetwork) ind.gt), (NeuralNetwork) ind.gt, ind.name));
				}
			}

			Thread[] thread = new Thread[pool.size()];

			// Create and start threads
			for (int i = 0; i < pool.size(); i++) {
				thread[i] = new Thread(new SimThread(i));
				thread[i].start();
			}

			// Wait for threads to complete by joining
			for (int i = 0; i < pool.size(); i++) {
				try {
					thread[i].join();
				} catch (InterruptedException e) {
					System.out.println(e.toString());
				}
			}
		} else {
			for (int i = 0; i < pool.size(); i++) {
				Phenotype ind = pool.get(i);
				pool.set(i, new Phenotype(simulate((NeuralNetwork)ind.gt), (NeuralNetwork)ind.gt, ind.name));
			}
		}

	}

	public abstract double simulate(NeuralNetwork nn);

	public abstract void makeNewGeneration();

	public abstract void clearStats();

}