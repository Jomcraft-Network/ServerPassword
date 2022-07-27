/* 
 *		ServerPassword - 1.19.x <> Codedesign by PT400C and Compaszer
 *		© Jomcraft-Network 2022
 */
package net.jomcraft.serverpassword.mixin;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.jomcraft.serverpassword.ServerPassword;
import net.jomcraft.serverpassword.EncryptPatch;
import net.jomcraft.serverpassword.GuiPassword;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;

@Mixin({ ConnectScreen.class })
public abstract class MixinConnectingScreen {

	@Shadow
	private static AtomicInteger UNIQUE_THREAD_ID;

	@Shadow
	static Logger LOGGER;

	@Shadow
	private static long NARRATION_DELAY_MS;

	@Shadow
	public static Component UNKNOWN_HOST_MESSAGE;

	@Nullable
	@Shadow
	public volatile Connection connection;

	@Shadow
	public volatile boolean aborted;

	@Shadow
	public Screen parent;

	public String pw = null;

	public InetSocketAddress inetsocketaddress = null;

	@Inject(at = @At("HEAD"), method = "connect(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;)V", cancellable = true)
	private void connect(final Minecraft p_169265_, final ServerAddress p_169266_, CallbackInfo ci) {
		LOGGER.info("Connecting to {}, {}", p_169266_.getHost(), p_169266_.getPort());
		ServerPassword.port = Integer.valueOf(p_169266_.getPort()) + 512;
		int s = UNIQUE_THREAD_ID.incrementAndGet();
		Thread thread = new Thread("Server Connector #" + s) {
			public void run() {
				try {
					if (MixinConnectingScreen.this.aborted) {
						return;
					}

					Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(p_169266_).map(ResolvedServerAddress::asInetSocketAddress);
					if (MixinConnectingScreen.this.aborted) {
						return;
					}

					if (!optional.isPresent()) {
						p_169265_.execute(() -> {
							p_169265_.setScreen(new DisconnectedScreen(MixinConnectingScreen.this.parent, CommonComponents.CONNECT_FAILED, ConnectScreen.UNKNOWN_HOST_MESSAGE));
						});
						return;
					}

					MixinConnectingScreen.this.inetsocketaddress = optional.get();

					boolean password = EncryptPatch.connectToServer(p_169266_.getHost(), (byte) 0, null);
					if (password) {
						if (ServerPassword.passwords.containsKey(p_169266_.getHost() + ":" + p_169266_.getPort())) {
							MixinConnectingScreen.this.pw = ServerPassword.passwords.get(p_169266_.getHost() + ":" + p_169266_.getPort());
						}

						p_169265_.execute(() -> {
							p_169265_.setScreen(new GuiPassword(parent, ((ConnectScreen) (Object) MixinConnectingScreen.this), inetsocketaddress, p_169266_.getHost(), p_169266_.getPort(), s, MixinConnectingScreen.this.pw));
						});

					} else {
						MixinConnectingScreen.this.connection = Connection.connectToServer(inetsocketaddress, p_169265_.options.useNativeTransport());
						MixinConnectingScreen.this.connection.setListener(new ClientHandshakePacketListenerImpl(MixinConnectingScreen.this.connection, p_169265_, MixinConnectingScreen.this.parent, MixinConnectingScreen.this::updateStatus));
						MixinConnectingScreen.this.connection.send(new ClientIntentionPacket(inetsocketaddress.getHostName(), inetsocketaddress.getPort(), ConnectionProtocol.LOGIN));
						MixinConnectingScreen.this.connection.send(new ServerboundHelloPacket(p_169265_.getUser().getName(), p_169265_.getProfileKeyPairManager().profilePublicKeyData()));
					}
				} catch (Exception exception2) {
					if (MixinConnectingScreen.this.aborted) {
						return;
					}

					Throwable throwable = exception2.getCause();
					Exception exception;
					if (throwable instanceof Exception) {
						Exception exception1 = (Exception) throwable;
						exception = exception1;
					} else {
						exception = exception2;
					}

					MixinConnectingScreen.LOGGER.error("Couldn't connect to server", (Throwable) exception2);
					String s = inetsocketaddress == null ? exception.getMessage() : exception.getMessage().replaceAll(inetsocketaddress.getHostName() + ":" + inetsocketaddress.getPort(), "").replaceAll(inetsocketaddress.toString(), "");
					p_169265_.execute(() -> {
						p_169265_.setScreen(new DisconnectedScreen(MixinConnectingScreen.this.parent, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", s)));
					});
				}

			}
		};
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
		ci.cancel();
	}

	@Shadow
	abstract void updateStatus(Component p_95718_);
}