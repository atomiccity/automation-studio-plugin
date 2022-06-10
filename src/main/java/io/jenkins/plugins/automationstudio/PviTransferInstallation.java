package io.jenkins.plugins.automationstudio;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class PviTransferInstallation extends ToolInstallation
        implements NodeSpecific<PviTransferInstallation>,
        EnvironmentSpecific<PviTransferInstallation> {
    @DataBoundConstructor
    public PviTransferInstallation(String name, String home) {
        super(name, home, null);
    }
    @Override
    public PviTransferInstallation forEnvironment(EnvVars environment) {
        return new PviTransferInstallation(getName(), environment.expand(getHome()));
    }

    @Override
    public PviTransferInstallation forNode(@NotNull Node node, TaskListener log) throws IOException, InterruptedException {
        return new PviTransferInstallation(getName(), translateFor(node, log));
    }

    @Extension
    @Symbol("pviTransfer")
    public static class DescriptorImpl extends ToolDescriptor<PviTransferInstallation> {
        @NonNull
        @Override
        public String getDisplayName() {
            return "Automation Studio: Runtime Utility Center";
        }

        @Override
        public PviTransferInstallation[] getInstallations() {
            return getDescriptor().getInstallations();
        }

        @Override
        public void setInstallations(PviTransferInstallation... installations) {
            getDescriptor().setInstallations(installations);
        }

        private PviTransferBuilder.DescriptorImpl getDescriptor() {
            Jenkins jenkins = Jenkins.getInstanceOrNull();
            if (jenkins != null &&
                    jenkins.getDescriptorByType(PviTransferBuilder.DescriptorImpl.class) != null) {
                return jenkins.getDescriptorByType(PviTransferBuilder.DescriptorImpl.class);
            } else {
                throw new NullPointerException("PviTransferBuilder.DescriptorImpl is null");
            }
        }
    }
}
