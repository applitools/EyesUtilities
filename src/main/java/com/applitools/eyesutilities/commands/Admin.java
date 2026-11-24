package com.applitools.eyesutilities.commands;

import com.applitools.eyesutilities.obj.AdminApi;
import com.applitools.eyesutilities.obj.serialized.admin.Account;
import com.applitools.eyesutilities.obj.serialized.admin.Subscriber;
import com.applitools.eyesutilities.obj.serialized.admin.User;
import com.applitools.eyesutilities.utils.Validate;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Parameters(commandDescription = "Manage users and teams on the server")
public class Admin extends CommandBase {
    private static final String GETTEAMS = "getTeams";
    private static final String GETUSERS = "getUsers";
    private static final String ADDTEAM = "addTeam";
    private static final String ADDUSER = "addUser";
    private static final String REMUSER = "remUser";
    private static final String SETVIEWER = "setViewer";

    public void printHelp(String subcmd) {
        jc.usage();
    }

    //region sub commands
    @Parameters(commandDescription = "Get user-id by providing username and password")
    private abstract class AdminCommand implements Command {
        @Parameter(names = {"-k", "--apiKey"}, description = "Applitools api-key", required = true)
        protected String apiKey;
        @Parameter(names = {"-or", "--orgId"}, description = "Organization id as it appears in your urls", required = true)
        protected String orgId;

        protected AdminApi adminApi;

        @Override
        public final void run() throws Exception {
            adminApi = new AdminApi(server, orgId, apiKey);
            subRun();
        }

        protected abstract void subRun() throws IOException;
    }

    @Parameters(commandDescription = "List all the teams in organization")
    private class GetTeams extends AdminCommand {
        @Override
        public void subRun() throws IOException {
            Account[] accounts = adminApi.getAccounts();
            for (Account account : accounts) {
                System.out.printf("%s\t|\t%s\n", account.getId(), account.getName());
            }
        }
    }

    @Parameters(commandDescription = "List all the users in a specific team in the organization")
    private class GetUsers extends AdminCommand {
        private static final String TABS = "|%-35s|%-20s|%-35s|%-7s|%-8s|\n";
        @Parameter(names = {"-ti", "--teamId"}, description = "The team id", required = true)
        private String teamId;

        @Override
        public void subRun() throws IOException {
            Account account = adminApi.getAccount(teamId);

            if (account == null)
                throw new RuntimeException("No team was found!");

            adminApi.getUsers();
            System.out.printf(TABS, "             Username", "        Name", "               Email", " Admin ", " Viewer ");
            for (Subscriber sub : account.getMembers().values()) {
                User currUser = adminApi.getUserById(sub.getName());
                if (currUser != null) {
                    System.out.printf(TABS,
                            currUser.getId(),
                            currUser.getFullName(),
                            currUser.getEmail(),
                            sub.getIsAdmin() ? "   x  " : "",
                            sub.getIsViewer() ? "    x  " : "");
                }
            }
        }
    }

    @Parameters(commandDescription = "Add New Team")
    private class AddNewTeam extends AdminCommand {
        @Parameter(names = {"-tn", "--teamName"}, description = "Add new team", required = true)
        private String teamName;

        public void subRun() throws IOException {
            Account account = adminApi.addAccount(new Account(teamName));
            System.out.printf("Team id:  %s\n", account.getId());
        }
    }

    @Parameters(commandDescription = "Add a new user to team")
    private class AddUser extends AdminCommand {
        @Parameter(names = {"-ti", "--teamId"}, description = "Team id", required = true)
        private String teamId;
        @Parameter(names = {"-ne", "--newUserEmail"}, description = "User Email")
        private String newUserEmail;
        @Parameter(names = {"-ni", "--newUserId"}, description = "User id to add")
        private String newUserId;
        @Parameter(names = {"-nn", "--newUserName"}, description = "User's full name", variableArity = true)
        private List<String> newUserName;
        @Parameter(names = {"-ve", "--viewer"}, description = "Set permissions to viewer")
        private boolean isViewer = false;
        @Parameter(names = {"-ad", "--admin"}, description = "Set permissions to team-admin")
        private boolean isAdmin = false;

        @Override
        public void subRun() throws IOException {
            User currUser = null;
            if (newUserId != null)
                currUser = adminApi.getUserById(newUserId);
            else if (newUserEmail != null)
                currUser = adminApi.getUserByEmail(newUserEmail);
            if (currUser == null) {
                if (newUserEmail == null)
                    throw new RuntimeException("Email required!"); //New user require email field
                if (newUserId == null)
                    newUserId = newUserEmail;
                if (newUserName == null) {
                    newUserName = new ArrayList<>();
                    String[] emailparts = newUserEmail.split("@");
                    String name = emailparts[0];
                    name = name.replaceAll("\\.", " ").toUpperCase();
                    newUserName.add(name);
                }

                StringBuilder nameBuilder = new StringBuilder();
                for (String name : newUserName)
                    nameBuilder.append(name).append(" ");
                currUser = new User(newUserId, newUserEmail, nameBuilder.toString());

                adminApi.addUser(currUser); //Adding user to the org
            }

            Account account = adminApi.getAccount(teamId);
            if (account == null)
                throw new RuntimeException("Team was not found!");

            account.add(currUser, isViewer, isAdmin);

            System.out.println("User with ID " + newUserEmail + "successfully created");
        }
    }

    @Parameters(commandDescription = "Set a team member's permission to view-only, or every member's with -all")
    private class SetViewer extends AdminCommand {
        private static final String CONFIRMATION = "YES";

        @Parameter(names = {"-ti", "--teamId"}, description = "Team id. Required for a single user; with -all, limits the change to this team")
        private String teamId;
        @Parameter(names = {"-un", "--username"}, description = "Username of the member, as it appears in the org's users (getUsers 'Username' column)")
        private String username;
        @Parameter(names = {"-all", "--all"}, description = "Set ALL members of the team (or of every team in the org when -ti is omitted) to view-only. Asks for confirmation")
        private boolean all = false;

        @Override
        public void subRun() throws IOException {
            if (all && username != null)
                throw new RuntimeException("Use either -un or -all, not both.");
            if (all)
                setAll();
            else
                setOne();
        }

        private void setOne() throws IOException {
            if (username == null || teamId == null)
                throw new RuntimeException("Both -ti and -un are required (or use -all).");

            if (adminApi.getUserById(username) == null)
                throw new RuntimeException("User '" + username + "' was not found in the organization.");

            Account account = adminApi.getAccount(teamId);
            if (account == null)
                throw new RuntimeException("Team was not found!");

            Subscriber member = account.getMembers() == null ? null : account.getMembers().get(username);
            if (member == null)
                throw new RuntimeException("User '" + username + "' is not a member of team '" + account.getName()
                        + "'. Use addUser to add them.");

            if (member.getIsViewer()) {
                System.out.printf("User %s is already view-only in team %s. Nothing changed.\n", username, account.getName());
                return;
            }

            member.setIsViewer(true);
            account.update();
            System.out.printf("User %s is now view-only in team %s.\n", username, account.getName());
        }

        private void setAll() throws IOException {
            List<Account> teams = new ArrayList<>();
            if (teamId != null) {
                Account account = adminApi.getAccount(teamId);
                if (account == null)
                    throw new RuntimeException("Team was not found!");
                teams.add(account);
            } else {
                teams.addAll(Arrays.asList(adminApi.getAccounts()));
            }

            List<Account> toUpdate = new ArrayList<>();
            int memberCount = 0;
            int adminCount = 0;
            for (Account account : teams) {
                int pending = 0;
                if (account.getMembers() != null) {
                    for (Subscriber member : account.getMembers().values()) {
                        if (member.getIsViewer()) continue;
                        pending++;
                        if (member.getIsAdmin()) adminCount++;
                    }
                }
                if (pending > 0) toUpdate.add(account);
                memberCount += pending;
            }

            String scope = teamId != null ? "team " + teams.get(0).getName() : "ALL " + teams.size() + " teams in org " + orgId;
            if (memberCount == 0) {
                System.out.printf("Every member of %s is already view-only. Nothing changed.\n", scope);
                return;
            }

            System.out.printf("WARNING: this will set %d member(s) across %d team(s) of %s to view-only on %s.\n",
                    memberCount, toUpdate.size(), scope, server);
            if (adminCount > 0)
                System.out.printf("%d of them are team admins. Their admin flag is kept, but they will be marked view-only too.\n", adminCount);
            System.out.printf("Type %s to proceed, anything else to cancel: ", CONFIRMATION);

            String answer = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (answer == null || !answer.trim().equals(CONFIRMATION)) {
                System.out.println("Cancelled. Nothing changed.");
                return;
            }

            int updated = 0;
            for (Account account : toUpdate) {
                int changed = 0;
                for (Subscriber member : account.getMembers().values()) {
                    if (!member.getIsViewer()) {
                        member.setIsViewer(true);
                        changed++;
                    }
                }
                try {
                    account.update();
                    updated += changed;
                    System.out.printf("Team %s: %d member(s) set to view-only.\n", account.getName(), changed);
                } catch (RuntimeException e) {
                    System.out.printf("Team %s: FAILED (%s). Its members were not changed.\n", account.getName(), e.getMessage());
                }
            }
            System.out.printf("Done. %d of %d member(s) set to view-only.\n", updated, memberCount);
        }
    }

    @Parameters(commandDescription = "Remove user")
    private class RemoveUser extends AdminCommand {

        @Parameter(names = {"-ri", "--removeUserId"}, description = "User id to remove (this is most likely the user's email address)", required = true)
        private String removeUserId;

        @Parameter(names = {"-ti", "--teamId"}, description = "Team id to remove the user from")
        private String removeTeamId;

        @Override
        public void subRun() throws IOException {
            if (removeTeamId != null) {
                Account account = adminApi.getAccount(removeTeamId);
                if (account == null)
                    throw new RuntimeException("Team is not present");
                account.remove(removeUserId);
                System.out.print("User with id " + removeUserId + " successfully removed");
            } else {
                System.out.println("Removing the user with id " + removeUserId + ".");
                System.out.println("You are about to remove the user entirely, are you sure you want to proceed? (Y\\N)\n");
                BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
                String answer = buffer.readLine();
                if (answer.toUpperCase().charAt(0) == 'Y') {
                    adminApi.removeUser(removeUserId);
                    System.out.print("User with id " + removeUserId + " successfully removed");
                } else
                    System.out.print("User not removed.");
            }
        }
    }

    //endregion
    private JCommander jc = new JCommander();

    @Parameter(names = {GETTEAMS}, description = "Get teams info from the server", variableArity = true)
    private List<String> getTeams = null;

    @Parameter(names = {GETUSERS}, description = "Get team's users from the server", variableArity = true)
    private List<String> getUsers = null;

    @Parameter(names = {ADDTEAM}, description = "Add new team to the server", variableArity = true)
    private List<String> addTeam = null;

    @Parameter(names = {ADDUSER}, description = "Add new user to a team", variableArity = true)
    private List<String> addUser = null;

    @Parameter(names = {REMUSER}, description = "Remove user from team or entirely", variableArity = true)
    private List<String> remUser = null;

    @Parameter(names = {SETVIEWER}, description = "Set a team member (or all members with -all) to view-only", variableArity = true)
    private List<String> setViewer = null;

    public Admin() {
        jc.setProgramName("Admin");
        jc.addCommand(GETTEAMS, new GetTeams());
        jc.addCommand(ADDTEAM, new AddNewTeam());
        jc.addCommand(GETUSERS, new GetUsers());
        jc.addCommand(ADDUSER, new AddUser());
        jc.addCommand(REMUSER, new RemoveUser());
        jc.addCommand(SETVIEWER, new SetViewer());
    }

    public void run() throws Exception {
        if (Validate.isAllNull(getTeams, addTeam, getUsers, addUser, remUser, setViewer)) {
            throw new RuntimeException("No parameters were provided");
        } else if (!Validate.isExactlyOneNotNull(getTeams, addTeam, getUsers, addUser, remUser, setViewer)) {
            throw new RuntimeException(
                    "Too many commands. Please specify exactly one command from: " +
                    "[getTeams, getUsers, addTeam, addUser, remUser, setViewer]"
            );
        } else {
            List<String> subargs = null;
            if (getTeams != null) {
                subargs = getTeams;
                subargs.add(0, GETTEAMS);
            } else if (addTeam != null) {
                subargs = addTeam;
                subargs.add(0, ADDTEAM);
            } else if (getUsers != null) {
                subargs = getUsers;
                subargs.add(0, GETUSERS);
            } else if (addUser != null) {
                subargs = addUser;
                subargs.add(0, ADDUSER);
            } else if (remUser != null) {
                subargs = remUser;
                subargs.add(0, REMUSER);
            } else if (setViewer != null) {
                subargs = setViewer;
                subargs.add(0, SETVIEWER);
            }

            String[] sargs = subargs.toArray(new String[subargs.size()]);
            try {
                jc.parse(sargs);
                Command command = (Command) jc.getCommands().get(jc.getParsedCommand()).getObjects().get(0);
                command.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                jc.usage();
            }
        }
    }
}
