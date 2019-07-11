package com.reinaldo.cwrn.games.survive;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.reinaldo.cwrn.games.Game;
import com.reinaldo.cwrn.games.Obj;

/**
 * 3 Threads
 * 
 * 1 = control the engine (enemies creation, movement, intersections and rules)
 * 2 = control the player 8x movement
 * 3 = control the bullets movement and rules
 * 
 * @author reinaldo.silva
 *
 */
public class Survive extends Game {

	private static final long serialVersionUID = 7047379880185017583L;
	
	private static final Random R = new Random();
	
	/** Controls the engine, every 30 miliseconds, the all objects and actions will be processed  (not counting player movement and bullets) */
	private final int TICK = 30;
	
	/** Every this x {@link #TICK} times, the number of {@link #MAX_ENEMIES} will increase by 5 (500 here is equals to 15 sec) */
	private final int INCREASE_DIFFICULTY = 200;
	
	/** Max number of bullets allowed to exist at the same time, bullets outside screen or that hit an enemy are destroied */
	private final int MAX_BULLETS = 10;
	
	/** Max number of enemies allowed to exist at the same time, this numer will increase during play time, after {@link #INCREASE_DIFFICULTY} x {@link #TICK}*/
	private int MAX_ENEMIES = 20;
	
	/* To store and manipulate temporary objects */
	private List<Obj> BULLETS = new ArrayList<>(MAX_BULLETS);
	private List<Obj> ENEMIES = new ArrayList<>(MAX_ENEMIES);
	

	/* to help control player`s movement */
	private boolean left = false;
	private boolean up = false;
	private boolean right = false;
	private boolean down = false;
	
	/** Amount of pixels the player can jump ate every movement (less pixels means slow movement, more pixels means fast but it doesn`t move smooth) */
	private final int PM = 3;
	
	
	private final JLabel lblPoints = new JLabel("0");
	private int points = 0;
	
	@Override
	public void start() {
		createWindow();
		GAME_RUNNING = true;
		
		createPlayer8AxisShoot(new ImageIcon[] {
				 createImage("up.png", this), 
				 createImage("left_down.png", this), 
				 createImage("left.png", this), 
				 createImage("left_up.png", this), 
				 createImage("up.png", this), 
				 createImage("right_up.png", this), 
				 createImage("right.png", this), 
				 createImage("right_down.png", this), 
				 createImage("down.png", this)});
		
		instructions();
		bulletListener();
		
		// Main Thread, will control enemies creation, movement, action, and game rules 
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TICK CONTROL
				try {
					int doit = 0;
					int difficulty_control = 0;
					
					while (GAME_RUNNING) {
						if (doit == 3) {
							createEnemies();
						}
						
						if (++doit == 2 || doit == 5) {
							moveEnemies();
						}
						
						controlBullets();
						
						checkEnemyIntersections();
						
						
						if (doit > 5) {
							doit = 0;
						}
						
						difficulty_control++;
						if (difficulty_control >= INCREASE_DIFFICULTY) {
							difficulty_control = 0;
							MAX_ENEMIES += 5;
						}
						Thread.sleep(TICK);
					}
				} catch (Exception ex) {
					error(ex);
				}
			}
		}).start();
	}

	/**
	 * To be called before game starts, summary of controls and objective
	 * */
	@Override
	public void instructions() {
		msg("Move 8 directions with [ ARROWS ], shoot with [ SPACE ], try to survive");
	}
	
	
	/**
	 * Create a player controllable object, with 8 axis movement and Shooting control<br/>
	 * The player "hit box" or size will be 20x20<br/>
	 * The method already limit the movement based on the window size<br/><br/>
	 * 
	 * The String[] appearance should be:<br/>
	 * 0 = NO MOVEMENT <br/>
	 * 1 = LEFT DOWN <br/>
	 * 2 = LEFT <br/>
	 * 3 = LEFT UP <br/>
	 * 4 = UP <br/>
	 * 5 = RIGHT UP <br/>
	 * 6 = RIGHT <br/>
	 * 7 = RIGHT DOWN <br/>
	 * 8 = DOWN <br/>
	 * 
	 * @param apperance
	 */
	public void createPlayer8AxisShoot(ImageIcon[] apperance) {
		newPlayerDefault("");
		player.setIcon(apperance[0]);
		player.setBounds(300, 100, 20, 20);
		player.setFont(new Font("Arial", Font.PLAIN, 10));
		player.setBorder(BorderFactory.createEmptyBorder());
		addKeyListener();
		
		// Start a thread to control the movement, if any, based on the state variables
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					
					while (GAME_RUNNING) {
						
						if (left && up && player.getX() > 5 && player.getY() > 5) { 
							player.setLocation(player.getX() - PM, player.getY() - PM);
							player.setIcon(apperance[3]);
							player.movingLeftUp();
						} else if (left && down && player.getX() > 5 && (player.getY() + 60) < getHeight()) {
							player.setLocation(player.getX() - PM, player.getY() + PM);
							player.setIcon(apperance[1]);
							player.movingLeftDown();
						} else if (right && up && ((player.getX()+40) < getWidth()) && player.getY() > 5) {
							player.setLocation(player.getX() + PM, player.getY() - PM);
							player.setIcon(apperance[5]);
							player.movingRightUp();
						} else if (right && down && ((player.getX()+40) < getWidth()) && ((player.getY()+60) < getHeight())) {
							player.setLocation(player.getX() + PM, player.getY() + PM);
							player.setIcon(apperance[7]);
							player.movingRightDown();
						} else if (left && player.getX() > 5) {
							player.setLocation(player.getX() - PM, player.getY());
							player.setIcon(apperance[2]);
							player.movingLeft();
						} else if (up && player.getY() > 5) {
							player.setLocation(player.getX(), player.getY() - PM);
							player.setIcon(apperance[4]);
							player.movingUp();
						} else if (right && (player.getX()+40) < getWidth()) {
							player.setLocation(player.getX() + PM, player.getY());
							player.setIcon(apperance[6]);
							player.movingRight();
						} else if (down && (player.getY()+60) < getHeight()) {
							player.setLocation(player.getX(), player.getY() + PM);
							player.setIcon(apperance[8]);
							player.movingDown();
						} else {
							//player.setIcon(apperance[0]);
							//Do nothng, leave the last saved position so the shooting can happen even with player is still
						}
						
						Thread.sleep(25); //need it to be 20 or 25 or 30 for all velocities to work together, player, bullet, enemy
					}
					
				} catch (Exception ex) {
					error(ex);
				}
			}
		}).start();
		
	}
	
	/**
	 * Define the state of movement, doing like this multiple keys can me interpreted at the same time <br/>
	 * A thread will read the 4 variables and control the movement and possible movement combination
	 */
	private void addKeyListener() {
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
				if (e.getKeyCode() == KeyEvent.VK_UP) up = false;
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) right= false;
				if (e.getKeyCode() == KeyEvent.VK_DOWN) down = false;
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_LEFT) left = true;
				if (e.getKeyCode() == KeyEvent.VK_UP) up = true;
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) right= true;
				if (e.getKeyCode() == KeyEvent.VK_DOWN) down = true;
			}
		});
	}
	
	
	/**
	 * Adds a keylistener to check for SPACE, and create bullets <br/>
	 * Define the bullet initial possition and it`s aim <br/><br/>
	 * Just {@link #MAX_BULLETS} can exist at the same time
	 * */
	private void bulletListener() {
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE && BULLETS.size() < MAX_BULLETS) {
					final Obj o = new Obj("");
					o.setBorder(BorderFactory.createLineBorder(Color.RED));
					o.setBounds(0, 0, 3, 3);
					
					int x = player.getX();
					int y = player.getY();
					
					if (player.isMovingLeftUp()) {
						o.setLocation(x, y);
						o.movingLeftUp();
					} else if (player.isMovingLeftDown()) {
						o.setLocation(x, y+20);
						o.movingLeftDown();
					} else if (player.isMovingRightUp()) {
						o.setLocation(x+20, y);
						o.movingRightUp();
					} else if (player.isMovingRightDown()) {
						o.setLocation(x+20, y+20);
						o.movingRightDown();
					} else if (player.isMovingUp()) {
						o.setLocation(x+9, y);
						o.movingUp();
					} else if (player.isMovingDown()) {
						o.setLocation(x+9, y+20);
						o.movingDown();
					} else if (player.isMovingLeft()) {
						o.setLocation(x, y+9);
						o.movingLeft();
					} else if (player.isMovingRight()) {
						o.setLocation(x+20, y+9);
						o.movingRight();
					}
					
					add(o);
					BULLETS.add(o);
				}
			}
		});
	}
	
	private void createWindow() {
		setSize(600, 300);
		setPreferredSize(new Dimension(600, 400));
		setVisible(true);
		lblPoints.setBounds(getWidth() - 70, 5, 50, 20);
		lblPoints.setHorizontalAlignment(JLabel.RIGHT);
		add(lblPoints);
	}
	
	
	/**
	 * Controls the bullet movement, based on its aim defined during the bullet creation <br/><br/>
	 * If the bullet is outside the screen area the bullet is destroied (and another one can be created) <br/>
	 * If the bullet hits an enemy, both are destroied (another bullet and another enemy can be created) <br/>
	 * */
	private void controlBullets() {
		try {
			int m = 5; // Wanted 1, but needs to be faster than player
			for (int i = BULLETS.size()-1 ; i>=0; i--) {
				final Obj o = BULLETS.get(i);
				
				// Bullet movement
				if (o.isMovingLeftDown()) { o.setLocation(o.getX()-m, o.getY()+m);} 
				else if (o.isMovingLeft()) { o.setLocation(o.getX()-m, o.getY());} 
				else if (o.isMovingLeftUp()) { o.setLocation(o.getX()-m, o.getY()-m);} 
				else if (o.isMovingUp()) { o.setLocation(o.getX(), o.getY()-m);} 
				else if (o.isMovingRightUp()) { o.setLocation(o.getX()+m, o.getY()-m);} 
				else if (o.isMovingRight()) { o.setLocation(o.getX()+m, o.getY());} 
				else if (o.isMovingRightDown()) { o.setLocation(o.getX()+m, o.getY()+m);} 
				else if (o.isMovingDown()) { o.setLocation(o.getX(), o.getY()+m);} 
				
				// Destroy if outside screen area
				if (o.getY() < -5 || o.getX() < -5 || o.getX() > getWidth() || o.getY() > getHeight()) {
					remove(o);
					BULLETS.remove(i);
				} 
				// If inside screen area, check for impact on enemies
				else {
					for (int x = ENEMIES.size() - 1 ; x>=0; x--) {
						Obj e = ENEMIES.get(x);
						
						// If hit the current enemy, both are destroied
						if (o.intersects(e)) {
							remove(o);
							BULLETS.remove(i);
							
							remove(e);
							ENEMIES.remove(x);
							
							addPoint();
							continue;
						}
					}
				}
			}
		} catch (Exception ex) {
			repaint();
		}
	}
	
	
	/**
	 * Create enemies in random positions, starting slightly outside the screen area <br/><br/>
	 * Up to {@link #MAX_ENEMIES} can be created
	 * */
	private void createEnemies() {
		if (ENEMIES.size() < MAX_ENEMIES) {
			final Obj o = new Obj("#");
			o.setBounds(-50, -50, 20, 20);
			o.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			
			// define the starting position
			final int pos = R.nextInt(40);
			final int w = getWidth();
			final int h = getHeight();
			// LEFT
			if (pos < 10) {
				o.setLocation(-4, R.nextInt(h));
			}
			// TOP
			else if (pos < 20) {
				o.setLocation(R.nextInt(w), -4);
			}
			// RIGHT
			else if (pos < 30) {
				o.setLocation(w, R.nextInt(h));
			}
			// BOTTOM
			else {
				o.setLocation(R.nextInt(w), h);
			}
			
			
			add(o);
			ENEMIES.add(o);
		}
	}
	
	/**
	 * Locate where the player is and move all enemies a few pixels close to that 
	 * position, doesn`t matter where the enemy is, it will try to follow the player.
	 * */
	private void moveEnemies() {
		final int m = 2;
		
		// Due to I don`t know why, using the enemy in a variable made some enemies to not follow the player
		// So, I`m using below the enemy inside the list
		for (int i = 0 ; i < ENEMIES.size() ; i++) {
			final int x = ENEMIES.get(i).getX();
			final int y = ENEMIES.get(i).getY();
			
			// player is below and to the left side of this obj
			if (player.getX()+9 < x && player.getY()+9 > y) {
				ENEMIES.get(i).setBounds(x-m, y+m, 20, 20);
			}
			// player is above an to the left side of this obj
			else if (player.getX()+9 < x && player.getY()+9 < y) {
				ENEMIES.get(i).setBounds(x-m, y-m, 20, 20);
			}
			// player is below and to the right side of this obj
			else if (player.getX()+9 > x && player.getY()+9 > y) {
				ENEMIES.get(i).setBounds(x+m, y+m, 20, 20);
			}
			// player is above and to the right side of obj
			else if (player.getX()+9 > x && player.getY()+9 < y) {
				ENEMIES.get(i).setBounds(x+m, y-m, 20, 20);
			}
			// player is on the left side of this obj
			else if (player.getX()+9 < x && player.getY()+9 == y) {
				ENEMIES.get(i).setBounds(x-m, y, 20, 20);
			}
			// player is on the right side of obj
			else if (player.getX()+9 > x && player.getY()+9 == y) {
				ENEMIES.get(i).setBounds(x+m, y, 20, 20);
			}
			// player is above obj
			else if (player.getX()+9 == x && player.getY()+9 < y) {
				ENEMIES.get(i).setBounds(x, y-m, 20, 20);
			}
			// player is below this obj
			else if (player.getX()+9 == x && player.getY()+9  > y) {
				ENEMIES.get(i).setBounds(x, y+m, 20, 20);
			}
		}
	}
	
	/**
	 * Loop through all enemies check for: <br/>
	 * if outside screen area, destry current enemy <br/>
	 * If hit the player, start game over process
	 * */
	private void checkEnemyIntersections() {
		for (int i = ENEMIES.size()-1 ; i>=0 ;i--) {
			final Obj o = ENEMIES.get(i);
			if (o.getX() < -10 || o.getY() < -10 || o.getX() > getWidth() || o.getY() > getHeight()) {
				remove(o);
				ENEMIES.remove(i);
			} else if (o.intersects(player)) {
				endGame();
			}
		}
	}
	
	
	private void addPoint() {
		lblPoints.setText(++points + "");
	}
	
	
	/**
	 * Game over process <br/>
	 * 
	 * TODO: Stop player last moviment
	 */
	private void endGame() {
		msg("You lost");
		GAME_RUNNING = false;
		left = false;
		right = false;
		up = false;
		down = false;
		System.exit(0);
	}

}
