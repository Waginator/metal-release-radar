package com.metalr2.web;

import com.metalr2.web.dto.request.FollowArtistRequest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class RestAssuredRequestHandler {

  private final String requestUri;

  public RestAssuredRequestHandler(String requestUri) {
    this.requestUri = requestUri;
  }

  public ValidatableResponse doPost(ContentType accept, FollowArtistRequest request) {
    return given()
              .contentType(accept)
              .accept(accept)
              .body(request)
            .when()
              .post(requestUri)
//              .peek()
            .then();
  }

  public ValidatableResponse doDelete(ContentType accept, FollowArtistRequest request) {
    return given()
              .contentType(accept)
              .accept(accept)
              .body(request)
            .when()
              .delete(requestUri)
//              .peek()
            .then();
  }
}
