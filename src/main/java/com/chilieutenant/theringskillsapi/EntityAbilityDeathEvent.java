package com.chilieutenant.theringskillsapi;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when an entity is killed by
 * {@link GeneralMethods#damageEntity(Player player, Entity entity, double damage, String ability)
 * GeneralMethods.damageEntity}
 */

public class EntityAbilityDeathEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private final Entity entity;
	private final MainAbility ability;
	private final double damage;

	/**
	 * Creates a new EntityBendingDeathEvent
	 *
	 * @param entity the entity who died
	 * @param damage the amount of damage done in the attack that killed the
	 *            victim
	 * @param ability the ability used to kill the entity
	 */
	public EntityAbilityDeathEvent(final Entity entity, final double damage, final MainAbility ability) {
		this.entity = entity;
		this.ability = ability;
		this.damage = damage;
	}

	/**
	 *
	 * @return the entity that was killed
	 */
	public Entity getEntity() {
		return this.entity;
	}

	/**
	 *
	 * @return the player who killed the entity
	 */
	public Player getAttacker() {
		return this.ability.getPlayer();
	}

	/**
	 *
	 * @return the ability used to kill the victim
	 */
	public Ability getAbility() {
		return this.ability;
	}

	/**
	 *
	 * @return the amount of damage done in the attack that killed the victim
	 */
	public double getDamage() {
		return this.damage;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
