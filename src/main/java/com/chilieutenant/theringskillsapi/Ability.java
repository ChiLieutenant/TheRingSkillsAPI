package com.chilieutenant.theringskillsapi;

public interface Ability {

	public String getName();
	public long getCooldown();
	public void progress();
	
}
