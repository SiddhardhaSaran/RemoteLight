package de.lars.remotelightclient.ui.panels.musicsync;

import de.lars.remotelightclient.Main;
import de.lars.remotelightclient.musicsync.MusicEffect;
import de.lars.remotelightclient.musicsync.MusicSyncManager;
import de.lars.remotelightclient.settings.Setting;
import de.lars.remotelightclient.settings.SettingsManager;
import de.lars.remotelightclient.ui.MainFrame;
import de.lars.remotelightclient.ui.MenuPanel;
import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.ui.comps.BigTextButton;
import de.lars.remotelightclient.utils.WrapLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MusicSyncPanel extends MenuPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5524656198416878733L;
	private MainFrame mainFrame;
	private MusicSyncManager msm;
	private SettingsManager sm = Main.getInstance().getSettingsManager();
	private MusicSyncOptionsPanel muiscEffectOptions;
	private JPanel bgrMusicEffects;
	
	public MusicSyncPanel() {
		mainFrame = Main.getInstance().getMainFrame();
		msm = Main.getInstance().getMusicSyncManager();
		
		setBackground(Style.panelBackground);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel bgrScroll = new JPanel();
		add(bgrScroll);
		bgrScroll.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(null);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		bgrScroll.add(scrollPane);
		
		bgrMusicEffects = new JPanel();
		WrapLayout wlayout = new WrapLayout(FlowLayout.LEFT);
		bgrMusicEffects.setLayout(wlayout);
		bgrMusicEffects.setBackground(Style.panelBackground);
		scrollPane.setViewportView(bgrMusicEffects);
		
		JPanel bgrOptions = new JPanel();
		bgrOptions.setBackground(Style.panelDarkBackground);
		bgrOptions.setLayout(new BoxLayout(bgrOptions, BoxLayout.X_AXIS));
		muiscEffectOptions = new MusicSyncOptionsPanel();
		bgrOptions.add(muiscEffectOptions);
		add(bgrOptions);
		
		addMusicEffectPanels();
	}
	
	public void addMusicEffectPanels() {
		bgrMusicEffects.removeAll();
		for(MusicEffect m : msm.getMusicEffects()) {
			BigTextButton btn = new BigTextButton(m.getDisplayname(), "");
			btn.setName(m.getName());
			btn.addMouseListener(btnMusicEffectListener);
			
			if(msm.getActiveEffect() != null && msm.getActiveEffect().getName().equals(m.getName())) {
				btn.setBorder(BorderFactory.createLineBorder(Style.accent));
			}
			bgrMusicEffects.add(btn);
		}
		updateUI();
	}
	
	private MouseAdapter btnMusicEffectListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			BigTextButton btn = (BigTextButton) e.getSource();
			
			if(msm.getActiveEffect() != null && msm.getActiveEffect().getName().equals(btn.getName())) {
				msm.stop();
				muiscEffectOptions.removeMusicEffectOptions();
			} else {
				for(MusicEffect m : msm.getMusicEffects()) {
					if(m.getName().equals(btn.getName())) {
						msm.start(m);
						showMusicEffectOptions();
						break;
					}
				}
			}
			addMusicEffectPanels();
		}
	};
	
	private void showMusicEffectOptions() {
		if(msm.getCurrentMusicEffectOptions() != null && msm.getCurrentMusicEffectOptions().size() > 0) {
			List<Setting> options = msm.getCurrentMusicEffectOptions();
			muiscEffectOptions.addMusicEffectOptions(options);
		}
	}

}
