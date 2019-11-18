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
     * Gets elastic search high level client. Checks on environment variable "env" to check which connection to use.
     *
     * @return the elastic search high level client
     */
    public static RestHighLevelClient getESClient() {
        if (!System.getenv("env").equalsIgnoreCase("test")) {
            return new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("localhost", 9200, "http")));
        } else return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(System.getProperty("wta.elasticsearch.host"),
                                Integer.parseInt(System.getProperty("wta.elasticsearch.port")),
                                "http")));
    }
}
