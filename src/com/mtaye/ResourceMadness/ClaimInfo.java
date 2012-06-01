package com.mtaye.ResourceMadness;

import com.mtaye.ResourceMadness.Game.HandleState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class ClaimInfo {
	private Stash _stash;
	private HandleState _handleState;
	
	public ClaimInfo(){
	}
	
	public ClaimInfo(Stash rmStash){
		this._stash = rmStash;
	}
	
	public ClaimInfo(HandleState handleState){
		this._handleState = handleState;
	}
	
	public ClaimInfo(Stash rmStash, HandleState handleState){
		this._stash = rmStash;
		this._handleState = handleState;
	}
	
	public Stash getStash(){
		return _stash;
	}
	
	public void setStash(Stash stash){
		_stash = stash;
	}
	
	public HandleState getHandleState(){
		return _handleState;
	}
	
	public void setHandleState(HandleState handleState){
		_handleState = handleState;
	}
}
