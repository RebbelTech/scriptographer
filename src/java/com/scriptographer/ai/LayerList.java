/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 * File created on 11.01.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.AbstractReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;

/**
 * The LayerList object represents a list of layers in an Illustrator document.
 * LayerLists are not created through a constructor, they're always accessed
 * through the {@link Document#layers} property.
 * 
 * @author lehni
 * 
 * @jshide
 */
public class LayerList extends AbstractReadOnlyList<Layer> implements ReadOnlyStringIndexList<Layer> {
	Document document;

	protected LayerList(Document document) {
		this.document = document;
	}
	
	private static native int nativeSize(int docHandle);

	public int size() {
		return nativeSize(document.handle);
	}

	private static native Layer nativeGet(int docHandle, int index);

	/**
	 * Retrieves a layer 
	 * @param index the index of the layer
	 */
	public Layer get(int index) {
		return nativeGet(document.handle, index);
	}

	private static native Layer nativeGet(int docHandle, String name);

	/**
	 * Retrieves a layer 
	 * @param name the name of the layer
	 */
	public Layer get(String name) {
		return nativeGet(document.handle, name);
	}
}
