/* 
 *      ServerPassword - 1.18.x <> Codedesign by PT400C and Compaszer
 *      © Jomcraft-Network 2021
 */
package net.jomcraft.serverpassword;

import java.util.List;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

	public ForgeConfigSpec.ConfigValue<String> password;
	public ForgeConfigSpec.ConfigValue<List<? extends String>> whitelist;

	public Config(final ForgeConfigSpec.Builder builder) {
		builder.push("General");
		this.password = builder.comment("The server stores your password here. Do NOT change manually!").translation("Password").define("Password", "");

		this.whitelist = builder.comment("Users which are allowed to enter the password (UUID)").defineList("Whitelist", Lists.newArrayList("*"), o -> o instanceof String);
		builder.pop();
	}
}