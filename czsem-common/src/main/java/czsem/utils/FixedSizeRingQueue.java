package czsem.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//from: http://stackoverflow.com/questions/13157675/looking-for-a-circular-fixed-size-array-based-deque
public class FixedSizeRingQueue<E> {

	private E[] data;
	int n = 0;

	public FixedSizeRingQueue(int size) {
		data = newArray(size);
	}

	public void push(E s) {
		data[n] = s;
		n = (n + 1) % data.length;
	}

	public void shift(E s) {
		data[n = (n - 1) % data.length] = s;
	}

	public E get(int index) {
		return data[(n + index) % data.length];
	}
	
	public List<E> copyToList() {
		ArrayList<E> ret = new ArrayList<E>(data.length);
		for (int i = 0; i< data.length; i++) {
			E elem = get(i);
			if (elem != null)
				ret.add(elem);
		}
		return ret;		
	}
	
	//from: http://stackoverflow.com/a/8052827/1857897
	@SafeVarargs
	static <E> E[] newArray(int length, E... array)
	{
	    return Arrays.copyOf(array, length);
	}

}
