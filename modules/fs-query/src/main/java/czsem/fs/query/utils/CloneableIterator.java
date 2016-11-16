package czsem.fs.query.utils;

import java.util.Iterator;

public interface CloneableIterator<T> extends Iterator<T> {

	public abstract CloneableIterator<T> cloneInitial();
	
	
	public default Iterable<T> toIterable() {
		return new Iterable<T>() {
			
			protected boolean reusable = true;
			
			@Override
			public Iterator<T> iterator() {
				if (reusable) {
					reusable = false;
					return CloneableIterator.this;
				}

				return cloneInitial();
			}
		}; 
	}

}