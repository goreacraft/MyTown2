package mytown.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic Flag class that should be able to serialize/deserialize any value.
 * 
 * Children should automatically inherit from their parents if the parent's value actually changed
 * 
 * @author Joe Goett
 *
 * @param <T>
 */
public class Flag<T> {
	private String name;
	private T value, inheritedValue;
	private Flag<T> parent;
	private List<Flag<T>> children;
	
	public Flag(String name, T value, Flag<T> parent, List<Flag<T>> children) {
		this.name = name;
		this.value = value;
		this.parent = parent;
		this.children = children;
	}
	
	public Flag(String name, T value, Flag<T> parent) {
		this(name, value, parent, new ArrayList<Flag<T>>());
	}
	
	public Flag(String name, T value) {
		this(name, value, null);
	}
	
	/**
	 * Will return the value this flag is set to, or the value it inherits from its parent
	 * @return
	 */
	public T getValue() {
		return value != null ? value : inheritedValue;
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public void inherit(T value, boolean forced) {
		if ((this.value == null || forced) && inheritedValue != value) {
			this.value = null;
			inheritedValue = value;
			for (Flag<T> child : children) {
				child.inherit(value, forced);
			}
		}
	}
	
	public Flag<T> getParent() {
		return parent;
	}
	
	public void setParent(Flag<T> parent) {
		this.parent = parent;
	}
	
	public void addChild(Flag<T> child) {
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild(Flag<T> child) {
		children.remove(child);
		child.setParent(null);
	}
	
	public List<Flag<T>> getChildren() {
		return children;
	}
	
	public Class<?> getType() {
		return value.getClass();
	}
	
	public boolean isArray() {
		return value.getClass().isArray();
	}
}