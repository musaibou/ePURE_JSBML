package jp.riken.yshimizu.ePURE;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

public class Dat_File_Generator {
	
	String xml_file_contents;
	String project_name;
	
	public Dat_File_Generator(String project_name, String xml_file_contents){
		
		this.project_name = project_name;
		this.xml_file_contents = xml_file_contents;
		
	}
	
	public ArrayList<byte[]> execute(){
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document;
		ListOf<Species> ls_species;
		ListOf<Reaction> ls_reaction;
		
		try{
			document = reader.readSBMLFromString(xml_file_contents);
		}catch(XMLStreamException e){
			System.out.println("some errors");
			return null;
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
			int default_initial_value = 0;
			
			writer_initial_values.write("No.,Name,Value");writer_initial_values.newLine();//header
			
			for(int i=0;i<ls_species.size();i++){
				Species spe = ls_species.get(i);
				String key = spe.getName();
				writer_initial_values.write((i+1) + "," + key + "," + default_initial_value);writer_initial_values.newLine();
			}
			
			writer_initial_values.flush();
			
			//parameters
			int default_parameter = 0;
			
			writer_parameters.write("Name,Value");writer_parameters.newLine();//header
			
			for(int i=0;i<ls_reaction.size();i++){
				Reaction react = ls_reaction.get(i);
				writer_parameters.write(react.getId() + "_k1," + default_parameter);writer_parameters.newLine();
			}
			
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
			System.out.println("some errors");
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
				System.out.println("some errors");
			}
		}
		
		ArrayList<byte[]> array = new ArrayList<>();
		
		array.add(byte_ostream_initial_values.toByteArray());
		array.add(byte_ostream_parameters.toByteArray());
		array.add(byte_ostream_reactions.toByteArray());
		
		return array;
		
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
