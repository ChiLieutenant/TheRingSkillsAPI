package com.chilieutenant.theringskillsapi;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneralMethods {
	
	public static Block getBottomBlock(final Location loc, final int positiveY, final int negativeY) {
		Block blockHolder = loc.getBlock();
		int y = 0;
		// Only one of these while statements will go
		while (isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(negativeY)) {
			y--;
			final Block tempblock = loc.clone().add(0, y, 0).getBlock();
			if (isAir(tempblock.getType())) {
				return blockHolder;
			}

			blockHolder = tempblock;
		}

		while (isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			blockHolder = loc.clone().add(0, y, 0).getBlock();
			if (isAir(blockHolder.getType())) {
				return blockHolder;
			}
		}

		return blockHolder;
	}
	
	public static boolean isAir(final Material material) {
		return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
	}
	private static final Set<Material> TRANSPARENT = new HashSet<>();

	static {
		TRANSPARENT.clear();
		for (final Material mat : Material.values()) {
			if (GeneralMethods.isTransparent(mat)) {
				TRANSPARENT.add(mat);
			}
		}
	}
	public static boolean isTransparent(final Block block) {
		return isTransparent(block.getType());
	}

	public static boolean isTransparent(final Material material) {
		return !material.isOccluding() && !material.isSolid();
	}
	public static boolean isWater(final Block block) {
		if (block == null) {
			return false;
		} else if (block.getState() instanceof Container) {
			return false; 
		} else {
			return isWater(block.getBlockData());
		}
	}

	public static boolean isWater(final BlockData data) {
		return (data instanceof Waterlogged) ? ((Waterlogged) data).isWaterlogged() : isWater(data.getMaterial());
	}

	public static boolean isWater(final Material material) {
		return material == Material.WATER || material == Material.SEAGRASS || material == Material.TALL_SEAGRASS || material == Material.KELP_PLANT || material == Material.KELP || material == Material.BUBBLE_COLUMN;
	}
	public static Material[] getTransparentMaterials() {
		return TRANSPARENT.toArray(new Material[TRANSPARENT.size()]);
	}

	public static HashSet<Material> getTransparentMaterialSet() {
		return new HashSet<>(TRANSPARENT);
	}
	public static boolean isObstructed(final Location location1, final Location location2) {
		final Vector loc1 = location1.toVector();
		final Vector loc2 = location2.toVector();

		final Vector direction = loc2.subtract(loc1);
		direction.normalize();

		Location loc;

		double max = 0;
		if (location1.getWorld().equals(location2.getWorld())) {
			max = location1.distance(location2);
		}

		for (double i = 0; i <= max; i++) {
			loc = location1.clone().add(direction.clone().multiply(i));
			final Material type = loc.getBlock().getType();
			if (type != Material.AIR && !(getTransparentMaterialSet().contains(type) || isWater(loc.getBlock()))) {
				return true;
			}
		}
		return false;
	}
	public static Entity getTargetedEntity(final Player player, final double range, final List<Entity> avoid) {
		double longestr = range + 1;
		Entity target = null;
		final Location origin = player.getEyeLocation();
		final Vector direction = player.getEyeLocation().getDirection().normalize();
		for (final Entity entity : getEntitiesAroundPoint(origin, range)) {
			if (entity instanceof Player) {
				if (((Player) entity).isDead() || ((Player) entity).getGameMode().equals(GameMode.SPECTATOR)) {
					continue;
				}
			}
			if (avoid.contains(entity)) {
				continue;
			}
			if (entity.getWorld().equals(origin.getWorld())) {
				if (entity.getLocation().distanceSquared(origin) < longestr * longestr && getDistanceFromLine(direction, origin, entity.getLocation()) < 2 && (entity instanceof LivingEntity) && entity.getEntityId() != player.getEntityId() && entity.getLocation().distanceSquared(origin.clone().add(direction)) < entity.getLocation().distanceSquared(origin.clone().add(direction.clone().multiply(-1)))) {
					target = entity;
					longestr = entity.getLocation().distance(origin);
				}
			}
		}
		if (target != null) {
			if (GeneralMethods.isObstructed(origin, target.getLocation())) {
				target = null;
			}
		}
		return target;
	}
	public static double getDistanceFromLine(final Vector line, final Location pointonline, final Location point) {
		final Vector AP = new Vector();
		double Ax, Ay, Az;
		Ax = pointonline.getX();
		Ay = pointonline.getY();
		Az = pointonline.getZ();

		double Px, Py, Pz;
		Px = point.getX();
		Py = point.getY();
		Pz = point.getZ();

		AP.setX(Px - Ax);
		AP.setY(Py - Ay);
		AP.setZ(Pz - Az);

		return (AP.crossProduct(line).length()) / (line.length());
	}
	public static BlockFace getBlockFaceFromValue(final int xyz, final double value) {
		switch (xyz) {
			case 0:
				if (value > 0) {
					return BlockFace.EAST;
				} else if (value < 0) {
					return BlockFace.WEST;
				} else {
					return BlockFace.SELF;
				}
			case 1:
				if (value > 0) {
					return BlockFace.UP;
				} else if (value < 0) {
					return BlockFace.DOWN;
				} else {
					return BlockFace.SELF;
				}
			case 2:
				if (value > 0) {
					return BlockFace.SOUTH;
				} else if (value < 0) {
					return BlockFace.NORTH;
				} else {
					return BlockFace.SELF;
				}
			default:
				return null;
		}
	}
	public static boolean checkDiagonalWall(final Location location, final Vector direction) {
		final boolean[] xyzsolid = { false, false, false };
		for (int i = 0; i < 3; i++) {
			double value;
			if (i == 0) {
				value = direction.getX();
			} else if (i == 1) {
				value = direction.getY();
			} else {
				value = direction.getZ();
			}
			final BlockFace face = GeneralMethods.getBlockFaceFromValue(i, value);
			if (face == null) {
				continue;
			}
			xyzsolid[i] = location.getBlock().getRelative(face).getType().isSolid();
		}
		final boolean a = xyzsolid[0] && xyzsolid[2];
		final boolean b = xyzsolid[0] && xyzsolid[1];
		final boolean c = xyzsolid[1] && xyzsolid[2];
		return (a || b || c || (a && b));
	}
	
	public static Entity getTargetedEntity(final Player player, final double range) {
		return getTargetedEntity(player, range, new ArrayList<Entity>());
	}
	public static List<Entity> getEntitiesAroundPoint(final Location location, final double radius) {
		return new ArrayList<>(location.getWorld().getNearbyEntities(location, radius, radius, radius, entity -> !(entity.isDead() || (entity instanceof Player && ((Player) entity).getGameMode().equals(GameMode.SPECTATOR)))));
	}
	public static Location getTargetedLocation(final Player player, final double range, final boolean ignoreTempBlocks, final boolean checkDiagonals, final Material... nonOpaque2) {
		final Location origin = player.getEyeLocation();
		final Vector direction = origin.getDirection();

		final HashSet<Material> trans = new HashSet<Material>();
		trans.add(Material.AIR);
		trans.add(Material.CAVE_AIR);
		trans.add(Material.VOID_AIR);

		if (nonOpaque2 != null) {
			for (final Material material : nonOpaque2) {
				trans.add(material);
			}
		}

		final Location location = origin.clone();
		final Vector vec = direction.normalize().multiply(0.2);

		for (double i = 0; i < range; i += 0.2) {
			location.add(vec);

			if (checkDiagonals && checkDiagonalWall(location, vec)) {
				location.subtract(vec);
				break;
			}

			final Block block = location.getBlock();

			if (trans.contains(block.getType())) {
				continue;
			} else if (ignoreTempBlocks && (TempBlock.isTempBlock(block))) {
				continue;
			} else {
				location.subtract(vec);
				break;
			}
		}

		return location;
	}

	
	
	public static Location getTargetedLocation(final Player player, final double range, final boolean ignoreTempBlocks, final Material... nonOpaque2) {
		return getTargetedLocation(player, range, ignoreTempBlocks, true, nonOpaque2);
	}

	public static Location getTargetedLocation(final Player player, final double range, final Material... nonOpaque2) {
		return getTargetedLocation(player, range, false, nonOpaque2);
	}

	public static Location getTargetedLocation(final Player player, final int range) {
		return getTargetedLocation(player, range, false);
	}
	
	public static Vector getDirection(final Location location, final Location destination) {
		double x1, y1, z1;
		double x0, y0, z0;

		x1 = destination.getX();
		y1 = destination.getY();
		z1 = destination.getZ();

		x0 = location.getX();
		y0 = location.getY();
		z0 = location.getZ();

		return new Vector(x1 - x0, y1 - y0, z1 - z0);
	}

	public static Block getTopBlock(final Location loc, final int range) {
		return getTopBlock(loc, range, range);
	}

	/**
	 * Returns the top block based around loc. PositiveY is the maximum amount
	 * of distance it will check upward. Similarly, negativeY is for downward.
	 */
	public static Block getTopBlock(final Location loc, final int positiveY, final int negativeY) {
		Block blockHolder = loc.getBlock();
		int y = 0;
		// Only one of these while statements will go
		while (!isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(positiveY)) {
			y++;
			final Block tempBlock = loc.clone().add(0, y, 0).getBlock();
			if (isAir(tempBlock.getType())) {
				return blockHolder;
			}
			blockHolder = tempBlock;
		}

		while (isAir(blockHolder.getType()) && Math.abs(y) < Math.abs(negativeY)) {
			y--;
			blockHolder = loc.clone().add(0, y, 0).getBlock();
			if (!isAir(blockHolder.getType())) {
				return blockHolder;
			}
		}
		return blockHolder;
	}
}
