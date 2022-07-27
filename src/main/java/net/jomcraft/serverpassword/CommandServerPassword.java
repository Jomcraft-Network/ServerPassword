/* 
 *		ServerPassword - 1.19.x <> Codedesign by PT400C and Compaszer
 *		© Jomcraft-Network 2022
 */
package net.jomcraft.serverpassword;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.server.ServerStartingEvent;

public class CommandServerPassword {

	protected static void register(ServerStartingEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("serverpassword");

		literalargumentbuilder.then(Commands.literal("set").executes((command) -> {
			return countRange(command.getSource(), "");
		}).then(Commands.argument("password", StringArgumentType.string()).executes((command) -> {
			return countRange(command.getSource(), StringArgumentType.getString(command, "password"));
		})));

		LiteralCommandNode<CommandSourceStack> node = event.getServer().getCommands().getDispatcher().register(literalargumentbuilder);

		event.getServer().getCommands().getDispatcher().register(Commands.literal("serverpw").redirect(node));
	}

	private static int countRange(CommandSourceStack source, String password) throws CommandSyntaxException {

		if (password.isEmpty()) {
			source.sendFailure(Component.literal(ChatFormatting.RED + "The password can't be empty!"));
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

		source.sendSuccess(Component.literal(ChatFormatting.AQUA + "The new password has been set!"), true);

		return 1;

	}
}