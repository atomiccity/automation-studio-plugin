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

public class RuntimeUtilityCenterInstallation extends ToolInstallation
        implements NodeSpecific<RuntimeUtilityCenterInstallation>,
        EnvironmentSpecific<RuntimeUtilityCenterInstallation> {
    @DataBoundConstructor
    public RuntimeUtilityCenterInstallation(String name, String home) {
        super(name, home, null);
    }
    @Override
    public RuntimeUtilityCenterInstallation forEnvironment(EnvVars environment) {
        return new RuntimeUtilityCenterInstallation(getName(), environment.expand(getHome()));
    }

    @Override
    public RuntimeUtilityCenterInstallation forNode(@NotNull Node node, TaskListener log) throws IOException, InterruptedException {
        return new RuntimeUtilityCenterInstallation(getName(), translateFor(node, log));
    }

    @Extension
    @Symbol("pviTransfer")
    public static class DescriptorImpl extends ToolDescriptor<RuntimeUtilityCenterInstallation> {
        @NonNull
        @Override
        public String getDisplayName() {
            return "Runtime Utility Center";
        }

        @Override
        public RuntimeUtilityCenterInstallation[] getInstallations() {
            return getDescriptor().getInstallations();
        }

        @Override
        public void setInstallations(RuntimeUtilityCenterInstallation... installations) {
            getDescriptor().setInstallations(installations);
        }

        private RuntimeUtilityCenterBuilder.DescriptorImpl getDescriptor() {
            Jenkins jenkins = Jenkins.getInstanceOrNull();
            if (jenkins != null &&
                    jenkins.getDescriptorByType(RuntimeUtilityCenterBuilder.DescriptorImpl.class) != null) {
                return jenkins.getDescriptorByType(RuntimeUtilityCenterBuilder.DescriptorImpl.class);
            } else {
                throw new NullPointerException("RuntimeUtilityCenterBuilder.DescriptorImpl is null");
            }
        }
    }
}
