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
 * File created on 02.01.2005.
 *
 * $Id: UnsealedJavaObject.java 238 2007-02-16 01:09:06Z lehni $
 */

package com.scriptographer.script.rhino;

import java.util.*;

import org.mozilla.javascript.*;

/**
 * @author lehni
 */
public class ExtendedJavaObject extends NativeJavaObject {
	HashMap properties;
	ExtendedJavaClass classWrapper = null;
	
	/**
	 * @param scope
	 * @param javaObject
	 * @param staticType
	 */
	public ExtendedJavaObject(Scriptable scope, Object javaObject,
		Class staticType, boolean unsealed) {
		super(scope, javaObject, staticType);
		properties = unsealed ? new HashMap() : null;
		classWrapper = ExtendedJavaTopPackage.getClassWrapper(scope, staticType);
	}

    public Scriptable getPrototype() {
    	Scriptable prototype = super.getPrototype();
        if (prototype == null)
        	prototype = classWrapper.getInstancePrototype();
        return prototype;
    }

	public void delete(String name) {
		if (properties != null)
			properties.remove(name);
	}
	
	public Object get(String name, Scriptable start) {
		Object obj;
		if (super.has(name, start)) {
			obj = super.get(name, start);
		} else if (properties != null && properties.containsKey(name)) {
			// see wether this object defines the property.
			obj = properties.get(name);
		} else {
			Scriptable prototype = this.getPrototype();
			if (name.equals("prototype")) {
				if (prototype == null) {
					// If no prototype object was created it, produce it on the fly.
					prototype = new NativeObject();
					this.setPrototype(prototype);
				}
				obj = prototype;
			} else if (prototype != null) {
				// if not, see wether the prototype maybe defines it.
				// NativeJavaObject misses to do so:
				obj = prototype.get(name, start);
			} else {
				obj = Scriptable.NOT_FOUND;
			}
		}
		return obj;
	}
	
	public void put(String name, Scriptable start, Object value) {
		if (super.has(name, start)) {
			super.put(name, start, value);
		} else if (name.equals("prototype")) {
			if (value instanceof Scriptable)
				this.setPrototype((Scriptable) value);
		} else if (properties != null) {
			properties.put(name, value);
		}
	}
	
	public boolean has(String name, Scriptable start) {
		boolean has = super.has(name, start);
		if (!has && properties != null)
			has = properties.get(name) != null;
		return has;
	}
	
	public Object[] getIds() {
		// concatenate the super classes ids array with the keySet from
		// properties HashMap:
		Object[] javaIds = super.getIds();
		if (properties != null) {
			int numProps = properties.size();
			if (numProps == 0)
				return javaIds;
			Object[] ids = new Object[javaIds.length + numProps];
			Collection propIds = properties.keySet();
			propIds.toArray(ids);
			System.arraycopy(javaIds, 0, ids, numProps, javaIds.length);
			return ids;
		} else {
			return javaIds;
		}
	}
}