package com.applitools.eyesutilities.commands;

import com.beust.jcommander.converters.CommaParameterSplitter;
import com.applitools.eyesutilities.obj.contexts.BranchesAPIContext;
import com.applitools.eyesutilities.utils.BaselinesManager;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Performs baselines delete operation")
public class DeleteBaselines extends BaselineCommand {
    @Parameter(
            names = {"-bld", "-deleteBaselines"},
            description = "Delete all baselines with the provided ids.",
            splitter = CommaParameterSplitter.class
    )
    private List<String> baselinesToDelete = new ArrayList<>();

    @Override
    public void run() throws Exception {
        System.out.println(baselinesToDelete);
        BranchesAPIContext context = BranchesAPIContext.Init(getFormattedServerUrl(), apiKey);
        BaselinesManager baselinesManager = new BaselinesManager(context);

        if (baselinesManager.deleteBaselines(baselinesToDelete)) {
            System.out.println("Baselines successfully deleted");
        } else {
            System.out.println("Baseline delete aborted");
        }
    }
}
