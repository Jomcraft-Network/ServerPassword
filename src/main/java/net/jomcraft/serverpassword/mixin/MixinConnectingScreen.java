/* 
 *      ServerPassword - 1.16.5 <> Codedesign by PT400C and Compaszer
 *      © Jomcraft-Network 2021
 */
package net.jomcraft.serverpassword.mixin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.jomcraft.serverpassword.ServerPassword;
import net.jomcraft.serverpassword.EncryptPatch;
import net.jomcraft.serverpassword.GuiPassword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

@Mixin({ ConnectingScreen.class })
public abstract class MixinConnectingScreen {

	public InetAddress inetaddress = null;

	@Shadow
	private static final Logger LOGGER = LogManager.getLogger();

	@Shadow
	private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);

	@Shadow
	private boolean aborted;

	@Shadow
	private NetworkManager connection;

	@Shadow
	private final Screen parent = null;

	@Shadow
	private ITextComponent status = new TranslationTextComponent("connect.connecting");

	public String pw = null;

	@Inject(at = @At("HEAD"), method = "connect(Ljava/lang/String;I)V", cancellable = true)
	private void testStuff(final String p_146367_1_, final int p_146367_2_, CallbackInfo ci) {
		LOGGER.info("Connecting to {}, {}", p_146367_1_, p_146367_2_);
		ServerPassword.port = Integer.valueOf(p_146367_2_) + 512;
		int s = UNIQUE_THREAD_ID.incrementAndGet();
		Thread thread = new Thread("Server Connector #" + s) {
			public void run() {
				Minecraft minecraft = Minecraft.getInstance();
				try {
					if (MixinConnectingScreen.this.aborted) {
						return;
					}

					MixinConnectingScreen.this.inetaddress = InetAddress.getByName(p_146367_1_);
					boolean password = EncryptPatch.connectToServer(p_146367_1_, (byte) 0, null);

					if (password) {

						if (ServerPassword.passwords.containsKey(p_146367_1_ + ":" + p_146367_2_)) {
							MixinConnectingScreen.this.pw = ServerPassword.passwords.get(p_146367_1_ + ":" + p_146367_2_);
						}

						minecraft.execute(() -> {
							minecraft.setScreen(new GuiPassword(parent, ((ConnectingScreen) (Object) MixinConnectingScreen.this), MixinConnectingScreen.this.inetaddress, p_146367_1_, p_146367_2_, s, MixinConnectingScreen.this.pw));
						});

					} else {

						MixinConnectingScreen.this.connection = NetworkManager.connectToServer(inetaddress, p_146367_2_, minecraft.options.useNativeTransport());
						MixinConnectingScreen.this.connection.setListener(new ClientLoginNetHandler(MixinConnectingScreen.this.connection, minecraft, MixinConnectingScreen.this.parent, (p_209549_1_) -> {
							MixinConnectingScreen.this.status = p_209549_1_;
						}));
						MixinConnectingScreen.this.connection.send(new CHandshakePacket(p_146367_1_, p_146367_2_, ProtocolType.LOGIN));
						MixinConnectingScreen.this.connection.send(new CLoginStartPacket(minecraft.getUser().getGameProfile()));
					}
				} catch (UnknownHostException unknownhostexception) {
					if (MixinConnectingScreen.this.aborted) {
						return;
					}

					LOGGER.error("Couldn't connect to server", (Throwable) unknownhostexception);
					minecraft.execute(() -> {
						minecraft.setScreen(new DisconnectedScreen(MixinConnectingScreen.this.parent, DialogTexts.CONNECT_FAILED, new TranslationTextComponent("disconnect.genericReason", "Unknown host")));
					});
				} catch (Exception exception) {
					if (MixinConnectingScreen.this.aborted) {
						return;
					}

					LOGGER.error("Couldn't connect to server", (Throwable) exception);
					String s = inetaddress == null ? exception.toString() : exception.toString().replaceAll(inetaddress + ":" + p_146367_2_, "");
					minecraft.execute(() -> {
						minecraft.setScreen(new DisconnectedScreen(MixinConnectingScreen.this.parent, DialogTexts.CONNECT_FAILED, new TranslationTextComponent("disconnect.genericReason", s)));
					});
				}

			}
		};
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
		ci.cancel();
	}
}