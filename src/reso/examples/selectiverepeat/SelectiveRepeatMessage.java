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
package reso.examples.selectiverepeat;

import reso.common.Message;

import java.util.ArrayList;
import java.util.List;

public class SelectiveRepeatMessage implements Message {
	
	private final List<Integer> numbers;
	
	public SelectiveRepeatMessage(int size) {
		numbers = new ArrayList<>();
		generate(size);
	}

	private void generate(int size) {
		for (int i=0; i<size; i++){
			numbers.add((int) (Math.random() * 100));
		}
	}

	public List<Integer> getPayload(){
		return numbers;
	}

	@Override
	public int getByteLength() {
		// The SelectiveRepeat message carries a single 'int'
		return Integer.SIZE / 8;
	}
}
