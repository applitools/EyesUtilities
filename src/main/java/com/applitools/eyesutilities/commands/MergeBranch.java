package com.applitools.eyesutilities.commands;

import com.applitools.eyesutilities.obj.contexts.BranchesAPIContext;
import com.applitools.eyesutilities.obj.serialized.MergeBranchResponse;
import com.applitools.eyesutilities.utils.BaselinesManager;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Performs branch merge operations")
public class MergeBranch extends BaselineCommand {
    @Parameter(names = {"-s", "-source"}, description = "The source branch name", required = true)
    private String sourceBranch;
    @Parameter(names = {"-t", "-target"}, description = "The target branch name. If not used or passed “default” will copy to the main branch.")
    private String targetBranch = "default";
    @Parameter(names = {"-d", "-deleteSource"}, description = "Delete the source branch after a successful copy")
    private boolean isDelete = false;
    @Parameter(names = {"-db", "-deleteBaselines"}, description = "Delete the baselines associated")
    private boolean isDeleteBaselines = false;
    @Parameter(names = {"-gt", "-gitMergeTimestamp"}, description = "A non-null value instructs Eyes to use timestamp to index the baseline")
    private String gitMergeTimestamp = null;

    @Override
    public void run() throws Exception {
        System.out.printf("Attempting to merge source branch: %s to target branch: %s.%n\n", sourceBranch, targetBranch);
        if (gitMergeTimestamp != null) {
            System.out.printf("\nUsing git merge timestamp %s to index baselines", gitMergeTimestamp);
        }
        BranchesAPIContext context = BranchesAPIContext.Init(getFormattedServerUrl(), apiKey);
        BaselinesManager baselinesManager = new BaselinesManager(context);
        MergeBranchResponse response = baselinesManager.mergeBranches(this);
        if (!response.isMerged())
            System.out.println("\nConflicts have been found!!! Merge aborted.");
        else {
            System.out.println("\nMerge succeeded");
            if (isDelete && baselinesManager.deleteBranch(sourceBranch, isDeleteBaselines))
                System.out.printf("Source branch: %s has been deleted%n", sourceBranch);
        }
    }

    //Per object serialization
    public String getSourceBranch() {
        return sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getGitMergeTimestamp() {
        return gitMergeTimestamp;
    }
}
