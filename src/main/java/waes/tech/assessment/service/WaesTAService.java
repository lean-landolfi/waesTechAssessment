package waes.tech.assessment.service;

import com.google.gson.*;
import com.google.inject.*;

@ImplementedBy(WaesTAServiceImpl.class)
public interface WaesTAService {
    String saveElement(JsonObject elementPart, String id, String side);

    String getDiffOfElementById(String id);
}
