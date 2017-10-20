package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for DeviceClientDiagnostic
 */
public class DeviceClientDiagnosticTest {
    @Mocked
    DeviceClientConfig mockConfig;

    @Mocked
    IotHubConnectionString mockIotHubConnectionString;

    @Mocked
    DeviceIO mockDeviceIO;

    // Tests_SRS_DEVICECLIENTDIAGNOSTIC_01_001 [If parameter of setDiagSamplingPercentage is negative, throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setDiagSamplingPercentageNegativeValueThrows()
    {
        // arrange
        DeviceClientDiagnostic diagnostic = new DeviceClientDiagnostic();

        // act
        diagnostic.setDiagSamplingPercentage(-1);
    }

    // Tests_SRS_DEVICECLIENTDIAGNOSTIC_01_002 [If parameter of setDiagSamplingPercentage is larger than 100, throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setDiagSamplingPercentageIllegalValueThrows()
    {
        // arrange
        DeviceClientDiagnostic diagnostic = new DeviceClientDiagnostic();

        // act
        diagnostic.setDiagSamplingPercentage(101);
    }

    // Tests_SRS_DEVICECLIENTDIAGNOSTIC_01_003 [The function shall add diagnostic property data to message if sampling percentage is set to 100.]
    @Test
    public void addDiagnosticInfoIfNecessaryWillAddDiagnosticInfo(@Mocked final Message mockMessage)
    {
        // arrange
        DeviceClientDiagnostic diagnostic = new DeviceClientDiagnostic();
        diagnostic.setDiagSamplingPercentage(100);

        // act
        diagnostic.addDiagnosticInfoIfNecessary(mockMessage);

        // assert
        new Verifications()
        {
            {
                mockMessage.setDiagnosticPropertyData((DiagnosticPropertyData) any);
                times = 1;
            }
        };
    }

    // Tests_SRS_DEVICECLIENTDIAGNOSTIC_01_004 [The function shall not add diagnostic property data to message and increment message number if sampling percentage is set to 0.]
    @Test
    public void addDiagnosticInfoIfNecessaryWillNotAddDiagnosticInfoIfPercentageIsZero(@Mocked final Message mockMessage)
    {
        // arrange
        final DeviceClientDiagnostic diagnostic = new DeviceClientDiagnostic();
        diagnostic.setDiagSamplingPercentage(0);

        // act
        diagnostic.addDiagnosticInfoIfNecessary(mockMessage);

        // assert
        new Verifications()
        {
            {
                mockMessage.setDiagnosticPropertyData((DiagnosticPropertyData) any);
                times = 0;
                assertEquals(0, Deencapsulation.getField(diagnostic, "currentMessageNumber"));
            }
        };
    }

    // Tests_SRS_DEVICECLIENTDIAGNOSTIC_01_005 [The function shall add diagnostic property data to message correspond to the sampling percentage.]
    @Test
    public void addDiagnosticInfoIfNecessaryWillAddDiagnosticInfoApplyToPercentage(@Mocked final Message mockMessage)
    {
        // arrange
        DeviceClientDiagnostic diagnostic = new DeviceClientDiagnostic();
        diagnostic.setDiagSamplingPercentage(80);

        // act
        for(int i=0;i<10;i++) {
            diagnostic.addDiagnosticInfoIfNecessary(mockMessage);
        }
        // assert
        new Verifications()
        {
            {
                mockMessage.setDiagnosticPropertyData((DiagnosticPropertyData) any);
                times = 8;
            }
        };
    }
}
