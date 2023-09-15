package net.jomcraft.serverpassword;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.server.dedicated.PropertyManager;

public class CommonProxy {

    public static int users_c = 0;
    public static ConfigFile config;
    public static HashMap<String, Long> time = new HashMap<>();
    public static ArrayList<String> fast = new ArrayList<>();
    public static ArrayList<String> permit = new ArrayList<>();
    public static int server_port;

    public void preInit(FMLPreInitializationEvent e) {

        if (e.getSide().equals(Side.SERVER)) {
            PropertyManager settings = new PropertyManager(new File("server.properties"));
            server_port = (int) (Integer.valueOf(settings.getIntProperty("server-port", 25565) + 512));
            config = new ConfigFile(e.getSuggestedConfigurationFile());
            ServerPassword.log(Level.INFO, "Initialized ServerPassword server-side!");
            new Controller(CommonProxy.server_port).start();
        }
    }

    public void init(FMLInitializationEvent e) {

    }


    public void construction(FMLConstructionEvent e) {

    }

    public void postInit(FMLPostInitializationEvent e) {

        users_c = 0;

    }

    public static ConfigFile getConfig() {
        return config;
    }

}