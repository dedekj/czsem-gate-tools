package czsem.fs.query.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class CloneableIteratorList<T> implements CloneableIterator<T> {
	
	protected final List<CloneableIterator<T>> list;
	protected final Iterator<CloneableIterator<T>> listIterator; 
	
	protected Iterator<T> current;

	public CloneableIteratorList(List<CloneableIterator<T>> list) {
		this.list = list;
		this.listIterator = list.iterator();
		this.current = listIterator.hasNext() ? listIterator.next() : Collections.emptyIterator();  
	}

	@Override
	public boolean hasNext() {
		return current.hasNext() || tryLoadNext();
	}

	protected boolean tryLoadNext() {
		while (listIterator.hasNext()) {
			current = listIterator.next();
			if (current.hasNext()) return true;
		}

		return false;
	}

	@Override
	public T next() {
		if (! hasNext()) throw new NoSuchElementException();
		return current.next();
	}

	@Override
	public CloneableIterator<T> cloneInitial() {
		List<CloneableIterator<T>> clone = list.stream().map(CloneableIterator::cloneInitial).collect(Collectors.toList());
		return new CloneableIteratorList<>(clone);
	}

}
