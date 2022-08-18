package com.chilieutenant.theringskillsapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

public class DamageHandler {

	/**
	 * Damages an Entity by amount of damage specified. Starts a
	 * {@link EntityDamageByEntityEvent}.
	 *
	 * @param ability The ability that is used to damage the entity
	 * @param entity The entity that is receiving the damage
	 * @param damage The amount of damage to deal
	 */
	public static void damageEntity(final Entity entity, Player source, double damage, final MainAbility ability, boolean ignoreArmor) {
		if (entity instanceof LivingEntity && TempArmor.hasTempArmor((LivingEntity) entity)) {
			ignoreArmor = true;
		}
		if (ability == null) {
			return;
		}
		if (source == null) {
			source = ability.getPlayer();
		}

		final AbilityDamageEvent damageEvent = new AbilityDamageEvent(entity, ability, damage, ignoreArmor);
		Bukkit.getServer().getPluginManager().callEvent(damageEvent);
		if (entity instanceof LivingEntity) {
			if (!damageEvent.isCancelled()) {
				damage = damageEvent.getDamage();

				if (((LivingEntity) entity).getHealth() - damage <= 0 && !entity.isDead()) {
					final EntityAbilityDeathEvent event = new EntityAbilityDeathEvent(entity, damage, ability);
					Bukkit.getServer().getPluginManager().callEvent(event);
				}

				final EntityDamageByEntityEvent finalEvent = new EntityDamageByEntityEvent(source, entity, DamageCause.CUSTOM, damage);
				((LivingEntity) entity).damage(damage, source);
				entity.setLastDamageCause(finalEvent);
				if (ignoreArmor) {
					if (finalEvent.isApplicable(DamageModifier.ARMOR)) {
						finalEvent.setDamage(DamageModifier.ARMOR, 0);
					}
				}
			}else {
				//source.sendMessage("2");
			}
		}else {
			//source.sendMessage("3");
		}

	}

	public static void damageEntity(final Entity entity, final Player source, final double damage, final MainAbility ability) {
		damageEntity(entity, source, damage, ability, true);
	}

	public static void damageEntity(final Entity entity, final double damage, final MainAbility ability) {
		damageEntity(entity, ability.getPlayer(), damage, ability);
	}
}
