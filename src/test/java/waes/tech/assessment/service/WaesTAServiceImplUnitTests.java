package waes.tech.assessment.service;

import com.google.gson.*;
import org.junit.*;
import org.slf4j.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.*;

public class WaesTAServiceImplUnitTests {
    private static final Logger LOG = getLogger(WaesTAServiceImpl.class.getCanonicalName());
    private static final Gson gson = new Gson();

    private ElasticSearchService mockedElasticSearchService = mock(ElasticSearchService.class);
    private WaesTAServiceImpl underTest = new WaesTAServiceImpl(mockedElasticSearchService);

    @Test
    public void nullIdShouldReturnProperMessage() {
        String result = underTest.getDiffOfElementById(null);

        assertEquals("Missing mandatory field id or id is empty.", result);
    }

    @Test
    public void emptyIdShouldReturnProperMessage() {
        String result = underTest.getDiffOfElementById(" ");

        assertEquals("Missing mandatory field id or id is empty.", result);
    }

    @Test
    public void emptyResponseFromESShouldReturnProperMessage() {

        when(mockedElasticSearchService.search(anyString())).thenReturn(null);

        String result = underTest.getDiffOfElementById("mocked_id");

        assertEquals("There is no data stored with that id.", result);

    }

    @Test
    public void shouldFailDueToRightPartNotExisting() {
        JsonObject response = new JsonObject();
        response.add("left", new JsonObject());

        when(mockedElasticSearchService.search(anyString())).thenReturn(response);

        String result = underTest.getDiffOfElementById("mocked_id");

        assertEquals("Missing right input for id mocked_id", result);
    }

    @Test
    public void shouldFailDueToLeftPartNotExisting() {
        JsonObject response = new JsonObject();
        response.add("right", new JsonObject());

        when(mockedElasticSearchService.search(anyString())).thenReturn(response);

        String result = underTest.getDiffOfElementById("mocked_id");

        assertEquals("Missing left input for id mocked_id", result);
    }

    @Test
    public void shouldReturnProperMessageToBothPartsBeingEqual() {
        JsonObject response = new JsonObject();
        response.add("right", new JsonObject());
        response.add("left", new JsonObject());

        when(mockedElasticSearchService.search(anyString())).thenReturn(response);

        String result = underTest.getDiffOfElementById("mocked_id");

        assertEquals("Inputs are equal.", result);
    }

    @Test
    public void shouldReturnProperMessageToOnePartHavingBiggerSize() {
        JsonObject right = new JsonObject();
        right.add("property_1", JsonParser.parseString("value_1"));
        JsonObject response = new JsonObject();
        response.add("right", right);
        response.add("left", new JsonObject());

        when(mockedElasticSearchService.search(anyString())).thenReturn(response);

        String result = underTest.getDiffOfElementById("mocked_id");

        assertEquals("Input sizes are different.", result);
    }

    @Test
    public void shouldReturnProperMessageToHavingDifferences() {
        JsonObject right = new JsonObject();
        right.add("property_1", JsonParser.parseString("value_1"));
        JsonObject left = new JsonObject();
        right.add("property_2", JsonParser.parseString("value_2"));
        JsonObject response = new JsonObject();
        response.add("right", right);
        response.add("left", left);

        when(mockedElasticSearchService.search(anyString())).thenReturn(response);

        String result = underTest.getDiffOfElementById("mocked_id");

        assertTrue(result.contains("Input sizes are different."));
    }

}