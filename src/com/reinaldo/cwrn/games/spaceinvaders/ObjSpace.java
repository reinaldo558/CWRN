package com.reinaldo.cwrn.games.spaceinvaders;

import java.awt.Color;

import javax.swing.BorderFactory;

import com.reinaldo.cwrn.games.Obj;

public class ObjSpace extends Obj {

	private int direction = 0;
	private int movementControl = 0;
	private int maxMove = 0;
	
	private boolean left = false;
	
	private static final long serialVersionUID = 2260735970903041055L;

	public ObjSpace(int x, int y, int maxMovement) {
		super("*V*");
		maxMove = maxMovement;
		setBorder(BorderFactory.createEmptyBorder());
		setForeground(Color.BLACK);
		setBackground(Color.BLACK);
		setBounds(x, y, 25, 20);
		setHorizontalTextPosition(CENTER);
		setHorizontalAlignment(CENTER);
		setVisible(true);
	}
	
	public ObjSpace movementLeftRight() {
		direction = 1;
		return this;
	}
	
	public ObjSpace noMovement() {
		direction = 0;
		return this;
	}
	
	
	public void move() {
		if (direction == 1) {
			if (left) {
				if (movementControl++ >= maxMove) {
					left = false;
					movementControl = 0;
				}
				setBounds(getX() - 2, getY(), getWidth(), getHeight());
			} else {
				if (movementControl++ >= maxMove) {
					left = true;
					movementControl = 0;
				}
				setBounds(getX() + 2, getY(), getWidth(), getHeight());
			}
		}
	}
	
}
