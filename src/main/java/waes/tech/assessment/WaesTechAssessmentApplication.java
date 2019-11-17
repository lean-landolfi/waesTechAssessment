package waes.tech.assessment;

import com.google.inject.*;
import waes.tech.assessment.controller.*;
import waes.tech.assessment.guice.Module;

public class WaesTechAssessmentApplication {
    public static void main(String[] args) {


        Guice.createInjector(new Module() {
        })
                .getInstance(MainController.class)
                .run();
    }
}
