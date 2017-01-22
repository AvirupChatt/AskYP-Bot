import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.bson.Document;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class main {
	private static final String CONSUMER_KEY = System.getenv("CONSUMER_KEY");
	private static final String CONSUMER_SECRET = System
			.getenv("CONSUMER_SECRET");
	private static final String ACCESS_TOKEN = System.getenv("ACCESS_TOKEN");
	private static final String ACCESS_SECRET = System.getenv("ACCESS_SECRET");

	private static final String HASHTAG = "#askYP";

	private static final String mongoUri = System.getenv("MONGODB_URI");
	private static final String mongoName = System.getenv("MONGO_NAME");
	private static final String mongoPass = System.getenv("MONGO_PASS");
	private static final String mongoStr = System.getenv("MONGO_STR");

	private static ConfigurationBuilder cb;
	private static Twitter twitter;
	private static DBSearch dbsearch;

	public static boolean postTweetReplyLocationReq(long postId, String usrname) {
		try {
			StatusUpdate stat = new StatusUpdate(
					"Hey @"
							+ usrname
							+ " which city are you looking to buy the product in? Reply to this tweet with your city to let me know!");
			stat.setInReplyToStatusId(postId);
			twitter.updateStatus(stat);
			return true;
		} catch (TwitterException e) {
			return false;
		}
	}

	public static boolean postTweetReply(long postId, String usrname,
			String location, String keywords) throws IOException {
		try {

			FindDeals newDeals = new FindDeals();
			//googLocation gloc = new googLocation();
			//gloc.setCity(location);
			
			newDeals.setUserInfo(73.56725599999999,45.5016889, keywords);
			StatusUpdate stat;

			if (newDeals.dealFound()) {
				stat = new StatusUpdate("Hey @" + usrname + ", "
						+ newDeals.getDeal());
				//stat.setInReplyToStatusId(postId);
				twitter.updateStatus(stat);
			} else {
				// do something else
			}

			return true;
		} catch (TwitterException e) {
			return false;
		}
	}

	public static ArrayList<Status> getDiscussion(Status status, Twitter twitter) {
		ArrayList<Status> replies = new ArrayList<>();

		ArrayList<Status> all = null;

		try {
			long id = status.getId();
			String screenname = status.getUser().getScreenName();

			Query query = new Query("@" + screenname + " since_id:" + id);

			System.out.println("query string: " + query.getQuery());

			try {
				query.setCount(20);
			} catch (Throwable e) {
				// enlarge buffer error?
				query.setCount(30);
			}

			QueryResult result = twitter.search(query);
			System.out.println("result: " + result.getTweets().size());

			all = new ArrayList<Status>();

			do {
				System.out.println("do loop repetition");

				List<Status> tweets = result.getTweets();

				for (Status tweet : tweets)
					if (tweet.getInReplyToStatusId() == id)
						all.add(tweet);

				if (all.size() > 0) {
					for (int i = all.size() - 1; i >= 0; i--)
						replies.add(all.get(i));
					all.clear();
				}

				query = result.nextQuery();

				if (query != null)
					result = twitter.search(query);

			} while (query != null);

		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return replies;
	}

	private static long SINCE_ID = 0;

	public static void main(String[] args) throws IOException, InterruptedException {
		// String brands =
		// "C._Howard_Hunt Canson Conté Copic Daler-Rowney Daniel_Smith_art_materials Derwent_Cumberland_Pencil_Company Esterbrook Faber-Castell Joseph_Gillotts_pens Golden_Artist_Colors Grumbacher Koh-i-Noor_Hardtmuth Krink Kuretake_art_products Kyukyodo Liquid_Assets_Paint__Pigment_Company Liquitex Magna_paint Navarino_Icons Olfa Pantone_Tria_markers Pentel Perry__Co. Prismacolor Royal_Talens Sakura_Color_Products_Corporation Sennelier Speedball_art_products Staedtler Tombow Triart_Design_Marker Utrecht_Art_Supplies Viarco Winsor__Newton X-Acto 76_gas_station Agip Amoco Ampol Anglo_Saxon_Petroleum Aral_AG ARCO Argos_Energies Ashland_Inc. Associated_Oil_Company Azpetrol BP Bunker_Oil_company BWOC Caltex Caltex_Woolworths Chevron_Corporation Citgo Clark_Brands Coles_Express Conoco Cosmo_Oil_Company Diamond_Shamrock Dragon_Petroleum Dry_gas Ecopetrol Edoardo_Raffinerie_Garrone Enco_brand Eni Esso Europa_oil_company Exxon ExxonMobil Fifth_Wheel_Truck_Stops FJ_Management Frontier_Oil Galp_Energia Getty_Oil Greenergy Grupa_Lotos Gulf_Oil Humble_Oil Husky_Energy Hyundai_Oilbank Idemitsu_Kosan Imperial_Oil Ingo_brand Irving_Oil Jet_brand JX_Holdings Kuwait_Petroleum_Corporation Lukoil Marathon_Petroleum Mobil Murco_Petroleum Murphy_Oil Neste Nippon_Oil NOCO_Energy_Corporation OLCO_Petroleum_Group Pakistan_State_Oil Pemex Pennzoil Petro-Canada Petrobras Petrofina Petrogas Petrol_Ofisi Petronas Petronor Phillips_66 Phillips_Petroleum_Company Pioneer_Energy PKN_Orlen ProJET PTT_Public_Company_Limited Repsol Richfield_Oil_Corporation RKA_Petroleum_Companies Royal_Farms S-Oil Oneida_Indian_Nation Seaoil_Philippines Sheetz Royal_Dutch_Shell Shell_Oil_Company Showa_Shell_Sekiyu Shell_Canada Shell_Pakistan Shell_V-Power Sinclair_Oil_Corporation SOCAR Sohio Speedway_LLC Spirit_Petroleum St1 Standard_Oil_of_Ohio Statoil_Fuel__Retail Sunoco Supertest_Petroleum Tamoil Terpel Texaco Total_S.A. Town_Pump TravelCenters_of_America Ultramar United_Petroleum Valero_Energy Wesco_oil_company YX_Energi Banana_Flavored_Milk Banania Barleycup Baron_von_Lemon Beverage_Partners_Worldwide Boga_soft_drink Bournvita Bovril Caro_beverage Chatime Clamato Clipper_tea Club-Mate Cravendale Crystal_Light Double_Seven_soft_drink Energen_cereal_drink Frugo General_Foods_International Gold_Spot Goombay_Punch Höpt Horlicks Jūrokucha Kalleh_company Kinohimitsu Manhattan_Special Mercy_drink Milo_drink MiO Mr_Shericks_Shakes Nestlé Nexcite Nutrimato Ovaltine PDQ_Chocolate PJs_Smoothies Postum Pysio Sinalco Sjora Smartboost Vitamalt Welchs Wylers Zarex_drink 20_Mule_Team_Borax 2000_Flushes Air_Wick Ariel_detergent Bounty_brand Brasso Calgon Cheer_brand Chore_Boy Cillit_Bang Comfort_fabric_softener Dawn_brand Downy Fels-Naptha Frosch_USA Glade_brand Glass_Plus Gold_Dust_washing_powder Harpic Joy_dishwashing_liquid Lysol Mr_Muscle Mr_Sheen Mr._Clean Palmolive_brand Pledge_brand Purex_laundry_detergent Renuzit Rinso Scrubbing_Bubbles Suavitel Swiffer Tide_brand Ty-D-Bol Vanish_brand Vileda Windex Woolite The_Arthur_Pequegnat_Clock_Company Atmos_clock Audichron Braun_company Chelsea_Clock_Company Elektronika_7 Florn Geochron Hammond_Clock_Company Junghans Metamec Mora_clock Ridgeway_Clocks Self_Winding_Clock_Company Seth_Thomas_Clock_Company Standard_Electric_Time_Company Stjärnsunds_manufakturverk Telechron Telefunken Waltham_Aircraft_Clock_Corporation Westclox Western_Union Clothing_brands_by_country Clothing_brands_by_type Abercrombie__Fitch_brands Fast_Retailing Gap_brands Inditex_brands Khaadi Pakistani_fashion_labels Perry_Ellis_International_brands Fashion_company_stubs A.F.C.A_clothing A.P.C. Acne_Studios Acqua_Limone Adika AKOO Alain_Figaret AllSaints American_Eagle_Outfitters Anne_Fontaine_brand Antthony_Mark_Hankins Arckiv Armoire_Officielle Arthur_Galan_AG Ascot_Chang Bench_Philippine_clothing_brand Bestseller_company Biba_Apparels Bigotti Bivolino Blaze_of_Sweden Bllack_Noir Bloch_company Bluenotes Bonia_fashion Bosideng Boxfresh BP_Studio Callisti CA Canterbury_of_New_Zealand Caraceni Carbrini_Sportswear Cassidi Castro_clothing Céline_brand Le_Château China_Heilan_Group Cockpit_USA Comptoir_des_Cotonniers Corneliani Costume_National Countess_Mara Croc_O_Shirt CuteCircuit Dale_of_Norway Damani_Dada Darling_London Denver_Hayes Desigual Diesel_brand Disco_Ruined_My_Life Dolfin_Swimwear Ken_Done Dorinha_Jeans_Wear Duchamp_clothing Duvelleroy Ede__Ravenscroft EDUN Embark_Fashion_Brand English_Eccentrics Escada Esprit_clothing Ethan_James_clothing Ethika Extè Fabletics Fashion_line Fenchurch_clothing Fendi Filippa_K Firetrap Forever_Lazy Fox_clothing French_Connection_clothing G-Star_Raw G2000 Gant_retailer Garage_clothing_retailer Garanimals Gebrüder_Stitch Genny Giordano_store Go_International Golf_Wang Grishko Gunhild_clothing Gunne_Sax HM Han_Kjøbenhavn Harari_clothing Hatley_brand Haus_Alkire Heilan_Home Helmut_Lang_fashion_brand Hervé_Leger Hield Anne_T._Hill Honigman HTnaturals Indigo_palms International_Sports_Clothing Iron_Heart_brand ISKO_clothing_company Izod_Lacoste J-Wear Jako Jean_Machine Jenny_Hellström Joe_Fresh Joseph_fashion_brand Joykeep_Jeans JustFab Karl_Kani Karma_clothing Khaadi Groupe_Zannier Takeo_Kikuchi André_Kim Elaine_Kim_fashion_designer King_Apparel Kiton Kookai Koton_company Lalpina La_Bonneterie_Cevenole La_tennis_Bensimon Lanidor Larusmiani LC_Waikiki Le_Mont_Saint_Michel_Clothing Leonisa LeSportsac Levi_Strauss__Co. J.Lindeberg LittleBig Loro_Piana Louis_Philippe_brand Lover_clothing Loyandford Luigi_Borrelli LUX_FIX Lyle__Scott Madonna_fashion_brands Mallzee Mandarina_Duck Mango_clothing Omar_Mansoor Marc_OPolo Marimekko Marina_Rinaldi Marithé_et_François_Girbaud La_Martina Mavi_Jeans Max_Mara Max_Studio MCS_fashion_brand Merc_Clothing Mexx Missoni Moods_of_Norway Morgan_clothing Morphsuits Moschino Mudd_Jeans Nakkna Noir_fashion Noko_Jeans Norse_Projects Nudie_Jeans OBEY_clothing OnePiece Ong_Shunmugam Ooji Orca_wetsuits_and_sports_apparel Cesare_Paciotti Pal_Zileri Carlo_Palazzi Paule_Ka Pendleton_Woolen_Mills Pepe_Jeans Police_brand Polly_Flinders Project_D Real_Gold Reflect-please Rêve_En_Vert Nina_Ricci_brand Rip_Curl Rosasen Rufskin S.E.H_Kelly SABA_clothing Sakis_Rouvas_Collection Salvatore_Ferragamo_S.p.A. Ferdinando_Sarmi Elsa_Schiaparelli Scotch_and_Soda_clothing Sealup Shiatzy_Chen Amstrad Casio_brands Hitachi Lenovo_laptops Philips RCA_brands Samsung_Electronics Siemens Sony Toshiba_brands Aigo Aiptek_Inc. Akai Alcatel_mobile_device_brand Archos Archos_AV_series Arise_India AstellKern Astraltune Azolt Ben_NanoNote BenQ BenQ-Siemens Breffo BrightHouse_retailer Bush_brand Celkon Clarion_company Cowon Creative_MuVo Creative_NOMAD Curtis_Mathes_Corporation Daewoo DataWind Dawlance Dell Dell_subsidiary Dell_Technologies Denon Discman DTVPal Dual_brand Emerson_Radio Funai Gigabeat Goji_Electronics Haier Harman_Kardon HEOS_by_Denon Hisense Hitachi HP_Pavilion_computer Humax IAUDIO IdeaPad_Y_Series Dell_Inspiron Intex_Technologies Invoxia IQAir Iriver Jablotron JVC Kenwood_Corporation Kyoto_Electronics Lanix LG_Electronics Logik_brand Lumigon LYF M-Pio_Co. Magnavox Magnetophon Marantz Matsui_brand Medion Meebox Meizu Mi-Fone Micromax_Informatics Milbert_Amplifiers Motorola_Mobility National_brand Olympus_m:robe Onida_Electronics Optoma_Corporation Orion_Electric Packard_Bell_1926 Panasonic Panasonic_brand PEL_Pakistan Philco Philips Philips_GoGear Pioneer_Corporation Powermat_Technologies Prinz_brand Prinztronic ProCare ProLine_company Purism,_inc. RCA_Lyra Rio_digital_audio_players SaeHan_Information_Systems Saisho Samsung_Electronics Samsung_Telecommunications SanDisk_Sansa Sandstrøm_brand Sanyo Schneider_Rundfunkwerke_AG Seiki_Digital Sharp_Corporation Sherwood_company Shure Siemens Siemens_Mobile Skulpt SMS_Audio Sony Sony_Mobile Syntax-Brillian_Corporation System76 TCL_Corporation TEAC_Corporation Technics_brand ThinkPad ThinkPad_W_Series Toshiba TVonics United_States_Television_Manufacturing_Corp. Verzo Vizio Vsun Walkman Westinghouse_Digital WOOx_Technology Xohm Xtreamer Zenith_Electronics Zonda_Telecom Zune Zvue Zync_Global 17_Cosmetics Albion_Co.,_Ltd. Almay Avon_Products Bain_de_Soleil Bajaj_Corp Balmshell Bayankala_skincare Beautycounter Biotherm Boots_UK O_Boticário Bobbi_Brown Burts_Bees Cativa_Natureza Chanel Clarins Clinique Coppertone_sunscreen CoverGirl Crème_Simon Cristtee Daigaku_Honyaku_Center Elizabeth_Arden,_Inc. Etude_House Eyes_Lips_Face Fabergé_cosmetics Forever_Living_Products Frais_Luxury_Products Fullips Hermès Imedeen IsaDora_cosmetics Jo_Malone_London Kiehls Lakmé_Cosmetics Lancôme Laura_Mercier_Cosmetics Lubriderm MAC_Cosmetics Madara_Cosmetics Mandom NARS_Cosmetics Natura Natural_Wonder_Revlon_subsidiary_brand Neutrogena No._7_brand OPI_Products Origins_cosmetics Parachute_brand Princess_Pat_brand Rimmel Sa_Sa_International_Holdings Schwan-Stabilo Sea_Breeze_brand Shiseido SK-II Space.NK St._Tropez_self-tan_brand Stratton_company Surya_Brasil Tarte_Cosmetics Tropic_Skin_Care Ultima_II_cosmetics_line Urban_Decay_cosmetics Yves_Saint_Laurent_brand Bamboozz Bausch__Lomb +Beryll Carrera_Sunglasses Cazal_Eyewear Chanel Cutler_and_Gross Alexander_Daas Flexon Foster_Grant General_Eyewear Gold__Wood Gunnar_Optiks The_Hundreds Ic!_berlin Italia_Independent Kaenon_Polarized L.G.R Luxottica Marchon_Eyewear Maui_Jim Moscot Mykita Nike_Vision NYS_Collection Oakley,_Inc. Oliver_Peoples Persol Polaroid_Eyewear Police_brand Randolph_Engineering Ray-Ban Revant_Optics Christian_Roth Rvs_Eyewear Savile_Row_Eyewear Scolani Serengeti_sunglasses_brand Shutter_Shades Shwood_Eyewear Silhouette_eyewear Anna_Sui Sunnies_Studios Swans_eyewear Transitions_Optical Von_Zipper Vuarnet Yves_Saint_Laurent_brand Aristoc DBApparel Fox_River_Mills Gerbe_lingerie Globe_International Gold_Toe_Brands Hanes Hanesbrands Jockey_International Kayser-Roth Leggs Levante_hosiery Pretty_Polly_hosiery Sock_Shop Spanx Vedette_Shapewear VienneMilano Wigwam_Mills Wolford Cooking_appliance_brands Dyson_products Electrolux_brands Gorenje Indesit_Company Maytag_brands Robert_Bosch_GmbH Sewing_machine_brands Whirlpool_Corporation_brands Admiral_electrical_appliances Bauknecht_company Bedazzler Beko Braun_company Breville BSH_Hausgeräte Bticino Candy_company Colston-Ariston Cuisinart Danby_appliances De_Dietrich_Remeha DeLonghi Dongbu_Daewoo_Electronics Dustbot Edesa Electrolux Electrolux_Ankarsrum_Assistent Eureka_company Frigidaire Fukuda_appliances Fulgor Gaggenau_Hausgeräte Gaggia Giacomini Gorenje Gree_Electric Haier Hamilton_Beach_Brands Hisense Hisense_Kelon Template:Home_appliance_brands Hotpoint IKEA ILVE_appliances Indesit_Company InSinkErator Kenmore_brand Kleenmaid Lehel_appliances LG_Electronics LG_Tromm Lofra Magister_company Malleable_Iron_Range_Company Maytag Medion Midea_Group Miele Morphy_Richards Mr._Coffee Neff_GmbH Olympic_Group John_Oster_Manufacturing_Company Osterizer OXO_brand Pars_Khazar Peerless-Premier_Appliance_Company PicaBot Proctor_Silex Robert_Bosch_GmbH Robomaxx Roomba Rowenta Russell_Hobbs Russell_Hobbs,_Inc Saeco Samsung_Electronics Scooba_brand Servis Sharp_Corporation Sisil Skyworth Smeg_appliances Snowa Speed_Queen Sub-Zero_brand Sunbeam_Australia Tappan_brand Tiger_Corporation Toastmaster_appliances Ultimate_Chopper Vestel Vestfrost West_Bend_Company West_Bend_Housewares Whirlpool_Corporation Worcester,_Bosch_Group Zanussi Zojirushi_Corporation Zyliss Hotel_chains_by_country Hotels_by_company Autograph_Collection_Hotels Boutique_resort_chains Defunct_hotel_chains Extended_stay_hotel_chains Timeshare_chains List_of_chained-brand_hotels 21c_Museum_Hotels Abu_Dhabi_National_Hotels AC_Hotels AccorHotels Ace_Hotel Adagio_hotel Aerowisata Affinia_Hotel_Collection Aitken_Spence AJ_Capital_Partners Allegro_Resorts_Corporation Aloft_Hotels Aman_Resorts Amari_Hotels_and_Resorts Americas_Best_Franchising AmericInn AMResorts APA_Group Aqua_Hotels_and_Resorts Aqueen_Hotels_and_Resorts Archipelago_International Arp-Hansen_Hotel_Group Autograph_Collection_Hotels Azimut_Hotels Baglioni_Hotels Bahia_Principe BB_Hotels Banyan_Tree_Holdings Baymont_Inn__Suites Beaches_Resorts Belmond_company Best_Value_Inn Best_Western Bloc_Hotels Boscolo_Hotels Bowman-Biltmore_Hotels Brisas_Hotels_and_Resorts Budget_Host Budget_Suites_of_America Cachet_Hotel_";

		// Configure twitter
		cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(CONSUMER_KEY);
		cb.setOAuthConsumerSecret(CONSUMER_SECRET);
		cb.setOAuthAccessToken(ACCESS_TOKEN);
		cb.setOAuthAccessTokenSecret(ACCESS_SECRET);

		// Build twitter
		twitter = new TwitterFactory(cb.build()).getInstance();

		// Get database
		

		try {
			// Query with time
			// TODO - add time
			while (true) {
				dbsearch = new DBSearch(mongoName, mongoPass, mongoStr);
				MongoCollection<Document> collectionBrands = dbsearch.db
						.getCollection("brands");
				MongoCursor<Document> resultBrands = null;
					Query query = new Query(HASHTAG);
					query.setCount(100);
					if (SINCE_ID != 0) {
						query.sinceId(SINCE_ID);
					}
					QueryResult result = twitter.search(query);
					int index = result.getTweets().size() - 1 >= 0 ? result
							.getTweets().size() - 1 : 0;
					if (result.getTweets().size() <= 0) {
						SINCE_ID = 0;
					} else {
						SINCE_ID = result.getTweets().get(index).getId();
					}
					// Get Tweets
					for (Status current : result.getTweets()) {

						tweetParser tp = new tweetParser();
						User user = current.getUser();

						// Parse Tweet for item info
						String msg = current.getText();
						String item_info = tweetParser.removeStopWords(msg);
						String parsed_iteminfo = tp.parseItemInfo(item_info,
								dbsearch.db.getCollection("types"));
						// TODO - compare item_info and parsed_iteminfo
						if (parsed_iteminfo.equals(item_info)) {
							// no brands
							List<Status> fav = twitter.getFavorites(user
									.getScreenName());
							String keywords = tp.removeStopWords(msg);
							keywords = keywords.replaceAll("[^a-zA-Z\\s]", "")
									.toLowerCase();
							keywords = keywords.replaceAll(" ", "+");

							for (Status currentFav : fav) {
								String fav_msg = currentFav.getText();
								resultBrands = collectionBrands.find(
										new Document("$text", new Document(
												"$search", fav_msg).append(
												"$caseSensitive", false)
												.append("$diacriticSensitive",
														false))).iterator();

								if (resultBrands.hasNext()) {
									String brand = resultBrands.next().toJson();
									JsonObject gson = new Gson().fromJson(
											brand, JsonObject.class);
									brand = gson.get("subject").getAsString();
									System.out.println("FOUND: " + brand);
									keywords = keywords + "+" + brand;
								} else {
								}
							}
						}
						// Parse Tweet for location info
						String location_info = tp.parseLocation(item_info);

						// check mongo db to see if user has stored location
						String usr_loc = dbsearch.getTwitterUserLocation(user);
						if (usr_loc.length() <= 0) {
							// no stored location so set as twitter user
							// location
							usr_loc = user.getLocation();
						}

						if (location_info.length() > 0) {
							postTweetReply(current.getId(),
									user.getScreenName(), location_info,
									item_info);
						} else if (usr_loc.length() > 0) {
							// database did not have location but user's twitter
							// did
							// post tweet reply
							postTweetReply(current.getId(),
									user.getScreenName(), usr_loc, item_info);

							// update user location
							dbsearch.createTwitterUserObj(user,
									user.getLocation());
						} else {
							// no stored location
							Status tweet_ob = twitter.showStatus(current
									.getId());
							// get replies
							ArrayList<Status> postreplies = getDiscussion(
									tweet_ob, twitter);
							boolean replied = false;
							for (Status treply : postreplies) {
								if (treply.getUser().getScreenName()
										.equals("yp_api_test")) {
									replied = true;
									break;
								}

							}
							if(!replied){
								// respond with need for location
								postTweetReplyLocationReq(current.getId(),
										user.getScreenName());

								// save tweet and update user
								dbsearch.updateTwitterUserwithTweet(user,
										current, item_info);
							}
						}

					}
					// check saved tweets
					MongoCursor<Document> cursor = dbsearch.getSavedTweets();
					while (cursor.hasNext()) {
						Document saved_tweet = cursor.next();
						Status tweet_ob = twitter.showStatus(saved_tweet.getLong("tweet_id"));
						// get replies
						ArrayList<Status> replies = getDiscussion(tweet_ob,
								twitter);
						for (Status stat : replies) {
							if (stat.getUser().getScreenName()
									.equals(tweet_ob.getUser().getScreenName())) {
								// reply from user

								// update user location
								dbsearch.createTwitterUserObj(stat.getUser(),
										stat.getText());

								// post reply
								boolean posted = postTweetReply(stat.getId(), stat.getUser()
										.getScreenName(), stat.getText(),
										(String) saved_tweet.get("parsed_iteminfo"));

								if(posted){
									System.out.println("**DELETED**");
									dbsearch.deleteTweet(saved_tweet,
										stat.getUser());
								}
							}
						}
					}
					//dbsearch.closedb();
				Thread.sleep(1200000);
			}

		} catch (TwitterException e) {
			System.out.println(e);
		}

	}
}
