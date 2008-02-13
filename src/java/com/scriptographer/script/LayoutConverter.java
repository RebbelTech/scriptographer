/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on Feb 12, 2008.
 *
 * $Id$
 */

package com.scriptographer.script;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;

import com.scratchdisk.script.ArgumentConverter;
import com.scratchdisk.script.ArgumentReader;
import com.scriptographer.adm.TableLayout;

/**
 * @author lehni
 *
 */
public class LayoutConverter extends ArgumentConverter {

	private static HashMap flowLayoutAlignment = new HashMap();
	static {
		flowLayoutAlignment.put("left", new Integer(FlowLayout.LEFT));
		flowLayoutAlignment.put("center", new Integer(FlowLayout.CENTER));
		flowLayoutAlignment.put("right", new Integer(FlowLayout.RIGHT));
		flowLayoutAlignment.put("leading", new Integer(FlowLayout.LEADING));
		flowLayoutAlignment.put("trailing", new Integer(FlowLayout.TRAILING));
	}

	public Object convert(ArgumentReader reader) {
		if (reader.isArray()) {
			String str = reader.readString();
			if (str != null) {
				Integer alignment = (Integer) flowLayoutAlignment.get(str);
				if (alignment != null) {
					// FlowLayout
					return new FlowLayout(
							alignment.intValue(),
							reader.readInteger(0),
							reader.readInteger(0));
				} else {
					// TableLayout
					return new TableLayout(str,
							reader.readString(""),
							reader.readInteger(0),
							reader.readInteger(0));
				}
			} else {
				reader.revert();
				// Try if there's an array now:
				Object[] array = (Object[]) reader.readObject(Object[].class);
				if (array != null) {
					// TableLayout
					return new TableLayout(array,
							(Object[]) reader.readObject(Object[].class),
							reader.readInteger(0),
							reader.readInteger(0));
				} else {
					reader.revert();
					// BorderLayout
					return new BorderLayout(
							reader.readInteger(0),
							reader.readInteger(0));
				}
			}
		} else if (reader.isHash()) {
			if (reader.has("columns")) {
				String str = reader.readString("columns");
				if (str != null) {
					return new TableLayout(	str,
							reader.readString("rows", ""),
							reader.readInteger("", 0),
							reader.readInteger(0));
				} else {
					Object[] array = (Object[]) reader.readObject("columns", Object[].class);
					if (array != null) {
						return new TableLayout(array,
								(Object[]) reader.readObject("rows", Object[].class),
								reader.readInteger("hgap", 0),
								reader.readInteger("vgap", 0));
					}
				}
			} else if (reader.has("alignment")) {
				// FlowLayout
				Integer alignment = reader.readInteger("alignment");
				if (alignment == null)
					alignment = (Integer) flowLayoutAlignment.get(reader.readString("alignment"));
				if (alignment != null) {
					return new FlowLayout(
							alignment.intValue(),
							reader.readInteger("hgap", 0),
							reader.readInteger("vgap", 0));
				}
			} else {
				return new BorderLayout(
						reader.readInteger("hgap", 0),
						reader.readInteger("vgap", 0));
			}
		}
		return null;
	}
}