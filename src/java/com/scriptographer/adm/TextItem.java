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
 * File created on 18.10.2005.
 *
 * $RCSfile: TextItem.java,v $
 * $Author: lehni $
 * $Revision: 1.7 $
 * $Date: 2006/11/04 11:47:26 $
 */

package com.scriptographer.adm;

import com.scriptographer.js.Unsealed;

public abstract class TextItem extends Item implements Unsealed {

	protected TextItem(Dialog dialog, long handle) {
		super(dialog, handle);
	}
	
	protected TextItem(Dialog dialog, int type) {
		super(dialog, type, OPTION_NONE);
	}

	/*
	 * item text accessors
	 * 
	 */

	public native void setText(String text);
	public native String getText();
}
