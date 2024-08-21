import com.ecommerce.practice.helper.LoginRequest;
import com.ecommerce.practice.helper.LoginResponse;
import com.ecommerce.practice.helper.OrderDetail;
import com.ecommerce.practice.helper.Orders;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class ECommerceAPITest {
    String token = "";
    String userId = "";
    JsonPath js;
    String productId = "";

    @Test
    public void login() {
        RequestSpecification request = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .setContentType(ContentType.JSON)
                .build();
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserEmail("training.shreesha@gmail.com");
        loginRequest.setUserPassword("Train123");

        RequestSpecification loginReq = given().relaxedHTTPSValidation().log().all().spec(request).body(loginRequest);
        LoginResponse loginResponse = loginReq.when().post("/api/ecom/auth/login")
                .then().log().all().extract().response().as(LoginResponse.class);
        token = loginResponse.getToken();
        userId = loginResponse.getUserId();
        System.out.println(loginResponse.getToken());
        System.out.println(loginResponse.getUserId());
    }

    @Test(dependsOnMethods = { "login" })
    public void create_product() {
        RequestSpecification request = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .addHeader("Authorization", token)
                .build();

        RequestSpecification reqProduct = given().relaxedHTTPSValidation().log().all().spec(request)
                .param("productName", "Football")
                .param("productAddedBy", userId)
                .param("productCategory", "sports")
                .param("productSubCategory", "ball")
                .param("productPrice", "21500")
                .param("productDescription", "Cosco")
                .param("productFor", "all")
                .multiPart("productImage", new File("src\\main\\resources\\football.jpeg"));
        String responseProduct = reqProduct.when().post("/api/ecom/product/add-product")
                .then().log().all().extract().response().asString();
        js = new JsonPath(responseProduct);
        productId = js.get("productId");
    }

    @Test(dependsOnMethods = { "create_product" })
    public void create_order() {
        RequestSpecification requestOrder = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .addHeader("Authorization", token)
                .setContentType(ContentType.JSON)
                .build();

        OrderDetail orderdetail = new OrderDetail();
        orderdetail.setCountry("India");
        orderdetail.setProductOrderedId(productId);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        orderDetailList.add(orderdetail);

        Orders orders = new Orders();
        orders.setOrders(orderDetailList);

        RequestSpecification reqCreateOrder = given().log().all().spec(requestOrder).body(orders);
        String responseCreateOrder = reqCreateOrder.when().post("/api/ecom/order/create-order")
                .then().log().all().extract().response().asString();
        System.out.println(responseCreateOrder);
    }

    @Test(dependsOnMethods = { "create_order" })
    public void delete_order() {
        RequestSpecification requestDelete = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .addHeader("Authorization", token)
                .setContentType(ContentType.JSON)
                .build();

        RequestSpecification reqDeleteProduct = given().log().all().spec(requestDelete).pathParam("productId", productId);
        String responseDeleteProduct = reqDeleteProduct.when().delete("/api/ecom/product/delete-product/{productId}")
                .then().log().all().extract().response().asString();
        js = new JsonPath(responseDeleteProduct);
        String productDeleteConfirmation = js.get("message");
        Assert.assertEquals("Product Deleted Successfully", productDeleteConfirmation);
    }
}
