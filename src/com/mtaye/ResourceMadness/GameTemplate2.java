package com.mtaye.ResourceMadness;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class GameTemplate2 implements Cloneable{
	private String _name;
	private GameSettings _gameSettings;
	
	public GameTemplate2(String name){
		this._name = name;
	}
	
	public GameTemplate2(String name, GameSettings gameSettings){
		this._name = name;
		this._gameSettings = gameSettings.clone();
	}
	
	public GameTemplate2(GameTemplate2 rmTemplate){
		this(rmTemplate._name, rmTemplate._gameSettings);
	}
	
	//Get
	public String getName() { return _name; }

	public GameSettings getGameSettings() { return _gameSettings; }
	
	//Set
	public void setName(String name){
		_name = name;
	}
	
	public void setGameSetings(GameSettings gameSettings){
		_gameSettings = gameSettings;
	}
	
	public boolean isEmpty(){
		//if(_filter.size()+_reward.size()+_tools.size()==0) return true;
		return false;
	}
}