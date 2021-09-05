/* 
 *      ServerPassword - 1.16.5 <> Codedesign by PT400C and Compaszer
 *      © Jomcraft-Network 2021
 */
package net.jomcraft.serverpassword;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

public class CommandServerPassword {

	protected static void register(FMLServerStartingEvent event) {
		LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("serverpassword");

		literalargumentbuilder.then(Commands.literal("set").executes((command) -> {
			return countRange(command.getSource(), "");
		}).then(Commands.argument("password", StringArgumentType.string()).executes((command) -> {
			return countRange(command.getSource(), StringArgumentType.getString(command, "password"));
		})));

		LiteralCommandNode<CommandSource> node = event.getServer().getCommands().getDispatcher().register(literalargumentbuilder);

		event.getServer().getCommands().getDispatcher().register(Commands.literal("serverpw").redirect(node));
	}

	private static int countRange(CommandSource source, String password) throws CommandSyntaxException {

		if (password.isEmpty()) {
			source.sendFailure(new StringTextComponent(TextFormatting.RED + "The password can't be empty!"));
			return 0;
		}

		String pw = null;
		try {
			pw = PWHashing.generatePBKDFPassword(password);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		ConfigManager.SERVER.password.set(pw);
		ConfigManager.SERVER.password.save();

		source.sendSuccess(new StringTextComponent(TextFormatting.AQUA + "The new password has been set!"), true);

		return 1;

	}
}