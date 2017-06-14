package jp.riken.yshimizu.ePURE;

import java.io.File;

public class ePURE_Main {
	
	/*---------------------------------------------------------
	 * field
	---------------------------------------------------------*/
	
	private static ePURE_Main main;
	
	/*---------------------------------------------------------
	 * main method
	---------------------------------------------------------*/
	
	public static void main(String[] args) {
		
		Chronograph cg = new Chronograph();
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
		
		cg.start();
		
		main = new ePURE_Main();
		
		//main.merge_model("./polyAla/", "test_2017");
		//main.merge_model("./model/", "merged_model_2017");
		//Make_Matlab_ODE mmo = new Make_Matlab_ODE("merged_model_2017.xml");
		Make_Matlab_ODE mmo = new Make_Matlab_ODE("MGG20131122_MassAction.xml");
		//Make_Matlab_ODE mmo = new Make_Matlab_ODE("./polyAla/polyAla20140110.xml");
		mmo.execute();
		main.check_memory();
		System.out.println("Safely completed!");
		System.out.println();
		cg.stop();
		System.out.println("It took " + cg.get_elapsed_time_s() + " s");
		
	}
	
	/*---------------------------------------------------------
	 * private method
	---------------------------------------------------------*/
	
	private void merge_model(String dir, String output){
		
		File file = new File(dir);
		File files[] = file.listFiles();
		String[] file_names = new String[files.length];
		
		for (int i=0; i<files.length; i++) {
			file_names[i]=files[i].toString();
		}
		
		Merge_Models mm = new Merge_Models(output, file_names, 2, 4);
		if(mm.execute() != true){
			System.out.println(mm.get_error_result());
		}
		
	}
	
	private void check_memory(){
		
		Runtime runtime = Runtime.getRuntime();
		System.out.println("TotalMemory: " + runtime.totalMemory() + " bytes.");
		System.out.println("FreeMemory:  " + runtime.freeMemory() + " bytes.");
		runtime = Runtime.getRuntime();
		long usedmemory = runtime.totalMemory() - runtime.freeMemory();
		System.out.println("Used Memory: " + usedmemory + " bytes.");
		
	}

}