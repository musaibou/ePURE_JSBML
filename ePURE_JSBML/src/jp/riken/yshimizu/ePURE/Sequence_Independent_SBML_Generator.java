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
	
	public Sequence_Independent_SBML_Generator(ePURE_Project_Summary summary, TreeMap<String, byte[]> byte_stream_map){
		
		this.project_name = summary.get_project_name();
		this.byte_stream_map = byte_stream_map;
		
		this.aa_map = summary.get_aa_map();
		this.tRNA_map = summary.get_tRNA_map();
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void execute(){
		
		System.out.println("making sequence independent files...");
		
		process_for_Aminoacylation_A("./BaseFile/Aminoacylation_A_20121220v02.xml", "Aminoacylation_A_aa_");
		process_for_Aminoacylation_B_or_Elongation_A("./BaseFile/Aminoacylation_B_20121220v03.xml", "Aminoacylation_B_aa_codon_");
		process_for_Aminoacylation_B_or_Elongation_A("./BaseFile/Elongation_A_20121116v04.xml", "Elongation_A_aa_codon_");
		process_for_single_file_type_files("./BaseFile/Elongation_B_20121116v03.xml", "Elongation_B_");
		process_for_single_file_type_files("./BaseFile/EnergyRegeneration_A_20121015v03.xml", "EnergyRegeneration_A_");
		process_for_single_file_type_files("./BaseFile/EnergyRegeneration_B_20121016v05.xml", "EnergyRegeneration_B_");
		process_for_single_file_type_files("./BaseFile/EnergyRegeneration_C_20121016v05.xml", "EnergyRegeneration_C_");
		process_for_single_file_type_files("./BaseFile/EnergyRegeneration_D_20130501v03.xml", "EnergyRegeneration_D_");
		process_for_single_file_type_files("./BaseFile/FMet_tRNASynthesis_20121015v08.xml", "FMet_tRNASynthesis_");
		process_for_single_file_type_files("./BaseFile/Initiation_A_20121016v04.xml", "Initiation_A_");
		process_for_single_file_type_files("./BaseFile/Initiation_B1_20121016v24.xml", "Initiation_B1_");
		process_for_single_file_type_files("./BaseFile/Initiation_B2_20131105v02.xml", "Initiation_B2_");
		process_for_single_file_type_files("./BaseFile/SmallMolecules_20130222v03.xml", "SmallMolecules_");
		
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
			System.out.println("some errors");
		} catch (IOException e) {
			System.out.println("some errors");
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
				System.out.println("some errors");
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
			System.out.println("some errors");
		} catch (IOException e) {
			System.out.println("some errors");
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
				System.out.println("some errors");
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
			System.out.println("some errors");
		} catch (IOException e) {
			System.out.println("some errors");
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
				System.out.println("some errors");
			}
		}
		
	}

}
