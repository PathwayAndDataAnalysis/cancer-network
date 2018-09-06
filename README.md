# Cancer-Network

## Overview: 

Cancer-Network is a tool that contextualizes a cancer patient's gene alterations within a larger body of known oncogenes. It generates a network to visualize these relations using a modified [BioPAX](http://www.biopax.org/) syntax.  Navigate [here](http://www.pathwaycommons.org/pc2/formats#sif_relations) to understand the mapping between a BioPAX structure and its respective modification.

### Cancer-Network accepts DNAseq results from the following sources: 

•	[Mutect](https://software.broadinstitute.org/cancer/cga/mutect) (versions 1 and 2)

•	[CNVkit](https://cnvkit.readthedocs.io/en/stable/)

•	[GeneTrails](https://knightdxlabs.ohsu.edu/)

### Current oncogene repositories queried to generate network:

•	[OncoKB](http://oncokb.org/#/)

•	[COSMIC](https://cancer.sanger.ac.uk/cosmic)

•	Bushman

•	[Mutex](https://github.com/pathwayanddataanalysis/mutex)

## Installation:

Before you begin, ensure that both Maven and Java 8 (JDK is needed to build the code. Once you have the jar, you can just have JRE to run it) are installed on your computer.

Clone the Cancer-Network repository to your local machine:
	
	git clone git@github.com:PathwayAndDataAnalysis/cancer-network.git

Navigate into the directory of the repository you just created:

	cd cancer-network

Clean and compile the code:

	mvn clean compile assembly:single
	
## Input:

Below is an example directory with gene alteration data for a mock "Patient 100" (we will use this example throughout the document):

	~/patient-100-gene-alterations
		|_cfDNA-CNA.csv
		|_cfDNA-mutect.csv
		|_cancer-genes.txt
		|_parameters.txt

The first two files in this directory, "cfDNA-CNA.csv" and "cfDNA-mutect.csv", are the gene alteration files for Patient 100. "parameters.txt" is a mandatory, user-provided file (the file name should not change). It is a series of key-value pairs that provides crucial information to the tool. 

### Keys in parameters.txt:

(*) tool allows multiple key-value pairs of this type
(**) mandatory key-value pair and multiple key-value pairs allowed

1. 	use-relation-type *: when searching databases for related oncogenes, this option tells Cancer-Network to limit its query to a specific type of relationship between the patient’s alterations and the databases’ hits.
				 	
These include:

controls-state-change-of
	controls-transport-of
	controls-phosphorylation-of
	controls-expression-of
	catalysis-precedes
	in-complex-with
	interacts-with	
	neighbor-of	
	consumption-controlled-by
	controls-production-of	
 	controls-transport-of-chemical
	chemical-affects	
	reacts-with	
	used-to-produce	

Refer to descriptions of these relationships [here](http://www.pathwaycommons.org/pc2/formats#sif_relations).

Each user-relation-type key-value pair also has a priority level associated with it. If a gene alteration has more than one kind of relationship with a specific oncogene, only the relationship of higher priority will appear in the network.  This acts to eliminate unnecessary noise/redundancies in the network. 

These priority numbers should be used sequentially (with 0 being the highest priority, 1 the second highest, et cetera). 
						
				

2. 	cancer-gene-resource*: name of a .txt file with new-line-delimited gene names, if the user wishes to supplement the default databases with any additional gene repositories. 
				
Example cancer-gene-resource file for Patient 100:
				
ABL1
ALOX5
	AR 

	


3. 	tooltips*: a .txt file of gene names followed by a list of tooltips (each gene name/list pairing is delimited by a new line)

Example tooltips file for Patient 100:

	ABL1	[Ponatinib, Imatinib, Dasatinib]
	ALOX5	[Celecoxib]
	AR	[Enzalutamide]

Note: the "tooltips" file and the "cancer-gene-resource" file can be one in the same.

     

4. 	gene-alteration-set**: name of the Mutect/CNVKit/Genetrails results file (extension will differ depending on type). 

The value for this key should include the type of analysis (see list of supported analyses below):

Current options for type of analysis:
				
CNVkit*
Mutect
Mutect2
GeneTrailsMutations
GeneTrailsCNV*

*requires 2 additional parameters:
	
1.	log2 throughput (type double)

2. saturation level (type double)


					
5. 	output-file: the desired title of the generated network sif and format files. By default, the file name is "network.txt". 
	

A complete example of a parameters.txt file:

	use-relation-type = controls-state-change-of 0
	use-relation-type = in-complex-with 1
	cancer-gene-resource = Custom cancer-genes-to-drugs.txt
	tooltips = cancer-genes-to-drugs.txt
	gene-alteration-set = Mutect cfDNA-mutect.csv
	gene-alteration-set = CNVkit cfDNA-CNA.csv 1 2
	output-file = patient-100-cancer-network

Command line prompt to execute job on Patient 100's data (from cancer-network directory):

java -jar target/cancer-network.jar ~/patient-100-gene-alterations


## Output:

Patient 100 output directory:

	~/patient-100-gene-alterations
		|_cfDNA-CNA.csv
		|_cfDNA-mutect.csv
		|_patient-100-cancer-network.format
		|_patient-100-cancer-network.sif
		|_parameters.txt
				
To view your newly generated sif file, please use ChiBE: Chisio BioPAX Editor, a free editing and visualization tool for pathway models.

Instructions for ChiBE installation & usage [here](https://github.com/PathwayCommons/chibe).


