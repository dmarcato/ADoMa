package com.megadevs.adoma.utils;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class CircularHashMap<T,V> implements Iterable<V>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5719448729990654367L;
	
	private LinkedList<T> KEYS;
	private LinkedList<V> DATA;
	private int MAXSIZE;

	public CircularHashMap(int maxSize){
		setMAXSIZE(maxSize);
		KEYS = Lists.newLinkedList();
		DATA = Lists.newLinkedList();
	}

	/**
	 * Add an element
	 * @param key
	 * @param value
	 * @return return true if the buffer was full and the older element was cleared.
	 */
	public boolean add(T key, V value){
		boolean state=false;
		synchronized (this) {
			if(KEYS.size() == MAXSIZE){
				KEYS.removeFirst();
				invokeCustomDestructor(DATA.removeFirst());
				state=true;
			}
			KEYS.add(key);
			DATA.add(value);
		}

		return state;
	}


	protected void invokeCustomDestructor(V obj){
		//Do nothing here..
	}


	/**
	 * Return an element by key
	 * @param key
	 * @return Return the element with the specified key, null otherwise
	 */
	public V get(T key){
		synchronized (this) {
			for(int i=0;i<KEYS.size();i++){
				if(KEYS.get(i).equals(key))return DATA.get(i);
			}
		}
		return null;

	}
	
	/**
	 * Return an element by position
	 * @param position
	 * @return Return the element at the given position, null otherwise
	 */
	public V get(int position) {
		return DATA.get(position);
	}
	
	/**
	 * Return all the values
	 * @return The LinkedList with values
	 */
	public LinkedList<V> values() {
		return DATA;
	}


	public int getMAXSIZE() {
		return MAXSIZE;
	}

	
	public void setMAXSIZE(int mAXSIZE) {
		MAXSIZE = mAXSIZE;
	}
	
	public int size() {
		return KEYS.size();
	}

	@Override
	public Iterator<V> iterator() {
		return new CircularHashMapIterator();
	}
	
	private class CircularHashMapIterator implements Iterator<V> {
		private int i = 0;
		
		@Override
		public boolean hasNext() {
			return i < DATA.size();
		}

		@Override
		public V next() {
			if (!hasNext()) throw new NoSuchElementException();
			return DATA.get(i++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	//Example main
	/*
	public static void main(String[] args) {
		CircularHashMap<String, Integer> x = new CircularHashMap<String, Integer>(3);
		System.out.println(x.add("uno", new Integer(1)));
		System.out.println(x.add("due", new Integer(2)));
		System.out.println(x.add("tre", new Integer(3)));
		System.out.println(x.add("quattro", new Integer(4)));
		System.out.println(x.add("cinque", new Integer(5)));
		System.out.println("get uno: "+x.get("uno"));
		System.out.println("get quattro: "+x.get("quattro"));
		System.out.println();
	}
	*/

}
