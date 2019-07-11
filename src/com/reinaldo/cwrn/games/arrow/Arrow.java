package com.reinaldo.cwrn.games.arrow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import com.reinaldo.cwrn.games.Game;
import com.reinaldo.cwrn.games.Obj;
import com.reinaldo.cwrn.games.ObjPattern;

/***
 * You can create more stages, see {@link #createFases()}
 * @author reinaldo.silva
 *
 */
public class Arrow extends Game {

	private static final long serialVersionUID = -5853544175993867440L;
	
	private static final Font F = new Font("Sans Serif", Font.PLAIN, 16);

	private JLabel lblArrows = new JLabel("");
	private int arrows = 10;
	private int shoots = 0;
	
	/** Stores all stages */
	private Map<Integer, List<ObjPattern>> fases = new HashMap<Integer, List<ObjPattern>>();
	/** Stores the current stage, for easy control and manipulation */
	private List<ObjPattern> activeObj;
	/** Controls the current stage */
	private int stage = 1;
	/** Used to be able to tell the time to move to the next stage*/
	private boolean prepareStage = true;
	
	private boolean active = false;
	private boolean control = false;
	
	// DIFFICULTY CONTROLS
	private int PLAYER_MOVEMENT_DELAY = 20;
	
	@Override
	public void start() {
		manageWindow();
		manageComponents();
		playerControl();
		
		createFases();
		
		instructions();
		
		letsBegin();
		
		active = true;
		control = true;
	}
	
	/**
	 * Main thread, "Tick control"
	 */
	private void letsBegin() {
		try {
			while (true) {
				checkRules();
				prepareStage();
				moveEmenies();
				repaint();
				Thread.sleep(50);
			}
		} catch (Exception ex) {
			error(ex);
		}
	}
	
	/**
	 * Veriy if player lost and exit, or won and go to next stage
	 */
	private void checkRules() {
		if (control && active) {
			if (arrows == 0 && activeObj.size() > 0) {
				active = false;
				control = false;
				msg("You lost");
				System.exit(0);
			} 
			
			if (activeObj != null && activeObj.size() == 0) {
				active = false;
				control = false;
				msg("You won! NEXT STAGE");
				stage += 1;
				prepareStage = true;
			}
		}
	}
	
	/**
	 * Prepare the next stage, put all objects on the screen and reset control variables
	 */
	private void prepareStage() {
		if (prepareStage) {
			prepareStage = false;
			try {
				fases.get(stage).forEach(x -> {
					add(x);
					x.setVisible(true);
				});
				activeObj = fases.get(stage);
				active = true;
				control = true;
				arrows = 10;
				shoots = 0;
				
				if (PLAYER_MOVEMENT_DELAY > 5) {
					PLAYER_MOVEMENT_DELAY += 1;
				}
				
				countArrows();
				msg("Stage ["+stage+"]. Get ready!");
			} catch (Exception ex) {
				msg("Oops, no more stages");
				System.exit(0);
			}
		}
	}
	
	private void moveEmenies() {
		activeObj.forEach(x -> x.move());
	}

	private void manageWindow() {
		setSize(800, 500);
		setPreferredSize(new Dimension(800, 500));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		setLocationRelativeTo(null);
	}
	
	private void manageComponents() {
		lblArrows.setBounds(680, 440, 100, 20);
		lblArrows.setHorizontalAlignment(JLabel.RIGHT);
		add(lblArrows);
		countArrows();
	}
	
	private void countArrows() {
		lblArrows.setText("");
		for (int i = 0 ; i < arrows ; i++) {
			lblArrows.setText(lblArrows.getText() + " |");
		}
	}

	private void playerControl() {
		newPlayerDefault("#");
		player.setBounds(20, 50, 20, 20);
		player.setText(")>");
		player.setFont(new Font("Arial", Font.BOLD, 18));
		player.setBorder(null);
		player.setForeground(Color.LIGHT_GRAY);
		
		// Automatic movement
		new Thread(new Runnable() {
			private boolean up = false;
			@Override
			public void run() {
				while (true) {
					try {
						// Shows or not the player, accordingly to game runtime rules
						if (active) {
							player.setVisible(true);
							
							// Only moves the player if allowed
							if (control) {
								if (up) {
									if (player.getY() < 10) {
										// If hit top, change direction
										up = false;
									} else {
										player.setBounds(10, player.getY() - 3, 20, 20);
									}
								} else {
									if (player.getY() > 430) {
										// If hit bottom, change direction
										up = true;
									} else {
										player.setBounds(10, player.getY() + 3, 20, 20);
									}
								}
							}
							
						} else {
							player.setVisible(false);
						}
						Thread.sleep(PLAYER_MOVEMENT_DELAY);
					} catch (Exception ex) {
						error(ex);
					}
				}
			}
		}).start();
		
		// Shoot control
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) { }
			@Override
			public void keyReleased(KeyEvent e) {

				// Only shoot if allowed and has arrows left
				if (control && shoots <= 10 && e.getKeyCode() == KeyEvent.VK_SPACE) {
					shoots += 1;
					final Obj o = new Obj("-->");
					o.setForeground(Color.BLACK);
					o.setBorder(null);
					o.setFont(F);
					o.setBounds(25, player.getY(), 25, 20);
					add(o);
					
					// Create a new thread for each arrow that will control everything related to it
					new Thread(new Runnable() {
						@Override
						public void run() {
							// Only proceed if arrow is inside screen 
							while (o.getX() < getWidth()) {
								try {
									// Move arrow 4 pixels to the right
									o.setBounds(o.getX() + 4, o.getY(), 25, 20);
									// If there are enemies left...
									if (activeObj != null && !activeObj.isEmpty()) {
										// Look for impact on any enemy
										// The arrow will not stop until leaves the screen, can hit multiple enemies
										for (int i = activeObj.size() - 1 ; i >= 0 ; i--) {
											if (o.intersects(activeObj.get(i))) {
												// If impact, removes only the enemy object
												remove(activeObj.get(i));
												activeObj.remove(i);
											}
										}
									}
									Thread.sleep(20);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
							// If the 'while' ended, than the arrow left the screen, must be removed and this thread terminated
							try {
								arrows -= 1; // remove one arrow from storage
								countArrows(); // Update arrow count on screen
								remove(o);
								System.out.println("object removed");
							} catch (Exception ex) {}
							System.out.println("Thread will die");
						}
					}).start();
				}
			}
			@Override
			public void keyPressed(KeyEvent e) { }
		});
		
		add(player);
	}
	
	
	@Override
	public void instructions() {
		msg("Use [SPACE] to shoot, you have a limited number of arrows, try to clean the stage");
	}
	
	
	
	/**
	 * Create the possible stages
	 */
	private void createFases() {
		final int w = getWidth();
		final int h = getHeight();
		
		// Fase 1: static obj
		// Random horizontal starting position, between 100 and (screen width - 50)
		final List<ObjPattern> f1 = new ArrayList<>();
		f1.add(new ObjPattern(".", w, h).dontMove());
		f1.add(new ObjPattern(".", w, h).dontMove());
		f1.add(new ObjPattern(".", w, h).dontMove());
		f1.add(new ObjPattern(".", w, h).dontMove());
		f1.add(new ObjPattern(".", w, h).dontMove());
		f1.add(new ObjPattern(".", w, h).dontMove());
		f1.add(new ObjPattern(".", w, h).dontMove());
		f1.add(new ObjPattern(".", w, h).dontMove());
		fases.put(1, f1);
		
		// Stage 2: Moving objects, but less than number of arrows
		final List<ObjPattern> f2 = new ArrayList<>();
		f2.add(new ObjPattern(".", w, h).upDownMoviment());
		f2.add(new ObjPattern(".", w, h).upDownMoviment());
		f2.add(new ObjPattern(".", w, h).upDownMoviment());
		f2.add(new ObjPattern(".", w, h).upDownMoviment());
		f2.add(new ObjPattern(".", w, h).upDownMoviment());
		f2.add(new ObjPattern(".", w, h).upDownMoviment());
		fases.put(2, f2);
		
		// Stage 3: Moving objects and more than number of arrows
		final List<ObjPattern> f3 = new ArrayList<>();
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		f3.add(new ObjPattern(".", w, h).upDownMoviment());
		fases.put(3, f3);
		
	}
	
}
