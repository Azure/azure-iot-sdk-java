package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;
import lombok.Setter;

public interface IWritablePropertyResponse
{
    Object getValue();
    void setValue(Object obj);

    int getAckCode();
    int setAckCode(int ackCode);

    long getAckVersion();
    void setAckVersion(long ackVersion);

    String getAckDescription();
    void setAckDescription(String ackDescription);
}
