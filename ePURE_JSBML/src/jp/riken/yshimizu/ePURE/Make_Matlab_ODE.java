package jp.riken.yshimizu.ePURE;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.*;

public class Make_Matlab_ODE {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private String model_name;
	
	private Model model;
	
	private State_For_ODE_Making state;
	private Param_For_ODE_Making param;
	private Reaction_For_ODE_Making reaction;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Make_Matlab_ODE(String sbml_file_contents) {
		
		state = new State_For_ODE_Making();
		param = new Param_For_ODE_Making();
		reaction = new Reaction_For_ODE_Making();
		
		init(sbml_file_contents);
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public ByteArrayOutputStream execute(){
		
		BufferedWriter writer = null;
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		try{
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			write_function_definition(writer);
			write_argument_handling(writer);
			write_initialization(writer);
			write_ODE(writer);
			write_return_values(writer);
			
			writer.flush();
			
		} catch (FileNotFoundException e) {
			System.out.println("Could not save ODE file.");
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Disk I/O error related to saving the ODE file.");
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				if(writer!=null){
					writer.flush();
					writer.close();
				}
				if(byte_ostream!=null){
					byte_ostream.close();
				}
			} catch (IOException e) {
				System.out.println("Disk I/O error related to saving the ODE file.");
			}
		}
		
		return byte_ostream;
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void init(String sbml_file_contents){
		
		System.out.println("ODE file will be prepared from merged SBML file...");
		System.out.println();
		
		SBMLReader reader = new SBMLReader();
		
		//solve name
		/*if(xml_file_name.endsWith(".xml")){
			model_name = this.xml_file_name.substring(0, xml_file_name.length()-4);
		} else{
			model_name = this.xml_file_name;
		}*/
		
		try{
			model = reader.readSBMLFromString(sbml_file_contents).getModel();
		}catch(XMLStreamException e){
			System.out.println("XML stream error in reading the merged SBML file.");
			e.printStackTrace();
			System.exit(0);
		}
		if(model.getName()!=model_name){
			model_name = model.getName();
		}
		
		//input state
		ListOf<Species> ls_species = model.getListOfSpecies();
		System.out.println("  Found " + ls_species.size() + " species.");
		for(int i=0;i<ls_species.size();i++){
			state.add(ls_species.get(i).getId());
		}
		
		//input parameter
		ListOf<Reaction> ls_reaction = model.getListOfReactions();
		System.out.println("  Found " + ls_reaction.size() + " reactions");
		System.out.println();
		for(int i=0;i<ls_reaction.size();i++){
			param.add(ls_reaction.get(i).getId()+"_"+ePURE_Header.default_parameter_name);
		}
		param.add(ePURE_Header.default_compartment_ID);//Last piece is from compartment, size becomes (number of reactions + 1)
		
		//input reaction
		for(int i=0;i<ls_reaction.size();i++){
			reaction.add(ls_reaction.get(i).getId());
			reaction.add_math(get_math(i));
		}
	}
	
	private String get_math(int index){
		
		Reaction r = model.getReaction(index);
		
		//ASTNode ast = r.getKineticLaw().getMath();
		ListOf<SpeciesReference> ls_reactants = r.getListOfReactants();
		
		//currently kinetic law is simple math using multiplication.
		//this is described by k1 x reactant1 or k1 x reactant1 x reactant2
		//So we here determined not to use KineticLaw class.
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(param.get_renamed(index) + " * ");
		int i;
		for(i=0;i<ls_reactants.size()-1;i++){
			sb.append(state.get_renamed(ls_reactants.get(i).getSpecies()) + " * ");
		}
		sb.append(state.get_renamed(ls_reactants.get(i).getSpecies()));
		
		/*ast.renameSIdRefs(default_parameter_name, param.get_renamed(index));
		
		for(int i=0;i<ls_reactants.size();i++){
			ast.renameSIdRefs(ls_reactants.get(i).getSpecies(), state.get_renamed(ls_reactants.get(i).getSpecies()));
		}*/
		
		return sb.toString();
		
	}
	
	private String get_ODE(int index){
		
		StringBuilder ode = new StringBuilder();
		ode.append("(");
		
		for(int i=0;i<reaction.size();i++){
			
			if(reaction.get_math(i).contains(state.get_renamed(index))){
				ode.append("-" + reaction.get_renamed(i));
			}
			
			ListOf<SpeciesReference> ls_products = model.getReaction(i).getListOfProducts();
			
			for(int j=0;j<ls_products.size();j++){
				if(ls_products.get(j).getSpecies().equals(state.get(index))){
					ode.append("+" + reaction.get_renamed(i));
				}
			}
			
		}
		
		ode.append(")/" + param.get_renamed(param.size()-1));
			
		return ode.toString();
		
	}
	
	private void write_function_definition(BufferedWriter writer) throws IOException{
		
		writer.write("function [output] = " + model_name +"(varargin)");writer.newLine();
		
		// HEADER
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% " + model_name);writer.newLine();
		writer.write("% Generated: " + new Date().toString());writer.newLine();
		writer.write("% ");writer.newLine();
		writer.write("% [output] = " + model_name + "() => output = initial conditions in column vector");writer.newLine();
		writer.write("% [output] = " + model_name + "('states') => output = state names in cell-array");writer.newLine();
		writer.write("% [output] = " + model_name + "('algebraic') => output = algebraic variable names in cell-array");writer.newLine();
		writer.write("% [output] = " + model_name + "('parameters') => output = parameter names in cell-array");writer.newLine();
		writer.write("% [output] = " + model_name + "('parametervalues') => output = parameter values in column vector");writer.newLine();
		writer.write("% [output] = " + model_name + "('reactions') => output = reaction names in cell-array");writer.newLine();
		writer.write("% [output] = " + model_name + "(time,state) => output = time derivatives in column vector");writer.newLine();
		writer.write("% [output] = " + model_name + "(time,state,param) => output = time derivatives in column vector");writer.newLine();
		writer.write("% ");writer.newLine();
		writer.write("% State names and ordering:");writer.newLine();
		writer.write("% ");writer.newLine();
		
		for (int i=0;i<state.size();i++){
			writer.write("% " + state.get_renamed(i) + ": " + state.get(i));writer.newLine();
		}
		
		writer.write("% ");writer.newLine();
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();;
		writer.newLine();
		writer.write("% Parameter names and ordering:");writer.newLine();
		writer.write("% ");writer.newLine();
		
		for (int i=0;i<param.size();i++){
			writer.write("% " + param.get_renamed(i) + ": " + param.get(i));writer.newLine();
		}
		
		writer.write("% ");writer.newLine();
		writer.write("% Reaction names and ordering:");writer.newLine();
		writer.write("% ");writer.newLine();
		
		for (int i=0;i<reaction.size();i++){
			writer.write("% " + reaction.get_renamed(i) + ": " + reaction.get(i));writer.newLine();
		}
		
		writer.write("% ");writer.newLine();
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.newLine();
		
		// VARIABLE DECLARATION
		writer.write("global time");writer.newLine();
		writer.newLine();
		
	}
	
	private void write_argument_handling(BufferedWriter writer) throws IOException{
		
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% HANDLE VARIABLE INPUT ARGUMENTS");writer.newLine();
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("if nargin == 0,");writer.newLine();
		writer.write("\t% Return initial conditions of the state variables (and possibly algebraic variables)");writer.newLine();
		writer.write("\toutput = [");
		
		int i_count = 0;
		int i_index = 0;
		
		while(i_index+1 < state.size()){
			writer.write(ePURE_Header.default_initial_conc + ", ");
			i_index++;
			i_count++;
			if(i_count == 10){
				writer.write("...");writer.newLine();writer.write("\t\t");
				i_count = 0;
			}
		}
		writer.write(ePURE_Header.default_initial_conc + "];");writer.newLine();
		
		writer.write("\toutput = output(:);");writer.newLine();
		writer.write("\treturn");writer.newLine();
		writer.write("elseif nargin == 1,");writer.newLine();
		writer.write("\tif strcmp(varargin{1},'states'),");writer.newLine();
		writer.write("\t\t% Return state names in cell-array");writer.newLine();
		writer.write("\t\toutput = {");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < state.size()){
			writer.write("'" + state.get(i_index) + "', ");
			i_index++;
			i_count++;
			if(i_count == 10){
				writer.write("...");writer.newLine();writer.write("\t\t\t");
				i_count = 0;
			}
		}
		writer.write("'" + state.get(i_index) + "'};");writer.newLine();
		
		writer.write("\telseif strcmp(varargin{1},'algebraic'),");writer.newLine();
		writer.write("\t\t% Return algebraic variable names in cell-array");writer.newLine();
		
		// algebraic variable may be empty in the current (20131127) ePURE specification
		writer.write("\t\toutput = {};");writer.newLine();
		
		// PARAMETER NAMES
		writer.write("\telseif strcmp(varargin{1},'parameters'),");writer.newLine();
		writer.write("\t\t% Return parameter names in cell-array");writer.newLine();
		writer.write("\t\toutput = {");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < param.size()){
			writer.write("'" + param.get(i_index) + "', ");
			i_index++;
			i_count++;
			if (i_count == 10){
				writer.write("...");writer.newLine();writer.write("\t\t\t");
				i_count = 0;
			}
		}
		writer.write("'" + param.get(i_index) + "'};");writer.newLine();
		
		// PARAMETER VALUES //
		writer.write("\telseif strcmp(varargin{1},'parametervalues'),");writer.newLine();
		writer.write("\t\t% Return parameter values in column vector");writer.newLine();
		writer.write("\t\toutput = [");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < param.size()){
			writer.write(ePURE_Header.default_parameter_value + ", ");
			i_index++;
			i_count++;
			if (i_count == 10){
				writer.write("...");writer.newLine();writer.write("\t\t\t");
				i_count = 0;
			}
		}
		writer.write(ePURE_Header.default_parameter_value + "];");writer.newLine();
		
		// REACTION NAMES //
		writer.write("\telseif strcmp(varargin{1},'reactions'),");writer.newLine();
		writer.write("\t\t% Return reaction names in cell-array");writer.newLine();
		writer.write("\t\toutput = {");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < reaction.size()){
			writer.write("'" + reaction.get(i_index) + "', ");
			i_index++;
			i_count++;
			if (i_count == 10 ){
				writer.write("...");writer.newLine();writer.write("\t\t\t");
				i_count = 0;
			}
		}
		writer.write("'" + reaction.get(i_index) + "'};");writer.newLine();
		
		writer.write("\telse");writer.newLine();
		writer.write("\t\terror('Wrong input arguments! Please read the help text to the ODE file.');");writer.newLine();
		writer.write("\tend");writer.newLine();
		writer.write("\toutput = output(:);");writer.newLine();
		writer.write("\treturn");writer.newLine();
		writer.write("elseif nargin == 2,");writer.newLine();
		writer.write("\ttime = varargin{1};");writer.newLine();
		writer.write("\tstate = varargin{2};");writer.newLine();
		writer.write("\tparam = [");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < param.size()){
			writer.write(ePURE_Header.default_parameter_value + ", ");
			i_index++;
			i_count++;
			if (i_count == 10){
				writer.write("...");writer.newLine();writer.write("\t\t");
				i_count = 0;
			}
		}
		writer.write(ePURE_Header.default_parameter_value + "];");writer.newLine();
		
		writer.write("\tparam = param(:);");writer.newLine();
		writer.write("elseif nargin == 3,");writer.newLine();
		writer.write("\ttime = varargin{1};");writer.newLine();
		writer.write("\tstate = varargin{2};");writer.newLine();
		writer.write("\tif length(state) ~= " + state.size() + ",");writer.newLine();
		writer.write("\t\terror('Wrong input arguments! Size of state is %d, while should be " + state.size() + ".', length(state));");writer.newLine();
		writer.write("\tend");writer.newLine();
		writer.write("\tstate = state(:);");writer.newLine();
		writer.write("\tparam = varargin{3};");writer.newLine();
		writer.write("\tif length(param) ~= " + param.size() + ",");writer.newLine();
		writer.write("\t\terror('Wrong input arguments! Size of param is %d, while should be " + param.size() + ".', length(param));");writer.newLine();
		writer.write("\tend");writer.newLine();
		writer.write("\tparam = param(:);");writer.newLine();
		writer.write("elseif nargin == 4,");writer.newLine();
		writer.write("\ttime = varargin{1};");writer.newLine();
		writer.write("\tstate = varargin{2};");writer.newLine();
		writer.write("\tstate = state(:);");writer.newLine();
		writer.write("\tparam = varargin{4};");writer.newLine();
		writer.write("\tparam = param(:);");writer.newLine();
		writer.write("else");writer.newLine();
		writer.write("\terror('Wrong input arguments! Please read the help text to the ODE file.');");writer.newLine();
		writer.write("end");writer.newLine();
		writer.newLine();
		
	}
	
	private void write_initialization(BufferedWriter writer) throws IOException{
		
		// INITIALIZE THE STATES
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% STATES");writer.newLine();
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% Using state() variable. ");writer.newLine();
		writer.newLine();
		
		// INITIALIZE THE PARAMETES
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% PARAMETERS");writer.newLine();
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% Using param() variable. ");writer.newLine();
		writer.newLine();
		
		// INITIALIZE THE REACTIONS
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% REACTION KINETICS ");writer.newLine();
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("react = zeros(" + reaction.size() + ",1);");writer.newLine();
		
		for(int i=0;i<reaction.size();i++){
			writer.write(reaction.get_renamed(i) + " = " + get_math(i) + ";");writer.newLine();
		}
		
		writer.newLine();
		
	}
	
	private void write_ODE(BufferedWriter writer) throws IOException{
		
		// WRITE THE ODES
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% DIFFERENTIAL EQUATIONS");writer.newLine();
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("output = zeros(" + state.size() + ",1);");writer.newLine();
		
		System.out.println("  Calculating " + state.size() + " ODE ...");
		System.out.println();
		
		for(int i=0;i<state.size();i++){
			writer.write("output(" + (i+1) + ") = " + get_ODE(i) + ";");writer.newLine();
			if((i+1)%100==0){
				System.out.println("  Processed " + (i+1) + "/" + state.size() + " ...");
			}
		}
		System.out.println();
		
		writer.newLine();
		
	}
	
	private void write_return_values(BufferedWriter writer) throws IOException{
		
		// WRITE RETURN VALUES
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% RETURN VALUES");writer.newLine();
		writer.write("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");writer.newLine();
		writer.write("% STATE ODEs");writer.newLine();
		writer.write("% output = state_dot;");writer.newLine();
		writer.write("% return a column vector ");writer.newLine();
		writer.write("end");writer.newLine();
		writer.newLine();
		writer.newLine();
		
	}

}
