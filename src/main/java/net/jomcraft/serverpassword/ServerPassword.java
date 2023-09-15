package net.jomcraft.serverpassword;

import java.util.ArrayList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;

@Mod(modid = ServerPassword.MODID, version = ServerPassword.VERSION, canBeDeactivated = false, acceptableRemoteVersions = "*")
public class ServerPassword {

    public static final String MODID = "serverpw";
    public static final String VERSION = "SP-VERSION";
    public static ArrayList<String> allowed = new ArrayList<>();
    public static final Logger log = LogManager.getLogger(ServerPassword.MODID);

    @SidedProxy(serverSide = "net.jomcraft.serverpassword.CommonProxy", clientSide = "net.jomcraft.serverpassword.ClientProxy")
    private static CommonProxy proxy;

    private static EncryptPatch ec;

    @Instance(value = "serverpw")
    private static ServerPassword instance;

    public static CommonProxy proxy() {
        return proxy;
    }

    public static EncryptPatch ec() {

        if (ec == null) {
            ec = new EncryptPatch();
        }
        return ec;
    }

    public static ServerPassword instance() {

        if (instance == null) {
            instance = new ServerPassword();
        }
        return instance;

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }

    @EventHandler
    public void construction(FMLConstructionEvent e) {
        proxy.construction(e);
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent e) {
        if (e.getSide().equals(Side.SERVER)) e.registerServerCommand((ICommand) new CommandPassword());
    }

    public static void log(Level level, String msg) {
        LogManager.getLogger("" + ServerPassword.MODID).log(level, msg);
    }

    public static boolean isCauldron() {
        try {
            if (Class.forName("thermos.ThermosClassTransformer") != null) {

                return true;

            }
        } catch (ClassNotFoundException e) {

        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unused"})
    public static boolean isDedi() {
        boolean isDedi;
        try {
            Class c = Minecraft.class;
            isDedi = false;
        } catch (NoClassDefFoundError e) {
            isDedi = true;
        }
        return isDedi;
    }
}