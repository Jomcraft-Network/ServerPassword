/* 
 *      ServerPassword - 1.18.x <> Codedesign by PT400C and Compaszer
 *      © Jomcraft-Network 2021
 */
package net.jomcraft.serverpassword;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.TreeMap;
import javax.crypto.SecretKey;
import net.minecraft.client.Minecraft;

public class EncryptPatch {

	public static TreeMap<String, SecretKey> keyStore = new TreeMap<>();

	public static boolean connectToServer(String host, byte id, String pw) throws IOException {

		try (Socket socket = new Socket()) {

			socket.connect(new InetSocketAddress(host, ServerPassword.port), 60000);

			try (DataInputStream in = new DataInputStream(socket.getInputStream()); DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
				socket.setKeepAlive(true);
				socket.setTcpNoDelay(true);

				String keyStoreName = host + " on " + ServerPassword.port;
				SecretKey key = null;
				boolean hasKey = EncryptPatch.keyStore.containsKey(keyStoreName);
				byte[] keyHash;

				if (hasKey) {
					key = EncryptPatch.keyStore.get(keyStoreName);
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
						EncryptPatch.keyStore.remove(keyStoreName);
					}
					EncryptPatch.keyStore.put(keyStoreName, key);
				}
				if (id == 0) {
					byte[] byteObject = ("SWP|Q<na>" + Minecraft.getInstance().getUser().getGameProfile().getId().toString()).getBytes();

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
					} catch (Exception e) {

					}
					if (response.equals("1")) {
						return true;
					}
				} else {
					byte[] byteObject = ("SWP|P<na>" + Minecraft.getInstance().getUser().getGameProfile().getId().toString() + "<na>" + pw).getBytes();

					byte[] encryptedObject = Crypter.encrypt("AES", byteObject, key);

					out.writeInt(encryptedObject.length);
					out.write(encryptedObject);

				}

				return false;

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;

	}
}