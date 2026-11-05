package iteration2;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class DepositTest {

    private static String userAuthHeader;
    private static int accountId;

    @BeforeAll
    public static void setup() {
        //создаем пользака
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "victor945",
                          "password": "Kate2000#",
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
                          "username": "victor945",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .header("Authorization");

        //создаем аккаунт
        accountId = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    public void userCanDepositMoneyTest() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body("""
                        {
                          "id": %d,
                          "balance": 100.50
                        }
                        """.formatted(accountId))
                .post("http://localhost:4111/api/v1/accounts/deposit")
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
                .body("find { it.id == " + accountId + " }.balance", equalTo(100.50f));
    }
}