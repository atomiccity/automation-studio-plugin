package io.jenkins.plugins.automationstudio;

import city.atomic.automationstudio.unittest.UnitTestServerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AutomationStudioUnitTest extends Builder implements SimpleBuildStep {
    private final String simExecutableDir;
    private final String outputDir;

    private String unitTestList;

    private int webServerPort;

    @DataBoundConstructor
    public AutomationStudioUnitTest(String simExecutableDir, String outputDir) {
        this.simExecutableDir = simExecutableDir;
        this.outputDir = outputDir;
        this.webServerPort = 80;
        this.unitTestList = null;
    }

    public String getUnitTestList() {
        return unitTestList;
    }

    @DataBoundSetter
    public void setUnitTestList(String unitTestList) {
        this.unitTestList = unitTestList;
    }

    public int getWebServerPort() {
        return webServerPort;
    }

    @DataBoundSetter
    public void setWebServerPort(int webServerPort) {
        this.webServerPort = webServerPort;
    }

    @Override
    public void perform(@NotNull Run<?, ?> run, @NotNull FilePath workspace, @NotNull EnvVars env,
                        @NotNull Launcher launcher, @NotNull TaskListener listener)
            throws InterruptedException, IOException {
        ArgumentListBuilder args = new ArgumentListBuilder();
        FilePath simExecutableFullPath = workspace.child(simExecutableDir).child("ar000loader.exe");

        if (!simExecutableFullPath.exists()) {
            listener.fatalError("Sim executable doesn't exist: " + simExecutableFullPath);
            run.setResult(Result.FAILURE);
            return;
        }

        args.add(workspace.child(simExecutableDir));
        args.add("-i127.0.0.1");
        args.add("-p4003");

        // Start SIM
        listener.getLogger().println("Starting sim process: " + args.toString());
        launcher.launch().cmds(args.toWindowsCommand()).envs(env).pwd(workspace);

        // Wait for SIM to enter RUN mode
        listener.getLogger().println("Waiting for sim to enter RUN mode...");
        Socket socket = new Socket("127.0.0.1", 4003);
        socket.setSoTimeout(2000);
        OutputStream socketOut = socket.getOutputStream();
        InputStream socketIn = socket.getInputStream();
        int retries = 250;
        boolean runMode = false;

        while (!runMode && retries > 0) {
            byte[] recvData = new byte[1024];
            socketOut.write("<Status Command=\"10\"/>".getBytes());
            socketIn.read(recvData);
            if (recvData.equals("<AR status RUN Command=\"99\"/>\0".getBytes())) {
                listener.getLogger().println("Sim is now in RUN mode");
                runMode = true;
                socket.close();
            }
            Thread.sleep(2000);
            retries--;
        }

        if (!runMode) {
            listener.fatalError("Could not start sim");
            run.setResult(Result.FAILURE);
            return;
        }

        List<String> testsToRun = new ArrayList<String>();
        if (unitTestList != null) {
            testsToRun.addAll(Arrays.asList(unitTestList.split(",")));
        } else {
            listener.getLogger().println("Fetching list of unit tests");
            URL url = new URL("http://127.0.0.1:" + webServerPort + "/WsTest/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            String response = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            if (conn.getResponseCode() != 200) {
                listener.getLogger().println("Response from unit test list request: " + conn.getResponseCode());
                listener.getLogger().println("Response: " + response);
                listener.fatalError("Couldn't get list of unit tests");
                run.setResult(Result.FAILURE);
                return;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            UnitTestServerResponse unitTestResponse = objectMapper.readValue(response, UnitTestServerResponse.class);
            for (UnitTestServerResponse.UnitTest ut : unitTestResponse.getItemList()) {
                listener.getLogger().println("Found unit test: " + ut.getDevice() + " - " + ut.getDescription());
                testsToRun.add(ut.getDevice());
            }
        }

        for (String test : testsToRun) {
            runTest(test, listener);
        }

        // Shutdown the sim
        args = new ArgumentListBuilder();
        FilePath simStopExecutableFullPath = workspace.child(simExecutableDir).child("ar000stop.exe");
        args.add(simStopExecutableFullPath);
        listener.getLogger().println("Stopping the sim: " + args.toString());
        launcher.launch().cmds(args.toWindowsCommand()).envs(env).pwd(workspace);
    }

    private void runTest(String testName, TaskListener listener) {
        listener.getLogger().println("Running test " + testName + "...");

        try {
            File resultOutputDir = new File(outputDir);
            resultOutputDir.mkdirs();
            URL url = new URL("http://127.0.0.1:" + webServerPort + "/WsTest/" + testName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            if (conn.getResponseCode() != 200) {
                listener.fatalError("Error starting unit test " + testName);
                in.close();
                conn.disconnect();
                return;
            }
            String inputLine;
            FileWriter fw = new FileWriter(resultOutputDir.getName() + "/" + testName + ".xml");
            while ((inputLine = in.readLine()) != null) {
                fw.write(inputLine);
            }
            in.close();
            fw.close();
            conn.disconnect();
        } catch (IOException e) {
            listener.fatalError("Error during unit test " + testName);
            throw new RuntimeException(e);
        }
    }
}
