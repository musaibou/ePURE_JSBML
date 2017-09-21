package jp.riken.yshimizu.ePURE;

public class ePURE_JSBML_Main {
	
	/*---------------------------------------------------------
	 * main method
	---------------------------------------------------------*/
	
	public static void main(String[] args) {
		
		ePURE_JSBML_Main main = new ePURE_JSBML_Main();
		
		Chronograph cg = new Chronograph();
		cg.start();
		
		/*
		switch(args.length){
		case 2:
			if(args[0].toLowerCase().equals("-o")){
				cg.start();
				Make_Matlab_ODE mmo = new Make_Matlab_ODE(args[1]);
				mmo.execute();
				System.out.println("Safely completed!");
				System.out.println();
				cg.stop();
				System.out.println("It took " + cg.get_elapsed_time_s() + " s");
				System.exit(0);
			}
		case 3:
			if(args[0].toLowerCase().equals("-m")){
				cg.start();
				main = new ePURE_Main();
				main.merge_model(args[2], args[1]);
				System.out.println("Safely completed!");
				System.out.println();
				cg.stop();
				System.out.println("It took " + cg.get_elapsed_time_s() + " s");
				System.exit(0);
			}
		default:
			System.out.println("Usage: merge model: java -jar ePURE_Main.jar -m <merged_model> <module_directory>");
			System.out.println("Usage: make Matlab ODE: java -jar ePURE_Main.jar -o <sbml_file>");
			System.exit(0);
		}*/
		
		String output_directory = "./";
		String conf_file = "./ePURE2.conf";
		
		String project_name = "MGG_PNAS";
		String rna_seq = "AUGGGUGGUUAA";
		
		//String project_name = "project_20aa";
		//String rna_seq = "AUGGCUUGUGACGAGUUUGGUCACAUUAAGCUGAUGAACCCUCAGCGUUCUACUGUUUGGUACUAA";
		
		//String project_name = "GFP";
		//String rna_seq = "AUGAGUAAAGGAGAAGAACUUUUCACUGGAGUUGUCCCAAUUCUUGUUGAAUUAGAUGGUGAUGUUAAUGGGCACAAAUUUUCUGUCAGCGGAGAGGGUGAAGGUGAUGCAACAUACGGAAAACUUACCCUUAAAUUUAUUUGCACUACUGGAAAACUACCUGUUCCAUGGCCAACACUUGUCACUACUCUGACGUAUGGUGUUCAAUGCUUUUCCCGUUAUCCGGAUCACAUGAAACGGCAUGACUUUUUCAAGAGUGCCAUGCCCGAAGGUUAUGUACAGGAACGCACUAUAUCUUUCAAAGAUGACGGGAACUACAAGACGCGUGCUGAAGUCAAGUUUGAAGGUGAUACCCUUGUUAAUCGUAUCGAGUUAAAAGGUAUUGAUUUUAAAGAAGAUGGAAACAUUCUCGGACACAAACUCGAGUACAACUAUAACUCACACAAUGUAUACAUCACGGCAGACAAACAAAAGAAUGGAAUCAAAGCUAACUUCAAAACUCGCCACAACAUUGAAGAUGGCUCCGUUCAACUAGCAGACCAUUAUCAGCAAAAUACUCCAAUUGGCGAUGGCCCUGUCCUUUUACCAGACAACCAUUACCUGUCGACACAAUCUGCCCUUUUGAAAGAUCCCAACGAAAAGCGUGACCACAUGGUCCUUCUUGAGUUUGUAACUGCUGCUGGGAUUACACAUGGCAUGGAUGAGCUCUACAAAUAA";
				
		ePURE_Project_Summary summary = new ePURE_Project_Summary(project_name, rna_seq, output_directory, conf_file);
		
		//outputs zip file from input sequences
		new SBML_Generator(summary).execute();
		
		//outputs zip file for simulation
		new Simulation_Files_Generator(summary).execute();
		
		System.out.println("completed!");
		System.out.println();
		cg.stop();
		System.out.println("It took " + cg.get_elapsed_time_s() + " s");
		
	}
	
}