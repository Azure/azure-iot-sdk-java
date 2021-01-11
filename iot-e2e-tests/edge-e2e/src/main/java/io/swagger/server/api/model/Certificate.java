package io.swagger.server.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * certificate in the body of a message
 **/
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class Certificate   {
  
  private String cert = null;

  public Certificate () {

  }

  public Certificate (String cert) {
    this.cert = cert;
  }

    
  @JsonProperty("cert")
  public String getCert() {
    return cert;
  }
  public void setCert(String cert) {
    this.cert = cert;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Certificate certificate = (Certificate) o;
    return Objects.equals(cert, certificate.cert);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cert);
  }

  @Override
  public String toString() {

    String sb = "class Certificate {\n" +
            "    cert: " + toIndentedString(cert) + "\n" +
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
