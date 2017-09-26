package jp.riken.yshimizu.ePURE;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Sample_Code_Generator {
	
	String xml_file_contents;
	String project_name;
	
	public Sample_Code_Generator(String project_name){
		
		this.project_name = project_name;
		
	}
	
	public byte[] execute(){
		
		BufferedWriter writer = null;
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		try{
			writer = new BufferedWriter(new OutputStreamWriter(byte_ostream));
			
			writer.write("%% Output initial value set to the model originally");writer.newLine();
			writer.newLine();
			writer.write("initial_values = " + project_name + "();");writer.newLine();
			writer.write("species_names = " + project_name + "('states');");writer.newLine();
			writer.write("parameter_names = " + project_name + "('parameters');");writer.newLine();
			writer.write("parameter_values = " + project_name + "('parametervalues');");writer.newLine();
			writer.newLine();
			writer.write("%% Read parameter values from csv");writer.newLine();
			writer.newLine();
			writer.write("data_dir = './dat/';");writer.newLine();
			writer.write("parameter_file = '"+ project_name + "_parameters.csv';");writer.newLine();
			writer.newLine();
			writer.write("parameter_data = importdata([data_dir parameter_file], ',', 1);");writer.newLine();
			writer.write("parameter_values = parameter_data.data;");writer.newLine();
			writer.newLine();
			writer.write("%% Read initial values from from csv");writer.newLine();
			writer.newLine();
			writer.write("data_dir = './dat/';");writer.newLine();
			writer.write("initial_values_file = '" + project_name + "_initial_values.csv';");writer.newLine();
			writer.newLine();
			writer.write("initial_values_data = importdata([data_dir initial_values_file], ',', 1);");writer.newLine();
			writer.write("initial_values = initial_values_data.data;");writer.newLine();
			writer.newLine();
			writer.write("%% ODE setting");writer.newLine();
			writer.newLine();
			writer.write("t=logspace(-4,3,200); %200 time points between 1e-5 and 1e3 sec");writer.newLine();
			writer.write("non_negative = 1:length(initial_values);");writer.newLine();
			writer.write("ode_opt = odeset('NonNegative', non_negative,'RelTol', 1e-3, 'AbsTol',1e-9);");writer.newLine();
			writer.newLine();
			writer.write("%% simulation");writer.newLine();
			writer.newLine();
			writer.write("model_h = @(t,x)" + project_name + "(t, x, parameter_values(:,1));");writer.newLine();
			writer.write("[t x] = ode15s(model_h, t,initial_values(:,1), ode_opt);");writer.newLine(); 
			writer.write("%% Plot");writer.newLine();
			writer.newLine();
			writer.write("figure();");writer.newLine();
			writer.write("loglog(t,x);");writer.newLine();
			writer.write("axis([10^(-4) 10^3 10^(-10) 10^5]);");writer.newLine(); 
			
			writer.flush();
			
		} catch (IOException e) {
			System.out.println("Disk I/O error related to making the sample m file.");
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
				System.out.println("Disk I/O error related to making the sample m file.");
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		return byte_ostream.toByteArray();
		
	}

}
