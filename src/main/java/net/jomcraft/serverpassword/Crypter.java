/* 
 *      ServerPassword - 1.16.5 <> Codedesign by PT400C and Compaszer
 *      © Jomcraft-Network 2021
 */
package net.jomcraft.serverpassword;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;

public class Crypter {

	public static byte[] encrypt(String algorithm, byte[] value, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(value);
		} catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
			return null;
		}
	}

	public static byte[] decrypt(String algorithm, byte[] value, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(value);
		} catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
			return null;
		}
	}

	public static SecretKey generateAESKey(int keySize) {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(keySize);
			return keyGenerator.generateKey();
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}

	public static KeyPair generateRSAKeyPair(int keySize) {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(keySize);
			return keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}

	public static KeyPair generateECKeyPair(int keySize) {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
			keyPairGenerator.initialize(keySize);
			return keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}

	public static SecretKey generateEC(String hashAlgorithm, int keySize, PrivateKey privateKey, PublicKey publicKey) {
		try {
			KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
			keyAgreement.init(privateKey);
			keyAgreement.doPhase(publicKey, true);
			byte[] hash = hash(hashAlgorithm, keyAgreement.generateSecret());
			byte[] rawKey = Arrays.copyOfRange(hash, hash.length - keySize / 8, hash.length);
			return new SecretKeySpec(rawKey, 0, rawKey.length, "AES");
		} catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException ex) {
			return null;
		}
	}

	public static String hash(String algorithm, String value) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(value.getBytes(StandardCharsets.UTF_8));
			byte[] digest = md.digest();
			return String.format("%064x", new java.math.BigInteger(1, digest));
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}

	public static byte[] hash(String algorithm, byte[] value) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(value);
			return md.digest();
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}
}