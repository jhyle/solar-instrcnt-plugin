package org.sonar.plugins.instrcnt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.BreakpointStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NopStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

class Invocation
{
	private String pck2cnt;
	private JavaMethod method;
	private Set<Type> returnTypes;
	private BigInteger instructions;

	private static List<JavaMethod> methodsInEvaluation = new LinkedList<JavaMethod>(); 
	private static Map<JavaMethod, Invocation> evaluatedMethods = new HashMap<JavaMethod, Invocation>();

	public static Map<JavaMethod, Invocation> evaluatedMethods()
	{
		return evaluatedMethods;
	}
	
	public static Invocation load(String className, String methodName, List<?> params, List<Set<Type>> args)
	{
		JavaClass clazz = JavaClass.load(className);
		JavaMethod method = clazz.getMethod(methodName, params, args);
		
		if (method == null) {
			return null;
		} else if (evaluatedMethods.containsKey(method)) {
			return evaluatedMethods.get(method);
		} else {
			Invocation invocation = new Invocation(method);
			evaluatedMethods.put(method, invocation);
			return invocation;
		}
	}
	
	private Invocation(JavaMethod method)
	{
		this.method = method;
	}
	
	public void analyze(String pck2cnt)
	{
		if (methodsInEvaluation.contains(method)) {
			return;
		}
		
		if (this.pck2cnt != null && this.pck2cnt.equals(pck2cnt)) {
			return;			
		} else {
			this.pck2cnt = pck2cnt;
			this.instructions = BigInteger.ZERO;
			this.returnTypes = new HashSet<Type>();
		}
		
//		if (!method.getSource().hasActiveBody() && !method.getSource().isNative()) {
//			System.out.println(method.getSource().getSignature() + " has no body");
////			System.exit(0);
//		}

		boolean counts = method.getSource().getDeclaringClass().getName().startsWith(pck2cnt);
		
		if (method.getSource().hasActiveBody()) {

			methodsInEvaluation.add(method);
			Body body = method.getSource().getActiveBody();

//			if (counts) {
//				System.out.println(body.toString());
//			}

			for (Unit unit : body.getUnits()) if (unit instanceof Stmt) {

				Stmt stmt = (Stmt) unit;

				if (stmt instanceof InvokeStmt) {
					Invocation invocation = analyzeInvoke(stmt.getInvokeExpr(), pck2cnt);
					if (invocation != null) {
						instructions = instructions.add(invocation.getInstructions());
					}
					
				} else if (stmt instanceof DefinitionStmt) {
					DefinitionStmt defStmt = (DefinitionStmt) stmt;
					
					if (stmt.containsInvokeExpr()) {
						Invocation invocation = analyzeInvoke(stmt.getInvokeExpr(), pck2cnt);
						if (invocation != null) {
							method.addValueTypes(defStmt.getLeftOp(), invocation.getReturnTypes());
							instructions = instructions.add(invocation.getInstructions());
						}
					
					} else {
						Set<Type> types;
	
						if (defStmt.getRightOp() instanceof ParameterRef) {
							ParameterRef param = (ParameterRef) defStmt.getRightOp();
							types = method.getArgTypes(param.getIndex());
						} else if (defStmt.getRightOp() instanceof CaughtExceptionRef) {
							// for some reasons the type of the right op (the exception) given by soot
							// is always java.lang.Throwable; so we have to derive the type from the var
							// on the left that the exception is assigned to
							types = new HashSet<Type>(Arrays.asList(((Local) defStmt.getLeftOp()).getType()));
						} else {
							types = method.getValueTypes(defStmt.getRightOp());
						}
	
						method.addValueTypes(defStmt.getLeftOp(), types);
					}
				}

				if (counts && !(stmt instanceof ReturnStmt) && !(stmt instanceof ReturnVoidStmt) && !(stmt instanceof IdentityStmt) && !(stmt instanceof NopStmt) && !(stmt instanceof BreakpointStmt)) {
					instructions = instructions.add(BigInteger.ONE);
				}
				
				if (stmt instanceof ReturnStmt) {
					Value value = ((ReturnStmt) stmt).getOp();
					returnTypes.addAll(method.getValueTypes(value));
				}
			}

			methodsInEvaluation.remove(method);
		}
	}

	private Invocation analyzeInvoke(InvokeExpr invokeExpr, String pck2cnt)
	{
		Invocation result = null;
		SootMethod sootMethod = invokeExpr.getMethod();
		
		List<Set<Type>> args = new ArrayList<Set<Type>>();
		for (Value arg : invokeExpr.getArgs()) {
			args.add(new HashSet<Type>(method.getValueTypes(arg)));
		}

		if (invokeExpr instanceof InterfaceInvokeExpr || invokeExpr instanceof VirtualInvokeExpr) {
		
			Value var = ((InstanceInvokeExpr) invokeExpr).getBase();
			for (Type type : method.getValueTypes(var)) {
				
				if (type instanceof ArrayType) {
					type = ((ArrayType) type).baseType;
				}

				if (type instanceof RefType) {
					String className = ((RefType) type).getClassName();
					Invocation call = Invocation.load(className, sootMethod.getName(), sootMethod.getParameterTypes(), args);
					if (call != null) {
						call.analyze(pck2cnt);
						if (result == null) {
							result = call;
						} else {
							result.merge(call);
						}
					}
				}
			}
			
		} else {
			String className = sootMethod.getDeclaringClass().getName();
			result = Invocation.load(className, sootMethod.getName(), sootMethod.getParameterTypes(), args);
			if (result != null) {
				result.analyze(pck2cnt);
			}
		}

		return result;
	}

	public Set<Type> getReturnTypes()
	{
		return returnTypes;
	}

	public BigInteger getInstructions()
	{
		return instructions;
	}

	public void merge(Invocation invocation)
	{
		returnTypes.addAll(invocation.getReturnTypes());
		instructions = instructions.add(invocation.getInstructions());
	}
}
