package czsem.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MultiSet<T> implements Iterable<T>
{
	private Map<T, Integer> map = new HashMap<T, Integer>();
	
	public String toFormatedString(String separator) {
		return toFormatedString(map.entrySet(), separator);
	}
	
	public List<Entry<T,Integer>> getSorted() {
		ArrayList<Entry<T, Integer>> sortedEntries = new ArrayList<Entry<T,Integer>>(map.entrySet());
		Collections.sort(sortedEntries, new Comparator<Entry<T, Integer>>() {
			@Override
			public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		return sortedEntries;
	}

	public List<Entry<T,Integer>> getSortedByKey() {
		ArrayList<Entry<T, Integer>> sortedEntries = new ArrayList<Entry<T,Integer>>(map.entrySet());
		Collections.sort(sortedEntries, new Comparator<Entry<T, Integer>>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Entry<T, Integer> o1, Entry<T, Integer> o2) {
				return ((Comparable<T>) o2.getKey()).compareTo(o1.getKey());
			}
		});
		
		return sortedEntries;
	}

	public String toOrderedFormatedString(String separator) {		
		List<Entry<T, Integer>> sortedEntries = getSorted();
		return toFormatedString(sortedEntries, separator);
	}

	public String toFormatedString(Iterable<Entry<T,Integer>> set, String separator)
	{
		StringBuilder sb = new StringBuilder();
		
		for (Entry<T, Integer> e : set)
		{
			toFormatedString(e.getKey(), e.getValue(), sb, separator);
		}
		
		return sb.toString();
	}

	public void toFormatedString(T key, Integer count, StringBuilder sb, String separator)
	{
			String outstr = "null";
			if (key != null) outstr = key.toString();
			sb.append(outstr);
			sb.append(": ");
			sb.append(count);
			sb.append(separator);
	}

	public void print(PrintStream out)
	{
		out.print(toFormatedString(", "));
	}
	
	public void printSorted(PrintStream out, String separator) {
		out.print(toOrderedFormatedString(separator));
	}

	public void printTopN(PrintStream out, String separator, int n) {
		List<T> topKeys = getTopKeys(n);
		
		StringBuilder sb = new StringBuilder();
		
		for (T key : topKeys) {
			toFormatedString(key, get(key), sb, separator);
		}
		
		out.print(sb.toString());
	}


	public int size() {
		return map.keySet().size();
	}

	public int sum() {
		int ret = 0;
		for (int count : map.values()) ret += count;
		return ret;
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @return 0 if the element is not in the set
	 */
	public int get(T element) {
		Integer i = map.get(element);
		if (i == null) return 0;
		else return i;
	}

	@Override
	public Iterator<T> iterator() {
		return map.keySet().iterator();
	}


	public int add(T e) {
		return add(e, 1);
	}
	
	public int add(T e, int count) {
		Integer i = get(e) + count;		
		map.put(e, i);
		return i;
	}

	public int removeAll(T o) {
		Integer ret = map.remove(o);
		return  ret == null ? 0 : ret;
	}

	public int remove(T o) {
		Integer i = map.get(o);
		if (--i<=0)
		{
			map.remove(o);
			return 0;
		}
		else
		{
			map.put(o,i);
			return i;
		}
	}

	public Object[] toArray() {
		return map.keySet().toArray();
	}

	public T[] toArray(T[] a) {
		return map.keySet().toArray(a);
	}

	public void clear() {
		map.clear();		
	}

	public void addAll(Iterable<T> data)
	{
		for (T t : data)
		{
			add(t);
		}
	}

	public void addAll(T[] data) {
		addAll(data, data.length);
	}
	
	@SuppressWarnings("unchecked")
	public void addAllFiltered(String[] data, int minLength) {
		for (int i = 0; (i < data.length); i++)
		{
			if (data[i] == null || data[i].length() < minLength)
				continue;
			add((T) data[i]);			
		}
	}


	public void addAll(T[] data, int count)
	{
		for (int i = 0; (i < data.length) && (i < count) ; i++)
		{
			add(data[i]);			
		}
		
	}

	public static class TopList<T>
	{
		public static class TopListEntry<T>
		{
			public TopListEntry(T t, int count2)
			{
				data = t;
				count = count2;
			}
			T data;
			int count = Integer.MIN_VALUE;
		}

		private TopListEntry<T>[] entries;
		
		@SuppressWarnings("unchecked")
		public TopList(int capacity)
		{
			entries = new TopListEntry[capacity+1];
		}
		
		@SuppressWarnings("rawtypes")
		public void add(T t, int count)
		{
			entries[0] = new TopListEntry<T>(t, count);
			Arrays.sort(entries, new Comparator<TopListEntry>() {

				@Override
				public int compare(TopListEntry o1, TopListEntry o2) {
					if (o1 == null)
					{
						if (o2 == null) return 0;
						else return -1;
					}
					if (o2 == null) return 1;
					return new Integer(o1.count).compareTo(o2.count) ;
				}
			});
		}
		
		public List<T> getTopKeys()
		{
			List<T> ret = new ArrayList<T>(entries.length-1);
			
			for (int i=0; i<entries.length-1; i++)
			{
				if (entries[entries.length-i-1] == null) break;
				ret.add(i, entries[entries.length-i-1].data);
			}
			
			return ret;
			
		}

		
	}
	
	public List<T> getTopKeys(int top_count)
	{
		TopList<T> top_list = new TopList<T>(top_count); 
				
		for (T k : map.keySet())
		{
			top_list.add(k, map.get(k));
		}
		
		return top_list.getTopKeys();		
	}
	
	public static void main(String[] args)
	{
		TopList<String> t = new TopList<String>(5);
		
		t.add("prvni", 3);
/*
		t.add("druhy", 3);
		t.add("sto", 100);
		t.add("treti", 2);
		t.add("ctvrty", 50);
*/	
		t.add("paty", 1);
		t.add(null, 7);
		
		List<String> k = t.getTopKeys();
		
		for (int i = 0; i < k.size(); i++)
		{
			System.err.println(k.get(i));			
		}
	}

	public Map<T, Integer> asMap() {
		return map;
	}

	public void addMultiSet(MultiSet<T> ms) {
		for (Entry<T, Integer> e : ms.asMap().entrySet()) {
			add(e.getKey(), e.getValue());
		}
	}

	public void removeAllBellowFreq(int minFreq) {
		List<T> toRemove = new ArrayList<>();
		for (Entry<T, Integer> e : asMap().entrySet()) {
			if (e.getValue() < minFreq) toRemove.add(e.getKey());
		}
		
		for (T r : toRemove) {
			removeAll(r);
		}	
	}

	public int removeAll(Collection<T> toRemoveCollection) {
		int ret = 0;
		for (T key : toRemoveCollection) {
			ret += removeAll(key);
		}
		return ret;
	}
}
