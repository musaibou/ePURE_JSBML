# ePURE_JSBML
ePURE_JSBML generates files to run a protein synthesis simulator in Matlab. The model for the simulator uses ODE and is based on the *E. coli*-based reconstituted in vitro translation system, [PURE system](http://dx.doi.org/10.1038/90802). For details, please visit our [website](https://sites.google.com/view/puresimulator) or read [our published manuscript](http://dx.doi.org/10.1073/pnas.1615351114).
### Download
Please download from "Download ZIP" button and unzip it. You can also use git clone command.
### Requirement
The application requires Java-installed comuputer. We tested with Java8 (1.8.0_121) on Windows 8 or Windows 10.
### How to use
ePURE__JSBML is a comand line-based application. Please start the application with the command  
`java -jar ePURE_JSBML.jar [options...]`  
  
Options shown below are available.  
~~~
-c,--conffile <arg>: conf file (optional; default is "./BaseFile/default_ePURE.conf")
-f,--seqfile <arg>: nuleotide sequence file(RNA or DNA; either -f or -s is neccessary)
 -i,--inivaluescsv <arg>: CSV file name for initial values (optional; default is "./BaseFile/default_initial_values.csv")
 -n,--name <arg>: project name (required)
 -o,--outputdir <arg>: output directory (optional; default is ./)
 -p,--parameterscsv <arg> :CSV file name for parameters (optional; default is "./BaseFile/default_parameters.csv")
 -s,--sequence <arg>: nucleotide sequence (RNA or DNA; (either -f or -s is neccessary)
~~~
