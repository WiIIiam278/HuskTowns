package net.william278.husktowns.advancement;

import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.OnlineUser;
import net.william278.husktowns.user.Preferences;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public interface AdvancementTracker {

    default void loadAdvancements() {
        // Create advancements file
        final File advancementsFile = new File(getPlugin().getDataFolder(), "advancements.json");
        if (!advancementsFile.exists()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(advancementsFile), StandardCharsets.UTF_8)) {
                getPlugin().getGsonBuilder().create().toJson(Advancement.DEFAULT_ADVANCEMENTS, writer);
            } catch (Exception e) {
                getPlugin().log(Level.SEVERE, "Failed to write default advancements: " + e.getMessage(), e);
            }
        }

        // Read advancements from file
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(advancementsFile), StandardCharsets.UTF_8)) {
            setAdvancements(getPlugin().getGson().fromJson(reader, Advancement.class));
        } catch (Exception e) {
            getPlugin().log(Level.SEVERE, "Failed to read advancements: " + e.getMessage(), e);
        }
    }

    default void checkAdvancements(@NotNull Town town, @NotNull OnlineUser user) {
        if (getAdvancements().isEmpty() && !getPlugin().getSettings().doAdvancements()) {
            return;
        }

        final Preferences preferences = getPlugin().getUserPreferences(user.getUuid())
                .orElseThrow(() -> new IllegalStateException("User preferences not found for " + user.getUsername()));
        final Set<String> currentAdvancements = new HashSet<>(preferences.getCompletedAdvancements());
        traverseAdvancements(getAdvancements().get(), town, user, preferences);

        if (!currentAdvancements.equals(preferences.getCompletedAdvancements())) {
            getPlugin().getDatabase().updateUser(user, preferences);
        }
    }

    private void traverseAdvancements(@NotNull Advancement advancement, @NotNull Town town, @NotNull OnlineUser user,
                                      @NotNull Preferences preferences) {
        if (!advancement.areConditionsMet(town, user)) {
            return;
        }

        // Award advancements to all town members
        getPlugin().getOnlineUsers().stream()
                .filter(online -> town.getMembers().containsKey(online.getUuid()))
                .forEach(online -> {
                    if (!preferences.isCompletedAdvancement(advancement.getKey())) {
                        advancement.getRewards().forEach(reward -> reward.give(online, getPlugin()));
                        preferences.addCompletedAdvancement(advancement.getKey());
                    }

                    this.awardAdvancement(advancement, user);
                });

        advancement.getChildren().forEach(child -> traverseAdvancements(child, town, user, preferences));
    }

    void awardAdvancement(@NotNull Advancement advancement, @NotNull OnlineUser user);

    Optional<Advancement> getAdvancements();

    void setAdvancements(@NotNull Advancement advancements);

    @NotNull
    HuskTowns getPlugin();
}
