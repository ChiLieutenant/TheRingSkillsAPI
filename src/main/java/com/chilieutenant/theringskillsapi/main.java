package com.chilieutenant.theringskillsapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

public class main extends JavaPlugin implements Listener{
	
	public static main instance;
	
	@Override
	public void onEnable(){
		instance = this;
		Bukkit.getLogger().info(ChatColor.AQUA + "AbilityMaker plugini açıldı.");
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getOnlinePlayers().forEach(player -> new AbilityPlayer(player.getUniqueId(), player.getName()));
		new BukkitRunnable() {
			public void run() {
				RegenTempBlock.manage();
				if(!MainAbility.getAbilities().isEmpty()) {
					final Iterator<MainAbility> iterator = MainAbility.getAbilities().iterator();
					while(iterator.hasNext()) {
						final MainAbility abil = iterator.next();
						if(abil.isRemoved()) {
							MainAbility.getAbilities().remove(abil);
							return;
						}else {
							abil.progress();
						}
					}
				}
			}
		}.runTaskTimer(this, 0, 1);
	}
	
	
	public void onDisable(){
		for(TempBlock tbl : TempBlock.instances.values()) {
			tbl.revertBlock();
		}
		TempBlock.instances.clear();
        Bukkit.getLogger().info(ChatColor.AQUA + "AbilityMaker plugini kapatıldı!");
    }

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		new AbilityPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (TempBlock.isTempBlock(event.getBlock()))
        {
        	//do what you need here
        	event.setCancelled(true);
        }
	}
 
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getToBlock();
        if (TempBlock.isTempBlock(block)) {
                //do what you need here.
        	event.setCancelled(true);
        }
    }
	
 
	
	public static Plugin getInstance() {
		// TODO Auto-generated method stub
		return instance;
	}
}
