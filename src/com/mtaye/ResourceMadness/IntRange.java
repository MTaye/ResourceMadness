package com.mtaye.ResourceMadness;

import com.mtaye.ResourceMadness.helper.Helper;

public class IntRange {
	private int low = -1;
	private int high = -1;
	
	public IntRange(){
	}
	
	public IntRange(String arg){
		Debug.warning("new IntegerRange("+arg+")");
		if(arg==null) return;
		String[] args = arg.split("-");
		if(args.length==0) return;
		if(args.length>0){
			int num = Helper.getIntByString(args[0]);
			if(num==-1) return;
			this.low = num;
			Debug.warning("Found low: "+low);
		}
		if(args.length>1){
			int num = Helper.getIntByString(args[1]);
			if(num==-1) return;
			this.high = num;
			Debug.warning("Found high: "+high);
		}
		normalize();
	}
	
	public IntRange(int low){
		this(low, -1);
	}
	
	public IntRange(int low, int high){
		this.low = low;
		this.high = high;
	}
	
	public IntRange(IntRange integerRange){
		this(integerRange.getLow(), integerRange.getHigh());
	}
	
	public void set(IntRange integerRange){
		setLow(integerRange.getLow());
		setHigh(integerRange.getHigh());
	}
	
	public void clear(){
		low = 0;
		high = -1;
	}
	
	public void setLow(int low){
		this.low = new Integer(low);
	}
	
	public int getLow(){
		return low;
	}
	
	public boolean hasLow(){
		if(low!=-1) return true;
		return false;
	}
	
	public void setHigh(int high){
		this.high = new Integer(high);
	}
	
	public int getHigh(){
		return high;
	}
	
	public boolean hasHigh(){
		if(high!=-1) return true;
		return false;
	}
	
	public void normalize(){
		if(!hasHigh()) return;
		if(high<low){
			int temp = low;
			low = new Integer(high);
			high = new Integer(temp);
		}
		if(high==low) high=-1;
	}
	
	public IntRange clone(){
		return new IntRange(getLow(), getHigh());
	}
	
	@Override
	public String toString(){
		return (hasLow()?""+low:"")+(hasHigh()?"-"+high:"");
	}
}
