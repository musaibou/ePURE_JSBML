# ePURE_JSBML
ePURE_JSBML generates files to run a protein synthesis simulator in [Matlab](https://www.mathworks.com/). The model for the simulator uses ODE and is based on the *E. coli*-based reconstituted in vitro translation system, [PURE system](http://dx.doi.org/10.1038/90802). For details, please visit our [website](https://sites.google.com/view/puresimulator) or read [our published manuscript](http://dx.doi.org/10.1073/pnas.1615351114).
## Download
Please download from "Download ZIP" button and unzip it. You can also use git clone command.
## Requirement
The application requires Java-installed comuputer. We tested with Java8 (1.8.0_121) on Windows 8.1 or Windows 10.
## How to use
ePURE__JSBML is a comand line-based application. Two zip files are generated: One is a collection of separated sub-models that can be visualized by [CellDesigner](http://www.celldesigner.org/). Another is a collection of simulation files ready for use in Matlab.  

Please start the application with the command  
`java -jar ePURE_JSBML.jar [options...]`  
Starting the program without any option displays command line information of the application.

#### Command line options  
~~~
-c,--conffile <arg>
    conf file (optional; default is "./BaseFile/default_ePURE.conf")
-f,--seqfile <arg>
    nuleotide sequence file(RNA or DNA; either -f or -s is neccessary)
-i,--inivaluescsv <arg>
    CSV file name for initial values (optional; default is "./BaseFile/default_initial_values.csv")
-n,--name <arg>
    project name (required)
-o,--outputdir <arg>
    output directory (optional; default is ./)
-p,--parameterscsv <arg>
    CSV file name for parameters (optional; default is "./BaseFile/default_parameters.csv")
-s,--sequence <arg>
    nucleotide sequence (RNA or DNA; (either -f or -s is neccessary)
~~~

#### Project name  
Please specify a project name by `-n` option. This option is required for generating simulation files. Since the name is used for the final model name, assigning inappropriate name displays `Invalid project name` error. 

#### Nucleotide sequence  
Nucleotide sequence for the simulation can be provided with a file by `-f` option or directly to the command by `-s` option. At least `-f` or `-s` option is required. If both are provided, `-s` option has priority over `-f` option. Either DNA or RNA sequence is available but they should be started with ATG or AUG and ended with TAG, TGA, TAA, UAG, UGA, or UAA.

#### Output directory  
Output directory can be changed from the current directly to arbitrary place by `-o` option.

#### Conf file  
The conf file specifies three points for generating simulation files. If you want to change the settings, please copy the default conf file in `BaseFile` directory `default_ePURE.conf`, edit settings, and save with a different file name and then use option `-c` when you launch the application.  
  
  - Whether each amino acid is included or not.  
The final model includes only amino acids specified with `1` in the conf file.  
  - Whether each tRNA is included or not.  
The final model includes only tRNAs specified with `1` in the conf file. 41 kinds of tRNAs that are used in *E. coli* cells are used in the simulation. tRNA is expressed as [amino acid][anticodon seq.], *e.g.*, AlaGGC for tRNA<sup>Ala</sup><sub>GGC</sub> or fMetCAU for tRNA<sup>fMet</sup><sub>CAU</sub>  
  - Which codon is read by which tRNA.  
Some codons are read by multiple tRNAs in cells. You can edit this rule by editing the conf file by describing the line as *e.g.* `UUG=LeuUAA|LeuCAA`  

#### Initial values
sddfsdf
