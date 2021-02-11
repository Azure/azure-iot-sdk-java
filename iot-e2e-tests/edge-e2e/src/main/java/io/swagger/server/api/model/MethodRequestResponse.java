package io.swagger.server.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * result of waiting on a method call
 **/
@SuppressWarnings("ALL")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MethodRequestResponse   {
  
  private Object requestPayload = null;
  private String responseId = null;

  public MethodRequestResponse () {

  }

  public MethodRequestResponse (Object requestPayload, String responseId) {
    this.requestPayload = requestPayload;
    this.responseId = responseId;
  }

    
  @JsonProperty("requestPayload")
  public Object getRequestPayload() {
    return requestPayload;
  }
  public void setRequestPayload(Object requestPayload) {
    this.requestPayload = requestPayload;
  }

    
  @JsonProperty("responseId")
  public String getResponseId() {
    return responseId;
  }
  public void setResponseId(String responseId) {
    this.responseId = responseId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MethodRequestResponse methodRequestResponse = (MethodRequestResponse) o;
    return Objects.equals(requestPayload, methodRequestResponse.requestPayload) &&
        Objects.equals(responseId, methodRequestResponse.responseId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestPayload, responseId);
  }

  @Override
  public String toString() {

      return "class MethodRequestResponse {\n" +
              "    requestPayload: " + toIndentedString(requestPayload) + "\n" +
              "    responseId: " + toIndentedString(responseId) + "\n" +
              "}";
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
