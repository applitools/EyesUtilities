package com.applitools.eyesutilities.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Allowlist of the commands the GUI can launch, and the flags each accepts. Flags are the CLI
 * short names without the dash; anything not listed is rejected, so the browser can never
 * smuggle arbitrary arguments into the child process. UtilityCommandParityTest keeps this in
 * step with the commands' @Parameter declarations.
 */
enum UtilityCommand {
    DIFFS("diffs", KeyKind.VIEW_KEY, true,
            results(), flags("dv"), flags()),
    IMAGES("images", KeyKind.VIEW_KEY, true,
            results("st"), flags("dv", "a", "b", "gs"), flags()),
    ANIDIFFS("anidiffs", KeyKind.VIEW_KEY, true,
            results("i"), flags("dv"), flags()),
    PLAYBACK("playback", KeyKind.VIEW_KEY, true,
            results("i"), flags("dv", "m"), flags()),
    REPORT("report", KeyKind.VIEW_KEY, true,
            results("t", "rt"), flags("dv"), flags()),
    MERGE("merge", KeyKind.API_KEY, false,
            flags("s", "t", "gt", "as"), flags("d", "db", "dv"), flags("s")),
    COPY_BASELINES("copyBaselines", KeyKind.API_KEY, false,
            flags("s", "t", "an", "li", "as"), flags("dv"), flags("t")),
    DELETE_BASELINES("deleteBaselines", KeyKind.API_KEY, false,
            flags("bld", "as"), flags("dv"), flags("bld")),
    ADMIN_GET_TEAMS("admin.getTeams", KeyKind.API_KEY, false,
            flags("or"), flags(), flags("or")),
    ADMIN_ADD_TEAM("admin.addTeam", KeyKind.API_KEY, false,
            flags("or", "tn"), flags(), flags("or", "tn")),
    ADMIN_GET_USERS("admin.getUsers", KeyKind.API_KEY, false,
            flags("or", "ti"), flags(), flags("or", "ti")),
    ADMIN_ADD_USER("admin.addUser", KeyKind.API_KEY, false,
            flags("or", "ti", "ne", "ni", "nn"), flags("ad", "ve"), flags("or", "ti")),
    // Team id is required here although the CLI makes it optional: without it, remUser
    // removes the user from the whole organization after an interactive Y/N prompt.
    ADMIN_REMOVE_USER("admin.remUser", KeyKind.API_KEY, false,
            flags("or", "ri", "ti"), flags(), flags("or", "ri", "ti"));

    enum KeyKind { VIEW_KEY, API_KEY }

    /** Flags that let a results command find batches without explicit result URLs. */
    static final Set<String> URL_ALTERNATIVE_FLAGS = flags("dr", "q");

    private static final String ADMIN_PREFIX = "admin.";

    final String id;
    final KeyKind keyKind;
    final boolean acceptsResultUrls;
    final Set<String> valueFlags;
    final Set<String> booleanFlags;
    final Set<String> requiredFlags;

    UtilityCommand(String id, KeyKind keyKind, boolean acceptsResultUrls,
                   Set<String> valueFlags, Set<String> booleanFlags, Set<String> requiredFlags) {
        this.id = id;
        this.keyKind = keyKind;
        this.acceptsResultUrls = acceptsResultUrls;
        this.valueFlags = valueFlags;
        this.booleanFlags = booleanFlags;
        this.requiredFlags = requiredFlags;
    }

    static UtilityCommand fromId(String id) {
        for (UtilityCommand c : values()) {
            if (c.id.equals(id)) return c;
        }
        throw new InvalidRequestException("Unknown EyesUtilities command: " + id);
    }

    /** Leading argv tokens: admin operations are subcommands of `admin`. */
    List<String> commandTokens() {
        if (id.startsWith(ADMIN_PREFIX)) return Arrays.asList("admin", id.substring(ADMIN_PREFIX.length()));
        return Collections.singletonList(id);
    }

    private static Set<String> results(String... extra) {
        Set<String> s = new LinkedHashSet<>(Arrays.asList("dr", "d", "l", "q", "as", "ui", "un"));
        s.addAll(Arrays.asList(extra));
        return Collections.unmodifiableSet(s);
    }

    private static Set<String> flags(String... names) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(names)));
    }
}
