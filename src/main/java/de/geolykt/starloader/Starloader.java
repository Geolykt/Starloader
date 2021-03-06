package de.geolykt.starloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.geolykt.starloader.launcher.Launcher;
import de.geolykt.starloader.mod.Extension;
import de.geolykt.starloader.mod.ExtensionManager;

public final class Starloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Starloader.class);

    private static Starloader instance;

    private static ExtensionManager extensions;

    private Starloader() {}

    public static void init() {
        if (instance != null) {
            throw new IllegalStateException("Starloader initialized twice!");
        }
        LOGGER.info("Java version: {}", System.getProperty("java.version"));
        instance = new Starloader();
        extensions = new ExtensionManager();
        // TODO get configuration via dependency injection, not via the singleton pattern.
        // I tried reflection, but for some odd reason it does not find the methods. Java is strange I guess
        extensions.loadExtensions(Launcher.INSTANCE.configuration.getExtensionList());
        long start = System.currentTimeMillis();
        LOGGER.info("Initializing extension: preinit");
        extensions.getExtensions().forEach(Extension::preInitialize);
        LOGGER.info("Initializing extension: init");
        extensions.getExtensions().forEach(extension -> {
            extension.initialize();
            LOGGER.info("Initialized extension {}.", extension.getDescription().getName());
        });
        LOGGER.info("Initializing extension: postinit");
        extensions.getExtensions().forEach(Extension::postInitialize);
        LOGGER.info("All Extensions initialized within {}ms", (System.currentTimeMillis() - start));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // FIXME don't use shutdown hooks and/or have them deadlock-proof.
            extensions.shutdown();
        }, "ExtensionsShutdownThread"));
    }

    public static ExtensionManager getExtensionManager() {
        return extensions;
    }
}
