package cloud.cave.extension;

import cloud.cave.broker.Marshaling;
import cloud.cave.server.common.Command;
import cloud.cave.service.CaveStorage;
import org.json.simple.JSONObject;

/**
 * Created by kgoyo on 21-10-2016.
 */
public class CountRoomCommand extends AbstractCommand implements Command {

    @Override
    public JSONObject execute(String... parameters) {
        CaveStorage storage = objectManager.getCaveStorage();
        int n = storage.countRooms();
        JSONObject reply =
                Marshaling.createValidReplyWithReturnValue("true",
                        "There are "+ n +" rooms in the cave.");
        return reply;
    }
}
