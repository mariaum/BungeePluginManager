package bungeepluginmanager;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventBus;

public class ModifiedPluginEventBus extends EventBus {

	private static final Set<AsyncEvent<?>> uncompletedEvents = Collections.newSetFromMap(new WeakHashMap<AsyncEvent<?>, Boolean>());
	private static final Object lock = new Object();

	public static void completeIntents(Plugin plugin) {
		synchronized (lock) {
			for (AsyncEvent<?> event : uncompletedEvents) {
				try {
					while (hasIntent(event, plugin)) {
						event.completeIntent(plugin);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	private static boolean hasIntent(AsyncEvent<?> event, Plugin plugin) {
		AtomicInteger intentCount = ReflectionUtils.getIntents(event).get(plugin);
		return intentCount != null && intentCount.get() > 0;
	}

	@Override
	public void post(Object event) {
		if (event instanceof AsyncEvent) {
			synchronized (lock) {
				uncompletedEvents.add((AsyncEvent<?>) event);	
			}
		}
		super.post(event);
	}

}
