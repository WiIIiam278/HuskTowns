package net.william278.husktowns.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ColorPicker {

    private final int width;
    private final int height;
    private final char swatch;
    private final String command;

    private ColorPicker(int width, int height, char swatch, @NotNull String command) {
        this.width = width;
        this.height = height;
        this.swatch = swatch;
        this.command = command.startsWith("/") ? command.trim() : "/" + command.trim();
    }

    @NotNull
    public Component toComponent() {
        Component colorPicker = Component.empty();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final String color = getColorAt(x, y);
                colorPicker = colorPicker.append(Component.text(swatch)
                        .color(TextColor.fromHexString(color))
                        .hoverEvent(Component.text(color).color(TextColor.fromHexString(color)))
                        .clickEvent(ClickEvent.suggestCommand(command + " " + color)));
            }
            colorPicker = colorPicker.append(Component.newline());
        }
        return colorPicker;
    }

    @NotNull
    private String getColorAt(int x, int y) {
        final Color color = Color.getHSBColor(
                (float) x / width, (float) y / height,
                (y == 0 ? (float) x / width : 1f));
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int width = 90;
        private int height = 6;
        private String command;
        private char swatch = '|';

        @NotNull
        public Builder width(int width) {
            this.width = width;
            return this;
        }

        @NotNull
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        @NotNull
        public Builder swatch(char swatch) {
            this.swatch = swatch;
            return this;
        }

        @NotNull
        public Builder command(String command) {
            this.command = command;
            return this;
        }

        public ColorPicker build() {
            return new ColorPicker(width, height, swatch, command);
        }

    }
}
