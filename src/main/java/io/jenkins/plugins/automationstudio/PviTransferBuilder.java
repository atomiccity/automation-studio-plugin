package io.jenkins.plugins.automationstudio;

import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;
import java.util.Arrays;

import static io.jenkins.plugins.automationstudio.ToolUtils.getFullToolPath;

public class PviTransferBuilder extends Builder implements SimpleBuildStep {
    private String runtimeUtilityCenterName;
    private final String pilFile;
    private boolean continueOnErrors;

    @DataBoundConstructor
    public PviTransferBuilder(String pilFile) {
        this.runtimeUtilityCenterName = null;
        this.pilFile = pilFile;
        this.continueOnErrors = false;
    }

    public PviTransferBuilder(String runtimeUtilityCenterName, String pilFile, boolean continueOnErrors) {
        this.runtimeUtilityCenterName = runtimeUtilityCenterName;
        this.pilFile = pilFile;
        this.continueOnErrors = continueOnErrors;
    }

    public String getRuntimeUtilityCenterName() {
        return runtimeUtilityCenterName;
    }

    @DataBoundSetter
    public void setRuntimeUtilityCenterName(String runtimeUtilityCenterName) {
        this.runtimeUtilityCenterName = runtimeUtilityCenterName;
    }

    public String getPilFile() {
        return pilFile;
    }

    public boolean isContinueOnErrors() {
        return continueOnErrors;
    }

    @DataBoundSetter
    public void setContinueOnErrors(boolean continueOnErrors) {
        this.continueOnErrors = continueOnErrors;
    }

    public PviTransferInstallation getInstallation() {
        PviTransferBuilder.DescriptorImpl descriptor =
                (PviTransferBuilder.DescriptorImpl) getDescriptor();
        for (PviTransferInstallation i : descriptor.getInstallations()) {
            if (runtimeUtilityCenterName == null || i.getName().equals(runtimeUtilityCenterName)) {
                return i;
            }
        }
        return null;
    }

    @Override
    public boolean requiresWorkspace() {
        return true;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(@NotNull Run<?, ?> run, @NotNull FilePath workspace, @NotNull EnvVars env,
                        @NotNull Launcher launcher, @NotNull TaskListener listener)
            throws InterruptedException, IOException {
        Result failureResult = continueOnErrors ? Result.NOT_BUILT : Result.FAILURE;
        ArgumentListBuilder args = new ArgumentListBuilder();
        String exeName = "PVITransfer.exe";
        PviTransferInstallation installation = getInstallation();

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

            listener.getLogger().println("Path to PVITransfer.exe: " + pathToTool);
            args.add(pathToTool);
        }

        // Don't start the GUI
        args.add("-silent");
        // Send output to StdOut so build server records it
        args.add("-consoleOutput");

        String normalizedPil = null;
        if (pilFile != null && pilFile.trim().length() != 0) {
            normalizedPil = pilFile.replaceAll("[\t\r\n]+", " ");
            normalizedPil = Util.replaceMacro(normalizedPil, env);
            if (!normalizedPil.isEmpty()) {
                // Project file needs to be absolute
                FilePath projectPath = workspace.child(normalizedPil);
                args.add("-" + projectPath);
            }
        }

        FilePath pwd = workspace;

        try {
            listener.getLogger().println(String.format("Executing the command \"%s\" from \"%s\"",
                    args.toWindowsCommand(), pwd));

            int result = launcher.launch().cmds(args.toWindowsCommand()).envs(env).pwd(pwd).join();
            if (result == 0) {
                run.setResult(Result.SUCCESS);
            } else {
                listener.getLogger().println("ERROR: PVITransfer.exe returned " + result);
                run.setResult(failureResult);
            }
        } catch (IOException e) {
            Util.displayIOException(e, listener);
        }
    }

    @Extension
    @Symbol("pviTransfer")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @CopyOnWrite
        private volatile PviTransferInstallation[] installations = new PviTransferInstallation[0];

        public DescriptorImpl() {
            super(PviTransferBuilder.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public PviTransferInstallation[] getInstallations() {
            return Arrays.copyOf(installations, installations.length);
        }

        public void setInstallations(PviTransferInstallation... antInstallations) {
            this.installations = antInstallations;
            save();
        }

        @NotNull
        @Override
        public String getDisplayName() {
            return "Automation Studio: Runtime Utility Center";
        }
    }
}
