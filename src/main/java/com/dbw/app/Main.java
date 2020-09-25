package com.dbw.app;

import com.google.inject.Guice;
import com.google.inject.Injector;

// mvn package shade:shade
// java -Xmx25m -jar ./target/dbw-1.0-SNAPSHOT.jar -c config/example.yml
// java -Xmx25m -cp lib/ojdbc7.jar:target/dbw-1.0-SNAPSHOT.jar com.dbw.app.Main -c config/orcl-example.yml

public class Main {

    public static App app;
    
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());
        app = injector.getInstance(App.class);
        app.init(args);
        app.start();
    }
}