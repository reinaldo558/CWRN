package com.reinaldo.cwrn.games.flappy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.reinaldo.cwrn.games.Game;

public class Flappy extends Game {

	private static final long serialVersionUID = 3535387489243757948L;
	
	/** Used to randomly define obstacles position and size */
	private static final Random R = new Random();
	/** Default color that will be used for obstacles border and background */
	private static final Color C = new Color(210, 210, 210);
	
	/** Stores all obstacles for easy access and control */
	private List<JLabel> btns = new ArrayList<JLabel>(100);

	/** Jump state, used to control the player moviment */
	private boolean jump = false;
	
	/** Used to control the position the last obstacle were created and thus the position the next obstacle will be created */
	private int last_x = 0;
	
	// Difficulty controls, will be changed during play time
	/** Controls the horizontal distance between obstacles */
	private int division_x = 100;
	/** Controls the vertical distance between obstacles */
	private int division_y = 150;
	/** Controls the frequency on which obstacles are created (will also have effect on how close they are to each other) */
	private int creationTime = 10;
	
	private final JLabel lblPoints = new JLabel("0");
	private double points = 0;
	
	@Override
	public void start() {
		manageWindow();
		instructions();
		
		newPlayerDefault(">");
		player.setBounds(20, 50, 20, 20);
		
		letsBegin();
		playerControl();
	}
	
	private void replay() {
		if (JOptionPane.showConfirmDialog(getContentPane(), "Play again?") == JOptionPane.OK_OPTION) {
			points = 0;
			lblPoints.setText("0");
			division_x = 100;
			division_y = 150;
			creationTime = 10;
			last_x = getWidth();
			try {
				btns.forEach(x -> remove(x));
			} catch (Exception ex) {}
			btns.clear();
			jump = false;
			player.setBounds(20, 50, 20, 20);
			try {
				repaint();
			} catch (Exception ex) {}
			letsBegin();
		}
	}
	
	private void letsBegin() {
		// TICK control, all game rules are here
		new Thread(new Runnable() {
			int controlCreation = 0;
			int controlDifficulty = 0;
			@Override
			public void run() {
				while (true) {
					try {
						// Check if player left the screen, up or down
						if ((player.getY() > (getHeight() - 50)) || (player.getY() < -5)) {
							msg("You lost, don't leave the screen");
							replay();
							break;
						}
						
						// Needs this space between creations, the time is lowered during game play
						if (++controlCreation == creationTime) {
							createObstaclesInPairs();
							controlCreation = 0;
						}
						
						if (!moveObstacles()) {
							msg("You lost!");
							replay();
							break;
						}
						
						
						// Increase difficulty
						if (++controlDifficulty > 30) {
							controlDifficulty = 0;
							
							if (division_x > 40) {
								division_x -= 10;
							}
							if (division_y > 60) {
								division_y -= 5;
							}
							if (creationTime > 3) {
								creationTime--;
							} 
						}
						
						lblPoints.setText(((int)points)+"");
						
						
						Thread.sleep(30);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	
	
	
	
	private void createObstaclesInPairs() {
		final JLabel up = new JLabel("");
		final JLabel down = new JLabel("");
		
		up.setBackground(C);
		down.setBackground(C);
		
		up.setBorder(BorderFactory.createLineBorder(C));
		down.setBorder(BorderFactory.createLineBorder(C));
		
		up.setBounds(last_x, 0, 20, R.nextInt(100));
		down.setBounds(last_x, (up.getHeight() + division_y), 20, 400);
		
		last_x += division_x;
		
		add(up);
		add(down);		
		btns.add(up);
		btns.add(down);
	}
	
	
	/**
	 * Go through all obstacles moving them 4 pixels to the left <br/><br/>
	 * 
	 * While moving check if hit player. <br/><br/>
	 * 
	 * After moving, goes through the obstacles again to check if left the screen, if so, removes the object
	 * 
	 * */
	private boolean moveObstacles() {
		for (JLabel b : btns) {
			//Move 4 pixels to the left
			b.setBounds(b.getX() - 4, b.getY(), 20, b.getHeight());
			
			// Check if hit player
			if (player.intersects(b)) {
				return false;
			}
			
		}
		
		// Removes all objects that left the screen (to save memory and processor)
		for (int i = btns.size()-1 ; i>= 0 ; i--) {
			if (btns.get(i).getX() < -5) {
				remove(btns.get(i));
				btns.remove(i);
				points+= 0.5f;
			}
		}
		return true;
	}

	private void manageWindow() {
		setSize(600, 300);
		setPreferredSize(new Dimension(600, 300));
		last_x = getWidth();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		
		lblPoints.setBounds(getWidth() - 30, 10, 50, 20);
		lblPoints.setHorizontalTextPosition(JLabel.RIGHT);
		add(lblPoints);
	}
	
	
	private void playerControl() {
		// Just to define the jump state
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) { }
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) jump = false;
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) jump = true;
			}
		});
		
		// Controls if the player will jump or fall
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (jump) {
							// To create a bit of difficulty and copy "flappy bird" moviment, upon jump
							// the player will go up a few pixels for a few milliseconds
							for (int i = 0 ; i < 30 ; i++) {
								player.setLocation(player.getX(), player.getY()-1);
								Thread.sleep(5);
							}
						} else {
							// The gravity must be stronger than the flying (fall faster than jumping)
							player.setLocation(player.getX(), player.getY() + 3);
						}
						Thread.sleep(20);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	@Override
	public void instructions() {
		msg("Fly with [ SPACE ], avoid obstacles, stay INSIDE the screen");
	}
	

}
