package org.sonar.plugins.instrcnt;

import java.util.List;

import org.sonar.api.SonarPlugin;

import com.google.common.collect.ImmutableList;

public class InstrCntPlugin extends SonarPlugin
{
	public List<?> getExtensions()
	{
		return ImmutableList.of(InstrCntSensor.class);
	}
}
