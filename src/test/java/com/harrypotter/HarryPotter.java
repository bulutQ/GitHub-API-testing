package com.harrypotter;

import com.harrypotter.pojo.House;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

public class HarryPotter {

    private final String API_KEY = "$2a$10$0C88vabfUSwPpSJek5zfSu32ez324iJUacSpQMlr8r02bwhXfXet2";

    @BeforeAll
    public static void setup() {
        baseURI = "https://www.potterapi.com/v1/";
    }

    @Test
    public void verifyHat() {
        Response response = given().contentType(ContentType.JSON).
                queryParam("apiKey", API_KEY).
                get("/sortingHat").prettyPeek();
        response.then().statusCode(200).and().contentType("application/json; charset=utf-8");
        List<String> house = Arrays.asList("Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff");
        String res = response.as(String.class);
        assertTrue(house.stream().anyMatch(h -> h.equals(res)));
    }

    @Test
    public void verifyBadKey() {
        Response response =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").
                        queryParam("key", "invalid").
                        get("/characters").prettyPeek();
        response.then().statusCode(401).
                contentType("application/json; charset=utf-8").
                statusLine(containsString("Unauthorized")).
                body("error", is("API Key Not Found"));
    }

    @Test
    public void verifyNoKey() {
        Response response =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").when().
                        get("/characters").prettyPeek();
        response.then().statusCode(409).contentType("application/json; charset=utf-8").
                statusLine(containsString("Conflict")).
                body("error", is("Must pass API key for request"));
    }

    @Test
    public void verifyNumberOfCharacters() {
        Response response =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").when().
                        queryParams("key", API_KEY).
                        get("/characters").prettyPeek();
        response.then().statusCode(200).contentType("application/json; charset=utf-8");
        List<Map<String, Object>> total = response.as(List.class);
        System.out.println(total.size());
        assertEquals(195, total.size());
    }

    @Test
    public void verifyNumberOfCharacterIdAndHouse() {
        Response response =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").when().
                        queryParams("key", API_KEY).
                        get("/characters");
        response.then().statusCode(200).contentType("application/json; charset=utf-8");

        List<Map<String, Object>> total = response.as(List.class);
        List<String> expected = Arrays.asList("Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff");
        List<String> actual = response.jsonPath().getList("house");

        for (Map<String, Object> stringObjectMap : total) {
            System.out.println(stringObjectMap);
            assertFalse(stringObjectMap.get("_id").toString().isEmpty());
            assertTrue(stringObjectMap.get("dumbledoresArmy").toString().contains("true") || stringObjectMap.get("dumbledoresArmy").toString().contains("false"));
        }
        assertTrue(!Collections.disjoint(actual, expected));
    }

    @Test
    public void verifyAllCharacterInfo() {
        Response response =
                given().
                        header("Accept","application/json").
                        queryParams("key",API_KEY).
                        when().
                        get("/characters").prettyPeek();

        response.then().
                assertThat().
                statusCode(200).
                contentType("application/json; charset=utf-8");


        List<Map<String,String>>allCharacters = response.jsonPath().getList("");

        System.out.println("allCharacters = " + allCharacters);

        List<Character>listOfCharacters = response.jsonPath().getList("",Character.class);

        int randomCharacter = new Random().nextInt(listOfCharacters.size());


        String anyName = allCharacters.get(randomCharacter).get("name");

        String anyNameFromCharacterList = Character.getName(1);

        System.out.println("anyName = " + anyName);

        Response response2 =
                given().
                        header("Accept","application/json").
                        queryParams("key",API_KEY).
                        queryParam("name",anyNameFromCharacterList).
                        when().
                        get("/characters").prettyPeek();

        Character character = response2.jsonPath().getObject("[0]",Character.class );

        System.out.println("Character = " + character);


        response2.then().
                assertThat().
                body("[0].name",is(anyName));
    }

    @Test
    public void verifyNameSearch() {
        Response response =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").
                        when().
                        queryParams("key", API_KEY).
                        queryParams("name", "Harry Potter").
                        get("/characters");
        response.then().statusCode(200).
                contentType("application/json; charset=utf-8");
        response.then().assertThat().body("name[0]", is("Harry Potter"));
        Response response1 =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").
                        when().
                        queryParams("key", API_KEY).
                        queryParams("name", "Marry Potter").
                        get("/characters").prettyPeek();
        response1.then().statusCode(200).contentType("application/json; charset=utf-8");

        List<Character> result = response1.jsonPath().getList("");
        assertTrue(result.isEmpty());
    }

    @Test
    public void verifyHouseMembers() {
        Response response =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").
                        when().
                        queryParams("key", API_KEY).
                        get("/houses");
        response.then().statusCode(200).
                contentType("application/json; charset=utf-8");

        List<Map<String, Object>> result = response.jsonPath().getList("");
        String gryfId = "";

        List<String> allMembers = new ArrayList<>();

        for (Map<String, Object> stringObjectMap : result) {
            if (stringObjectMap.get("name").equals("Gryffindor")) {
                gryfId = stringObjectMap.get("_id").toString();
                allMembers.add(stringObjectMap.get("members").toString());
            }
        }
        Response response1 =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").
                        pathParam("id",gryfId).
                        when().
                        queryParams("key", API_KEY).
                        get("/houses/{id}");

        List<String> memberIds=response1.jsonPath().getList("members._id",String.class);

        assertEquals(allMembers,memberIds);
    }

    @Test
    public void verifyHouseMembersAgain(){
        Response response =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").
                        pathParam("id","5a05e2b252f721a3cf2ea33f").
                        when().
                        queryParams("key", API_KEY).
                        get("/houses/{id}");

        List<String> members = response.jsonPath().getList("members[0]._id",String.class);

        Response response1=given().contentType(ContentType.JSON).
                header("Accept", "application/json").
                queryParam("house","Gryffindor").
                when().
                queryParams("key", API_KEY).
                get("/characters");

        List<String> characters = response1.jsonPath().getList("_id",String.class);

        assertTrue(members.get(0).equals(characters.get(0)));
    }

    @Test
    public void verifyHouseMostMembers(){
        Response response =
                given().contentType(ContentType.JSON).
                        header("Accept", "application/json").
                        when().
                        queryParams("key", API_KEY).
                        get("/houses");
        response.then().statusCode(200).
                contentType("application/json; charset=utf-8");

        List<House> allHouses = response.jsonPath().getList("",House.class);

        int max=allHouses.get(0).getMembers().size(), max_i=0;

        for (int i = 0; i <allHouses.size() ; i++) {
            if(allHouses.get(i).getMembers().size()>=max){
                max_i=i;
            }
        }
        assertEquals("Gryffindor",allHouses.get(max_i).getName());
    }

}
