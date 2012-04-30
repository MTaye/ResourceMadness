package com.mtaye.ResourceMadness;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.mtaye.ResourceMadness.RMConfig.PermissionType;
import com.nijikokun.bukkit.Permissions.Permissions;

public class RMPermissionListener implements Listener{
	public RM plugin;
	public RMPermissionListener(RM plugin){
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPluginDisable(final PluginDisableEvent event){
		PluginManager pm = plugin.getServer().getPluginManager();
        Plugin p = pm.getPlugin("Permissions");
        if((p==null)&&(plugin.permissions!=null)){
        	plugin.permissions = null;
        	plugin.log.info(RMText.preLog+"Un-hooked from Permissions 3.");
        }
        p = pm.getPlugin("PermissionsEx");
        if((p==null)&&(plugin.permissionsEx!=null)){
        	plugin.permissionsEx = null;
        	plugin.log.info(RMText.preLog+"Un-hooked from PermissionsEx.");
        }
   	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPluginEnable(final PluginEnableEvent event){
		switch(plugin.config.getPermissionType()){
		default: setupPermissions(); break;
		case AUTO:
			PluginManager pm = plugin.getServer().getPluginManager();
			Plugin p = pm.getPlugin("Permissions");
			if(p!=null){
				if(p.isEnabled()){
					plugin.config.setPermissionType(PermissionType.P3);
					setupPermissions();
					return;
				}
			}
			p = pm.getPlugin("PermissionsEx");
			if(p!=null){
				if(p.isEnabled()){
				   	plugin.config.setPermissionType(PermissionType.PEX);
				   	setupPermissions();
					return;
				}
			}
			//plugin.config.setPermissionType(PermissionType.BUKKIT);
			//setupPermissions();
			break;
		case FALSE:
		}
	
	}
	
	public void setupPermissions(){
		PluginManager pm = plugin.getServer().getPluginManager();
		switch(plugin.config.getPermissionType()){
		case P3:
			if(plugin.permissions==null){
				try{
					Plugin p = pm.getPlugin("Permissions");
					if(plugin.permissions == null){
						try{
							plugin.permissions = ((Permissions)p).getHandler();
							plugin.log.log(Level.INFO, RMText.preLog+"Found Permissions 3.");
						}
						catch (Exception e){
							plugin.permissions = null;
							plugin.log.log(Level.WARNING, RMText.preLog+"Permissions 3 is not enabled!");
						}
					}
				}
				catch (java.lang.NoClassDefFoundError e){
					plugin.permissions = null;
					plugin.log.log(Level.WARNING, RMText.preLog+"Permissions 3 plugin not found!");
				}
			}
			break;
		case PEX:
			if(plugin.permissionsEx==null){
				try{
					if(pm.isPluginEnabled("PermissionsEx")){
					    plugin.permissionsEx = PermissionsEx.getPermissionManager();
					    if(plugin.permissionsEx==null) plugin.log.log(Level.WARNING, RMText.preLog+"PermissionsEx is not enabled!");
					    plugin.log.log(Level.INFO, RMText.preLog+"Found PermissionsEx.");
					}
					else plugin.log.log(Level.WARNING, RMText.preLog+"PermissionsEx not found.");
				}
				catch (Exception e){
					plugin.permissionsEx = null;
					plugin.log.log(Level.WARNING, RMText.preLog+"PermissionsEx not found!");
				}
			}
			break;
		case BUKKIT:
			if(!plugin.permissionBukkit){
				plugin.permissionBukkit = true;
				plugin.log.log(Level.INFO, RMText.preLog+"Using Bukkit permissions.");
			}
			break;
		}
		if((plugin.permissions == null)&&(plugin.permissionsEx == null)&&(!plugin.permissionBukkit)) plugin.config.setPermissionType(PermissionType.FALSE);
	}
}