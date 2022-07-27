/* 
 *		ServerPassword - 1.18.x <> Codedesign by PT400C and Compaszer
 *		© Jomcraft-Network 2022
 */
package net.jomcraft.serverpassword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ServerPassword.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

	public static Controller controller = null;
	public static int users_c = 0;
	public static HashMap<String, Long> time = new HashMap<>();
	public static ArrayList<String> fast = new ArrayList<>();
	public static ArrayList<String> permit = new ArrayList<>();
	public static int server_port;
	public static ArrayList<String> allowed = new ArrayList<>();

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void preInit(ServerAboutToStartEvent event) {
		if (event.getServer() instanceof DedicatedServer) {
			server_port = ((DedicatedServer) event.getServer()).getProperties().serverPort + 512;
			ServerPassword.log.log(Level.INFO, "Initialized ServerPassword server-side!");
			controller = new Controller(server_port);
			controller.start();
		}
	}

	@SubscribeEvent
	public static void serverStart(ServerStartingEvent event) {
		CommandServerPassword.register(event);
	}

	@SubscribeEvent
	public static void preInit(ServerStoppingEvent event) {

		try {
			if (controller != null)
				controller.listener.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}