package waes.tech.assessment.service;

import com.google.gson.*;
import com.google.inject.*;

import java.util.*;

@ImplementedBy(ElasticSearchServiceImpl.class)
public interface ElasticSearchService {
    JsonObject search(String id);

    String save(Map<String, Object> mapToIndex, String id, String side);
}
