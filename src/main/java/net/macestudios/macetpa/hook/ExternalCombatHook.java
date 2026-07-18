package net.macestudios.macetpa.hook;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

public final class ExternalCombatHook implements CombatHook {
    private static final String[] COMBAT_METHODS = {
            "isInCombat",
            "isTagged",
            "isCombatTagged",
            "isInFight",
            "isFighting",
            "hasCombatTag"
    };
    private static final String[] REMAINING_METHODS = {
            "getRemainingCombatTime",
            "getRemainingTime",
            "getRemainingTagTime",
            "getCombatTime",
            "getTagTime",
            "getTimeLeft"
    };
    private static final String[] PROVIDER_METHODS = {
            "getCombatManager",
            "getCombatTagManager",
            "getTagManager",
            "getPlayerManager",
            "getPlayerHandler",
            "getAPI",
            "getApi"
    };
    private static final String[] DELUXE_API_CLASSES = {
            "de.timderspieler.deluxecombat.api.DeluxeCombatAPI",
            "me.timderspieler.deluxecombat.api.DeluxeCombatAPI",
            "nl.marido.deluxecombat.api.DeluxeCombatAPI",
            "com.github.timderspieler.deluxecombat.api.DeluxeCombatAPI"
    };

    private final Server server;
    private final Logger logger;
    private final String[] pluginNames;
    private final String displayName;
    private final boolean deluxeCombat;
    private Plugin plugin;
    private String status = "not installed";
    private boolean warned;

    public ExternalCombatHook(Server server, Logger logger, String pluginName, String displayName, boolean deluxeCombat) {
        this.server = server;
        this.logger = logger;
        this.pluginNames = new String[]{pluginName};
        this.displayName = displayName;
        this.deluxeCombat = deluxeCombat;
        refresh();
    }

    public ExternalCombatHook(Server server, Logger logger, String[] pluginNames, String displayName, boolean deluxeCombat) {
        this.server = server;
        this.logger = logger;
        this.pluginNames = pluginNames.clone();
        this.displayName = displayName;
        this.deluxeCombat = deluxeCombat;
        refresh();
    }

    public void refresh() {
        plugin = null;
        for (String pluginName : pluginNames) {
            plugin = server.getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                break;
            }
        }
        if (plugin == null || !plugin.isEnabled()) {
            status = "not installed";
            return;
        }
        status = hasCompatibleProvider() ? "hooked" : "detected, no compatible combat API found";
    }

    @Override
    public boolean isInCombat(UUID playerId) {
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        Player player = server.getPlayer(playerId);
        if (player == null) {
            return false;
        }
        try {
            for (Object provider : providers()) {
                Boolean result = invokeBoolean(provider, player, COMBAT_METHODS);
                if (Boolean.TRUE.equals(result)) {
                    return true;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warn(exception);
        }
        return false;
    }

    @Override
    public long remainingSeconds(UUID playerId) {
        if (plugin == null || !plugin.isEnabled()) {
            return 0;
        }
        Player player = server.getPlayer(playerId);
        if (player == null) {
            return 0;
        }
        try {
            for (Object provider : providers()) {
                Number value = invokeNumber(provider, player, REMAINING_METHODS);
                if (value != null) {
                    long seconds = value.longValue();
                    return seconds > 1000 ? Math.max(1, seconds / 1000) : Math.max(1, seconds);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warn(exception);
        }
        return isInCombat(playerId) ? 1 : 0;
    }

    @Override
    public String name() {
        return displayName;
    }

    @Override
    public String status() {
        return status;
    }

    private boolean hasCompatibleProvider() {
        Player player = server.getOnlinePlayers().stream().findFirst().orElse(null);
        if (player == null) {
            return true;
        }
        try {
            for (Object provider : providers()) {
                if (hasAnyMethod(provider, player, COMBAT_METHODS)) {
                    return true;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warn(exception);
        }
        return false;
    }

    private List<Object> providers() throws ReflectiveOperationException {
        List<Object> providers = new ArrayList<>();
        providers.add(plugin);
        for (String methodName : PROVIDER_METHODS) {
            Object provider = invokeNoArgs(plugin, methodName);
            if (provider != null) {
                providers.add(provider);
            }
        }
        Object staticApi = staticApiProvider();
        if (staticApi != null) {
            providers.add(staticApi);
        }
        return providers;
    }

    private Object staticApiProvider() throws ReflectiveOperationException {
        if (!deluxeCombat) {
            return null;
        }
        Object api = invokeStaticNoArgs(plugin.getClass(), "getAPI");
        if (api != null) {
            return api;
        }
        for (String className : DELUXE_API_CLASSES) {
            Class<?> clazz = loadPluginClass(className);
            if (clazz == null) {
                continue;
            }
            Object fromStatic = invokeStaticNoArgs(clazz, "getAPI");
            if (fromStatic != null) {
                return fromStatic;
            }
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        return null;
    }

    private Object invokeNoArgs(Object target, String methodName) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), methodName);
        if (method == null || method.getParameterCount() != 0) {
            return null;
        }
        method.setAccessible(true);
        return method.invoke(target);
    }

    private Object invokeStaticNoArgs(Class<?> type, String methodName) throws ReflectiveOperationException {
        Method method = findMethod(type, methodName);
        if (method == null || method.getParameterCount() != 0 || !Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        method.setAccessible(true);
        return method.invoke(null);
    }

    private Boolean invokeBoolean(Object target, Player player, String[] methodNames) throws ReflectiveOperationException {
        for (String methodName : methodNames) {
            Method method = findPlayerMethod(target.getClass(), methodName, player);
            if (method == null || method.getReturnType() != boolean.class && method.getReturnType() != Boolean.class) {
                continue;
            }
            method.setAccessible(true);
            return (Boolean) method.invoke(target, player);
        }
        return null;
    }

    private Number invokeNumber(Object target, Player player, String[] methodNames) throws ReflectiveOperationException {
        for (String methodName : methodNames) {
            Method method = findPlayerMethod(target.getClass(), methodName, player);
            if (method == null || !Number.class.isAssignableFrom(box(method.getReturnType()))) {
                continue;
            }
            method.setAccessible(true);
            return (Number) method.invoke(target, player);
        }
        return null;
    }

    private boolean hasAnyMethod(Object target, Player player, String[] methodNames) {
        for (String methodName : methodNames) {
            Method method = findPlayerMethod(target.getClass(), methodName, player);
            if (method != null) {
                return true;
            }
        }
        return false;
    }

    private Method findPlayerMethod(Class<?> type, String methodName, Player player) {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                continue;
            }
            if (method.getParameterTypes()[0].isAssignableFrom(player.getClass()) || method.getParameterTypes()[0].isAssignableFrom(Player.class)) {
                return method;
            }
        }
        return null;
    }

    private Method findMethod(Class<?> type, String methodName) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private Class<?> loadPluginClass(String className) {
        try {
            return Class.forName(className, true, plugin.getClass().getClassLoader());
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    private Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return switch (type.getName().toLowerCase(Locale.ROOT)) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            default -> type;
        };
    }

    private void warn(Exception exception) {
        status = "hook error: " + exception.getClass().getSimpleName();
        if (!warned) {
            warned = true;
            logger.warning("Could not read combat state from " + displayName + ": " + exception.getMessage());
        }
    }
}
