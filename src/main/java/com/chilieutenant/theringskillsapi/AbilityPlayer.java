package com.chilieutenant.theringskillsapi;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class AbilityPlayer {
	
	private static final Map<UUID, AbilityPlayer> ABILITYPLAYERS = new ConcurrentHashMap<>();
	
	private final Player player;
	private final UUID uuid;
	private final String name;
	private final HashMap<String, Long> cooldowns = new HashMap<>();
	private final AbilityPlayer aPlayer;

	public AbilityPlayer(final UUID uuid, final String playerName) {
		this.uuid = uuid;
		this.name = playerName;
		this.player = Bukkit.getPlayer(uuid);
		this.aPlayer = this;
		ABILITYPLAYERS.put(uuid, this);
	}

	
	public long getCooldownByName(String abilityName) {
		if(cooldowns.containsKey(abilityName)) {
			return cooldowns.get(abilityName);
		}
		return 0;
	}
	
	public void addCooldown(MainAbility ability) {
		cooldowns.put(ability.getName(), System.currentTimeMillis() + ability.getCooldown());
	}
	
	public boolean isOnCooldown(MainAbility ability) {
		if(cooldowns.containsKey(ability.getName())) {
			return cooldowns.get(ability.getName()) > System.currentTimeMillis();
		}
		return false;
	}

	
	public static Map<UUID, AbilityPlayer> getPlayers() {
		return ABILITYPLAYERS;
	}
	
	public static AbilityPlayer getAbilityPlayer(final OfflinePlayer oPlayer) {
		if (oPlayer == null) {
			return null;
		}

		return AbilityPlayer.getPlayers().get(oPlayer.getUniqueId());
	}

	public static AbilityPlayer getAbilityPlayer(final Player player) {
		if (player == null) {
			return null;
		}

		return getAbilityPlayer((OfflinePlayer)player);
	}

	
}
