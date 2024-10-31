package io.cockroachdb.pestcontrol;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ActiveProfiles;

import io.cockroachdb.pestcontrol.config.ApplicationProperties;

@SpringBootTest(classes = {Application.class}, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration-test")
@Cluster("local-secure")
@ActiveProfiles({"default", "test", "secure"})
public abstract class AbstractIntegrationTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ApplicationProperties applicationProperties;

    protected String getTagValue() {
        Cluster tag = AnnotationUtils.findAnnotation(getClass(), Cluster.class);
        Assertions.assertNotNull(tag, "Missing @Cluster");
        return tag.value();
    }
}
