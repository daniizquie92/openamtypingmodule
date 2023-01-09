package org.typing.forgerock.auth;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URI;
import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;

import java.util.Base64;

public class TypingDNAapi {
    private String api_key;
    private String api_secret;

    private String api_url = "https://api.typingdna.com/";

    public TypingDNAapi(String api_key, String api_secret){
        this.api_key = api_key;
        this.api_secret = api_secret;
    }

    public JSONObject getUser(String id){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash =messageDigest.digest(id.getBytes("UTF-8"));
            id = DatatypeConverter.printHexBinary(hash);
        }catch(Exception ex){}
        String authString = auth();
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(URI.create(this.api_url + "user/" + id));
            httpget.setHeader("Accept", "application/json");
            httpget.setHeader("Content-type", "application/json");
            httpget.setHeader("Authorization", authString.toString());

            ResponseHandler<String> responseHandler = response -> {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            JSONObject jsonRespuesta = (JSONObject) JSONValue.parse(responseBody);
            System.out.println(jsonRespuesta.get("status"));
            System.out.println(jsonRespuesta);
            return jsonRespuesta;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public JSONObject getAuto(String id, String tp){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash =messageDigest.digest(id.getBytes("UTF-8"));
            id = DatatypeConverter.printHexBinary(hash);
        }catch(Exception ex){}

        String authString = auth();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(URI.create(this.api_url + "auto/" + id));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", authString.toString());
            String json = "{\r\n" +
                    "  \"tp\": \"" + tp + "\"\r\n" +
                    "}";
            StringEntity stringEntity = new StringEntity(json, "UTF-8");
            httpPost.setEntity(stringEntity);

            //System.out.println("Ejecutando la peticion " + httpPost.getRequestLine());

            ResponseHandler<String> responseHandler = response -> {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            };

            String responseBody = httpclient.execute(httpPost, responseHandler);

            //JSONObject jsonRespuesta = new JSONObject(responseBody);
            JSONObject jsonRespuesta = (JSONObject) JSONValue.parse(responseBody);
            return jsonRespuesta;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject deleteUser(String id, String text){
        text = text.toLowerCase();
        int i, l;
        int hval = 0x721b5ad4;
        for (i = 0, l = text.length(); i < l; i++) {
            hval ^= text.charAt(i);
            hval += ((hval << 1) + (hval << 4) + (hval << 7) + (hval << 8) + (hval << 24));
        }
        long textId = Integer.toUnsignedLong(hval);

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash =messageDigest.digest(id.getBytes("UTF-8"));
            id = DatatypeConverter.printHexBinary(hash);
        }catch(Exception ex){}

        String authString = auth();
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpDelete httpdelete = new HttpDelete(URI.create(this.api_url + "user/" + id + "?type=2&textid=" + textId));
            httpdelete.setHeader("Accept", "application/json");
            httpdelete.setHeader("Content-type", "application/json");
            httpdelete.setHeader("Authorization", authString);

            ResponseHandler<String> responseHandler = response -> {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            };
            String responseBody = httpclient.execute(httpdelete, responseHandler);
            JSONObject jsonRespuesta = (JSONObject) JSONValue.parse(responseBody);
            return jsonRespuesta;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String auth(){
        StringBuilder authString = new StringBuilder();
        authString.append("Basic ");

        Base64.Encoder encoder = Base64.getEncoder();
        authString.append(encoder.encodeToString(String.format("%s:%s", api_key, api_secret).getBytes()));
        return authString.toString();
    }
}

