package org.panda.cancernetwork;

import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.panda.resource.network.PathwayCommons;
import org.panda.utility.graph.Graph;

import java.util.*;

/**
 * Loads Pathway Commons SIF.
 *
 * @author Ozgun Babur
 */
public class NetworkLoader
{
	Map<SIFEnum, Integer> types;

	public NetworkLoader(Map<SIFEnum, Integer> types)
	{
		this.types = types;
	}

	public List<Map<String, Graph>> load()
	{
		List<Map<String, Graph>> list = new ArrayList<>();

		int max = types.values().stream().max(Integer::compare).get();

		for (int i = 0; i <= max; i++)
		{
			int index = i;
			Map<String, Graph> map = new HashMap<>();
			list.add(map);

			types.keySet().stream().filter(type -> types.get(type) == index).forEach(type ->
				map.put(type.getTag(), PathwayCommons.get().getGraph(type)));
		}


		return list;
	}
}
