package com.chilieutenant.theringskillsapi;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TempBlock {

	private Material prevMaterial;
	private BlockData blockData;
	private Block block;
	public static Map<Block, TempBlock> instances = new ConcurrentHashMap<Block, TempBlock>();
	
	public TempBlock(Block block, Material newType) {
		prevMaterial = block.getType();
		this.block = block;
		block.setType(newType);
		instances.put(block, this);
	}
	
	public TempBlock(Block block, Material newType, BlockData blockData) {
		prevMaterial = block.getType();
		blockData = block.getBlockData();
		this.block = block;
		block.setType(newType);
		block.setBlockData(blockData);
		instances.put(block, this);
	}
	
	public void revertAllTempBlocks() {
		for(TempBlock tbl : instances.values()) {
			tbl.revertBlock();
		}
		instances.clear();
	}
	
	public static TempBlock get(final Block block) {
		if (isTempBlock(block)) {
			return instances.get(block);
		}
		return null;
	}
	
	public Location getLocation() {
		return block.getLocation();
	}
	
	public Block getBlock() {
		return block;
	}
	
	public static boolean isTempBlock(final Block block) {
		return block != null && instances.containsKey(block);
	}
	
	public void setRevertTime(long time) {
		new BukkitRunnable() {
			public void run() {
				if(block == null || !instances.containsKey(block)) {
					this.cancel();
					return;
				}
				block.setType(prevMaterial);
				if(blockData != null) {
					block.setBlockData(blockData);
				}
				if(instances.containsKey(block)) {
					instances.remove(block);
				}
				this.cancel();
			}
		}.runTaskLater(main.getInstance(), time);
	}
	
	public void revertBlock() {
		block.setType(prevMaterial);
		if(blockData != null) {
			block.setBlockData(blockData);
		}
		if(instances.containsKey(block)) {
			instances.remove(block);
		}
	}
}
