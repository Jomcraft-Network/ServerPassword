/* 
 *      ServerPassword - 1.12.2 <> Codedesign by PT400C; Supported by julKoeln and PT400C - AESKey Enum
 *      � Jomcraft-Network 2018
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