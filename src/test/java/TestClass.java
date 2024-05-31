import Helpers.UserAPITest;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
public class TestClass {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://hr-challenge.dev.tapyou.com/api/test";
    }


    @Test
    @Description("Request a list of users by male / female")
    public void testGetUsersByGender() {
        String[] genders = {"male", "female"};
        for (String gender : genders) {
            Response response = given()
                    .queryParam("gender", gender)
                    .when()
                    .get("/users")
                    .then()
                    .statusCode(200)
                    .contentType("application/json")
                    .extract()
                    .response();

            assertTrue(response.jsonPath().getBoolean("isSuccess"));
            assertEquals(0, response.jsonPath().getInt("errorCode"));
            assertNull(response.jsonPath().getString("errorMessage"));
            assertTrue(response.jsonPath().getList("idList").size() > 0);
        }
    }

    @Test
    @Description("Negative scenarios for requesting a list of users by male / female")
    public void testGetUsersByInvalidGender() {
        String[] invalidGenders = {"invalidGender", "", "123", "null", "maleFemale"};
        for (String gender : invalidGenders) {
            Response response = given()
                    .queryParam("gender", gender)
                    .when()
                    .get("/users")
                    .then()
                    .statusCode(400) // Предполагая, что API возвращает 400 Bad Request для недопустимых значений
                    .contentType("application/json")
                    .extract()
                    .response();

            assertFalse(response.jsonPath().getBoolean("isSuccess"));
            assertNotEquals(0, response.jsonPath().getInt("errorCode"));
            assertNotNull(response.jsonPath().getString("errorMessage"));
            assertNull(response.jsonPath().getList("idList"));
        }
    }

    @Test
    @Description("Request a user by ID (presumed male)")
    public void testGetMaleUserById(SoftAssertions softly) {
        int[] userIds = {10, 15, 33, 94, 501, 911};
        for (int userId : userIds) {
            UserAPITest.testGetUserById(softly, userId, "male");
        }
        softly.assertAll();
    }

    @Test
    @Description("Request a user by ID (presumed female)")
    public void testGetFemaleUserById(SoftAssertions softly) {
        int[] userIds = {5, 15, 16, 300, 502, 503};
        for (int userId : userIds) {
            UserAPITest.testGetUserById(softly, userId, "female");
        }
        softly.assertAll();
    }
    @Test
    @Description("Negative scenarios for requesting a user by ID")
    public void testGetNonExistingUser(SoftAssertions softly) {
        int[] nonExistingUserIds = {0, -1, 777};
        for (int nonExistingUserId : nonExistingUserIds) {
            Response response = given()
                    .pathParam("id", nonExistingUserId)
                    .when()
                    .get("/user/{id}");

            System.out.println("Response Body: " + response.getBody().asString());
            softly.assertThat(response.getStatusCode()).isEqualTo(404);
            softly.assertThat(response.getBody().asString()).contains("User not found");
            softly.assertThat(response.jsonPath().getBoolean("isSuccess")).isFalse();
            softly.assertThat(response.jsonPath().getInt("errorCode")).isNotEqualTo(0);
            softly.assertThat(response.jsonPath().getString("errorMessage")).isNotNull();
        }
    }
}
