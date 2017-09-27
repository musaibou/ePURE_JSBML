package jp.riken.yshimizu.ePURE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.sbml.jsbml.validator.SyntaxChecker;

public class ePURE_JSBML_Main {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	//default setting
	private String output_directory = ePURE_Header.default_output_directory;
	private String conf_file = ePURE_Header.default_conf_file;
	private String project_name = null;
	private String seq = null;
	private String initial_values_csv = ePURE_Header.default_initial_values_file;
	private String parameters_csv = ePURE_Header.default_parameters_file;
	
	/*---------------------------------------------------------
	 * main method
	---------------------------------------------------------*/
	
	public static void main(String[] args){
		
		Chronograph cg = new Chronograph();
		cg.start();
		
		new ePURE_JSBML_Main().start(args);
		
		cg.stop();
		System.out.println("It took " + cg.get_elapsed_time_s() + " s");
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void start(String[] args) {
		
		//command line
		parse_command_line(args);
		
		//summarize the project
		ePURE_Project epure = new ePURE_Project(project_name, seq, output_directory, conf_file, initial_values_csv, parameters_csv);
		
		//outputs zip file from input sequences
		new SBML_Generator(epure).execute();
		
		//outputs zip file for simulation
		new Simulation_Files_Generator(epure).execute();
		
		System.out.println("Completed!");
		System.out.println();
		
	}
	
	private void parse_command_line(String[] args){
		
		Options opts = new Options();
		
		opts.addOption("n", "name", true, "project name (required)");
		opts.addOption("f", "seqfile", true, "nuleotide sequence file(RNA or DNA; either -f or -s is neccessary)");
		opts.addOption("s", "sequence", true, "nucleotide sequence (RNA or DNA; (either -f or -s is neccessary)");
		opts.addOption("o", "outputdir", true, "output directory (optional; default is " + ePURE_Header.default_output_directory + ")");
		opts.addOption("c", "conffile", true, "conf file (optional; default is \"" + ePURE_Header.default_conf_file + "\")");
		opts.addOption("i", "inivaluescsv", true, "CSV file name for initial values (optional; default is \"" + ePURE_Header.default_initial_values_file + "\")");
		opts.addOption("p", "parameterscsv", true, "CSV file name for parameters (optional; default is \"" + ePURE_Header.default_parameters_file + "\")");
		
		DefaultParser parser = new DefaultParser();
		CommandLine cl;
		HelpFormatter help = new HelpFormatter();
		
		BufferedReader reader = null;
		String str;
		StringBuilder sb = new StringBuilder();
		
		try{
			cl = parser.parse(opts, args);
			
			//-n option
			project_name = cl.getOptionValue("n");
			if(project_name == null){
				throw new ParseException("Project name is missing.");
			}else if(SyntaxChecker.isValidId(project_name, ePURE_Header.SBML_level, ePURE_Header.SBML_version)==false){
				throw new ParseException("Invalid project name.");
			}else{
				System.out.println("Project name: " + project_name);
			}
			
			//-f option
			String file_name = cl.getOptionValue("f");
			if(file_name != null){
				try{
					reader = new BufferedReader(new FileReader(file_name));
					while(true){
						str = reader.readLine();
						if(str==null){
							break;
						}
						//replace numbers and spaces
						str = str.replace(" ", "");
						str = str.replaceAll("[0-9]", "");
						
						sb.append(str);
					}
				} catch (FileNotFoundException e) {
					System.out.println("Could not find the sequence file.");
					e.printStackTrace();
					System.exit(0);
				} catch (IOException e) {
					System.out.println("Disk I/O error related to the sequence file.");
					e.printStackTrace();
					System.exit(0);
				} finally {
					try {
						if (reader != null) {
							reader.close();
						}
					} catch (IOException e) {
						System.out.println("Disk I/O error related to the sequence file.");
						e.printStackTrace();
						System.exit(0);
					}
				}
				seq = sb.toString();
			}
			
			//-s option, -s option has priority over -f option
			if(cl.getOptionValue("s")!=null){
				seq = cl.getOptionValue("s");
			}
			
			//confirm sequence is null or not
			if(seq == null){
				throw new ParseException("Nucleotide sequence is missing.");
			}
			
			//-o option (optional, default is current directory)
			if(cl.getOptionValue("o")!=null){
				output_directory = cl.getOptionValue("o");
				System.out.println("Output directory: " + output_directory);
			}else{
				System.out.println("Output directory: Use default (" + ePURE_Header.default_output_directory + ")");
			}
			
			//-c option (optional, default is "./BaseFile/default_ePURE.conf"
			if(cl.getOptionValue("c")!=null){
				conf_file = cl.getOptionValue("c");
				System.out.println("Conf file: " + conf_file);
			}else{
				System.out.println("Conf file: Use default (" + ePURE_Header.default_conf_file +")");
			}
			
			//-i option (optional, default is "./BaseFile/default_initial_values.csv"
			if(cl.getOptionValue("i")!=null){
				initial_values_csv = cl.getOptionValue("i");
				System.out.println("Initial values csv file: " + initial_values_csv);
			}else{
				System.out.println("Initial values csv file: Use default (" + ePURE_Header.default_initial_values_file + ")");
			}
			
			//-p option (optional, default is "./BaseFile/default_parameters.csv"
			if(cl.getOptionValue("p")!=null){
				parameters_csv = cl.getOptionValue("p");
				System.out.println("Parameters csv file: " + parameters_csv);
			}else{
				System.out.println("Parameters csv file: Use default (" + ePURE_Header.default_parameters_file + ")");
			}
			
		}catch(ParseException e){
			System.out.println(e.getMessage());
			help.printHelp("ePURE_JSBML", opts);
			System.exit(0);
		}
		
	}
	
}