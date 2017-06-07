package org.panda.cancernetwork;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapping from altered genes to their sets of gene features (alterations).
 *
 * @author Ozgun Babur
 */
public class GeneAlterationSet
{
	/**
	 * Map from genes to the set of alterations.
	 */
	Map<String, Set<GeneFeature>> altered;

	public GeneAlterationSet()
	{
		altered = new HashMap<>();
	}

	/**
	 * Adds a new gene alteration.
	 * @param gene the altered gene
	 * @param feature the alteration
	 */
	public void addGeneAlteration(String gene, GeneFeature feature)
	{
		if (!altered.containsKey(gene)) altered.put(gene, new HashSet<>());
		altered.get(gene).add(feature);
	}

	/**
	 * Alternative way to add a new gene alteration is to send parameters of the GeneFeature instead of the object
	 * itself.
	 * @param gene the altered gene
	 * @param letter the letter displayed for alteration
	 * @param tooltip the tooltip text for the alteration
	 * @param bgColor background color for alteration
	 * @param borderColor border color for alteration
	 */
	public void addGeneAlteration(String gene, String letter, String tooltip, String bgColor, String borderColor)
	{
		this.addGeneAlteration(gene, new GeneFeature(letter, tooltip, bgColor, borderColor));
	}

	/**
	 * Gets the alterations for a gene.
	 * @param gene the gene of interest
	 * @return gene alterations
	 */
	public Set<GeneFeature> getGeneFeatures(String gene)
	{
		return altered.get(gene);
	}

	/**
	 * Gets the set of altered genes.
	 *
	 * @return altered genes
	 */
	public Set<String> getGenes()
	{
		return altered.keySet();
	}
}
