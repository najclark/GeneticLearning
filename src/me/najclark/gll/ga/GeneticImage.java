package me.najclark.gll.ga;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import me.najclark.gll.nn.NetworkPicture;
import me.najclark.gll.nn.NeuralNetwork;

public class GeneticImage {

	private NetworkPicture np;
	private LinearGraphPanel gp;
	private PieChart pc;
	
	public GeneticImage(){
		np = new NetworkPicture();
		gp = new LinearGraphPanel();
		pc = new PieChart();
	}
	
	public BufferedImage getImage(ArrayList<Individual> inds, Dimension d){
		JPanel main = new JPanel(new BorderLayout());
		main.setSize(d);

		Individual best = getBest(inds);
		Individual median = getMedian(inds);
		Individual worst = getWorst(inds);
		
		gp.addPoint(new Color(46, 204, 113), best.fitness); //green
		gp.addPoint(new Color(241, 196, 15), median.fitness); //yellow
		gp.addPoint(new Color(231, 76, 60), worst.fitness); //red
		
		ArrayList<String> startingLetters = new ArrayList<String>();
		for(Individual i : inds){
			startingLetters.add(i.name.substring(0, 1));
		}
		populatePieChart(startingLetters);
		
		main.add(gp, BorderLayout.LINE_START);
		main.add(pc, BorderLayout.LINE_END);
		
		JPanel bottom = new JPanel(new GridLayout(1, 3));
		main.add(bottom, BorderLayout.PAGE_END);
		np.update((NeuralNetwork)best.pt);
		bottom.add(new JLabel(new ImageIcon(np.getNetworkImage(bottom.getWidth()/3, bottom.getHeight(), 6))));
		np.update((NeuralNetwork)median.pt);
		bottom.add(new JLabel(new ImageIcon(np.getNetworkImage(bottom.getWidth()/3, bottom.getHeight(), 6))));
		np.update((NeuralNetwork)worst.pt);
		bottom.add(new JLabel(new ImageIcon(np.getNetworkImage(bottom.getWidth()/3, bottom.getHeight(), 6))));
		
		return getScreenShot(main);
	}
	
	private BufferedImage getScreenShot(JPanel panel){
        BufferedImage bi = new BufferedImage(
            panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        panel.paint(bi.getGraphics());
        return bi;
    }
	
	private void populatePieChart(ArrayList<String> votes) {
		HashMap<String, Integer> people = new HashMap<String, Integer>();

		for (String s : votes) {
			s = s.toLowerCase();
			if (people.containsKey(s)) {
				people.put(s, people.get(s) + 1);
			} else {
				people.put(s, 1);
			}
		}
		
		for(String key : people.keySet()){
			pc.updateSliceByLabel(key, people.get(key));
		}
	}
	
	private Individual getBest(ArrayList<Individual> inds){
		double fitness = Double.MIN_VALUE;
		Individual best = new Individual();
		for(int i = 0; i < inds.size(); i++){
			Individual cur = inds.get(i);
			if(cur.fitness > fitness){
				fitness = cur.fitness;
				best = cur;
			}
		}
		return best;
	}
	
	private Individual getWorst(ArrayList<Individual> inds){
		double fitness = Double.MAX_VALUE;
		Individual worst = new Individual();
		for(int i = 0; i < inds.size(); i++){
			Individual cur = inds.get(i);
			if(cur.fitness < fitness){
				fitness = cur.fitness;
				worst = cur;
			}
		}
		return worst;
	}
	
	private Individual getMedian(ArrayList<Individual> inds){
		ArrayList<Individual> clone = new ArrayList<Individual>();
		clone.addAll(inds);
		Collections.sort(clone);
		return clone.get(clone.size()/2);
	}
	
	
}
