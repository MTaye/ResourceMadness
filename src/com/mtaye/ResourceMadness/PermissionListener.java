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

import com.mtaye.ResourceMadness.Config.PermissionType;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PermissionListener implements Listener{
	public RM rm;
	public PermissionListener(RM plugin){
		this.rm = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPluginDisable(final PluginDisableEvent event){
		PluginManager pm = rm.getServer().getPluginManager();
        Plugin p = pm.getPlugin("Permissions");
        if((p==null)&&(rm.permissions!=null)){
        	rm.permissions = null;
        	rm.log.info(Text.preLog+"Un-hooked from Permissions 3.");
        }
        p = pm.getPlugin("PermissionsEx");
        if((p==null)&&(rm.permissionsEx!=null)){
        	rm.permissionsEx = null;
        	rm.log.info(Text.preLog+"Un-hooked from PermissionsEx.");
        }
   	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPluginEnable(final PluginEnableEvent event){
		switch(rm.config.getPermissionType()){
		default: setupPermissions(); break;
		case AUTO:
			PluginManager pm = rm.getServer().getPluginManager();
			Plugin p = pm.getPlugin("Permissions");
			if(p!=null){
				if(p.isEnabled()){
					rm.config.setPermissionType(PermissionType.P3);
					setupPermissions();
					return;
				}
			}
			p = pm.getPlugin("PermissionsEx");
			if(p!=null){
				if(p.isEnabled()){
				   	rm.config.setPermissionType(PermissionType.PEX);
				   	setupPermissions();
					return;
				}
			}
			rm.config.setPermissionType(PermissionType.BUKKIT);
			setupPermissions();
			break;
		case FALSE:
		}
	
	}
	
	public void setupPermissions(){
		PluginManager pm = rm.getServer().getPluginManager();
		switch(rm.config.getPermissionType()){
		case P3:
			if(rm.permissions==null){
				try{
					Plugin p = pm.getPlugin("Permissions");
					if(rm.permissions == null){
						try{
							rm.permissions = ((Permissions)p).getHandler();
							rm.log.log(Level.INFO, Text.preLog+"Found Permissions 3.");
						}
						catch (Exception e){
							rm.permissions = null;
							rm.log.log(Level.WARNING, Text.preLog+"Permissions 3 is not enabled!");
						}
					}
				}
				catch (java.lang.NoClassDefFoundError e){
					rm.permissions = null;
					rm.log.log(Level.WARNING, Text.preLog+"Permissions 3 plugin not found!");
				}
			}
			break;
		case PEX:
			if(rm.permissionsEx==null){
				try{
					if(pm.isPluginEnabled("PermissionsEx")){
					    rm.permissionsEx = PermissionsEx.getPermissionManager();
					    if(rm.permissionsEx==null) rm.log.log(Level.WARNING, Text.preLog+"PermissionsEx is not enabled!");
					    rm.log.log(Level.INFO, Text.preLog+"Found PermissionsEx.");
					}
					else rm.log.log(Level.WARNING, Text.preLog+"PermissionsEx not found.");
				}
				catch (Exception e){
					rm.permissionsEx = null;
					rm.log.log(Level.WARNING, Text.preLog+"PermissionsEx not found!");
				}
			}
			break;
		case BUKKIT:
			if(!rm.permissionBukkit){
				rm.permissionBukkit = true;
				rm.log.log(Level.INFO, Text.preLog+"Using Bukkit permissions.");
			}
			break;
		}
		if((rm.permissions == null)&&(rm.permissionsEx == null)&&(!rm.permissionBukkit)){
			rm.config.setPermissionType(PermissionType.FALSE);
		}
	}
}