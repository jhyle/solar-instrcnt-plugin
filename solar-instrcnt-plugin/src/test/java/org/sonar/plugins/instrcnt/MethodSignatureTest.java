package org.sonar.plugins.instrcnt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sonar.plugins.instrcnt.MethodSignature;

import soot.ArrayType;
import soot.DoubleType;
import soot.IntType;
import soot.RefType;

public class MethodSignatureTest
{
	@Test
	public void testMain()
	{
		MethodSignature sig = new MethodSignature("com.jhyle.test.Main.main(java.lang.String[])");
		assertEquals("com.jhyle.test.Main", sig.getClassName());
		assertEquals("main", sig.getMethodName());
		assertEquals(1, sig.getParams().size());
		assertEquals(ArrayType.v(RefType.v("java.lang.String"), 1), sig.getParams().get(0));
	}

	@Test
	public void testComplicated()
	{
		MethodSignature sig = new MethodSignature("com.jhyle.test.Class._calc(int i, java.lang.String[][], java.lang.Double, double[])");
		assertEquals("com.jhyle.test.Class", sig.getClassName());
		assertEquals("_calc", sig.getMethodName());
		assertEquals(4, sig.getParams().size());
		assertEquals(IntType.v(), sig.getParams().get(0));
		assertEquals(ArrayType.v(RefType.v("java.lang.String"), 2), sig.getParams().get(1));
		assertEquals(RefType.v("java.lang.Double"), sig.getParams().get(2));
		assertEquals(ArrayType.v(DoubleType.v(), 1), sig.getParams().get(3));
	}
}
