package jp.riken.yshimizu.ePURE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.TreeMap;

public class Sequence_Independent_SBML_Generator {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private HashMap<String, Boolean> aa_map;
	private HashMap<String, Boolean> tRNA_map;
	
	private String project_name;
	private TreeMap<String, byte[]> byte_stream_map;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Sequence_Independent_SBML_Generator(ePURE_Project epure, TreeMap<String, byte[]> byte_stream_map){
		
		this.project_name = epure.get_project_name();
		this.byte_stream_map = byte_stream_map;
		
		this.aa_map = epure.get_aa_map();
		this.tRNA_map = epure.get_tRNA_map();
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void execute(){
		
		System.out.println("  Making sequence independent files...");
		
		process_for_Aminoacylation_A(ePURE_Header.base_Aminoacylation_A, "Aminoacylation_A_aa_");
		process_for_Aminoacylation_B_or_Elongation_A(ePURE_Header.base_Aminoacylation_B, "Aminoacylation_B_aa_codon_");
		process_for_Aminoacylation_B_or_Elongation_A(ePURE_Header.base_Elongation_A, "Elongation_A_aa_codon_");
		process_for_single_file_type_files(ePURE_Header.base_Elongation_B, "Elongation_B_");
		process_for_single_file_type_files(ePURE_Header.base_EnergyRegeneration_A, "EnergyRegeneration_A_");
		process_for_single_file_type_files(ePURE_Header.base_EnergyRegeneration_B, "EnergyRegeneration_B_");
		process_for_single_file_type_files(ePURE_Header.base_EnergyRegeneration_C, "EnergyRegeneration_C_");
		process_for_single_file_type_files(ePURE_Header.base_EnergyRegeneration_D, "EnergyRegeneration_D_");
		process_for_single_file_type_files(ePURE_Header.base_FMet_tRNASynthesis, "FMet_tRNASynthesis_");
		process_for_single_file_type_files(ePURE_Header.base_Initiation_A, "Initiation_A_");
		process_for_single_file_type_files(ePURE_Header.base_Initiation_B1, "Initiation_B1_");
		process_for_single_file_type_files(ePURE_Header.base_Initiation_B2, "Initiation_B2_");
		process_for_single_file_type_files(ePURE_Header.base_SmallMolecules, "SmallMolecules_");
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void process_for_Aminoacylation_A(String base_file, String output_file){
		
		for(String key:aa_map.keySet()){
			if(aa_map.get(key)==true){
				make_stream_for_Aminoacylation_A(base_file, key, output_file);
			}
		}
		
	}
	
	private void make_stream_for_Aminoacylation_A(String base_file, String aa, String output_file){
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		String str = null;
		output_file = output_file.replace("aa", aa);
		output_file = output_file + project_name + ".xml";
		
		try{
			reader = new BufferedReader(new FileReader(base_file));
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			while(true){
				str = reader.readLine();
				if(str==null){
					break;
				}
				while(str.contains("Aaa")){
					str = str.replace("Aaa", aa);
				}
				while(str.contains("_space_")){
					str = str.replace("_space_", "_");
				}
				while(str.contains("_underscore_")){
					str = str.replace("_underscore_", "");
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
				if(reader != null) {
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
	
	
	private void process_for_Aminoacylation_B_or_Elongation_A(String base_file, String output_file){
		
		for(String key:tRNA_map.keySet()){
			if(tRNA_map.get(key)==true){
				String aa = key.substring(0, key.length()-3);
				make_stream_for_Aminoacylation_B_or_Elongation_A(base_file, key, aa, output_file);
			}
		}
		
	}
	
	private void make_stream_for_Aminoacylation_B_or_Elongation_A(String base_file, String aa_anticodon, String aa, String output_file){
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		String str = null;
		output_file = output_file.replace("aa_codon", aa_anticodon);
		output_file = output_file + project_name + ".xml";
		String Aaa;
		if(aa.length()==4){
			Aaa = aa.substring(1, 4);
		}else{
			Aaa = aa;
		}
		
		try{
			reader = new BufferedReader(new FileReader(base_file));
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			while(true){
				str = reader.readLine();
				if(str==null){
					break;
				}
				while(str.contains("Aaa")){
					str = str.replace("Aaa", Aaa);
				}
				while(str.contains("_space_")){
					str = str.replace("_space_", "_");
				}
				while(str.contains("_underscore_")){
					str = str.replace("_underscore_", "");
				}
				while(str.contains("tRNA_super_Bbb_endsuper__sub_YYY_endsub_")){
					str = str.replace("tRNA_super_Bbb_endsuper__sub_YYY_endsub_", "tRNA" + aa_anticodon);
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
				if(reader != null) {
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
	
	private void process_for_single_file_type_files(String base_file, String output_file){
		
		make_stream_for_single_file_type_files(base_file, output_file);
		
	}
	
	private void make_stream_for_single_file_type_files(String base_file, String output_file){
		
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
				while(str.contains("_underscore_")){
					str = str.replace("_underscore_", "");
				}
				while(str.contains("tRNA_super_fMet_endsuper__sub_CAU_endsub_")){
					str = str.replace("tRNA_super_fMet_endsuper__sub_CAU_endsub_", "tRNAfMetCAU");
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
				if(reader != null) {
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
