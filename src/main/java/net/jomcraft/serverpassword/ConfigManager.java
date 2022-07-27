/* 
 *		ServerPassword - 1.18.x <> Codedesign by PT400C and Compaszer
 *		© Jomcraft-Network 2022
 */
package net.jomcraft.serverpassword;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ConfigManager {

	public static final ForgeConfigSpec SERVER_SPEC;
	public static final Config SERVER;

	static {
		{
			final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
			SERVER = specPair.getLeft();
			SERVER_SPEC = specPair.getRight();
		}
	}
}