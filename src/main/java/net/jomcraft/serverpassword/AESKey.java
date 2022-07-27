/* 
 *		ServerPassword - 1.18.x <> Codedesign by PT400C and Compaszer
 *		© Jomcraft-Network 2022
 */
package net.jomcraft.serverpassword;

public enum AESKey {

	MIN(128), MEDIUM(192), MAX(256);

	private final int size;

	AESKey(int size) {
		this.size = size;
	}

	public int size() {
		return this.size;
	}
}