package de.lars.remotelightclient.ui.panels.controlbars;

import javax.swing.JPanel;

import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.ui.panels.controlbars.comps.BrightnessSlider;
import de.lars.remotelightclient.ui.panels.controlbars.comps.OutputInfo;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.border.EmptyBorder;

public class DefaultControlBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6631254065528102825L;
	private Dimension size = new Dimension(100, 45);

	/**
	 * Create the panel.
	 */
	public DefaultControlBar() {
		setBorder(new EmptyBorder(0, 5, 0, 5));
		setBackground(Style.panelDarkBackground);
		setLayout(new GridLayout(0, 3, 2, 0));
		setPreferredSize(size);
		setMaximumSize(size);
		
		JPanel bgrBrightness = new JPanel();
		bgrBrightness.setBackground(Style.panelDarkBackground);
		add(bgrBrightness);
		bgrBrightness.setLayout(new BorderLayout(0, 0));
		bgrBrightness.add(new BrightnessSlider(Style.panelDarkBackground));
		
		JPanel bgrOutput = new JPanel();
		bgrOutput.setBackground(Style.panelDarkBackground);
		add(bgrOutput);
		bgrOutput.setLayout(new BorderLayout(0, 0));
		bgrOutput.add(new OutputInfo(Style.panelDarkBackground));
		
		JPanel bgrAction = new JPanel();
		bgrAction.setBackground(Style.panelDarkBackground);
		add(bgrAction);
		bgrAction.setLayout(new BorderLayout(0, 0));
	}

}
