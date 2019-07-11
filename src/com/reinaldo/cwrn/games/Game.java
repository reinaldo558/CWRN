package com.reinaldo.cwrn.games;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;


public abstract class Game extends JFrame {
	
	// L / R / U / D / 0 
	public static char MOVING_TO = '0';
	
	public static boolean GAME_RUNNING = false;
	
	public Obj player;
	
	/** Used to remove the window key listeners while a message is being shown, after the message the listeners goes back to normal */
	AWTEventListener myListener = new AWTEventListener() {
        @Override
        public void eventDispatched(AWTEvent event) {
            if (event instanceof KeyEvent) {
            	if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_SPACE) {
            		((KeyEvent) event).consume();
            	}
            }
        }
    };
	
	
	public Game() {
		setLayout(null);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	private static final long serialVersionUID = -5143749960782661836L;

	public abstract void start();
	
	public abstract void instructions();
	
	public void msg(final String txt) {
		Toolkit.getDefaultToolkit().addAWTEventListener(myListener, AWTEvent.KEY_EVENT_MASK);
	    JOptionPane.showMessageDialog(this, txt);
	    Toolkit.getDefaultToolkit().removeAWTEventListener(myListener);
	}
	
	
	
	
	
	/**
	 * Create a player controllable object, with moviment and controls for LEFT and RIGHT only<br/>
	 * No shooting, no up, no down<br/>
	 * This method already limit the moviment based on the window size<br/>
	 * The player "hit box" or size, will be 20x20
	 * @param startingX Define the horizontal starting position of the player
	 * @param startingY Define the vertical starting position of the player
	 * @param apperanceLEFT Define the character to be shown as the player is moving LEFT
	 * @param apperanceRIGHT Define the character to be shown as the player is moving RIGHT
	 * @param apperanceDEFAULT Define the default character to show
	 */
	public void createPlayerLeftRight(int startingX, int startingY, final String apperanceLEFT, final String apperanceRIGHT, final String apperanceDEFAULT) {
		newPlayerDefault(apperanceDEFAULT);
		
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {				
				moveLeftAndRight(e);
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				MOVING_TO = '0';				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {				
				moveLeftAndRight(e);
			}
		});
		
		
		// TODO CREATE A THREAD TO READ THE INT VARIABLE AND CONTROL THE PLAYER
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						while (true) {
							if (MOVING_TO == 'L') {
								player.setText(apperanceLEFT);
								player.setBounds(player.getX() - 4, player.getY(), 20, 20);
							} else if (MOVING_TO == 'R') {
								player.setText(apperanceRIGHT);
								player.setBounds(player.getX() + 4, player.getY(), 20, 20);
							} else {
								player.setText(apperanceDEFAULT);
							}
							Thread.sleep(40);
						}
					} catch (Exception ex) {
						error(ex);
					}
				}
			}
		}).start();
		
	}
	
	
	
	private void moveLeftAndRight(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT && player.getX() > 5) {
			MOVING_TO = 'L';
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			if ((player.getX() + 20) < getWidth()) {
				MOVING_TO = 'R';
			} else {
				MOVING_TO = '0';
			}
		} else {
			MOVING_TO = '0';
		}
	}
	
	public void newPlayerDefault(final String lbl) {
		player = new Obj(lbl);
		player.setForeground(Color.RED);
		player.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
		player.setHorizontalAlignment(JLabel.CENTER);
		player.setFont(new Font("Arial", Font.PLAIN, 16));
		add(player);
		player.setVisible(true);
	}
	
	public void error(Exception ex) {
		ex.printStackTrace();
		msg(ex.getMessage());
		System.exit(0);
	}
	
	
	public ImageIcon createImage(String imgNamePath, Object clazz) {
		URL imageURL = clazz.getClass().getResource(imgNamePath);
        return new ImageIcon(imageURL, "");
	}

}
