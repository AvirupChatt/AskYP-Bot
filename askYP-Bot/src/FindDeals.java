import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class FindDeals {

  private final static String YPAPI = "http://dcr.yp.ca/api/search/latest?";
  private double LONGITUDE;
  private double LATITUDE;
  private int RADIUS = 10;
  private String KEYWORDS;
  private String MERCHANT;
  private String URL;
  private boolean dealFound=false;
  private String currentDeal = "";
  private String LOCATION;
  public FindDeals() {

  }

  public void Deal() throws Exception {

    try {
      Gson gson = new Gson();
      
      Double total = 0.0;
      
      do {
        URL url = new URL(YPAPI+"longitude="+LONGITUDE+"&latitude="+LATITUDE+"&radius="+RADIUS+"&keyword="+KEYWORDS);

        Scanner scanner = new Scanner(url.openStream());
        String response = scanner.useDelimiter("\\Z").next();

        LinkedTreeMap json_map = gson.fromJson(response , LinkedTreeMap.class);      

        LinkedTreeMap pager = (LinkedTreeMap) json_map.get("pager");
        total =  (Double) pager.get("total");

        if(RADIUS > 40 && total == 0) {
          total = -1.0;
        } else if(total == 0) {
          RADIUS = RADIUS*2;
        }
        
        if(total != -1.0 || total != 0) {
          LinkedTreeMap result = (LinkedTreeMap) json_map.get("result");
          LinkedTreeMap r_translation = (LinkedTreeMap) result.get("Translation");
          LinkedTreeMap r_en = (LinkedTreeMap) r_translation.get("en");
          URL =  (String) r_en.get("url");

          LinkedTreeMap merchant = (LinkedTreeMap) json_map.get("Merchant");
          LinkedTreeMap m_translation = (LinkedTreeMap) merchant.get("Translation");
          LinkedTreeMap m_en = (LinkedTreeMap) m_translation.get("en");
          MERCHANT = (String) m_en.get("name");
          
          String deal = MERCHANT+" has a good deals that you should check out: "+URL;
          scanner.close();
          dealFound = true;
          currentDeal = deal;
          
        } else {
          scanner.close();
          dealFound = false;
        }
        

      }while(total == 0.0);

        

    } catch (IOException e) {
    }
    
  }
  
  public boolean dealFound(){
    return dealFound;
  }
  
  public String getDeal(){
    return currentDeal;
  }

  public void setUserInfo(double longitude, double latitude, String keywords) {
    LONGITUDE = longitude;
    LATITUDE = latitude;
    if(keywords.length() > 0){
    	KEYWORDS = keywords;
    	return;
    }
    keywords = "deals";
  }
  
  public void setUserInfoStringLoc(String location, String keywords){
	  KEYWORDS = keywords;
	  LOCATION = location;
  }

}
