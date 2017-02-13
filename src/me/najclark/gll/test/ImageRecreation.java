package me.najclark.gll.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
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

import me.najclark.gll.ga.GeneticAlgorithm;
import me.najclark.gll.ga.Genotype;
import me.najclark.gll.ga.Phenotype;

public class ImageRecreation {

	BufferedImage base;
	static int rDividen = 1;

	public static void main(String[] args) {
		new ImageRecreation().run(100, 1, 50, 4, "/home/nicholas/git/GeneticLearning/src/me/najclark/gll/res/bear.jpg");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run(int population, double mutateRate, int maxSpots, int sides, String imgPath) {

		try {
			base = ImageIO.read(new File(imgPath));

			BufferedImage image = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			Graphics g = image.getGraphics();
			g.drawImage(base, 0, 0, null);
			g.dispose();

		} catch (IOException e) {
			e.printStackTrace();
		}

		GeneticAlgorithm ga = new GeneticAlgorithm(mutateRate) {

			@Override
			public Phenotype mutate(Phenotype ind) {
				Splotch past = (Splotch) ind.gt;
				Splotch gl = new Splotch(past.poly, past.c);
				boolean changed = false;

				if (random.nextDouble() < mutateRate) {
					changed = true;
					Shape old = gl.poly;

					int x = old.getBounds().x;
					int y = old.getBounds().y;
					int r = old.getBounds().width;

					double choice = random.nextDouble();
					if (choice < 0.25) {
						x = random.nextInt(base.getWidth() - 100);
					} else if (choice < 0.5 && 0.25 <= choice) {
						y = random.nextInt(base.getHeight() - 100);
					} else if (choice < 0.75 && 0.5 <= choice) {
						r = Math.max(30, random.nextInt(base.getHeight() / rDividen));
					} else {
						gl = new Splotch(old);
					}
					gl.poly = new Ellipse2D.Double(x, y, r, r);
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

				return evaluate((Splotch) gt);
			}

			public double distance(Color a, Color b) {
				return Math.sqrt(Math.pow(a.getRed() + b.getRed(), 2) + Math.pow(a.getGreen() + b.getGreen(), 2)
						+ Math.pow(a.getBlue() + b.getBlue(), 2));
			}

			public double evaluate(Splotch spot) {

				BufferedImage generated = new BufferedImage(base.getWidth(), base.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = (Graphics2D) generated.getGraphics();

				// g2d.setColor(intToColor(spot.grayScale));
				g2d.setColor(Color.BLACK);
				g2d.fillRect(0, 0, base.getWidth(), base.getHeight());
				g2d.setColor(spot.c);
				g2d.fill(spot.poly);

				double fitness = 0;
				for (int x = 0; x < generated.getWidth(); x++) {
					for (int y = 0; y < generated.getHeight(); y++) {
						if (spot.poly.getBounds().contains(new Point(x, y))) {
							if (x < base.getWidth() && y < base.getHeight()) {
								Color a = new Color(generated.getRGB(x, y));
								Color b = new Color(base.getRGB(x, y));

								fitness += distance(a, b);
							}
						}
					}
				}
				return fitness / 1000;
			}

			@Override
			public void makeNewGeneration() {

				// Make new generation
				ArrayList<Phenotype> newPool = new ArrayList<Phenotype>();

				for (Phenotype pt : pool) {
					Phenotype daughter = mutate(pt);
					double fitness = evaluate((Splotch) daughter.gt);
					daughter.fitness = fitness;
					int attempts = 0;

					while (fitness < pt.fitness && attempts < 10) {
						daughter = mutate(pt);
						fitness = evaluate((Splotch) daughter.gt);
						daughter.fitness = fitness;
						attempts++;
					}
					if (fitness < pt.fitness) {
						newPool.add(daughter);
					} else {
						newPool.add(pt);
					}
					// newPool.add(daughter);
					// newPool.add(pt);
				}

				double oldFitness = 0;
				double newFitness = 0;
				for (int i = 0; i < pool.size(); i++) {
					oldFitness += pool.get(i).fitness;
					newFitness += newPool.get(i).fitness;
				}
				double avgOldFitness = oldFitness / pool.size();
				double avgNewFitness = newFitness / pool.size();

				pool.clear();
				pool.addAll(newPool);
				output = "Generation: " + generation + ". Avg Old Fitness: " + avgOldFitness + " vs " + avgNewFitness;
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
		JLabel lblOut = new JLabel();

		panel.add(lbl);
		panel.add(lblOut);
		frame.add(panel);
		frame.setSize(base.getWidth() * 2, base.getHeight() + 70);
		frame.setVisible(true);

		while (ga.getAvgFitness() < 100) {
			ga.runGeneration();

			BufferedImage generated = new BufferedImage(base.getWidth() * 2, base.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) generated.getGraphics();

			// ArrayList<Phenotype> list = ga.getHighestHalf(ga.pool);
			ArrayList<Phenotype> list = ga.pool;
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, base.getWidth(), base.getHeight());
			for (int i = 0; i < list.size(); i++) {
				// g2d.setColor(intToColor(((Splotch)
				// list.get(i).gt).grayScale));
				g2d.setColor(((Splotch) list.get(i).gt).c);
				g2d.fill(((Splotch) list.get(i).gt).poly);
			}

			g2d.drawImage(base, base.getWidth(), 0, null);
			lblOut.setText(ga.getOutput());

			lbl.setIcon(new ImageIcon(generated));
			// System.out.println(ga.getOutput());
		}
	}

	private Color intToColor(int colNum) {
		int rgbNum = 255 - (int) ((colNum / 50.0) * 255.0);
		return new Color(rgbNum, rgbNum, rgbNum);
	}

}

class Splotch implements Genotype {
	private static Random random = new Random();
	Shape poly;
	// int grayScale;
	Color c;

	public Splotch(Shape poly, Color c) {
		this.poly = poly;
		this.c = c;
	}

	public Splotch(Shape poly) {
		this(poly, randomColor());
	}

	public Splotch(int width, int height, int sides, Color c) {
		this(randomPoly(width, height, sides), c);
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
		int x = random.nextInt(width - 100);
		int y = random.nextInt(height - 100);
		int r = Math.max(30, random.nextInt(height / ImageRecreation.rDividen));
		return new Ellipse2D.Double(x, y, r, r);
	}

	private static Color randomColor() {
		return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), random.nextInt(255));
		// return random.nextInt(50) + 1;
	}
}