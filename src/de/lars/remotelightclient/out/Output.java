package de.lars.remotelightclient.out;

import java.awt.Color;
import java.io.Serializable;

import de.lars.remotelightclient.devices.ConnectionState;

public abstract class Output implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4585718970709898453L;
	private String id;
	private int pixels;
	
	public Output(String id, int pixels) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public int getPixels() {
		return pixels;
	}

	public void setPixels(int pixels) {
		this.pixels = pixels;
	}
	
	public void onActivate() {
	}
	
	public void onDeactivate() {
	}
	
	public abstract ConnectionState getState();

	public void onOutput(Color[] pixels) {
	}

}
