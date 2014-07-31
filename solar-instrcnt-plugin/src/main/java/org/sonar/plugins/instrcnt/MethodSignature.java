package org.sonar.plugins.instrcnt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.PrimType;
import soot.RefType;
import soot.ShortType;
import soot.Type;

class MethodSignature
{
	private String className;
	private String methodName;
	private List<Type> params;
	
	private static Map<String, PrimType> primitiveTypes = new HashMap<String, PrimType>();
	
	static {
		primitiveTypes.put("byte", ByteType.v());
		primitiveTypes.put("short", ShortType.v());
		primitiveTypes.put("int", IntType.v());
		primitiveTypes.put("long", LongType.v());
		primitiveTypes.put("float", FloatType.v());
		primitiveTypes.put("double", DoubleType.v());
		primitiveTypes.put("boolean", BooleanType.v());
		primitiveTypes.put("char", CharType.v());
	}
	
	/**
	 * Extracts the signature informations from a signature string of
	 * the format: package.path.ClassName.methodName(paramType1, paramType2)
	 * 
	 * @param signature
	 */
	public MethodSignature(String signature)
	{
		String[] parsedSignature = Strings.extractMethodSignature(signature);
		this.className = extractClassName(parsedSignature);
		this.methodName = extractMethodName(parsedSignature);
		this.params = extractParamList(parsedSignature);
	}
	
	private String extractClassName(String[] parsedSignature)
	{
		return parsedSignature[0].substring(0, parsedSignature[0].lastIndexOf('.'));
	}

	private String extractMethodName(String[] parsedSignature)
	{
		return parsedSignature[0].substring(parsedSignature[0].lastIndexOf('.') + 1);
	}
	
	private List<Type> extractParamList(String[] parsedSignature)
	{
		List<Type> params = new ArrayList<Type>();
		for (int i = 1; i < parsedSignature.length; i += 2) {
			params.add(sootType(parsedSignature[i]));
		}
		return params;
	}

	private Type sootType(String type)
	{
		Type sootType;

		int arrayLevel = StringUtils.countMatches(type, "[]");
		if (arrayLevel == 0) {
			if (primitiveTypes.containsKey(type)) {
				sootType = primitiveTypes.get(type);
			} else {
				sootType = RefType.v(type);
			}
		} else {
			String baseType = type.substring(0, type.indexOf('['));
			if (primitiveTypes.containsKey(baseType)) {
				sootType = ArrayType.v(primitiveTypes.get(baseType), arrayLevel);
			} else {
				sootType = ArrayType.v(RefType.v(baseType), arrayLevel);
			}
		}
		
		return sootType;
	}
	
	public String getClassName()
	{
		return this.className;
	}
	
	public String getMethodName()
	{
		return this.methodName;
	}
	
	public List<Type> getParams()
	{
		return params;
	}
}
