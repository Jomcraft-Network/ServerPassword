package net.jomcraft.serverpassword;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.SecretKey;
import org.apache.logging.log4j.Logger;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentTranslation;

public class EncryptPatch {

    private static TreeMap<String, SecretKey> keyStore = new TreeMap<>();
    private static GuiConnecting clazz;

    private static void storeValue(NetworkManager man) {
        try {

            Field f2 = clazz.getClass().getDeclaredField("field_146371_g");
            f2.setAccessible(true);
            f2.set(clazz, man);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private static boolean isCancelled() {
        try {
            Field f = clazz.getClass().getDeclaredField("field_146373_h");
            f.setAccessible(true);
            return ((boolean) f.get(clazz));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void connect2(GuiConnecting gui, Logger LOGGER, String ip, int port, AtomicInteger CONNECTION_ID, Minecraft mc, GuiScreen previousGuiScreen) {
        LOGGER.info("Connecting to {}, {}", ip, Integer.valueOf(port));
        int s = CONNECTION_ID.incrementAndGet();
        ClientProxy.port = Integer.valueOf(port) + 512;
        clazz = gui;
        (new Thread("Server Connector #" + s) {
            public void run() {

                InetAddress inetaddress = null;

                try {

                    if (isCancelled()) {
                        return;
                    }
                    inetaddress = InetAddress.getByName(ip);

                    boolean password = connectToServer(ip, (byte) 0, null);

                    String pw = null;
                    if (password) {

                        if (ClientProxy.passwords.containsKey(ip + ":" + port)) {
                            pw = ClientProxy.passwords.get(ip + ":" + port);
                        }

                        mc.displayGuiScreen(new GuiPassword(previousGuiScreen, gui, inetaddress, ip, port, s, pw));

                    } else {

                        NetworkManager temp = NetworkManager.provideLanClient(inetaddress, port);

                        temp.setNetHandler(new NetHandlerLoginClient(temp, mc, previousGuiScreen));
                        temp.scheduleOutboundPacket(new C00Handshake(5, ip, port, EnumConnectionState.LOGIN), new GenericFutureListener[0]);
                        temp.scheduleOutboundPacket(new C00PacketLoginStart(mc.getSession().func_148256_e()), new GenericFutureListener[0]);
                        storeValue(temp);

                    }

                } catch (UnknownHostException unknownhostexception) {
                    if (isCancelled()) {
                        return;
                    }

                    LOGGER.error("Couldn't connect to server", (Throwable) unknownhostexception);
                    mc.displayGuiScreen(new GuiDisconnected(previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[]{"Unknown host"})));
                } catch (IOException exception) {
                    if (isCancelled()) {
                        return;
                    }

                    LOGGER.error("Couldn't connect to server", (Throwable) exception);
                    String s = exception.toString();

                    if (inetaddress != null) {
                        String s1 = inetaddress + ":" + port;
                        s = s.replaceAll(s1, "");
                    }
                    mc.displayGuiScreen(new GuiDisconnected(previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[]{s})));
                }

            }
        }).start();
    }

    public static boolean connectToServer(String host, byte id, String pw) throws IOException {

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, ClientProxy.port), 2000);
            try (DataInputStream in = new DataInputStream(socket.getInputStream()); DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);

                String keyStoreName = host + " on " + ClientProxy.port;
                SecretKey key = null;
                boolean hasKey = keyStore.containsKey(keyStoreName);
                byte[] keyHash;

                if (hasKey) {
                    key = keyStore.get(keyStoreName);
                    keyHash = Crypter.hash("SHA-512", key.getEncoded());
                } else {
                    keyHash = "client".getBytes();
                }

                Integer length = in.readInt();

                byte[] clientKey = new byte[length];

                in.read(clientKey);

                boolean correctKey = Arrays.equals(clientKey, keyHash);

                out.writeInt(keyHash.length);
                out.write(keyHash);

                if (!correctKey) {
                    KeyPair keyPair = Crypter.generateECKeyPair(ECKey.MAX.size());
                    out.writeInt(keyPair.getPublic().getEncoded().length);
                    out.write(keyPair.getPublic().getEncoded());

                    Integer length2 = in.readInt();
                    byte[] encoded = new byte[length2];
                    in.read(encoded);

                    KeyFactory keyFactory = KeyFactory.getInstance("EC");
                    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encoded);
                    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
                    key = Crypter.generateEC("SHA-512", AESKey.MIN.size(), keyPair.getPrivate(), publicKey);

                    if (hasKey) {
                        keyStore.remove(keyStoreName);
                    }
                    keyStore.put(keyStoreName, key);
                }
                if (id == 0) {

                    byte[] byteObject = ("SWP|Q<na>" + Minecraft.getMinecraft().getSession().func_148256_e().getId().toString()).getBytes();

                    byte[] encryptedObject = Crypter.encrypt("AES", byteObject, key);

                    out.writeInt(encryptedObject.length);
                    out.write(encryptedObject);

                    String response = null;
                    try {
                        Integer length2 = in.readInt();
                        byte[] encoded = new byte[length2];
                        in.read(encoded);

                        byte[] decryptedResponse = Crypter.decrypt("AES", (byte[]) encoded, key);
                        response = new String(decryptedResponse);
                    } catch (NullPointerException e) {

                    }
                    if (response.equals("1")) {

                        return true;
                    }
                } else {
                    byte[] byteObject = ("SWP|P<na>" + Minecraft.getMinecraft().getSession().func_148256_e().getId().toString() + "<na>" + pw).getBytes();

                    byte[] encryptedObject = Crypter.encrypt("AES", byteObject, key);

                    out.writeInt(encryptedObject.length);
                    out.write(encryptedObject);

                }

                return false;
            } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                ex.printStackTrace();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;

    }

}