package waes.tech.assessment.service;

import com.google.common.collect.*;
import com.google.gson.*;
import com.google.inject.*;
import org.apache.http.*;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.*;
import org.elasticsearch.action.support.replication.*;
import org.elasticsearch.action.update.*;
import org.elasticsearch.client.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static org.slf4j.LoggerFactory.*;

@Singleton
public class MainService {

    private static final Logger LOG = getLogger(MainService.class.getCanonicalName());
    private static final Gson gson = new Gson();

    /**
     * Save element object.
     *
     * @param elementPart Either right or left part of the element in Json format.
     * @param id          Id of the element.
     * @param side        Side of the element. Either right or left.
     * @return String representing result of the upsert.
     */
    public String saveElement(JsonObject elementPart, String id, String side) {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")))) {

            Map<String, Object> mapToIndex = gson.fromJson(elementPart, Map.class); //Creating a map of one item in which the key is the side of the element

            //Create an upsert statement to ensure that we attempt to create one element in ElasticSearch only if it doesn't exist or we update it if it does
            IndexRequest insertRequest = new IndexRequest("test").id(id).source(jsonBuilder()
                    .startObject()
                    .field(side, mapToIndex)
                    .endObject());
            UpdateRequest updateRequest = new UpdateRequest().index("test").id(id).doc(jsonBuilder()
                    .startObject()
                    .field(side, mapToIndex)
                    .endObject())
                    .upsert(insertRequest);

            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);

            ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo(); //Retrieving upsert status.

            if (shardInfo.getSuccessful() > 0) { //Upsert was successful
                LOG.info("Update successful.");
                return "Update successful";
            } else if (shardInfo.getFailed() > 0) { //Upsert failed.
                LOG.info("Update failed.");
                Arrays.stream(shardInfo.getFailures()).forEach(failure ->
                        LOG.info("Error was: %s", failure.reason()) //Printing every failure reason
                );
                return "Update failed. Please see logs.";
            } else {
                LOG.info("No upserts took place since there were nothing to update/insert.");
                return "No upserts took place since there were nothing to update/insert.";
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return "Unexpected error.";
        }
    }

    /**
     * Gets diff of parts left and right for element with given id.
     *
     * @param id element id
     * @return Diff of parts right and left of element with given id.
     */
    public String getDiffOfElementById(String id) {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")))) {

            if (id == null || id.isEmpty()) //Checking id validity
                return "Missing mandatory field id or id is empty.";

            GetRequest getRequest = new GetRequest("test", id);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            JsonObject response = gson.fromJson(getResponse.getSourceAsString(), JsonObject.class);

            if (Objects.isNull(response))
                return "There is no data stored with that id.";

            Map right = gson.fromJson(response.get("right"), Map.class);
            Map left = gson.fromJson(response.get("left"), Map.class);

            //Checking if we have everything in place to perform the diff

            if (Objects.isNull(right))
                return "Missing right input for id " + id;
            if (Objects.isNull(left))
                return "Missing left input for id " + id;


            //Analyzing diff and returning
            if (right.equals(left)) return "Inputs are equal.";
            else if (right.size() != left.size()) return "Input sizes are different";
            else return "Differences found: " + Maps.difference(right, left).toString();

        } catch (IOException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

}