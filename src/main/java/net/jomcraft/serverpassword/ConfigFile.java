package net.jomcraft.serverpassword;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public final class ConfigFile {
    private transient final Configuration config;

    public static String password;
    public static List<String> whitelist;

    public ConfigFile(File file) {
        config = new Configuration(file);
        addConfigValues();
    }

    private void addConfigValues() {
        config.setCategoryComment("General", "Settings which apply as main configuration of the mod.");
        password = config.get("General", "Password", "", "Set the server's password <Encrypted>").getString();
        whitelist = java.util.Arrays.asList(config.get("General", "Whitelist", new String[]{"*"}, "Ussers which are allowed to enter the password").getStringList());
        config.save();
    }

    public void syncConfiguration() {
        config.load();
        addConfigValues();
        config.save();
    }

    public Configuration getInstance() {
        return config;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<IConfigElement> getCategories() {
        List<IConfigElement> elements = new ArrayList<IConfigElement>();

        for (String s : config.getCategoryNames()) {
            IConfigElement element = new ConfigElement(config.getCategory(s));
            for (IConfigElement e : (List<IConfigElement>) element.getChildElements()) {
                elements.add(e);
            }

        }
        return elements;
    }

}
