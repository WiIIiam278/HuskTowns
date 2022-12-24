package net.william278.husktowns.town;

import com.google.gson.annotations.Expose;
import de.themoep.minedown.adventure.MineDown;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.audit.Log;
import net.william278.husktowns.claim.Position;
import net.william278.husktowns.config.Locales;
import net.william278.husktowns.user.CommandUser;
import net.william278.husktowns.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Town {

    private int id;

    @Expose
    private String name;

    @Nullable
    @Expose
    private String bio;

    @Nullable
    @Expose
    private String greeting;

    @Nullable
    @Expose
    private String farewell;

    @Expose
    private Map<UUID, Integer> members;

    @Expose
    private RuleSet rules;

    @Expose
    private long claims;

    @Expose
    private long level;

    @Expose
    private BigDecimal money;

    @Nullable
    @Expose
    private Spawn spawn;

    @Expose
    private Log log;

    @Expose
    private Color color;

    private Town(int id, @NotNull String name, @Nullable String bio, @Nullable String greeting,
                 @Nullable String farewell, @NotNull Map<UUID, Integer> members, @NotNull RuleSet rules,
                 long claims, @NotNull BigDecimal money, long level, @Nullable Spawn spawn,
                 @NotNull Log log, @NotNull Color color) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.greeting = greeting;
        this.farewell = farewell;
        this.members = members;
        this.rules = rules;
        this.claims = claims;
        this.money = money;
        this.level = level;
        this.spawn = spawn;
        this.log = log;
        this.color = color;
    }

    @SuppressWarnings("unused")
    private Town() {
    }

    public static Town of(int id, @NotNull String name, @Nullable String bio, @Nullable String greeting,
                          @Nullable String farewell, @NotNull Map<UUID, Integer> members, @NotNull RuleSet rules,
                          long claims, @NotNull BigDecimal money, long level, @Nullable Spawn spawn,
                          @NotNull Log log, @NotNull Color color) {
        return new Town(id, name, bio, greeting, farewell, members, rules, claims, money, level, spawn, log, color);
    }

    @NotNull
    public static Color getRandomColor(String nameSeed) {
        final Random random = new Random(nameSeed.hashCode());
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public int getId() {
        return id;
    }

    public void updateId(int id) {
        this.id = id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<String> getBio() {
        return Optional.ofNullable(bio);
    }

    public void setBio(@NotNull String bio) {
        this.bio = bio;
    }

    public Optional<String> getGreeting() {
        return Optional.ofNullable(greeting);
    }

    public void setGreeting(@NotNull String greeting) {
        this.greeting = greeting;
    }

    public Optional<String> getFarewell() {
        return Optional.ofNullable(farewell);
    }

    public void setFarewell(@NotNull String farewell) {
        this.farewell = farewell;
    }

    @NotNull
    public Map<UUID, Integer> getMembers() {
        return members;
    }

    @NotNull
    public UUID getMayor() {
        return members.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("Town has no mayor"));
    }

    public void addMember(@NotNull UUID uuid, @NotNull Role role) {
        this.members.put(uuid, role.getWeight());
    }

    public void removeMember(@NotNull UUID uuid) {
        this.members.remove(uuid);
    }

    @NotNull
    public RuleSet getRules() {
        return rules;
    }

    public void setRules(@NotNull RuleSet ruleSet) {
        this.rules = ruleSet;
    }

    public long getClaims() {
        return claims;
    }

    public void setClaims(long claims) {
        this.claims = claims;
    }

    @NotNull
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(@NotNull BigDecimal money) {
        this.money = money;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    public Optional<Spawn> getSpawn() {
        return Optional.ofNullable(spawn);
    }

    public void setSpawn(@NotNull Spawn spawn) {
        this.spawn = spawn;
    }

    public void clearSpawn() {
        this.spawn = null;
    }

    @NotNull
    public Log getLog() {
        return log;
    }

    @NotNull
    public LocalDateTime getFoundedTime() {
        return log.getFoundedTime();
    }

    @NotNull
    public Color getColor() {
        return color;
    }

    @NotNull
    public String getColorRgb() {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @NotNull
    public MineDown getOverview(@NotNull CommandUser user, @NotNull HuskTowns plugin) {
        final StringJoiner joiner = new StringJoiner("\n");
        final boolean isTownMember = user instanceof User player && getMembers().containsKey(player.getUuid());

        plugin.getLocales().getRawLocale("town_details_title", Locales.escapeText(getName()))
                .ifPresent(joiner::add);

        plugin.getLocales().getRawLocale("town_details_meta",
                        Locales.escapeText(getFoundedTime().format(DateTimeFormatter.ofPattern("MMM dd yyyy, HH:mm"))),
                        Integer.toString(getId()),
                        plugin.getDatabase().getUser(getMayor())
                                .map(User::getUsername)
                                .orElse("Unknown"))
                .ifPresent(joiner::add);

        getBio().map(Locales::escapeText).flatMap(bio ->
                        plugin.getLocales().getRawLocale("town_details_bio", bio,
                                plugin.getLocales().wrapText(bio, 40)))
                .ifPresent(joiner::add);

        plugin.getLocales().getRawLocale("town_details_stats",
                        Long.toString(getLevel()),
                        getMoney().toString(),
                        Long.toString(getMembers().size()))
                .ifPresent(joiner::add);

        getSpawn().ifPresent(spawn -> {
            if (spawn.isPublic() || isTownMember) {
                final Position position = spawn.getPosition();
                plugin.getLocales().getRawLocale("town_details_spawn",
                        Integer.toString((int) position.getX()),
                        Integer.toString((int) position.getY()),
                        Integer.toString((int) position.getZ()),
                        (plugin.getSettings().crossServer ? spawn.getServer() : "") + position.getWorld().getName(),
                        Integer.toString((int) position.getPitch()),
                        Integer.toString((int) position.getYaw()));
            }
        });

        if (isTownMember) {
            //todo buttons
        }

        return new MineDown(joiner.toString());
    }

}
