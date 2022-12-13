package net.william278.husktowns.config;

import net.william278.annotaml.YamlFile;

@YamlFile(header = """
        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃       HuskTowns Config       ┃
        ┃    Developed by William278   ┃
        ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
        ┣╸ Information: https://william278.net/project/husktowns
        ┗╸ Documentation: https://william278.net/docs/husktowns""")
public class Settings {

    public String language;

    @SuppressWarnings("unused")
    private Settings() {
    }

}
