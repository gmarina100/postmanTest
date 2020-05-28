package com.curso;

import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;

import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class ProyectoSegundamanoMarina {

    //Variables
    static String base_url = "https://webapi.segundamano.mx";
    static String email;
    static int password;
    static String accessToken;
    static String accountId;
    static String adId;

    @Test
    public void test1() {
        //Create user
        double rndNumber = Math.random() * 6 + 1;
        email = "ventas_" + rndNumber + "@ventas.net";
        password = (int) Math.random() * 99999 + 99999;
        String datos = email + ":" + password;
        String encodeString = Base64.getEncoder().encodeToString(datos.getBytes());
        String bodyRequest = "{\"account\":{\"email\":\"" + email + "\"}}";

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", base_url);
        Response response = given().header("Authorization", "Basic " + encodeString).body(bodyRequest).post();

        String body = response.getBody().asString();

        System.out.println("Response: " + body);

        accessToken = JsonPath.read(body, "$.access_token");
        System.out.println("Access token = " + accessToken);

        String accountIdValue = JsonPath.read(body, "$.account.account_id");
        accountId = accountIdValue.split("/")[3];
        System.out.println("Account ID = " + accountId);

        //Validations
        assertEquals(201, response.getStatusCode());
        assertNotNull(body);
        assertNotNull(JsonPath.read(body, "$.account.account_id"));
        assertEquals(email, JsonPath.read(body, "$.account.email"));
    }


    @Test
    public void test2() {
        //Add phone number to account created
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts/%s", base_url, accountId);

        String request = "{" +
                "    \"account\": {" +
                "        \"name\": \"" + email + "\"," +
                "        \"phone\": \"3345673428\"," +
                "        \"phone_hidden\": true" +
                "    }" +
                "}";
        Response response = given().log().all().header("Authorization", "tag:scmcoord.com,2013:api " + accessToken).body(request).patch();

        String body = response.getBody().asString();

        System.out.println("Body test:" + body);

        //Validations
        assertEquals(200, response.getStatusCode());
        assertNotNull(body);
        assertTrue(body.contains("can_publish"));
        assertEquals(true, JsonPath.read(body, "$.account.can_publish"));
    }

    @Test
    public void test3() {
        //Post and Ad
        RestAssured.baseURI = String.format("%s/nga/api/v1.7/private/accounts/%s/klfst", base_url, accountId);

        String request = "{\"ad\":{\"locations\":[{\"code\":\"16\",\"key\":\"region\",\"label\":\"Jalisco\",\"locations\":[{\"code\":\"647\",\"key\":\"municipality\",\"label\":\"Tlajomulco de Z\\u00fa\\u00f1iga\",\"locations\":[{\"code\":\"48363\",\"key\":\"area\",\"label\":\"San Agustin\"}]}]}],\"subject\":\"Juego de cucharones\",\"body\":\"Juego de cucharones grandes\",\"category\":{\"code\":\"3042\"},\"images\":[],\"price\":{\"currency\":\"MXN\",\"price_value\":200},\"ad_details\":{},\"phone_hidden\":1,\"plate\":\"\",\"vin\":\"\",\"type\":{\"code\":\"s\",\"label\":\"\"},\"ad\":\"Juego de cucharones\"},\"category_suggestion\":false,\"commit\":true}";
        Response response = given().log().all()
                .queryParam("lang", "es")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/plain, */*")
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .header("Authorization", "tag:scmcoord.com,2013:api " + accessToken)
                .body(request).post();

        String body = response.getBody().asString();

        System.out.println("Body test:" + body);

        adId = JsonPath.read(body, "$.ad.ad_id").toString().split("/ads/")[1];
        System.out.println("Ad Id " + adId);

        //Validations
        assertEquals(201, response.getStatusCode());
        assertNotNull(body);
        assertTrue(body.contains("action_type"));
        assertEquals("new", JsonPath.read(body, "$.action.action_type"));
    }

    @Test
    public void test4() {
        //Edit and Ad
        RestAssured.baseURI = String.format("%s/nga/api/v1.7/private/accounts/%s/klfst/%s/actions", base_url, accountId, adId);

        String request = "{\"ad\":{\"locations\":[{\"code\":\"16\",\"key\":\"region\",\"label\":\"Jalisco\",\"locations\":[{\"code\":\"647\",\"key\":\"municipality\",\"label\":\"Tlajomulco de Z\\u00fa\\u00f1iga\",\"locations\":[{\"code\":\"48363\",\"key\":\"area\",\"label\":\"San Agustin\"}]}]}],\"subject\":\"Juego de cucharones\",\"body\":\"Juego de cucharones grandes\",\"category\":{\"code\":\"3042\"},\"images\":[],\"price\":{\"currency\":\"MXN\",\"price_value\":200},\"ad_details\":{},\"phone_hidden\":1,\"plate\":\"\",\"vin\":\"\",\"type\":{\"code\":\"s\",\"label\":\"\"},\"ad\":\"Juego de cucharones\"},\"category_suggestion\":false,\"commit\":true}";
        Response response = given().log().all()
                .queryParam("lang", "es")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/plain, */*")
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .header("Authorization", "tag:scmcoord.com,2013:api " + accessToken)
                .body(request).post();

        String body = response.getBody().asString();

        System.out.println("Body test:" + body);

        adId = JsonPath.read(body, "$.ad.ad_id").toString().split("/ads/")[1];
        System.out.println("Ad Id " + adId);

        //Validations
        assertEquals(201, response.getStatusCode());
        assertNotNull(body);
        assertEquals(2, Integer.parseInt(JsonPath.read(body,"$.action.action_id").toString().split("/actions/")[1]));
        assertEquals("edit", JsonPath.read(body, "$.action.action_type"));

    }

    @Test
    public void test5() {
        //Delete and Ad
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s/klfst/%s", base_url, accountId, adId);

        String request = "{\"delete_reason\":{\"code\":\"5\"}}";
        Response response = given().log().all()
                .queryParam("lang", "es")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/plain, */*")
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .header("Authorization", "tag:scmcoord.com,2013:api " + accessToken)
                .body(request).delete();

        String body = response.getBody().asString();

        System.out.println("Body test:" + body);

        //Validaciones
        assertEquals(403, response.getStatusCode());
        assertNotNull(body);
        assertEquals("FORBIDDEN", JsonPath.read(body, "$.error.code"));
    }

}
