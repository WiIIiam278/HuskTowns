package me.william278.husktowns.object.util;

import java.util.regex.Pattern;

public final class RegexUtil {

    public static final Pattern TOWN_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_\\-]+");
    public static final Pattern TOWN_MESSAGE_PATTERN = Pattern.compile(".*");

}
