import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;



public class tweetParser {
	public String parseLocation(String str){
		// "in" and "by" might be used for something other than a location
		String location_hints[] = {
			"near me",
			"near",
			"nearby",
			"around",
			"close to",
			"by",
			"in"
		};
		for(String hint: location_hints){
			if(str.indexOf(hint)>0){
				return str.substring(str.indexOf(hint)+hint.length());
			}
		}
		return "";
	}
	
	public String parseItemInfo(String str, MongoCollection<Document> collection){
		// remove location hints and subsequent text
		String location_hints[] = {
				"near me",
				"near",
				"nearby",
				"around",
				"close to",
				"by",
				"in"
			};
			for(String hint: location_hints){
				if(str.indexOf(hint)>0){
					str = str.substring(0, str.indexOf(hint));
					break;
				}
			}
	        // TODO - remove common / unnecessary words 
			
			// Check for type matches
			 MongoCursor<Document> cursor = null;
			 cursor = collection.find(new Document("$text", new Document("$search", str)
			 .append("$caseSensitive", false)
			 .append("$diacriticSensitive", false))).iterator();
			 
			 String item_info = "";
			 while (cursor.hasNext()) {
	                Document item_type = cursor.next();
	                item_info = item_info + item_type.getString("subject") + " ";
	            }
			 return item_info;
		
	}
}
