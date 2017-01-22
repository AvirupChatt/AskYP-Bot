import java.util.ArrayList;

import org.bson.Document;

import twitter4j.Status;
import twitter4j.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

import static com.mongodb.client.model.Filters.*;

public class DBSearch {
	public MongoClientURI uri;
	public MongoClient client;
	public MongoDatabase db;

	public DBSearch(String mongoname, String mongopass, String mongostr){
		uri  = new MongoClientURI("mongodb://"+mongoname+":"+mongopass+mongostr); 
	    client = new MongoClient(uri);
	    db = client.getDatabase(uri.getDatabase());
	     
	}
	
	
	public Document createTwitterUserObj(User twitter_usr) {
		//Check if duplicate
		MongoCollection<Document> collection = db.getCollection("twitter_users");
		collection.drop();
		Document doc = collection.find(eq("user_id",twitter_usr.getId())).first();
		if(doc!=null){
			//User Exists
			return doc;
		}
		Document user_obj = new Document();
		user_obj.put("user_id", twitter_usr.getId());
		user_obj.put("screenname", twitter_usr.getScreenName());
		user_obj.put("location", twitter_usr.getLocation());
		
		return user_obj;
	}

	public Document createMongoTweetObj(Status tweet, String parsed_iteminfo, String parsed_location) {
		//Check if duplicate
		MongoCollection<Document> collection = db.getCollection("tweets");
		collection.drop();
		Document doc = collection.find(eq("tweet_id",tweet.getId())).first();
		if(doc!=null){
			//Tweet Exists
			return doc;
		}
		Document tweet_obj = new Document();
		tweet_obj.put("tweet_id", tweet.getId());
		tweet_obj.put("parsed_iteminfo", parsed_iteminfo);
		tweet_obj.put("parsed_location", parsed_iteminfo);
		tweet_obj.put("owner", tweet.getUser().getScreenName());
		tweet_obj.put("location", "0");
		if (tweet.getGeoLocation() != null) {
			tweet_obj.put("location", tweet.getGeoLocation());
		}
		
		return tweet_obj;

	}
	
	public void updateTwitterUserwithTweet(User twitter_usr, Status tweet, String parsed_iteminfo, String parsed_location){
		Document twitter_user = createTwitterUserObj(twitter_usr);
		Document tweet_obj = createMongoTweetObj(tweet, parsed_iteminfo, parsed_location);
		
		ArrayList<String> tweet_ids = (ArrayList<String>) twitter_user.get("tweet_ids");
		if(tweet_ids.indexOf(tweet_obj.getString("_id"))>0){
			// The tweet already exists
			return;
		}
		tweet_ids.add(tweet_obj.getString("_id"));
		twitter_user.put("tweet_ids", tweet_ids);
	}
	
	
/*
 			MongoClientURI uri=new MongoClientURI("mongodb://"+mongoName+":"+mongoPass+"@ds117899.mlab.com:17899/heroku_n8xvw1jh"); 
			MongoClient client=new MongoClient(uri);
			MongoDatabase db=client.getDatabase(uri.getDatabase());
		    MongoCollection<Document> collection = db.getCollection("types");
		    collection.drop();
		    collection.createIndex(new Document("subject", "text"), new IndexOptions());

	    	String types[] = {
	        		"shoes",
	        		"shirts",
	        		"t-shirts",
	        		"tshirts",
	        		"glasses",
	        		"sunglasses",
	        		"cosmetics",
	        		"gloves",
	        		"hats",
	        		"boots"
	        	};
	        	int i = 0;
	        	for(String type : types){
	        		collection.insertOne(new Document("_id",i).append("subject", type));
	        		i++;
	        	}
	        	client.close(); 
 */
 
}
