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
	/**
	 * Types of relations and their priority.
	 */
	Map<SIFEnum, Integer> types;

	/**
	 * Constructor with types of relations and their priority
	 * @param types types of relations and their priority
	 */
	public NetworkLoader(Map<SIFEnum, Integer> types)
	{
		this.types = types;
	}

	/**
	 * Loads the network.
	 * @return ordered sets of graphs
	 */
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
