package com.applitools.eyesutilities;

import com.applitools.eyesutilities.commands.Admin;
import com.applitools.eyesutilities.commands.AnimatedDiffs;
import com.applitools.eyesutilities.commands.Command;
import com.applitools.eyesutilities.commands.CommandBase;
import com.applitools.eyesutilities.commands.CopyBaseline;
import com.applitools.eyesutilities.commands.DeleteBaselines;
import com.applitools.eyesutilities.commands.DownloadDiffs;
import com.applitools.eyesutilities.commands.DownloadImages;
import com.applitools.eyesutilities.commands.MergeBranch;
import com.applitools.eyesutilities.commands.Parse;
import com.applitools.eyesutilities.commands.Playback;
import com.applitools.eyesutilities.commands.Report;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "A 'swiss knife' made to get advanced functionality out of Applitools API")
public class EyesUtilities {
    private static final String cur_ver = EyesUtilities.class.getPackage().getImplementationVersion();
    private Parse parser = new Parse();
    private DownloadDiffs downloadDiffs = new DownloadDiffs();
    private DownloadImages downloadImages = new DownloadImages();
    private Report report = new Report();
    private MergeBranch mergeBranch = new MergeBranch();
    private CopyBaseline copyBaselines = new CopyBaseline();
    private DeleteBaselines deleteBaselines = new DeleteBaselines();
    private AnimatedDiffs animatedDiffs = new AnimatedDiffs();
    private Playback playback = new Playback();
    private Admin admin = new Admin();

    public static void main(String[] args) {
        System.out.printf("Eyes Utilities version %s \n", cur_ver);
        EyesUtilities main = new EyesUtilities();
        JCommander jc = new JCommander();
        jc.setCaseSensitiveOptions(false);
        jc.setProgramName("EyesUtilities");

        try {
            main.run(jc, args);
        } catch (Throwable e) {
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                System.out.println(e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
    }

    private void run(JCommander jc, String[] args) throws Exception {
        if (args.length == 2 && args[0].compareTo("admin") == 0) {
            admin.printHelp(args[1]);
            return;
        }

        jc.addCommand("parse", parser);
        jc.addCommand("diffs", downloadDiffs);
        jc.addCommand("images", downloadImages);
        jc.addCommand("report", report);
        jc.addCommand("merge", mergeBranch);
        jc.addCommand("copyBaselines", copyBaselines);
        jc.addCommand("deleteBaselines", deleteBaselines);
        jc.addCommand("anidiffs", animatedDiffs);
        jc.addCommand("playback", playback);
        jc.addCommand("admin", admin);
        jc.parse(args);

        if (jc.getParsedCommand() == null) {
            jc.usage();
            return;
        }

        Command command = (Command) jc.getCommands().get(jc.getParsedCommand()).getObjects().get(0);

        if (command instanceof CommandBase) {
            ((CommandBase) command).Execute();
        }
        else {
            command.run();
        }
    }
}
