package jp.riken.yshimizu.ePURE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Simulation_Files_Generator {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private String output_directory;
	private String project_name;
	private String original_zip_file_name;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Simulation_Files_Generator(ePURE_Project_Summary summary) {
		
		this.output_directory = summary.get_output_directory();
		this.project_name = summary.get_project_name();
		this.original_zip_file_name = summary.get_zipped_SBML_files_name();
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void execute(){
		
		//from here, outputs simulation files then they are zipped
		String xml_file_name;
		byte[] xml_file_contents;
		String m_file_name;
		byte[] m_file_contents;
		String initial_value_csv_name;
		byte[] initial_value_csv_contents;
		String parameters_csv_name;
		byte[] parameters_csv_contents;
		String reactions_csv_name;
		byte[] reactions_csv_contents;
		String sample_code_name;
		byte[] sample_code_contents;
		
		//merges SBML models
		ZipFile zip_file = null;
		try {
			zip_file = new ZipFile(output_directory + original_zip_file_name);
		} catch (IOException e) {
			System.out.println("some erros");
		}
		ByteArrayOutputStream xml_byte_ostream = new Merge_Models(project_name, zip_file, 2, 4).execute();
		xml_file_contents = xml_byte_ostream.toByteArray();
		xml_file_name = "model/" + project_name + ".xml";
		
		//make matlab ODE
		ByteArrayOutputStream m_byte_ostream = new Make_Matlab_ODE(xml_byte_ostream.toString()).execute();
		m_file_contents = m_byte_ostream.toByteArray();
		m_file_name = project_name + ".m";
		
		//make CSV files
		ArrayList<byte[]> array = new Dat_File_Generator(project_name, xml_byte_ostream.toString()).execute();
		initial_value_csv_name = "dat/" + project_name + "_initial_values.csv";
		initial_value_csv_contents = array.get(0);
		parameters_csv_name = "dat/" + project_name + "_parameters.csv";
		parameters_csv_contents = array.get(1);
		reactions_csv_name = "dat/" + project_name + "_reactions.csv";
		reactions_csv_contents = array.get(2);
		
		//make sample code
		sample_code_name = project_name + "_Sample.m";
		sample_code_contents = new Sample_Code_Generator(project_name).execute();
		
		//save data
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		ZipOutputStream zip_ostream = new ZipOutputStream(byte_ostream);
		FileOutputStream file_ostream = null;
		ZipEntry entry = null;
		
		try{
			
			//matlab ODE
			entry = new ZipEntry(m_file_name);
			entry.setMethod(ZipOutputStream.DEFLATED);
			zip_ostream.putNextEntry(entry);
			zip_ostream.write(m_file_contents);
			zip_ostream.closeEntry();
			
			//sample code
			entry = new ZipEntry(sample_code_name);
			entry.setMethod(ZipOutputStream.DEFLATED);
			zip_ostream.putNextEntry(entry);
			zip_ostream.write(sample_code_contents);
			zip_ostream.closeEntry();
			
			//merged model
			entry = new ZipEntry(xml_file_name);
			entry.setMethod(ZipOutputStream.DEFLATED);
			zip_ostream.putNextEntry(entry);
			zip_ostream.write(xml_file_contents);
			zip_ostream.closeEntry();
			
			//csv
			entry = new ZipEntry(initial_value_csv_name);
			entry.setMethod(ZipOutputStream.DEFLATED);
			zip_ostream.putNextEntry(entry);
			zip_ostream.write(initial_value_csv_contents);
			zip_ostream.closeEntry();
			
			entry = new ZipEntry(parameters_csv_name);
			entry.setMethod(ZipOutputStream.DEFLATED);
			zip_ostream.putNextEntry(entry);
			zip_ostream.write(parameters_csv_contents);
			zip_ostream.closeEntry();
			
			entry = new ZipEntry(reactions_csv_name);
			entry.setMethod(ZipOutputStream.DEFLATED);
			zip_ostream.putNextEntry(entry);
			zip_ostream.write(reactions_csv_contents);
			zip_ostream.closeEntry();
			
			//finalizing
			zip_ostream.finish();
			byte_ostream.close();
			File out_zip_file = new File(output_directory, project_name + "_Simulate.zip");
			file_ostream = new FileOutputStream(out_zip_file);
			byte[] stream = byte_ostream.toByteArray();
			file_ostream.write(stream);
			file_ostream.flush();
			file_ostream.close();
		}catch(IOException e){
			System.out.println("some errors");
		}finally{
			try{
				if(byte_ostream!=null){
					byte_ostream.close();
				}
				if(zip_ostream!=null){
					zip_ostream.close();
				}
				if(file_ostream!=null){
					file_ostream.close();
				}
			}catch(IOException e){
				System.out.println("some errors");
			}
		}
	}
}
