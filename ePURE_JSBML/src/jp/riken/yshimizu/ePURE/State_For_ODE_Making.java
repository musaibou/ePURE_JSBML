package jp.riken.yshimizu.ePURE;

import java.util.ArrayList;

public class State_For_ODE_Making {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private ArrayList<String> array_state;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public State_For_ODE_Making(){
		
		array_state = new ArrayList<>();
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void add(String str){
		
		array_state.add(str);
		
	}
	
	public String get(int i){
		
		return array_state.get(i);
		
	}
	
	public String get_renamed(String str){
		
		int i = array_state.indexOf(str);
		return "state(" + (i+1) + ")";
		
	}
	
	public String get_renamed(int i){
		
		return "state(" + (i+1) + ")";
		
	}
	
	public int size(){
		
		return array_state.size();
		
	}
	
}
