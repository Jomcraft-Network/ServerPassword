package net.jomcraft.serverpassword.mixin;

import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.mojang.authlib.GameProfile;
import net.jomcraft.serverpassword.ConfigManager;
import net.jomcraft.serverpassword.ServerEvents;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.server.management.IPBanList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.ProfileBanEntry;
import net.minecraft.server.management.WhiteList;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mixin({ PlayerList.class })
public abstract class MixinPlayerList {

	@Shadow
	public static File USERBANLIST_FILE;
	@Shadow
	public static File IPBANLIST_FILE;
	@Shadow
	public static File WHITELIST_FILE;

	@Shadow
	private List<ServerPlayerEntity> players;

	@Shadow
	private BanList bans;

	@Shadow
	private IPBanList ipBans;

	@Shadow
	private WhiteList whitelist;

	@Shadow
	protected int maxPlayers;

	@Shadow
	private static SimpleDateFormat BAN_DATE_FORMAT;

	@Overwrite
	@Nullable
	public ITextComponent canPlayerLogin(SocketAddress p_206258_1_, GameProfile p_206258_2_) {

		if (this.bans.isBanned(p_206258_2_)) {
			ServerEvents.allowed.remove(p_206258_2_.getId().toString());
			ProfileBanEntry profilebanentry = this.bans.get(p_206258_2_);
			IFormattableTextComponent iformattabletextcomponent1 = new TranslationTextComponent("multiplayer.disconnect.banned.reason", profilebanentry.getReason());
			if (profilebanentry.getExpires() != null) {
				iformattabletextcomponent1.append(new TranslationTextComponent("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(profilebanentry.getExpires())));
			}

			return iformattabletextcomponent1;
		} else if (!this.isWhiteListed(p_206258_2_)) {
			ServerEvents.allowed.remove(p_206258_2_.getId().toString());
			return new TranslationTextComponent("multiplayer.disconnect.not_whitelisted");
		} else if (this.ipBans.isBanned(p_206258_1_)) {
			ServerEvents.allowed.remove(p_206258_2_.getId().toString());
			IPBanEntry ipbanentry = this.ipBans.get(p_206258_1_);
			IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent("multiplayer.disconnect.banned_ip.reason", ipbanentry.getReason());
			if (ipbanentry.getExpires() != null) {
				iformattabletextcomponent.append(new TranslationTextComponent("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanentry.getExpires())));
			}

			return iformattabletextcomponent;
		} else if (ServerLifecycleHooks.getCurrentServer().isDedicatedServer()) {
			if (ConfigManager.SERVER.password.get().equals("")) {

				return new StringTextComponent("\u00A7cKicked from the server!\n\n\u00A7bThe default password has to be changed by an admin!");

			} else if (!ServerEvents.allowed.contains(p_206258_2_.getId().toString())) {

				if (ServerEvents.fast.contains(p_206258_1_.toString().split("/")[0])) {
					ServerEvents.fast.remove(p_206258_1_.toString().split("/")[0]);
					return new StringTextComponent("\u00A7cKicked from the server!\n\n\u00A7cThe request came to quickly!");

				} else {

					if (ServerEvents.permit.contains(p_206258_2_.getId().toString())) {
						ServerEvents.permit.remove(p_206258_2_.getId().toString());
						return new StringTextComponent("\u00A7cKicked from the server!\n\n\u00A7cThe server denied your request: \nYou may not be permitted to join or entered an invalid password!");
					} else {

						return new StringTextComponent("\u00A7cKicked from the server!\n\n\u00A7cThe server denied your request: \nYou may not be permitted to join or entered an invalid password!");
					}
				}

			} else {
				ServerEvents.allowed.remove(p_206258_2_.getId().toString());
				return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(p_206258_2_) ? new TranslationTextComponent("multiplayer.disconnect.server_full") : null;
			}
		} else {
			return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(p_206258_2_) ? new TranslationTextComponent("multiplayer.disconnect.server_full") : null;
		}
	}

	@Shadow
	public abstract boolean isWhiteListed(GameProfile p_152607_1_);

	@Shadow
	public abstract boolean canBypassPlayerLimit(GameProfile p_183023_1_);
}