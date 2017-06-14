package jp.riken.yshimizu.ePURE;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.*;

public class Merge_Models {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private String merged_model_ID;
	private String[] sbml_files;
	private int SBML_level;
	private int SBML_version;
	
	private static String default_parameter_name = "k1";
	private static double default_initial_conc = 1;
	private static double default_parameter_value = 1;
	private static String default_compartment_ID = "default";
	
	private SBMLReader reader;
	private SBMLWriter writer;
	private SBMLDocument document;
	private SBMLDocument output_document;
	private Model output_model;
	private Compartment compartment;
	
	private String error_result;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Merge_Models(String merged_model_ID, String[] sbml_files, int SBML_level, int SBML_version){
		
		this.merged_model_ID = merged_model_ID;
		this.sbml_files = sbml_files;
		
		this.SBML_level = SBML_level;
		this.SBML_version = SBML_version;
		
		reader = new SBMLReader();
		writer = new SBMLWriter();
		document = new SBMLDocument();
		
		output_document = new SBMLDocument(SBML_level, SBML_version);
		output_model = output_document.createModel(this.merged_model_ID);
		output_model.setName(this.merged_model_ID);
		
		compartment = output_model.createCompartment();
		compartment.setId(default_compartment_ID);
		compartment.setSpatialDimensions(3);
		compartment.setSize(1.0);
		compartment.setUnits("volume");
		compartment.setConstant(true);
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public boolean execute(){
		
		if(error_check() != true) return false;
		
		LinkedHashMap<String, Species> species_map = new LinkedHashMap<String, Species>();
		LinkedHashMap<String, Reaction> reaction_map = new LinkedHashMap<String, Reaction>();
		long ID_number = 0;
		
		//Only species and reactions are merged
		ListOf<Species> ls_species;
		ListOf<Reaction> ls_reaction;
		long pre_number_of_species = 0;
		long pre_number_of_reaction = 0;
		
		for(int i = 0; i < sbml_files.length; i++){
			
			System.out.println("Processing " + (i+1) + "/" + sbml_files.length + " files...");
			
			try{
				document = reader.readSBML(sbml_files[i]);
			}catch(IOException e){
				System.out.println("some errors");
			}catch(XMLStreamException e){
				System.out.println("some errors");
			}
			
			Model model = document.getModel();
			ls_species = model.getListOfSpecies();
			ls_reaction = model.getListOfReactions();
			pre_number_of_species += ls_species.size();
			pre_number_of_reaction += ls_reaction.size();
			
			//species
			
			/*TODO
			 * 
			 * species names (==IDs) must be unique even if they form complexes;
			 * For example, EF-G_GDP and GDP_EF-G can be recognized as same species
			 * but recognized as different species in this program..
			 * 
			 * */
			
			for(int j=0;j<ls_species.size();j++){
				Species spe = ls_species.get(j);
				String key = spe.getId();
				if(species_map.containsKey(key)==false){
					spe = convert_species(spe);
					species_map.put(spe.getId(), spe);
				}
			}
			
			//reactions
			
			/*TODO
			 * 
			 * The same problem as species is inherent..
			 * 
			 */
			
			for(int j=0;j<ls_reaction.size();j++){
				Reaction react = ls_reaction.get(j);
				String key = make_key_name_for_reaction_map(react);
				
				if(reaction_map.containsKey(key)==false){
					ID_number++;
					react = convert_reaction(react, ID_number);
					reaction_map.put(key, react);
				}
			}
			
		}
		
		System.out.println();
		
		int merged_species = 0;
		for (String key : species_map.keySet()) {
			output_model.addSpecies(species_map.get(key));
			merged_species++;
		}
		
		int merged_reactions = 0;
		for (String key : reaction_map.keySet()) {
			output_model.addReaction(reaction_map.get(key));
			merged_reactions++;
		}
		
		System.out.println(merged_species + " / " + pre_number_of_species + " species are merged.");
		System.out.println(merged_reactions + " / " + pre_number_of_reaction + " reactions are merged.");
		System.out.println();
		
		try{
			writer.writeSBMLToFile(output_document, merged_model_ID + ".xml");
		}catch(FileNotFoundException e){
			System.out.println("some errors");
		}catch(XMLStreamException e){
			System.out.println("some errors");
		}
		//writer.writeSBML(output_document, merged_model_ID + ".xml");
		System.out.println("saved merged model as " + merged_model_ID + ".xml");
		System.out.println();
		
		return true;
		
	}
	
	public String get_error_result(){
		
		return error_result;
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private boolean error_check(){
		
		//this program requires SBML level 2, version 4
		if(SBML_level!=2||SBML_version!=4){
			error_result = "This program requires SBML level 2, version 4";
			return false;
		}
		
		int total_errors = 0;
		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i<sbml_files.length; i++){
			
			try{
				document = reader.readSBML(sbml_files[i]);
			}catch(IOException e){
				System.out.println("some errors");
			}catch(XMLStreamException e){
				System.out.println("some errors");
			}
			long errors = document.getNumErrors();
			
			System.out.println("Checking " + (i+1) + "/" + sbml_files.length + "file...");
			
			if(errors>0){
				
				System.out.println("Found " + errors + " errors in " + sbml_files[i]);
				
				total_errors += errors;
				sb.append("Errors found in " + sbml_files[i] + "\n\n");
				sb.append(document.getErrorLog().toString());
				
			} else {
				System.out.println("ok");
			}
			
			System.out.println();
			
		}
		
		error_result = sb.toString();
		
		if(total_errors>0){
			return false;
		} else {
			return true;
		}
		
	}
	
	private ArrayList<String> sort_list(ListOf<SpeciesReference> list){
		
		ArrayList<String> al = new ArrayList<String>();
		
		for(int i=0;i<list.size();i++){
			al.add(list.get(i).getSpecies());
		}
		
		Collections.sort(al);
		return al;
		
	}
	
	private String make_key_name_for_reaction_map(Reaction react){
		
		StringBuilder sb = new StringBuilder();
		
		ArrayList<String> reactants = sort_list(react.getListOfReactants());
		ArrayList<String> products = sort_list(react.getListOfProducts());
		//ArrayList<String> modifiers = sort_list(react.getListOfModifiers());
		
		for(int j=0;j<reactants.size();j++){
			sb.append(reactants.get(j)+"_-_");
		}
		
		sb.append("___");
		
		for(int j=0;j<products.size();j++){
			sb.append(products.get(j)+"_-_");
		}
		
		/*sb.append("___");
		
		for(int j=0;j<modifiers.size();j++){
			sb.append(modifiers.get(j)+"_-_");
		}*/
		
		return sb.toString();
		
	}
	
	private Species convert_species(Species original){
		
		Species converted = new Species(SBML_level, SBML_version);
		
		converted.setId(original.getId());
		converted.setName(original.getName());
		converted.setCompartment(compartment.getId());
		converted.setInitialConcentration(default_initial_conc);
		
		return converted;
		
	}
	
	@SuppressWarnings("deprecation")
	private Reaction convert_reaction(Reaction original, long ID_number){
		
		Reaction converted = new Reaction(SBML_level, SBML_version);
		
		DecimalFormat f = new DecimalFormat("0000000000");
		converted.setId("re"+f.format(ID_number));
		
		converted.setReversible(original.getReversible());
		
		//reactant
		
		for(int i=0;i<original.getNumReactants();i++){
			
			SpeciesReference orig_sr = original.getReactant(i);
			
			SpeciesReference sr = new SpeciesReference(SBML_level, SBML_version);
			sr.setSpecies(orig_sr.getSpecies());
			
			//TODO set mass action related
			/*try{
				sr.createStoichiometryMath(JSBML.parseFormula("1"));
			}catch(ParseException e){
				System.out.println("some errors");
			}*/
			
			converted.addReactant(sr);
			
		}
		
		//product
		
		for(int i=0;i<original.getNumProducts();i++){
			
			SpeciesReference orig_sr = original.getProduct(i);
			
			SpeciesReference sr = new SpeciesReference(SBML_level, SBML_version);
			sr.setSpecies(orig_sr.getSpecies());
			
			//TODO set mass action related
			/*try{
				sr.createStoichiometryMath(JSBML.parseFormula("1"));
			}catch(ParseException e){
				System.out.println("some errors");
			}*/
			
			converted.addProduct(sr);
			
		}
		
		//modifier is not processed.
		/*
		for(int i=0;i<original.getNumModifiers();i++){
			
			ModifierSpeciesReference orig_sr = original.getModifier(i);
			
			ModifierSpeciesReference sr = new ModifierSpeciesReference(SBML_level, SBML_version);
			sr.setSpecies(orig_sr.getSpecies());
			converted.addModifier(sr);
			
		}*/
		
		//KineticLaw
		/*TODO
		 * parameter is not effectively used below
		 */
		KineticLaw kinetic_law = converted.createKineticLaw();
		Parameter parameter = new Parameter();
		
		parameter.setId(default_parameter_name);
		parameter.setName(default_parameter_name);
		parameter.setUnits("substance");
		parameter.setValue(default_parameter_value);
		parameter.setConstant(true);
		
		StringBuilder sb = new StringBuilder();
		//directly write mathML
		sb.append("<?xml version='1.0' encoding='UTF-8'?>" + "\n");
		sb.append("<math xmlns='http://www.w3.org/1998/Math/MathML'>" + "\n");
		sb.append("  <apply>");
		sb.append("    <times/>");
		sb.append("    <ci> " + parameter.getId() + " </ci>");
		for(int i=0;i<converted.getListOfReactants().size();i++){
			sb.append("    <ci> " + converted.getListOfReactants().get(i).getSpecies() + " </ci>");
		}
		sb.append("  </apply>");
		sb.append("</math>");
		
		kinetic_law.setMath(JSBML.readMathMLFromString(sb.toString()));
		
		kinetic_law.addParameter(parameter);
		
		/*sb.append(parameter.getId());
		for(int i=0;i<converted.getListOfReactants().size();i++){
			sb.append(" * ");
			sb.append(converted.getListOfReactants().get(i).getSpecies());
		}
		try{
			kinetic_law.setMath(JSBML.parseFormula(sb.toString()));
		}catch(ParseException e){
			System.out.println("some errors");
		}*/
		
		return converted;
		
	}
	
}
