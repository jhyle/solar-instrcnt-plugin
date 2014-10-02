package org.sonar.plugins.instrcnt;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.Type;

class JavaVar
{
	private String name;
	private Set<Type> types;

	public JavaVar(String name)
	{
		this.name = name;
	}
	
	public void addType(Type type)
	{
		if (types == null) {
			types = new HashSet<Type>();
		}
		types.add(type);
	}

	public Set<Type> getTypes()
	{
		if (types == null) {
			return Collections.emptySet();
		} else {
			return types;
		}
	}
	
	public String toString()
	{
		if (types == null) {
			return name + "=null";
		} else {
			boolean first = true;
			StringBuilder sb = new StringBuilder().append(name).append("=[");
			for (Type type : types) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append(type.toString());
			}
			return sb.append("]").toString();
		}
	}
}
