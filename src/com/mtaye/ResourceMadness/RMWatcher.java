package com.mtaye.ResourceMadness;

import java.util.logging.Level;

/**
 * ResourceMadness for Bukkit
 *
 * @author M-Taye
 */
public class RMWatcher implements Runnable {
	private RM plugin;
	private boolean autoSave = false;
	private int autoSaveTimer = 0;
	//Watcher runs every 1/4 second
	public RMWatcher(RM plugin){
		this.plugin = plugin;
		autoSaveTimer = 60*plugin.config.getAutoSave();
		if(autoSaveTimer>0) autoSave = true;
	}

	@Override
	public void run(){
		if(autoSave){
			if(autoSaveTimer>1) autoSaveTimer-=1;
			else{
				autoSaveTimer = 60*plugin.config.getAutoSave();
				plugin.saveAllBackup();
			}
		}
		try{
			for(RMGame rmGame : RMGame.getGames().values()){
				rmGame.update();
			}
		}
		catch (Exception e){
			plugin.log.log(Level.INFO, "Warning! An error occured!");
			e.printStackTrace();
		}
	}
}