package io.cockroachdb.pestcontrol.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import io.cockroachdb.pestcontrol.schema.ClusterProperties;
import io.cockroachdb.pestcontrol.schema.ClusterType;
import io.cockroachdb.pestcontrol.schema.NodeModel;
import io.cockroachdb.pestcontrol.schema.nodes.Locality;

@Component
public class LocalDisruptionManager implements DisruptionManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ShellCommands shellCommands = new ShellCommands() {
    };

    @Value("${application.scriptPath}")
    private Path scriptPath;

    private static void copy(InputStream in, OutputStream out) throws IOException {
        try (InputStream is = new BufferedInputStream(in)) {
            byte[] buffer = new byte[1024 * 8];
            int len;
            while ((len = is.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }

    @Override
    public boolean supports(ClusterType clusterType) {
        return EnumSet.of(ClusterType.local_insecure, ClusterType.local_secure).contains(clusterType);
    }

    @Override
    public void disruptNode(ClusterProperties clusterProperties, NodeModel nodeModel) {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeProcess(shellCommands.disrupt(nodeModel), barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }
    }

    @Override
    public void recoverNode(ClusterProperties clusterProperties, NodeModel nodeModel) {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeProcess(shellCommands.recover(nodeModel), barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }
    }

    @Override
    public void disruptLocality(ClusterProperties clusterProperties, Locality locality) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recoverLocality(ClusterProperties clusterProperties, Locality locality) {
        throw new UnsupportedOperationException();
    }

    private int executeProcess(List<String> commands, ByteArrayOutputStream barr) {
        int code = -1;
        Instant start = Instant.now();

        try {
            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(scriptPath.toFile())
                    .start();

            logger.debug("Started process: %s".formatted(process.info()));

            process.waitFor(30, TimeUnit.SECONDS);
            try (InputStream inputStream = process.getInputStream();
                 InputStream errorStream = process.getErrorStream()) {
                copy(inputStream, barr);
                copy(errorStream, barr);
            }

            code = process.exitValue();

            logger.debug("Process finished in %s with exit code %d: %s"
                    .formatted(Duration.between(start, Instant.now()), code, process.info()));

            return code;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException("Timeout waiting for process completion", e);
        } catch (IOException e) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), e);
        }
    }
}
