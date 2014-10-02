package org.sonar.plugins.instrcnt;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.SootClass;
import soot.Type;

class JavaClass extends VarHolder
{
	private static Map<String, JavaClass> classes = new HashMap<String, JavaClass>();
	
	public static JavaClass load(String name)
	{
		JavaClass clazz = classes.get(name);
		if (clazz == null) {
			clazz = new JavaClass(Scene.v().getSootClass(name));
			classes.put(name, clazz);
		}
		return clazz;
	}

	public static Collection<JavaClass> loaded()
	{
		return classes.values();
	}
	
	private SootClass clazz;
	
	private JavaClass(SootClass clazz)
	{
		this.clazz = clazz;
	}
	
	public JavaMethod getMethod(String name, List<?> params, List<Set<Type>> args)
	{
		SootClass actualClass = clazz;
		while (!actualClass.declaresMethod(name, params) && actualClass.hasSuperclass()) {
			actualClass = actualClass.getSuperclass();
		}
		try {
			return new JavaMethod(actualClass.getMethod(name, params), args);
		} catch (Exception e) {
			return null;
		}
	}

	public String toString()
	{
		String vars = super.toString(); 
		return clazz.toString() + (vars != "" ? "=" : "") + vars;
	}
}
