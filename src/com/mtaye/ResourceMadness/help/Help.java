package com.mtaye.ResourceMadness.help;

import org.bukkit.ChatColor;

import com.mtaye.ResourceMadness.RM;
import com.mtaye.ResourceMadness.RMGame;
import com.mtaye.ResourceMadness.RMPlayer;
import com.mtaye.ResourceMadness.RMText;
import com.mtaye.ResourceMadness.Helper.RMTextHelper;
import com.mtaye.ResourceMadness.setting.Setting;
import com.mtaye.ResourceMadness.setting.SettingBool;
import com.mtaye.ResourceMadness.setting.SettingInt;
import com.mtaye.ResourceMadness.setting.SettingPrototype;
import com.mtaye.ResourceMadness.setting.SettingStr;

public class Help {
	private RM rm;
	
	public Help(RM plugin){
		rm = plugin;
	}
	
	public void setInfo(RMPlayer rmp, RMGame rmGame, int page){
		if(rmp.hasPermission("resourcemadness.set")){
			int pages = 2;
			if(page<=0) page = 1;
			if(page>pages) page = pages;
			
			rmp.sendMessage(RMText.getLabelArgs("help_set", ""+page, ""+pages));
			
			SettingPrototype[] settingLib = rmGame.getGameConfig().getSettingLibrary().toArray();
			
			int i = 0;
			int iEnd = 0;
			
			if(page==1){
				i = 0;
				iEnd = Setting.warnunequal.ordinal();
			}
			else if(page==2){
				i = Setting.warnunequal.ordinal();
				iEnd = Setting.values().length;
			}
			
			while(i<iEnd){
				SettingPrototype setting = settingLib[i];
				if(rmp.hasPermission("resourcemadness.set."+setting.name())) if(!setting.isLocked()) rmp.sendMessage(RMText.getLabel("help_set."+setting.name()));
			}
		}
	}
	
	public void settingsInfo(RMPlayer rmp, RMGame rmGame, int page){
		int pages = 2;
		if(page<=0) page = 1;
		if(page>pages) page = pages;
		
		SettingPrototype[] settingLib = rmGame.getGameConfig().getSettingLibrary().toArray();
		
		int i = 0;
		int iEnd = 0;
		
		if(page==1){
			i = 0;
			iEnd = Setting.warnunequal.ordinal();
		}
		else if(page==2){
			i = Setting.warnunequal.ordinal();
			iEnd = Setting.values().length;
		}
		
		while(i<iEnd){
			SettingPrototype setting = settingLib[i];
			rmp.sendMessage(settingsInfoMessage(rmp, rmGame, setting));
		}
	}
	
	public String settingsInfoMessage(RMPlayer rmp, RMGame rmGame, SettingPrototype setting){
		
		String str = "";
		if(setting instanceof SettingInt) str = ((SettingInt)setting).toString();
		else if(setting instanceof SettingBool) str = isTrueFalse(((SettingBool) setting).get());
		else if(setting instanceof SettingStr) str = ((SettingStr)setting).get();
		
		switch(setting.setting()){
		case minplayers: case maxplayers: case minteamplayers: case maxteamplayers:
			str = (str!="0"?(ChatColor.GREEN+""+str):(RMText.getLabel("common.no_limit")));
			break;
		case timelimit:
			str = (str!="0"?(ChatColor.GREEN+""+str):(RMText.getLabel("common.no_limit")));
			break;
		case safezone:
			str = (str!="0"?(ChatColor.GREEN+""+str):(RMText.getLabel("common.disabled")));
			break;
		case random:
			str = ChatColor.AQUA + (str!="0"?(ChatColor.GREEN+""+str+" "+RMText.getLabel("common.item(s)")):(RMText.getLabel("common.disabled")));
			break;
		case password:
			str = (!rmp.hasOwnerPermission(rmGame.getGameConfig().getOwnerName())?getPassword(((SettingStr) setting).get(),true):getPassword(((SettingStr) setting).get()));
			break;
		}
		return (setting.isLocked()?ChatColor.RED:ChatColor.YELLOW)+RMText.getLabel("cmd.set."+setting.name())+" "+str+" "+RMText.getLabel("setting."+setting.name()+".");
	}
	
	//Is True / False
	public String isTrueFalse(boolean bool){
		return (bool?(ChatColor.GREEN+RMText.getLabel("common.true")):(ChatColor.GRAY+RMText.getLabel("common.false")));
	}
	
	public String getPassword(String str){
		return getPassword(str, false);
	}
	
	public String getPassword(String str, boolean hide){
		if(hide) str = ChatColor.GREEN+RMTextHelper.genString("*", str.length());
		else str = ChatColor.GREEN+str;
		return (str.length()!=0?(str):(RMText.getLabel("common.disabled")));
	}
}
