/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * File created on 03.01.2005.
 *
 * $RCSfile: TextEdit.java,v $
 * $Author: lehni $
 * $Revision: 1.9 $
 * $Date: 2006/11/04 11:47:26 $
 */

package com.scriptographer.adm;

public class TextEdit extends TextValueItem {
	// Options
	public final static short
		OPTION_PASSWORD = 1 << 1,
		OPTION_UNICODE = 1 << 2, // [cpaduan] 6/18/02 - Creates a Unicode based edit box (if possible). Currently has no effect on Windows.
		OPTION_DISABLE_DRAG_DROP = 1 << 3, // Disables drag & drop from or to text edits. Currently mac-only.

	// self defined pseudo options, for creation of the right TYPE:
		OPTION_READONLY = 1 << 4,
		OPTION_MULTILINE = 1 << 5, 
		// for TYPE_TEXT_EDIT_POPUP:
		OPTION_POPUP = 1 << 6,
		OPTION_SCROLLING = 1 << 7;

	// ADMTextEditStyle, ADMTextEditPopupStyle
	public final static int
		STYLE_SINGLELINE = 0,
		STYLE_NUMERIC = 2,        // 'Numeric' means float. Default.
		STYLE_EXCLUSIVE = 5,      // only for TextEditPopup
		STYLE_TRACK_RAW_KEYS = 4, // Mac-only; ignores default Carbon event processing; not compatible with kADMUnicodeEditCreateOption
		STYLE_PASSWORD = 32;      // Win32 value for ES_PADMSWORD
	
	protected TextEdit(Dialog dialog, long handle) {
		super(dialog, handle);
	}
	
	/**
	 * For subclasses
	 * 
	 * @param dialog
	 * @param type
	 * @param options
	 */
	protected TextEdit(Dialog dialog, int type, int options) {
		super(dialog, type, options);
	}
	
	public TextEdit(Dialog dialog, int options) {
		// filter out the pseudo styles from the options:
		// (max. real bitis 3, and the mask is (1 << (max + 1)) - 1
		super(dialog, getType(options), options & ((1 << 4) - 1));
	}

	public TextEdit(Dialog dialog) {
		this(dialog, OPTION_NONE);
	}

	private static int getType(int options) {
		// abuse the ADM's password style for creating it as a type...
		if ((options & OPTION_PASSWORD) != 0) {
			return TYPE_TEXT_EDIT_PASSWORD;
		} else if ((options & OPTION_POPUP) != 0) {
			return (options & OPTION_SCROLLING) != 0 ? TYPE_TEXT_EDIT_SCROLLING_POPUP
				: TYPE_TEXT_EDIT_POPUP;
		} else {
			boolean multiline = ((options & OPTION_MULTILINE) != 0);
			if ((options & OPTION_READONLY) != 0) {
				return multiline ? TYPE_TEXT_EDIT_MULTILINE_READONLY
					: TYPE_TEXT_EDIT_READONLY;
			} else {
				return multiline ? TYPE_TEXT_EDIT_MULTILINE
					: TYPE_TEXT_EDIT;
			}
		}
	}
	
	/*
	 * Callback functions
	 */
	
	protected void onPreCut() throws Exception {
		callFunction("onPreCut");
	}
	
	protected void onCut() throws Exception {
		callFunction("onCut");
	}
	
	protected void onPreCopy() throws Exception {
		callFunction("onPreCopy");
	}
	
	protected void onCopy() throws Exception {
		callFunction("onCopy");
	}
	
	protected void onPrePaste() throws Exception {
		callFunction("onPrePaste");
	}
	
	protected void onPaste() throws Exception {
		callFunction("onPaste");
	}
	
	protected void onPreClear() throws Exception {
		callFunction("onPreClear");
	}
	
	protected void onClear() throws Exception {
		callFunction("onClear");
	}
	
	protected void onPreSelectionChange() throws Exception {
		callFunction("onPreSelectionChange");
	}
	
	protected void onSelectionChange() throws Exception {
		callFunction("onSelectionChange");
	}
	
	protected void onPreRedo() throws Exception {
		callFunction("onPreRedo");
	}
	
	protected void onRedo() throws Exception {
		callFunction("onRedo");
	}
	
	protected void onPreUndo() throws Exception {
		callFunction("onPreUndo");
	}
	
	protected void onUndo() throws Exception {
		callFunction("onUndo");
	}
	
	protected void onNotify(int notifier, ListEntry entry) throws Exception {
		switch (notifier) {
			case Notifier.NOTIFIER_PRE_CLIPBOARD_CUT:
				onPreCut();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_CUT:
				onCut();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_COPY:
				onPreCopy();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_COPY:
				onCopy();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_PASTE:
				onPrePaste();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_PASTE:
				onPaste();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_CLEAR:
				onPreClear();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_CLEAR:
				onClear();
				break;
			case Notifier.NOTIFIER_PRE_TEXT_SELECTION_CHANGED:
				onPreSelectionChange();
				break;
			case Notifier.NOTIFIER_TEXT_SELECTION_CHANGED:
				onSelectionChange();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_REDO:
				onPreRedo();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_REDO:
				onRedo();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_UNDO:
				onPreUndo();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_UNDO:
				onUndo();
				break;
		}
	}

	/* 
	 * text edits
	 * 
	 */

	public void setStringValue(Object value) {
		if (value != null)
			setText(value.toString());
	}
	
	public String getStringValue() {
		return getText();
	}
	
	public native int getPrecision();
	public native void setPrecision(int precision);
		
	public native void setMaxLength(int length);
	public native int getMaxLength();

	public native void setSelection(int start, int end);
	public native int[] getSelection();
	public native void selectAll();
	
	public void setSelection(int[] range) {
		setSelection(range[0], range[1]);
	}
	
	public void setSelection(int pos) {
		setSelection(pos, pos);
	}

	public native void setAllowMath(boolean allowMath);
	public native boolean getAllowMath();
	
	public native void setAllowUnits(boolean allowUnits);
	public native boolean getAllowUnits();

	/*
	 *  child items
	 */
	private static final int
		ITEM_TEXTEDIT = 3,
		ITEM_POPUP = 4;

	private TextEdit textEdit;
	private PopupList popupList;
	
	public TextEdit getTextEdit() {
		if (textEdit == null) {
			int handle = getChildItemHandle(ITEM_TEXTEDIT);
			textEdit = handle != 0 ? new TextEdit(dialog, handle) : null;
		}
		return textEdit;
	}
	
	public PopupList getPopupList() {
		if (popupList == null) {
			int handle = getChildItemHandle(ITEM_POPUP);
			popupList = handle != 0 ? new PopupList(dialog, handle) : null;
		}
		return popupList;
	}
}
