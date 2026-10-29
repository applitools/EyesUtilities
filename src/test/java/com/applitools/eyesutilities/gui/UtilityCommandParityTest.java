package com.applitools.eyesutilities.gui;

import com.applitools.eyesutilities.commands.Admin;
import com.applitools.eyesutilities.commands.AnimatedDiffs;
import com.applitools.eyesutilities.commands.CopyBaseline;
import com.applitools.eyesutilities.commands.DeleteBaselines;
import com.applitools.eyesutilities.commands.DownloadDiffs;
import com.applitools.eyesutilities.commands.DownloadImages;
import com.applitools.eyesutilities.commands.MergeBranch;
import com.applitools.eyesutilities.commands.Playback;
import com.applitools.eyesutilities.commands.Report;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Keeps the GUI allowlist in step with the commands' @Parameter declarations: every flag the
 * GUI may send must exist, or renaming a flag silently breaks a form field. Admin subcommands
 * live on Admin's private JCommander, hence the reflection.
 */
public class UtilityCommandParityTest {

    private static final String KEY_FLAG = "k";

    @Test
    public void shouldOnlyAllowFlagsTheRealCommandsDefine() throws Exception {
        Map<UtilityCommand, JCommander> real = realCommands();
        for (UtilityCommand command : UtilityCommand.values()) {
            Set<String> unknown = new HashSet<>(command.valueFlags);
            unknown.addAll(command.booleanFlags);
            unknown.add(KEY_FLAG);
            unknown.removeAll(shortNames(real.get(command)));
            assertEquals(command.id + " allowlists flags the command doesn't define", Collections.emptySet(), unknown);
        }
    }

    private static Map<UtilityCommand, JCommander> realCommands() throws Exception {
        Map<UtilityCommand, JCommander> m = new EnumMap<>(UtilityCommand.class);
        m.put(UtilityCommand.DIFFS, jc(new DownloadDiffs()));
        m.put(UtilityCommand.IMAGES, jc(new DownloadImages()));
        m.put(UtilityCommand.ANIDIFFS, jc(new AnimatedDiffs()));
        m.put(UtilityCommand.PLAYBACK, jc(new Playback()));
        m.put(UtilityCommand.REPORT, jc(new Report()));
        m.put(UtilityCommand.MERGE, jc(new MergeBranch()));
        m.put(UtilityCommand.COPY_BASELINES, jc(new CopyBaseline()));
        m.put(UtilityCommand.DELETE_BASELINES, jc(new DeleteBaselines()));
        Field field = Admin.class.getDeclaredField("jc");
        field.setAccessible(true);
        Map<String, JCommander> admin = ((JCommander) field.get(new Admin())).getCommands();
        m.put(UtilityCommand.ADMIN_GET_TEAMS, admin.get("getTeams"));
        m.put(UtilityCommand.ADMIN_ADD_TEAM, admin.get("addTeam"));
        m.put(UtilityCommand.ADMIN_GET_USERS, admin.get("getUsers"));
        m.put(UtilityCommand.ADMIN_ADD_USER, admin.get("addUser"));
        m.put(UtilityCommand.ADMIN_REMOVE_USER, admin.get("remUser"));
        return m;
    }

    private static JCommander jc(Object command) {
        return JCommander.newBuilder().addObject(command).build();
    }

    private static Set<String> shortNames(JCommander jc) {
        // Descriptions are otherwise only built lazily by parse()/usage().
        jc.createDescriptions();
        Set<String> names = new HashSet<>();
        for (ParameterDescription p : jc.getParameters()) {
            for (String name : p.getParameter().names()) {
                if (name.startsWith("-") && !name.startsWith("--")) names.add(name.substring(1));
            }
        }
        return names;
    }
}
