package me.najclark.gll.test;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.GlyphVector;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ch.randelshofer.media.avi.AVIOutputStream;
import me.najclark.gll.ga.GeneticAlgorithm;
import me.najclark.gll.ga.Genotype;

public class ImageRecreation {

	ArrayList<Splotch> spots = new ArrayList<Splotch>();
	BufferedImage base;
	BufferedImage generated;
	static int rDividen = 1;
	double score = 0;
	long start = System.currentTimeMillis();
	boolean run = true;
	boolean play = true;

	// *************************************************************************************************
	public static String subject = "landscape";
	// square, rectangle, circle, ellipse, polygon, line, curve, letter
	public static String shape = "curve";
	public static int alphaMin = 150;
	public static int alphaMax = 255;
	public static boolean animation = false;
	public static double animationFreq = 0.5; // in seconds
	public static int animationFPS = 30; // frames per second
	public static double time = 300; // in seconds
	// *************************************************************************************************

	public static void main(String[] args) {
		new ImageRecreation().run("/home/nicholas/git/GeneticLearning/src/me/najclark/gll/res/" + subject + ".jpg");
	}

	public void run(String imgPath) {

		try {
			base = ImageIO.read(new File(imgPath));
			generated = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);

			BufferedImage image = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			Graphics g = image.getGraphics();
			g.drawImage(base, 0, 0, null);
			g.dispose();

		} catch (IOException e) {
			e.printStackTrace();
		}

		GeneticAlgorithm ga = new GeneticAlgorithm(1) {

			NumberFormat nf = NumberFormat.getInstance();
			double maxScore = evaluate(generated);
			double bestScore = evaluate(generated);
			int lastImprove = 0;
			int shapes = 0;

			@Override
			public double simulate(Genotype gt) {
				return 0;
			}

			public double distance(Color a, Color b) {
				double sumOfDiff = 0;
				sumOfDiff += Math.pow(a.getRed() - b.getRed(), 2); // red
				sumOfDiff += Math.pow(a.getGreen() - b.getGreen(), 2); // green
				sumOfDiff += Math.pow(a.getBlue() - b.getBlue(), 2); // blue
				sumOfDiff += Math.pow(a.getAlpha() - b.getAlpha(), 2); // alpha
				return sumOfDiff;
			}

			public double evaluate(BufferedImage test) {
				double fitness = 0;
				for (int x = 0; x < test.getWidth(); x++) {
					for (int y = 0; y < test.getHeight(); y++) {
						if (x < base.getWidth() && y < base.getHeight()) {
							Color a = new Color(test.getRGB(x, y));
							Color b = new Color(base.getRGB(x, y));

							fitness += distance(a, b);
						}
					}
				}
				return fitness;
			}

			@Override
			public void makeNewGeneration() {
				BufferedImage test = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = (Graphics2D) test.getGraphics();
				g2d.drawImage(generated, 0, 0, null);
				g2d.setColor(Splotch.randomColor());

				Shape s = Splotch.randomPoly(base.getWidth(), base.getHeight());
				if (shape.contains("circle") || shape.contains("ellipse") || shape.contains("square")
						|| shape.contains("rectangle") || shape.contains("polygon") || shape.contains("letter")) {
					if (shape.contains("letter")) {
						Point p = Splotch.randomCoords(base.getWidth(), base.getHeight());
						g2d.translate(p.x, p.y);
					}
					g2d.fill(s);
				} else {
					g2d.setStroke(new BasicStroke(Splotch.randomSize(base.getWidth())));
					g2d.draw(s);
				}

				double newFitness = evaluate(test);
				if (newFitness <= bestScore) {
					generated = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = (Graphics2D) generated.getGraphics();
					g.drawImage(test, 0, 0, null);

					bestScore = newFitness;
					lastImprove = 0;
					shapes++;
				}
				lastImprove++;
				generation++;
				nf.setMaximumFractionDigits(2);
				score = ((1 - (bestScore / maxScore)) * 10000) / 100;
				output = "Generation: " + generation + ". Shapes: " + shapes + ". Score: " + nf.format(score)
						+ "% Last Improved: " + lastImprove;
			}

			@Override
			public void clearStats() {
			}
		};

		JFrame frame = new JFrame("Image Recreator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		JPanel panel = new JPanel(new BorderLayout());
		JPanel btnPanel = new JPanel(new GridLayout(1, 2));
		JLabel lbl = new JLabel("", SwingConstants.CENTER);
		JLabel lblOut = new JLabel("", SwingConstants.CENTER);
		JLabel lblTime = new JLabel("", SwingConstants.CENTER);
		JButton pause = new JButton("Pause");
		JButton stop = new JButton("Stop");

		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pause.getText().equals("Pause")) {
					play = false;
					pause.setText("Play");
				} else {
					play = true;
					pause.setText("Pause");
				}
			}
		});

		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				run = false;
			}
		});

		panel.add(lbl, BorderLayout.BEFORE_FIRST_LINE);
		panel.add(lblOut, BorderLayout.CENTER);
		panel.add(lblTime, BorderLayout.AFTER_LAST_LINE);
		btnPanel.add(pause);
		btnPanel.add(stop);

		frame.add(panel, BorderLayout.CENTER);
		frame.add(btnPanel, BorderLayout.PAGE_END);
		frame.setSize(Math.max(base.getWidth() * 2, 500), base.getHeight() + 100);
		frame.setResizable(false);
		frame.setVisible(true);

		AVIOutputStream out = null;
		if (animation) {
			try {
				out = new AVIOutputStream(new File("/home/nicholas/" + subject + "_" + shape + "_video.mp4"),
						AVIOutputStream.VideoFormat.JPG);
				out.setVideoCompressionQuality(0.3f);
				out.setTimeScale(1);
				out.setFrameRate(animationFPS);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		long lastCapture = 0;
		// while (score < 100 && run) {
		while (System.currentTimeMillis() - start < time * 1000 && run) {
			while (!play) {
				try {
					Thread.sleep(0, 1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			ga.runGeneration();

			BufferedImage test = new BufferedImage(base.getWidth() * 2, base.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) test.getGraphics();
			g2d.drawImage(generated, 0, 0, null);
			g2d.drawImage(base, base.getWidth(), 0, null);
			lblOut.setText(ga.getOutput());
			lblTime.setText("Time elapsed: " + elapsed(start));

			lbl.setIcon(new ImageIcon(test));
			if (System.currentTimeMillis() - lastCapture > animationFreq * 1000 && animation) {
				try {
					out.writeFrame(getScreenShot(panel));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				lastCapture = System.currentTimeMillis();
			}
		}
		if (animation) {
			System.out.println("Saving movie...");
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			File outputfile = new File("/home/nicholas/" + subject + "_" + shape + ".png");
			ImageIO.write(generated, "png", outputfile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		System.out.println("finished " + subject + ", time elapsed: " + elapsed(start));
	}

	public static BufferedImage getScreenShot(Component component) {

		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		component.printAll(image.getGraphics());
		return image;
	}

	public static String elapsed(long start) {
		SimpleDateFormat sdf = new SimpleDateFormat("ss");
		Date date;
		String elapsed = "";
		try {
			date = sdf.parse(String.valueOf((System.currentTimeMillis() - start) / 1000));
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(date);
			elapsed += calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":"
					+ calendar.get(Calendar.SECOND);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return elapsed;
	}

	public static void saveMovie(ArrayList<BufferedImage> images) {
		AVIOutputStream out = null;
		try {
			out = new AVIOutputStream(new File("/home/nicholas/" + subject + "_video.mp4"),
					AVIOutputStream.VideoFormat.JPG);
			out.setVideoCompressionQuality(0.3f);
			out.setTimeScale(1);
			out.setFrameRate(30);

			for (BufferedImage image : images) {
				out.writeFrame(image);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
		this(poly, randomColor());
	}

	public Splotch(int width, int height, int sides, Color c) {
		this(randomPoly(width, height), c);
	}

	public Splotch(int width, int height) {
		this(randomPoly(width, height), randomColor());
	}

	public static Shape randomPoly(int width, int height) {
		Point p = randomCoords(width, height);

		int w = randomSize(width);
		if (ImageRecreation.shape.toLowerCase().contains("square")) {
			return new Rectangle(p.x, p.y, w, w);
		} else if (ImageRecreation.shape.toLowerCase().contains("circle")) {
			return new Ellipse2D.Double(p.x, p.y, w, w);
		} else if (ImageRecreation.shape.toLowerCase().contains("rectangle")) {
			return new Rectangle(p.x, p.y, w, randomSize(width));
		} else if (ImageRecreation.shape.toLowerCase().contains("ellipse")) {
			return new Ellipse2D.Double(p.x, p.y, w, randomSize(width));
		} else if (ImageRecreation.shape.toLowerCase().contains("line")) {
			Point p1 = randomCoords(width, height);
			return new Line2D.Double(p.x, p.y, p1.x, p1.y);
		} else if (ImageRecreation.shape.toLowerCase().contains("polygon")) {
			int[] x = { p.x, 0, 0, 0 };
			int[] y = { p.y, 0, 0, 0 };
			for (int i = 1; i < x.length; i++) {
				Point p1 = randomCoords(width, height);
				x[i] = p1.x;
				y[i] = p1.y;
			}
			return new Polygon(x, y, x.length);
		} else if (ImageRecreation.shape.toLowerCase().contains("curve")) {
			CubicCurve2D c = new CubicCurve2D.Double();
			Point p1 = randomCoords(width, height);
			Point p2 = randomCoords(width, height);
			Point p3 = randomCoords(width, height);
			c.setCurve(p.x, p.y, p1.x, p1.x, p2.x, p2.y, p3.x, p3.y);
			return c;
		} else {
			Font f = new JPanel().getFont().deriveFont(Font.BOLD, w);
			GlyphVector v = f.createGlyphVector(new JPanel().getFontMetrics(f).getFontRenderContext(),
					String.valueOf(randomLetter()));
			return v.getOutline();
		}
	}

	public static int randomSize(int width) {
		return random.nextInt(width) + 10;
	}

	public static Point randomCoords(int width, int height) {
		return new Point(random.nextInt(width), random.nextInt(height));
	}

	public static char randomLetter() {
		String chars = "abcdefghijklmnopqrstuvwxyz0123456789~`_-+={[}]|:;'<,>.?/ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		return chars.charAt(random.nextInt(chars.length()));
	}

	public static Color randomColor() {
		return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), random.nextInt((ImageRecreation.alphaMax - ImageRecreation.alphaMin) + 1) + ImageRecreation.alphaMin);
	}
}