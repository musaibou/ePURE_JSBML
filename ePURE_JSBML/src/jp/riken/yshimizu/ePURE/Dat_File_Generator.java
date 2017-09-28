package jp.riken.yshimizu.ePURE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import com.opencsv.CSVParser;

public class Dat_File_Generator {
	
	private String xml_file_contents;
	private String initial_values_csv;
	private String parameters_csv;
	
	public Dat_File_Generator(ePURE_Project epure, String xml_file_contents){
		
		this.xml_file_contents = xml_file_contents;
		this.initial_values_csv = epure.get_initial_values_csv();
		this.parameters_csv = epure.get_parameters_csv();
		
	}
	
	public ArrayList<byte[]> execute(){
		
		HashMap<String, Double> initial_values_map = new HashMap<>();
		set_initial_values_map(initial_values_map);
		
		ArrayList<Parameter_Struct> param_array = new ArrayList<>();
		set_parameter_array(param_array);
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = null;
		ListOf<Species> ls_species;
		ListOf<Reaction> ls_reaction;
		
		try{
			document = reader.readSBMLFromString(xml_file_contents);
		}catch(XMLStreamException e){
			System.out.println("XML stream error in reading the merged SBML file.");
			e.printStackTrace();
			System.exit(0);
		}
		
		Model model = document.getModel();
		ls_species = model.getListOfSpecies();
		ls_reaction = model.getListOfReactions();
		
		BufferedWriter writer_initial_values = null;
		BufferedWriter writer_parameters = null;
		BufferedWriter writer_reactions = null;
		
		ByteArrayOutputStream byte_ostream_initial_values = new ByteArrayOutputStream();
		ByteArrayOutputStream byte_ostream_parameters = new ByteArrayOutputStream();
		ByteArrayOutputStream byte_ostream_reactions = new ByteArrayOutputStream();
		
		try{
			writer_initial_values = new BufferedWriter(new OutputStreamWriter(byte_ostream_initial_values));
			writer_parameters = new BufferedWriter(new OutputStreamWriter(byte_ostream_parameters));
			writer_reactions = new BufferedWriter(new OutputStreamWriter(byte_ostream_reactions));
			
			String str = null;
			
			//initial_values
			System.out.println("Making initial values CSV...");
			int default_initial_value = 0;
			writer_initial_values.write("No.,Name,Value");writer_initial_values.newLine();//header
			
			for(int i=0;i<ls_species.size();i++){
				Species spe = ls_species.get(i);
				String key = spe.getName();
				if(initial_values_map.containsKey(key)){
					writer_initial_values.write((i+1) + "," + key + "," + initial_values_map.get(key));writer_initial_values.newLine();
				}else{
					writer_initial_values.write((i+1) + "," + key + "," + default_initial_value);writer_initial_values.newLine();
				}
			}
			
			writer_initial_values.flush();
			
			//parameters
			System.out.println("Making parameters CSV...");
			System.out.println();
			int hit;
			int temp;
			
			writer_parameters.write("Name,Value");writer_parameters.newLine();//header
			
			for(int i=0;i<ls_reaction.size();i++){
				if((i+1)%500==0){
					System.out.println("  Processed " + (((i+1)/500)*500) + "/" + ls_reaction.size() + " ...");
				}
				Reaction react = ls_reaction.get(i);
				
				hit = 0;
				temp = -1;
				
				for(int j=0;j<param_array.size();j++){
					Pattern pattern_reactants = Pattern.compile(param_array.get(j).reactants);
					Pattern pattern_products = Pattern.compile(param_array.get(j).products);
					Matcher matcher_reactants = pattern_reactants.matcher(get_reactants(react));
					Matcher matcher_products = pattern_products.matcher(get_products(react));
					if(matcher_reactants.matches()&&matcher_products.matches()){
						hit++;
						temp = j;
					}
				}
				
				if(hit==1){
					writer_parameters.write(react.getId() + "_k1," + param_array.get(temp).k);writer_parameters.newLine();
				}else{
					writer_parameters.write(react.getId() + "_k1," + "NA");writer_parameters.newLine();
					System.out.println("Could not assign " + react.getId() + "_k1, \"NA\" is alternatively assigned. Please check." );
				}
				
			}
			
			System.out.println();
			
			writer_parameters.write("default,1");writer_parameters.newLine();
			
			writer_parameters.flush();
			
			//reactions
			writer_reactions.write("id,reactants,products");writer_reactions.newLine();
			
			for(int i=0;i<ls_reaction.size();i++){
				Reaction react = ls_reaction.get(i);
				writer_reactions.write(react.getId());
				writer_reactions.write(",");
				str = get_reactants(react);
				if(str.contains(",")){
					writer_reactions.write("\"" + str + "\"");
				}else{
					writer_reactions.write(str);
				}
				writer_reactions.write(",");
				str = get_products(react);
				if(str.contains(",")){
					writer_reactions.write("\"" + str + "\"");
				}else{
					writer_reactions.write(str);
				}
				writer_reactions.newLine();
			}
			
			writer_reactions.flush();
			
		} catch (IOException e) {
			System.out.println("Disk I/O error related to making the dat files");
		} finally {
			try {
				if(writer_initial_values!=null){
					writer_initial_values.flush();
					writer_initial_values.close();
				}
				if(writer_parameters!=null){
					writer_parameters.flush();
					writer_parameters.close();
				}
				if(writer_reactions!=null){
					writer_reactions.flush();
					writer_reactions.close();
				}
				if(byte_ostream_initial_values!=null){
					byte_ostream_initial_values.close();
				}
				if(byte_ostream_parameters!=null){
					byte_ostream_parameters.close();
				}
				if(byte_ostream_reactions!=null){
					byte_ostream_reactions.close();
				}
			} catch (IOException e) {
				System.out.println("Disk I/O error related to making the dat files");
			}
		}
		
		ArrayList<byte[]> array = new ArrayList<>();
		
		array.add(byte_ostream_initial_values.toByteArray());
		array.add(byte_ostream_parameters.toByteArray());
		array.add(byte_ostream_reactions.toByteArray());
		
		return array;
		
	}
	
	private void set_initial_values_map(HashMap<String, Double> map){
		
		BufferedReader reader = null;
		String str;
		CSVParser CSV_parser = new CSVParser();
		double a = 0;
		
		try{
			reader = new BufferedReader(new FileReader(initial_values_csv));
			reader.readLine();//header
			
			while(true){
				str = reader.readLine();
				
				if(str==null){
					break;
				}
				String[] s = CSV_parser.parseLine(str);
				
				if(s.length==2){
					try{
						a = Double.valueOf(s[1]);
					}catch(NumberFormatException e){
						System.out.println(initial_values_csv + " has some defects: " + str);
						e.printStackTrace();
						System.exit(0);
					}
					map.put(s[0], a);
				}else{
					System.out.println(initial_values_csv + " has some defects: " + str);
					System.exit(0);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the initial values CSV file: " + initial_values_csv );
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Disk I/O error related to the initial values CSV file: " + initial_values_csv);
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				System.out.println("Disk I/O error related to the initial values CSV file: " + initial_values_csv);
				e.printStackTrace();
				System.exit(0);
			}
		}
		
	}
	
	private class Parameter_Struct {
		
		//public int id;
		public	String reactants;
		public	String products;
		public	double k; 
		
	}
	
	private void set_parameter_array(ArrayList<Parameter_Struct> array){
		
		BufferedReader reader = null;
		String str;
		CSVParser CSV_parser = new CSVParser();
		
		try{
			reader = new BufferedReader(new FileReader(parameters_csv));
			reader.readLine();//header
			
			while(true){
				str = reader.readLine();
				
				if(str==null){
					break;
				}
				
				String[] s = CSV_parser.parseLine(str);
				
				if(s.length==4){
					Parameter_Struct ps = new Parameter_Struct();
					//ps.id = Integer.valueOf(s[0]);
					ps.reactants = s[1];
					ps.products = s[2];
					try{
						ps.k = Double.valueOf(s[3]);
					}catch(NumberFormatException e){
						System.out.println(parameters_csv + " has some defects: " + str);
						e.printStackTrace();
						System.exit(0);
					}
					array.add(ps);
				}else{
					System.out.println(parameters_csv + " has some defects: " + str);
					System.exit(0);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find the parameter CSV file: " + parameters_csv );
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Disk I/O error related to the parameter CSV file: " + parameters_csv);
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				System.out.println("Disk I/O error related to the parameter CSV file: " + parameters_csv);
				e.printStackTrace();
				System.exit(0);
			}
		}
		
	}
	
	private String get_reactants(Reaction react){
		
		int i;
		
		StringBuilder sb = new StringBuilder();
		ListOf<SpeciesReference> list = react.getListOfReactants();
		
		for(i=0;i<list.size()-1;i++){
			sb.append(list.get(i).getSpeciesInstance().getName()+",");
		}
		sb.append(list.get(i).getSpeciesInstance().getName());
		
		return sb.toString();
		
	}
	
	private String get_products(Reaction react){
		
		int i;
		
		StringBuilder sb = new StringBuilder();
		ListOf<SpeciesReference> list = react.getListOfProducts();
		
		for(i=0;i<list.size()-1;i++){
			sb.append(list.get(i).getSpeciesInstance().getName()+",");
		}
		sb.append(list.get(i).getSpeciesInstance().getName());
		
		return sb.toString();
		
	}
	
}
