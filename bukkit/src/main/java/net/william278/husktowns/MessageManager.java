package net.william278.husktowns;

import de.themoep.minedown.MineDown;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.md_5.bungee.api.ChatMessageType;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MessageManager {

    // HashMap mapping every message string to a message
    private static final Map<String, String> messages = new HashMap<>();

    // HashSet of players in verbatim message mode
    private static final HashMap<UUID, ChatMessageType> verbatimRecipients = new HashMap<>();

    public static boolean isPlayerReceivingVerbatimMessages(Player player) {
        return verbatimRecipients.containsKey(player.getUniqueId());
    }

    public static void addVerbatimRecipient(Player player, ChatMessageType type) {
        verbatimRecipients.put(player.getUniqueId(), type);
    }

    public static void removeVerbatimRecipient(Player player) {
        verbatimRecipients.remove(player.getUniqueId());
    }

    private static final HuskTowns plugin = HuskTowns.getInstance();

    // (Re-)Load the messages file with the correct language
    public static void loadMessages(String language) throws IOException {
        final YamlDocument config = YamlDocument.create(
                new File(plugin.getDataFolder(), "messages-" + language + ".yml"),
                Objects.requireNonNull(plugin.getResource("locales/" + language + ".yml")));

        // Load messages
        messages.clear();
        for (String message : config.getRoutesAsStrings(false)) {
            messages.put(message, StringEscapeUtils.unescapeJava(config.getString(message)));
        }
    }

    public static void sendVerboseMessage(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (verbatimRecipients.containsKey(p.getUniqueId())) {
                p.spigot().sendMessage(verbatimRecipients.get(p.getUniqueId()), new MineDown(message).toComponent());
            }
        }
    }

    // Send a message with multiple placeholders
    public static void sendMessage(CommandSender p, String messageID, String... placeholderReplacements) {
        dispatchMessage(p, messageID, placeholderReplacements);
    }

    // Send a message with multiple placeholders
    public static void sendMessage(Player p, String messageID, String... placeholderReplacements) {
        dispatchMessage(p, ChatMessageType.CHAT, messageID, placeholderReplacements);
    }

    // Send a message with multiple placeholders
    public static void sendActionBar(Player p, String messageID, String... placeholderReplacements) {
        dispatchMessage(p, ChatMessageType.ACTION_BAR, messageID, placeholderReplacements);
    }

    private static String getPlaceholderFormattedMessage(String messageID, String... placeholderReplacements) {
        String message = getRawMessage(messageID);

        // Return empty if there is nothing there
        if (StringUtils.isEmpty(message)) {
            return "";
        }

        int replacementIndexer = 1;

        // Replace placeholders
        for (String replacement : placeholderReplacements) {
            String replacementString = "%" + replacementIndexer + "%";
            message = message.replace(replacementString, replacement);
            replacementIndexer = replacementIndexer + 1;
        }
        return message;
    }

    // Send a message to a player / console receiver
    private static void dispatchMessage(Player p, ChatMessageType chatMessageType, String messageID, String... placeholderReplacements) {
        // Convert to baseComponents[] via MineDown formatting and send
        p.spigot().sendMessage(chatMessageType, new MineDown(getPlaceholderFormattedMessage(messageID, placeholderReplacements)).replace().toComponent());
    }

    private static void dispatchMessage(CommandSender p, String messageID, String... placeholderReplacements) {
        // Convert to baseComponents[] via MineDown formatting and send
        p.spigot().sendMessage(new MineDown(getPlaceholderFormattedMessage(messageID, placeholderReplacements)).replace().toComponent());
    }

    // Send a message with no placeholder parameters
    public static void sendMessage(Player p, String messageID) {
        String message = getRawMessage(messageID);

        // Don't send empty messages
        if (StringUtils.isEmpty(message)) {
            return;
        }

        // Convert to baseComponents[] via MineDown formatting and send
        p.spigot().sendMessage(new MineDown(message).replace().toComponent());
    }

    public static String getRawMessage(String messageId) {
        return messages.getOrDefault(messageId, messageId);
    }

    public static String getRawMessage(String messageId, String... placeholderReplacements) {
        String message = getRawMessage(messageId);
        int replacementIndexer = 1;

        // Replace placeholders
        for (String replacement : placeholderReplacements) {
            String replacementString = "%" + replacementIndexer + "%";
            message = message.replace(replacementString, replacement);
            replacementIndexer = replacementIndexer + 1;
        }
        return message;
    }

}
