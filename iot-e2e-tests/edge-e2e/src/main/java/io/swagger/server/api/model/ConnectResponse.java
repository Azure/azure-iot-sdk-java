package io.swagger.server.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * result of a connection to a service, device, or module client
 **/
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class ConnectResponse   {
  
  private String connectionId = null;

  public ConnectResponse () {

  }

  public ConnectResponse (String connectionId) {
    this.connectionId = connectionId;
  }

    
  @JsonProperty("connectionId")
  public String getConnectionId() {
    return connectionId;
  }
  public void setConnectionId(String connectionId) {
    this.connectionId = connectionId;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectResponse connectResponse = (ConnectResponse) o;
    return Objects.equals(connectionId, connectResponse.connectionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(connectionId);
  }

  @Override
  public String toString() {

    String sb = "class ConnectResponse {\n" +
            "    connectionId: " + toIndentedString(connectionId) + "\n" +
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
