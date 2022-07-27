/* 
 *		ServerPassword - 1.19.x <> Codedesign by PT400C and Compaszer
 *		© Jomcraft-Network 2022
 */
package net.jomcraft.serverpassword;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.Base64;
import org.apache.commons.io.IOUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPassword extends Screen {
	private Screen lastScreen;
	private CustomTextFieldWidget ipEdit;
	private ConnectScreen connectionScreen = null;
	private final InetSocketAddress inetaddress;
	private final String ip;
	private final int port;
	private final int conn;
	private Checkbox keepPW;
	private final String pw;
	private Button enterButton;

	public GuiPassword(Screen lastScreenIn, ConnectScreen connectingScreen, InetSocketAddress inetaddress, String ip, int port, int conn, String pw) {
		super(Component.literal("Select Password"));
		this.conn = conn;
		this.port = port;
		this.ip = ip;
		this.inetaddress = inetaddress;
		connectionScreen = connectingScreen;

		this.pw = pw;
		this.lastScreen = lastScreenIn;
	}

	@Override
	public void tick() {

		if (ipEdit != null) {
			this.ipEdit.tick();
			enterButton.active = !this.ipEdit.getValue().isEmpty();
		}

	}

	@SuppressWarnings("deprecation")
	private void onEnter(Button button) {
		if (button.active) {

			if (keepPW.selected()) {

				String query = new String(Base64.getEncoder().encode((GuiPassword.this.ip + ":" + GuiPassword.this.port).getBytes()));
				String password = new String(Base64.getEncoder().encode(GuiPassword.this.ipEdit.getValue().getBytes()));

				ServerPassword.passwords.put(GuiPassword.this.ip + ":" + GuiPassword.this.port, GuiPassword.this.ipEdit.getValue());
				try {
					File fl = new File("./passwords.txt");

					BufferedReader br = new BufferedReader(new FileReader(fl));
					boolean ex = false;
					Integer lineN = -1;
					String line = null;
					while ((line = br.readLine()) != null) {

						if (line.startsWith(query + "<id>")) {
							ex = true;
							lineN++;
							break;
						}

					}
					br.close();

					if (!ex) {
						BufferedWriter bw = new BufferedWriter(new FileWriter(fl, true));
						bw.append("\n" + query + "<id>1<id>" + password);
						bw.close();
					} else {

						String content = IOUtils.toString(new FileInputStream(fl));
						content = content.replaceAll(line, query + "<id>1<id>" + password);
						IOUtils.write(content, new FileOutputStream(fl));

					}

				} catch (IOException ex) {
					ex.printStackTrace();
				}

			} else {
				if (ServerPassword.passwords.containsKey(GuiPassword.this.ip + ":" + GuiPassword.this.port))
					ServerPassword.passwords.remove(GuiPassword.this.ip + ":" + GuiPassword.this.port);

				try {
					File fl = new File("./passwords.txt");

					String query = new String(Base64.getEncoder().encode((GuiPassword.this.ip + ":" + GuiPassword.this.port).getBytes()));
					File tempFile = new File(fl.getAbsolutePath() + ".tmp");
					BufferedReader br = new BufferedReader(new FileReader(fl));
					PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
					String line = null;
					while ((line = br.readLine()) != null) {

						if (!line.startsWith(query + "<id>")) {

							pw.println(line);
							pw.flush();
						}

					}
					pw.close();
					br.close();

					fl.delete();

					tempFile.renameTo(fl);

				} catch (IOException ex) {
					ex.printStackTrace();
				}

			}

			this.minecraft.execute(() -> {
				this.minecraft.setScreen(GuiPassword.this.connectionScreen);
			});

			(new Thread("Server Connector #" + GuiPassword.this.conn) {
				public void run() {

					try {
						EncryptPatch.connectToServer(GuiPassword.this.ip, (byte) 1, GuiPassword.this.ipEdit.getValue());
					} catch (IOException e1) {

						e1.printStackTrace();
					}

					try {
						if (GuiPassword.this.connectionScreen.aborted) {
							return;
						}

						Connection con = Connection.connectToServer(inetaddress, minecraft.options.useNativeTransport());
						con.setListener(new ClientHandshakePacketListenerImpl(con, minecraft, GuiPassword.this.lastScreen, (p_209549_1_) -> {
							GuiPassword.this.connectionScreen.status = p_209549_1_;
						}));
						con.send(new ClientIntentionPacket(inetaddress.getHostName(), inetaddress.getPort(), ConnectionProtocol.LOGIN));
						con.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getProfileKeyPairManager().profilePublicKeyData()));

						GuiPassword.this.connectionScreen.connection = con;

					} catch (Exception exception) {
						if (GuiPassword.this.connectionScreen.aborted) {
							return;
						}

						String s = inetaddress == null ? exception.toString() : exception.toString().replaceAll(inetaddress + ":" + port, "");
						GuiPassword.this.minecraft.execute(() -> {
							GuiPassword.this.minecraft.setScreen(new DisconnectedScreen(GuiPassword.this.lastScreen, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", s)));
						});
					}

				}
			}).start();
		}
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);

		this.addRenderableWidget(this.enterButton = new Button(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20, Component.literal("Enter Password"), (p_213025_1_) -> {
			this.onEnter(p_213025_1_);
		}));
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, Component.literal("Cancel"), (p_213025_1_) -> {
			this.minecraft.setScreen(lastScreen);
		}));

		boolean enabled = false;
		if (pw != null && !pw.isEmpty()) {
			enabled = true;
		}

		keepPW = new Checkbox(this.width / 2 - 100, this.height / 4 + 70, 150, 20, Component.literal("Store Password"), true);
		keepPW.selected = enabled;

		keepPW.x = this.width / 2 - 100;
		keepPW.y = this.height / 4 + 84;
		this.ipEdit = new CustomTextFieldWidget(this.font, this.width / 2 - 100, 116, 200, 20, Component.translatable("addServer.enterIp"));
		this.children.add(keepPW);
		this.ipEdit.x = this.width / 2 - 100;
		this.ipEdit.y = 116;
		this.ipEdit.setWidth(200);
		this.ipEdit.setHeight(20);
		this.ipEdit.setMaxLength(16);
		this.ipEdit.setFocus(true);
		if (enabled)
			ipEdit.setValue(pw);
		this.setInitialFocus(this.ipEdit);
		this.children.add(ipEdit);
		this.enterButton.active = !this.ipEdit.getValue().isEmpty();
	}

	@Override
	public void resize(Minecraft p_231152_1_, int p_231152_2_, int p_231152_3_) {
		String s = this.ipEdit.getValue();
		this.init(p_231152_1_, p_231152_2_, p_231152_3_);
		this.ipEdit.setValue(s);
	}

	@Override
	public void onClose() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.renderBackground(p_230430_1_);
		drawCenteredString(p_230430_1_, this.font, "Password Request", this.width / 2, 20, 16777215);
		drawString(p_230430_1_, this.font, "Enter the password here:", this.width / 2 - 100, 100, 10526880);
		if (this.ipEdit != null) {
			this.ipEdit.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
			this.keepPW.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
		}
		super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
	}
}