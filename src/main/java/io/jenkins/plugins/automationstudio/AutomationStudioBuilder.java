package io.jenkins.plugins.automationstudio;

import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.ListBoxModel;
import org.jenkinsci.Symbol;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Arrays;

public class AutomationStudioBuilder extends Builder {
    // GUI Fields
    private final String automationStudioName;
    private final String projectFile;
    private final String configurationName;
    private final String buildMode;
    private final boolean simulation;
    private final boolean buildRUCPackage;
    private final String tempDir;
    private final String binDir;
    private final boolean unstableIfWarnings;
    private final boolean continueOnErrors;

    @DataBoundConstructor
    public AutomationStudioBuilder(String automationStudioName, String projectFile, String configurationName,
                                   String buildMode, boolean simulation, boolean buildRUCPackage, String tempDir,
                                   String binDir, boolean unstableIfWarnings, boolean continueOnErrors) {
        this.automationStudioName = automationStudioName;
        this.projectFile = projectFile;
        this.configurationName = configurationName;
        this.buildMode = buildMode;
        this.simulation = simulation;
        this.buildRUCPackage = buildRUCPackage;
        this.tempDir = tempDir;
        this.binDir = binDir;
        this.unstableIfWarnings = unstableIfWarnings;
        this.continueOnErrors = continueOnErrors;
    }

    public String getAutomationStudioName() {
        return automationStudioName;
    }

    public String getProjectFile() {
        return projectFile;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public String getBuildMode() {
        return buildMode;
    }

    public boolean isSimulation() {
        return simulation;
    }

    public boolean isBuildRUCPackage() {
        return buildRUCPackage;
    }

    public String getTempDir() {
        return tempDir;
    }

    public String getBinDir() {
        return binDir;
    }

    public boolean isUnstableIfWarnings() { return unstableIfWarnings; }

    public boolean isContinueOnErrors() { return continueOnErrors; }

    public AutomationStudioInstallation getInstallation() {
        DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();
        for (AutomationStudioInstallation i : descriptor.getInstallations()) {
            if (automationStudioName != null && i.getName().equals(automationStudioName)) {
                return i;
            }
        }
        return null;
    }

    private static String getFullToolPath(@NotNull Launcher launcher, String pathToTool, String execName)
            throws IOException, InterruptedException {
        String fullPath = (pathToTool != null ? pathToTool : "");
        FilePath exe = new FilePath(launcher.getChannel(), fullPath);

        if (exe.isDirectory()) {
            if (!fullPath.endsWith("\\")) {
                fullPath += "\\";
            }
            fullPath += execName;
        }

        return fullPath;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        ArgumentListBuilder args = new ArgumentListBuilder();
        String exeName = "BR.AS.Build.exe";
        AutomationStudioInstallation installation = getInstallation();

        if (launcher.isUnix()) {
            listener.fatalError("This builder can't be used on Unix systems");
            return false;
        }

        if (installation == null) {
            args.add(exeName);
        } else {
            EnvVars env = build.getEnvironment(listener);
            Node node = Computer.currentComputer().getNode();

            if (node != null) {
                installation = installation.forNode(node, listener);
                installation = installation.forEnvironment(env);
                String pathToTool = getFullToolPath(launcher, installation.getHome(), exeName);
                FilePath exe = new FilePath(launcher.getChannel(), pathToTool);
/*
                try {
                    if (!exe.exists()) {
                        listener.fatalError(pathToTool + " doesn't exist");
                        return false;
                    }
                } catch (IOException e) {
                    listener.fatalError("Failed checking for existence of " + pathToTool);
                    return false;
                }
*/
                listener.getLogger().println("Path to BR.AS.Build.exe: " + pathToTool);
                args.add(pathToTool);
            }
        }

        EnvVars env = build.getEnvironment(listener);

        String normalizedProject = null;
        if (projectFile != null && projectFile.trim().length() != 0) {
            normalizedProject = projectFile.replaceAll("[\t\r\n]+", " ");
            normalizedProject = Util.replaceMacro(normalizedProject, env);
            normalizedProject = Util.replaceMacro(normalizedProject, build.getBuildVariables());
            if (!normalizedProject.isEmpty()) {
                // Project file needs to be absolute
                FilePath projectPath = build.getWorkspace().child(normalizedProject);
                args.add(projectPath);
            }
        }

        String normalizedConfig = null;
        if (configurationName != null && configurationName.trim().length() != 0) {
            normalizedConfig = configurationName.replaceAll("[\t\r\n]+", " ");
            normalizedConfig = Util.replaceMacro(normalizedConfig, env);
            normalizedConfig = Util.replaceMacro(normalizedConfig, build.getBuildVariables());
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
            normalizedBinDir = Util.replaceMacro(normalizedBinDir, build.getBuildVariables());
            if (!normalizedBinDir.isEmpty()) {
                FilePath binPath = build.getWorkspace().child(normalizedBinDir);
                args.add("-o");
                args.add(binPath);
            }
        }

        // Add tempDir
        String normalizedTempDir = null;
        if (tempDir != null && tempDir.trim().length() != 0) {
            normalizedTempDir = tempDir.replaceAll("[\t\r\n]+", " ");
            normalizedTempDir = Util.replaceMacro(normalizedTempDir, env);
            normalizedTempDir = Util.replaceMacro(normalizedTempDir, build.getBuildVariables());
            if (!normalizedTempDir.isEmpty()) {
                FilePath tempPath = build.getWorkspace().child(normalizedTempDir);
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

        // Determine if PWD is module root or workspace
        FilePath pwd = build.getModuleRoot();
        if (normalizedProject != null) {
            if (!pwd.child(normalizedProject).exists()) {
                pwd = build.getWorkspace();
            }
        }

        try {
            listener.getLogger().println(String.format("Executing the command \"%s\" from \"%s\"",
                    args.toString(), pwd));

            AutomationStudioConsoleAnnotator annotator = new AutomationStudioConsoleAnnotator(listener.getLogger(),
                    build.getCharset());

            int result = launcher.launch().cmds(args.toWindowsCommand()).envs(env).stdout(annotator).pwd(pwd).join();
            if (result == 0) {
                // No errors or warnings
                listener.getLogger().println("No errors or warnings during build.");
            } else if (result == 1) {
                // Warnings
                listener.getLogger().println("Warnings present during build.");
                if (unstableIfWarnings) {
                    listener.getLogger().println("Setting build to UNSTABLE because of warnings.");
                    build.setResult(Result.UNSTABLE);
                }
            } else if (result == 3) {
                // Errors
                listener.getLogger().println("Errors present during build.");
                return continueOnErrors ? true : false;
            }

            return true;
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    @Extension
    @Symbol("automationstudio")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @CopyOnWrite
        private volatile AutomationStudioInstallation[] installations = new AutomationStudioInstallation[0];

        public DescriptorImpl() {
            super(AutomationStudioBuilder.class);
            load();
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
