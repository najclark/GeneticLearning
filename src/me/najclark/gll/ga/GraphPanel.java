package me.najclark.gll.ga;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6418087846700038804L;
	HashMap<Color, ArrayList<Double>> hash;
	final int PAD = 20;
	int max = Integer.MAX_VALUE;
	boolean points = true;

	public GraphPanel() {
		hash = new HashMap<Color, ArrayList<Double>>();
	}

	public void addPoint(Color key, double value) {
		if (hash.containsKey(key)) {
			hash.get(key).add(value);
		} else {
			ArrayList<Double> list = new ArrayList<Double>();
			list.add(value);
			hash.put(key, list);
		}

		if (hash.get(key).size() > max) {
			hash.get(key).remove(0);
		}
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth();
		int h = getHeight();
		// Draw ordinate.
		g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));
		// Draw abcissa.
		g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));
		// Draw labels.
		Font font = g2.getFont();
		FontRenderContext frc = g2.getFontRenderContext();
		LineMetrics lm = font.getLineMetrics("0", frc);
		float sh = lm.getAscent() + lm.getDescent();
		// Ordinate label.
		String s = "Fitness";
		float sy = PAD + ((h - 2 * PAD) - s.length() * sh) / 2 + lm.getAscent();
		for (int i = 0; i < s.length(); i++) {
			String letter = String.valueOf(s.charAt(i));
			float sw = (float) font.getStringBounds(letter, frc).getWidth();
			float sx = (PAD - sw) / 2;
			g2.drawString(letter, sx, sy);
			sy += sh;
		}
		// Abcissa label.
		s = "Generation";
		sy = h - PAD + (PAD - sh) / 2 + lm.getAscent();
		float sw = (float) font.getStringBounds(s, frc).getWidth();
		float sx = (w - sw) / 2;
		g2.drawString(s, sx, sy);

		for (Color key : hash.keySet()) {
			// Draw lines.
			ArrayList<Double> list = hash.get(key);
			double xInc = (double) (w - 2 * PAD) / (list.size() - 1);
			double scale = (double) (h - 2 * PAD) / getMax();
			g2.setPaint(key);
			for (int i = 0; i < list.size() - 1; i++) {
				double x1 = PAD + i * xInc;
				double y1 = h - PAD - scale * list.get(i);
				double x2 = PAD + (i + 1) * xInc;
				double y2 = h - PAD - scale * list.get(i + 1);
				g2.draw(new Line2D.Double(x1, y1, x2, y2));
			}
			// Mark data points.
			if (points) {
				g2.setPaint(Color.red);
				for (int i = 0; i < list.size(); i++) {
					double x = PAD + i * xInc;
					double y = h - PAD - scale * list.get(i);
					g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
					if ((i + 1) % ((list.size() * PAD * 3 / getWidth()) + 1) == 0) {
						g2.setPaint(Color.gray);
						g2.drawString(String.valueOf(i), (float) x, sy);
						g2.draw(new Line2D.Double(PAD + i * xInc, h - PAD, PAD + i * xInc, PAD));
					}
				}
			}
		}
	}

	private double getMax() {
		double max = -Double.MAX_VALUE;
		for (Color key : hash.keySet()) {
			for (int i = 0; i < hash.get(key).size(); i++) {
				if (hash.get(key).get(i) > max)
					max = hash.get(key).get(i);
			}
		}
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void showPoints(boolean points) {
		this.points = points;
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GraphPanel gp = new GraphPanel();
		gp.showPoints(true);
		Random r = new Random();
		for (int i = 1; i < 100; i++) {
			gp.addPoint(Color.green, r.nextInt(i));
			gp.addPoint(Color.yellow, r.nextInt(i));
			gp.addPoint(Color.red, r.nextInt(i));
		}
		f.add(gp);
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);
	}
}