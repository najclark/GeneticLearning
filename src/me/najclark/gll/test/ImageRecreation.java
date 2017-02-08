package me.najclark.gll.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
		new ImageRecreation().run(100, 0.05, 50, 4,
				"/home/nicholas/git/GeneticLearning/src/me/najclark/gll/res/penguins.jpg");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run(int population, double mutateRate, int maxSpots, int sides, String imgPath) {

		try {
			base = ImageIO.read(new File(imgPath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		GeneticAlgorithm ga = new GeneticAlgorithm(mutateRate) {

			@Override
			public Phenotype mutate(Phenotype ind) {
				Splotch gl = (Splotch) ind.gt;
				boolean changed = false;

				if (random.nextDouble() < mutateRate) {
					changed = true;
					Shape old = gl.poly;

					int x = old.getBounds().x;
					int y = old.getBounds().y;
					int w = old.getBounds().width;
					int h = old.getBounds().height;
					Color c = gl.c;

					double choice = random.nextDouble();
					if (choice < 0.2) {
						x = random.nextInt(base.getWidth());
						w = random.nextInt(base.getWidth() - x);
					} else if (choice < 0.4 && 0.2 <= choice) {
						y = random.nextInt(base.getHeight());
						h = random.nextInt(base.getHeight() - y);
					} else if (choice < 0.6 && 0.4 <= choice) {
						w = random.nextInt(base.getWidth() - x);
					} else if (choice < 0.8 && 0.6 <= choice) {
						h = random.nextInt(base.getHeight() - y);
					} else {
						gl = new Splotch(old);
					}
					gl.poly = new Rectangle(x, y, w, h);
				}

				String mutatedName = ind.name;
				if (changed) {
					mutatedName = Phenotype.mutateName(ind.name, 1, Phenotype.SKIP_FIRST);
				}
				return new Phenotype(0, gl, mutatedName);
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public double simulate(Genotype gt) {

				BufferedImage generated = new BufferedImage(base.getWidth(), base.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = (Graphics2D) generated.getGraphics();

				Splotch spot = (Splotch) gt;
				g2d.setColor(spot.c);
				g2d.fill(spot.poly);

				double fitness = 0;
				for (int x = 0; x < generated.getWidth(); x++) {
					for (int y = 0; y < generated.getHeight(); y++) {
						if (spot.poly.getBounds().contains(new Point(x, y))) {
							Color a = new Color(generated.getRGB(x, y));
							Color b = new Color(base.getRGB(x, y));

							fitness += distance(a, b);
						}
					}
				}

				return fitness / 1000;
			}

			public double distance(Color a, Color b) {
				return Math.sqrt(Math.pow(a.getRed() + b.getRed(), 2) + Math.pow(a.getGreen() + b.getGreen(), 2)
						+ Math.pow(a.getBlue() + b.getBlue(), 2));
			}

			@Override
			public void makeNewGeneration() {

				// Shuffle lowest to highest
				// double lowest = Integer.MAX_VALUE;
				// for (int i = 0; i < pool.size(); i++) {
				// if (pool.get(i).fitness < lowest) {
				// lowest = pool.get(i).fitness;
				// }
				// }
				//
				// for (int i = 0; i < pool.size(); i++) {
				// Phenotype pt = pool.get(i);
				// pt.fitness = Math.abs(pt.fitness + lowest);
				// pool.set(i, pt);
				// }

				// Make new generation
				ArrayList<Phenotype> newPool = new ArrayList<Phenotype>();
				pool = getSorted(pool);
				//newPool.addAll(getHighestHalf(pool));
				for(int i = pool.size() - 1; i >= pool.size()*0.1; i--){
					newPool.add(pool.get(i));
				}

				// Phenotype worst = null;
				// double highest = Double.MIN_NORMAL;
				// for(int i = 0; i < pool.size(); i++){
				// if(pool.get(i).fitness > highest){
				// worst = pool.get(i);
				// highest = pool.get(i).fitness;
				// }
				// }
				//
				// for(int i = 0; i < pool.size(); i++){
				// if(pool.get(i) != worst){
				// newPool.add(pool.get(i));
				// }
				// }

				// System.out.println(Arrays.asList(pool));
				//System.out.println(((Splotch) getSorted(pool).get(0).gt).c);

				for (int i = 0; i < pool.size()*0.9; i++) {
					newPool.add(new Phenotype(0, new Splotch(base.getWidth(), base.getHeight(), sides),
							Phenotype.generateName()));
				}System.out.println(pool.size());

				double fitness = 0;
				for (Phenotype pt : pool) {
					fitness += pt.fitness;
				}
				avgFitness = fitness / pool.size();

				pool.clear();
				pool.addAll(newPool);
				output = "Generation: " + generation + ". Best Fitness: " + getBestIndividual().fitness;
			}

			@Override
			public void clearStats() {
				avgFitness = 0;
				generation++;
			}
		};

		for (int i = 0; i < population; i++) {
			// GeneList<Splotch> spots = new GeneList<Splotch>();
			// for (int a = 0; a < maxSpots; a++) {
			// spots.add(new Splotch(base.getWidth(), base.getHeight(), sides));
			// }
			Splotch spot = new Splotch(base.getWidth(), base.getHeight(), sides);
			ga.pool.add(new Phenotype(0, spot, Phenotype.generateName()));
		}

		JFrame frame = new JFrame("Image Recreator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		JLabel lbl = new JLabel();

		panel.add(lbl);
		frame.add(panel);
		frame.setSize(base.getWidth(), base.getHeight());
		frame.setVisible(true);

		while (ga.getAvgFitness() < 100) {
			ga.runGeneration();

			BufferedImage generated = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) generated.getGraphics();

			// ArrayList<Phenotype> list = ga.getHighestHalf(ga.pool);
			ArrayList<Phenotype> list = ga.pool;
			for (int i = 0; i < list.size(); i++) {
				g2d.setColor(((Splotch) list.get(i).gt).c);
				g2d.fill(((Splotch) list.get(i).gt).poly);
			}

			lbl.setIcon(new ImageIcon(generated));
			// System.out.println(ga.getOutput());
		}
	}

}

class Splotch implements Genotype {
	private static Random random = new Random();
	Shape poly;
	Color c;

	public Splotch(Shape poly, Color c) {
		this.poly = poly;
		this.c = c;
	}

	public Splotch(Shape poly) {

	}

	public Splotch(Color c) {

	}

	public Splotch(int width, int height, int sides) {
		this(randomPoly(width, height, sides), randomColor());
	}

	private static Shape randomPoly(int width, int height, int sides) {
		// int[] xpoints = new int[sides];
		// int[] ypoints = new int[sides];
		//
		// for (int j = 0; j < sides; j++) {
		// xpoints[j] = random.nextInt(width);
		// ypoints[j] = random.nextInt(height);
		// }
		int x = random.nextInt(width);
		int y = random.nextInt(height);
		return new Rectangle(x, y, random.nextInt(width - x), random.nextInt(height - y));
	}

	private static Color randomColor() {
		return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), random.nextInt(255));
	}
}