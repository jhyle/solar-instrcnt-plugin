package org.sonar.plugins.instrcnt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import soot.Type;

public class VarHolder
{
	private Map<Integer, JavaVar> vars;

	public void addVarType(Integer varId, String name, Type type)
	{
		if (vars == null) {
			vars = new HashMap<Integer, JavaVar>();
		}
		
		JavaVar var = vars.get(varId);
		if (var == null) {
			var = new JavaVar(name);
			vars.put(varId, var);
		}

		var.addType(type);
	}

	public void addVarTypes(Integer varId, String name, Set<Type> types)
	{
		for (Type type : types) {
			addVarType(varId, name, type);
		}
	}
	
	public Set<Type> getVarTypes(Integer varId)
	{
		if (vars == null) {
			return Collections.emptySet();
		}
		
		JavaVar var = vars.get(varId);
		if (var == null) {
			return Collections.emptySet();
		}
		
		return var.getTypes();
	}

	public String toString()
	{
		if (vars == null) {
			return "";
		} else {
			boolean first = true;
			StringBuilder sb = new StringBuilder().append("[");
			for (JavaVar var : vars.values()) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append(var.toString());
			}
			return sb.append("]").toString();
		}
	}
}
