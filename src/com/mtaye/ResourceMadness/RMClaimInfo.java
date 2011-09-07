package com.mtaye.ResourceMadness;

import com.mtaye.ResourceMadness.RMGame.HandleState;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMClaimInfo {
	private RMStash _stash;
	private HandleState _handleState;
	
	public RMClaimInfo(){
	}
	
	public RMClaimInfo(RMStash rmStash){
		this._stash = rmStash;
	}
	
	public RMClaimInfo(HandleState handleState){
		this._handleState = handleState;
	}
	
	public RMClaimInfo(RMStash rmStash, HandleState handleState){
		this._stash = rmStash;
		this._handleState = handleState;
	}
	
	public RMStash getStash(){
		return _stash;
	}
	
	public void setStash(RMStash stash){
		_stash = stash;
	}
	
	public HandleState getHandleState(){
		return _handleState;
	}
	
	public void setHandleState(HandleState handleState){
		_handleState = handleState;
	}
}
