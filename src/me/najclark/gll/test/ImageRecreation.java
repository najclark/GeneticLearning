package me.najclark.gll.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import me.najclark.gll.ga.GeneList;
import me.najclark.gll.ga.GeneticAlgorithm;
import me.najclark.gll.ga.Genotype;
import me.najclark.gll.ga.Phenotype;

public class ImageRecreation {

	BufferedImage base;
	
	public static void main(String[] args) {
		new ImageRecreation().run(100, 0.01, 50, 4, "/home/nicholas/git/GeneticLearning/src/me/najclark/gll/res/penguins.jpg");
	}

	public void run(int population, double mutateRate, int maxSpots, int sides, String imgPath) {
		
		try {
			base = ImageIO.read(new File(imgPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		GeneticAlgorithm ga = new GeneticAlgorithm(mutateRate) {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public double simulate(Genotype gt) {

				BufferedImage generated = new BufferedImage(base.getWidth(), base.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = (Graphics2D) generated.getGraphics();

				GeneList<Splotch> genes = (GeneList) gt;
				for (int i = 0; i < genes.size(); i++) {
					g2d.setColor(genes.get(i).c);
					g2d.fill(genes.get(i).poly);
				}
				
				double fitness = 0;
				for(int x = 0; x < generated.getWidth(); x++){
					for(int y = 0; y < generated.getHeight(); y++){
						Color a = new Color(generated.getRGB(x, y));
						Color b = new Color(base.getRGB(x, y));
						
						fitness += distance(a, b);
					}
				}

				return fitness;
			}

			public double distance(Color a, Color b) {
				return Math.sqrt(Math.pow(a.getRed() + b.getRed(), 2) + Math.pow(a.getGreen() + b.getGreen(), 2)
						+ Math.pow(a.getBlue() + b.getBlue(), 2));
			}

			@Override
			public void makeNewGeneration() {
				
				//Shuffle lowest to highest
				double highest = Integer.MIN_VALUE;
				for(int i = 0; i < pool.size(); i++){
					if(pool.get(i).fitness > highest){
						highest = pool.get(i).fitness;
					}
				}
				
				for(int i = 0; i < pool.size(); i++){
					Phenotype pt = pool.get(i);
					pt.fitness = Math.abs(pt.fitness - highest);
					pool.set(i, pt);
				}
				
				//Make new generation
				ArrayList<Phenotype> newPool = new ArrayList<Phenotype>();
				populateMatingPool(pool);

				for (int i = 0; i < pool.size(); i++) {
					Phenotype p1 = pickParent(null, 0);
					Phenotype p2 = pickParent(p1, 0);

					Phenotype child = mutate(crossover(p1, p2));
					newPool.add(child);
				}

				double fitness = 0;
				for(Phenotype pt : pool){
					fitness += pt.fitness;
				}
				avgFitness = fitness / pool.size();
				
				pool.clear();
				pool.addAll(newPool);

				output = "Generation: " + generation + ". Avg Fitness: " + avgFitness;
			}

			@Override
			public void clearStats() {
				avgFitness = 0;
				generation++;
			}
		};

		for (int i = 0; i < population; i++) {
			GeneList<Splotch> spots = new GeneList<Splotch>();
			for (int a = 0; a < maxSpots; a++) {
				int[] xpoints = new int[sides];
				int[] ypoints = new int[sides];

				for (int j = 0; j < sides; j++) {
					xpoints[j] = ga.random.nextInt(base.getWidth());
					ypoints[j] = ga.random.nextInt(base.getHeight());
				}

				Color c = new Color(ga.random.nextInt(255), ga.random.nextInt(255), ga.random.nextInt(255),
						ga.random.nextInt(255));
				Polygon poly = new Polygon(xpoints, ypoints, sides);

				spots.add(new Splotch(poly, c));
			}
			ga.pool.add(new Phenotype(0, spots, Phenotype.generateName()));
		}
		
		JFrame frame = new JFrame("Image Recreator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		JLabel lbl = new JLabel();
		
		panel.add(lbl);
		frame.add(panel);
		frame.setSize(base.getWidth(), base.getHeight());
		frame.setVisible(true);
		
		while(ga.getAvgFitness() < 100){
			ga.runGeneration();
			
			BufferedImage generated = new BufferedImage(base.getWidth(), base.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) generated.getGraphics();

			GeneList<Splotch> genes = (GeneList) ga.getBestIndividual().gt;
			for (int i = 0; i < genes.size(); i++) {
				g2d.setColor(genes.get(i).c);
				g2d.fill(genes.get(i).poly);
			}
			
			lbl.setIcon(new ImageIcon(generated));
			System.out.println(ga.pool.size());
			System.out.println(ga.getOutput());
		}
	}

}

class Splotch {
	Polygon poly;
	Color c;

	public Splotch(Polygon poly, Color c) {
		this.poly = poly;
		this.c = c;
	}

	public Splotch(Polygon poly) {

	}

	public Splotch(Color c) {

	}

	public Splotch() {

	}

	private Polygon randomPoly(int sides) {
		int[] xpoints = new int[sides];
		int[] ypoints = new int[sides];

		for (int j = 0; j < sides; j++) {
			xpoints[j] = new Random().nextInt(); // TODO Bind these
			ypoints[j] = new Random().nextInt();
		}
		return new Polygon(xpoints, ypoints, sides);
	}
}