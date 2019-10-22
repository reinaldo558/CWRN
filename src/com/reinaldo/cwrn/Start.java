package com.reinaldo.cwrn;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import com.reinaldo.cwrn.games.Game;
import com.reinaldo.cwrn.games.arrow.Arrow;
import com.reinaldo.cwrn.games.avoid.Avoid;
import com.reinaldo.cwrn.games.flappy.Flappy;
import com.reinaldo.cwrn.games.spaceinvaders.Space;
import com.reinaldo.cwrn.games.survive.Survive;

/** Can't work right now*/
public class Start {
	
	private static final Map<Integer, Game> G = initializeMap(); 
	
	
	public static void main(String[] x) {
		try {
			int i = getOption();
			G.get(i).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	private static int getOption() {
		final String op = JOptionPane.showInputDialog("Choose a game from 1 to " + G.size() + ":");
		try {
			return Integer.parseInt(op);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Option not valid");
			System.out.println("Option not valid");
			System.exit(0);
		}
		return 0;
	}
	
	
	private static Map<Integer, Game> initializeMap() {
		Map<Integer, Game> map = new HashMap<Integer, Game>();
		
		map.put(1, new Avoid());
		map.put(2, new Survive());
		map.put(3, new Flappy());
		map.put(4, new Arrow());
		map.put(5, new Space());
		
		return map;
	}

}
