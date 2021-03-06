/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on May 3, 2010.
 */

package com.scriptographer.adm;

import java.awt.GridLayout;
import java.util.EnumSet;

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.EnumUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.layout.HorizontalLayout;
import com.scriptographer.ai.FontFamily;
import com.scriptographer.ai.FontList;
import com.scriptographer.ai.FontWeight;

/**
 * @author lehni
 *
 */
public class FontPopupList extends ItemGroup {

	private FontList fontList;
	private Item familyItem;
	private Item weightItem;
	private PopupList familyList;
	private PopupList weightList;

	public FontPopupList(Dialog dialog, EnumSet<FontPopupListOption> options) {
		super(dialog);
		fontList = FontList.getInstance();
		if (options != null && options.contains(FontPopupListOption.EDITABLE)) {
			TextEditItem familyEdit = new TextEdit(dialog, new TextOption[] {
					TextOption.POPUP, TextOption.SCROLLING
			}) {
				protected void onChange() {
					if (updateTextEdit(this) && updateWeightList())
						FontPopupList.this.onChange();
				}
			};
			familyItem = familyEdit;
			familyList = familyEdit.getPopupList();
			TextEditItem weightEdit = new TextEdit(dialog, new TextOption[] {
					TextOption.POPUP, TextOption.SCROLLING
			}) {
				protected void onChange() {
					if (updateTextEdit(this))
						FontPopupList.this.onChange();
				}
			};
			weightItem = weightEdit;
			weightList = weightEdit.getPopupList();
		} else {
			familyItem = familyList = new PopupList(dialog) {
				protected void onChange() {
					if (updateWeightList())
						FontPopupList.this.onChange();
			}
			};
			weightItem = weightList = new PopupList(dialog){
				protected void onChange() {
					FontPopupList.this.onChange();
				}
			};
		}
		// Add a gap between the components.
		if (options != null && options.contains(FontPopupListOption.VERTICAL)) {
			setLayout(new GridLayout(0, 1, 0, 3));
		} else {
			setLayout(new HorizontalLayout(HorizontalLayout.LEFT, 3, 0));
		}
		int width = familyItem.getTextSize(" ").width;
		familyItem.setWidth(width * 48);
		weightItem.setWidth(width * 32);
		add(familyItem);
		add(weightItem);
		updateFamilyList();
		updateWeightList();
	}

	public FontPopupList(Dialog dialog, FontPopupListOption[] options) {
		this(dialog, EnumUtils.asSet(options));
	}

	public FontPopupList(Dialog dialog) {
		this(dialog, (EnumSet<FontPopupListOption>) null);
	}
	
	private void updateFamilyList() {
		familyList.removeAll();
		for (FontFamily family : fontList) {
			ListEntry entry = new ListEntry(familyList);
			entry.setText(family.getName());
		}
		familyList.setSelectedEntry(familyList.getFirst());
	}

	private boolean updateWeightList() {
		// Try to preserve selected weight across fonts:
		ListEntry selected = weightList.getSelectedEntry();
		String current = selected != null ? selected.getText() : null;
		selected = null;
		weightList.removeAll();
		FontFamily family = getFontFamily();
		if (family != null) {
			for (FontWeight weight : family) {
				ListEntry entry = new ListEntry(weightList);
				String name = weight.getName();
				entry.setText(name);
				if (name.equals(current))
					selected = entry;
			}
			if (selected == null)
				selected = weightList.getFirst();
			weightList.setSelectedEntry(selected);
			return true;
		}
		return false;
	}

	protected boolean updateTextEdit(TextEdit textItem) {
		PopupList list = textItem.getPopupList();
		String text = textItem.getText().toLowerCase();
		if (list.get(text) == null) {
			for (ListEntry entry : list) {
				if (entry.getText().toLowerCase().startsWith(text)) {
					list.setSelectedEntry(entry);
					return true;
				}
			}
		}
		return false;
	}

	private Callable onChange = null;

	protected void onChange() {
		if (onChange != null)
			ScriptographerEngine.invoke(onChange, this);
	}

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	public FontFamily getFontFamily() {
		ListEntry entry = familyList.getSelectedEntry();
		if (entry != null)
			return fontList.get(entry.getText());
		return null;
	}

	public void setFontFamily(FontFamily family) {
		ListEntry entry = familyList.get(family.getName());
		if (entry != null) {
			familyList.setSelectedEntry(entry);
			updateWeightList();
		}
	}

	public FontWeight getFontWeight() {
		FontFamily family = getFontFamily();
		if (family != null) {
			ListEntry entry = weightList.getSelectedEntry();
			if (entry != null)
				return family.get(entry.getText());
		}
		return null;
	}

	public void setFontWeight(FontWeight weight) {
		setFontFamily(weight.getFamily());
		ListEntry entry = weightList.get(weight.getName());
		if (entry != null)
			weightList.setSelectedEntry(entry);
	}
}
