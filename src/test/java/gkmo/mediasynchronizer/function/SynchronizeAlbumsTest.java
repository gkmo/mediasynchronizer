package gkmo.mediasynchronizer.function;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class SynchronizeAlbumsTest {

    private static SQSEvent input;

    @BeforeClass
    public static void createInput() throws IOException {
        // TODO: set up your sample input object here.
        input = null;
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testProcessAlbum() {
        SynchronizeAlbums handler = new SynchronizeAlbums();
        Context ctx = createContext();

        Void output = handler.handleRequest(input, ctx);

        // TODO: validate output here if needed.
        Assert.assertEquals(null, output);
    }
}
