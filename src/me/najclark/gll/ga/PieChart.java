package me.najclark.gll.ga;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;

class Slice implements Comparable<Slice>{
	double value;
	Color color;
	String label;

	public Slice(double value, Color color, String label) {
		this.value = value;
		this.color = color;
		this.label = label;
	}

	@Override
	public int compareTo(Slice o) {
		return Double.compare(o.value, value);
	}
}

class PieChart extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1642013835665649439L;
	ArrayList<Slice> slices;
	NumberFormat nf;

	PieChart() {
		slices = new ArrayList<Slice>();
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
	}

	public void addSlice(Slice s) {
		slices.add(s);
	}
	
	public void updateSliceByLabel(String label, double value){
		for(int i = 0; i < slices.size(); i++){
			if(slices.get(i).label.equalsIgnoreCase(label)){
				Slice s = slices.get(i);
				s.value = value;
				slices.set(i, s);
				break;
			}
		}
	}

	public void paint(Graphics g) {
		drawPie((Graphics2D) g, new Rectangle(80, 0, getWidth()-80, getHeight()), slices);
	}

	private double getTotal() {
		double total = 0;
		for (Slice s : slices) {
			total += s.value;
		}
		return total;
	}
	
	private ArrayList<Slice> getSorted(){
		ArrayList<Slice> values = new ArrayList<Slice>();
		values.addAll(slices);
		Collections.sort(values);
		return values;
	}

	void drawPie(Graphics2D g, Rectangle area, ArrayList<Slice> slices) {
		double total = getTotal();
		double curValue = 0.0D;
		int startAngle = 0;
		for (int i = 0; i < slices.size(); i++) {
			startAngle = (int) (curValue * 360 / total);
			int arcAngle = (int) (slices.get(i).value * 360 / total);
			g.setColor(slices.get(i).color);
			g.fillArc(area.x, area.y, area.width, area.height, startAngle, arcAngle);
			g.setColor(getSorted().get(i).color);
			g.drawString(getSorted().get(i).label + ": " + nf.format((getSorted().get(i).value / getTotal()) * 100) + "%", 2, 15 + 17 * i);
			curValue += slices.get(i).value;
		}
	}
}