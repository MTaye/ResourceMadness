package com.mtaye.ResourceMadness;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
 

public class SignWrapper
{

  private final String world;
  private final int x;
  private final int y;
  private final int z;
  

  public SignWrapper(Sign sign)
  {
        world = sign.getWorld().getName();
        x = sign.getX();
        y = sign.getY();
        z = sign.getZ();
  }
  

  public Sign getHandle()
  {
        World world = Bukkit.getWorld(this.world);
        if(world == null)
          return null;
        BlockState bs = world.getBlockAt(x, y, z).getState();
        if(!(bs instanceof Sign))
          return null;
        return (Sign)bs;
  }
}