package me.najclark.gll.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public abstract class GeneticAlgorithm {

	private ArrayList<Phenotype> matingPool = new ArrayList<Phenotype>();
	public ArrayList<Phenotype> pool;
	public Random random = new Random();
	protected int maxFitness = 0;
	protected double mutateRate = 0.01;
	protected int generation = 0;
	protected double avgFitness = 0;
	protected String output = "";
	protected boolean isMultiThreaded = true;

	public GeneticAlgorithm(double mutateRate){
		pool = new ArrayList<Phenotype>();
		this.mutateRate = mutateRate;
	}
	
	public GeneticAlgorithm(){
		this(0);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Phenotype mutate(Phenotype ind) {
		GeneList gl = (GeneList) ind.gt;
		boolean changed = false;

		if(random.nextDouble() < mutateRate){
			changed = true;
			Object a = gl.get(random.nextInt(gl.size()));
			Object b = gl.get(random.nextInt(gl.size()));
			gl.set(gl.indexOf(b), a);
			gl.set(gl.indexOf(a), b);
		}

		String mutatedName = ind.name;
		if (changed) {
			mutatedName = Phenotype.mutateName(ind.name, 1, Phenotype.SKIP_FIRST);
		}
		return new Phenotype(0, gl, mutatedName);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Phenotype crossover(Phenotype ind, Phenotype ind2) {
		GeneList p1 = (GeneList) ind.gt;
		GeneList p2 = (GeneList) ind2.gt;
		
		if(p1.size() == p2.size()){
			int crossPoint = random.nextInt(p1.size());
			for(int i = crossPoint; i < p2.size(); i++){
				p1.set(i, p2.get(i));
			}
		}
		return new Phenotype(0, p1, Phenotype.mixNames(ind.name, ind2.name));
	}

	public Genotype acceptReject(ArrayList<Phenotype> pool, int maxFitness) {
		Random random = new Random();
		int besafe = 0;
		while (besafe < 10000) {
			int index = random.nextInt(pool.size());
			Phenotype partner = pool.get(index);
			int r = random.nextInt(maxFitness);
			double key = partner.fitness;

			if (r < key) {
				return partner.gt;
			}
			besafe++;
		}
		return pool.get(0).gt;

	}

	public Phenotype pickParent(Phenotype not, int iteration) {
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
							new Phenotype(simulate(ind.gt), ind.gt, ind.name));
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
				pool.set(i, new Phenotype(simulate(ind.gt), ind.gt, ind.name));
			}
		}

	}

	public abstract double simulate(Genotype pt);

	public abstract void makeNewGeneration();

	public abstract void clearStats();

}