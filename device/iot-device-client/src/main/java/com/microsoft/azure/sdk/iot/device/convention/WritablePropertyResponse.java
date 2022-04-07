package com.microsoft.azure.sdk.iot.device.convention;

import lombok.Getter;
import lombok.Setter;

public interface WritablePropertyResponse
{
    Object getValue();
    void setValue(Object obj);

    Integer getAckCode();
    void setAckCode(Integer ackCode);

    Long getAckVersion();
    void setAckVersion(Long ackVersion);

    String getAckDescription();
    void setAckDescription(String ackDescription);
}
