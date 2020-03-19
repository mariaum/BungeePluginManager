package bungeepluginmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePluginManager extends Plugin {

	@Override
	public void onEnable() {
		if (!ReflectionUtils.init(this)) {
			return;
		}
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Commands(this));
		ReflectionUtils.setFieldValue(ProxyServer.getInstance().getPluginManager(), "eventBus", new ModifiedPluginEventBus());
	}

}
