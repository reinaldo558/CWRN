package com.reinaldo.cwrn.games.avoid;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.reinaldo.cwrn.games.Game;
import com.reinaldo.cwrn.games.Obj;

public class Avoid extends Game {
	
	private static final long serialVersionUID = 255012853156012520L;
	
	private static final List<Obj> ENEMIES = new ArrayList<Obj>(50); 
	
	private static int MAX_ENEMIES = 40;
	private static int SPEED_CHASE = 40;
	
	private static int control = 0;
	
	
	private final Random R = new Random();

	@Override
	public void start() {
		manageWindow();
		manageComponents();
		letsBegin();
		manageRules();
	}
	
	@Override
	public void instructions() {
		msg("Use the keyboard arrows to control left and right and AVOID the falling objects");
	}
	
	
	
	private void manageWindow() {
		setSize(250, 500);
		setPreferredSize(new Dimension(200, 500));
		
	}

	private void manageComponents() {
		createPlayerLeftRight(120, 430, "<", ">","/\\");
		player.setLocation(120, 430);
		player.setVisible(true);
	}
	
	private void manageRules() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				try {
					while (GAME_RUNNING) {
						// Manage Creation ------------------------------------------------------------------------------
						if (ENEMIES.size() < MAX_ENEMIES && control == 5) {
							final Obj o = new Obj("###");
							o.setBounds(R.nextInt(230), -10, 20, 10);
							o.setVisible(true);
							add(o);
							ENEMIES.add(o);
						}
						
						// Manage Moviment ------------------------------------------------------------------------------
						for (int i = ENEMIES.size() - 1 ; i >= 0; i--) {
							final Obj o = ENEMIES.get(i);
							if (o.intersects(player)) {
								endgame();
							} else {
								if (control % 2 == 1) {
									o.moveDown();
								} 
							}
							
							if (o.getY() > getHeight()) {
								remove(o);
								ENEMIES.remove(i);
							}
						}
						
						if (++control > 5) {
							control = 0;
						}
						
						Thread.sleep(SPEED_CHASE);
					}
				} catch (Exception ex) {
					error(ex);
				}
			}
		}).start();
	}
	
	
	
	private void endgame() {
		GAME_RUNNING = false;
		MOVING_TO = '0';
		msg("You lose!");
		System.exit(0);
	}
	
	
	private void letsBegin() {
		setVisible(true);
		instructions();
		GAME_RUNNING = true;
	}
}
