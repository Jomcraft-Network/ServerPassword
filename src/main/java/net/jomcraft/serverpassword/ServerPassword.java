package net.jomcraft.serverpassword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("serverpassword")
public class ServerPassword {
	public static final String MODID = "serverpassword";
	public static final Logger log = LogManager.getLogger(ServerPassword.MODID);
	private static EncryptPatch ec;
	public static final String VERSION = "1.0.0";
	public static ServerPassword instance;
	public static HashMap<String, String> passwords = new HashMap<>();
	public static volatile int port = 0;

	@SuppressWarnings("deprecation")
	public ServerPassword() {
		instance = this;

		MinecraftForge.EVENT_BUS.register(ServerPassword.class);
		final ModLoadingContext modLoadingContext = ModLoadingContext.get();
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		});

		DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
			modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.SERVER_SPEC);

		});
	}

	public void preInit(FMLClientSetupEvent event) {

		ServerPassword.log.log(Level.INFO, "Initialized ServerPassword client-side!");

		try {
			File fl = new File("./passwords.txt");
			if (!fl.exists()) {
				fl.createNewFile();
			} else {

				BufferedReader br = new BufferedReader(new FileReader(fl));
				String line = null;
				while ((line = br.readLine()) != null) {

					if (!line.equals("")) {
						try {
							String[] args = line.split("<id>");
							String server = new String(Base64.getDecoder().decode(args[0].getBytes()));
							String bool = args[1];

							if (bool.equals("1")) {

								String password = new String(Base64.getDecoder().decode(args[2].getBytes()));
								passwords.put(server, password);
							}

						} catch (NullPointerException ex) {
							ex.printStackTrace();

						}
					}

				}

				br.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public static EncryptPatch ec() {

		if (ec == null) {
			ec = new EncryptPatch();
		}
		return ec;

	}

	public static ServerPassword getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public static String getModVersion() {
		// Stupid FG 3 workaround
		TomlParser parser = new TomlParser();
		InputStream stream = ServerPassword.class.getClassLoader().getResourceAsStream("META-INF/mods.toml");
		CommentedConfig file = parser.parse(stream);
		return ((ArrayList<CommentedConfig>) file.get("mods")).get(0).get("version");
	}
}