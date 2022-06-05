package io.jenkins.plugins.automationstudio;

import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

import java.util.regex.Pattern;

public class AutomationStudioErrorNote extends ConsoleNote {
    public final static Pattern PATTERN = Pattern.compile("(.*)[Ee]rror\\\\s(([A-Z]*)\\\\d+)?:\\\\s(.*)");

    @Override
    public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
        text.addMarkup(0, text.length(), "<span class=error-inline>", "</span>");
        return null;
    }
}
