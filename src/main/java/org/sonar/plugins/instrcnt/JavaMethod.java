package org.sonar.plugins.instrcnt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;

class JavaMethod extends VarHolder
{
	private String signature;
	private SootMethod method;
	private List<Set<Type>> args;
	
	public JavaMethod(SootMethod method, List<Set<Type>> args)
	{
		if (!method.hasActiveBody() && method.isConcrete()) {
			method.retrieveActiveBody();
		}
		this.method = method;
		this.args = args;
	}

	public SootMethod getSource()
	{
		return method;
	}
	
	public Set<Type> getArgTypes(int i)
	{
		return args.get(i);
	}
	
	public Set<Type> getValueTypes(Value value)
	{
		if (value instanceof Local) {
			return getVarTypes(((Local) value).getNumber());
		} else if (value instanceof FieldRef) {
			SootField field = ((FieldRef) value).getField();
			JavaClass clazz = JavaClass.load(field.getDeclaringClass().getName());
			Set<Type> types = clazz.getVarTypes(field.getNumber());
			if (types.size() == 0) {
				types = new HashSet<Type>();
				types.add(field.getType());
			}
			return types;
		} else {
			return new HashSet<Type>(Arrays.asList(value.getType()));
		}
	}

	public void addValueTypes(Value value, Set<Type> types)
	{
		while (value instanceof ArrayRef) {
			ArrayRef array = (ArrayRef) value;
			value = array.getBase();
		}
		
		if (value instanceof Local) {
			Local local = (Local) value;
			addVarTypes(local.getNumber(), local.getName(), types);
		} else if (value instanceof FieldRef) {
			SootField field = ((FieldRef) value).getField();
			JavaClass clazz = JavaClass.load(field.getDeclaringClass().getName());
			clazz.addVarTypes(field.getNumber(), field.getName(), types);
		} else {
			System.out.println("unhandled assignment to " + value.getClass().toString());
		}
	}

	public String getSignature()
	{
		if (signature == null) {
			signature = method.getSignature();
		}
		return signature;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof JavaMethod) {
			JavaMethod m = (JavaMethod) obj;
			if (m.getSignature().equals(getSignature())) {
				for (int i = 0; i < args.size(); i++) {
					if (!equalTypes(args.get(i), m.args.get(i))) {
						return false;
					}
				}
				return true;
			}
		}
		
		return false;
	}

	private static boolean equalTypes(Set<Type> types1, Set<Type> types2)
	{
		if (types1.size() != types2.size()) return false;
		
		for (Type type1 : types1) {
			boolean found = false;
			for (Type type2 : types2) {
				if (type1.getNumber() == type2.getNumber()) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode()
	{
		int hash = getSignature().hashCode();
		for (Set<Type> types : args) {
			for (Type type : types) {
				hash += type.getNumber();
			}
		}
		return hash;
	}

	@Override
	public String toString()
	{
		String vars = super.toString();
		return method.toString() + (vars != "" ? ":" : "") + vars;
	}
}
