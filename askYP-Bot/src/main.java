
import java.net.UnknownHostException;
import java.util.List;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import com.mongodb.*;

public class main {
	private static final String CONSUMER_KEY = System.getenv("CONSUMER_KEY");
	private static final String CONSUMER_SECRET = System.getenv("CONSUMER_SECRET");
	private static final String ACCESS_TOKEN = System.getenv("ACCESS_TOKEN");
	private static final String ACCESS_SECRET = System.getenv("ACCESS_SECRET");

	private static final String HASHTAG = "#abc123lmnop";
	
	private static final String mongoUri = System.getenv("MONGODB_URI");
	private static final String mongoName = System.getenv("MONGO_NAME");
	private static final String mongoPass = System.getenv("MONGO_PASS");
	
    public static BasicDBObject saveTweet(Status tweet, String parsed[]){
        
        BasicDBObject tweet_obj = new BasicDBObject();
        tweet_obj.put("id", tweet.getId());
        tweet_obj.put("parsed", parsed);
        tweet_obj.put("owner", tweet.getUser().getScreenName());
        tweet_obj.put("location", "0");
        if(tweet.getGeoLocation() != null){
        	tweet_obj.put("location", tweet.getGeoLocation());
        }
       
        
        BasicDBObject tweetData = tweet_obj;
        
        return tweetData;
    }
    
	public static void main(String[] args) throws UnknownHostException{
		 MongoClientURI uri  = new MongoClientURI("mongodb://"+mongoName+":"+mongoPass+"@ds117899.mlab.com:17899/heroku_n8xvw1jh"); 
	     MongoClient client = new MongoClient(uri);
	     DB db = client.getDB(uri.getDatabase());
	        
		// Configure Twitter
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(CONSUMER_KEY);
		cb.setOAuthConsumerSecret(CONSUMER_SECRET);
		cb.setOAuthAccessToken(ACCESS_TOKEN);
		cb.setOAuthAccessTokenSecret(ACCESS_SECRET);
	  
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		Query query = new Query(HASHTAG);
		query.setCount(10);
		
		 try {
			QueryResult result = twitter.search(query);
			    
		    // MongoDB Test - TODO - does this save?
		    DBCollection tweets = db.getCollection("tweets");
		    Status first = result.getTweets().get(0);
		    String test_parsed[] = {"shoe","mens"}; 
		    tweets.insert(saveTweet(first,test_parsed));
		    
		    for (Status current : result.getTweets()) {
		      GeoLocation loc = current.getGeoLocation();
		      String userLoc = current.getUser().getLocation();
		      Place place = current.getPlace();  // Maybe can also use .getPlace() for a location
		      String user = current.getUser().getScreenName();
		      String msg = current.getText();
		      
		      /*
		      //Could be used to parse through favorites
		      List<Status> fav = twitter.getFavorites(user);
		      int counter = 1;
		      System.out.println("--------------------->LIKES<---------------------");
		      for (Status currentFav : fav) {
		        System.out.println(counter + "- fav:  " + currentFav.getText());
		        counter++;
		      }
		      System.out.println("--------------------------------------------------");
				*/
			    }

			  } catch (TwitterException e) {

			  }
		 
		 client.close();
	}
}
