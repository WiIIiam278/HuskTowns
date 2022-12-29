package net.william278.husktowns.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Utility for displaying an interactive color picker in chat
 */
public class ColorPicker {

    private final int width;
    private final int height;
    private final char swatch;
    private final String command;

    /**
     * Create a new color picker
     *
     * @param width   The width of the color picker
     * @param height  The height of the color picker
     * @param swatch  The character to use for the color swatches
     * @param command The command to run when a color is selected
     * @see #builder()
     */
    private ColorPicker(int width, int height, char swatch, @NotNull String command) {
        this.width = width;
        this.height = height;
        this.swatch = swatch;
        this.command = command.startsWith("/") ? command.trim() : "/" + command.trim();
    }

    /**
     * Get the color picker as an adventure {@link Component}
     *
     * @return the color picker as an adventure {@link Component}
     */
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

    /**
     * Get the color at a given position in the color picker grid
     *
     * @param x The x position
     * @param y The y position
     * @return The RGB color code at the given position
     */
    @NotNull
    private String getColorAt(int x, int y) {
        final Color color = Color.getHSBColor(
                (float) x / width, (float) y / height,
                (y == 0 ? (float) x / width : 1f));
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Get a {@link Builder color picker builder}
     *
     * @return a new color picker builder
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for making color pickers
     */
    public static class Builder {

        private int width = 90;
        private int height = 6;
        private String command;
        private char swatch = '▏';

        /**
         * Set the width (number of columns) of the color picker
         *
         * @param width the width of the color picker
         * @return this builder
         */
        @NotNull
        public Builder width(int width) {
            this.width = width;
            return this;
        }

        /**
         * Set the height (number of rows) of the color picker
         *
         * @param height the height of the color picker
         * @return this builder
         */
        @NotNull
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        /**
         * Set the swatch character to use for each color
         *
         * @param swatch the swatch character
         * @return this builder
         */
        @NotNull
        public Builder swatch(char swatch) {
            this.swatch = swatch;
            return this;
        }

        /**
         * Set the command prefix to execute when a color is clicked
         *
         * @param command the command prefix
         * @return this builder
         */
        @NotNull
        public Builder command(String command) {
            this.command = command;
            return this;
        }

        /**
         * Build a color picker
         *
         * @return a new color picker
         */
        @NotNull
        public ColorPicker build() {
            return new ColorPicker(width, height, swatch, command);
        }

    }
}
