/* 
 *		ServerPassword - 1.19.x <> Codedesign by PT400C and Compaszer
 *		© Jomcraft-Network 2022
 */
package net.jomcraft.serverpassword;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.TreeMap;
import javax.crypto.SecretKey;

import org.apache.logging.log4j.Level;

public class Controller extends Thread {

	private final int port;

	ServerSocket listener = null;

	public Controller(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			Controller.this.listener = new ServerSocket(port);
			while (!listener.isClosed()) {
				if (ServerEvents.users_c <= 2) {
					ServerEvents.users_c++;
					new ListenThread(listener.accept(), port).start();

				} else {

					listener.accept().close();
				}

			}
		} catch (SocketException ex) {
			ServerPassword.log.log(Level.DEBUG, ex);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				Controller.this.listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

		private void connect() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
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
				if (ConfigManager.SERVER.password.get().equals("")) {
					byteResponse = "0".getBytes();
				} else {
					byteResponse = "1".getBytes();
				}

				byte[] encryptedResponse = Crypter.encrypt("AES", byteResponse, key);
				this.out.writeInt(encryptedResponse.length);
				this.out.write(encryptedResponse);

			} else if (args[0].equals("SWP|P")) {

				String uuid = args[1];

				if (ConfigManager.SERVER.whitelist.get().contains("*")) {

					String pw = args[2];

					if (PWHashing.validatePassword(pw, ConfigManager.SERVER.password.get())) {
						ServerEvents.allowed.add(uuid);
					}

				} else if (ConfigManager.SERVER.whitelist.get().contains(uuid)) {

					String pw = args[2];

					if (PWHashing.validatePassword(pw, ConfigManager.SERVER.password.get())) {
						ServerEvents.allowed.add(uuid);
					}

				} else {
					ServerEvents.permit.add(uuid);
				}
			}

			this.out.flush();
		}

		public void run() {
			try {
				String address = this.socket.getRemoteSocketAddress().toString().split("/")[0];
				if (ServerEvents.time.containsKey(address)) {

					long bi1 = ServerEvents.time.get(address);
					long bi2 = Long.parseLong("500");
					long bi3 = (long) bi1 + (long) bi2;

					if (bi3 > System.currentTimeMillis()) {

						ServerEvents.fast.add(address);

					} else {
						// CALL
						connect();
						ServerEvents.time.put(address, System.currentTimeMillis());

					}

				} else {
					// CALL
					connect();
					ServerEvents.time.put(address, System.currentTimeMillis());
				}

			} catch (Exception e) {

				e.printStackTrace();

			} finally {
				ServerEvents.users_c--;

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
					ex.printStackTrace();
				}
			}
		}
	}
}