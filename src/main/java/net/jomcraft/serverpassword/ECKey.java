/* 
 *      ServerPassword - 1.16.5 <> Codedesign by PT400C and Compaszer
 *      © Jomcraft-Network 2021
 */
package net.jomcraft.serverpassword;

public enum ECKey {

	MIN(224), MEDIUM(256), MAX(384);

	private final int size;

	ECKey(int size) {
		this.size = size;
	}

	public int size() {
		return this.size;
	}
}