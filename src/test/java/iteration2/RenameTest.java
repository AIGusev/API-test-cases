package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class RenameTest {
    private static String userAuthHeader;


    @BeforeAll
    public static void setup() {

        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        //создаем пользака
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Arseniy144",
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
                          "username": "Arseniy144",
                          "password": "Pavelpavel1#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .header("Authorization");
    }

    @Test
    public void renameTest() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", userAuthHeader)
                .body("""
                        {
                          "name": "John Smith"
                        }
                        """)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(200);

    }
}
