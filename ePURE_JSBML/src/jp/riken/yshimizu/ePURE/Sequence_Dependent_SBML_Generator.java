package jp.riken.yshimizu.ePURE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Sequence_Dependent_SBML_Generator {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private String project_name;
	
	private HashMap<String, ArrayList<String>> codon_vs_tRNA_map;
	private ArrayList<String> codon_array;
	
	private TreeMap<String, byte[]> byte_stream_map;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Sequence_Dependent_SBML_Generator(ePURE_Project epure, TreeMap<String, byte[]> byte_stream_map) {
		
		this.project_name = epure.get_project_name();
		this.byte_stream_map = byte_stream_map;
		
		this.codon_vs_tRNA_map = epure.get_codon_vs_tRNA_map();
		this.codon_array = epure.get_codon_array();
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void execute(){
		
		System.out.println("  Making sequence dependent files...");
		
		process_for_Initiation_C(ePURE_Header.base_Initiation_C, "Initiation_C_");
		process_for_Termination_A_or_B(ePURE_Header.base_Termination_A, "Termination_A_AAAABBB_RFx_CCCDDD_");
		process_for_Termination_A_or_B(ePURE_Header.base_Termination_B, "Termination_B_AAAABBB_RFx_CCCDDD_");
		process_for_Termination_C(ePURE_Header.base_Termination_C, "Termination_C_AAAABBB_CCCDDD_");
		process_for_Elongation_Ca1(ePURE_Header.base_Elongation_Ca1, "AAAABBB_Elongation_Ca1_CCCDDD_");
		process_for_Elongation_Ca2(ePURE_Header.base_Elongation_Ca2, "AAAABBB_Elongation_Ca2_CCCDDD_");
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void process_for_Initiation_C(String base_file, String output_file){
		
		make_stream_for_Initiation_C(base_file, output_file);
		
	}
	
	private void make_stream_for_Initiation_C(String base_file, String output_file){
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		String str = null;
		
		output_file = output_file + project_name + ".xml";
		
		try{
			reader = new BufferedReader(new FileReader(base_file));
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			while(true){
				
				str = reader.readLine();
				if(str==null){
					break;
				}
				while(str.contains("_space_")){
					str = str.replace("_space_", "_");
				}
				while(str.contains("_sub_")){
					str = str.replace("_sub_", "");
				}
				while(str.contains("_endsub_")){
					str = str.replace("_endsub_", "");
				}
				while(str.contains("_super_")){
					str = str.replace("_super_", "");
				}
				while(str.contains("_endsuper_")){
					str = str.replace("_endsuper_", "");
				}
				while(str.contains("_underscore_")){
					str = str.replace("_underscore_", "");
				}
				while(str.contains("XXX0002")){
					str = str.replace("XXX0002", codon_array.get(1)+"0002");
				}
				while(str.contains("Pept0001")){
					str = str.replace("Pept0001", "fMet");
				}
				
				writer.write(str);writer.newLine();
			}
			
			writer.flush();
			byte_stream_map.put(output_file, byte_ostream.toByteArray());
			
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the base SBML file: " + base_file);
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Disk I/O error related to the base SBML file: " + base_file);
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if(reader!=null) {
					reader.close();
				}
				if(writer!=null){
					writer.flush();
					writer.close();
				}
				if(byte_ostream!=null){
					byte_ostream.close();
				}
			} catch (IOException e) {
				System.out.println("Disk I/O error related to the base SBML file: " + base_file);
				e.printStackTrace();
				System.exit(0);
			}
		}
		
	}
	
	private void process_for_Termination_A_or_B(String base_file, String output_file){
		
		String pre_codon = codon_array.get(codon_array.size()-2);
		String stop_codon = codon_array.get(codon_array.size()-1);
		
		ArrayList<String> tRNA_array = codon_vs_tRNA_map.get(pre_codon);
		
		for(int i=0;i<tRNA_array.size();i++){
			
			if(stop_codon.equals("UAA")){
				make_stream_for_Termination_A_or_B(base_file, "RF1", codon_array.size(), tRNA_array.get(i), output_file);
				make_stream_for_Termination_A_or_B(base_file, "RF2", codon_array.size(), tRNA_array.get(i), output_file);
			}else if(stop_codon.equals("UAG")){
				make_stream_for_Termination_A_or_B(base_file, "RF1", codon_array.size(), tRNA_array.get(i), output_file);
			}else if(stop_codon.equals("UGA")){
				make_stream_for_Termination_A_or_B(base_file, "RF2", codon_array.size(), tRNA_array.get(i), output_file);
			}
			
		}
		
	}
	
	private void make_stream_for_Termination_A_or_B(String base_file, String RF, int current_position, String tRNA, String output_file){
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		String str = null;
		
		int a = current_position;
		DecimalFormat f = new DecimalFormat("0000");
		String pre_number = f.format(a-1);
		String current_number = f.format(a);
		
		output_file = output_file.replace("AAAA", current_number);
		output_file = output_file.replace("BBB", codon_array.get(current_position-1));
		output_file = output_file.replace("RFx", RF);
		output_file = output_file.replace("CCCDDD", tRNA);
		output_file = output_file + project_name + ".xml";
		
		try{
			reader = new BufferedReader(new FileReader(base_file));
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			while(true){
				
				str = reader.readLine();
				if(str==null){
					break;
				}
				while(str.contains("_space_")){
					str = str.replace("_space_", "_");
				}
				while(str.contains("_sub_")){
					str = str.replace("_sub_", "");
				}
				while(str.contains("_endsub_")){
					str = str.replace("_endsub_", "");
				}
				while(str.contains("_super_")){
					str = str.replace("_super_", "");
				}
				while(str.contains("_endsuper_")){
					str = str.replace("_endsuper_", "");
				}
				while(str.contains("_underscore_")){
					str = str.replace("_underscore_", "");
				}
				while(str.contains("MMMM")){
					str = str.replace("MMMM", pre_number);
				}
				while(str.contains("MMMN")){
					str = str.replace("MMMN", current_number);
				}
				while(str.contains("BbbYYY")){
					str = str.replace("BbbYYY", tRNA);
				}
				while(str.contains("RFN")){
					str = str.replace("RFN", RF);
				}
				while(str.contains("XXY")){
					str = str.replace("XXY", codon_array.get(current_position-1));
				}
				
				writer.write(str);writer.newLine();
			}
			
			writer.flush();
			byte_stream_map.put(output_file, byte_ostream.toByteArray());
			
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the base SBML file: " + base_file);
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Disk I/O error related to the base SBML file: " + base_file);
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if(reader!=null) {
					reader.close();
				}
				if(writer!=null){
					writer.flush();
					writer.close();
				}
				if(byte_ostream!=null){
					byte_ostream.close();
				}
			} catch (IOException e) {
				System.out.println("Disk I/O error related to the base SBML file: " + base_file);
				e.printStackTrace();
				System.exit(0);
			}
		}
		
	}
	
	private void process_for_Termination_C(String base_file, String output_file){
		
		String pre_codon = codon_array.get(codon_array.size()-2);
		
		ArrayList<String> tRNA_array = codon_vs_tRNA_map.get(pre_codon);
		
		for(int i=0;i<tRNA_array.size();i++){
			
			make_stream_for_Termination_C(base_file, codon_array.size(), tRNA_array.get(i), output_file);
			
		}
		
	}
	
	private void make_stream_for_Termination_C(String base_file, int current_position, String tRNA, String output_file){
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		String str = null;
		
		int a = current_position;
		DecimalFormat f = new DecimalFormat("0000");
		String current_number = f.format(a);
		
		output_file = output_file.replace("AAAA", current_number);
		output_file = output_file.replace("BBB", codon_array.get(current_position-1));
		output_file = output_file.replace("CCCDDD", tRNA);
		output_file = output_file + project_name + ".xml";
		
		try{
			reader = new BufferedReader(new FileReader(base_file));
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			while(true){
				
				str = reader.readLine();
				if(str==null){
					break;
				}
				while(str.contains("_space_")){
					str = str.replace("_space_", "_");
				}
				while(str.contains("_sub_")){
					str = str.replace("_sub_", "");
				}
				while(str.contains("_endsub_")){
					str = str.replace("_endsub_", "");
				}
				while(str.contains("_super_")){
					str = str.replace("_super_", "");
				}
				while(str.contains("_endsuper_")){
					str = str.replace("_endsuper_", "");
				}
				while(str.contains("_underscore_")){
					str = str.replace("_underscore_", "");
				}
				while(str.contains("MMMN")){
					str = str.replace("MMMN", current_number);
				}
				while(str.contains("BbbYYY")){
					str = str.replace("BbbYYY", tRNA);
				}
				while(str.contains("XXY")){
					str = str.replace("XXY", codon_array.get(current_position-1));
				}
				
				writer.write(str);writer.newLine();
			}
			
			writer.flush();
			byte_stream_map.put(output_file, byte_ostream.toByteArray());
			
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the base SBML file: " + base_file);
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Disk I/O error related to the base SBML file: " + base_file);
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if(reader!=null) {
					reader.close();
				}
				if(writer!=null){
					writer.flush();
					writer.close();
				}
				if(byte_ostream!=null){
					byte_ostream.close();
				}
			} catch (IOException e) {
				System.out.println("Disk I/O error related to the base SBML file: " + base_file);
				e.printStackTrace();
				System.exit(0);
			}
		}
		
	}
	
	private void process_for_Elongation_Ca1(String base_file, String output_file){
		
		//first elongation step
		
		String pept = "fMet";
		int current_position = 2;
		String tRNA = "fMetCAU";
		
		make_Elongation_Ca1_or_Ca2(base_file, pept, "N/A", current_position, tRNA, output_file);
		
		//second step ~
		
		for(int i=3;i<codon_array.size();i++){
			
			DecimalFormat f = new DecimalFormat("0000");
			pept = "Pept" + f.format(i-1);
			current_position = i;
			
			ArrayList<String> tRNA_array = codon_vs_tRNA_map.get(codon_array.get(i-2));
			
			for(int j=0;j<tRNA_array.size();j++){
				tRNA = tRNA_array.get(j);
				make_Elongation_Ca1_or_Ca2(base_file, pept, "N/A", current_position, tRNA, output_file);
			}
			
		}
		
	}
	
	private void process_for_Elongation_Ca2(String base_file, String output_file){
		
		//first step
		
		String pre_pept = "fMet";
		String post_pept = "Pept0002";
		int current_position = 2;
		String tRNA;
		
		ArrayList<String> tRNA_array = codon_vs_tRNA_map.get(codon_array.get(1));
		
		for(int i=0;i<tRNA_array.size();i++){
			tRNA = tRNA_array.get(i);
			make_Elongation_Ca1_or_Ca2(base_file, pre_pept, post_pept, current_position, tRNA, output_file);
		}
		
		//second step ~
		
		for(int i=3;i<codon_array.size();i++){
			
			DecimalFormat f = new DecimalFormat("0000");
			pre_pept = "Pept" + f.format(i-1);
			post_pept = "Pept" + f.format(i);
			current_position = i;
			
			tRNA_array = codon_vs_tRNA_map.get(codon_array.get(i-1));
			
			for(int j=0;j<tRNA_array.size();j++){
				tRNA = tRNA_array.get(j);
				make_Elongation_Ca1_or_Ca2(base_file, pre_pept, post_pept, current_position, tRNA, output_file);
			}
			
		}
		
	}
	
	private void make_Elongation_Ca1_or_Ca2(String base_file, String pre_pept, String post_pept, int current_position, String tRNA, String output_file){
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		String str = null;
		
		DecimalFormat f = new DecimalFormat("0000");
		String pre_position = f.format(current_position);
		String post_position = f.format(current_position+1);
		
		output_file = output_file.replace("AAAA", pre_position);
		output_file = output_file.replace("BBB", codon_array.get(current_position-1));
		output_file = output_file.replace("CCCDDD", tRNA);
		output_file = output_file + project_name + ".xml";
		
		//mMet, fMet
		String aa = "";
		if(tRNA.startsWith("mMet")||tRNA.startsWith("fMet")){
			aa = "Met";
		}else{
			aa = tRNA.substring(0, 3);
		}
		
		try{
			reader = new BufferedReader(new FileReader(base_file));
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			while(true){
				str = reader.readLine();
				if(str==null){
					break;
				}
				while(str.contains("_space_")){
					str = str.replace("_space_", "_");
				}
				while(str.contains("_sub_")){
					str = str.replace("_sub_", "");
				}
				while(str.contains("_endsub_")){
					str = str.replace("_endsub_", "");
				}
				while(str.contains("_super_")){
					str = str.replace("_super_", "");
				}
				while(str.contains("_endsuper_")){
					str = str.replace("_endsuper_", "");
				}
				while(str.contains("_underscore_")){
					str = str.replace("_underscore_", "");
				}
				while(str.contains("PeptMMML")){
					str = str.replace("PeptMMML", pre_pept);
				}
				while(str.contains("PeptMMMM")){
					str = str.replace("PeptMMMM", post_pept);
				}
				while(str.contains("MMMM")){
					str = str.replace("MMMM", pre_position);
				}
				while(str.contains("MMMN")){
					str = str.replace("MMMN", post_position);
				}
				while(str.contains("BbbYYY")){
					str = str.replace("BbbYYY", tRNA);
				}
				while(str.contains("BbzYYX")){
					str = str.replace("BbzYYX", tRNA);
				}
				while(str.contains("XXX")){
					str = str.replace("XXX", codon_array.get(current_position-1));
				}
				while(str.contains("XXY")){
					str = str.replace("XXY", codon_array.get(current_position));
				}
				while(str.contains("Aaa")){
					str = str.replace("Aaa", aa);
				}
				
				writer.write(str);writer.newLine();
			}
			
			writer.flush();
			byte_stream_map.put(output_file, byte_ostream.toByteArray());
			
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the base SBML file: " + base_file);
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Disk I/O error related to the base SBML file: " + base_file);
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if(reader!=null) {
					reader.close();
				}
				if(writer!=null){
					writer.flush();
					writer.close();
				}
				if(byte_ostream!=null){
					byte_ostream.close();
				}
			} catch (IOException e) {
				System.out.println("Disk I/O error related to the base SBML file: " + base_file);
				e.printStackTrace();
				System.exit(0);
			}
		}
		
	}
	
	
	
}
