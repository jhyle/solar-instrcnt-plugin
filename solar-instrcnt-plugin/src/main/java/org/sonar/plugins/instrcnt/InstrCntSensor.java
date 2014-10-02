package org.sonar.plugins.instrcnt;

import java.io.File;
import java.math.BigInteger;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;

@Properties({
		@Property(key="sonar.plugins.instrcnt.entrypoints", name="Entry points", global = false, project = true, multiValues = true, description = "provide one or more starting points for counting, e.g. com.jhyle.instrcount.InstrCount.main(java.lang.String[])"),
		@Property(key="sonar.plugins.instrcnt.package", name="Package to count", global = false, project = true, multiValues = false, description = "provide the package in which to count the instructions, e.g. com.jhyle.instrcount")
	})
public class InstrCntSensor implements Sensor
{
	private Settings settings;
	
	private String classPath = System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar" + File.pathSeparator
			+ System.getProperty("java.home") + File.separator + "lib" + File.separator + "jce.jar" + File.pathSeparator
			+ System.getProperty("java.class.path");

	public InstrCntSensor(Settings settings)
	{
		this.settings = settings;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
	
	public void describe(SensorDescriptor descriptor)
	{
		descriptor.name("InstructionCountSensor").provides(InstrCntMetrics.NORI).workOnLanguages("java");
	}

	public void execute(SensorContext context) 
	{
		String pckg = settings.getString("sonar.plugins.instrcnt.package");
		if (StringUtils.isEmpty(pckg)) return;
		
		String cp = this.classPath;
		for (String path : settings.getStringArray("sonar.libraries")) {
			cp += File.pathSeparator + path;
		}
		for (String path : settings.getStringArray("sonar.binaries")) {
			cp += File.pathSeparator + path;
		}

		BigInteger nori = BigInteger.ZERO;
		for (String entry : settings.getStringArray("sonar.plugins.instrcnt.entrypoints")) {
			MethodSignature entrypoint = new MethodSignature(entry);
			JavaProgram program = new JavaProgram(cp, entrypoint);
			nori = nori.add(program.count(pckg));
		}

		context.addMeasure(context.measureBuilder().forMetric(InstrCntMetrics.NORI).onProject().withValue(nori.doubleValue()).build());
	}
}
