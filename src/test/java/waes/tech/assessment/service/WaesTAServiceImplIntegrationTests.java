package waes.tech.assessment.service;

import com.google.gson.*;
import org.elasticsearch.action.get.*;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.*;
import org.junit.*;
import org.slf4j.*;
import org.testcontainers.elasticsearch.*;
import waes.tech.assessment.utils.*;

import static org.junit.Assert.*;
import static org.slf4j.LoggerFactory.*;

public class WaesTAServiceImplIntegrationTests {
    private static final Logger LOG = getLogger(WaesTAServiceImplIntegrationTests.class.getCanonicalName());
    private static final String INDEX = "wta-index";
    private static final Gson gson = new Gson();

    @Rule
    public ElasticsearchContainer container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:7.4.2");
    private WaesTAService waesTAService = new WaesTAServiceImpl(new ElasticSearchService());

    @Before
    public void setUp() {
        System.setProperty("wta.elasticsearch.host", container.getContainerIpAddress());
        System.setProperty("wta.elasticsearch.port", container.getFirstMappedPort() + "");
        // Start the container. This step might take some time...
        container.start();

        try (RestHighLevelClient client = ESProvider.getESClient()) { //Initializing index if it doesn't exist
            GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX);
            if (!client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);
                client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            }

        } catch (Exception e) {
            LOG.error("Unexpected error: ", e);
        }
    }

    @After
    public void tearDown() {
        // Stop the container.
        container.stop();
    }

    @Test
    public void fetchingNonExistentIdReturnsProperMessage() {
        assertEquals("There is no data stored with that id.", waesTAService.getDiffOfElementById("non-existent_id"));
    }

    @Test
    public void shouldInsertRightPartOfElement() {
        JsonObject jsonObject = new JsonObject();
        JsonObject right = new JsonObject();
        right.add("test_key_1", JsonParser.parseString("test_value_1"));
        jsonObject.add("right", right);

        waesTAService.saveElement(right, "test_id_1", "right");

        try (RestHighLevelClient client = ESProvider.getESClient()) {
            GetRequest getRequest = new GetRequest(INDEX, "test_id_1");
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            assertEquals("test_id_1", getResponse.getId());
        } catch (Exception e) {
            LOG.error("Unexpected error: ", e);
        }
    }

    @Test
    public void shouldInsertRightPartOfElementAndAskForDiffReturnsLeftIsMissing() {
        JsonObject jsonObject = new JsonObject();
        JsonObject right = new JsonObject();
        right.add("test_key_1", JsonParser.parseString("test_value_1"));
        jsonObject.add("right", right);

        waesTAService.saveElement(right, "test_id_6", "right");

        try (RestHighLevelClient client = ESProvider.getESClient()) {
            GetRequest getRequest = new GetRequest(INDEX, "test_id_6");
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            assertEquals("test_id_6", getResponse.getId());
        } catch (Exception e) {
            LOG.error("Unexpected error: ", e);
        }

        String result = waesTAService.getDiffOfElementById("test_id_6");

        assertEquals(result, "Missing left input for id test_id_6");
    }

    @Test
    public void shouldInsertFullElementWithEqualPartsAndGetDiffMessageAccordingly() {
        JsonObject jsonObject = new JsonObject();
        JsonObject right = new JsonObject();
        right.add("test_key_1", JsonParser.parseString("test_value_1"));
        JsonObject left = new JsonObject();
        left.add("test_key_1", JsonParser.parseString("test_value_1"));

        jsonObject.add("right", right);
        jsonObject.add("left", left);

        waesTAService.saveElement(right, "test_id_2", "right");
        waesTAService.saveElement(left, "test_id_2", "left");

        try (RestHighLevelClient client = ESProvider.getESClient()) { //Asserting that both parts of the element were indexed
            GetRequest getRequest = new GetRequest(INDEX, "test_id_2");
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            JsonObject response = gson.fromJson(getResponse.getSourceAsString(), JsonObject.class);
            assertNotNull(response.get("right"));
            assertNotNull(response.get("left"));
        } catch (Exception e) {
            LOG.error("Unexpected error: ", e);
        }

        //Retrieving diff analysis result.
        String analysisResult = waesTAService.getDiffOfElementById("test_id_2");
        assertEquals("Inputs are equal.", analysisResult);
    }

    @Test
    public void shouldInsertFullElementWithDifferentSizedPartsAndGetDiffMessageAccordingly() {
        JsonObject jsonObject = new JsonObject();
        JsonObject right = new JsonObject();
        right.add("test_key_1", JsonParser.parseString("test_value_1"));
        right.add("test_key_2", JsonParser.parseString("test_value_2"));
        JsonObject left = new JsonObject();
        left.add("test_key_1", JsonParser.parseString("test_value_1"));

        jsonObject.add("right", right);
        jsonObject.add("left", left);

        waesTAService.saveElement(right, "test_id_3", "right");
        waesTAService.saveElement(left, "test_id_3", "left");

        try (RestHighLevelClient client = ESProvider.getESClient()) { //Asserting that both parts of the element were indexed
            GetRequest getRequest = new GetRequest(INDEX, "test_id_3");
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            JsonObject response = gson.fromJson(getResponse.getSourceAsString(), JsonObject.class);
            assertNotNull(response.get("right"));
            assertNotNull(response.get("left"));
        } catch (Exception e) {
            LOG.error("Unexpected error: ", e);
        }

        //Retrieving diff analysis result.
        String analysisResult = waesTAService.getDiffOfElementById("test_id_3");
        assertEquals("Input sizes are different.", analysisResult);
    }

    @Test
    public void shouldInsertFullElementWithDifferentPartsAndGetDiffMessageAccordingly() {
        JsonObject jsonObject = new JsonObject();
        JsonObject right = new JsonObject();
        right.add("test_key_1", JsonParser.parseString("test_value_1"));
        right.add("test_key_2", JsonParser.parseString("test_value_2"));
        right.add("test_key_3", JsonParser.parseString("test_value_3"));
        JsonObject left = new JsonObject();
        left.add("test_key_4", JsonParser.parseString("test_value_4"));
        left.add("test_key_5", JsonParser.parseString("test_value_5"));
        left.add("test_key_6", JsonParser.parseString("test_value_6"));

        jsonObject.add("right", right);
        jsonObject.add("left", left);

        waesTAService.saveElement(right, "test_id_4", "right");
        waesTAService.saveElement(left, "test_id_4", "left");

        try (RestHighLevelClient client = ESProvider.getESClient()) { //Asserting that both parts of the element were indexed
            GetRequest getRequest = new GetRequest(INDEX, "test_id_4");
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            JsonObject response = gson.fromJson(getResponse.getSourceAsString(), JsonObject.class);
            assertNotNull(response.get("right"));
            assertNotNull(response.get("left"));
        } catch (Exception e) {
            LOG.error("Unexpected error: ", e);
        }

        //Retrieving diff analysis result.
        String analysisResult = waesTAService.getDiffOfElementById("test_id_4");
        assertTrue(analysisResult.contains("Differences found: "));
    }

}
