package org.panda.cancernetwork;

import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.panda.resource.network.PathwayCommons;
import org.panda.utility.graph.Graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class NetworkLoader
{
	Set<SIFEnum> types;

	public NetworkLoader(Set<SIFEnum> types)
	{
		this.types = types;
	}

	public Map<String, Graph> load()
	{
		Map<String, Graph> map = new HashMap<>();

		for (SIFEnum type : types)
		{
			map.put(type.getTag(), PathwayCommons.get().getGraph(type));
		}

		return map;
	}
}
