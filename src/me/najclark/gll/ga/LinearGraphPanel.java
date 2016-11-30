package me.najclark.gll.ga;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
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

public class LinearGraphPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6418087846700038804L;
	HashMap<Color, ArrayList<Double>> hash;
	final int PAD = 40;
	int max = Integer.MAX_VALUE;
	boolean points = true;

	public LinearGraphPanel() {
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
		// Draw labels.
		Font font = g2.getFont();
		FontRenderContext frc = g2.getFontRenderContext();
		LineMetrics lm = font.getLineMetrics("0", frc);
		float sh = lm.getAscent() + lm.getDescent();
		float sy = h - PAD + (PAD - sh) / 2 + lm.getAscent();

		// draw white background
		g2.setColor(Color.WHITE);
		g2.fillRect(PAD, PAD, getWidth() - 2 * PAD, getHeight() - 2 * PAD);
		g2.setColor(Color.BLACK);

		for (Color key : hash.keySet()) {
			// Draw lines.
			ArrayList<Double> list = hash.get(key);
			double xInc = (double) (w - 2 * PAD) / (list.size() - 1);
			double scale = (double) (h - 2 * PAD) / getMax();
			for (int i = 0; i < list.size(); i++) {
				double x = PAD + i * xInc;
				double y = h - PAD - scale * list.get(i);
				if (points) { // Mark data points.
					g2.setPaint(Color.red);
					g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
				}
			}
			g2.setPaint(key);
			for (int i = 0; i < list.size() - 1; i++) {
				double x1 = PAD + i * xInc;
				double y1 = h - PAD - scale * list.get(i);
				double x2 = PAD + (i + 1) * xInc;
				double y2 = h - PAD - scale * list.get(i + 1);
				g2.draw(new Line2D.Double(x1, y1, x2, y2));
			}

			int numberYDivisions = 10;
			// create hatch marks and grid lines for y axis.
			for (int i = 0; i < numberYDivisions + 1; i++) {
				int x0 = PAD;
				int x1 = x0 + 8;
				int y0 = getHeight() - ((i * (getHeight() - PAD * 2 - PAD)) / numberYDivisions + PAD);
				int y1 = y0;
				g2.setColor(new Color(149, 165, 166));
				g2.drawLine(PAD + 1 + 4, y0, getWidth() - PAD, y1);
				g2.setColor(Color.BLACK);
				String yLabel = ((int) ((getMin() + (getMax() - getMin()) * ((i * 1.0) / numberYDivisions)) * 100))
						/ 100.0 + "";
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(yLabel);
				g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
				g2.drawLine(x0, y0, x1, y1);
			}

			// and for x axis
			for (int i = 0; i < list.size(); i++) {
				if (list.size() > 1) {
					int x0 = PAD + i * (w - 2 * PAD) / (list.size() - 1);
					int x1 = x0;
					int y0 = getHeight() - PAD;
					int y1 = y0 - 10;
					if ((i % ((int) ((list.size() / 20.0)) + 1)) == 0) {
						g2.setColor(new Color(149, 165, 166));
						g2.drawLine(x0, getHeight() - PAD - 1 - 10, x1, PAD);
						g2.setColor(Color.BLACK);
						String xLabel = i + "";
						FontMetrics metrics = g2.getFontMetrics();
						int labelWidth = metrics.stringWidth(xLabel);
						g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
					}
					g2.drawLine(x0, y0, x1, y1);
				}
			}

			g2.setPaint(Color.black);
			// Draw ordinate.
			g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));
			// Draw abcissa.
			g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));
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

	private double getMin() {
		double min = Double.MAX_VALUE;
		for (Color key : hash.keySet()) {
			for (int i = 0; i < hash.get(key).size(); i++) {
				if (hash.get(key).get(i) < min)
					min = hash.get(key).get(i);
			}
		}
		return min;
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
		LinearGraphPanel gp = new LinearGraphPanel();
		gp.showPoints(true);
		Random r = new Random();
		for (int i = 1; i < 10; i++) {
			gp.addPoint(new Color(46, 204, 113), r.nextInt(i));
			gp.addPoint(new Color(241, 196, 15), r.nextInt(i));
			gp.addPoint(new Color(231, 76, 60), r.nextInt(i));
		}
		PieChart pc = new PieChart();
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < alphabet.length(); i++) {
			pc.addSlice(new Slice(r.nextInt(100), new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)), String.valueOf(alphabet.charAt(i))));
		}
		pc.updateSliceByLabel("n", 100);
		f.add(gp);
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);
	}
}