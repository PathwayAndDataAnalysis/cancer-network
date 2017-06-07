package org.panda.cancernetwork;

import org.panda.resource.CancerGeneBushman;
import org.panda.resource.CancerGeneCensus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum for cancer genes.
 *
 * @author Ozgun Babur
 */
public enum CancerGeneResource
{
	/**
	 * The OncoKB database.
	 */
	OncoKB(param -> org.panda.resource.OncoKB.get().getAllSymbols()),

	/**
	 * COSMIC Cancer Gene Census.
	 */
	COSMIC(param -> CancerGeneCensus.get().getAllSymbols()),

	/**
	 * Bushman lab list of cancer genes.
	 */
	Bushman(param -> CancerGeneBushman.get().getAllSymbols()),

	/**
	 * Genes in a Mutex result.
	 */
	Mutex(param ->
	{
		Map<String, Double> scoreMap = MutexReader.readBestScoresRecursive(param[1]);
		double thr = Double.valueOf(param[2]);
		return scoreMap.keySet().stream().filter(g -> scoreMap.get(g) <= thr).collect(Collectors.toSet());
	}),

	/**
	 * Custom set of cancer genes given in a file.
	 */
	Custom(param -> Files.lines(Paths.get(param[1])).filter(l -> !l.startsWith("#")).map(l -> l.split("\t")[0])
		.collect(Collectors.toSet()));

	CancerGeneResource(Loader loader)
	{
		this.loader = loader;
	}

	Loader loader;

	/**
	 * A Loader knows where to find cancer genes.
	 */
	interface Loader
	{
		Set<String> load(String... param) throws IOException;
	}

	/**
	 * Gets the set of cancer genes using the parameter value and the current working directory.
	 * @param paramValue parameters cannot contain spaces and treated as multi parameters when they do
	 * @param workingDirectory the base directory for anything
	 * @return set of cancer genes
	 * @throws IOException
	 */
	public static Set<String> getCancerGenes(String paramValue, String workingDirectory) throws IOException
	{
		paramValue = paramValue.trim();
		String[] param = paramValue.split("\\s+");
		CancerGeneResource resource = valueOf(param[0]);

		if (param.length > 1 && !param[1].startsWith("/")) param[1] = workingDirectory + File.separator + param[1];

		return resource.loader.load(param);
	}
}
