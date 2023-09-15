package net.jomcraft.serverpassword;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CommandPassword implements ICommand {

    @SuppressWarnings("rawtypes")
    private final List aliases;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CommandPassword() {
        aliases = new ArrayList();
        aliases.add("serverpw");
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "serverpassword";
    }

    public String getCommandUsage(ICommandSender var1) {
        return "/serverpassword set [password]";
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public List getCommandAliases() {
        return this.aliases;
    }


    @SuppressWarnings({"static-access"})
    @Override
    public void processCommand(ICommandSender sender, String[] argString) {
        if (!(sender instanceof DedicatedServer)) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "No permissions!"));
            return;

        }

        if (argString.length == 0) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "There's no argument given! Try the argument " + EnumChatFormatting.AQUA + "set"));
            return;

        } else if (argString.length == 1) {

            if (argString[0].equals("set")) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You have to enter a new password"));
            } else {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Please specify one argument! (set)"));
            }

            return;
        } else if (argString.length == 2) {

            if (argString[0].equals("set")) {

                String password = argString[1];
                String pw = null;
                try {
                    pw = PWHashing.generatePBKDFPassword(password);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                CommonProxy.getConfig().getInstance().get("General", "Password", "", "Set the server's password <Encrypted>").set(pw);
                CommonProxy.getConfig().password = pw;
                CommonProxy.getConfig().getInstance().save();
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "The new password has been set!"));

            } else {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Please specify one argument! (set)"));
            }

            return;

        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Formatting issues! Try /serverpw set [password]"));
        }
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public List addTabCompletionOptions(ICommandSender var1, String[] p_71516_2_) {
        return p_71516_2_.length == 1 ? CommandBase.getListOfStringsMatchingLastWord(p_71516_2_, new String[]{"set"}) : null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        return true;
    }

}
