package ch.hearc;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.CDL;

import java.io.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Main {

    static String DYNAMIC_TOKEN;

    public static void main(String[] args) {

        try {

            Scanner scan= new Scanner(System.in);
            System.out.println("USERNAME: ");
            String user= scan.nextLine();
            System.out.println("\nPASSWORD: ");
            String pwd = scan.nextLine();

            DYNAMIC_TOKEN = getToken(user, pwd);

            System.out.println("\nDe quel fichier voulez-vous les métadonnées (ID de l'objet) ? ");
            // 13969
            String metaDataFile= scan.nextLine();
            getMetadata(metaDataFile);

            System.out.println("\nDe quel fichier voulez-vous la pièce jointe (ID de l'objet) ?");
            String attachmentDataFile = scan.nextLine();
            getAttachement(attachmentDataFile);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getToken(String user, String pwd) throws Exception{

        StringBuilder sb = new StringBuilder();
        sb.append("BEARER ");
        HttpPost post = new HttpPost("http://157.26.82.44:2240/token");


        // add request parameter, form parameters
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        urlParameters.add(new BasicNameValuePair("username", user));
        urlParameters.add(new BasicNameValuePair("password", pwd));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            // System.out.println(EntityUtils.toString(response.getEntity()));

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));

            sb.append(json.get("access_token").toString());
        }
        return sb.toString();

    }


    public static void getMetadata(String fileID) {

        try {
            // Création de connexion
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("http://157.26.82.44:2240/api/document/").append(fileID).append("/metadata");
            URL url = new URL(stringBuilder.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // Création de la requête
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", DYNAMIC_TOKEN);
            con.connect();

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else

                // Sort le code et la réponse
                System.out.println(con.getResponseCode() + " " + con.getResponseMessage());

            // Traitement du JSON
            String inline = "";
            Scanner scanner = new Scanner(con.getInputStream());

            //Write all the JSON data into a string using a scanner
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }

            //Close the scanner
            scanner.close();

            //Using the JSON simple library parse the string into a json object
            JSONParser parse = new JSONParser();
            JSONObject data_obj = (JSONObject) parse.parse(inline);

            //Affichage de la réponse entière
            //System.out.println(data_obj.toString());

            //Affichage d'un élément du JSON
            //System.out.println(data_obj.get("ObjectID"));

            // Pretty Print
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(data_obj.toString());
            String prettyJsonString = gson.toJson(je);
            System.out.println("Voici les métadonnées : \n");
            System.out.println(prettyJsonString);


            org.json.JSONArray jsonArray = new JSONArray();
            jsonArray.put(data_obj);

            StringBuilder sb = new StringBuilder().append(fileID).append("_").append("metadata").append(".csv");

            File file = new File(sb.toString());
            String csv = CDL.toString(jsonArray);
            FileUtils.writeStringToFile(file, csv);

            // Fermeture de la connexion
            con.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public static void getAttachement(String fileID) {
        try {
            // Création de connexion
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("http://157.26.82.44:2240/api/document/").append(fileID).append("/attachment");
            URL url = new URL(stringBuilder.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // Création de la requête
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", DYNAMIC_TOKEN);
            con.connect();

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else

                // Sort le code et la réponse
                System.out.println(con.getResponseCode() + " " + con.getResponseMessage());

            // Traitement du JSON
            String inline = "";
            Scanner scanner = new Scanner(con.getInputStream());

            //Write all the JSON data into a string using a scanner
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }

            //Close the scanner
            scanner.close();

            //Using the JSON simple library parse the string into a json object
            JSONParser parse = new JSONParser();
            JSONObject data_obj = (JSONObject) parse.parse(inline);

            //Affichage de la réponse entière
            //System.out.println(data_obj.toString());

            //Affichage d'un élément du JSON
            //System.out.println(data_obj.get("ObjectID"));


            // Fermeture de la connexion
            con.disconnect();

            // PDF Reader
            StringBuilder sb = new StringBuilder().append(fileID).append("_").append("attachment").append(".pdf");
            OutputStream out = new FileOutputStream(sb.toString());
            out.write(java.util.Base64.getDecoder().decode(data_obj.get("File").toString()));
            out.close();

            System.out.println("\nLe pdf a bien été crée dans votre dossier");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
