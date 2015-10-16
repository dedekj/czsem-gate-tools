package czsem.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class PercentilBuilder <T extends Comparable<T>> {
	private List<T> data;

	public PercentilBuilder(List<T> data) {
		this.data = data;
	}

	public PercentilBuilder() {
		this.data = new ArrayList<T>();
	}

	public PercentilBuilder(Collection<T> addData) {
		this();
		this.data.addAll(addData);
	}

	public PercentilBuilder(MultiSet<T> mutiset) {
		this(new ArrayList<T>(mutiset.sum()));
		
		for (Entry<T, Integer> e : mutiset.asMap().entrySet()) {
			for (int i=0; i < e.getValue(); i++)
				data.add(e.getKey());
		}
	}
	
	public void add(T item) {
		data.add(item);
	}
	
	public void sort() {
		Collections.sort(data);
	}
	
	public T getPercentil(int percentil) {
		return data.get(data.size()*percentil/100);
	}

}
