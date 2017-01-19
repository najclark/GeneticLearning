package me.najclark.gll.ga;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import me.najclark.gll.nn.NetworkPicture;

public class GeneticFrame {

	private ArrayList<BufferedImage> images;
	private NetworkPicture np;
	private JFrame frame;
	private JPanel panel;
	private JLabel label;
	private JSlider slider;

	public GeneticFrame(String title, int width, int height) {
		images = new ArrayList<BufferedImage>();
		np = new NetworkPicture();
		frame = new JFrame(title);
		panel = new JPanel(new GridLayout(2, 1));
		label = new JLabel();
		slider = new JSlider();

		frame.setSize(width, height);

		np.setNodes(Color.blue);
		np.setLines(Color.blue);
		np.setNegativeLines(Color.red);

		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMaximum(images.size());
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValue() - 1 > 0 && source.getValue() - 1 < images.size()) {
					label.setIcon(new ImageIcon(images.get(source.getValue() - 1)));
				}
			}
		});

		panel.add(label);
		panel.add(slider);

		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void update(GeneticAlgorithm pop) {
		Individual ind = pop.getBestIndividual();
		np.update(ind.nn);
		BufferedImage img = np.getNetworkImage(frame.getWidth(), frame.getHeight(), 60);
		Graphics g = img.getGraphics();
		g.setColor(Color.black);
		g.drawString(pop.getOutput(), 50, 25);

		g.drawString("Best Individual Name: " + ind.name, 50, 50);

		ArrayList<String> startingLetters = new ArrayList<String>();
		for (Individual i : pop.getPopulation()) {
			startingLetters.add(i.name.substring(0, 1));
		}
		g.drawString("Highest Starting Letter: " + calculateElectionWinner(startingLetters), 50, 75);

		images.add(img);
		slider.setMinimum(1);
		slider.setMaximum(images.size());
	}

	private static String calculateElectionWinner(ArrayList<String> votes) {
		HashMap<String, Integer> people = new HashMap<String, Integer>();

		for (String s : votes) {
			s = s.toLowerCase();
			if (people.containsKey(s)) {
				people.put(s, people.get(s) + 1);
			} else {
				people.put(s, 1);
			}
		}

		int largest = 0;
		String key = "";
		for (String p : people.keySet()) {
			if (people.get(p) > largest) {
				key = p;
				largest = people.get(p);
			} else if (people.get(p) == largest)
				key += ", " + p;
		}

		return key;
	}

}
