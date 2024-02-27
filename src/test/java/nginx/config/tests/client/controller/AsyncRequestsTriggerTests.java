package nginx.config.tests.client.controller;

import nginx.config.tests.client.controller.AsyncRequestsTrigger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AsyncRequestsTriggerTests {

    @Autowired
    AsyncRequestsTrigger asyncRequestsTrigger;

    @Test
    public void triggerTests() throws InterruptedException {

        asyncRequestsTrigger.trigger();
    }

}
