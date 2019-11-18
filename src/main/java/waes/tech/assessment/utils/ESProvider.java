package waes.tech.assessment.utils;

import org.apache.http.*;
import org.elasticsearch.client.*;

/**
 * Elastic Search high level client provider.
 */
public class ESProvider {

    private ESProvider() {
    }

    /**
     * Gets elastic search high level client.
     *
     * @return the elastic search high level client
     */
    public static RestHighLevelClient getESClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
    }
}
