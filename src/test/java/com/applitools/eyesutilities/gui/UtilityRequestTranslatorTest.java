package com.applitools.eyesutilities.gui;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class UtilityRequestTranslatorTest {

    private static final String KEY = "key_123";
    private static final String RESULT_URL = "https://eyes.applitools.com/app/batches/111/222";

    private static UtilityRequest req(Map<String, Object> values, String... urls) {
        UtilityRequest r = new UtilityRequest();
        r.values = values;
        r.urls = Arrays.asList(urls);
        r.key = KEY;
        return r;
    }

    private static Map<String, Object> values(Object... pairs) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], pairs[i + 1]);
        return m;
    }

    private static List<String> args(UtilityCommand command, UtilityRequest r) {
        return UtilityRequestTranslator.toArgs(command, r);
    }

    @Test
    public void shouldStartWithTheCommandName() {
        assertEquals("diffs", args(UtilityCommand.DIFFS, req(values(), RESULT_URL)).get(0));
    }

    @Test
    public void shouldEmitAdminOperationsAsAdminSubcommands() {
        List<String> a = args(UtilityCommand.ADMIN_GET_TEAMS, req(values("or", "org1")));
        assertEquals(Arrays.asList("admin", "getTeams"), a.subList(0, 2));
    }

    @Test
    public void shouldEmitValueFlagFollowedByItsValue() {
        List<String> a = args(UtilityCommand.MERGE, req(values("s", "feature/login")));
        assertEquals("feature/login", a.get(a.indexOf("-s") + 1));
    }

    @Test
    public void shouldTrimValues() {
        List<String> a = args(UtilityCommand.MERGE, req(values("s", "  feature/login  ")));
        assertEquals("feature/login", a.get(a.indexOf("-s") + 1));
    }

    @Test
    public void shouldEmitTrueBooleanFlag() {
        assertTrue(args(UtilityCommand.IMAGES, req(values("gs", true), RESULT_URL)).contains("-gs"));
    }

    @Test
    public void shouldOmitFalseBooleanFlag() {
        assertFalse(args(UtilityCommand.IMAGES, req(values("a", false), RESULT_URL)).contains("-a"));
    }

    @Test
    public void shouldOmitBlankValueFlag() {
        assertFalse(args(UtilityCommand.DIFFS, req(values("d", " "), RESULT_URL)).contains("-d"));
    }

    @Test
    public void shouldPassTheKeyAfterKeyFlag() {
        List<String> a = args(UtilityCommand.DIFFS, req(values(), RESULT_URL));
        assertEquals(KEY, a.get(a.indexOf("-k") + 1));
    }

    @Test
    public void shouldAppendResultUrlsLast() {
        List<String> a = args(UtilityCommand.DIFFS, req(values("dv", true), RESULT_URL));
        assertEquals(RESULT_URL, a.get(a.size() - 1));
    }

    @Test
    public void shouldAcceptDateRangeInsteadOfResultUrls() {
        List<String> a = args(UtilityCommand.REPORT, req(values("dr", "[1-5-2026,1-31-2026]")));
        assertEquals("[1-5-2026,1-31-2026]", a.get(a.indexOf("-dr") + 1));
    }

    @Test
    public void shouldRejectMissingKey() {
        UtilityRequest r = req(values("s", "x"));
        r.key = " ";
        assertThrows(InvalidRequestException.class, () -> args(UtilityCommand.MERGE, r));
    }

    @Test
    public void shouldRejectResultsCommandWithoutUrlsOrBatchFilter() {
        assertThrows(InvalidRequestException.class, () -> args(UtilityCommand.DIFFS, req(values())));
    }

    @Test
    public void shouldRejectFlagNotAllowedForTheCommand() {
        assertThrows(InvalidRequestException.class,
                () -> args(UtilityCommand.DIFFS, req(values("bld", "abc"), RESULT_URL)));
    }

    @Test
    public void shouldRejectCallerSuppliedKeyFlag() {
        assertThrows(InvalidRequestException.class,
                () -> args(UtilityCommand.MERGE, req(values("s", "x", "k", "other-key"))));
    }

    @Test
    public void shouldRejectMissingRequiredFlag() {
        assertThrows(InvalidRequestException.class,
                () -> args(UtilityCommand.COPY_BASELINES, req(values("s", "feature/x"))));
    }

    @Test
    public void shouldRejectValueThatWouldSplitIntoTwoArgsFileLines() {
        assertThrows(InvalidRequestException.class, () -> args(UtilityCommand.MERGE, req(values("s", "x\n-db"))));
    }

    @Test
    public void shouldRejectValueTheArgsFileReaderWouldDropAsComment() {
        assertThrows(InvalidRequestException.class, () -> args(UtilityCommand.MERGE, req(values("s", "#123"))));
    }

    @Test
    public void shouldRejectResultUrlThatIsAFlag() {
        assertThrows(InvalidRequestException.class, () -> args(UtilityCommand.DIFFS, req(values(), "-dv")));
    }

    @Test
    public void shouldRejectResultUrlsForNonResultsCommand() {
        assertThrows(InvalidRequestException.class,
                () -> args(UtilityCommand.MERGE, req(values("s", "x"), RESULT_URL)));
    }

    @Test
    public void shouldRequireTeamIdForRemoveUserSoItNeverPromptsForOrgWideRemoval() {
        assertThrows(InvalidRequestException.class,
                () -> args(UtilityCommand.ADMIN_REMOVE_USER, req(values("or", "org1", "ri", "a@b.com"))));
    }

    @Test
    public void shouldRejectUnknownCommandId() {
        assertThrows(InvalidRequestException.class, () -> UtilityCommand.fromId("rm"));
    }
}
