package com.mtaye.ResourceMadness;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMWatcher implements Runnable {
	private RM plugin;
	
	public RMWatcher(RM plugin){
		this.plugin = plugin;
	}

	@Override
	public void run() {
		try{
			for(RMGame rmGame : RMGame.getGames()){
				rmGame.update();
			}
		} catch(Exception e){
			System.out.println("Warning! An error occured!");
			e.printStackTrace();
		}
	}
}
