package waes.tech.assessment;

import com.google.inject.*;
import waes.tech.assessment.controller.*;
import waes.tech.assessment.guice.Module;

public class WaesTechAssessmentApplication {

    //Entry point of application.
    public static void main(String[] args) {

        Guice.createInjector(new Module() {
        })
                .getInstance(WaesTAController.class)
                .run();
    }
}
