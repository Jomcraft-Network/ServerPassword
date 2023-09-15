package net.jomcraft.serverpassword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

public class ClientProxy extends CommonProxy {

    public static HashMap<String, String> passwords = new HashMap<>();
    public static volatile int port = 0;

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        ServerPassword.log(Level.INFO, "Initialized ServerPassword client-side!");

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
}
