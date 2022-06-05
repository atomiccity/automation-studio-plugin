package io.jenkins.plugins.automationstudio;

import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;

public class AutomationStudioConsoleAnnotator extends LineTransformationOutputStream {
    private final OutputStream out;
    private final Charset charset;

    public AutomationStudioConsoleAnnotator(OutputStream out, Charset charset) {
        this.out = out;
        this.charset = charset;
    }

    @Override
    protected void eol(byte[] b, int len) throws IOException {
        String line = charset.decode(ByteBuffer.wrap(b, 0, len)).toString();

        line = trimEOL(line);

        Matcher m = AutomationStudioErrorNote.PATTERN.matcher(line);
        if (m.matches()) {
            new AutomationStudioErrorNote().encodeTo(out);
        }

        m = AutomationStudioWarningNote.PATTERN.matcher(line);
        if (m.matches()) {
            new AutomationStudioWarningNote().encodeTo(out);
        }

        out.write(b, 0, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        out.close();
    }
}
