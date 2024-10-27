package io.cockroachdb.pestcontrol;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.StringUtils;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication(exclude = {
        JdbcRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
public class Application implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Value("${server.port}")
    private String serverPort;

    private static void printHelpAndExit(String message) {
        System.out.println("Usage: java --jar pestcontrol.jar <options> [args..]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("--help                    this help");
        System.out.println("--profiles [profile,..]   spring profiles to activate");
        System.out.println("  Available profiles:");
        System.out.println("    verbose      - enable verbose activity logging.");
        System.out.println("    verbose-sql  - enable verbose SQL trace logging.");
        System.out.println("    verbose-http - enable verbose HTTP trace logging.");
        System.out.println();
        System.out.println("All other options are passed to spring container.");
        System.out.println();
        System.out.println(message);

        System.exit(0);
    }

    public static void main(String[] args) {
        LinkedList<String> argsList = new LinkedList<>(Arrays.asList(args));
        LinkedList<String> passThroughArgs = new LinkedList<>();

        Set<String> profiles =
                StringUtils.commaDelimitedListToSet(System.getProperty("spring.profiles.active"));

        while (!argsList.isEmpty()) {
            String arg = argsList.pop();
            if (arg.equals("--help")) {
                printHelpAndExit("");
            } else if (arg.equals("--profiles")) {
                if (argsList.isEmpty()) {
                    printHelpAndExit("Expected list of profile names");
                }
                profiles.clear();
                profiles.addAll(StringUtils.commaDelimitedListToSet(argsList.pop()));
            } else {
                passThroughArgs.add(arg);
            }
        }

        if (!profiles.isEmpty()) {
            System.setProperty("spring.profiles.active", String.join(",", profiles));
        }

        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.SERVLET)
                .logStartupInfo(true)
                .profiles(profiles.toArray(new String[0]))
                .run(passThroughArgs.toArray(new String[] {}));
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info(
                "Welcome to Pest Control, see http://localhost:%s".formatted(serverPort));
    }
}
