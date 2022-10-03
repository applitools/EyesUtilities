# EyesUtilities [ ![Download](https://img.shields.io/github/v/tag/applitools/EyesUtilities?label=Download&style=plastic) ](https://github.com/applitools/EyesUtilities/releases/latest)
Eyes Utilities is a Java based CLI tool to extend Applitools capabilities by communicating directly to Applitools API.
Using EyesUtilities it is possible to generate customized offline reports, perform administration operations,
generate playback and diff animations, download test diffs and original images and perform operations between branches.

### Prerequisites
* Applitools account. If you still don't have your Applitools account,
  you can start your trial by going to the [Applitools website](https://applitools.com).
    * To quickly get on-board go to [the getting started tutorial](https://applitools.com/resources/tutorial).
    * If you already have Applitools account but can't find proper tool, try the [Webtester here](https://github.com/yanirta/WebTester).
* For many listed operations, an additional set of keys will be required.
  To get your keys please reach out to applitools support [via this link](https://help.applitools.com/hc/en-us/requests/new) or at [support@applitools.com](mailto:support@applitools.com).

The general syntax is derived from the fact that the EyesUtilities is built in Java.  
As a result every cli call should start with:
>Java -jar EyesUtilities.jar [command] [command specific parameters...]

## Appendix
* [Generate steps animation](#generate-steps-animation)
* [Generate test playback](#generate-test-playback)
* [Download test diffs](#download-test-diffs)
* [Download test images](#download-test-images)
* [Generating batch(es) Report](#generating-batches-Report)
* [Administration](#administration)
* [Merge branch](#merge-branch)
* [Copy baselines](#copy-baselines)

### Generate steps animation
This command will generate a set of animated gifs for each failing step inside the provided test.
The animation will iterate between three states: (a) The expected from the baseline, (b) The actual and (c) The actual with purple diff marks.
After the execution, the results will be saved, the default location is: `{workdir_root}/Artifacts/{batch_id}/{test_id}/`  
<img src="https://user-images.githubusercontent.com/90543395/144752211-75da8461-21e7-4676-bb75-eaa06e453279.gif" width="550">

Syntax:
> java -jar EyesUtilities.jar anidiffs -k [EntKey] <[optional params]> [ResultUrl]
+ Required parameters:
    + `-k [EntKey]` - API key with read permissions.
    + `[ResultUrl]` - Applitools test result url to be analyzed.
+ Optional parameters:
    + `-i [mSecs]` - Transition interval between the images in milliseconds. default: 1000
    + `-d [pathTmpl]` - Specify destination path template.
    + `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.
      default:
      >{workdir_root}/Artifacts/{batch_id}/{test_id}/file:{step_index}_{step_tag}_{artifact_type}.{file_ext}
        + Available path template parameters: user_root, workdir_root, batch_id, test_id, test_name, batch_name, app_name, os, hostapp, viewport, branch_name, step_index, step_tag, artifact_type, file_ext

### Generate test playback
Generates one unified animated gif with all the actual steps of a test.  
<img src="https://user-images.githubusercontent.com/90543395/144752022-470a67e3-c078-49fc-887a-72b1664b26aa.gif" width="550">

Syntax:
> java -jar EyesUtilities.jar playback -k [EntKey] <[optional params]> [ResultUrl]
+ Required parameters:
    + `-k [EntKey]` - API key with read permissions.
    + `[ResultUrl]` - Applitools test result url to be analyzed.
+ Optional parameters:
    + `-i [mSecs]` - Transition interval between the images in milliseconds. default: 1000
    + `-d [pathTmpl]` - Specify destination path template.
    + `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.

  default:
  >{workdir_root}/Artifacts/{batch_id}/{test_id}/file:{step_index}_{step_tag}_{artifact_type}.{file_ext}
  + Available path template parameters: user_root, workdir_root, batch_id, test_id, test_name, batch_name, app_name, os, hostapp, viewport, branch_name, step_index, step_tag, artifact_type, file_ext
    + `-m` - A Flag, Sets 'on' diff marks if a step is different from it's expected baseline.

### Download test diffs
Downloads the images of the failed steps with diff marks on them.

Syntax:
> java -jar EyesUtilities.jar diffs -k [EntKey] <[optional params]> [ResultUrl]

+ Required parameters:
    + `-k [EntKey]` - API key with read permissions.
    + `[ResultUrl]` - Applitools test result url to be analyzed.
+ Optional parameters:
    + `-i [mSecs]` - Transition interval between the images in milliseconds. default: 1000
    + `-d [pathTmpl]` - Specify destination path template.
    + `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.

  default:
  >{workdir_root}/Artifacts/{batch_id}/{test_id}/file:{step_index}_{step_tag}_{artifact_type}.{file_ext}
  + Available path template parameters: user_root, workdir_root, batch_id, test_id, test_name, batch_name, app_name, os, hostapp, viewport, branch_name, step_index, step_tag, artifact_type, file_ext

### Download test images
Downloads the baseline and the actual images of a test.

Syntax:
> java -jar EyesUtilities.jar images -k [EntKey] <[optional params]> [ResultUrl]

+ Required parameters:
    + `-k [EntKey]` - API key with read permissions.
    + `[ResultUrl]` - Applitools test result url to be analyzed.
+ Optional parameters:
    + `-a` - Flag to download only actuals
    + `-b` - Flag to download only baselines
    + `-d [pathTmpl]` - Specify destination path template.
    + `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.

  default:
  >{workdir_root}/Artifacts/{batch_id}/{test_id}/file:{step_index}_{step_tag}_{artifact_type}.{file_ext}
  + Available path template parameters: user_root, workdir_root, batch_id, test_id, test_name, batch_name, app_name, os, hostapp, viewport, branch_name, step_index, step_tag, artifact_type, file_ext

### Generating batch(es) Report
This command will generate an offline report of provided Applitools' results url(s),
based on a provided template.
The default name and the location of the template is `./report.teml`.

<img src="https://user-images.githubusercontent.com/6667420/37169255-1196032e-2310-11e8-87da-aa6a3174da72.png"  width="550">  

Syntax:
> java -jar EyesUtilities.jar report -k [EntKey] <[optional params]> [ResultUrls]

+ Required parameters:
    + `-k [EntKey]` - API key with read permissions.
    + `[results]` - Any combination of full result urls and/or batch id's. If only batch-id's provided, a server url must appear once in the list.
+ Optional parameters:
    + `-d [FolderPath]` - Set custom report output destination. Default: `'.'`
    + `-t [FilePath]` - Set report template file. Default `./report.templ`
    + `-rt [title]` - Set the title of the report
    + `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.

##### Template syntax:
The engine lies beneath this report generation is based on [Velocity framework]() which widely used in MVC frameworks to implement web-apps.
A template can be constructed with parameters and simple logic using VTL(Velocity Template Language), which's full syntax reference can be found [here](http://velocity.apache.org/engine/1.7/vtl-reference.html).

Here is the partial list of parameters that are exposed for usage in template construction:
+ `$title` - The title that (if) passed in the command to be contained.
+ `$batches` - The main object that stores the batches and overall metrics
+ `$batches.BatchesPassed` - Overall passed batches count
+ `$batches.BatchesFailed` - Overall failed batches count
+ `$batches.BatchesUnresolved` - Overall unresolved batches count
+ `$batches.BatchesRunning` - Overall running batches count
+ `$batches.BatchesPassedRate` - Overall passed batches rate(%)
+ `$batches.BatchesFailedRate` - Overall failed batches rate(%)
+ `$batches.BatchesUnresolvedRate` - Overall unresolved batches rate(%)
+ `$batches.BatchesRunningRate` - Overall running batches rate(%)
+ `$batches.BatchesBatchesMatched` - Overall matched batches count
+ `$batches.BatchesBatchesMismatched` - Overall mismatched batches count
+ `$batches.BatchesNew` - Overall new batches count
+ `$batches.TestsPassed` - Overall batches tests passed count
+ `$batches.TestsFailed` - Overall batches failed passed count
+ `$batches.TestsUnresolved` - Overall batches tests unresolved count
+ `$batches.TestsRunning` - Overall batches tests running count
+ `$batches.TestsPassedRate` - Overall batches tests passed rate(%)
+ `$batches.TestsFailedRate` - Overall batches tests failed rate(%)
+ `$batches.TestsUnresolvedRate` - Overall batches tests unresolved rate(%)
+ `$batches.TestsRunningRate` - Overall batches tests running count
+ `$batches.TestsTotal` - Overall batches tests total
+ `$batches.TestsMismatched` - Overall batches tests mismatched count
+ `$batches.TestsMatched` - Overall batches tests matched count
+ `$batches.TestsNew` - Overall batches tests new count
+ `$batches.TestsAborted` - Overall batches tests aborted count
+ `$batches.StepsTotal` - Overall batches steps total
+ `$batches.StepsMatched` - Overall batches steps matched count
+ `$batches.StepsMismatched` - Overall batches steps mismatched count
+ `$batches.StepsNew` - Overall batches steps new count
+ `$batches.StepsMissing` - Overall batches steps missing count
+ `$batches.StepsMatchedRate` - Overall batches steps matched rate(%)
+ `$batches.StepsMismatchedRate` - Overall batches steps mismatched rate(%)
+ `$batches.StepsNewRate` - Overall batches steps new rate(%)
+ `$batches.StepsMissingRate` - Overall batches steps missing rate(%)
+ `#foreach($batch in $batches)` - Iterate over each batch in $batches
    + `$batch` - The object that contains all the tests and batch level data and metrics.
        + `$batch.Status` - The status of the entire batch
        + `$batch.Name` - The name of the batch
        + `$batch.id` - The id of the batch
        + `$batch.startedAt` - The started date of the batch
        + `$batch.TestsPassed` - Batch overall passed tests count.
        + `$batch.TestsFailed` - Batch overall failed tests count.
        + `$batch.TestsRunning` - Batch overall running tests count.
        + `$batch.TestsNew` - Batch overall new tests count.
        + `$batch.TestsMatched` - Batch overall matched tests count.
        + `$batch.TestsMismatched` - Batch overall mismatched tests count.
        + `$batch.TestsUnresolved` - Batch overall unresolved tests count.
        + `$batch.TestsAborted` - Batch overall aborted tests count.
        + `$batch.TotalTests` - Batch overall total tests count.
        + `$batch.TotalActualSteps` - Batch overall actual total steps.
        + `$batch.TotalBaselineSteps` - Batch overall baseline total steps.
        + `$batch.StepsMatched` - Batch overall matched total steps.
        + `$batch.testsMismatched` - Batch overall mismatched total steps.
        + `$batch.StepsMissing` - Batch overall missing total steps.
        + `$batch.StepsNew` - Batch overall new total steps.
        + `$batch.MatchedRate` - Batch overall matched steps rate(%).
        + `$batch.MismatchedRate` - Batch overall mismatched steps rate(%).
        + `$batch.NewRate` - Batch overall new steps rate(%).
        + `$batch.MissingRate` - Batch overall missing steps rate(%).
        + `#foreach($test in $batch.tests)` - Iterate over each test in $batch.tests
            + `$test.ScenarioName` - The test name
            + `$test.AppName` - Application name
            + `$test.url` - Test result url
            + `$test.BranchName` - Actual branch name (Destination branch)
            + `$test.BaselineBranchName` - Baseline branch name (Source branch)
            + `$test.Id` - Test id
            + `$test.StartedAt` - Started date/time
            + `$test.Duration` - Test duration in seconds
            + `$test.Status` - Current test status
            + `$test.State` - Test state wheter Completed or not
            + `$test.Result` - Test execution result
            + `$test.BaselineEnvId` - Baseline Env-Id
            + `$test.IsAborted` - Is Aborted?
            + `$test.IsDifferent` - Is test has differences?
            + `$test.IsDefaultStatus` - Is the current status is default or overridden?
            + `$test.IsNew` - Is the baseline is new?
            + `$test.IsStarred` - Is Starred in the dashboard?
            + `$test.TotalBaselineSteps` - Total baseline steps in test
            + `$test.TotalActualSteps` - Total actual steps in test
            + `$test.MissingCount` - Total missing steps in test
            + `$test.NewCount` - Total new steps in test
            + `$test.MatchedCount` - Total matching steps in test
            + `$test.MismatchedCount` - Total mismatching steps in test
            + `$test.getPlaybackAnimation` - Download & Generate the test flow as an animated gif and returns it's relative path.
            + `#foreach($step in $test.FailedSteps)` - Iterate over each step in $test.FailedSteps
                + `$step.Diff` - Download & Generate step diff image and returns file's relative path.
                + `$step.AnimatedThumbprints` - Download & Generate step animated thumbprint and returns file's relative path.
                + `$step.AnimatedDiff` - Download & Generate step fully sized animation and returns file's relative path.
                + `$step.ExpectedImage` - Downloads step baseline (expected) image and returns file's relative path.
                + `$step.ActualImage` - Downloads step actual image and returns file's relative path.
            + `#foreach($step in $test.Steps)` - Iterate over each step in $test.Steps
                + `$step.ExpectedImage` - Downloads step baseline (expected) image and returns file's relative path.
                + `$step.ActualImage` - Downloads step actual image and returns file's relative path.

A complete example of a template can be found [here](https://github.com/applitools/EyesUtilities/blob/main/Report/report.templ)  
This example generates html report but the same idea can be applied on any textual format.

### Administration
Perform Admin operations on teams and users in organization.
Before starting, make sure you have Server/Org admin account.

General syntax:
> java -jar EyesUtilities.jar admin <Subcommand> <[Subcommand params]>

##### Sub commands
+ getTeams - List all teams in organization's account
    + Syntax:
  > java -jar EyesUtilities.jar admin getTeams -k [api-key] -or [org-id] <[optional params]>
    + Required parameters:
        +   `-k [api-key]` - API key with read permissions.
        +   `-or [org-id]` - Organization id*
    + Optional parameters:
        +   `-as [url]` - Applitools alternative server, default: eyes.applitools.com
        +   `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.
+ getUsers - List all users in a team
    + Syntax:
  > java -jar EyesUtilities.jar admin getUsers -k [api-key] -or [org-id] -ti [team-id] <[optional params]>
    + Required parameters:
        +   `-k [api-key]` - API key with read permissions.
        +   `-or [org-id]` - Organization id*
        +   `-ti [team-id]` - Team id
    + Optional parameters:
        +   `-as [url]` - Applitools alternative server, default: eyes.applitools.com
        +   `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.
+ addTeam - Add team to organization's account
    + Syntax:
  > java -jar EyesUtilities.jar admin addTeam -k [api-key] -or [org-id] -tn [team-name] <[optional params]>
    + Required parameters:
        +   `-k [api-key]` - API key with read permissions.
        +   `-or [org-id]` - Organization id*
        +   `-tn [team-name]` - The name of the new team
    + Optional parameters:
        +   `-as [url]` - Applitools alternative server, default: eyes.applitools.com
        +   `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.
+ addUser - Add user to a team
    + Syntax:
  > java -jar EyesUtilities.jar admin addUser -k [api-key] -or [org-id] -ti [team id] <[optional params]>
    + Required parameters:
        +   `-k [api-key]` - API key with read permissions.
        +   `-or [org-id]` - Organization id*
        +   `-ti [team-id]` - Team id
    + Optional parameters:
        +   `-as [url]` - Applitools alternative server, default: eyes.applitools.com
        +   `-ne [newUserEmail]` - The email of the new user. Required if the user is new to the account.
        +   `-ni [newUserId]` - The user-id of the new user, default: same as the newUserEmail.
        +   `-nn [newUserName]` - The full name of the new user, default: extracted from the newUserEmail (the part before the '@').
        +   `-ve` - Set permission to viewer, default: false
        +   `-ad` - Set permission to team admin, default: false
        +   `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.
+ remUser - Remove user from a team or from organization
    + Syntax:
  > java -jar EyesUtilities.jar admin remUser -k [api-key] -or [org-id] -ri [remove-user-id] <[optional params]>
    + Required parameters:
        +   `-k [api-key]` - API key with read AND write permissions.
        +   `-or [org-id]` - Organization id*
        +   `-ri [remove-user-id]` - The id of the user to be removed (this is most likely their email address)
    + Optional parameters:
        +   `-as [url]` - Applitools alternative server, default: eyes.applitools.com
        +   `-ti [team-id]` - Team id, if set will only be omitted from the team
        +   `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.

          * Organization id (orgId) field can be found as one of the url parameters in the admin/manage panel.
### Merge branch
Perform branch merging with additional options.

Syntax:
> java -jar EyesUtilities.jar merge -k [apiKey] -s [sourceBranch] <[optional params]>

+ Required parameters:
    + `-k [apiKey]` - API key with merge permissions.
    + `-s [sourceBranch]` - Source branch name
+ Optional parameters:
    + `-as [url]` - Applitools alternative server, default: eyes.applitools.com
    + `-t [targetBranch]` - Target branch for merge
    + `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.
    + `-d` - Delete the source branch after a successful merge. The apiKey must have merge, read and write permissions.
    + `-db` - After successfully merging baselines from source branch to target branch, delete baselines from source branch. 

### Copy baselines
Perform baselines copy with additional options.
Unless filter applied, all baselines will be copied.
If a baseline exists in target branch, it will be overwritten.

Syntax:
> java -jar EyesUtilities.jar copyBaselines -k [apiKey] -t [targetBranch]

+ Required parameters:
    + `-k [apiKey]` - API key with read AND write permissions. 
    + `-t [targetBranch]` - Target branch name
+ Optional parameters:
    + `-as [url]` - Applitools alternative server, default: eyes.applitools.com
    + `-s [sourceBranch]` - Source branch name  default: 'default' branch
    + `-an [appName]` - Filter baselines by application name
    + `-dv` - Disable SSL certificate check and ignore possible errors. Note that using this flag is unsecured and dangerous.

### Delete baselines
Delete baselines by id.
Only the baseline with the specified id will be deleted.

Syntax:
> java -jar EyesUtilities.jar deleteBaselines -k [apiKey] -bld [baselineId]
> 
+ Required parameters:
    + `-k [apiKey]` - API key with read AND write permissions. 
    + `-bld [baselineId]` - Delete the baseline with the corresponding id
+ Optional parameters:
    + `-as [url]` - Applitools alternative server, default: eyes.applitools.com

## Resources
+ [Applitools website](https://applitools.com)
+ [Web-Tester](https://github.com/yanirta/WebTester)
+ [Image-Tester](https://github.com/applitools/ImageTester)
+ [Applitools support portal](http://support.applitools.com/)
