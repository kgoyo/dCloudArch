package cloud.cave.service;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by amao on 9/22/16.
 */
public class TestMongoDBStorage {

    MongoCollection<Document> memes;

    @Before
    public void setup() {
        MongoClient mongoClient = new MongoClient("172.17.0.2", 27017);

        MongoDatabase db = mongoClient.getDatabase("help");

        memes = db.getCollection("memes");
    }

    @Ignore
    @Test
    public void shouldQueryCollection(){
        Document dankMeme = new Document()
                .append("name", "Navy Seal");

        FindIterable<Document> iterable = memes.find(dankMeme);

        JSONParser parser = new JSONParser();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                String json = JSON.serialize(document);
                System.out.println(json);
            }
        });
    }
}
