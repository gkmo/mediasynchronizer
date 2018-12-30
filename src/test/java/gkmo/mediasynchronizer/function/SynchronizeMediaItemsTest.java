package gkmo.mediasynchronizer.function;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class SynchronizeMediaItemsTest {

    private SQSEvent input;

    @Before
    public void createInput() throws IOException {
        // TODO: set up your sample input object here.
        input = TestUtils.parse("/sns-event.json", SQSEvent.class);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testSynchronizeMediaItems() {
        SynchronizeMediaItems handler = new SynchronizeMediaItems();
        Context ctx = createContext();

        Void output = handler.handleRequest(input, ctx);

        // TODO: validate output here if needed.
        Assert.assertEquals("Hello from SNS!", output);
    }
}
