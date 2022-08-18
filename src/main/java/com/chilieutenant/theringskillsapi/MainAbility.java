package com.chilieutenant.theringskillsapi;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class MainAbility implements Ability{

	private static final List<MainAbility> abilities = new ArrayList<MainAbility>();
	private static final Map<Class<? extends MainAbility>, Map<UUID, Map<Integer, MainAbility>>> INSTANCES_BY_PLAYER = new ConcurrentHashMap<>();
	private static final Map<String, MainAbility> ABILITIES_BY_NAME = new ConcurrentSkipListMap<>(); // preserves ordering.
	
	protected Player player;
	protected AbilityPlayer aPlayer;
	private static int idCounter;
	private long starttime;
	private int id;
	private boolean started;
	private boolean removed;
	
	static {
		idCounter = Integer.MIN_VALUE;
	}
	public MainAbility(final Player player) {
		if(player == null) {
			return;
		}
		this.player = player;
		this.id = MainAbility.idCounter;
		this.aPlayer = AbilityPlayer.getAbilityPlayer(player);
		if (idCounter == Integer.MAX_VALUE) {
			idCounter = Integer.MIN_VALUE;
		} else {
			idCounter++;
		}
	}
	
	public boolean isWaterbendable(Block b) {
		return b.getType() == Material.WATER;
	}
	
	public boolean isTransparent(final Material material) {
		return !material.isOccluding() && !material.isSolid();
	}
	
	public Block getWaterSourceBlock(final Player player, final double range) {
		final Location location = player.getEyeLocation();
		final Vector vector = location.getDirection().clone().normalize();

		final AbilityPlayer bPlayer = AbilityPlayer.getAbilityPlayer(player);
		final Set<Material> trans = GeneralMethods.getTransparentMaterialSet();
		final Block testBlock = player.getTargetBlock(trans, range > 3 ? 3 : (int) range);
		if (bPlayer == null) {
			return null;
		} else if (isWaterbendable(testBlock)) {
			return testBlock;
		}

		for (double i = 0; i <= range; i++) {
			final Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if ((!isTransparent(block.getType()))) {
				continue;
			} else if (isWaterbendable(block)) {
				return block;
			}
		}
		return null;
	}
	
	public static List<MainAbility> getAbilities(){
		return abilities;
	}
	
	public static <T extends MainAbility> Collection<T> getAbilities(final Player player, final Class<T> clazz) {
		if (player == null || clazz == null || INSTANCES_BY_PLAYER.get(clazz) == null || INSTANCES_BY_PLAYER.get(clazz).get(player.getUniqueId()) == null) {
			return Collections.emptySet();
		}
		return (Collection<T>) INSTANCES_BY_PLAYER.get(clazz).get(player.getUniqueId()).values();
	}
	
	public static <T extends MainAbility> T getAbility(final Player player, final Class<T> clazz) {
		final Collection<T> abils = getAbilities(player, clazz);
		if (abils.iterator().hasNext()) {
			return abils.iterator().next();
		}
		return null;
	}
	
	public static <T extends MainAbility> boolean hasAbility(final Player player, final Class<T> clazz) {
		return getAbility(player, clazz) != null;
	}
	
	public static boolean hasAbility(Player player, MainAbility ability) {
		if(!abilities.isEmpty()) {
			for(MainAbility abil : abilities) {
				if(abil.getName().equalsIgnoreCase(ability.getName())) {
					if(ability.getPlayer().equals(player)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void start() {
		if(player == null) {
			return;
		}
		AbilityStartEvent event = new AbilityStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		this.started = true;
		this.starttime = System.currentTimeMillis();
		final Class<? extends MainAbility> clazz = this.getClass();
		final UUID uuid = this.player.getUniqueId();
		if (!INSTANCES_BY_PLAYER.containsKey(clazz)) {
			INSTANCES_BY_PLAYER.put(clazz, new ConcurrentHashMap<UUID, Map<Integer, MainAbility>>());
		}
		if (!INSTANCES_BY_PLAYER.get(clazz).containsKey(uuid)) {
			INSTANCES_BY_PLAYER.get(clazz).put(uuid, new ConcurrentHashMap<Integer, MainAbility>());
		}
		INSTANCES_BY_PLAYER.get(clazz).get(uuid).put(this.id, this);
		abilities.add(this);
	}

	public boolean isRemoved() {
		return this.removed;
	}
	
	public void remove() {
		if(player == null) {
			return;
		}
		AbilityEndEvent event = new AbilityEndEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		this.started = false;
		this.removed = true;
		final Map<UUID, Map<Integer, MainAbility>> classMap = INSTANCES_BY_PLAYER.get(this.getClass());
		if (classMap != null) {
			final Map<Integer, MainAbility> playerMap = classMap.get(this.player.getUniqueId());
			if (playerMap != null) {
				playerMap.remove(this.id);
				if (playerMap.size() == 0) {
					classMap.remove(this.player.getUniqueId());
				}
			}

			if (classMap.size() == 0) {
				INSTANCES_BY_PLAYER.remove(this.getClass());
			}
		}
		//abilities.remove(this);
	}
	
	public long getStarttime() {
		return this.starttime;
	}

	public Player getPlayer() {
		return player;
	}
	
}
