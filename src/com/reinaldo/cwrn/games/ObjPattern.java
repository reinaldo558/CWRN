package com.reinaldo.cwrn.games;

import java.awt.Color;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class ObjPattern extends JLabel {

	private static final long serialVersionUID = 1L;
	
	private static final Random R = new Random();
	
	private int h = 0;
	private int w = 0;
	
	// 1 = updown | 2 = circle | 3 = triangle
	private int direction = 0;
	
	// up and down control
	private boolean up = R.nextBoolean();

	/**
	 * Creates a default JLabel object to be moved with a predefined pattern <br/><br/>
	 * 
	 * The object will be created with Random X and Y position: <br/>
	 *  X: Random starting position between <b>100</b> and <b>screenW - 50</b><br/>
	 *  Y: Random starting position between <b>10</b> and <b>screenH - 40</b><br/><br/>
	 *  
	 * The object will have fixed size (hit box) of 30x30<br/>
	 * The object will have border<br/>
	 * The moviment rules and controls are defined here, check <b>see also</b> area<br/>
	 * 
	 * @param text String that will be shown inside the 30x30 area
	 * @param screenW Width of the playable screen
	 * @param screenH Height of the playable screen
	 * 
	 * @see #dontMove()
	 * @see #upDownMoviment()
	 * @see #circleMoviment()
	 * @see #triangleMoviment()
	 * @see #move()
	 * */
	public ObjPattern(String text, int screenW, int screenH) {
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBackground(Color.BLACK);
		setText(text);
		w = screenW - 150;
		h = screenH - 50;
		setBounds(R.nextInt(w) + 100, R.nextInt(h)+10, 30, 30);
	}
	
	/**
	 * Define this object as static, forcing it to stay still inside the playable area
	 * @return
	 */
	public ObjPattern dontMove() {
		direction = 0;
		//setBounds(getX(), R.nextInt(h)+10, getWidth(), getHeight());
		return this;
	}
	
	/**
	 * Define this object with up and down moviment <br/>
	 * The initial trajetory will be random (starting going up or starting going down) <br/>
	 * @return
	 */
	public ObjPattern upDownMoviment() {
		direction = 1;
		return this;
	}
	
	/**
	 * Define this object with circle moviment, with: <br/>
	 *  - Diameter of 300 pixels<br/>
	 *  - Direction Random <br/>
	 *  - Starting X: 400 to (screen width - 200) <br/>
	 *  - Starting Y: 200 to (screen height - 200) <br/>
	 * @return 
	 */
	public ObjPattern circleMoviment() {
		direction = 2;
		return this;
	}
	
	/**
	 * Define this object with a triangle moviment, with: <br/>
	 *  - Random triangle shape (/\  <   >   \/) <br/>
	 *  - Random direction <br/>
	 *  - All "paths" having the 200 pixels <br/>
	 *  - Starting X: 300 to (screen width - 100) <br/>
	 *  - Starting Y: 150 to (screen height - 100) <br/>
	 * @return
	 */
	public ObjPattern triangleMoviment() {
		direction = 3;
		return this;
	}
	
	
	/**
	 * To be called inside the "TICK Control Thread" <br/>
	 * Controls this object movement <br/>
	 * 
	 * @see #dontMove()
	 * @see #upDownMoviment()
	 * @see #circleMoviment()
	 * @see #triangleMoviment()
	 */
	public void move() {
		if (direction == 1) {
			moveUpDown();
		} else if (direction == 2) {
			moveCircle();
		} else if (direction == 3) {
			moveTriangle();
		}
	}
	
	private void moveUpDown() {
		if (up) {
			if (getY() < 20) {
				up = false;
			} else {
				setBounds(getX(), getY() - 1, getWidth(), getHeight());
			}
		} else {
			if (getY() > h) {
				up = true;
			} else {
				setBounds(getX(), getY() + 1, getWidth(), getHeight());
			}
		}
	}
	
	private void moveCircle() {
		
	}
	
	private void moveTriangle() {
		
	}
	
	//private void moveDiagonal() {}
	
	//private void moveVPattern() {}
	
	//private void moveArc() {}
	
	//private void moveSnake() {}
	
}
