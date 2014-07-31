package org.sonar.plugins.instrcnt;

import java.util.List;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.google.common.collect.ImmutableList;

public class InstrCntMetrics implements Metrics
{
	public static final Metric NORI = new Metric.Builder("reachable_instructions", "NORI", Metric.ValueType.INT)
		.setDescription("Number of reachable instructions via the flow graph starting from an entry point.")
		.setDomain(CoreMetrics.DOMAIN_SIZE)
		.setQualitative(false)
		.create();

	public List<Metric> getMetrics()
	{
		return ImmutableList.of(NORI);
	}
}
