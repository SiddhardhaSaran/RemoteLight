package de.lars.remotelightclient.animation.animations;

import java.awt.Color;

import de.lars.remotelightclient.Main;
import de.lars.remotelightclient.animation.Animation;
import de.lars.remotelightclient.out.OutputManager;
import de.lars.remotelightclient.settings.SettingsManager.SettingCategory;
import de.lars.remotelightclient.settings.types.SettingBoolean;
import de.lars.remotelightclient.settings.types.SettingDouble;
import de.lars.remotelightclient.utils.color.ColorUtil;
import de.lars.remotelightclient.utils.color.PixelColorUtils;
import de.lars.remotelightclient.utils.color.RainbowWheel;
import de.lars.remotelightclient.utils.maths.MathHelper;
import de.lars.remotelightclient.utils.maths.OpenSimplexNoise;

public class RainbowNoise extends Animation {
	
	private OpenSimplexNoise noiseBright, noiseColor;
	private Color[] strip;
	
	private float zoff = 0.0f;
	private float zincrement = 0.05f;

	public RainbowNoise() {
		super("RainbowNoise");
		// register settings
		this.addSetting(new SettingBoolean("animation.rainbownoise.brgightnesschange", "Brightness change", SettingCategory.Intern, "Vary the brightness", false));
		this.addSetting(new SettingDouble("animation.rainbownoise.xincrement", "x-Increment", SettingCategory.Intern, null, 0.02, 0, 5, 0.005));
		this.addSetting(new SettingDouble("animation.rainbownoise.yincrement", "y-Increment", SettingCategory.Intern, null, 0.005, 0, 5, 0.005));
		this.addSetting(new SettingDouble("animation.rainbownoise.timeincrement", "Time-Increment", SettingCategory.Intern, null, 0.02, 0, 5, 0.002));
	}
	
	@Override
	public void onEnable() {
		// initialize strip color array
		strip = PixelColorUtils.colorAllPixels(Color.BLACK, Main.getLedNum());
		// initialize noise
		noiseBright = new OpenSimplexNoise();
		noiseColor = new OpenSimplexNoise();
		super.onEnable();
	}
	
	@Override
	public void onLoop() {
		// get increment values from settings
		float xincrement = (float) ((SettingDouble) getSetting("animation.rainbownoise.xincrement")).getValue();
		float yincrement = (float) ((SettingDouble) getSetting("animation.rainbownoise.yincrement")).getValue();
		zincrement = (float) ((SettingDouble) getSetting("animation.rainbownoise.timeincrement")).getValue();
		
		zoff += zincrement;
		float xoff = 0.0f;
		float yoff = 0.0f;
		
		for(int x = 0; x < strip.length; x++) {
			// increase xoff and yoff
			xoff += xincrement;
			yoff += yincrement;
			
			// get brightness noise value
			float nB = (float) noiseBright.eval(xoff, yoff, zoff);
			int bright = (int) MathHelper.map(nB, -1, 1, 0, 100);
			
			// get hue noise value
			float nC = (float) noiseColor.eval(xoff, yoff, zoff);
			int hue = (int) MathHelper.map(nC, -1, 1, 0, RainbowWheel.getRainbow().length-1);
			
			Color color = RainbowWheel.getRainbow()[hue];
			
			// change brightness of color (if enabled in options)
			if(((SettingBoolean)getSetting("animation.rainbownoise.brgightnesschange")).getValue()) {
				color = ColorUtil.dimColor(color, bright);
			}
			
			// set color on position x to new color
			strip[x] = color;
		}
		
		// show the strip
		OutputManager.addToOutput(strip);
		
		super.onLoop();
	}

}
