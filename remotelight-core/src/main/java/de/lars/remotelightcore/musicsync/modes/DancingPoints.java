/*******************************************************************************
 * ______                     _       _     _       _     _   
 * | ___ \                   | |     | |   (_)     | |   | |  
 * | |_/ /___ _ __ ___   ___ | |_ ___| |    _  __ _| |__ | |_ 
 * |    // _ \ '_ ` _ \ / _ \| __/ _ \ |   | |/ _` | '_ \| __|
 * | |\ \  __/ | | | | | (_) | ||  __/ |___| | (_| | | | | |_ 
 * \_| \_\___|_| |_| |_|\___/ \__\___\_____/_|\__, |_| |_|\__|
 *                                             __/ |          
 *                                            |___/           
 * 
 * Copyright (C) 2019 Lars O.
 * 
 * This file is part of RemoteLight.
 ******************************************************************************/
package de.lars.remotelightcore.musicsync.modes;

import java.awt.Color;
import java.util.Random;

import de.lars.remotelightcore.RemoteLightCore;
import de.lars.remotelightcore.musicsync.MusicEffect;
import de.lars.remotelightcore.out.OutputManager;
import de.lars.remotelightcore.settings.SettingsManager;
import de.lars.remotelightcore.settings.SettingsManager.SettingCategory;
import de.lars.remotelightcore.settings.types.SettingBoolean;
import de.lars.remotelightcore.settings.types.SettingColor;
import de.lars.remotelightcore.utils.color.PixelColorUtils;
import de.lars.remotelightcore.utils.color.RainbowWheel;

public class DancingPoints extends MusicEffect {
	
	private SettingsManager s = RemoteLightCore.getInstance().getSettingsManager();
	private int numLEDs;
	private int numPoints;	// Number of points displayed
	private int[] pos;		// Position of points
	private int[] targetPos;// Target position, where the point wants to go
	private Color[] color;	// Colors of points
	private Color[] strip;	// LED strip
	private int maxMove = 10;	// Max distance a point can move
	private int minMove = 4;	// Min distance a point must move
	private long lastBump; //for detecting silence
	private final int SILENCE_TIME = 5; // Time in seconds for idle activity

	public DancingPoints() {
		super("DancingPoints");
		
		s.addSetting(new SettingBoolean("musicsync.dancingpoints.randomcolor", "Random color", SettingCategory.MusicEffect, "", true));
		this.addOption("musicsync.dancingpoints.randomcolor");
		s.addSetting(new SettingColor("musicsync.dancingpoints.color", "Color", SettingCategory.MusicEffect, "", Color.RED));
		this.addOption("musicsync.dancingpoints.color");
		s.addSetting(new SettingBoolean("musicsync.dancingpoints.idleactivity", "Idle activity", SettingCategory.MusicEffect, "Move points randomly when no music is playing.", false));
		this.addOption("musicsync.dancingpoints.idleactivity");
	}
	
	@Override
	public void onEnable() {
		this.numLEDs = RemoteLightCore.getLedNum();
		this.numPoints = numLEDs / 6;
		this.pos = new int[numPoints];
		this.targetPos = new int[numPoints];
		this.color = new Color[numPoints];
		this.strip = PixelColorUtils.colorAllPixels(Color.BLACK, numLEDs);
		this.lastBump = System.currentTimeMillis();
		
		for(int i = 0; i < numPoints; i++) {
			pos[i] = numLEDs / numPoints * i;
			targetPos[i] = pos[i];
			setColor(i);
			strip[pos[i]] = color[i];	// Place point on strip
		}
		super.onEnable();
	}
	
	@Override
	public void onLoop() {
		boolean bump = this.isBump();
		boolean idleActivity = ((SettingBoolean) s.getSettingFromId("musicsync.dancingpoints.idleactivity")).getValue();
		
		if(bump) lastBump = System.currentTimeMillis();
		
		if(idleActivity && (System.currentTimeMillis() - lastBump) >= SILENCE_TIME * 1000) {
			bump = new Random().nextInt(25) == 2;
		}
		
		for(int i = 0; i < numPoints; i++) {
			if(bump && targetPos[i] == pos[i]) {	// Set target position for point if point is not moving
				setNewTargetPos(i);
			}
			moveToTargetPos(i);
			setColor(i);
			
			strip[pos[i]] = color[i];
		}
		
		OutputManager.addToOutput(strip);
		
		for(int i = 0; i < numPoints; i++) {
			strip[pos[i]] = Color.BLACK;
		}
		super.onLoop();
	}
	
	
	private void setNewTargetPos(int i) {
		int tPos = new Random().nextInt(maxMove - minMove) + minMove;	// Distance
		
		boolean right = new Random().nextInt(2) == 0; // Direction
		if(!right) tPos *= -1;
		
		int newPos = pos[i] + tPos;
		if(newPos < 0) newPos = 0;
		if(newPos >= numLEDs) newPos = numLEDs - 1;
		
		targetPos[i] = newPos;
	}
	
	
	private void moveToTargetPos(int i) {
		if(targetPos[i] > pos[i]) {	// Target pos is above current pos
			pos[i]++;
		}
		if(targetPos[i] < pos[i]) {	// Target pos is under current pos
			pos[i]--;
		}
	}
	
	
	private void setColor(int i) {
		if(((SettingBoolean) s.getSettingFromId("musicsync.dancingpoints.randomcolor")).getValue()) {	// Random color
			int rainbowPos = RainbowWheel.getRainbow().length / numPoints * i;
			color[i] = RainbowWheel.getRainbow()[rainbowPos];
		} else {
			color[i] = ((SettingColor) s.getSettingFromId("musicsync.dancingpoints.color")).getValue();
		}
	}

}