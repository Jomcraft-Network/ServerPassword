package net.jomcraft.serverpassword;

import org.apache.logging.log4j.Level;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.TreeMap;
import javax.crypto.SecretKey;

public class Controller extends Thread {

    private final int port;

    public Controller(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket listener = new ServerSocket(port)) {

            while (!listener.isClosed()) {
                if (CommonProxy.users_c <= 2) {
                    CommonProxy.users_c++;

                    new ListenThread(listener.accept(), port).start();

                } else {

                    listener.accept().close();
                }

            }
        } catch (IOException ex) {
            ServerPassword.log.log(Level.INFO, ex);
        }

    }

    private static class ListenThread extends Thread {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private int port;

        private TreeMap<String, SecretKey> keyStore = new TreeMap<>();

        public ListenThread(Socket socket, int port) {
            this.socket = socket;
            this.port = port;
        }

        @SuppressWarnings("static-access")
        private void connect() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
            this.socket.setTcpNoDelay(true);
            this.socket.setKeepAlive(true);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            String client = this.socket.getInetAddress().getHostAddress();
            String keyStoreName = client + " on " + port;

            SecretKey key = null;
            boolean hasKey = this.keyStore.containsKey(keyStoreName);
            byte[] keyHash;

            if (hasKey) {
                key = this.keyStore.get(keyStoreName);
                keyHash = Crypter.hash("SHA-512", key.getEncoded());
            } else {
                keyHash = "server".getBytes();
            }
            this.out.writeInt(keyHash.length);
            this.out.write(keyHash);

            Integer length = in.readInt();
            byte[] clientKey = new byte[length];

            this.in.read(clientKey);

            boolean correctKey = Arrays.equals(clientKey, keyHash);

            if (!correctKey) {
                KeyPair keyPair = Crypter.generateECKeyPair(ECKey.MAX.size());
                Integer length2 = in.readInt();
                byte[] encoded = new byte[length2];
                this.in.read(encoded);

                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encoded);
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

                this.out.writeInt(keyPair.getPublic().getEncoded().length);
                this.out.write(keyPair.getPublic().getEncoded());
                key = Crypter.generateEC("SHA-512", AESKey.MIN.size(), keyPair.getPrivate(), publicKey);

                if (hasKey) {
                    this.keyStore.remove(keyStoreName);
                }
                this.keyStore.put(keyStoreName, key);
            }

            Integer length2 = this.in.readInt();
            byte[] encoded = new byte[length2];
            this.in.read(encoded);

            byte[] decryptedObject = Crypter.decrypt("AES", (byte[]) encoded, key);

            String object = new String(decryptedObject);
            String[] args = object.split("<na>");
            if (args[0].equals("SWP|Q")) {

                byte[] byteResponse = null;
                if (CommonProxy.getConfig().password.equals("")) {
                    byteResponse = "0".getBytes();
                } else {
                    byteResponse = "1".getBytes();
                }

                byte[] encryptedResponse = Crypter.encrypt("AES", byteResponse, key);
                this.out.writeInt(encryptedResponse.length);
                this.out.write(encryptedResponse);

            } else if (args[0].equals("SWP|P")) {

                String uuid = args[1];

                if (CommonProxy.getConfig().whitelist.contains("*")) {

                    String pw = args[2];
                    if (PWHashing.validatePassword(pw, CommonProxy.getConfig().password)) {

                        ServerPassword.allowed.add(uuid);
                    }

                } else if (CommonProxy.getConfig().whitelist.contains(uuid)) {

                    String pw = args[2];
                    if (PWHashing.validatePassword(pw, CommonProxy.getConfig().password)) {
                        ServerPassword.allowed.add(uuid);
                    }

                } else {
                    CommonProxy.permit.add(uuid);
                }

            }

            this.out.flush();
        }

        public void run() {
            try {
                String address = this.socket.getRemoteSocketAddress().toString().split("/")[1].split(":")[0];
                if (CommonProxy.time.containsKey(address)) {

                    long bi1 = CommonProxy.time.get(address);
                    long bi2 = Long.parseLong("500");
                    long bi3 = (long) bi1 + (long) bi2;

                    if (bi3 > System.currentTimeMillis()) {
                        CommonProxy.fast.add(address);
                    } else {
                        //CALL
                        connect();
                        CommonProxy.time.put(address, System.currentTimeMillis());
                    }

                } else {
                    //CALL
                    connect();
                    CommonProxy.time.put(address, System.currentTimeMillis());
                }

            } catch (ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException | IOException |
                     InvalidKeyException e) {
                ServerPassword.log.log(Level.INFO, e);

            } finally {
                CommonProxy.users_c--;

                try {
                    if (this.in != null) {
                        this.in.close();
                    }

                    if (this.out != null) {
                        this.out.close();
                    }

                    if (!this.socket.isClosed()) {
                        this.socket.close();
                    }
                } catch (IOException ex) {
                    ServerPassword.log.log(Level.INFO, ex);
                }

            }
        }

    }

}