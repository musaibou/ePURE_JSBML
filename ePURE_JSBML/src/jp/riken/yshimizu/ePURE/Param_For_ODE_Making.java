package jp.riken.yshimizu.ePURE;

import java.util.ArrayList;

public class Param_For_ODE_Making {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private ArrayList<String> array_param;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Param_For_ODE_Making(){
		
		array_param = new ArrayList<>();
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void add(String str){
		
		array_param.add(str);
		
	}
	
	public String get(int i){
		
		return array_param.get(i);
		
	}
	
	public String get_renamed(int i){
		
		return "param(" + (i+1) + ")";
		
	}
	
	public int size(){
		
		return array_param.size();
		
	}

}
