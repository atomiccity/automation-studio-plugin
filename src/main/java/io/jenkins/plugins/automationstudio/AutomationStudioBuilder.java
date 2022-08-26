package io.jenkins.plugins.automationstudio;

import city.atomic.automationstudio.Cpu;
import city.atomic.automationstudio.Hardware;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.util.Arrays;

import static io.jenkins.plugins.automationstudio.ToolUtils.getFullToolPath;

public class AutomationStudioBuilder extends Builder implements SimpleBuildStep {
    // GUI Fields
    private String automationStudioName;
    private final String projectFile;
    private final String configurationName;
    private String buildMode;
    private boolean simulation;
    private boolean buildRUCPackage;
    private String tempDir;
    private String binDir;
    private String version;
    private String buildOptions;
    private String ansicBuildOptions;
    private boolean unstableIfWarnings;
    private boolean continueOnErrors;

    @DataBoundConstructor
    public AutomationStudioBuilder(String projectFile, String configurationName) {
        this.automationStudioName = null;
        this.projectFile = projectFile;
        this.configurationName = configurationName;
        this.buildMode = "Build";
        this.simulation = false;
        this.buildRUCPackage = false;
        this.tempDir = null;
        this.binDir = null;
        this.version = null;
        this.buildOptions = null;
        this.ansicBuildOptions = null;
        this.unstableIfWarnings = false;
        this.continueOnErrors = false;
    }

    public AutomationStudioBuilder(String automationStudioName, String projectFile, String configurationName,
                                   String buildMode, boolean simulation, boolean buildRUCPackage, String tempDir,
                                   String binDir, String version, String buildOptions, String ansicBuildOptions,
                                   boolean unstableIfWarnings, boolean continueOnErrors) {
        this.automationStudioName = automationStudioName;
        this.projectFile = projectFile;
        this.configurationName = configurationName;
        this.buildMode = buildMode;
        this.simulation = simulation;
        this.buildRUCPackage = buildRUCPackage;
        this.tempDir = tempDir;
        this.binDir = binDir;
        this.version = version;
        this.buildOptions = buildOptions;
        this.ansicBuildOptions = ansicBuildOptions;
        this.unstableIfWarnings = unstableIfWarnings;
        this.continueOnErrors = continueOnErrors;
    }

    public String getProjectFile() {
        return projectFile;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public String getAutomationStudioName() {
        return automationStudioName;
    }

    @DataBoundSetter
    public void setAutomationStudioName(String automationStudioName) {
        this.automationStudioName = automationStudioName;
    }

    public String getBuildMode() {
        return buildMode;
    }

    @DataBoundSetter
    public void setBuildMode(String buildMode) {
        this.buildMode = buildMode;
    }

    public boolean isSimulation() {
        return simulation;
    }

    @DataBoundSetter
    public void setSimulation(boolean simulation) {
        this.simulation = simulation;
    }

    public boolean isBuildRUCPackage() {
        return buildRUCPackage;
    }

    @DataBoundSetter
    public void setBuildRUCPackage(boolean buildRUCPackage) {
        this.buildRUCPackage = buildRUCPackage;
    }

    public String getTempDir() {
        return tempDir;
    }

    @DataBoundSetter
    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getBinDir() {
        return binDir;
    }

    @DataBoundSetter
    public void setBinDir(String binDir) {
        this.binDir = binDir;
    }

    public String getVersion() {
        return version;
    }

    @DataBoundSetter
    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildOptions() {
        return buildOptions;
    }

    @DataBoundSetter
    public void setBuildOptions(String buildOptions) {
        this.buildOptions = buildOptions;
    }

    public String getAnsicBuildOptions() {
        return ansicBuildOptions;
    }

    @DataBoundSetter
    public void setAnsicBuildOptions(String ansicBuildOptions) {
        this.ansicBuildOptions = ansicBuildOptions;
    }

    @DataBoundSetter
    public void setUnstableIfWarnings(boolean unstableIfWarnings) {
        this.unstableIfWarnings = unstableIfWarnings;
    }

    public boolean isUnstableIfWarnings() {
        return unstableIfWarnings;
    }

    public boolean isContinueOnErrors() {
        return continueOnErrors;
    }

    @DataBoundSetter
    public void setContinueOnErrors(boolean continueOnErrors) {
        this.continueOnErrors = continueOnErrors;
    }

    public AutomationStudioInstallation getInstallation() {
        DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();
        for (AutomationStudioInstallation i : descriptor.getInstallations()) {
            if (automationStudioName != null && i.getName().equals(automationStudioName)) {
                return i;
            }
        }
        return null;
    }

    @Override
    public void perform(@NotNull Run<?, ?> run, @NotNull FilePath workspace, @NotNull EnvVars env,
                        @NotNull Launcher launcher, @NotNull TaskListener listener)
            throws InterruptedException, IOException {
        Result failureResult = continueOnErrors ? Result.NOT_BUILT : Result.FAILURE;
        ArgumentListBuilder args = new ArgumentListBuilder();
        String exeName = "BR.AS.Build.exe";
        AutomationStudioInstallation installation = getInstallation();

        if (launcher.isUnix()) {
            listener.fatalError("This builder can't be used on Unix systems");
            run.setResult(failureResult);
            return;
        }

        if (installation == null) {
            args.add(exeName);
        } else {
            String pathToTool = getFullToolPath(launcher, installation.getHome(), exeName);
            FilePath exe = new FilePath(launcher.getChannel(), pathToTool);

            try {
                if (!exe.exists()) {
                    listener.fatalError(pathToTool + " doesn't exist");
                    run.setResult(failureResult);
                    return;
                }
            } catch (IOException e) {
                listener.fatalError("Failed checking for existence of " + pathToTool);
                run.setResult(failureResult);
                return;
            }

            listener.getLogger().println("Path to BR.AS.Build.exe: " + pathToTool);
            args.add(pathToTool);
        }

        String normalizedProject = null;
        if (projectFile != null && projectFile.trim().length() != 0) {
            normalizedProject = projectFile.replaceAll("[\t\r\n]+", " ");
            normalizedProject = Util.replaceMacro(normalizedProject, env);
            if (!normalizedProject.isEmpty()) {
                // Project file needs to be absolute
                FilePath projectPath = workspace.child(normalizedProject);
                args.add(projectPath);
            }
        }

        String normalizedConfig = null;
        if (configurationName != null && configurationName.trim().length() != 0) {
            normalizedConfig = configurationName.replaceAll("[\t\r\n]+", " ");
            normalizedConfig = Util.replaceMacro(normalizedConfig, env);
            if (!normalizedConfig.isEmpty()) {
                args.add("-c");
                args.add(normalizedConfig);
            }
        }

        // Add binDir
        String normalizedBinDir = null;
        if (binDir != null && binDir.trim().length() != 0) {
            normalizedBinDir = binDir.replaceAll("[\t\r\n]+", " ");
            normalizedBinDir = Util.replaceMacro(normalizedBinDir, env);
            if (!normalizedBinDir.isEmpty()) {
                FilePath binPath = workspace.child(normalizedBinDir);
                args.add("-o");
                args.add(binPath);
            }
        }

        // Add tempDir
        String normalizedTempDir = null;
        if (tempDir != null && tempDir.trim().length() != 0) {
            normalizedTempDir = tempDir.replaceAll("[\t\r\n]+", " ");
            normalizedTempDir = Util.replaceMacro(normalizedTempDir, env);
            if (!normalizedTempDir.isEmpty()) {
                FilePath tempPath = workspace.child(normalizedTempDir);
                args.add("-t");
                args.add(tempPath);
            }
        }

        if (buildMode != null && buildMode.trim().length() != 0) {
            args.add("-buildMode");
            args.add(buildMode);
        }

        if (simulation) {
            args.add("-simulation");
        }

        if (buildRUCPackage) {
            args.add("-buildRUCPackage");
        }

        FilePath pwd = workspace;
        String fullProjectFilePath = workspace.child(projectFile).getRemote();

        // Set version if given
        if (version != null) {
            city.atomic.automationstudio.Project p = city.atomic.automationstudio.Project.load(fullProjectFilePath);
            city.atomic.automationstudio.Config c = p.findConfig(configurationName);
            if (c != null) {
                Hardware hw = c.getHardware();
                hw.setConfigVersion(version);
                hw.save();
            }
        }

        // Set build options if given
        if (buildOptions != null) {
            city.atomic.automationstudio.Project p = city.atomic.automationstudio.Project.load(fullProjectFilePath);
            city.atomic.automationstudio.Config c = p.findConfig(configurationName);
            if (c != null) {
                Cpu cpu = c.getCpu();
                cpu.setAdditionalBuildOptions(buildOptions);
                cpu.save();
            }
        }
        if (ansicBuildOptions != null) {
            city.atomic.automationstudio.Project p = city.atomic.automationstudio.Project.load(fullProjectFilePath);
            city.atomic.automationstudio.Config c = p.findConfig(configurationName);
            if (c != null) {
                Cpu cpu = c.getCpu();
                cpu.setAnsicAdditionalBuildOptions(ansicBuildOptions);
                cpu.save();
            }
        }

        try {
            listener.getLogger().printf("Executing the command \"%s\" from \"%s\"%n", args.toString(), pwd);

            AutomationStudioConsoleAnnotator annotator = new AutomationStudioConsoleAnnotator(listener.getLogger(),
                    run.getCharset());

            int result = launcher.launch().cmds(args.toWindowsCommand()).envs(env).stdout(annotator).pwd(pwd).join();
            if (result == 0) {
                // No errors or warnings
                listener.getLogger().println("No errors or warnings during build.");
            } else if (result == 1) {
                // Warnings
                listener.getLogger().println("Warnings present during build.");
                if (unstableIfWarnings) {
                    listener.getLogger().println("Setting build to UNSTABLE because of warnings.");
                    run.setResult(Result.UNSTABLE);
                }
            } else if (result == 3) {
                // Errors
                listener.getLogger().println("Errors present during build.");
                run.setResult(failureResult);
            }
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            run.setResult(failureResult);
        }
    }

    @Extension
    @Symbol("automationStudio")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @CopyOnWrite
        private volatile AutomationStudioInstallation[] installations = new AutomationStudioInstallation[0];

        public DescriptorImpl() {
            super(AutomationStudioBuilder.class);
            load();
        }

        @NotNull
        @Override
        public String getDisplayName() {
            return "Automation Studio";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public AutomationStudioInstallation[] getInstallations() {
            return Arrays.copyOf(installations, installations.length);
        }

        public void setInstallations(AutomationStudioInstallation... antInstallations) {
            this.installations = antInstallations;
            save();
        }

        public ListBoxModel doFillBuildModeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Build", "Build");
            items.add("Rebuild", "Rebuild");
            items.add("Build & Transfer", "BuildAndTransfer");
            items.add("Build & Create Compact Flash", "BuildAndCreateCompactFlash");

            return items;
        }
    }
}
