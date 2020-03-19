package bungeepluginmanager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ReflectionUtils {

	public static Class<? extends URLClassLoader> PLUGIN_CLASSLOADER;
	public static Constructor<? extends URLClassLoader> PLUGIN_CLASSLOADER_CTR;

	private static Field ASYNC_EVENT_INTENTS;

	public static boolean init(BungeePluginManager plugin) {
		try {
			PLUGIN_CLASSLOADER = (Class<? extends URLClassLoader>) Class.forName("net.md_5.bungee.api.plugin.PluginClassloader");
			PLUGIN_CLASSLOADER_CTR = PLUGIN_CLASSLOADER.getDeclaredConstructor(ProxyServer.class, PluginDescription.class, URL[].class);
			PLUGIN_CLASSLOADER_CTR.setAccessible(true);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to reflect net.md_5.bungee.api.plugin.PluginClassloader", e);
			return false;
		}

		try {
			ASYNC_EVENT_INTENTS = AsyncEvent.class.getDeclaredField("intents");
			ASYNC_EVENT_INTENTS.setAccessible(true);
		} catch (NoSuchFieldException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to reflect net.md_5.bungee.api.event.AsyncEvent", e);
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object obj, String fieldname) {
		Class<?> clazz = obj.getClass();
		do {
			try {
				Field field = clazz.getDeclaredField(fieldname);
				field.setAccessible(true);
				return (T) field.get(obj);
			} catch (Throwable t) {
			}
		} while ((clazz = clazz.getSuperclass()) != null);
		return null;
	}

	public static void setFieldValue(Object obj, String fieldname, Object value) {
		Class<?> clazz = obj.getClass();
		do {
			try {
				Field field = clazz.getDeclaredField(fieldname);
				field.setAccessible(true);
				field.set(obj, value);
			} catch (Throwable t) {
			}
		} while ((clazz = clazz.getSuperclass()) != null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getStaticFieldValue(Class<?> clazz, String fieldname) {
		do {
			try {
				Field field = clazz.getDeclaredField(fieldname);
				field.setAccessible(true);
				return (T) field.get(null);
			} catch (Throwable t) {
			}
		} while ((clazz = clazz.getSuperclass()) != null);
		return null;
	}

	public static void invokeMethod(Object obj, String methodname, Object... args) {
		Class<?> clazz = obj.getClass();
		do {
			try {
				for (Method method : clazz.getDeclaredMethods()) {
					if (method.getName().equals(methodname) && method.getParameterTypes().length == args.length) {
						method.setAccessible(true);
						method.invoke(obj, args);
					}
				}
			} catch (Throwable t) {
			}
		} while ((clazz = clazz.getSuperclass()) != null);
	}

	public static Map<Plugin, AtomicInteger> getIntents(AsyncEvent<?> event) {
		try {
			return (Map<Plugin, AtomicInteger>) ASYNC_EVENT_INTENTS.get(event);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
