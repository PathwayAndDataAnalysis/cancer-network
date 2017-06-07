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
 * Main class for cancer network generation. It reads parameters from a file and generates the desired network.
 *
 * @author Ozgun Babur
 */
public class Main
{
	/**
	 * The name of the parameters file.
	 */
	public static final String PARAMETERS_FILENAME = "parameters.txt";

	/**
	 * The default name of the network file.
	 */
	public static final String DEFAULT_OUTPUT_FILENAME = "network";

	/**
	 * The directory that contains the parameters file.
	 */
	String directory;

	/**
	 * Desired binary relation types and their priority mapping. The priority is used for complexity management. If
	 * there are multiple relation between two genes, only the highest priority set remains, others removed.
	 */
	Map<SIFEnum, Integer> sifTypes;

	/**
	 * Set of gene alterations. They can come from a patient.
	 */
	GeneAlterationSet gas;

	/**
	 * Set of cancer genes.
	 */
	Set<String> cancerGenes;

	/**
	 * Desired tooltip text for each gene. Mapping is from gene to the tooltip text.
	 */
	Map<String, String> tooltips;

	/**
	 * Name of the output file.
	 */
	String outputFile;

	/**
	 * Constructor that sets the working directory and initializes data structures.
	 *
	 * @param directory The directory that contains parameters file and where the output will be generated.
	 */
	public Main(String directory)
	{
		this.directory = directory;
		sifTypes = new HashMap<>();
		gas = new GeneAlterationSet();
		cancerGenes = new HashSet<>();
	}

	/**
	 * The method that does the job.
	 *
	 * @throws IOException
	 */
	public void generateNetwork() throws IOException
	{
		// read the parameters file and set the variables
		readParameters(directory);

		// load the SIF network
		NetworkLoader nl = new NetworkLoader(sifTypes);
		List<Map<String, Graph>> graphs = nl.load();

		// get the set of altered genes from the collection of gene alterations
		Set<String> genes = gas.getGenes();

		//-- DEBUG START
		// print the intersection of altered genes and the cancer genes
		genes.stream().filter(cancerGenes::contains).forEach(System.out::println);
		//-- DEBUG END

		// initialize collections to keep track of graph elements
		Set<String> genesInGraph = new HashSet<>();
		Set<String> edges = new HashSet<>();

		// decide which relations should be on the network
		findEdges(genes, cancerGenes, graphs, genesInGraph, edges);

		// decide the output file name
		String sifNoExt = outputFile == null ? DEFAULT_OUTPUT_FILENAME : outputFile;
		sifNoExt = directory + File.separator + sifNoExt;

		// initialize file writers for the output
		BufferedWriter sifWriter = Files.newBufferedWriter(Paths.get(sifNoExt + ".sif"));
		BufferedWriter fmtWriter = Files.newBufferedWriter(Paths.get(sifNoExt + ".format"));

		// start writing the format file
		fmtWriter.write("node\tall-nodes\tcolor\t255 255 255");
		fmtWriter.write("\tnode\tall-nodes\tbordercolor\t0 0 0");

		// write SIF relations in .sif file
		for (String edge : edges)
		{
			sifWriter.write(edge + "\n");
		}

		// append gene names to the end of sif file for visualizing the altered genes with no pathway relations
		genes.stream()./*filter(cancerGenes::containsKey).*/forEach(g ->
		{
			FileUtil.writeln(g, sifWriter);
			genesInGraph.add(g);
		});

		// write alteration boxes, colors and tooltips of genes into the format file
		for (String gene : genesInGraph)
		{
			if (genes.contains(gene))
			{
				// if the altered it is also a cancer gene, it has a thick black border
				if (cancerGenes.contains(gene))
				{
					FileUtil.lnwrite("node\t" + gene + "\tbordercolor\t0 0 0", fmtWriter);
					FileUtil.lnwrite("node\t" + gene + "\tborderwidth\t2", fmtWriter);
				}

				// write alterations as info boxes
				for (GeneFeature geneFeature : gas.getGeneFeatures(gene))
				{
					FileUtil.lnwrite("node\t" + gene + "\trppasite\t" + geneFeature, fmtWriter);
				}
			}

			// paint cancer genes in green
			if (cancerGenes.contains(gene))
			{
				Color bgcolor = new Color(200, 255, 180);

				FileUtil.lnwrite("node\t" + gene + "\tcolor\t" + bgcolor.getRed() + " " + bgcolor.getGreen() + " " +
					bgcolor.getBlue(), fmtWriter);
			}

			// set tooltips
			if (tooltips != null && tooltips.containsKey(gene))
			{
				FileUtil.lnwrite("node\t" + gene + "\ttooltip\t" + tooltips.get(gene), fmtWriter);
			}
		}

		// close output file streams
		sifWriter.close();
		fmtWriter.close();
	}

	/**
	 * Searches for edges that connect the given two sets of genes.
	 *
	 * @param set1 first set of genes - can be altered genes
	 * @param set2 second set of genes - can be cancer genes
	 * @param graphsList the priority-ordered list of sets of graphs
	 * @param genesInGraph genes in the result graph
	 * @param edges edges in the result graph
	 */
	private void findEdges(Set<String> set1, Set<String> set2, List<Map<String, Graph>> graphsList,
		Set<String> genesInGraph, Set<String> edges)
	{
		// whenever a higher priority relation exists between two genes, the pair is avoided for lower priority
		// relations
		Set<String> avoid = new HashSet<>();

		// for each priority set, search relations iteratively
		for (Map<String, Graph> graphMap : graphsList)
		{
			findEdges(set1, set2, graphMap, genesInGraph, edges, avoid);
		}
	}

	/**
	 *
	 * @param set1 first set of genes - can be altered genes
	 * @param set2 second set of genes - can be cancer genes
	 * @param graphs current set of graph relations to consider
	 * @param genesInGraph genes in the result graph
	 * @param edges edges in the result graph
	 * @param avoid pairs of genes that are already covered with higher priority relations
	 */
	private void findEdges(Set<String> set1, Set<String> set2, Map<String, Graph> graphs,
		Set<String> genesInGraph, Set<String> edges, Set<String> avoid)
	{
		// pairs of genes that covered in this iteration
		Set<String> newAvoids = new HashSet<>();

		// iterate over neighbors of first gene set and see if any of them are in the second set
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
							// generate undirected edge

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

	private void generateDirectedEdge(String gene1, String gene2, String type, Graph graph,
		Set<String> edges, Set<String> genesInGraph, Set<String> avoid, Set<String> newAvoids)
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

	/**
	 * reads the parameters file and configures parameters.
	 * @param dir the directory that contains parameters file
	 * @throws IOException
	 */
	void readParameters(String dir) throws IOException
	{
		Files.lines(Paths.get(dir + File.separator + PARAMETERS_FILENAME)).
			filter(l -> !l.startsWith("#")).map(l -> l.split("=")).
			forEach(t ->
			{
				// the token before "=" has to be one of the values in the Parameters enum
				Parameter param = Parameter.findEnum(t[0].trim());

				if (param != null)
				{
					try
					{
						// the specific Parameter enum knows how to configure the Main class using the parameter value
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

		TOOLTIPS((value, main) -> main.tooltips = Files.lines(Paths.get(main.directory + File.separator + value))
			.map(l -> l.split("\t")).collect(Collectors.toMap(t -> t[0], t -> t[1])))
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
