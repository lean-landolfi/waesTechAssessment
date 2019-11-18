package waes.tech.assessment.service;

import org.junit.*;
import org.junit.rules.*;
import org.slf4j.*;
import org.testcontainers.elasticsearch.*;

import static org.slf4j.LoggerFactory.*;

class MainServiceIntegrationTests extends ExternalResource {
    private static final Logger LOG = getLogger(MainServiceIntegrationTests.class.getCanonicalName());

    @Rule
    private ElasticsearchContainer container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:6.4.1");

    @Before
    public void setUp() {
        // Start the container. This step might take some time...
        container.start();
    }

    @After
    public void tearDown() {
        // Stop the container.
        container.stop();
    }
}
