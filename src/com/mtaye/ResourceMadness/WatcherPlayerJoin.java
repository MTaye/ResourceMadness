package com.mtaye.ResourceMadness;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class WatcherPlayerJoin implements Runnable {
	GamePlayer gamePlayer;

	//Watcher runs every 1/4 second
	public WatcherPlayerJoin(GamePlayer gamePlayer){
		this.gamePlayer = gamePlayer;
	}

	@Override
	public void run(){
		gamePlayer.onPlayerJoin();
	}
}