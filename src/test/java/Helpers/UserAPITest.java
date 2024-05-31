package Helpers;

import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;

public class UserAPITest {

    public static void testGetUserById(SoftAssertions softly, int userId, String gender) {
        LocalDate currentDate = LocalDate.now();
        LocalDate earliestDate = currentDate.minusYears(100);


        Response response = given()
                .pathParam("id", userId)
                .when()
                .get("/user/{id}")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract()
                .response();
        long responseTime = response.time();

        softly.assertThat(responseTime)
                .as("Response time is long for ID %d", userId)
                .isLessThan(999);

        softly.assertThat(response.jsonPath().getBoolean("isSuccess")).as("Success error for %d", userId)
                .isTrue();
        softly.assertThat(response.jsonPath().getInt("errorCode")).as("errorCode for user with ID %d", userId)
                .isEqualTo(0);
        softly.assertThat(response.jsonPath().getString("errorMessage"))
                .as("errorMessage for user with ID %d", userId)
                .isNull();

        softly.assertThat(response.jsonPath().getInt("user.id"))
                .as("User error %d", userId)
                .isEqualTo(userId);
        softly.assertThat(response.jsonPath().getString("user.name"))
                .as("Name error %d", userId).isNotNull();

        if (gender.equalsIgnoreCase("male")) {
            softly.assertThat(response.jsonPath().getString("user.gender"))
                    .as("Gender error for user with ID %d", userId)
                    .isEqualTo("male");
        } else {
            softly.assertThat(response.jsonPath().getString("user.gender"))
                    .as("Gender error for user with ID %d", userId)
                    .isEqualTo("female");
        }

        softly.assertThat(response.jsonPath().getInt("user.age"))
                .as("Age error %d", userId)
                .isPositive().isLessThan(100);
        softly.assertThat(response.jsonPath().getString("user.city")).isNotNull();
        softly.assertThat(response.jsonPath().getString("user.registrationDate"))
                .as("Date error %d", userId)
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+")
                .satisfies(registrationDate -> {
                    LocalDate parsedDate = LocalDate.parse((String) registrationDate, DateTimeFormatter.ISO_DATE_TIME);
                    softly.assertThat(parsedDate)
                            .as("Registration date exception for user ID %d", userId)
                            .isBeforeOrEqualTo(currentDate)
                            .isAfter(earliestDate);
                });
    }
}
