package de.lars.remotelightclient.musicsync.modes;

import java.awt.Color;
import java.util.HashMap;

import de.lars.remotelightclient.Main;
import de.lars.remotelightclient.musicsync.MusicEffect;
import de.lars.remotelightclient.musicsync.sound.SoundProcessing;
import de.lars.remotelightclient.network.Client;
import de.lars.remotelightclient.settings.SettingsManager;
import de.lars.remotelightclient.settings.SettingsManager.SettingCategory;
import de.lars.remotelightclient.settings.types.SettingBoolean;
import de.lars.remotelightclient.settings.types.SettingInt;
import de.lars.remotelightclient.utils.RainbowWheel;

public class Rainbow extends MusicEffect {
	
	private SettingsManager s = Main.getInstance().getSettingsManager();
	private int pix = Main.getLedNum();
	private int half = pix / 2;
	private int lastLeds = 0;
	private Color[] strip;
	private int step = 0;
	
	public boolean smoothRise = true;
	public boolean smoothFall = true;
	public int steps = 5;

	public Rainbow() {
		super("Rainbow");
		
		s.addSetting(new SettingBoolean("musicsync.rainbow.smoothrise", "SmoothRise", SettingCategory.MusicEffect, "", true));
		s.addSetting(new SettingBoolean("musicsync.rainbow.smoothfall", "SmoothFall", SettingCategory.MusicEffect, "", true));
		s.addSetting(new SettingInt("musicsync.rainbow.steps", "Steps", SettingCategory.MusicEffect, "", 5, 1, 20, 1));
	}
	
	private void initOptions() {
		smoothRise = ((SettingBoolean) s.getSettingFromId("musicsync.rainbow.smoothrise")).getValue();
		smoothFall= ((SettingBoolean) s.getSettingFromId("musicsync.rainbow.smoothfall")).getValue();
		steps = ((SettingInt) s.getSettingFromId("musicsync.rainbow.steps")).getValue();
	}
	
	@Override
	public void onEnable() {
		this.initOptions();
		strip = new Color[pix];
		
		for(int i = 0; i < half; i++) {
			step += 5;
			if(step >= RainbowWheel.getRainbow().length)
				step = 0;
			
			Color c = RainbowWheel.getRainbow()[step];
			//half1
			strip[(half - 1 - i)] = c;
			//half2
			strip[(half + i)] = c;
		}
		super.onEnable();
	}
	
	@Override
	public void onLoop() {
		this.initOptions();
		boolean bump = this.isBump();
		SoundProcessing soundProcessor = this.getSoundProcessor();
		HashMap<Integer, Color> pixelHash = new HashMap<>();
		
		double mul = 0.1 * this.getSensitivity(); // multiplier for amount of pixels
		int[] amp = soundProcessor.getAmplitudes(); //6 bands
		
		int x = 0;
		for(int i = 0; i < amp.length; i++) {
			x += amp[i];
		}
		int ampAv = x / amp.length; //average of all amp bands
		
		int leds = (int) (ampAv * mul); //how many leds should glow
		if(leds > half) leds = half;
		
		//Smooth
		if(smoothRise && smoothFall) {
			if(lastLeds > leds) {
				leds = lastLeds;
				lastLeds--;
			} else {
				lastLeds += 2;
				if(lastLeds > leds) lastLeds = leds;
				leds = lastLeds;
			}
			
		} else if(smoothRise) {
			if(lastLeds > leds) {
				lastLeds = leds;
			} else {
				lastLeds += 2;
				if(lastLeds > leds) lastLeds = leds;
				leds = lastLeds;
			}
			
		} else if(smoothFall) {
			if(lastLeds > leds) {
				leds = lastLeds;
				lastLeds--;
			} else {
				lastLeds = leds;
			}
			
		}
		
		
		//Rainbow
		for(int i = 0; i < pix; i++) {
			pixelHash.put(i, strip[i]);
		}
		
		if(bump)
			step += steps + (steps / 2) + 20;
		else
			step += steps;

		if(step >= RainbowWheel.getRainbow().length)
			step = 0;
		
		//move
		for(int i = 0; i < half; i++) {
			Color c = RainbowWheel.getRainbow()[step];
			
			if(i == (half - 1)) {
				strip[i] = c;	//half1
				strip[half] = c;//half2
				
			} else {
				strip[i] = strip[i + 1];
				strip[pix - 1 - i] = strip[pix - 2 - i];
			}
		}
		
		//effect
		for(int i = 0; i < (half - leds); i++) {
			pixelHash.put(i, Color.BLACK);
			pixelHash.put(pix - 1 - i, Color.BLACK);
		}
		
		Client.sendWS281xList(pixelHash);
		
		super.onLoop();
	}

}
