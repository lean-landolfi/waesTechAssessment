package waes.tech.assessment.guice;

import com.google.inject.*;
import waes.tech.assessment.controller.*;

public class Module extends AbstractModule {
    //Guice DI configuration
    @Override
    protected void configure() {
        bind(MainController.class).in(Singleton.class);
    }
}
