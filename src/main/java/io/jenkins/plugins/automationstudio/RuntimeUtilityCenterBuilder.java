package io.jenkins.plugins.automationstudio;

import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import org.jenkinsci.Symbol;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Arrays;

public class RuntimeUtilityCenterBuilder extends Builder {
    private final String runtimeUtilityCenterName;
    private final String pilFile;
    private final boolean continueOnErrors;


    @DataBoundConstructor
    public RuntimeUtilityCenterBuilder(String runtimeUtilityCenterName, String pilFile, boolean continueOnErrors) {
        this.runtimeUtilityCenterName = runtimeUtilityCenterName;
        this.pilFile = pilFile;
        this.continueOnErrors = continueOnErrors;
    }

    public String getPilFile() {
        return pilFile;
    }

    public RuntimeUtilityCenterInstallation getInstallation() {
        RuntimeUtilityCenterBuilder.DescriptorImpl descriptor =
                (RuntimeUtilityCenterBuilder.DescriptorImpl) getDescriptor();
        for (RuntimeUtilityCenterInstallation i : descriptor.getInstallations()) {
            if (runtimeUtilityCenterName != null && i.getName().equals(runtimeUtilityCenterName)) {
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
        String exeName = "PVITransfer.exe";
        RuntimeUtilityCenterInstallation installation = getInstallation();

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
                listener.getLogger().println("Path to PVITransfer.exe: " + pathToTool);
                args.add(pathToTool);
            }
        }

        // Don't start the GUI
        args.add("-silent");
        // Send output to StdOut so buildserver records it
        args.add("-consoleOutput");

        EnvVars env = build.getEnvironment(listener);

        String normalizedPil = null;
        if (pilFile != null && pilFile.trim().length() != 0) {
            normalizedPil = pilFile.replaceAll("[\t\r\n]+", " ");
            normalizedPil = Util.replaceMacro(normalizedPil, env);
            normalizedPil = Util.replaceMacro(normalizedPil, build.getBuildVariables());
            if (!normalizedPil.isEmpty()) {
                // Project file needs to be absolute
                FilePath projectPath = build.getWorkspace().child(normalizedPil);
                args.add("-\"" + projectPath + "\"");
            }
        }

        // Determine if PWD is module root or workspace
        FilePath pwd = build.getModuleRoot();
        if (normalizedPil != null) {
            if (!pwd.child(normalizedPil).exists()) {
                pwd = build.getWorkspace();
            }
        }

        try {
            listener.getLogger().println(String.format("Executing the command \"%s\" from \"%s\"",
                    args.toString(), pwd));

            int result = launcher.launch().cmds(args.toWindowsCommand()).envs(env).pwd(pwd).join();
            return continueOnErrors ? true : (result == 0);
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    @Extension
    @Symbol("runtimeutilitycenter")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @CopyOnWrite
        private volatile RuntimeUtilityCenterInstallation[] installations = new RuntimeUtilityCenterInstallation[0];

        public DescriptorImpl() {
            super(RuntimeUtilityCenterBuilder.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        public RuntimeUtilityCenterInstallation[] getInstallations() {
            return Arrays.copyOf(installations, installations.length);
        }

        public void setInstallations(RuntimeUtilityCenterInstallation... antInstallations) {
            this.installations = antInstallations;
            save();
        }
    }
}
