package com.mtaye.ResourceMadness;

import com.mtaye.ResourceMadness.Game.FilterType;

public class RequestMoney {
	private Double _money = null;
	private FilterType _filterType = null;
	
	public RequestMoney(FilterType filterType, Double money){
		_money = money;
		_filterType = filterType;
	}
	
	public Double getMoney(){
		return _money;
	}
	public FilterType getFilterType(){
		return _filterType;
	}
	
	public void clear(){
		_money = null;
		_filterType = null;
	}
}
