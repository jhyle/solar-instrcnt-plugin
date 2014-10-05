package org.sonar.plugins.instrcnt;

import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Scene;
import soot.Type;
import soot.options.Options;

import com.google.common.collect.ImmutableSet;

class JavaProgram
{
	private Invocation start;
	
	public JavaProgram(String classpath, MethodSignature entrypoint)
	{
		Options.v().set_via_shimple(true);
		Options.v().set_whole_program(true);
		Options.v().set_output_format(Options.output_format_shimple);
		Scene.v().setSootClassPath(classpath);
		Scene.v().loadClassAndSupport(entrypoint.getClassName());
		Scene.v().loadNecessaryClasses();
		
		List<Set<Type>> args = new ArrayList<Set<Type>>();
		for (Type type : entrypoint.getParams()) {
			args.add(ImmutableSet.of(type));
		}

		start = Invocation.load(entrypoint.getClassName(), entrypoint.getMethodName(), entrypoint.getParams(), args);
		if (start == null) {
			throw new InvalidParameterException("Could not find " + entrypoint.toString() + "!");
		}
	}
	
	public Collection<JavaClass> classes()
	{
		return JavaClass.loaded();
	}

	public Map<JavaMethod, Invocation> methods()
	{
		return Invocation.evaluatedMethods();
	}
	
	public BigInteger count(String pckg2count)
	{
		start.analyze(pckg2count);
		return start.getInstructions();
	}

}
