package org.sonar.plugins.instrcnt;

import java.util.List;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.google.common.collect.ImmutableList;

public class InstrCntMetrics implements Metrics
{
	public static final Metric NOEI = new Metric.Builder("executable_instructions", "Executable instructions", Metric.ValueType.FLOAT)
		.setDescription("Number of executable instructions starting from an entry point.")
		.setDomain(CoreMetrics.DOMAIN_SIZE)
		.setQualitative(false)
		.create();

	public List<Metric> getMetrics()
	{
		return ImmutableList.of(NOEI);
	}
}
