package com.mtaye.ResourceMadness;

public class Money {
	double _money = (double)0;
	
	public Money(){
	}
	
	public Money(double money){
		_money = money;
	}
	
	public double get(){
		return _money;
	}
	
	public void set(double money){
		_money = money;
		if(_money<0) _money = 0;
	}
	
	public double add(double money){
		return addSub(money, true);
	}
	
	public double sub(double money){
		return addSub(money, false);
	}
	
	private double addSub(double money, boolean positive){
		double overflow = positive?_money+money:_money-money;
		if(overflow<0){
			_money = 0;
			return -overflow;
		}
		else{
			_money = _money + money;
			return 0;
		}
	}
}
