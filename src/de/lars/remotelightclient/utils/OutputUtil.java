package de.lars.remotelightclient.utils;

import de.lars.remotelightclient.devices.ConnectionState;
import de.lars.remotelightclient.devices.Device;
import de.lars.remotelightclient.devices.arduino.Arduino;
import de.lars.remotelightclient.devices.remotelightserver.RemoteLightServer;
import de.lars.remotelightclient.out.Output;

public class OutputUtil {
	
	public static String getOutputTypeAsString(Output o) {
		if(o instanceof Arduino) {
			return "Arduino";
		}
		if(o instanceof RemoteLightServer) {
			return "RemoteLightServer";
		}
		return "Unknown output";
	}
	
	public static String getDeviceConnectionInfo(Device d) {
		if(d instanceof Arduino) {
			return ((Arduino) d).getSerialPort();
		}
		if(d instanceof RemoteLightServer) {
			return ((RemoteLightServer)d).getIp();
		}
		return "No connection info";
	}
	
	public static String getConnectionStateAsString(ConnectionState s) {
		if(s == ConnectionState.CONNECTED) {
			return "Connected";
		}
		if(s == ConnectionState.DISCONNECTED) {
			return "Disconnected";
		}
		if(s == ConnectionState.FAILED) {
			return "Connection failed";
		}
		return "";
	}

}
