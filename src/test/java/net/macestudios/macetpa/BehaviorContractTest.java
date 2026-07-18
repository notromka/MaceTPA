package net.macestudios.macetpa;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class BehaviorContractTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir"));

    @Test
    public void commandAliasesUseRequestedNames() throws IOException {
        String pluginYml = read("src/main/resources/plugin.yml");

        assertTrue(pluginYml.contains("  tpauto:"));
        assertTrue(pluginYml.contains("aliases: [tpadeny]"));
        assertFalse(pluginYml.contains("  tpaauto:"));
    }

    @Test
    public void messagesMatchRequestedCopy() throws IOException {
        String messages = read("src/main/resources/messages.yml");

        assertTrue(messages.contains("player-not-found: \"{prefix} <red>This user is not online.\""));
        assertTrue(messages.contains("cooldown: \"{prefix} <red>You cannot do this right now, on cooldown.\""));
        assertTrue(messages.contains("teleport-success: \"{prefix} <green>You teleported\""));
        assertTrue(messages.contains("teleport-actionbar: \"<gradient:#8c52ff:#5ce0e6>Teleporting in {time}</gradient>\""));
        assertTrue(messages.contains("tpaauto-enabled: \"{prefix} <green>You turned on tpauto.\""));
        assertTrue(messages.contains("tpaauto-disabled: \"{prefix} <red>You turned off tpauto.\""));
        assertFalse(messages.contains("Your teleport request to {target} expired."));
        assertFalse(messages.contains("The teleport request from {player} expired."));
    }

    @Test
    public void teleportFlowDoesNotEmitRemovedFeedback() throws IOException {
        String teleportManager = read("src/main/java/net/macestudios/macetpa/manager/TeleportManager.java");
        String plugin = read("src/main/java/net/macestudios/macetpa/MaceTPA.java");

        assertTrue(teleportManager.contains("messageManager.actionbar(player, \"teleport-actionbar\", placeholders);"));
        assertFalse(teleportManager.contains("\"teleport-countdown\""));
        assertFalse(teleportManager.contains("soundManager.play(player, \"teleport-success\")"));
        assertFalse(teleportManager.contains("soundManager.particle"));
        assertFalse(plugin.contains("request-expired-sender"));
        assertFalse(plugin.contains("request-expired-target"));
    }

    @Test
    public void receiveAndTeleportDelayUseTwoSounds() throws IOException {
        String commandSupport = read("src/main/java/net/macestudios/macetpa/command/CommandSupport.java");
        String teleportManager = read("src/main/java/net/macestudios/macetpa/manager/TeleportManager.java");
        String config = read("src/main/resources/config.yml");

        assertTrue(commandSupport.contains("plugin.soundManager().play(target, \"request-received\");"));
        assertTrue(commandSupport.contains("plugin.soundManager().play(target, \"request-received-2\");"));
        assertTrue(teleportManager.contains("soundManager.play(player, \"teleport-countdown-tick\");"));
        assertTrue(teleportManager.contains("soundManager.play(player, \"teleport-countdown-tick-2\");"));
        assertTrue(config.contains("teleport-delay-seconds: 5"));
        assertTrue(config.contains("request-received-2:"));
    }

    @Test
    public void configHidesAdvancedSectionsAndUsesPerSoundOptions() throws IOException {
        String config = read("src/main/resources/config.yml");
        String messages = read("src/main/resources/messages.yml");

        assertFalse(config.contains("gui-confirm:"));
        assertFalse(config.contains("gui-cancel:"));
        assertFalse(config.contains("particles:"));
        assertFalse(config.contains("\nback:"));
        assertFalse(config.contains("\neconomy:"));
        assertFalse(config.contains("\nworld-settings:"));
        assertTrue(config.contains("external-hooks:"));
        assertTrue(config.contains("request-sent:\n    enabled: true\n    name: ENTITY_ENDER_EYE_LAUNCH\n    volume: 1.0\n    pitch: 1.0"));
        assertTrue(config.contains("request-received-2:\n    enabled: true\n    name: BLOCK_NOTE_BLOCK_BELL\n    volume: 1.0\n    pitch: 1.0"));

        assertFalse(messages.contains("economy-disabled:"));
        assertFalse(messages.contains("not-enough-money:"));
        assertFalse(messages.contains("money-taken:"));
        assertFalse(messages.contains("money-refunded:"));
        assertFalse(messages.contains("help: |"));
    }

    private static String read(String path) throws IOException {
        return Files.readString(ROOT.resolve(path));
    }
}
