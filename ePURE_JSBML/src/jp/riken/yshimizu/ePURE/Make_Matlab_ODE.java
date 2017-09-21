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
	
	//parameters below are provided by Merge_Models class
	private static String default_parameter_name = "k1";
	private static int default_initial_conc = 1;//this is originally double
	private static int default_parameter_value = 1;//this is originally double
	private static String default_compartment_ID = "default";
	
	private SBMLReader reader;
	private Model model;
	
	private State_For_ODE_Making state;
	private Param_For_ODE_Making param;
	private Reaction_For_ODE_Making reaction;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Make_Matlab_ODE(String sbml_file_contents) {
		
		reader = new SBMLReader();
		
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
		
		StringBuilder sb =new StringBuilder();
		
		try{
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			write_function_definition(sb);
			write_argument_handling(sb);
			write_initialization(sb);
			write_ODE(sb);
			write_return_values(sb);
			
			writer.write(sb.toString());
			
			writer.flush();
			
		} catch (FileNotFoundException e) {
			System.out.println("some errors");
		} catch (IOException e) {
			System.out.println("some errors");
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
				System.out.println("some errors");
			}
		}
		
		return byte_ostream;
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void init(String sbml_file_contents){
		
		System.out.println("Initializing ...");
		System.out.println();
		
		//solve name
		/*if(xml_file_name.endsWith(".xml")){
			model_name = this.xml_file_name.substring(0, xml_file_name.length()-4);
		} else{
			model_name = this.xml_file_name;
		}*/
		
		try{
			model = reader.readSBMLFromString(sbml_file_contents).getModel();
		}catch(XMLStreamException e){
			System.out.println("some errors");
		}
		if(model.getName()!=model_name){
			model_name = model.getName();
		}
		
		//input state
		ListOf<Species> ls_species = model.getListOfSpecies();
		System.out.println("Found " + ls_species.size() + " species.");
		for(int i=0;i<ls_species.size();i++){
			state.add(ls_species.get(i).getId());
		}
		
		//input parameter
		ListOf<Reaction> ls_reaction = model.getListOfReactions();
		System.out.println("Found " + ls_reaction.size() + " reactions");
		System.out.println();
		for(int i=0;i<ls_reaction.size();i++){
			param.add(ls_reaction.get(i).getId()+"_"+default_parameter_name);
		}
		param.add(default_compartment_ID);//Last piece is from compartment, size becomes (number of reactions + 1)
		
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
	
	private void write_function_definition(StringBuilder sb){
		
		System.out.println("Write function definition ... ");
		
		sb.append("function [output] = " + model_name +"(varargin)\n");
		
		// HEADER
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% " + model_name + "\n");
		sb.append("% Generated: " + new Date().toString() + "\n");
		sb.append("% \n");
		sb.append("% [output] = " + model_name + "() => output = initial conditions in column vector\n");
		sb.append("% [output] = " + model_name + "('states') => output = state names in cell-array\n");
		sb.append("% [output] = " + model_name + "('algebraic') => output = algebraic variable names in cell-array\n");
		sb.append("% [output] = " + model_name + "('parameters') => output = parameter names in cell-array\n");
		sb.append("% [output] = " + model_name + "('parametervalues') => output = parameter values in column vector\n");
		sb.append("% [output] = " + model_name + "('reactions') => output = reaction names in cell-array\n");
		sb.append("% [output] = " + model_name + "(time,state) => output = time derivatives in column vector\n");
		sb.append("% [output] = " + model_name + "(time,state,param) => output = time derivatives in column vector\n");
		sb.append("% \n");
		sb.append("% State names and ordering:\n");
		sb.append("% \n");
		
		for (int i=0;i<state.size();i++){
			sb.append("% " + state.get_renamed(i) + ": " + state.get(i) + "\n");
		}
		
		sb.append("% \n");
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("\n");
		sb.append("% Parameter names and ordering:\n");
		sb.append("% \n");
		
		for (int i=0;i<param.size();i++){
			sb.append("% " + param.get_renamed(i) + ": " + param.get(i) +"\n");
		}
		
		sb.append("% \n");
		sb.append("% Reaction names and ordering:\n");
		sb.append("% \n");
		
		for (int i=0;i<reaction.size();i++){
			sb.append("% " + reaction.get_renamed(i) + ": " + reaction.get(i) + "\n");
		}
		
		sb.append("% \n");
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("\n");
		
		// VARIABLE DECLARATION
		sb.append("global time\n");
		sb.append("\n");
		
	}
	
	private void write_argument_handling(StringBuilder sb){
		
		System.out.println("Write argument handling ... ");
		
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% HANDLE VARIABLE INPUT ARGUMENTS\n");
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("if nargin == 0,\n");
		sb.append("\t% Return initial conditions of the state variables (and possibly algebraic variables)\n");
		sb.append("\toutput = [");
		
		int i_count = 0;
		int i_index = 0;
		
		while(i_index+1 < state.size()){
			sb.append(default_initial_conc + ", ");
			i_index++;
			i_count++;
			if(i_count == 10){
				sb.append("...\n\t\t");
				i_count = 0;
			}
		}
		sb.append(default_initial_conc + "];\n");
		
		sb.append("\toutput = output(:);\n");
		sb.append("\treturn\n");
		sb.append("elseif nargin == 1,\n");
		sb.append("\tif strcmp(varargin{1},'states'),\n");
		sb.append("\t\t% Return state names in cell-array\n");
		sb.append("\t\toutput = {");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < state.size()){
			sb.append("'" + state.get(i_index) + "', ");
			i_index++;
			i_count++;
			if(i_count == 10){
				sb.append("...\n\t\t\t");
				i_count = 0;
			}
		}
		sb.append("'" + state.get(i_index) + "'};\n");
		
		sb.append("\telseif strcmp(varargin{1},'algebraic'),\n");
		sb.append("\t\t% Return algebraic variable names in cell-array\n");
		
		// algebraic variable may be empty in the current (20131127) ePURE specification
		sb.append("\t\toutput = {};\n");
		
		// PARAMETER NAMES
		sb.append("\telseif strcmp(varargin{1},'parameters'),\n");
		sb.append("\t\t% Return parameter names in cell-array\n");
		sb.append("\t\toutput = {");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < param.size()){
			sb.append("'" + param.get(i_index) + "', ");
			i_index++;
			i_count++;
			if (i_count == 10){
				sb.append("...\n\t\t\t");
				i_count = 0;
			}
		}
		sb.append("'" + param.get(i_index) + "'};\n");
		
		// PARAMETER VALUES //
		sb.append("\telseif strcmp(varargin{1},'parametervalues'),\n");
		sb.append("\t\t% Return parameter values in column vector\n");
		sb.append("\t\toutput = [");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < param.size()){
			sb.append(default_parameter_value + ", ");
			i_index++;
			i_count++;
			if (i_count == 10){
				sb.append("...\n\t\t\t");
				i_count = 0;
			}
		}
		sb.append(default_parameter_value + "];\n");
		
		// REACTION NAMES //
		sb.append("\telseif strcmp(varargin{1},'reactions'),\n");
		sb.append("\t\t% Return reaction names in cell-array\n");
		sb.append("\t\toutput = {");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < reaction.size()){
			sb.append("'" + reaction.get(i_index) + "', ");
			i_index++;
			i_count++;
			if (i_count == 10 ){
				sb.append("...\n\t\t\t");
				i_count = 0;
			}
		}
		sb.append("'" + reaction.get(i_index) + "'};\n");
		
		sb.append("\telse\n");
		sb.append("\t\terror('Wrong input arguments! Please read the help text to the ODE file.');\n");
		sb.append("\tend\n");
		sb.append("\toutput = output(:);\n");
		sb.append("\treturn\n");
		sb.append("elseif nargin == 2,\n");
		sb.append("\ttime = varargin{1};\n");
		sb.append("\tstate = varargin{2};\n");
		sb.append("\tparam = [");
		
		i_count = 0;
		i_index = 0;
		
		while(i_index+1 < param.size()){
			sb.append(default_parameter_value + ", ");
			i_index++;
			i_count++;
			if (i_count == 10){
				sb.append("...\n\t\t");
				i_count = 0;
			}
		}
		sb.append(default_parameter_value + "];\n");
		
		sb.append("\tparam = param(:);\n");
		sb.append("elseif nargin == 3,\n");
		sb.append("\ttime = varargin{1};\n");
		sb.append("\tstate = varargin{2};\n");
		sb.append("\tif length(state) ~= " + state.size() + ",\n");
		sb.append("\t\terror('Wrong input arguments! Size of state is %d, while should be " + state.size() + ".', length(state));\n");
		sb.append("\tend\n");
		sb.append("\tstate = state(:);\n");
		sb.append("\tparam = varargin{3};\n");
		sb.append("\tif length(param) ~= " + param.size() + ",\n");
		sb.append("\t\terror('Wrong input arguments! Size of param is %d, while should be " + param.size() + ".', length(param));\n");
		sb.append("\tend\n");
		sb.append("\tparam = param(:);\n");
		sb.append("elseif nargin == 4,\n");
		sb.append("\ttime = varargin{1};\n");
		sb.append("\tstate = varargin{2};\n");
		sb.append("\tstate = state(:);\n");
		sb.append("\tparam = varargin{4};\n");
		sb.append("\tparam = param(:);\n");
		sb.append("else\n");
		sb.append("\terror('Wrong input arguments! Please read the help text to the ODE file.');\n");
		sb.append("end\n");
		sb.append("\n");
		
	}
	
	private void write_initialization(StringBuilder sb){
		
		System.out.println("Write initialization ... ");
		
		// INITIALIZE THE STATES
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% STATES\n");
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% Using state() variable. \n");
		sb.append("\n");
		
		// INITIALIZE THE PARAMETES
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% PARAMETERS\n");
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% Using param() variable. \n");
		sb.append("\n");
		
		// INITIALIZE THE REACTIONS
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% REACTION KINETICS \n");
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("react = zeros(" + reaction.size() + ",1);\n");
		
		for(int i=0;i<reaction.size();i++){
			sb.append(reaction.get_renamed(i) + " = " + get_math(i) + ";\n");
		}
		
		sb.append("\n");
		
	}
	
	private void write_ODE(StringBuilder sb){
		
		System.out.println("Write ODE ... ");
		
		// WRITE THE ODES
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% DIFFERENTIAL EQUATIONS\n");
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("output = zeros(" + state.size() + ",1);\n");
		
		System.out.println("  Calculating " + state.size() + " ODE ...");
		System.out.println();
		
		for(int i=0;i<state.size();i++){
			sb.append("output(" + (i+1) + ") = " + get_ODE(i) + ";\n");
			if((i+1)%100==0){
				System.out.println("  processed " + (i+1) + "/" + state.size() + " ...");
			}
		}
		System.out.println();
		
		sb.append("\n");
		
	}
	
	private void write_return_values(StringBuilder sb){
		
		System.out.println("Write return values ... ");
		
		// WRITE RETURN VALUES
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% RETURN VALUES\n");
		sb.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		sb.append("% STATE ODEs\n");
		sb.append("% output = state_dot;\n");
		sb.append("% return a column vector \n");
		sb.append("end\n");
		sb.append("\n");
		sb.append("\n");
		
	}

}
