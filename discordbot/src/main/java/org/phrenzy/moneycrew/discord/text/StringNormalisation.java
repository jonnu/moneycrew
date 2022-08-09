package org.phrenzy.moneycrew.discord.text;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.Optional;

@UtilityClass
public class StringNormalisation {

    private static final String META_PREFIX_PATTERN = "^(jmc_)";
    private static final String NORMALISATION_PATTERN = "[^\\p{L}\\s\\p{Nd}]+";

    public static String normalise(final String input) {
        return Optional.ofNullable(input)
                .map(StringNormalisation::normaliseMetaPrefixes)
                .map(StringNormalisation::normaliseSpaces)
                .map(StringNormalisation::normaliseCodePoints)
                .map(string -> string.toLowerCase(Locale.getDefault()))
                .orElse("");
    }

    private static String normaliseMetaPrefixes(final String input) {
        return input.replaceFirst(META_PREFIX_PATTERN, "");
    }

    private static String normaliseCodePoints(final String input) {
        return input.replaceAll(NORMALISATION_PATTERN, "");
    }

    private static String normaliseSpaces(final String input) {
        return input.replace(' ', '_');
    }
}
