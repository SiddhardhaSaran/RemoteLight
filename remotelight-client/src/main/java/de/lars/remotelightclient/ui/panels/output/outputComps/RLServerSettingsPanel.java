/*-
 * >===license-start
 * RemoteLight
 * ===
 * Copyright (C) 2019 - 2020 Lars O.
 * ===
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * <===license-end
 */

package de.lars.remotelightclient.ui.panels.output.outputComps;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.utils.ui.UiUtils;
import de.lars.remotelightcore.devices.arduino.RgbOrder;
import de.lars.remotelightcore.devices.remotelightserver.RemoteLightServer;
import de.lars.remotelightcore.lang.i18n;
import de.lars.remotelightcore.out.OutputManager;

public class RLServerSettingsPanel extends DeviceSettingsPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3968905553069494626L;
	private RemoteLightServer rlServer;
	private JTextField fieldId;
	private JSpinner spinnerPixels;
	private JComboBox<RgbOrder> comboOrder;
	private Dimension size;
	private JTextField fieldHostname;
	private JSpinner spinnerShift;
	private JSpinner spinnerClone;
	private JCheckBox checkboxCloneMirrored;

	/**
	 * Create the panel.
	 */
	public RLServerSettingsPanel(RemoteLightServer rlServer, boolean setup) {
		super(rlServer, setup);
		this.rlServer = rlServer;
		size = new Dimension(800, 40);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JPanel panelId = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelId.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panelId.setPreferredSize(size);
		panelId.setMaximumSize(size);
		panelId.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panelId);
		
		JLabel lblNameId = new JLabel(i18n.getString("OutputPanel.NameID")); //$NON-NLS-1$
		panelId.add(lblNameId);
		
		fieldId = new JTextField();
		panelId.add(fieldId);
		fieldId.setColumns(10);
		
		JPanel panelPort = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panelPort.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panelPort.setPreferredSize(size);
		panelPort.setMaximumSize(size);
		panelPort.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panelPort);
		
		JLabel lblHostname = new JLabel(i18n.getString("OutputPanel.HostnameIP")); //$NON-NLS-1$
		panelPort.add(lblHostname);
		
		fieldHostname = new JTextField();
		panelPort.add(fieldHostname);
		fieldHostname.setColumns(10);
		
		JPanel panelPixels = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panelPixels.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		panelPixels.setPreferredSize(size);
		panelPixels.setMaximumSize(size);
		panelPixels.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panelPixels);
		
		JLabel lblPixels = new JLabel(i18n.getString("OutputPanel.Pixels")); //$NON-NLS-1$
		panelPixels.add(lblPixels);
		
		spinnerPixels = new JSpinner();
		spinnerPixels.setModel(new SpinnerNumberModel(new Integer(OutputManager.MIN_PIXELS), new Integer(OutputManager.MIN_PIXELS), null, new Integer(1)));
		UiUtils.configureSpinner(spinnerPixels);
		panelPixels.add(spinnerPixels);
		
		JPanel panelOrder = new JPanel();
		FlowLayout flowLayout_Order = (FlowLayout) panelOrder.getLayout();
		flowLayout_Order.setAlignment(FlowLayout.LEFT);
		panelOrder.setPreferredSize(size);
		panelOrder.setMaximumSize(size);
		panelOrder.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panelOrder);
		
		JLabel lblRgbOrder = new JLabel(i18n.getString("OutputPanel.RgbOrder"));
		panelOrder.add(lblRgbOrder);
		
		comboOrder = new JComboBox<RgbOrder>();
		comboOrder.setModel(new DefaultComboBoxModel<>(RgbOrder.values()));
		panelOrder.add(comboOrder);
		
		JLabel lblOutputPatch = new JLabel(i18n.getString("OutputPanel.OutputPatch"), SwingConstants.LEFT);
		lblOutputPatch.setFont(Style.getFontBold(11));
		lblOutputPatch.setBorder(new EmptyBorder(5, 5, 0, 0));
		add(lblOutputPatch);
		
		JPanel panelShift = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panelShift.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		panelShift.setPreferredSize(new Dimension(800, 40));
		panelShift.setMaximumSize(new Dimension(800, 40));
		panelShift.setAlignmentX(0.0f);
		add(panelShift);
		
		JLabel lblShift = new JLabel(i18n.getString("OutputPanel.ShiftPixels"));
		panelShift.add(lblShift);
		
		spinnerShift = new JSpinner();
		spinnerShift.setModel(new SpinnerNumberModel(rlServer.getOutputPatch().getShift(), -rlServer.getPixels(), rlServer.getPixels(), 1));
		UiUtils.configureSpinner(spinnerShift);
		spinnerShift.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int max = (int) spinnerPixels.getValue() - 1;
				spinnerShift.setModel(new SpinnerNumberModel((int) spinnerShift.getValue(), -max, max, 1));
			}
		});
		panelShift.add(spinnerShift);
		
		JLabel lblClone = new JLabel(i18n.getString("OutputPanel.Clone"));
		panelShift.add(lblClone);
		
		spinnerClone = new JSpinner();
		spinnerClone.setModel(new SpinnerNumberModel(rlServer.getOutputPatch().getClone(), 0, rlServer.getPixels() / 2, 1));
		UiUtils.configureSpinner(spinnerClone);
		spinnerClone.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				spinnerClone.setModel(new SpinnerNumberModel((Number) spinnerClone.getValue(), 0, rlServer.getPixels() / 2, 1));
			}
		});
		panelShift.add(spinnerClone);
		
		checkboxCloneMirrored = new JCheckBox(i18n.getString("OutputPanel.Mirror"));
		checkboxCloneMirrored.setSelected(rlServer.getOutputPatch().isCloneMirrored());
		panelShift.add(checkboxCloneMirrored);
		
		setValues();
	}
	
	private void setValues() {
		if(rlServer.getId() != null) {
			fieldId.setText(rlServer.getId());
		}
		if(rlServer.getIp() != null) {
			fieldHostname.setText(rlServer.getIp());
		}
		spinnerPixels.setValue(rlServer.getPixels());
		
		if(rlServer.getRgbOrder() == null) {
			rlServer.setRgbOrder(RgbOrder.RGB);
		}
		comboOrder.setSelectedItem(rlServer.getRgbOrder());
	}

	@Override
	public boolean save() {
		if(fieldId.getText().isEmpty()) {
			return false;
		}
		rlServer.setId(fieldId.getText());
		rlServer.setIp(fieldHostname.getText());
		rlServer.setPixels((int) spinnerPixels.getValue());
		rlServer.setRgbOrder((RgbOrder) comboOrder.getSelectedItem());
		rlServer.getOutputPatch().setShift((int) spinnerShift.getValue());
		rlServer.getOutputPatch().setClone((int) spinnerClone.getValue());
		rlServer.getOutputPatch().setCloneMirrored(checkboxCloneMirrored.isSelected());
		return true;
	}

	@Override
	public String getId() {
		return fieldId.getText();
	}

}
