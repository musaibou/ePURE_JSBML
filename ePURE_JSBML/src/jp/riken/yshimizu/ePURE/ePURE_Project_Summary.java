package jp.riken.yshimizu.ePURE;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ePURE_Project_Summary {
	
	/*---------------------------------------------------------
	 * field
	---------------------------------------------------------*/
	
	private String project_name;
	private String rna_seq;
	private String output_directory;
	private String zipped_SBML_files_name;
	
	private HashMap<String, Boolean> aa_map;
	private HashMap<String, Boolean> tRNA_map;
	private HashMap<String, ArrayList<String>> codon_vs_tRNA_map;
	private ArrayList<String> codon_array;
	
	private int SBML_level = 2;
	private int SBML_version = 4;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public ePURE_Project_Summary(String project_name, String seq, String output_directory, String conf_file) {
		
		this.output_directory = output_directory;
		this.project_name = project_name;
		this.rna_seq = seq;
		this.zipped_SBML_files_name = project_name + "_SBML_files.zip";
		
		aa_map = new HashMap<>();
		tRNA_map = new HashMap<>();
		codon_vs_tRNA_map = new HashMap<>();
		
		//set default maps
		set_maps();
		
		//read conf file to update maps
		if(conf_file!=null){
			set_maps(conf_file);
		}
		
		//sequence check
		this.rna_seq = this.rna_seq.toUpperCase();
		
		//input sequence information to the array
		codon_array = new ArrayList<>();
		int seq_size = this.rna_seq.length();
		if(seq_size%3!=0){
			System.out.println("a sequence is not correct.");
		}else{
			for(int i=0;i<(seq_size/3);i++){
				codon_array.add(this.rna_seq.substring(i*3, i*3+3));
			}
		}
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void set_maps(){
		
		aa_map.put("Ala", true);aa_map.put("Cys", true);aa_map.put("Asp", true);aa_map.put("Glu", true);aa_map.put("Phe", true);
		aa_map.put("Gly", true);aa_map.put("His", true);aa_map.put("Ile", true);aa_map.put("Lys", true);aa_map.put("Leu", true);
		aa_map.put("Met", true);aa_map.put("Asn", true);aa_map.put("Pro", true);aa_map.put("Gln", true);aa_map.put("Arg", true);
		aa_map.put("Ser", true);aa_map.put("Thr", true);aa_map.put("Val", true);aa_map.put("Trp", true);aa_map.put("Tyr", true);
		
		
		tRNA_map.put("AlaGGC", true);tRNA_map.put("AlaUGC", true);
		tRNA_map.put("ArgACG", true);tRNA_map.put("ArgCCG", true);tRNA_map.put("ArgUCU", true);tRNA_map.put("ArgCCU", true);
		tRNA_map.put("AsnGUU", true);
		tRNA_map.put("AspGUC", true);
		tRNA_map.put("CysGCA", true);
		tRNA_map.put("GlnUUG", true);tRNA_map.put("GlnCUG", true);
		tRNA_map.put("GluUUC", true);
		tRNA_map.put("GlyGCC", true);tRNA_map.put("GlyUCC", true);tRNA_map.put("GlyCCC", true);
		tRNA_map.put("HisGUG", true);
		tRNA_map.put("IleGAU", true);tRNA_map.put("IleCAU", true);
		tRNA_map.put("LeuUAA", true);tRNA_map.put("LeuCAA", true);tRNA_map.put("LeuGAG", true);tRNA_map.put("LeuUAG", true);tRNA_map.put("LeuCAG", true);
		tRNA_map.put("LysUUU", true);
		tRNA_map.put("fMetCAU", true);
		tRNA_map.put("mMetCAU", true);
		tRNA_map.put("PheGAA", true);
		tRNA_map.put("ProGGG", true);tRNA_map.put("ProUGG", true);tRNA_map.put("ProCGG", true);
		tRNA_map.put("SerGGA", true);tRNA_map.put("SerUGA", true);tRNA_map.put("SerCGA", true);tRNA_map.put("SerGCU", true);
		tRNA_map.put("ThrGGU", true);tRNA_map.put("ThrUGU", true);tRNA_map.put("ThrCGU", true);
		tRNA_map.put("TrpCCA", true);
		tRNA_map.put("TyrGUA", true);
		tRNA_map.put("ValGAC", true);tRNA_map.put("ValUAC", true);
		
		ArrayList<String> array;
		
		array = new ArrayList<>();array.add("PheGAA");
		codon_vs_tRNA_map.put("UUU", array);
		codon_vs_tRNA_map.put("UUC", array);
		
		array = new ArrayList<>();array.add("LeuUAA");
		codon_vs_tRNA_map.put("UUA", array);
		
		array = new ArrayList<>();array.add("LeuUAA");array.add("LeuCAA");
		codon_vs_tRNA_map.put("UUG", array);
		
		array = new ArrayList<>();array.add("LeuGAG");array.add("LeuUAG");
		codon_vs_tRNA_map.put("CUU", array);
		
		array = new ArrayList<>();array.add("LeuGAG");
		codon_vs_tRNA_map.put("CUC", array);
		
		array = new ArrayList<>();array.add("LeuUAG");
		codon_vs_tRNA_map.put("CUA", array);
		
		array = new ArrayList<>();array.add("LeuUAG");array.add("LeuCAG");
		codon_vs_tRNA_map.put("CUG", array);
		
		array = new ArrayList<>();array.add("IleGAU");
		codon_vs_tRNA_map.put("AUU", array);
		codon_vs_tRNA_map.put("AUC", array);
		
		array = new ArrayList<>();array.add("IleCAU");
		codon_vs_tRNA_map.put("AUA", array);
		
		array = new ArrayList<>();array.add("fMetCAU");array.add("mMetCAU");
		codon_vs_tRNA_map.put("AUG", array);
		
		array = new ArrayList<>();array.add("ValGAC");array.add("ValUAC");
		codon_vs_tRNA_map.put("GUU", array);
		
		array = new ArrayList<>();array.add("ValGAC");
		codon_vs_tRNA_map.put("GUC", array);
		
		array = new ArrayList<>();array.add("ValUAC");
		codon_vs_tRNA_map.put("GUA", array);
		codon_vs_tRNA_map.put("GUG", array);
		
		array = new ArrayList<>();array.add("SerGGA");array.add("SerUGA");
		codon_vs_tRNA_map.put("UCU", array);
		
		array = new ArrayList<>();array.add("SerGGA");
		codon_vs_tRNA_map.put("UCC", array);
		
		array = new ArrayList<>();array.add("SerUGA");
		codon_vs_tRNA_map.put("UCA", array);
		
		array = new ArrayList<>();array.add("SerUGA");array.add("SerCGA");
		codon_vs_tRNA_map.put("UCG", array);
		
		array = new ArrayList<>();array.add("ProGGG");array.add("ProUGG");
		codon_vs_tRNA_map.put("CCU", array);
		
		array = new ArrayList<>();array.add("ProGGG");
		codon_vs_tRNA_map.put("CCC", array);
		
		array = new ArrayList<>();array.add("ProUGG");
		codon_vs_tRNA_map.put("CCA", array);
		
		array = new ArrayList<>();array.add("ProUGG");array.add("ProCGG");
		codon_vs_tRNA_map.put("CCG", array);
		
		array = new ArrayList<>();array.add("ThrGGU");array.add("ThrUGU");
		codon_vs_tRNA_map.put("ACU", array);
		
		array = new ArrayList<>();array.add("ThrGGU");
		codon_vs_tRNA_map.put("ACC", array);
		
		array = new ArrayList<>();array.add("ThrUGU");
		codon_vs_tRNA_map.put("ACA", array);
		
		array = new ArrayList<>();array.add("ThrUGU");array.add("ThrCGU");
		codon_vs_tRNA_map.put("ACG", array);
		
		array = new ArrayList<>();array.add("AlaGGC");array.add("AlaUGC");
		codon_vs_tRNA_map.put("GCU", array);
		
		array = new ArrayList<>();array.add("AlaGGC");
		codon_vs_tRNA_map.put("GCC", array);
		
		array = new ArrayList<>();array.add("AlaUGC");
		codon_vs_tRNA_map.put("GCA", array);
		codon_vs_tRNA_map.put("GCG", array);
		
		array = new ArrayList<>();array.add("TyrGUA");
		codon_vs_tRNA_map.put("UAU", array);
		codon_vs_tRNA_map.put("UAC", array);
		
		array = new ArrayList<>();array.add("HisGUG");
		codon_vs_tRNA_map.put("CAU", array);
		codon_vs_tRNA_map.put("CAC", array);
		
		array = new ArrayList<>();array.add("GlnUUG");
		codon_vs_tRNA_map.put("CAA", array);
		
		array = new ArrayList<>();array.add("GlnUUG");array.add("GlnCUG");
		codon_vs_tRNA_map.put("CAG", array);
		
		array = new ArrayList<>();array.add("AsnGUU");
		codon_vs_tRNA_map.put("AAU", array);
		codon_vs_tRNA_map.put("AAC", array);
		
		array = new ArrayList<>();array.add("LysUUU");
		codon_vs_tRNA_map.put("AAA", array);
		codon_vs_tRNA_map.put("AAG", array);
		
		array = new ArrayList<>();array.add("AspGUC");
		codon_vs_tRNA_map.put("GAU", array);
		codon_vs_tRNA_map.put("GAC", array);
		
		array = new ArrayList<>();array.add("GluUUC");
		codon_vs_tRNA_map.put("GAA", array);
		codon_vs_tRNA_map.put("GAG", array);
		
		array = new ArrayList<>();array.add("CysGCA");
		codon_vs_tRNA_map.put("UGU", array);
		codon_vs_tRNA_map.put("UGC", array);
		
		array = new ArrayList<>();array.add("TrpCCA");
		codon_vs_tRNA_map.put("UGG", array);
		
		array = new ArrayList<>();array.add("ArgACG");
		codon_vs_tRNA_map.put("CGU", array);
		codon_vs_tRNA_map.put("CGC", array);
		codon_vs_tRNA_map.put("CGA", array);
		
		array = new ArrayList<>();array.add("ArgCCG");
		codon_vs_tRNA_map.put("CGG", array);
		
		array = new ArrayList<>();array.add("SerGCU");
		codon_vs_tRNA_map.put("AGU", array);
		codon_vs_tRNA_map.put("AGC", array);
		
		array = new ArrayList<>();array.add("ArgUCU");
		codon_vs_tRNA_map.put("AGA", array);
		
		array = new ArrayList<>();array.add("ArgUCU");array.add("ArgCCU");
		codon_vs_tRNA_map.put("AGG", array);
		
		array = new ArrayList<>();array.add("GlyGCC");
		codon_vs_tRNA_map.put("GGU", array);
		codon_vs_tRNA_map.put("GGC", array);
		
		array = new ArrayList<>();array.add("GlyUCC");
		codon_vs_tRNA_map.put("GGA", array);
		
		array = new ArrayList<>();array.add("GlyUCC");array.add("GlyCCC");
		codon_vs_tRNA_map.put("GGG", array);
		
	}
	
	private void set_maps(String conf_file){
		
		BufferedReader reader = null;
		String str;
		
		try{
			reader = new BufferedReader(new FileReader(conf_file));
			
			while(true){
				str = reader.readLine();
				
				if(str==null){
					break;
				}
				
				str = str.replace(" ", "");
				
				if(str.contains("=")){
					String[] s = str.split("=");
					if(aa_map.containsKey(s[0])){
						if(s[1].equals("0")){
							aa_map.put(s[0], false);
						}else if(s[1].equals("1")){
							aa_map.put(s[0], true);
						}else{
							System.out.println("found some errors in conf file.");
						}
					}else if(tRNA_map.containsKey(s[0])){
						if(s[1].equals("0")){
							tRNA_map.put(s[0], false);
						}else if(s[1].equals("1")){
							tRNA_map.put(s[0], true);
						}else{
							System.out.println("found some errors in conf file.");
						}
					}else if(codon_vs_tRNA_map.containsKey(s[0])){
						ArrayList<String> array = new ArrayList<>();
						if(s[1].contains("|")){
							String[] s2 = s[1].split("\\|");
							for(int i=0;i<s2.length;i++){
								if(tRNA_map.containsKey(s2[i])){
									array.add(s2[i]);
								}else{
									System.out.println("found some errors in conf file.3");
								}
							}
						}else{
							if(tRNA_map.containsKey(s[1])){
								array.add(s[1]);
							}else{
								System.out.println("found some errors in conf file.");
							}
						}
						codon_vs_tRNA_map.put(s[0], array);
					}else{
						System.out.println("found some errors in conf file.");
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("some errors");
		} catch (IOException e) {
			System.out.println("some errors");
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				System.out.println("some errors");
			}
		}
		
	}
	
	private boolean sequence_check(String str){
		
		for(int i=0;i<str.length();i++){
			if(str.charAt(i)!='A'&str.charAt(i)!='g'
				&str.charAt(i)!='c'&str.charAt(i)!='t'
				&str.charAt(i)!='A'&str.charAt(i)!='G'
				&str.charAt(i)!='C'&str.charAt(i)!='T'
				&str.charAt(i)!='U'&str.charAt(i)!='u'){
				return false;
			}
		   		
		}
		return true;
	}
	
	
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public String get_project_name(){
		return project_name;
	}
	
	public String get_rna_seq(){
		return rna_seq;
	}
	
	public String get_output_directory(){
		return output_directory;
	}
	
	public String get_zipped_SBML_files_name(){
		return zipped_SBML_files_name;
	}
	
	public HashMap<String, Boolean> get_aa_map(){
		return aa_map;
	}
	
	public HashMap<String, Boolean> get_tRNA_map(){
		return tRNA_map;
	}
	
	public HashMap<String, ArrayList<String>> get_codon_vs_tRNA_map(){
		return codon_vs_tRNA_map;
	}
	
	public ArrayList<String> get_codon_array(){
		return codon_array;
	}
	
	public int get_SBML_leve(){
		return SBML_level;
	}
	
	public int get_SBML_version(){
		return SBML_version;
	}
	
	public void update_aa_map(String key, boolean flag){
		if(aa_map.containsKey(key)){
			aa_map.put(key, flag);
		}
	}
	
	public void update_tRNA_map(String key, boolean flag){
		if(tRNA_map.containsKey(key)){
			tRNA_map.put(key, flag);
		}
	}
	
	public void update_codon_vs_tRNA_map(String key, ArrayList<String> array){
		if(codon_vs_tRNA_map.containsKey(key)){
			codon_vs_tRNA_map.put(key, array);
		}
	}
	
}
