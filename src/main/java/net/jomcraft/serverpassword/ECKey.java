/* 
 *      ServerPassword - 1.12.2 <> Codedesign by PT400C; Supported by julKoeln and PT400C - ECKey Enum
 *      © Jomcraft-Network 2018
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