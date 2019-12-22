package com.metalr2.web;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.ValidatableResponse;

import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class RestAssuredRequestHandler<T> {

  private final String requestUri;

  public RestAssuredRequestHandler(String requestUri) {
    this.requestUri = requestUri;
    RestAssured.defaultParser = Parser.JSON;
  }

  public ValidatableResponse doGet(ContentType accept) {
    return doGet("", accept, Collections.emptyMap());
  }

  public ValidatableResponse doGet(ContentType accept, Map<String,Object> params) {
    return doGet("", accept, params);
  }

  public ValidatableResponse doGet(String pathSegment, ContentType accept) {
    return doGet(pathSegment, accept, Collections.emptyMap());
  }

  public ValidatableResponse doGet(String pathSegment, ContentType accept, Map<String,Object> params) {
    return given()
             .accept(accept)
             .params(params)
           .when()
             .get(requestUri + pathSegment)
           .then();
  }

  public ValidatableResponse doPost(ContentType accept, T request) {
    return given()
              .contentType(accept)
              .accept(accept)
              .body(request)
            .when()
              .post(requestUri)
            .then();
  }

  public ValidatableResponse doPost(String pathSegment, ContentType accept) {
    return given()
           .contentType(accept)
           .accept(accept)
          .when()
           .post(requestUri + pathSegment)
          .then();
  }

  public ValidatableResponse doDelete(ContentType accept, T request) {
    return given()
              .contentType(accept)
              .accept(accept)
              .body(request)
            .when()
              .delete(requestUri)
            .then();
  }

  public ValidatableResponse doDelete(String pathSegment, ContentType accept) {
    return given()
            .contentType(accept)
            .accept(accept)
           .when()
            .delete(requestUri + pathSegment)
           .then();
  }

}
