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
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class AutomationStudioInstallation extends ToolInstallation
        implements NodeSpecific<AutomationStudioInstallation>, EnvironmentSpecific<AutomationStudioInstallation> {
    @DataBoundConstructor
    public AutomationStudioInstallation(String name, String home) {
        super(name, home, null);
    }

    @Override
    public AutomationStudioInstallation forEnvironment(EnvVars environment) {
        return new AutomationStudioInstallation(getName(), environment.expand(getHome()));
    }

    @Override
    public AutomationStudioInstallation forNode(@NonNull Node node, TaskListener log)
            throws IOException, InterruptedException {
        return new AutomationStudioInstallation(getName(), translateFor(node, log));
    }

    @Extension
    @Symbol("automationstudio")
    public static class DescriptorImpl extends ToolDescriptor<AutomationStudioInstallation> {
        @NonNull
        @Override
        public String getDisplayName() {
            return "Automation Studio";
        }

        @Override
        public AutomationStudioInstallation[] getInstallations() {
            return getDescriptor().getInstallations();
        }

        @Override
        public void setInstallations(AutomationStudioInstallation... installations) {
            getDescriptor().setInstallations(installations);
        }

        private AutomationStudioBuilder.DescriptorImpl getDescriptor() {
            Jenkins jenkins = Jenkins.getInstanceOrNull();
            if (jenkins != null && jenkins.getDescriptorByType(AutomationStudioBuilder.DescriptorImpl.class) != null) {
                return jenkins.getDescriptorByType(AutomationStudioBuilder.DescriptorImpl.class);
            } else {
                throw new NullPointerException("AutomationStudioBuilder.DescriptorImpl is null");
            }
        }
    }
}
