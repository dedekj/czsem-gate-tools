package czsem.fs.query.utils;

import java.util.NoSuchElementException;

public class SingletonIterator<T> implements CloneableIterator<T> {
	
	protected final T value;
	protected boolean hasNext = true; 

	public SingletonIterator(T value) {
		this.value = value;
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public T next() {
		if (! hasNext()) throw new NoSuchElementException();

		hasNext = false;
		return value;
	}

	@Override
	public CloneableIterator<T> cloneInitial() {
		return new SingletonIterator<>(value);
	}

}
