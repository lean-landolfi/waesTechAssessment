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

@Singleton
public class ElasticSearchService {
    private static final Logger LOG = getLogger(ElasticSearchService.class.getCanonicalName());
    private static final Gson gson = new Gson();

    JsonObject search(String term) {
        try (RestHighLevelClient client = ESProvider.getESClient()) {

            GetRequest getRequest = new GetRequest("test", term);
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

            return gson.fromJson(getResponse.getSourceAsString(), JsonObject.class);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    String save(Map<String, Object> mapToIndex, String id, String side) {
        try (RestHighLevelClient client = ESProvider.getESClient()) {
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