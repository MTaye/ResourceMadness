package com.mtaye.ResourceMadness;

public class RMStats {
	private static int _serverWins = 0;
	private static int _serverLosses = 0;
	private static int _serverTimesPlayed = 0;
	private static int _serverItemsFound = 0;
	private static int _serverItemsFoundTotal = 0;
	
	private int _wins = 0;
	private int _losses = 0;
	private int _timesPlayed = 0;
	private int _itemsFound = 0;
	private int _itemsFoundTotal = 0;
	
	public RMStats(){
	}
	
	//Get
	public int getWins(){
		return _wins;
	}
	public int getLosses(){
		return _losses;
	}
	public int getTimesPlayed(){
		return _timesPlayed;
	}
	public int getItemsFound(){
		return _itemsFound;
	}
	public int getItemsFoundTotal(){
		return _itemsFoundTotal;
	}
	//Get Ratio
	public String getTextRatio(){
		return getWins()+":"+getLosses();
	}
	
	//Set
	public void setWins(int value){
		if(value==-1) return;
		_wins = value;
	}
	public void setLosses(int value){
		if(value==-1) return;
		_losses = value;
	}
	public void setTimesPlayed(int value){
		if(value==-1) return;
		_timesPlayed = value;
	}
	public void setItemsFound(int value){
		if(value==-1) return;
		_itemsFound = value;
	}
	public void setItemsFoundTotal(int value){
		if(value==-1) return;
		_itemsFoundTotal = value;
	}
	
	//Add
	public void addWins(){
		_wins++;
	}
	public void addLosses(){
		_losses++;
	}
	public void addTimesPlayed(){
		_timesPlayed++;
	}
	public void addItemsFound(){
		_itemsFound++;
	}
	public void addItemsFoundTotal(){
		_itemsFoundTotal++;
	}
	public void addItemsFoundTotal(int value){
		if(value<0) value = 0;
		_itemsFoundTotal+=value;
	}
	
	//Clear
	public void clearWins(){
		_wins = 0;
	}
	public void clearLosses(){
		_losses = 0;
	}
	public void clearTimesPlayed(){
		_timesPlayed = 0;
	}
	public void clearItemsFound(){
		_itemsFound = 0;
	}
	public void clearItemsFoundTotal(){
		_itemsFoundTotal = 0;
	}
	
	//Static
	
	//Get
	public static int getServerWins(){
		return _serverWins;
	}
	public static int getServerLosses(){
		return _serverLosses;
	}
	public static int getServerTimesPlayed(){
		return _serverTimesPlayed;
	}
	public static int getServerItemsFound(){
		return _serverItemsFound;
	}
	public static int getServerItemsFoundTotal(){
		return _serverItemsFoundTotal;
	}
	//Get Ratio
	public String getServerTextRatio(){
		return getServerWins()+":"+getServerLosses();
	}
	
	//Set
	public static void setServerWins(int value){
		if(value==-1) return;
		_serverWins = value;
	}
	public static void setServerLosses(int value){
		if(value==-1) return;
		_serverLosses = value;
	}
	public static void setServerTimesPlayed(int value){
		if(value==-1) return;
		_serverTimesPlayed = value;
	}
	public static void setServerItemsFound(int value){
		if(value==-1) return;
		_serverItemsFound = value;
	}
	public static void setServerItemsFoundTotal(int value){
		if(value==-1) return;
		_serverItemsFoundTotal = value;
	}
	
	//Add
	public static void addServerWins(){
		_serverWins++;
	}
	public static void addServerLosses(){
		_serverLosses++;
	}
	public static void addServerTimesPlayed(){
		_serverTimesPlayed++;
	}
	public static void addServerItemsFound(){
		_serverItemsFound++;
	}
	public static void addServerItemsFoundTotal(){
		_serverItemsFoundTotal++;
	}
	public static void addServerItemsFoundTotal(int value){
		if(value<0) value = 0;
		_serverItemsFoundTotal++;
	}
	
	//Clear
	public static void clearServerWins(){
		_serverWins = 0;
	}
	public static void clearServerLosses(){
		_serverLosses = 0;
	}
	public static void clearServerTimesPlayed(){
		_serverTimesPlayed = 0;
	}
	public static void clearServerItemsFound(){
		_serverItemsFound = 0;
	}
	public static void clearServerItemsFoundTotal(){
		_serverItemsFoundTotal = 0;
	}
}