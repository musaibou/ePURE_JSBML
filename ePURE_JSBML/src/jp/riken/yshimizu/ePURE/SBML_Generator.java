package jp.riken.yshimizu.ePURE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SBML_Generator {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private ePURE_Project epure;
	
	private String output_directory;
	private String zipped_SBML_files_name;
	
	private TreeMap<String, byte[]> byte_stream_map;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public SBML_Generator(ePURE_Project epure) {
		
		this.epure = epure;
		this.output_directory = epure.get_output_directory();
		this.zipped_SBML_files_name = epure.get_zipped_SBML_files_name();
		
		byte_stream_map = new TreeMap<>();
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void execute(){
		
		System.out.println("Making SBML files...");
		System.out.println();
		
		//sequence independent SBMLs
		
		new Sequence_Independent_SBML_Generator(epure, byte_stream_map).execute();
		
		//sequence dependent SBMLs
		
		new Sequence_Dependent_SBML_Generator(epure, byte_stream_map).execute();
		
		//save zip file
		ByteArrayOutputStream byte_ostream = new ByteArrayOutputStream();
		
		ZipOutputStream zip_ostream = new ZipOutputStream(byte_ostream);
		FileOutputStream file_ostream = null;
		
		try{
			for(String key:byte_stream_map.keySet()){
				
				ZipEntry entry = new ZipEntry(key);
				entry.setMethod(ZipOutputStream.DEFLATED);
				zip_ostream.putNextEntry(entry);
				zip_ostream.write(byte_stream_map.get(key));
				zip_ostream.closeEntry();
				
			}
			zip_ostream.finish();
			byte_ostream.close();
			File zip_file = new File(output_directory, zipped_SBML_files_name);
			file_ostream = new FileOutputStream(zip_file);
			byte[] stream = byte_ostream.toByteArray();
			file_ostream.write(stream);
			file_ostream.flush();
			file_ostream.close();
		}catch(IOException e){
			System.out.println("Disk I/O error related to saving SBML files as a zip file.");
			e.printStackTrace();
			System.exit(0);
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
				System.out.println("Disk I/O error related to saving SBML files as a zip file.");
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		System.out.println();
		System.out.println("  Zipped " + byte_stream_map.size() + " files into " + zipped_SBML_files_name);
		System.out.println();
		
	}
	
}
