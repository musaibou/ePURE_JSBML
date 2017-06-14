package jp.riken.yshimizu.ePURE;

import java.math.BigDecimal;

public class Chronograph {
	
	/*---------------------------------------------------------
	 * private field
	---------------------------------------------------------*/
	
	private long start_time;
	private long stop_time;
	private long elapsed_time_ms;
	private double elapsed_time_s;
	
	/*---------------------------------------------------------
	 * constructor
	---------------------------------------------------------*/
	
	public Chronograph(){
		
		start_time = 0;
		stop_time = 0;
		elapsed_time_ms = 0;
		elapsed_time_s = 0;
		
	}
	
	/*---------------------------------------------------------
	 * public method
	---------------------------------------------------------*/
	
	public void start(){
		
		start_time = System.currentTimeMillis();
		stop_time = start_time;
		elapsed_time_ms = 0;
		
	}
	
	public void stop(){
		
		stop_time = System.currentTimeMillis();
		elapsed_time_ms = stop_time - start_time;
		
	}
	
	public long get_elapsed_time_ms(){
		
		return elapsed_time_ms;
		
	}
	
	public double get_elapsed_time_s(){
		
		elapsed_time_s = (double)elapsed_time_ms * 0.001;
		BigDecimal x = new BigDecimal(elapsed_time_s);
		x = x.setScale(3, BigDecimal.ROUND_HALF_UP);
		elapsed_time_s = x.doubleValue();
		return elapsed_time_s;
		
	}
	
}
