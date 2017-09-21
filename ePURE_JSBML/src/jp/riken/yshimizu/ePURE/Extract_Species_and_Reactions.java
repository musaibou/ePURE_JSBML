package jp.riken.yshimizu.ePURE;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

public class Extract_Species_and_Reactions {
	
	String file_name;
	String project_name;
	
	public Extract_Species_and_Reactions(String file_name){
		
		this.project_name = file_name.substring(0, file_name.length()-4);
		this.file_name = file_name;
		
	}
	
	public void execute(){
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document;
		ListOf<Species> ls_species;
		ListOf<Reaction> ls_reaction;
		
		try{
			document = reader.readSBML(file_name);
		}catch(XMLStreamException e){
			System.out.println("some errors");
			return;
		}catch(IOException e){
			System.out.println("some errors");
			return;
		}
		
		Model model = document.getModel();
		ls_species = model.getListOfSpecies();
		ls_reaction = model.getListOfReactions();
		
		BufferedWriter writer_species = null;
		BufferedWriter writer_reactions = null;
		
		try{
			writer_species = new BufferedWriter(new FileWriter(project_name + "_species.txt"));
			writer_reactions = new BufferedWriter(new FileWriter(project_name + "_reactions.txt"));
			
			writer_species.write("Name");writer_species.newLine();
			writer_reactions.write("ID\treactants\tproduts");writer_reactions.newLine();
			
			for(int i=0;i<ls_species.size();i++){
				Species spe = ls_species.get(i);
				String key = spe.getName();
				writer_species.write(key);writer_species.newLine();
			}
			
			for(int i=0;i<ls_reaction.size();i++){
				Reaction react = ls_reaction.get(i);
				writer_reactions.write(react.getId());
				writer_reactions.write("\t");
				writer_reactions.write(get_reactants(react));
				writer_reactions.write("\t");
				writer_reactions.write(get_products(react));
				writer_reactions.newLine();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("some errors");
		} catch (IOException e) {
			System.out.println("some errors");
		} finally {
			try {
				writer_reactions.flush();
				writer_species.flush();
				writer_reactions.close();
				writer_species.close();
				
			} catch (IOException e) {
				System.out.println("some errors");
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
