/*******************************************************************************
 * Copyright (c) 2011 Bruno Quoitin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Bruno Quoitin - initial API and implementation
 ******************************************************************************/
package reso.examples.project;

import reso.common.Message;

public class ProjectMessage implements Message {
	
	public final int num; 
	
	public ProjectMessage(int num) {
		this.num= num;
	}
	
	public String toString() {
		return "Project [num=" + num + "]";
	}

	@Override
	public int getByteLength() {
		// The project message carries a single 'int'
		return Integer.SIZE / 8;
	}
}
