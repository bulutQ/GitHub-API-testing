package com.github;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

public class Tests {

    @BeforeAll
    public static void setUp(){
        baseURI="https://api.github.com";
    }

    /**
     * 1.Send a get request to /orgs/:org. Request includes :
     *      •Path param org with value cucumber
     * 2.Verify status code 200, content type application/json; charset=utf-8
     * 3.Verify value of the login field is cucumber
     * 4.Verify value of the name field is cucumber
     * 5.Verify value of the id field is 320565
     */

    @Test
    @DisplayName("Verify organization information")
    public void test_01(){
        Response response = given().
                            contentType(ContentType.JSON).
                            pathParam("org", "cucumber").
                            get("/orgs/{org}");

            response.then().statusCode(200).
                    contentType("application/json; charset=utf-8").
                    assertThat().
                    body("login", is("cucumber")).
                    body("name", is("Cucumber")).
                    body("id", is(320565));
    }

    /**
     * 1.Send a get request to /orgs/:org. Request includes :
     *      •Header Accept with value application/xml
     *      •Path param org with value cucumber
     * 2.Verify status code 415, content type application/json; charset=utf-8
     * 3.Verify response status line include message Unsupported Media Type
     */
    @Test
    @DisplayName("Verify error message")
    public void test_02(){
        Response response=given().
                contentType(ContentType.JSON).
                header("Accept","application/xml").
                pathParam("org", "cucumber").
                get("/orgs/{org}");

            response.then().statusCode(415).
                    contentType("application/json; charset=utf-8").
                    assertThat().
                    header("status",containsString("Unsupported Media Type"));
    }

    /**
     * Number of repositories
     * 1.Send a get request to /orgs/:org. Request includes :•Path param org with value cucumber
     * 2.Grab the value of the field public_repos
     * 3.Send a get request to /orgs/:org/repos. Request includes :•Path param org with value cucumber
     * 4.Verify that number of objects in the response  is equal to value from step 2
     */
    @Test
    @DisplayName("Limit Error")
    public void numOfRepo(){
        Response response=
                given().
                        contentType(ContentType.JSON).
                        pathParam("org","cucumber").
                        get("/orgs/{org}");
        int pub = response.jsonPath().getInt("public_repos");
        Response response1=
                get("/orgs/{org}/repos","cucumber");
        int count = response1.jsonPath().getList("").size();
        assertEquals(pub,count);
    }
    @Test
    public void repoId(){
        Response response =given().
                contentType(ContentType.JSON).
                pathParam("org","cucumber").
                get("/orgs/{org}/repos");
        //For fun
        List<Integer> id = response.jsonPath().getList("id");
        Set<Integer> uniqueId= new HashSet<>(id);
        assertEquals(id.size(),uniqueId.size());
        List<String> node = response.jsonPath().getList("node_id");
        Set<String> uniqueNod= new HashSet<>(node);
        assertEquals(node.size(),uniqueNod.size());
    }
    @Test
    public void repoOwner(){
        Response response = given().
                contentType(ContentType.JSON).
                get("/orgs/{org}","cucumber");
        int id = response.jsonPath().getInt("id");
        Response response1=
                get("/orgs/{org}/repos","cucumber");
        List<Integer> ownerId = response1.jsonPath().getList("owner.id");
        boolean check = ownerId.stream().allMatch(o->o.equals(id));
        assertTrue(check);
    }
    @Test
    public void ascFullName(){
        Response response=given().
                contentType(ContentType.JSON).queryParam("sort","full_name").
                get("/orgs/{org}/repos","cucumber");
        List<String> original = response.jsonPath().getList("name");
        assertTrue(alphabetically(original,"bigger"));
    }
    public static boolean alphabetically(List<String> original, String Luffy) {
        Iterator<String> monkey = original.iterator();
        String now = monkey.next(), later = monkey.next();
        while (monkey.hasNext()) {
            later = monkey.next();
            if (Luffy.equals("bigger")) {
                if (now.compareTo(later) > 0) {
                    return false;
                }
                now = later;
            } else if (Luffy.equals("smaller")) {
                if (now.compareTo(later) < 0) {
                    return false;
                }
            }
        }
        return true;
    }
    @Test
    public void descFullName(){
        Response response=given().
                contentType(ContentType.JSON).
                queryParam("sort","full_name").
                queryParam("direction","desc").
                get("/orgs/{org}/repos","cucumber");
        List<String> original = response.jsonPath().getList("name");
        assertTrue(alphabetically(original,"smaller"));
    }
    @Test
    public void defaultSort(){
        Response response = given().contentType(ContentType.JSON).
                get("/orgs/{org}/repos","cucumber");
        List<String> dates = response.jsonPath().getList("created_at");
        // List<Date> dates = response.jsonPath().getList("created_at");
        // SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean check = false;
        for (int i = 0; i <dates.size()-1 ; i++) {
            int diff = dates.get(i).compareTo(dates.get(i+1));
            if (diff < 0) {
                check = true;
                break;
            }
        }
        assertTrue(check);
    }

}
