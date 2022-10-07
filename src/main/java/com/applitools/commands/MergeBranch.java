package com.applitools.commands;

import com.applitools.obj.contexts.BranchesAPIContext;
import com.applitools.obj.serialized.MergeBranchResponse;
import com.applitools.utils.BaselinesManager;
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

    @Override
    public void run() throws Exception {
        System.out.printf("Attempting to merge source branch: %s to target branch: %s.%n", sourceBranch, targetBranch);
        BranchesAPIContext context = BranchesAPIContext.Init(getFormattedServerUrl(), apiKey);
        System.out.println("Before Baseline Manager");
        BaselinesManager baselinesManager = new BaselinesManager(context);
        System.out.println("Before merge Branches");
        MergeBranchResponse response = baselinesManager.mergeBranches(this);
        System.out.println("After merge Branches");
        if (!response.isMerged())
            System.out.println("\nConflicts have been found!!! Merge aborted" +
                    "\nPlease resolve conflicts through yanirta test-manager and try again");
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
}
