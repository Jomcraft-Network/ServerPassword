/* 
 *      ServerPassword - 1.17.x <> Codedesign by PT400C and Compaszer
 *      © Jomcraft-Network 2021
 */
package net.jomcraft.serverpassword.mixin;

import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.jomcraft.serverpassword.ConfigManager;
import net.jomcraft.serverpassword.ServerEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

@Mixin({ PlayerList.class })
public abstract class MixinPlayerList {

	@Shadow
	public static File USERBANLIST_FILE;
	@Shadow
	public static File IPBANLIST_FILE;
	@Shadow
	public static File WHITELIST_FILE;

	@Shadow
	private List<ServerPlayer> players = Lists.newArrayList();

	@Shadow
	private UserBanList bans;

	@Shadow
	private IpBanList ipBans;

	@Shadow
	private UserWhiteList whitelist;

	@Shadow
	protected int maxPlayers;

	@Shadow
	private static SimpleDateFormat BAN_DATE_FORMAT;

	@Overwrite
	@Nullable
	public Component canPlayerLogin(SocketAddress p_11257_, GameProfile p_11258_) {
		if (this.bans.isBanned(p_11258_)) {
			ServerEvents.allowed.remove(p_11258_.getId().toString());
			UserBanListEntry userbanlistentry = this.bans.get(p_11258_);
			MutableComponent mutablecomponent1 = new TranslatableComponent("multiplayer.disconnect.banned.reason", userbanlistentry.getReason());
			if (userbanlistentry.getExpires() != null) {
				mutablecomponent1.append(new TranslatableComponent("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userbanlistentry.getExpires())));
			}

			return mutablecomponent1;
		} else if (!this.isWhiteListed(p_11258_)) {
			ServerEvents.allowed.remove(p_11258_.getId().toString());
			return new TranslatableComponent("multiplayer.disconnect.not_whitelisted");
		} else if (this.ipBans.isBanned(p_11257_)) {
			ServerEvents.allowed.remove(p_11258_.getId().toString());
			IpBanListEntry ipbanlistentry = this.ipBans.get(p_11257_);
			MutableComponent mutablecomponent = new TranslatableComponent("multiplayer.disconnect.banned_ip.reason", ipbanlistentry.getReason());
			if (ipbanlistentry.getExpires() != null) {
				mutablecomponent.append(new TranslatableComponent("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanlistentry.getExpires())));
			}

			return mutablecomponent;
		} else if (ServerLifecycleHooks.getCurrentServer().isDedicatedServer()) {
			if (ConfigManager.SERVER.password.get().equals("")) {

				return new TextComponent("\u00A7cKicked from the server!\n\n\u00A7bThe default password has to be changed by an admin!");

			} else if (!ServerEvents.allowed.contains(p_11258_.getId().toString())) {

				if (ServerEvents.fast.contains(p_11257_.toString().split("/")[0])) {
					ServerEvents.fast.remove(p_11257_.toString().split("/")[0]);
					return new TextComponent("\u00A7cKicked from the server!\n\n\u00A7cThe request came to quickly!");

				} else {

					if (ServerEvents.permit.contains(p_11258_.getId().toString())) {
						ServerEvents.permit.remove(p_11258_.getId().toString());
						return new TextComponent("\u00A7cKicked from the server!\n\n\u00A7cThe server denied your request: \nYou may not be permitted to join or entered an invalid password!");
					} else {

						return new TextComponent("\u00A7cKicked from the server!\n\n\u00A7cThe server denied your request: \nYou may not be permitted to join or entered an invalid password!");
					}
				}

			} else {
				ServerEvents.allowed.remove(p_11258_.getId().toString());
				return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(p_11258_) ? new TranslatableComponent("multiplayer.disconnect.server_full") : null;
			}
		} else {
			return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(p_11258_) ? new TranslatableComponent("multiplayer.disconnect.server_full") : null;
		}
	}

	@Shadow
	public abstract boolean isWhiteListed(GameProfile p_152607_1_);

	@Shadow
	public abstract boolean canBypassPlayerLimit(GameProfile p_183023_1_);
}