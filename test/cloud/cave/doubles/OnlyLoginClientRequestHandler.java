package cloud.cave.doubles;

import cloud.cave.broker.*;
import cloud.cave.server.common.ServerConfiguration;
import org.json.simple.JSONObject;

/**
 * Created by kgoyo on 22-09-2016.
 */
public class OnlyLoginClientRequestHandler implements ClientRequestHandler {


    private Invoker i;
    private boolean running;

    public OnlyLoginClientRequestHandler(Invoker i) {
        this.i = i;
        running = true;
    }

    @Override
    public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) throws CaveIPCException {
        JSONObject reply;
        if (running) {
            reply = i.handleRequest(requestJson);
        } else {
            reply = Marshaling.createInvalidReplyWithExplanation(StatusCode.SERVER_FAILURE,"can't connect to server");
        }
        return reply;
    }

    @Override
    public void initialize(ServerConfiguration config) {
        //do nothing
    }

    public void kill() {
        running = false;
    }
}
