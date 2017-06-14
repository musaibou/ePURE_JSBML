package jp.riken.yshimizu.ePURE;

import java.util.ArrayList;

public class Reaction_For_ODE_Making {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private ArrayList<String> array_reaction;
	private ArrayList<String> array_math;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Reaction_For_ODE_Making(){
		
		array_reaction = new ArrayList<String>();
		array_math = new ArrayList<String>();
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void add(String str){
		
		array_reaction.add(str);
		
	}
	
	public String get(int i){
		
		return array_reaction.get(i);
		
	}
	
	public String get_renamed(int i){
		
		return "react(" + (i+1) + ")";
		
	}
	
	public int size(){
		
		return array_reaction.size();
		
	}
	
	public void add_math(String str){
		
		array_math.add(str);
		
	}
	
	public String get_math(int i){
		
		return array_math.get(i);
		
	}
	
}
