package de.lars.remotelightclient.ui.panels.colors.gradients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import de.lars.colorpicker.ColorPicker;
import de.lars.colorpicker.listener.ColorListener;
import de.lars.remotelightclient.ui.Style;
import de.lars.remotelightclient.ui.components.TScrollPane;
import de.lars.remotelightclient.ui.components.TabButtons;
import de.lars.remotelightclient.utils.ColorTool;
import de.lars.remotelightclient.utils.ui.CustomStyledDocument;
import de.lars.remotelightclient.utils.ui.TextLineNumber;
import de.lars.remotelightclient.utils.ui.UiUtils;
import de.lars.remotelightclient.utils.ui.WrapLayout;
import de.lars.remotelightcore.utils.ExceptionHandler;
import de.lars.remotelightcore.utils.color.palette.AbstractPalette;
import de.lars.remotelightcore.utils.color.palette.PaletteData;
import de.lars.remotelightcore.utils.color.palette.PaletteParser;
import de.lars.remotelightcore.utils.color.palette.PaletteParser.PaletteParseException;

public class GradientEditPanel extends JPanel {
	private static final long serialVersionUID = 7638664136509067917L;
	
	private GradientBar gradientBar;
	private PaletteData palette;
	private JTextField fieldName;
	private MarkerEditPanel panelMarkerEdit;
	private ColorPicker colorPicker;
	private JTextPane editor;
	private boolean editorTextReplaceMode; // cancel events when replacing the text programmatically
	
	public GradientEditPanel() {
		setBackground(Style.panelBackground);
		setLayout(new BorderLayout());
		
		JPanel panelHeader = new JPanel();
		panelHeader.setBackground(Style.panelBackground);
		panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
		add(panelHeader, BorderLayout.NORTH);
		
		fieldName = new JTextField();
		fieldName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		fieldName.setPreferredSize(new Dimension(0, 40));
		fieldName.setBackground(Style.panelDarkBackground);
		fieldName.setForeground(Style.textColor);
		fieldName.setCaretColor(Style.accent);
		fieldName.setFont(Style.getFontRegualar(16));
		fieldName.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, Style.textColorDarker), 
				BorderFactory.createEmptyBorder(5, 10, 0, 10)));
		panelHeader.add(fieldName);
		
		gradientBar = new GradientBar(null);
		gradientBar.setMinimumSize(new Dimension(100, 20));
		gradientBar.setPreferredSize(new Dimension(0, 30));
		gradientBar.setShowMarkers(true);
		gradientBar.setCornerRadius(5);
		gradientBar.setPaddingHorizontal(5);
		gradientBar.setMarkerListener(onGradientMarkerChange);
		panelHeader.add(Box.createVerticalStrut(20)); // add spacer
		panelHeader.add(gradientBar);
		
		panelMarkerEdit = new MarkerEditPanel();
		panelHeader.add(panelMarkerEdit);
		
		JPanel panelSetup = new JPanel();
		panelSetup.setBackground(Style.panelBackground);
		panelSetup.setLayout(new BorderLayout());
		
		TScrollPane scrollPane = new TScrollPane(panelSetup);
		//scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setViewportBorder(null);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, BorderLayout.CENTER);
		
		colorPicker =  new ColorPicker(Color.RED, 0, true, true, false, false);
		colorPicker.setBackground(Style.panelBackground);
		colorPicker.addColorListener(colorChangeListener);
		colorPicker.setPreferredSize(new Dimension(300, 200));
		colorPicker.setMinimumSize(new Dimension(100, 200));
		
		JPanel panelCodeEditor = new JPanel();
		panelCodeEditor.setLayout(new BorderLayout());
		panelCodeEditor.setBackground(Style.panelBackground);
		
		editor = new JTextPane();
		editor.setBackground(Style.panelDarkBackground);
		editor.setForeground(Style.textColor);
		editor.setCaretColor(Style.accent);
		editor.setFont(Style.getFontRegualar(14));
		editor.setStyledDocument(createStyledDocument());
		editor.getDocument().addDocumentListener(onCodeEditorChange);
		panelCodeEditor.add(editor, BorderLayout.CENTER);
		
		TextLineNumber lineNumbers = new TextLineNumber(editor);
		lineNumbers.setBackground(Style.panelAccentBackground);
		lineNumbers.setForeground(Style.textColorDarker);
		lineNumbers.setCurrentLineForeground(Style.accent);
		panelCodeEditor.add(lineNumbers, BorderLayout.WEST);
		
		TabButtons tabBtns = new TabButtons();
		tabBtns.setBackground(Style.panelBackground);
		tabBtns.setHeight(25);
		tabBtns.addButton("ColorPicker");
		tabBtns.addButton("Code Editor");
		tabBtns.setActionListener(l -> {
			Component centerComp = ((BorderLayout) panelSetup.getLayout()).getLayoutComponent(BorderLayout.CENTER);
			if(centerComp != null) {
				panelSetup.remove(centerComp);
			}
			if("ColorPicker".equals(l.getActionCommand())) {
				panelSetup.add(colorPicker, BorderLayout.CENTER);
				// update color picker with color from last selected marker index
				updateColorPicker(gradientBar.getSelectedMarkerIndex());
			} else {
				panelSetup.add(panelCodeEditor, BorderLayout.CENTER);
			}
			panelSetup.updateUI();
		});
		panelSetup.add(tabBtns, BorderLayout.NORTH);
		tabBtns.selectButton("ColorPicker");
		
	}
	
	private ColorListener colorChangeListener = new ColorListener() {
		@Override
		public void onColorChanged(Color color) {
			int selectedIndex = gradientBar.getSelectedMarkerIndex();
			if(palette != null && selectedIndex >= 0 && selectedIndex < palette.getPalette().size()) {
				palette.getPalette().setColorAtIndex(selectedIndex, ColorTool.convert(color));
				updateGradientBar();
				showPaletteInEditor(palette);
			}
		}
	};
	
	private MarkerListener onGradientMarkerChange = new MarkerListener() {
		@Override
		public void onMarkerSelected(int index) {
			updateColorPicker(index);
		}
		@Override
		public void onMarkerDragged(int index, boolean eventEnd) {
			if(eventEnd) { // update editor only when dragging stopped
				showPaletteInEditor(palette);
			}
		}
	};
	
	protected void removeSelectedMarker() {
		int index = gradientBar.getSelectedMarkerIndex();
		if(index != -1 && palette != null) {
			palette.getPalette().removeColorAtIndex(index);
			updateGradientBar();
			gradientBar.setSelectedMarker(index - 1);
			updateColorPicker(gradientBar.getSelectedMarkerIndex());
		}
	}
	
	protected void addMarker() {
		if(palette != null) {
			try {
				AbstractPalette p = palette.getPalette();
				de.lars.remotelightcore.utils.color.Color color = p.size() > 0 ? p.getColorAtIndex(p.size() - 1) : de.lars.remotelightcore.utils.color.Color.RED;
				int index = palette.getPalette().addColor(color);
				gradientBar.setSelectedMarker(index);
				updateGradientBar();
				updateColorPicker(index);
			} catch(Exception e) {
				ExceptionHandler.handle(new Exception("Could not add color to palette.", e));
			}
		}
	}
	
	public void updateValues(boolean updateEditor) {
		if(palette != null) {
			fieldName.setText(palette.getName());
			gradientBar.setColorPalette(palette.getPalette());
			gradientBar.resetMarkerSelection();
			gradientBar.repaint();
			if(palette.getPalette().size() > 0) {
				// select first marker and display its color in the color picker
				gradientBar.setSelectedMarker(0);
				colorPicker.setSelectedColor(ColorTool.convert(palette.getPalette().getColorAtIndex(0)));
			}
			if(updateEditor) {
				showPaletteInEditor(palette);
			}
		}
	}
	
	public void updateValues() {
		updateValues(true);
	}
	
	public void updateGradientBar() {
		if(palette != null) {
			gradientBar.setColorPalette(palette.getPalette());
			gradientBar.repaint();
		}
	}
	
	public void updateColorPicker(int index) {
		if(palette != null && index >= 0 && index < palette.getPalette().size()) {
			colorPicker.setSelectedColor(ColorTool.convert(palette.getPalette().getColorAtIndex(index)));
		}
	}

	public PaletteData getPalette() {
		return palette;
	}

	public void setPalette(PaletteData palette) {
		this.palette = palette;
	}
	
	
	private DocumentListener onCodeEditorChange = new DocumentListener() {
		@Override
		public void removeUpdate(DocumentEvent e) {
			if(editorTextReplaceMode) {
				return;
			}
			try {
				parsePaletteFromEditor(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (BadLocationException e1) {}
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			if(editorTextReplaceMode) {
				editorTextReplaceMode = false;
				return;
			}
			try {
				parsePaletteFromEditor(e.getDocument().getText(0, e.getDocument().getLength()));
			} catch (BadLocationException e1) {}
		}
		@Override
		public void changedUpdate(DocumentEvent e) {}
	};
	
	public void parsePaletteFromEditor(String text) {
		try {
			PaletteData pd = PaletteParser.parseFromString(text);
			if(!fieldName.getText().equals(text)) {
				fieldName.setText(pd.getName());
			}
			palette.setPalette(pd.getPalette());
			updateGradientBar();
		} catch (PaletteParseException e) {
			// TODO: display error message in UI
			System.err.println(e.getMessage());
		}
	}
	
	public void showPaletteInEditor(PaletteData paletteData) {
		try {
			String code = PaletteParser.parseToString(paletteData);
			editorTextReplaceMode = true;
			editor.setText(code);
		} catch (PaletteParseException e) {
			ExceptionHandler.handle(new PaletteParseException("Error while parsing color palette to String.", e));
		}
	}
	
	private CustomStyledDocument createStyledDocument() {
		CustomStyledDocument doc = new CustomStyledDocument();
		doc
			.addRegEx("[{}]", doc.brackets) // brackets
			.addRegEx("\\b\\d*\\.?\\d+\\b", doc.value) // any (decimal) number
			.addRegEx("\\d*\\.?\\d+[ \\t]*(?=([:]))", doc.key) // any (decimal) number before ':'
			.addRegEx("^.*(?=([{]))", doc.highlighted) // everything before '{'
			.addRegEx("/\\*(?:.|[\\n\\r])*?\\*/", doc.comment); // comments
		return doc;
	}
	
	
	private class MarkerEditPanel extends JPanel {
		private static final long serialVersionUID = 3800579001431712108L;
		
		private JButton btnAddMarker;
		private JButton btnRemoveMarker;
		
		public MarkerEditPanel() {
			setBackground(Style.panelBackground);
			setLayout(new WrapLayout(WrapLayout.LEFT));
			
			btnAddMarker = new JButton("Add marker");
			UiUtils.configureButton(btnAddMarker);
			btnAddMarker.addActionListener(l -> addMarker());
			add(btnAddMarker);
			
			btnRemoveMarker = new JButton("Remove marker");
			UiUtils.configureButton(btnRemoveMarker);
			btnRemoveMarker.addActionListener(l -> removeSelectedMarker());
			add(btnRemoveMarker);
		}
		
	}

}
