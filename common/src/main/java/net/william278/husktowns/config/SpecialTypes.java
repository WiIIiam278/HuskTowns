package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
@YamlFile(header = "Internal resource config containing lists of special block and entity types")
public class SpecialTypes {

    @YamlKey("crop_blocks")
    private List<String> cropBlocks;

    @YamlKey("pressure_sensitive_blocks")
    private List<String> pressureSensitiveBlocks;

    @YamlKey("griefing_mobs")
    private List<String> griefingMobs;

    @SuppressWarnings("unused")
    private SpecialTypes() {
    }

    public boolean isCropBlock(@NotNull String block) {
        return cropBlocks.contains(formatKey(block));
    }

    public boolean isPressureSensitiveBlock(@NotNull String block) {
        return pressureSensitiveBlocks.contains(formatKey(block));
    }

    public boolean isGriefingMob(@NotNull String mob) {
        return griefingMobs.contains(formatKey(mob));
    }

    @NotNull
    private static String formatKey(@NotNull String key) {
        return key.trim().toLowerCase().replace("minecraft:", "");
    }

}
