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
 * File created on 20.10.2005.
 * 
 * $Id:ImageStatic.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

import java.io.IOException;

/**
 * @author lehni
 */
public class ImagePane extends Item {

	/**
	 * Creates a ImageStatic Item.
	 * 
	 * @param dialog
	 * @param image
	 */
	public ImagePane(Dialog dialog) {
		super(dialog, ItemType.PICTURE_STATIC);
	}

	private Image image = null;
	
	private native void nativeSetImage(int iconHandle);

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		nativeSetImage(image != null ? image.createIconHandle() : 0);
		this.image = image;
	}
	
	public void setImage(Object obj) throws IOException {
		setImage(Image.getImage(obj));
	}
}
