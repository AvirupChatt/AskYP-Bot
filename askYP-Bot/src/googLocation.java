import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class googLocation {
  
  private static final String AUTH_KEY =  "AIzaSyBQgc5AX9fc2UYApA_LfKpXSKaPe_fgLdw";
  private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
  private String CITY;
      
  public googLocation(){
    
  }
  
  public void setCity(String city) {
    CITY = city;    
  }
  
  public Double getLat() throws IOException{
    Gson gson = new Gson();
    
    URL url = new URL(URL+CITY+"&key="+AUTH_KEY);

    Scanner scanner = new Scanner(url.openStream());
    String response = scanner.useDelimiter("\\Z").next();

    LinkedTreeMap json_map = gson.fromJson(response , LinkedTreeMap.class);      

    LinkedTreeMap result = (LinkedTreeMap) json_map.get("result");
    LinkedTreeMap geometry = (LinkedTreeMap) result.get("geometry");
    LinkedTreeMap location = (LinkedTreeMap) geometry.get("location");
    Double lat =  (Double) location.get("lat");
    return lat;
  }

  public Double getLon() throws IOException{
    Gson gson = new Gson();
    
    URL url = new URL(URL+CITY+"&key="+AUTH_KEY);

    Scanner scanner = new Scanner(url.openStream());
    String response = scanner.useDelimiter("\\Z").next();

    LinkedTreeMap json_map = gson.fromJson(response , LinkedTreeMap.class);      

    LinkedTreeMap result = (LinkedTreeMap) json_map.get("result");
    LinkedTreeMap geometry = (LinkedTreeMap) result.get("geometry");
    LinkedTreeMap location = (LinkedTreeMap) geometry.get("location");
    Double lon =  (Double) location.get("lng");
    return lon;
  }
  
}