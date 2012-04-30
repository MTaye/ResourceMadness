package com.mtaye.ResourceMadness;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class RMInventory implements Inventory{
	public Inventory[] _inventories;
	
	public RMInventory(Chest chest){
		Chest secondChest = findSecondChest(chest);
		if(secondChest!=null){
			_inventories = new Inventory[2];
			_inventories[1] = secondChest.getInventory();
		}
		else _inventories = new Inventory[1];
		_inventories[0] = chest.getInventory();
	}
	
	public RMInventory(Inventory... inventories){
		_inventories = inventories;
	}
	
	public Chest findSecondChest(Chest chest){
		Block b = chest.getBlock();
		Block[] blocks = new Block[4];
		blocks[0] = b.getRelative(BlockFace.NORTH);
		blocks[1] = b.getRelative(BlockFace.EAST);
		blocks[2] = b.getRelative(BlockFace.SOUTH);
		blocks[3] = b.getRelative(BlockFace.WEST);
		for(Block block : blocks){
			if(block.getType()!=Material.CHEST) continue;
			return (Chest)block.getState();
		}
		return null;
	}

	@Override
	public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
		HashMap<Integer, ItemStack> ret = new HashMap<Integer, ItemStack>();
		for(Inventory inv : _inventories){
			ret = inv.addItem(items);
			if(ret.size()==0) return ret;
		}
		return ret;
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(int items) {
		return all(Material.getMaterial(items));
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(Material arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(ItemStack arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		for(Inventory inv : _inventories){
			inv.clear();
		}
	}

	@Override
	public void clear(int slot) {
		getLocalInventory(slot).clear(getLocalSlot(slot));
	}

	@Override
	public boolean contains(int id) {
		for(Inventory inv : _inventories){
			return inv.contains(id);
		}
		return false;
	}

	@Override
	public boolean contains(Material mat) {
		return contains(mat.getId());
	}

	@Override
	public boolean contains(ItemStack item) {
		return contains(item.getTypeId());
	}

	@Override
	public boolean contains(int id, int amount) {
		for(Inventory inv : _inventories){
			return inv.contains(id, amount);
		}
		return false;
	}

	@Override
	public boolean contains(Material mat, int amount) {
		return contains(mat.getId(), amount);
	}

	@Override
	public boolean contains(ItemStack arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int first(int id) {
		int size = getSize();
		for(int i=0; i<size; i++){
			ItemStack item = getItem(i);
			if(item==null) continue;
			if(getItem(i).getType() == Material.getMaterial(id)){
				return i;
			}
		}
		return -1;
	}

	@Override
	public int first(Material mat) {
		return first(mat.getId());
	}

	@Override
	public int first(ItemStack item) {
		return first(item.getTypeId());
	}

	@Override
	public int firstEmpty() {
		int size = getSize();
		for(int i=0; i<size; i++){
			ItemStack item = getItem(i);
			if(item==null) return i;
			if(getItem(i).getTypeId() == 0){
				return i;
			}
		}
		return -1;
	}

	@Override
	public ItemStack[] getContents() {
		ItemStack[] ret = new ItemStack[getSize()];
		int i=0;
		for(Inventory inv : _inventories){
			ItemStack[] contents = inv.getContents();
			for(ItemStack item : contents){
				ret[i] = item;
				i++;
			}
		}
		return ret;
	}

	@Override
	public ItemStack getItem(int slot) {
		return getLocalInventory(slot).getItem(getLocalSlot(slot));
	}

	@Override
	public String getName() {
		return _inventories[0].getName();
	}

	@Override
	public int getSize() {
		int size=0;
		for(Inventory inv : _inventories){
			size+=inv.getSize();
		}
		return size;
	}

	@Override
	public void remove(int id) {
		for(Inventory inv : _inventories){
			inv.remove(id);
		}
	}

	@Override
	public void remove(Material mat) {
		for(Inventory inv : _inventories){
			inv.remove(mat);
		}
	}

	@Override
	public void remove(ItemStack item) {
		for(Inventory inv : _inventories){
			inv.remove(item);
		}
	}

	@Override
	public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
		HashMap<Integer, ItemStack> ret = new HashMap<Integer, ItemStack>();
		int i=0;
		for(ItemStack item : items){
			ret.put(i++, item);
		}
		for(Inventory inv : _inventories){
			if(ret.size()==0) return ret;
			ItemStack[] remove = ret.values().toArray(new ItemStack[ret.size()]);
			ret = inv.removeItem(remove);
		}
		return ret;
	}

	@Override
	public void setContents(ItemStack[] items) {
		for(int i=0; i<items.length; i++){
			setItem(i, items[i]);
		}
	}

	@Override
	public void setItem(int slot, ItemStack item) {
		getLocalInventory(slot).setItem(getLocalSlot(slot), item);
	}
	
	public Inventory getLocalInventory(int slot){
		for(Inventory inv : _inventories){
			slot-=inv.getSize();
			if(slot<0) return inv;
		}
		return null;
	}
	
	public int getLocalSlot(int slot){
		for(Inventory inv : _inventories){
			slot-=inv.getSize();
			if(slot<0) return slot+inv.getSize();
			return slot;
		}
		return -1;
	}
	
	public int getGlobalSlot(int slot, Inventory localInv){
		for(Inventory inv : _inventories){
			if(inv==localInv) return slot;
			else return slot + inv.getSize();
		}
		return -1;
	}
	
	//NOT WORKING
	public InventoryHolder getHolder(){
		for(Inventory inv:_inventories){
			return inv.getHolder();
		}
		return null;
	}
	
	public int getMaxStackSize(){
		for(Inventory inv:_inventories){
			return inv.getMaxStackSize();
		}
		return 64;
	}
	
	public String getTitle(){
		for(Inventory inv:_inventories){
			return inv.getTitle();
		}
		return null;
	}
	
	public InventoryType getType(){
		for(Inventory inv:_inventories){
			return inv.getType();
		}
		return null;
	}
	
	 public List<HumanEntity> getViewers(){
		 List<HumanEntity> viewers = new ArrayList<HumanEntity>();
		for(Inventory inv:_inventories){
			List<HumanEntity> invViewers = inv.getViewers();
			for(HumanEntity viewer:invViewers){
				if(!viewers.contains(viewer)){
					viewers.add(viewer);
				}
			}
		}
		return viewers;
	}
	 
	 public ListIterator<ItemStack> iterator(){
		 for(Inventory inv:_inventories){
			 return inv.iterator();
		 }
		 return null;
	 }
	 
	 public ListIterator<ItemStack> iterator(int index){
		 for(Inventory inv:_inventories){
			 return inv.iterator(index);
		 }
		 return null;
	 }
	 
	 public void setMaxStackSize(int size){
		 for(Inventory inv:_inventories){
			 inv.setMaxStackSize(size);
		 }
	 }
}
