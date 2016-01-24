package pl.danlz.remotecontrol.samsung.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.danlz.remotecontrol.samsung.adapter.TVAdapter;
import pl.danlz.remotecontrol.samsung.adapter.impl.DummyTVAdapterImpl;
import pl.danlz.remotecontrol.samsung.adapter.impl.TVAdapterImpl;
import pl.danlz.remotecontrol.samsung.config.Configuration;
import pl.danlz.remotecontrol.samsung.gui.MainController;
import pl.danlz.remotecontrol.samsung.gui.SettingsController;
import pl.danlz.remotecontrol.samsung.logger.Logger;
import pl.danlz.remotecontrol.samsung.upnp.UPnPAdapter;
import pl.danlz.remotecontrol.samsung.upnp.impl.UPnPAdapterImpl;

/**
 * Application context for managing beans.
 *
 * @author Leszek
 */
public final class AppCtx {

	private static final Logger LOG = Logger.getLogger(AppCtx.class);

	private final static Map<Class<?>, Object> REGISTRY = new HashMap<>();

	private static <I, B extends I> void registerBean(Class<I> iface, B bean) {
		LOG.debug("Registering bean implementation [" + bean.getClass() + "] for [" + iface + "]");
		REGISTRY.put(iface, bean);
	}

	static {
		/*
		 * Beans must be registered/created in bottom up order. Beans referenced
		 * by others beans must be created first.
		 */

		registerBean(Configuration.class, Configuration.getInstance());
		registerBean(UPnPAdapter.class, new UPnPAdapterImpl());

		String dummyStr = System.getProperty("dummyMode");
		boolean dummyMode = Boolean.valueOf(dummyStr);
		if (dummyMode) {
			registerBean(TVAdapter.class, new DummyTVAdapterImpl());
		} else {
			registerBean(TVAdapter.class, new TVAdapterImpl());
		}

		registerBean(ExecutorService.class, Executors.newSingleThreadExecutor());
		registerBean(SettingsController.class, new SettingsController());
		registerBean(MainController.class, new MainController());
	}

	/**
	 * Gets bean implementing given interface.
	 *
	 * @param iface
	 *            interface
	 * @return bean
	 */
	@SuppressWarnings("unchecked")
	public static <I> I getBean(Class<I> iface) {
		LOG.debug("Getting bean for: " + iface);
		Object bean = REGISTRY.get(iface);
		if (bean == null) {
			throw new IllegalArgumentException("No implementation found for [" + iface + "]");
		}
		return (I) bean;
	}
}
