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
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Base64;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import cpw.mods.fml.client.config.GuiCheckBox;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.util.concurrent.GenericFutureListener;

@SideOnly(Side.CLIENT)
public class GuiPassword extends GuiScreen {
    private final GuiScreen lastScreen;
    private final TextField ipEdit;
    private static GuiConnecting guiClass = null;
    private final InetAddress inetaddress;
    private final String ip;
    private final int port;
    private final int conn;
    private GuiCheckBox keepPW;
    private final String pw;

    @SuppressWarnings("unchecked")
    public GuiPassword(GuiScreen lastScreenIn, GuiConnecting gui, InetAddress inetaddress, String ip, int port, int conn, String pw) {
        boolean enabled = false;
        if (pw != null) {
            enabled = true;
        }
        this.conn = conn;
        this.port = port;
        this.ip = ip;
        this.inetaddress = inetaddress;
        guiClass = gui;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, "Enter Password"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, "Cancel"));
        this.buttonList.add(keepPW = new GuiCheckBox(2003, this.width / 2 - 100, this.height / 4 + 87, "Store Password", true));
        keepPW.setIsChecked(enabled);
        this.ipEdit = new TextField(2, this.fontRendererObj, this.width / 2 - 100, 116, 200, 20);
        this.ipEdit.setMaxStringLength(16);
        this.ipEdit.setFocused(true);
        if (enabled) ipEdit.setText(pw);
        this.pw = pw;

        this.lastScreen = lastScreenIn;
    }

    public void updateScreen() {
        this.ipEdit.updateCursorCounter();
    }

    @SuppressWarnings("unchecked")
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + 12, "Enter Password"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + 12, "Cancel"));
        keepPW.xPosition = this.width / 2 - 100;
        keepPW.yPosition = this.height / 4 + 87;
        @SuppressWarnings("unused") boolean enabled = false;
        if (pw != null) {
            enabled = true;
        }
        this.buttonList.add(keepPW);
        this.ipEdit.x = this.width / 2 - 100;
        this.ipEdit.y = 116;
        this.ipEdit.width = 200;
        this.ipEdit.height = 20;
        this.ipEdit.setMaxStringLength(16);
        this.ipEdit.setFocused(true);
        ((GuiButton) this.buttonList.get(0)).enabled = !this.ipEdit.getText().isEmpty();
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @SuppressWarnings("static-access")
    protected void actionPerformed(GuiButton button) {

        if (button.enabled && button.id == 0) {

            if (keepPW.isChecked()) {

                String query = new String(Base64.getEncoder().encode((GuiPassword.this.ip + ":" + GuiPassword.this.port).getBytes()));
                String password = new String(Base64.getEncoder().encode(GuiPassword.this.ipEdit.getText().getBytes()));

                ClientProxy.passwords.put(GuiPassword.this.ip + ":" + GuiPassword.this.port, GuiPassword.this.ipEdit.getText());
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
                if (ClientProxy.passwords.containsKey(GuiPassword.this.ip + ":" + GuiPassword.this.port))
                    ClientProxy.passwords.remove(GuiPassword.this.ip + ":" + GuiPassword.this.port);

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

            this.mc.displayGuiScreen(GuiPassword.this.guiClass);

            (new Thread("Server Connector #" + GuiPassword.this.conn) {
                public void run() {

                    try {
                        EncryptPatch.connectToServer(GuiPassword.this.ip, (byte) 1, GuiPassword.this.ipEdit.getText());
                    } catch (IOException e1) {

                        e1.printStackTrace();
                    }

                    NetworkManager temp = NetworkManager.provideLanClient(GuiPassword.this.inetaddress, GuiPassword.this.port);

                    temp.setNetHandler(new NetHandlerLoginClient(temp, GuiPassword.this.mc, GuiPassword.this.lastScreen));
                    temp.scheduleOutboundPacket(new C00Handshake(5, GuiPassword.this.ip, GuiPassword.this.port, EnumConnectionState.LOGIN), new GenericFutureListener[0]);
                    temp.scheduleOutboundPacket(new C00PacketLoginStart(mc.getSession().func_148256_e()), new GenericFutureListener[0]);

                    storeValue(temp);

                }
            }).start();
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(lastScreen);
        }

    }

    private static void storeValue(NetworkManager man) {
        try {

            Field f2 = guiClass.getClass().getDeclaredField("field_146371_g");
            f2.setAccessible(true);
            f2.set(guiClass, man);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (this.ipEdit.textboxKeyTyped(typedChar, keyCode)) {
            ((GuiButton) this.buttonList.get(0)).enabled = !this.ipEdit.getText().isEmpty();
        } else if (keyCode == 28 || keyCode == 156) {
            this.actionPerformed((GuiButton) this.buttonList.get(0));
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.ipEdit.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(Minecraft.getMinecraft().fontRenderer, "Password Request", this.width / 2, 20, 16777215);
        this.drawString(Minecraft.getMinecraft().fontRenderer, "Enter the password here:", this.width / 2 - 100, 100, 10526880);
        this.ipEdit.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}