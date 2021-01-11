package io.swagger.server.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * parameters and response for a sync method call
 **/
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class RoundtripMethodCallBody   {
  
  private Object requestPayload = null;
  private Object responsePayload = null;
  private Integer statusCode = null;

  public RoundtripMethodCallBody () {

  }

  public RoundtripMethodCallBody (Object requestPayload, Object responsePayload, Integer statusCode) {
    this.requestPayload = requestPayload;
    this.responsePayload = responsePayload;
    this.statusCode = statusCode;
  }

    
  @JsonProperty("requestPayload")
  public Object getRequestPayload() {
    return requestPayload;
  }
  public void setRequestPayload(Object requestPayload) {
    this.requestPayload = requestPayload;
  }

    
  @JsonProperty("responsePayload")
  public Object getResponsePayload() {
    return responsePayload;
  }
  public void setResponsePayload(Object responsePayload) {
    this.responsePayload = responsePayload;
  }

    
  @JsonProperty("statusCode")
  public Integer getStatusCode() {
    return statusCode;
  }
  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RoundtripMethodCallBody roundtripMethodCallBody = (RoundtripMethodCallBody) o;
    return Objects.equals(requestPayload, roundtripMethodCallBody.requestPayload) &&
        Objects.equals(responsePayload, roundtripMethodCallBody.responsePayload) &&
        Objects.equals(statusCode, roundtripMethodCallBody.statusCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestPayload, responsePayload, statusCode);
  }

  @Override
  public String toString() {

    String sb = "class RoundtripMethodCallBody {\n" +
            "    requestPayload: " + toIndentedString(requestPayload) + "\n" +
            "    responsePayload: " + toIndentedString(responsePayload) + "\n" +
            "    statusCode: " + toIndentedString(statusCode) + "\n" +
            "}";
    return sb;
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
