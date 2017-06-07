package org.panda.cancernetwork;

import org.panda.utility.ValToColor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Loaders for gene alteration data files.
 *
 * @author Ozgun Babur
 */
public enum AlterationReader
{
	/**
	 * Loader for CNVkit results.
	 */
	CNVkit((gas, param) ->
	{
		String filename = param[1];
		double thrLog2 = Double.valueOf(param[2]);
		double saturationVal = Double.valueOf(param[3]);

		ValToColor vtc = new ValToColor(new double[]{-saturationVal, 0, saturationVal},
			new Color[]{new Color(100, 100, 255), Color.WHITE, new Color(255, 100, 100)});

		Files.lines(Paths.get(filename)).skip(1).map(l -> l.split("\t"))
			.filter(t -> Math.abs(Double.valueOf(t[4])) >= thrLog2).filter(t -> !t[3].equals("-")).forEach(t ->
		{
			Double log2 = Double.valueOf(t[4]);
			String color = vtc.getColorInString(log2);

			for (String gene : t[3].split(","))
			{
				gas.addGeneAlteration(gene, "c", "log2 = " + log2, color, null);
			}
		});
	}),

	/**
	 * Loader for Mutect results.
	 */
	Mutect((gas, param) -> Files.lines(Paths.get(param[1])).skip(1).map(l -> l.split("\t")).
		forEach(t -> gas.addGeneAlteration(t[5], "m", t[6], GeneFeature.DEFAULT_BACKGROUND_COLOR,
			t[6].startsWith("Splice") ? "255 0 0" : GeneFeature.DEFAULT_BORDER_COLOR))),

	/**
	 * Loader for GeneTrails mutations.
	 */
	GeneTrailsMutations((gas, param) -> Files.lines(Paths.get(param[1])).skip(1).map(l -> l.split("\t")).
		forEach(t -> gas.addGeneAlteration(t[8], "m", t[7], GeneFeature.DEFAULT_BACKGROUND_COLOR,
			t[16].toLowerCase().contains("splice") ? "255 0 0" : GeneFeature.DEFAULT_BORDER_COLOR))),

	/**
	 * Loader for GeneTrails CNV data.
	 */
	GeneTrailsCNV((gas, param) ->
	{
		String filename = param[1];
		double saturationVal = Double.valueOf(param[2]);

		ValToColor vtc = new ValToColor(new double[]{-saturationVal, 0, saturationVal},
			new Color[]{new Color(100, 100, 255), Color.WHITE, new Color(255, 100, 100)});

		Files.lines(Paths.get(filename)).skip(1).map(l -> l.split("\t"))
			.forEach(t ->
			{
				Double log2 = Math.log(Double.valueOf(t[7])) / Math.log(2);
				String color = vtc.getColorInString(log2);

				gas.addGeneAlteration(t[4], "c", "log2 = " + log2, color, null);
			});
	});

	Loader loader;

	AlterationReader(Loader loader)
	{
		this.loader = loader;
	}

	/**
	 * A Loader knows where to find gene alterations.
	 */
	interface Loader
	{
		void load(GeneAlterationSet gas, String... params) throws IOException;
	}

	/**
	 * Populates the given gene alteration set with gene alterations.
	 * @param paramValue parameters in a string
	 * @param gas gene alteration set
	 * @param workingDirectory base directory for files
	 * @throws IOException
	 */
	public static void loadAlterations(String paramValue, GeneAlterationSet gas, String workingDirectory)
		throws IOException
	{
		paramValue = paramValue.trim();
		String[] param = paramValue.split("\\s+");

		if (param.length > 1 && !param[1].startsWith("/"))
		{
			param[1] = workingDirectory + File.separator + param[1];
		}

		AlterationReader reader = valueOf(param[0]);
		reader.loader.load(gas, param);
	}
}
