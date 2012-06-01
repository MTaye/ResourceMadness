package com.mtaye.ResourceMadness.command;

import org.bukkit.command.CommandExecutor;

import com.mtaye.ResourceMadness.RM;

public abstract class Command implements CommandExecutor {
    protected RM plugin;
    
    /*
    public Command(RM plugin){
    	this.plugin = plugin;
    }
    */
}
