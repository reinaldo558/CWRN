package com.reinaldo.cwrn.games.spaceinvaders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import com.reinaldo.cwrn.games.Game;
import com.reinaldo.cwrn.games.Obj;

/**
 * No end space invaders
 * 
 * - Stages can be easily created here, see {@link #createStages()}
 * 
 * - There is no end (if you win), once all stages have been cleared, everything 
 *   resets to the starting value but the difficulty control variables are doubled 
 *   
 * - Using only two (manual) Threads, one for the player to move smoothly and 
 *   another Thread to control everything else (game tick)
 * 
 * @author reinaldo.silva
 *
 */
public class Space extends Game {
	
	
	private static final long serialVersionUID = 1L;
	
	/** For performance, to not create a new border with every alien */
	private static final Border BULLET_BORDER = BorderFactory.createLineBorder(Color.RED);
	/** To help on some decisions */
	private static final Random R = new Random();

	// Player movement control helpers
	private static boolean moveLeft = false;
	private static boolean moveRight = false;

	/** - Contains all stages for this game (more can be created coding {@link #createStages()} ) <br/>
	 *  - help to manage all current enemies, their movement and actions */
	private Map<Integer, List<ObjSpace>> stages = new HashMap<Integer, List<ObjSpace>>();
	/** Current stage control */
	private int stage = 0;
	/** Change stage control */
	private boolean changeStage = true;
	
	/** Stores all bulets shoot by aliens, to help manipulate their movement and rules */
	private final List<Obj> bulletsAliens = new ArrayList<>();
	/** Stores all bullets shoot by the player, to help manipulate their movement and rules */
	private final List<Obj> bullets = new ArrayList<Obj>();
	/** Max number of bullets alowed to exist at the same time on screen, for the player */
	private int MAX_BULLETS = 20;
	/** Max number of bullets alowed to exist at the same time on screen, for all aliens combined */
	private int MAX_BULLETS_ALIENS = 50;
	
	/** Performance tweek, to not call the same method hundred of times needlessly */
	private final int PLAYER_Y = 530;

	@Override
	public void start() {
		// Preparations
		createStages();
		manageWindow();
		
		GAME_RUNNING = true;
		managePlayer();
		
		// Game start !
		tickControl();
	}
	
	
	/**
	 * Upon game over, clean all objects and variable (that matter), shows a message and exit the program
	 * */
	private void gameOver() {
		GAME_RUNNING = false;
		MAX_BULLETS = 0;
		MAX_BULLETS_ALIENS = 0;
		
		moveLeft = false;
		moveRight = false;
		
		bulletsAliens.forEach(b -> remove(b));
		bullets.forEach(b -> remove(b));
		
		bulletsAliens.clear();
		bullets.clear();
		
		msg("You lost");
		System.exit(0);
	}
	
	
	/***
	 * Main thread, controls the game, all its rules and actions
	 */
	private void tickControl() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (GAME_RUNNING) {
						prepareStage();
						controlBullets();
						controlAliens();
						controlAlienShooting();
						checkRules();
						Thread.sleep(25);
					}
				} catch (Exception ex) {
					error(ex);
				}
			}
		}).start();
	}
	
	/***
	 * Check of player won
	 */
	private void checkRules() {
		if (stages.get(stage).isEmpty()) {
			msg("You won, get ready for next stage..");
			changeStage = true;
		}
	}
	
	
	
	private void controlAlienShooting() {
		// Create (maybe) a new bullet for each alien
		try {
			stages.get(stage).forEach(alien -> {
				// I can put a timer on each alien, to shoot every 5 seconds for example, but it is easier to have a random making the decision
				// In this case 0.33% change to shoot every tick (25ms) for each alien
				if (bulletsAliens.size() < MAX_BULLETS_ALIENS && R.nextInt(300) == 77) {
					int x = alien.getX();
					int y = alien.getY();
					
					final Obj bullet = new Obj(".");
					bullet.setBorder(BorderFactory.createLineBorder(Color.BLACK));
					bullet.setBounds(x+12, y+20, 3, 3);
					
					add(bullet);
					bulletsAliens.add(bullet);
				}
			});
			
			
			// Moves the bullet down
			bulletsAliens.forEach(b -> b.setBounds(b.getX(), b.getY()+2, 3, 3));
			
			// Check for impact on player (game over) or if left the screen (destroy bullet)
			for (int i = bulletsAliens.size()-1 ; i>= 0; i--) {
				final Obj b = bulletsAliens.get(i);
				if (b.intersects(player)) {
					gameOver();
				} else {
					if (b.getY() > getHeight() - 10) {
						remove(b);
						bulletsAliens.remove(i);
					}
				}
			}
		} catch (Exception ex) {}
		
	}
	
	
	/**
	 * Just move all aliens, if they have the movement function
	 */
	private void controlAliens() {
		try {
			stages.get(stage).forEach(alien -> {
				alien.move();
			});
		} catch (Exception ex) {}
	}
	
	
	/**
	 * Reset all control variables and objects, increase the stage and paint all new elements on the screen 
	 */
	private void prepareStage() {
		if (changeStage) {
			System.out.println("preparing stage");
			changeStage = false;
			stage += 1;
			
			bullets.forEach(b -> remove(b));
			bullets.clear();
			
			bulletsAliens.forEach(b -> remove(b));
			bulletsAliens.clear();
			
			if (stages.containsKey(stage)) {
				stages.get(stage).forEach(enemy -> add(enemy));
			} else {
				// Out of stages, reset everything and double the difficulty
				createStages();
				
				//less ammo
				if (MAX_BULLETS > 5) {
					MAX_BULLETS -= 5;
				}
				
				//aliens shooting doubled
				MAX_BULLETS_ALIENS *= 2;
				
				stage = 0;
				changeStage = true;
				
				msg("Congratulations, you beat this run, prepare for double difficulty");
				
				prepareStage();
			}
			
			
			try {
				repaint();
			} catch (Exception ex) {}
		}
	}
	
	
	/**
	 * Move bullets, check for impact on aliens, check if bullet left the screen boundaries and control if bullet still needs to exist
	 */
	private void controlBullets() {
		for (int i = bullets.size() - 1 ; i >= 0 ; i--) {
			final Obj bullet = bullets.get(i);
			// If outside screen, destroy
			if (bullet.getY() < -5) {
				remove(bullet);
				bullets.remove(i);
			} 
			// If inside screen
			else {
				// Move bullet upwards
				bullet.setBounds(bullet.getX(), bullet.getY() - 4, bullet.getWidth(), bullet.getHeight());
				
				final List<ObjSpace> aliens = stages.get(stage);
				// Check all current aliens for impact
				if (aliens != null && !aliens.isEmpty()) {
					for (int j = aliens.size() - 1 ; j >= 0 ; j--) {
						final Obj alien = aliens.get(j);
						// If bullet hit alien, destroy alien and bullet
						if (bullet.intersects(alien)) {
							remove(alien);
							aliens.remove(j);
							
							remove(bullet);
							bullets.remove(i);
							
							try {
								repaint();
							} catch (Exception ex) {}
							
							continue;
						}
					}
				}
				
			}
			
			
		}
	}
	
	private void managePlayer() {
		newPlayerDefault("/\\");
		player.setBounds(400, PLAYER_Y, 20, 20);
		player.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					moveLeft = false;
				} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					moveRight = false;
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					if (bullets.size() < MAX_BULLETS) {
						shoot();
					}
				} else {
					if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						moveLeft = true;
					} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						moveRight = true;
					} 
				}
			}
		});
		
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (GAME_RUNNING) {
					try {
						if (moveLeft) {
							player.setText("|\\");
							player.setBounds(player.getX() - 3, PLAYER_Y, 20, 20);
						} else if (moveRight) {
							player.setText("/|");
							player.setBounds(player.getX() + 3, PLAYER_Y, 20, 20);
						} else {
							player.setText("/\\");
						}
						
						if (moveLeft && player.getX() < -10) {
							player.setBounds(780, PLAYER_Y, 20, 20);
						}
						if (moveRight && player.getX() > 780) {
							player.setBounds(-5, PLAYER_Y, 20, 20);
						}
						
						
						Thread.sleep(10);
					} catch (Exception ex) {
						error(ex);
					}
				}
			}
		}).start();
	}
	
	
	private void shoot() {
		int pos = player.getX();
		final Obj bullet = new Obj(".");
		bullet.setBounds(pos+9, PLAYER_Y, 3, 3);
		bullet.setBorder(BULLET_BORDER);
		add(bullet);
		bullets.add(bullet);
	}
	
	
	private void manageWindow() {
		setSize(800, 600);
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Space invaders");
		setLayout(null);
		setVisible(true);
		setLocationRelativeTo(null);
	}
	

	@Override
	public void instructions() {
		msg("use [ARROWS] left and right to move, use [SPACE] to shoot, avoid being killed, kill every alien");
	}
	
	private void createStages() {
		// Stage 1, no movement, 40 enemies (10 col / 4 lines)
		final List<ObjSpace> s1 = new ArrayList<>();
		s1.add(new ObjSpace(20+100, 50, 0).noMovement());
		s1.add(new ObjSpace(70+100, 50, 0).noMovement());
		s1.add(new ObjSpace(120+100, 50, 0).noMovement());
		s1.add(new ObjSpace(170+100, 50, 0).noMovement());
		s1.add(new ObjSpace(220+100, 50, 0).noMovement());
		s1.add(new ObjSpace(270+100, 50, 0).noMovement());
		s1.add(new ObjSpace(320+100, 50, 0).noMovement());
		s1.add(new ObjSpace(370+100, 50, 0).noMovement());
		s1.add(new ObjSpace(420+100, 50, 0).noMovement());
		s1.add(new ObjSpace(470+100, 50, 0).noMovement());
		

		s1.add(new ObjSpace(100+100, 100, 0).noMovement());
		s1.add(new ObjSpace(150+100, 100, 0).noMovement());
		s1.add(new ObjSpace(200+100, 100, 0).noMovement());
		s1.add(new ObjSpace(250+100, 100, 0).noMovement());
		s1.add(new ObjSpace(300+100, 100, 0).noMovement());
		s1.add(new ObjSpace(350+100, 100, 0).noMovement());
		s1.add(new ObjSpace(400+100, 100, 0).noMovement());
		s1.add(new ObjSpace(450+100, 100, 0).noMovement());
		s1.add(new ObjSpace(500+100, 100, 0).noMovement());
		s1.add(new ObjSpace(550+100, 100, 0).noMovement());
		

		s1.add(new ObjSpace(40+100, 150, 0).noMovement());
		s1.add(new ObjSpace(90+100, 150, 0).noMovement());
		s1.add(new ObjSpace(140+100, 150, 0).noMovement());
		s1.add(new ObjSpace(190+100, 150, 0).noMovement());
		s1.add(new ObjSpace(240+100, 150, 0).noMovement());
		s1.add(new ObjSpace(290+100, 150, 0).noMovement());
		s1.add(new ObjSpace(340+100, 150, 0).noMovement());
		s1.add(new ObjSpace(390+100, 150, 0).noMovement());
		s1.add(new ObjSpace(440+100, 150, 0).noMovement());
		s1.add(new ObjSpace(490+100, 150, 0).noMovement());
		

		s1.add(new ObjSpace(80+100, 200, 0).noMovement());
		s1.add(new ObjSpace(130+100, 200, 0).noMovement());
		s1.add(new ObjSpace(180+100, 200, 0).noMovement());
		s1.add(new ObjSpace(230+100, 200, 0).noMovement());
		s1.add(new ObjSpace(280+100, 200, 0).noMovement());
		s1.add(new ObjSpace(330+100, 200, 0).noMovement());
		s1.add(new ObjSpace(380+100, 200, 0).noMovement());
		s1.add(new ObjSpace(430+100, 200, 0).noMovement());
		s1.add(new ObjSpace(480+100, 200, 0).noMovement());
		s1.add(new ObjSpace(530+100, 200, 0).noMovement());
		
		stages.put(1, s1);
		
		//----------------------------------------------------------------------------------------------
		// Stage 2, left and right 15 times, 40 enemies (10 col / 4 lines)
		
		final List<ObjSpace> s2 = new ArrayList<>();
		s2.add(new ObjSpace(150-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(200-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(250-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(300-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(350-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(400-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(450-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(500-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(550-40, 50, 15).movementLeftRight());
		s2.add(new ObjSpace(600-40, 50, 15).movementLeftRight());

		s2.add(new ObjSpace(250-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(300-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(350-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(400-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(450-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(500-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(550-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(600-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(650-40, 100, 15).movementLeftRight());
		s2.add(new ObjSpace(700-40, 100, 15).movementLeftRight());

		s2.add(new ObjSpace(150-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(200-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(250-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(300-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(350-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(400-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(450-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(500-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(550-40, 150, 15).movementLeftRight());
		s2.add(new ObjSpace(600-40, 150, 15).movementLeftRight());

		s2.add(new ObjSpace(250-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(300-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(350-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(400-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(450-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(500-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(550-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(600-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(650-40, 200, 15).movementLeftRight());
		s2.add(new ObjSpace(700-40, 200, 15).movementLeftRight());
		
		stages.put(2, s2);
		//----------------------------------------------------------------------------------------------
		// Stage 3, left and right 40 times, 60 enemies (10 col / 6 lines)
		
		final List<ObjSpace> s3 = new ArrayList<>();
		s3.add(new ObjSpace(150-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(200-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(250-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(300-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(350-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(400-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(450-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(500-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(550-40, 50, 40).movementLeftRight());
		s3.add(new ObjSpace(600-40, 50, 40).movementLeftRight());

		s3.add(new ObjSpace(250-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(300-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(350-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(400-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(450-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(500-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(550-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(600-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(650-40, 100, 40).movementLeftRight());
		s3.add(new ObjSpace(700-40, 100, 40).movementLeftRight());

		s3.add(new ObjSpace(150-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(200-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(250-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(300-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(350-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(400-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(450-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(500-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(550-40, 150, 40).movementLeftRight());
		s3.add(new ObjSpace(600-40, 150, 40).movementLeftRight());

		s3.add(new ObjSpace(250-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(300-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(350-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(400-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(450-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(500-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(550-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(600-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(650-40, 200, 40).movementLeftRight());
		s3.add(new ObjSpace(700-40, 200, 40).movementLeftRight());

		s3.add(new ObjSpace(150-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(200-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(250-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(300-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(350-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(400-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(450-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(500-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(550-40, 250, 40).movementLeftRight());
		s3.add(new ObjSpace(600-40, 250, 40).movementLeftRight());

		s3.add(new ObjSpace(250-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(300-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(350-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(400-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(450-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(500-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(550-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(600-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(650-40, 300, 40).movementLeftRight());
		s3.add(new ObjSpace(700-40, 300, 40).movementLeftRight());
		
		stages.put(3, s3);
		
		//----------------------------------------------------------------------------------------------
		// Stage 4, mixes lines with movement 40 times, lines without movement, 80 enemies (10 col / 8 lines)
		final List<ObjSpace> s4 = new ArrayList<>();
		
		s4.add(new ObjSpace(50, 40, 0).noMovement());
		s4.add(new ObjSpace(100, 40, 0).noMovement());
		s4.add(new ObjSpace(150, 40, 0).noMovement());
		s4.add(new ObjSpace(200, 40, 0).noMovement());
		s4.add(new ObjSpace(250, 40, 0).noMovement());
		s4.add(new ObjSpace(300, 40, 0).noMovement());
		s4.add(new ObjSpace(350, 40, 0).noMovement());
		s4.add(new ObjSpace(400, 40, 0).noMovement());
		s4.add(new ObjSpace(450, 40, 0).noMovement());
		s4.add(new ObjSpace(500, 40, 0).noMovement());
		
		s4.add(new ObjSpace(100, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(150, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(200, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(250, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(300, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(350, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(400, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(450, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(500, 80, 40).movementLeftRight());
		s4.add(new ObjSpace(550, 80, 40).movementLeftRight());

		s4.add(new ObjSpace(150, 120, 0).noMovement());
		s4.add(new ObjSpace(200, 120, 0).noMovement());
		s4.add(new ObjSpace(250, 120, 0).noMovement());
		s4.add(new ObjSpace(300, 120, 0).noMovement());
		s4.add(new ObjSpace(350, 120, 0).noMovement());
		s4.add(new ObjSpace(400, 120, 0).noMovement());
		s4.add(new ObjSpace(450, 120, 0).noMovement());
		s4.add(new ObjSpace(500, 120, 0).noMovement());
		s4.add(new ObjSpace(550, 120, 0).noMovement());
		s4.add(new ObjSpace(600, 120, 0).noMovement());
		
		s4.add(new ObjSpace(50, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(150, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(200, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(250, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(300, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(350, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(400, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(450, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(500, 160, 40).movementLeftRight());
		s4.add(new ObjSpace(550, 160, 40).movementLeftRight());

		s4.add(new ObjSpace(100, 200, 0).noMovement());
		s4.add(new ObjSpace(150, 200, 0).noMovement());
		s4.add(new ObjSpace(200, 200, 0).noMovement());
		s4.add(new ObjSpace(250, 200, 0).noMovement());
		s4.add(new ObjSpace(300, 200, 0).noMovement());
		s4.add(new ObjSpace(350, 200, 0).noMovement());
		s4.add(new ObjSpace(400, 200, 0).noMovement());
		s4.add(new ObjSpace(450, 200, 0).noMovement());
		s4.add(new ObjSpace(500, 200, 0).noMovement());
		s4.add(new ObjSpace(550, 200, 0).noMovement());
		
		s4.add(new ObjSpace(150, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(200, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(250, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(300, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(350, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(400, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(450, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(500, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(550, 240, 40).movementLeftRight());
		s4.add(new ObjSpace(600, 240, 40).movementLeftRight());

		s4.add(new ObjSpace(100, 280, 0).noMovement());
		s4.add(new ObjSpace(150, 280, 0).noMovement());
		s4.add(new ObjSpace(200, 280, 0).noMovement());
		s4.add(new ObjSpace(250, 280, 0).noMovement());
		s4.add(new ObjSpace(300, 280, 0).noMovement());
		s4.add(new ObjSpace(350, 280, 0).noMovement());
		s4.add(new ObjSpace(400, 280, 0).noMovement());
		s4.add(new ObjSpace(450, 280, 0).noMovement());
		s4.add(new ObjSpace(500, 280, 0).noMovement());
		s4.add(new ObjSpace(550, 280, 0).noMovement());
		
		s4.add(new ObjSpace(100, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(150, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(200, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(250, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(300, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(350, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(400, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(450, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(500, 320, 40).movementLeftRight());
		s4.add(new ObjSpace(550, 320, 40).movementLeftRight());
		
		stages.put(4, s4);
	}

} 





