package jp.riken.yshimizu.ePURE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.*;

public class Merge_Models {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private String merged_model_ID;
	private int SBML_level;
	private int SBML_version;
	
	private ZipFile zip_file = null;
	
	private SBMLDocument output_document;
	private Model output_model;
	private Compartment compartment;
	
	private long pre_number_of_species;
	private long pre_number_of_reaction;
	private LinkedHashMap<String, Species> species_map = new LinkedHashMap<String, Species>();
	private LinkedHashMap<String, Reaction> reaction_map = new LinkedHashMap<String, Reaction>();
	private long ID_number;
	
	//private String error_result;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Merge_Models(String project_name, ZipFile zip_file){
		
		this.zip_file = zip_file;
		
		this.SBML_level = ePURE_Header.SBML_level;
		this.SBML_version = ePURE_Header.SBML_version;
		this.merged_model_ID = project_name;
		
		output_document = new SBMLDocument(ePURE_Header.SBML_level, ePURE_Header.SBML_version);
		output_model = output_document.createModel(this.merged_model_ID);
		output_model.setName(this.merged_model_ID);
		
		compartment = output_model.createCompartment();
		compartment.setId(ePURE_Header.default_compartment_ID);
		compartment.setSpatialDimensions(3);
		compartment.setSize(1.0);
		compartment.setUnits("volume");
		compartment.setConstant(true);
		
		pre_number_of_species = 0;
		pre_number_of_reaction = 0;
		
		ID_number = 0;
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public ByteArrayOutputStream execute(){
		
		SBMLReader reader = new SBMLReader();
		SBMLWriter writer = new SBMLWriter();
		
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		if(zip_file!=null){
			
			int number_of_files = zip_file.size();
			int count = 0;
			InputStream istream = null;
			ZipEntry entry = null;
			
			Enumeration<? extends ZipEntry> entries = zip_file.entries();
			
			System.out.println(number_of_files + " SBML files will be merged...");
			System.out.println();
			
			while(entries.hasMoreElements()){
				
				count++;
				if(count%10==0){
					System.out.println("  Processed " + count + "/" + number_of_files + " files from zip...");
				}
				entry = entries.nextElement();
				
				try {
					istream = zip_file.getInputStream(entry);
					append_document_contents(reader.readSBMLFromStream(istream));
					istream.close();
				} catch (IOException e) {
					System.out.println("Disk I/O related to the SBML file: " + entry.getName());
					e.printStackTrace();
					System.exit(0);
				} catch (XMLStreamException e) {
					System.out.println("XML parse error related to the SBML file: " + entry.getName());
					e.printStackTrace();
					System.exit(0);
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
		
		System.out.println("  " + merged_species + " / " + pre_number_of_species + " species are merged.");
		System.out.println("  " + merged_reactions + " / " + pre_number_of_reaction + " reactions are merged.");
		System.out.println();
		
		try{
			writer.write(output_document, byte_ostream);
		}catch(XMLStreamException e){
			System.out.println("XML stream error while saving the merged SBML file.");
			e.printStackTrace();
			System.exit(0);
		}finally{
			try{
				if(byte_ostream!=null){
					byte_ostream.close();
				}
			}catch(IOException e){
				System.out.println("Disk I/O error related to merging the SBML files.");
				e.printStackTrace();
				System.exit(0);
			}
		}
		//writer.writeSBML(output_document, merged_model_ID + ".xml");
		System.out.println("  Saved merged model as " + merged_model_ID + ".xml");
		System.out.println();
		
		return byte_ostream;
		
	}
	
	/*public String get_error_result(){
		
		return error_result;
		
	}*/
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void append_document_contents(SBMLDocument doc){
		
		//Only species and reactions are merged
		ListOf<Species> ls_species;
		ListOf<Reaction> ls_reaction;
				
		Model model = doc.getModel();
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
			//String key = spe.getId();
			String key = spe.getName();//170616 modified
			
			if(species_map.containsKey(key)==false){
				spe = convert_species(spe);
				species_map.put(key, spe);
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
	
	/*private boolean error_check(){
		
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
		
	}*/
	
	private ArrayList<String> sort_list(ListOf<SpeciesReference> list){
		
		ArrayList<String> al = new ArrayList<String>();
		
		for(int i=0;i<list.size();i++){
			//al.add(list.get(i).getSpecies());
			al.add(list.get(i).getSpeciesInstance().getName());//170616 modified
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
			sb.append(reactants.get(j)+"\t");
		}
		
		sb.append("\t\t");
		
		for(int j=0;j<products.size();j++){
			sb.append(products.get(j)+"\t");
		}
		
		/*sb.append("___");
		
		for(int j=0;j<modifiers.size();j++){
			sb.append(modifiers.get(j)+"_-_");
		}*/
		
		return sb.toString();
		
	}
	
	private Species convert_species(Species original){
		
		Species converted = new Species(SBML_level, SBML_version);
		
		//converted.setId(original.getId());
		converted.setId(original.getName());//170616 modified
		converted.setName(original.getName());
		converted.setCompartment(compartment.getId());
		converted.setInitialConcentration(ePURE_Header.default_initial_conc);
		
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
			Species spe = orig_sr.getSpeciesInstance();//170616 modified
			spe = convert_species(spe);
			
			SpeciesReference sr = new SpeciesReference(SBML_level, SBML_version);
			//sr.setSpecies(orig_sr.getSpecies());
			sr.setSpecies(spe);//170616 modified
			
			
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
			Species spe = orig_sr.getSpeciesInstance();//170616 modified
			spe = convert_species(spe);
			
			SpeciesReference sr = new SpeciesReference(SBML_level, SBML_version);
			//sr.setSpecies(orig_sr.getSpecies());
			sr.setSpecies(spe);//170616 modified
			
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
		
		parameter.setId(ePURE_Header.default_parameter_name);
		parameter.setName(ePURE_Header.default_parameter_name);
		parameter.setUnits("substance");
		parameter.setValue(ePURE_Header.default_parameter_value);
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
