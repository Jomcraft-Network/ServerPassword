package net.jomcraft.serverpassword;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.DependsOn;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@DependsOn("forge")
@IFMLLoadingPlugin.TransformerExclusions({"net.jomcraft.serverpassword"})
public class ServerPasswordPlugin implements IFMLLoadingPlugin {

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"net.jomcraft.serverpassword.ServerPasswordClassTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

}
