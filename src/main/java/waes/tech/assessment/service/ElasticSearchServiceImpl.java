package waes.tech.assessment.service;

import com.google.gson.*;
import com.google.inject.*;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.*;
import org.elasticsearch.action.support.replication.*;
import org.elasticsearch.action.update.*;
import org.elasticsearch.client.*;
import org.slf4j.*;
import waes.tech.assessment.utils.*;

import java.io.*;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static org.slf4j.LoggerFactory.*;

/**
 * Elastic Search service that performs requested operations.
 */
@Singleton
public class ElasticSearchServiceImpl implements ElasticSearchService {
    private static final Logger LOG = getLogger(ElasticSearchServiceImpl.class.getCanonicalName());
    private static final Gson gson = new Gson();

    private static final String INDEX = "wta-index";

    /**
     * ID based search that returns the document as JsonObject.
     *
     * @param id The id to look for.
     * @return The document stored in ES containing the decoded base64 data.
     */
    @Override
    public JsonObject search(String id) {
        try (RestHighLevelClient client = ESProvider.getESClient()) {

            GetRequest getRequest = new GetRequest(INDEX, id);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            return gson.fromJson(getResponse.getSourceAsString(), JsonObject.class);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    /**
     * Upserts document in ES and returns a string informing the upsert result.
     *
     * @param mapToIndex The document to index.
     * @param id         Id of the document.
     * @param side       Side of element. Can be left or right.
     * @return A string that has information regarding the status of the upsert.
     */
    @Override
    public String save(Map<String, Object> mapToIndex, String id, String side) {
        try (RestHighLevelClient client = ESProvider.getESClient()) {
            //Create an upsert statement to ensure that we attempt to create one element in ElasticSearch only if it doesn't exist or we update it if it does
            IndexRequest insertRequest = new IndexRequest(INDEX).id(id).source(jsonBuilder()
                    .startObject()
                    .field(side, mapToIndex)
                    .endObject());
            UpdateRequest updateRequest = new UpdateRequest().index(INDEX).id(id).doc(jsonBuilder()
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
                        LOG.info("Error was: {}", failure.reason()) //Printing every failure reason
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
}