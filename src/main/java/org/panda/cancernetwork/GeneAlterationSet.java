package org.panda.cancernetwork;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class GeneAlterationSet
{
	Map<String, Set<GeneFeature>> altered;

	public GeneAlterationSet()
	{
		altered = new HashMap<>();
	}

	public void addGeneAlteration(String gene, GeneFeature feature)
	{
		if (!altered.containsKey(gene)) altered.put(gene, new HashSet<>());
		altered.get(gene).add(feature);
	}

	public void addGeneAlteration(String gene, String letter, String tooltip, String bgColor, String borderColor)
	{
		this.addGeneAlteration(gene, new GeneFeature(letter, tooltip, bgColor, borderColor));
	}

	public Set<GeneFeature> getGeneFeatures(String gene)
	{
		return altered.get(gene);
	}

	public Set<String> getGenes()
	{
		return altered.keySet();
	}
}
