package de.lars.remotelightclient.out;

import java.awt.Color;

import org.tinylog.Logger;

import de.lars.remotelightclient.Main;
import de.lars.remotelightclient.devices.ConnectionState;
import de.lars.remotelightclient.utils.PixelColorUtils;

public class OutputManager {
	
	private volatile Output activeOutput;
	private volatile static Color[] outputPixels;
	private int delay = 100;
	private int brightness = 100;
	private boolean active;
	
	public OutputManager() {
		active = false;
	}

	public Output getActiveOutput() {
		return activeOutput;
	}

	public void setActiveOutput(Output activeOutput) {
		this.setEnabled(false);
		if(this.activeOutput != null && !(this.activeOutput.getId().equals(activeOutput.getId()))) {
			
			deactivate(this.activeOutput);
		}

		activate(activeOutput);
		this.activeOutput = activeOutput;
		this.loop();
		
	}
	
	/**
	 * Connects the device if not connected
	 */
	public void activate(Output output) {
		Logger.info("Activate output: " + output.getId());
		if(output.getState() != ConnectionState.CONNECTED) {
			output.onActivate();
		}
	}
	
	/**
	 * Disconnects the device if connected
	 */
	public void deactivate(Output output) {
		Logger.info("Deactivate output: " + output.getId());
		if(output.getState() == ConnectionState.CONNECTED) {
			output.onDeactivate();
		}
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	public int getDelay() {
		return delay;
	}
	
	/**
	 * 
	 * @param brightness Value between 0 and 100
	 */
	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}
	
	public int getBrightness() {
		return brightness;
	}
	
	public void setEnabled(boolean enabled) {
		active = enabled;
	}
	
	public boolean isEnabled() {
		return active;
	}
	
	public void close() {
		setEnabled(false);
		if(activeOutput != null) {
			activeOutput.onOutput(PixelColorUtils.colorAllPixels(Color.BLACK, Main.getLedNum()));
			activeOutput.onOutput(PixelColorUtils.colorAllPixels(Color.BLACK, Main.getLedNum()));
			deactivate(activeOutput);
		}
		//save last output before closing
		Main.getInstance().getSettingsManager().getSettingObject("out.lastoutput").setValue(activeOutput);
		//save brightness
		Main.getInstance().getSettingsManager().getSettingObject("out.brightness").setValue(getBrightness());
	}
	
	public static void addToOutput(Color[] pixels) {
		PixelColorUtils.changeBrightness(pixels, Main.getInstance().getOutputManager().getBrightness());
		outputPixels = pixels;
	}
	
	private void loop() {
		if(!active) {
			active = true;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Logger.info("Started output loop.");
					
					while(active) {
						if((outputPixels != null) && (activeOutput != null)) {
							activeOutput.onOutput(outputPixels);
							
							try {
								Thread.sleep(delay);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					Logger.info("Stopped output loop.");
				}
			}).start();
		}
	}

}
