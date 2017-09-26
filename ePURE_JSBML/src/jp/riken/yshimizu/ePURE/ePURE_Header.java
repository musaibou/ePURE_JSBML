package jp.riken.yshimizu.ePURE;

public final class ePURE_Header {
	
	static final int SBML_level = 2;
	static final int SBML_version = 4;
	
	static final String default_output_directory = "./";
	static final String default_conf_file = "./BaseFile/default_ePURE.conf";
	
	static final String base_Aminoacylation_A = "./BaseFile/Aminoacylation_A_20121220v02.xml";
	static final String base_Aminoacylation_B = "./BaseFile/Aminoacylation_B_20121220v03.xml";
	static final String base_Elongation_A = "./BaseFile/Elongation_A_20121116v04.xml";
	static final String base_Elongation_B = "./BaseFile/Elongation_B_20121116v03.xml";
	static final String base_EnergyRegeneration_A = "./BaseFile/EnergyRegeneration_A_20121015v03.xml";
	static final String base_EnergyRegeneration_B = "./BaseFile/EnergyRegeneration_B_20121016v05.xml";
	static final String base_EnergyRegeneration_C = "./BaseFile/EnergyRegeneration_C_20121016v05.xml";
	static final String base_EnergyRegeneration_D = "./BaseFile/EnergyRegeneration_D_20130501v03.xml";
	static final String base_FMet_tRNASynthesis = "./BaseFile/FMet_tRNASynthesis_20121015v08.xml";
	static final String base_Initiation_A = "./BaseFile/Initiation_A_20121016v04.xml";
	static final String base_Initiation_B1 = "./BaseFile/Initiation_B1_20121016v24.xml";
	static final String base_Initiation_B2 = "./BaseFile/Initiation_B2_20131105v02.xml";
	static final String base_SmallMolecules = "./BaseFile/SmallMolecules_20130222v03.xml";
	static final String base_Initiation_C = "./BaseFile/Initiation_C_20121016v08.xml";
	static final String base_Termination_A = "./BaseFile/Termination_A_20121220v04.xml";
	static final String base_Termination_B = "./BaseFile/Termination_B_20121225v05.xml";
	static final String base_Termination_C = "./BaseFile/Termination_C_20121226v11.xml";
	static final String base_Elongation_Ca1 = "./BaseFile/Elongation_Ca1_20130624v02.xml";
	static final String base_Elongation_Ca2 = "./BaseFile/Elongation_Ca2_20130624v02.xml";
	
	static final String default_parameter_name = "k1";
	static final double default_initial_conc = 1;
	static final double default_parameter_value = 1;
	static final String default_compartment_ID = "default";
	
	static final String default_initial_values_file = "./BaseFile/default_initial_values.csv";
	static final String default_parameters_file = "./BaseFile/default_parameters.csv";
	
}
