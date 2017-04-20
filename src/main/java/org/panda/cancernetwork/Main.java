package org.panda.cancernetwork;

import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.panda.utility.FileUtil;
import org.panda.utility.graph.DirectedGraph;
import org.panda.utility.graph.Graph;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class Main
{
	public static final String PARAMETERS_FILENAME = "parameters.txt";
	public static final String DEFAULT_OUTPUT_FILENAME = "network";

	String directory;

	Set<SIFEnum> sifTypes;
	GeneAlterationSet gas;
	Set<String> cancerGenes;
	String outputFile;

	public Main(String directory)
	{
		this.directory = directory;
		sifTypes = new HashSet<>();
		gas = new GeneAlterationSet();
		cancerGenes = new HashSet<>();
	}

	public void generateNetwork() throws IOException
	{
		readParameters(directory);

		NetworkLoader nl = new NetworkLoader(sifTypes);
		Map<String, Graph> graphs = nl.load();

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
		}

		sifWriter.close();
		fmtWriter.close();
	}

	private void findEdges(Set<String> set1, Set<String> set2, Map<String, Graph> graphs,
		Set<String> genesInGraph, Set<String> edges)
	{
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
							edges.add(gene1 + "\t" + type + "\t" + gene2 + "\t" + graph.getMediatorsInString(gene1, gene2));
							genesInGraph.add(gene1);
							genesInGraph.add(gene2);
						}
					}
					for (String gene2 : ((DirectedGraph) graph).getUpstream(gene1))
					{
						if (set2.contains(gene2))
						{
							edges.add(gene2 + "\t" + type + "\t" + gene1 + "\t" + graph.getMediatorsInString(gene2, gene1));
							genesInGraph.add(gene1);
							genesInGraph.add(gene2);
						}
					}
				}
				else
				{
					for (String gene2 : graph.getNeighbors(gene1))
					{
						if (set2.contains(gene2))
						{
							String g1 = gene1;
							String g2= gene2;
							if (g2.compareTo(g1) < 0)
							{
								String temp = g1;
								g1 = g2;
								g2 = temp;
							}

							edges.add(g1 + "\t" + type + "\t" + g2 + "\t" + graph.getMediatorsInString(g1, g2));
							genesInGraph.add(gene1);
							genesInGraph.add(gene2);
						}
					}
				}
			}
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
			SIFEnum sifEnum = SIFEnum.typeOf(value);
			if (sifEnum != null)
			{
				main.sifTypes.add(sifEnum);
			}
		}),

		CANCER_GENE_RESOURCE((value, main) ->
			main.cancerGenes.addAll(CancerGeneResource.getCancerGenes(value, main.directory))),

		GENE_ALTERATION_SET((value, main) -> AlterationReader.loadAlterations(value, main.gas, main.directory)),

		OUTPUT_FILE((value, main) ->
		{
			if (value.endsWith(".sif")) value = value.substring(0, value.length() - 4);
			main.outputFile = value;
		})
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
