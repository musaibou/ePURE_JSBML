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
	String output_directory = ePURE_Header.default_output_directory;
	String conf_file = ePURE_Header.default_conf_file;
	String project_name = null;
	String seq = null;
	
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
	 * public method
	---------------------------------------------------------*/
	
	public void start(String[] args) {
		
		//command line
		parse_command_line(args);
		
		//summarize the project
		ePURE_Project_Summary summary = new ePURE_Project_Summary(project_name, seq, output_directory, conf_file);
		
		//outputs zip file from input sequences
		//new SBML_Generator(summary).execute();
		
		//outputs zip file for simulation
		//new Simulation_Files_Generator(summary).execute();
		
		System.out.println("completed!");
		System.out.println();
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void parse_command_line(String[] args){
		
		Options opts = new Options();
		
		opts.addOption("n", "name", true, "project name (required)");
		opts.addOption("f", "seqfile", true, "nuleotide sequence file(RNA or DNA; either -f or -s is neccessary)");
		opts.addOption("s", "sequence", true, "nucleotide sequence (RNA or DNA; (either -f or -s is neccessary)");
		opts.addOption("o", "outputdir", true, "output directory (optional; default is " + ePURE_Header.default_output_directory + ")");
		opts.addOption("c", "conffile", true, "conf file (optional; default is \"" + ePURE_Header.default_conf_file + "\")");
		
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
				throw new ParseException("project name is missing.");
			}else if(SyntaxChecker.isValidId(project_name, ePURE_Header.SBML_level, ePURE_Header.SBML_version)==false){
				throw new ParseException("Invalid project name.");
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
				} catch (IOException e) {
					System.out.println("Disk I/O error.");
				} finally {
					try {
						if (reader != null) {
							reader.close();
						}
					} catch (IOException e) {
						System.out.println("Disk I/O error.");
					}
				}
				seq = sb.toString();
			}
			
			//-s option
			if(cl.getOptionValue("s")!=null){
				seq = cl.getOptionValue("s");
			}
			
			//confirm sequence is null or not
			if(seq == null){
				throw new ParseException("nucleotide sequence is missing.");
			}
			
			//-o option (optional, default is current directory)
			if(cl.getOptionValue("o")!=null){
				output_directory = cl.getOptionValue("o");
			}else{
				System.out.println("Using default output directory (" + ePURE_Header.default_output_directory + ")");
			}
			//-c option (optional, default is "./BaseFile/default_ePURE.conf"
			if(cl.getOptionValue("c")!=null){
				conf_file = cl.getOptionValue("c");
			}else{
				System.out.println("Using default conf file (" + ePURE_Header.default_conf_file +")");
			}
			
		}catch(ParseException e){
			System.out.println(e.getMessage());
			help.printHelp("ePURE_JSBML", opts);
			System.exit(0);
		}
		
	}
	
}