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
 * @author Ozgun Babur
 */
public enum CancerGeneResource
{
	OncoKB(param -> org.panda.resource.OncoKB.get().getAllSymbols()),
	COSMIC(param -> CancerGeneCensus.get().getAllSymbols()),
	Bushman(param -> CancerGeneBushman.get().getAllSymbols()),
	Mutex(param ->
	{
		Map<String, Double> scoreMap = MutexReader.readBestScoresRecursive(param[1]);
		double thr = Double.valueOf(param[2]);
		return scoreMap.keySet().stream().filter(g -> scoreMap.get(g) <= thr).collect(Collectors.toSet());
	}),
	Custom(param -> Files.lines(Paths.get(param[1])).filter(l -> !l.startsWith("#")).map(l -> l.split("\t")[0])
		.collect(Collectors.toSet()));

	CancerGeneResource(Loader loader)
	{
		this.loader = loader;
	}

	Loader loader;

	interface Loader
	{
		Set<String> load(String... param) throws IOException;
	}

	public static Set<String> getCancerGenes(String paramValue, String workingDirectory) throws IOException
	{
		paramValue = paramValue.trim();
		String[] param = paramValue.split("\\s+");
		CancerGeneResource resource = valueOf(param[0]);

		if (param.length > 1 && !param[1].startsWith("/")) param[1] = workingDirectory + File.separator + param[1];

		return resource.loader.load(param);
	}
}
