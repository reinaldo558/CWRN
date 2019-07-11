package com.reinaldo.cwrn.games;


import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class Obj extends JLabel {
	
	private static final long serialVersionUID = 3496539245185546789L;
	
	public boolean toDestroy = false;
	
	/**
	 * 1 = LEFT DOWN <br/>
	 * 2 = LEFT <br/>
	 * 3 = LEFT UP <br/>
	 * 4 = UP <br/>
	 * 5 = RIGHT UP <br/>
	 * 6 = RIGHT <br/>
	 * 7 = RIGHT DOWN <br/>
	 * 8 = DOWN <br/>
	 */
	public int direction = 0;
	
	public void movingLeftDown() { direction = 1; }
	public void movingLeft() { direction = 2; }
	public void movingLeftUp() { direction = 3; }
	public void movingUp() { direction = 4; }
	public void movingRightUp() { direction = 5; }
	public void movingRight() { direction = 6; }
	public void movingRightDown() { direction = 7; }
	public void movingDown() { direction = 8; }

	public boolean isMovingLeftDown() { return direction == 1; }
	public boolean isMovingLeft() { return direction == 2; }
	public boolean isMovingLeftUp() { return direction == 3; }
	public boolean isMovingUp() { return direction == 4; }
	public boolean isMovingRightUp() { return direction == 5; }
	public boolean isMovingRight() { return direction == 6; }
	public boolean isMovingRightDown() { return direction == 7; }
	public boolean isMovingDown() { return direction == 8; }
	
	public Obj(final String label) {
		setText(label);
		setForeground(Color.WHITE);
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createLineBorder(Color.WHITE));
	}
	
	public boolean intersects(final JLabel other) {
		return getBounds().intersects(other.getBounds());
	}
	
	public void moveDown() {
		setBounds(getX(), getY()+5, getWidth(), getHeight());
	}
	

}
