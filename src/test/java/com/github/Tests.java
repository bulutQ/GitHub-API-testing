package com.github;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

}
