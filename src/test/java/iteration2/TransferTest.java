package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class TransferTest {

    private static String userAuthHeader;
    private static int firstAccountId;
    private static int secondAccountId;


    @BeforeAll
    public static void setup() {

        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        //создаем пользака
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Pavel16",
                          "password": "Pavelpavel1#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(201);

        //логинимся и берем его токен
        userAuthHeader = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "Pavel16",
                          "password": "Pavelpavel1#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .header("Authorization");

        //создаем 2 счета
        firstAccountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        secondAccountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body("""
                        {
                          "id": %d,
                          "balance": 100.50
                        }
                        """.formatted(firstAccountId))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Test
    public void userTransfer() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body("""
                        {
                          "senderAccountId": %d,
                          "receiverAccountId": %d,
                          "amount": 50.50
                        }
                        """.formatted(firstAccountId, secondAccountId))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(200)
                .body("find { it.id == %d }.balance".formatted(firstAccountId), org.hamcrest.Matchers.equalTo(50.00f))
                .body("find { it.id == %d }.balance".formatted(secondAccountId), org.hamcrest.Matchers.equalTo(50.50f));

    }
}
