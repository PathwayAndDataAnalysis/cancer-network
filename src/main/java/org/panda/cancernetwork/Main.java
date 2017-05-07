package org.panda.cancernetwork;

import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.panda.utility.FileUtil;
import org.panda.utility.graph.DirectedGraph;
import org.panda.utility.graph.Graph;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ozgun Babur
 */
public class Main
{
	public static final String PARAMETERS_FILENAME = "parameters.txt";
	public static final String DEFAULT_OUTPUT_FILENAME = "network";

	String directory;

	Map<SIFEnum, Integer> sifTypes;
	GeneAlterationSet gas;
	Set<String> cancerGenes;
	Map<String, String> tooltips;
	String outputFile;

	public Main(String directory)
	{
		this.directory = directory;
		sifTypes = new HashMap<>();
		gas = new GeneAlterationSet();
		cancerGenes = new HashSet<>();
	}

	public void generateNetwork() throws IOException
	{
		readParameters(directory);

		NetworkLoader nl = new NetworkLoader(sifTypes);
		List<Map<String, Graph>> graphs = nl.load();

		Set<String> genes = gas.getGenes();

		genes.stream().filter(cancerGenes::contains).forEach(System.out::println);

		Set<String> genesInGraph = new HashSet<>();
		Set<String> edges = new HashSet<>();


		findEdges(genes, cancerGenes, graphs, genesInGraph, edges);

		String sifNoExt = outputFile == null ? DEFAULT_OUTPUT_FILENAME : outputFile;
		sifNoExt = directory + File.separator + sifNoExt;

		BufferedWriter sifWriter = Files.newBufferedWriter(Paths.get(sifNoExt + ".sif"));
		BufferedWriter fmtWriter = Files.newBufferedWriter(Paths.get(sifNoExt + ".format"));

		fmtWriter.write("node\tall-nodes\tcolor\t255 255 255");
		fmtWriter.write("\tnode\tall-nodes\tbordercolor\t0 0 0");

		for (String edge : edges)
		{
			sifWriter.write(edge + "\n");
		}

		genes.stream()./*filter(cancerGenes::containsKey).*/forEach(g ->
		{
			FileUtil.writeln(g, sifWriter);
			genesInGraph.add(g);
		});

		for (String gene : genesInGraph)
		{
			if (genes.contains(gene))
			{
				if (cancerGenes.contains(gene))
				{
					FileUtil.lnwrite("node\t" + gene + "\tbordercolor\t200 0 0", fmtWriter);
					FileUtil.lnwrite("node\t" + gene + "\tborderwidth\t2", fmtWriter);
				}

				for (GeneFeature geneFeature : gas.getGeneFeatures(gene))
				{
					FileUtil.lnwrite("node\t" + gene + "\trppasite\t" + geneFeature, fmtWriter);
				}
			}
			if (cancerGenes.contains(gene))
			{
				Color bgcolor = new Color(200, 255, 180);

				FileUtil.lnwrite("node\t" + gene + "\tcolor\t" + bgcolor.getRed() + " " + bgcolor.getGreen() + " " +
					bgcolor.getBlue(), fmtWriter);
			}
			if (tooltips != null && tooltips.containsKey(gene))
			{
				FileUtil.lnwrite("node\t" + gene + "\ttooltip\t" + tooltips.get(gene), fmtWriter);
			}
		}

		sifWriter.close();
		fmtWriter.close();
	}

	private void findEdges(Set<String> set1, Set<String> set2, List<Map<String, Graph>> graphsList,
		Set<String> genesInGraph, Set<String> edges)
	{
		Set<String> avoid = new HashSet<>();
		for (Map<String, Graph> graphMap : graphsList)
		{
			findEdges(set1, set2, graphMap, genesInGraph, edges, avoid);
		}
	}

	private void findEdges(Set<String> set1, Set<String> set2, Map<String, Graph> graphs,
		Set<String> genesInGraph, Set<String> edges, Set<String> avoid)
	{
		Set<String> newAvoids = new HashSet<>();

		for (String gene1 : set1)
		{
			for (String type : graphs.keySet())
			{
				Graph graph = graphs.get(type);

				if (graph.isDirected())
				{
					for (String gene2 : ((DirectedGraph) graph).getDownstream(gene1))
					{
						if (set2.contains(gene2))
						{
							generateDirectedEdge(gene1, gene2, type, graph, edges, genesInGraph, avoid, newAvoids);
						}
					}
					for (String gene2 : ((DirectedGraph) graph).getUpstream(gene1))
					{
						if (set2.contains(gene2))
						{
							generateDirectedEdge(gene2, gene1, type, graph, edges, genesInGraph, avoid, newAvoids);
						}
					}
				}
				else
				{
					for (String gene2 : graph.getNeighbors(gene1))
					{
						if (set2.contains(gene2))
						{
							String key1 = gene1 + " " + gene2;
							String key2 = gene2 + " " + gene1;
							if (!avoid.contains(key1) && !avoid.contains(key2))
							{
								String g1 = gene1;
								String g2 = gene2;
								if (g2.compareTo(g1) < 0)
								{
									String temp = g1;
									g1 = g2;
									g2 = temp;
								}

								edges.add(g1 + "\t" + type + "\t" + g2 + "\t" + graph.getMediatorsInString(g1, g2));
								genesInGraph.add(gene1);
								genesInGraph.add(gene2);

								newAvoids.add(key1);
								newAvoids.add(key2);
							}
						}
					}
				}
			}
		}
		avoid.addAll(newAvoids);
	}

	private void generateDirectedEdge(String gene1, String gene2, String type, Graph graph, Set<String> edges, Set<String> genesInGraph, Set<String> avoid, Set<String> newAvoids)
	{
		String key = gene1 + " " + gene2;
		if (!avoid.contains(key))
		{
			edges.add(gene1 + "\t" + type + "\t" + gene2 + "\t" + graph.getMediatorsInString(gene1, gene2));
			genesInGraph.add(gene1);
			genesInGraph.add(gene2);
			newAvoids.add(key);
		}
	}

	void readParameters(String dir) throws IOException
	{
		Files.lines(Paths.get(dir + File.separator + PARAMETERS_FILENAME)).
			filter(l -> !l.startsWith("#")).map(l -> l.split("=")).
			forEach(t ->
			{
				Parameter param = Parameter.findEnum(t[0].trim());
				if (param != null)
				{
					try
					{
						param.reader.read(t[1].trim(), this);
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
				else
				{
					System.err.println("Unknown parameter = " + t[0].trim());
				}
			});
	}

	enum Parameter
	{
		USE_RELATION_TYPE((value, main) ->
		{
			String[] t = value.split(" ");
			SIFEnum sifEnum = SIFEnum.typeOf(t[0]);
			if (sifEnum != null)
			{
				main.sifTypes.put(sifEnum, Integer.valueOf(t[1]));
			}
		}),

		CANCER_GENE_RESOURCE((value, main) ->
			main.cancerGenes.addAll(CancerGeneResource.getCancerGenes(value, main.directory))),

		GENE_ALTERATION_SET((value, main) -> AlterationReader.loadAlterations(value, main.gas, main.directory)),

		OUTPUT_FILE((value, main) ->
		{
			if (value.endsWith(".sif")) value = value.substring(0, value.length() - 4);
			main.outputFile = value;
		}),

		TOOLTIPS((value, main) -> main.tooltips = Files.lines(Paths.get(main.directory + File.separator + value)).map(l -> l.split("\t"))
			.collect(Collectors.toMap(t -> t[0], t -> t[1])))
		;

		ParameterReader reader;

		Parameter(ParameterReader reader)
		{
			this.reader = reader;
		}

		String getText()
		{
			return toString().toLowerCase().replaceAll("_", "-");
		}

		static Parameter findEnum(String text)
		{
			for (Parameter parameter : values())
			{
				if (parameter.getText().equals(text)) return parameter;
			}
			return null;
		}
	}

	interface ParameterReader
	{
		void read(String value, Main main) throws IOException;
	}

	public static void main(String[] args) throws IOException
	{
		Main main = new Main(args[0]);
		main.generateNetwork();
	}
}
