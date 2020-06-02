/*-
 * >===license-start
 * RemoteLight
 * ===
 * Copyright (C) 2019 - 2020 Drumber
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

package de.lars.remotelightclient.ui.comps.dialogs;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;

import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightcore.utils.ExceptionHandler;

public class ErrorDialog {
	
	public static void showErrorDialog(Throwable e) {
		showErrorDialog(e, null);
	}
	
	public static void showErrorDialog(Throwable e, String title) {
		showErrorDialog(e, title, true);
	}
	
	public static void showErrorDialog(Throwable e, String title, boolean lineWrap) {
		JPanel root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		
		JLabel header = new JLabel(String.format("A %s Error occured", e.getClass().getCanonicalName()));
		header.setHorizontalAlignment(JLabel.LEFT);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.setFont(Style.getFontRegualar(12));
		root.add(header);
		
		root.add(Box.createRigidArea(new Dimension(0, 20)));
		
		JTextArea text = new JTextArea(ExceptionHandler.getStackTrace(e));
		text.setLineWrap(lineWrap);
		text.setCaretPosition(0);
		text.setEditable(false);
		
		JScrollPane scroll = new JScrollPane(text);
		scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
		scroll.setSize(new Dimension(200, 150));
		scroll.setPreferredSize(new Dimension(200, 150));
		root.add(scroll);
		
		JOptionPane.showMessageDialog(null, root, (title != null ? title : "Exception"), JOptionPane.ERROR_MESSAGE);
	}

}
