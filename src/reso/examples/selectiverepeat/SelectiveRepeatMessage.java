package reso.examples.selectiverepeat;

import reso.common.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * It generates a list of random numbers and returns them as a payload
 */
public class SelectiveRepeatMessage implements Message {
	
	private final List<Integer> numbers;
	
	// Creating a new ArrayList of integers and then calling the generate method to fill it with random
	// numbers.
	public SelectiveRepeatMessage(int size) {
		numbers = new ArrayList<>();
		generate(size);
	}

	/**
	 * It generates a list of random numbers
	 * 
	 * @param size the size of the array
	 */
	private void generate(int size) {
		for (int i=0; i<size; i++){
			numbers.add((int) (Math.random() * 100));
		}
	}

	/**
	 * This function returns a list of integers.
	 * 
	 * @return A list of integers
	 */
	public List<Integer> getPayload(){
		return numbers;
	}

	
	/**
	 * The SelectiveRepeat message carries a single 'int'
	 * 
	 * @return The number of bytes in the message.
	 */
	@Override
	public int getByteLength() {
		// The SelectiveRepeat message carries a single 'int'
		return 512*numbers.size();
	}
}
