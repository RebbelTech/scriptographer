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
 * File created on 18.01.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.util.HashMap;

import com.scratchdisk.list.ExtendedArrayList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class ItemList extends ExtendedArrayList<Item> implements ReadOnlyStringIndexList<Item> {
	HashMap<Item, Item> map;

	public ItemList() {
		map = new HashMap<Item, Item>();
	}

	/**
	 * @jshide
	 */
	public ItemList(ReadOnlyList<Item> items) {
		this();
		addAll(items);
	}

	public ItemList(Item[] items) {
		this(Lists.asList(items));
	}

	public Class<Item> getComponentType() {
		return Item.class;
	}

	/**
	 * Adds the item to the ItemSet, only if it does not already exist in it.
	 * @param index
	 * @param item
	 * @return true if the item was added to the set.
	 */
	public Item add(int index, Item item) {
		if (map.get(item) == null) {
			if (super.add(index, item) != null) {
				map.put(item, item);
				return item;
			}
		}
		return null;
	}

	public Item get(String name) {
		for (Item item : this)
			if (!item.isDefaultName() && item.getName().equals(name))
				return item;
		return null;
	}

	public Item remove(int index) {
		Item obj = super.remove(index);
		if (obj != null)
			map.remove(obj);
		return obj;
	}

	public boolean contains(Object element) {
		return map.get(element) != null;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for (int i = 0; i < size(); i++) {
			if (i > 0)
				buffer.append(", ");
			buffer.append(get(i).toString());
		}
		buffer.append("]");
		return buffer.toString();
	}
}
