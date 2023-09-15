package net.jomcraft.serverpassword;

import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListOps;
import net.minecraft.server.management.UserListWhitelist;

public class PatchMethods {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");

    @SuppressWarnings("rawtypes")
    public static boolean canJoin(GameProfile profile, boolean whiteListEnforced, UserListOps ops, UserListWhitelist whiteListedPlayers) {
        boolean OP = false;
        boolean white = false;
        try {
            Field f = UserList.class.getDeclaredField("field_152696_d");

            f.setAccessible(true);
            Map mp = (Map) f.get(ops);
            OP = mp.containsKey(profile.getId().toString());

            Field f2 = UserList.class.getDeclaredField("field_152696_d");

            f2.setAccessible(true);
            Map mp2 = (Map) f2.get(whiteListedPlayers);
            white = mp2.containsKey(profile.getId().toString());

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return !whiteListEnforced || OP || white;
    }

    @SuppressWarnings("static-access")
    public static String allowUser(SocketAddress p_148542_1_, GameProfile p_148542_2_) {
        String s = null;
        if (CommonProxy.getConfig().password.equals("")) {

            return "\u00A7cKicked from the server!\n\n\u00A7bThe default password has to be changed by an admin!";

        } else if (!ServerPassword.allowed.contains(p_148542_2_.getId().toString())) {

            if (CommonProxy.fast.contains(p_148542_1_.toString().split("/")[1].split(":")[0])) {

                CommonProxy.fast.remove(p_148542_1_.toString().split("/")[1].split(":")[0]);
                return "\u00A7cKicked from the server!\n\n\u00A7cThe request came to quickly!";

            } else {

                if (CommonProxy.permit.contains(p_148542_2_.getId().toString())) {
                    CommonProxy.permit.remove(p_148542_2_.getId().toString());
                    return "\u00A7cKicked from the server!\n\n\u00A7cThe server denied your request: \nYou may not be permitted to join or entered an invalid password!";
                } else {

                    return "\u00A7cKicked from the server!\n\n\u00A7cThe server denied your request: \nYou may not be permitted to join or entered an invalid password!";
                }
            }

        } else {
            ServerPassword.allowed.remove(p_148542_2_.getId().toString());
        }

        return s;
    }

    @SuppressWarnings("static-access")
    public static String allowUserToConnect(SocketAddress p_148542_1_, GameProfile p_148542_2_, boolean whiteListEnforced, UserListOps ops, UserListWhitelist whiteListedPlayers, UserListBans bannedPlayers, BanList bannedIPs, @SuppressWarnings("rawtypes") List playerEntityList, int maxPlayers) {

        String s;

        if (ServerPassword.isDedi()) {
            if (CommonProxy.getConfig().password.equals("")) {

                return "\u00A7cKicked from the server!\n\n\u00A7bThe default password has to be changed by an admin!";

            } else if (!ServerPassword.allowed.contains(p_148542_2_.getId().toString())) {

                if (CommonProxy.fast.contains(p_148542_1_.toString().split("/")[1].split(":")[0])) {

                    CommonProxy.fast.remove(p_148542_1_.toString().split("/")[1].split(":")[0]);
                    return "\u00A7cKicked from the server!\n\n\u00A7cThe request came to quickly!";

                } else {

                    if (CommonProxy.permit.contains(p_148542_2_.getId().toString())) {
                        CommonProxy.permit.remove(p_148542_2_.getId().toString());
                        return "\u00A7cKicked from the server!\n\n\u00A7cThe server denied your request: \nYou may not be permitted to join or entered an invalid password!";
                    } else {

                        return "\u00A7cKicked from the server!\n\n\u00A7cThe server denied your request: \nYou may not be permitted to join or entered an invalid password!";
                    }
                }

            }

        }

        ServerPassword.allowed.remove(p_148542_2_.getId().toString());
        if (bannedPlayers.func_152702_a(p_148542_2_)) {
            UserListBansEntry userlistbansentry = (UserListBansEntry) bannedPlayers.func_152683_b(p_148542_2_);
            s = "You are banned from this server!\nReason: " + userlistbansentry.getBanReason();

            if (userlistbansentry.getBanEndDate() != null) {
                s = s + "\nYour ban will be removed on " + dateFormat.format(userlistbansentry.getBanEndDate());
            }

            return s;
        } else if (!canJoin(p_148542_2_, whiteListEnforced, ops, whiteListedPlayers)) {
            return "You are not white-listed on this server!";
        } else if (bannedIPs.func_152708_a(p_148542_1_)) {
            IPBanEntry ipbanentry = bannedIPs.func_152709_b(p_148542_1_);
            s = "Your IP address is banned from this server!\nReason: " + ipbanentry.getBanReason();

            if (ipbanentry.getBanEndDate() != null) {
                s = s + "\nYour ban will be removed on " + dateFormat.format(ipbanentry.getBanEndDate());
            }

            return s;
        } else {
            return playerEntityList.size() >= maxPlayers ? "The server is full!" : null;
        }
    }

}
