# MaceTPA release obfuscation rules.
# The normal Gradle build remains readable; only ./gradlew obfuscate uses these rules.

-dontshrink
-dontoptimize
-ignorewarnings

-keepattributes StackMapTable,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,Signature,InnerClasses,EnclosingMethod

# Bukkit/Paper loads the main class from plugin.yml by name.
-keep public class net.macestudios.macetpa.MaceTPA extends org.bukkit.plugin.java.JavaPlugin {
    public <init>();
    public void onEnable();
    public void onDisable();
}

# Keep any JavaPlugin subclass safe for Bukkit plugin loading.
-keep public class * extends org.bukkit.plugin.java.JavaPlugin {
    public <init>();
    public void onEnable();
    public void onDisable();
}

# Commands are registered with executor instances and should keep stable public entrypoints.
-keep class net.macestudios.macetpa.command.** {
    public <init>(...);
    public boolean onCommand(...);
}

# Listener constructors and event handler methods must remain usable by Bukkit.
-keep class net.macestudios.macetpa.listener.** {
    public <init>(...);
}

-keepclassmembers class * {
    @org.bukkit.event.EventHandler <methods>;
}

# Keep model records stable for readable diagnostics and future serialization safety.
-keep class net.macestudios.macetpa.model.** { *; }

# External APIs and server APIs are compile/runtime libraries, not plugin code to transform.
-keep class org.bukkit.** { *; }
-keep class io.papermc.** { *; }
-keep class net.kyori.** { *; }
-keep class me.clip.placeholderapi.** { *; }
-keep class net.milkbowl.vault.** { *; }

-dontwarn org.bukkit.**
-dontwarn io.papermc.**
-dontwarn net.kyori.**
-dontwarn me.clip.placeholderapi.**
-dontwarn net.milkbowl.vault.**
-dontwarn com.google.**
-dontwarn org.jetbrains.**
