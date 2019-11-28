package de.lars.remotelightclient.ui.panels.scripts;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tinylog.Logger;

import de.lars.remotelightclient.Main;
import de.lars.remotelightclient.lua.LuaManager;
import de.lars.remotelightclient.settings.SettingsManager;
import de.lars.remotelightclient.settings.types.SettingObject;
import de.lars.remotelightclient.ui.MainFrame;
import de.lars.remotelightclient.ui.MenuPanel;
import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.ui.comps.BigTextButton;
import de.lars.remotelightclient.ui.panels.controlbars.DefaultControlBar;
import de.lars.remotelightclient.ui.panels.controlbars.comps.SpeedSlider;
import de.lars.remotelightclient.utils.DirectoryUtil;
import de.lars.remotelightclient.utils.UiUtils;
import de.lars.remotelightclient.utils.WrapLayout;

public class ScriptsPanel extends MenuPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1750844483582377827L;
	private MainFrame mainFrame;
	private SettingsManager sm;
	private LuaManager luaManager;
	private JPanel bgrScripts, bgrOptions;
	
	public ScriptsPanel() {
		sm = Main.getInstance().getSettingsManager();
		luaManager = Main.getInstance().getLuaManager();
		mainFrame = Main.getInstance().getMainFrame();
		
		sm.addSetting(new SettingObject("scripts.speed", null, 50));
		addControlBar();
		setBackground(Style.panelBackground);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel bgrScroll = new JPanel();
		add(bgrScroll);
		bgrScroll.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setViewportBorder(null);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		bgrScroll.add(scrollPane);
		
		bgrScripts = new JPanel();
		WrapLayout wlayout = new WrapLayout(FlowLayout.LEFT);
		bgrScripts.setLayout(wlayout);
		bgrScripts.setBackground(Style.panelBackground);
		scrollPane.setViewportView(bgrScripts);
		
		bgrOptions = new JPanel();
		bgrOptions.setBackground(Style.panelDarkBackground);
		WrapLayout wlayout2 = new WrapLayout(FlowLayout.LEFT);
		bgrOptions.setLayout(wlayout2);
		add(bgrOptions);
		
		addOptionButtons();
		addScriptPanels();
	}
	
	private void addOptionButtons() {
		bgrOptions.removeAll();
		
		JButton btnScriptDir = new JButton("Open scripts folder");
		UiUtils.configureButton(btnScriptDir);
		btnScriptDir.setName("scriptdir");
		btnScriptDir.addActionListener(optionButtonsListener);
		bgrOptions.add(btnScriptDir);
		
		JButton btnScan = new JButton("Scan");
		UiUtils.configureButton(btnScan);
		btnScan.setName("scan");
		btnScan.addActionListener(optionButtonsListener);
		bgrOptions.add(btnScan);
		
		bgrOptions.updateUI();
	}
	
	private ActionListener optionButtonsListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = ((JButton) e.getSource()).getName();
			
			/*
			 * Open lua scripts directory
			 */
			if(name.equals("scriptdir")) {
				try {
					Desktop.getDesktop().open(new File(DirectoryUtil.getLuaPath()));
				} catch (IOException ex) {
					Logger.error(ex, "Could not open scripts directory!");
				}
			}
			/*
			 * Scan for lua files
			 */
			if(name.equals("scan")) {
				luaManager.scanLuaScripts(DirectoryUtil.getLuaPath());
				addScriptPanels();
			}
		}
	};
	
	
	public void addScriptPanels() {
		bgrScripts.removeAll();
		for(File script : luaManager.getLuaScripts(DirectoryUtil.getLuaPath())) {
			
			BigTextButton btn = new BigTextButton(DirectoryUtil.getFileName(script), "");
			btn.setName(script.getAbsolutePath());
			btn.addMouseListener(scriptPanelsListener);
			
			if(luaManager.getActiveLuaScriptPath() != null && luaManager.getActiveLuaScriptPath().equals(script.getAbsolutePath())) {
				btn.setBorder(BorderFactory.createLineBorder(Style.accent));
			}
			bgrScripts.add(btn);
		}
		updateUI();
	}
	
	private MouseAdapter scriptPanelsListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			BigTextButton btn = (BigTextButton) e.getSource();
			if(luaManager.getActiveLuaScriptPath() != null && luaManager.getActiveLuaScriptPath().equals(btn.getName())) {
				luaManager.stopLuaScript();
			} else {
				for(File script : luaManager.getLuaScripts(DirectoryUtil.getLuaPath())) {
					if(script.getAbsolutePath().equals(btn.getName())) {
						luaManager.runLuaScript(script.getAbsolutePath());
						break;
					}
				}
			}
			addScriptPanels();
		}
	};
	
	
	private void addControlBar() {
		DefaultControlBar controlBar = new DefaultControlBar();
		SpeedSlider speedSlider = new SpeedSlider(Style.panelDarkBackground);
		speedSlider.getSlider().addChangeListener(sliderSpeedListener);
		speedSlider.getSlider().setValue((int) sm.getSettingObject("scripts.speed").getValue());
		luaManager.setDelay((int) sm.getSettingObject("scripts.speed").getValue());
		controlBar.setActionPanel(speedSlider);
		mainFrame.setControlBarPanel(controlBar);
	}
	
	private ChangeListener sliderSpeedListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			int speed = ((JSlider) e.getSource()).getValue();
			sm.getSettingObject("scripts.speed").setValue(speed);
			luaManager.setDelay(speed);
		}
	};
	
}
