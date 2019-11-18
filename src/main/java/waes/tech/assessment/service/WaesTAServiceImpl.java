package waes.tech.assessment.service;

import com.google.common.collect.*;
import com.google.gson.*;
import com.google.inject.*;
import org.slf4j.*;

import java.util.*;

import static org.slf4j.LoggerFactory.*;

@Singleton
public class WaesTAServiceImpl implements WaesTAService {

    private static final Logger LOG = getLogger(WaesTAServiceImpl.class.getCanonicalName());
    private static final Gson gson = new Gson();

    private ElasticSearchService elasticSearchService;

    @Inject
    WaesTAServiceImpl(final ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    /**
     * Save element object.
     *
     * @param elementPart Either right or left part of the element in Json format.
     * @param id          Id of the element.
     * @param side        Side of the element. Either right or left.
     * @return String representing result of the upsert.
     */
    public String saveElement(JsonObject elementPart, String id, String side) {

        Map<String, Object> mapToIndex = gson.fromJson(elementPart, Map.class); //Creating a map of one item in which the key is the side of the element

        return gson.toJson(elasticSearchService.save(mapToIndex, id, side));
    }

    /**
     * Gets diff of parts left and right for element with given id.
     *
     * @param id element id
     * @return Diff of parts right and left of element with given id.
     */
    public String getDiffOfElementById(String id) {
        if (id == null || id.trim().isEmpty()) //Checking id validity
            return "Missing mandatory field id or id is empty.";

        LOG.info("Attempting to retrieve element of id {}", id);
        JsonObject response = elasticSearchService.search(id);

        if (Objects.isNull(response))
            return "There is no data stored with that id.";


        LOG.debug("Found element: {}", response);
        Map right = gson.fromJson(response.get("right"), Map.class);
        Map left = gson.fromJson(response.get("left"), Map.class);

        //Checking if we have everything in place to perform the diff

        if (Objects.isNull(right))
            return "Missing right input for id " + id;
        if (Objects.isNull(left))
            return "Missing left input for id " + id;


        LOG.debug("About to evaluate diff.");
        //Analyzing diff and returning
        if (right.equals(left)) return "Inputs are equal.";
        else if (right.size() != left.size()) return "Input sizes are different.";
        else return "Differences found: " + Maps.difference(right, left).toString();
        //Returning proper differences since I'm assuming is more valuable
    }

}