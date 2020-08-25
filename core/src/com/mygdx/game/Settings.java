package com.mygdx.game;

import java.io.File;

public final class Settings {
	
	public static final String VERSION = "0.1";
	public static final boolean RELEASE_MODE = false || new File("../../debug_mode.tmp").exists() == false;
	
	// Debug
	public static final boolean SIMPLE_SCROLLING = !RELEASE_MODE && false;
	public static final boolean SHOW_OUTLINES = !RELEASE_MODE && false;

	public static final int WINDOW_WIDTH_PIXELS = 1200;
	public static final int WINDOW_HEIGHT_PIXELS = (int)(WINDOW_WIDTH_PIXELS * .68);
	public static final int LOGICAL_WIDTH_PIXELS = 1200;
	public static final int LOGICAL_HEIGHT_PIXELS = (int)(LOGICAL_WIDTH_PIXELS * .68);
	
	public static final int MAP_WIDTH = 31;//21;
	public static final int MAP_HEIGHT = 60;
	public static final float PLAYER_SPEED = LOGICAL_WIDTH_PIXELS/8;//120;//50;
	public static final float MAX_MOVEMENT = 20;//50;//150; // After adjusting for FPS

	public static final float PLAYER_SIZE = LOGICAL_HEIGHT_PIXELS / 17;
	public static final float MAP_SQ_SIZE = LOGICAL_HEIGHT_PIXELS / 15;
	public static final float COLLECTABLE_SIZE = LOGICAL_HEIGHT_PIXELS / 20;
	public static final int AVATAR_RESPAWN_TIME_SECS = 3;
	public static final int MAX_PLAYERS = 4;
	public static final int START_LIVES = 999;
	public static final int START_SPEED = 17;
	public static final int WINNING_COINS = 100;
	
	public static final String TITLE = "SPLAT!";
	
	private Settings() { }

}
