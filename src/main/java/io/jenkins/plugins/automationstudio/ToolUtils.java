package io.jenkins.plugins.automationstudio;

import hudson.FilePath;
import hudson.Launcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ToolUtils {
    public static String getFullToolPath(@NotNull Launcher launcher, String pathToTool, String execName)
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
}
