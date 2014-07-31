package org.sonar.plugins.instrcnt;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

@Properties(
	    @Property(key="sonar.plugins.instrcnt.entrypoints", name="Entry points", global = false, project = true)
	)
public class InstrCntSensor implements Sensor
{
	private Settings settings;
	
	public InstrCntSensor(Settings settings)
	{
		this.settings = settings;
	}
	
	public boolean shouldExecuteOnProject(Project project)
	{
		return true;
	}

	public void analyse(Project project, SensorContext context)
	{
		for (String entry : settings.getStringArray("sonar.plugins.instrcnt.entrypoints")) {
			MethodSignature sig = new MethodSignature(entry);
			context.saveMeasure(InstrCntMetrics.NORI, 123.0);
		}
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}
