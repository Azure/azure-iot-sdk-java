package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsDeviceAuthenticationCBS;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsDeviceAuthenticationCBSSendTask;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsDeviceAuthenticationCBSTokenRenewalTask;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for DeviceClient.
 * Methods: 100%
 * Lines: 84%
 */
public class AmqpsDeviceAuthenticationCBSSendTaskTest
{
    @Mocked
    AmqpsDeviceAuthenticationCBS mockAmqpsDeviceAuthenticationCBS;

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_001: [The constructor shall throw IllegalArgumentException if the amqpsDeviceAuthenticationCBS parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientIsNull() throws IllegalArgumentException
    {
        // act
        new AmqpsDeviceAuthenticationCBSTokenRenewalTask(null);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_002: [The constructor shall save the amqpsDeviceAuthenticationCBS.]
    @Test
    public void constructorSavesParameters() throws IllegalArgumentException
    {
        // act
        AmqpsDeviceAuthenticationCBSSendTask amqpsSessionManagerTask = new AmqpsDeviceAuthenticationCBSSendTask(mockAmqpsDeviceAuthenticationCBS);

        // assert
        AmqpsDeviceAuthenticationCBS actualAmqpsDeviceAuthenticationCBS = Deencapsulation.getField(amqpsSessionManagerTask, "amqpsDeviceAuthenticationCBS");
        assertTrue(mockAmqpsDeviceAuthenticationCBS == actualAmqpsDeviceAuthenticationCBS);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_003: [The function shall call the amqpsDeviceAuthenticationCBS.sendAuthenticationMessages.]
    @Test
    public void run() throws IllegalArgumentException
    {
        // arrange
        AmqpsDeviceAuthenticationCBSSendTask amqpsSessionManagerTask = new AmqpsDeviceAuthenticationCBSSendTask(mockAmqpsDeviceAuthenticationCBS);

        // act
        amqpsSessionManagerTask.run();

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "sendAuthenticationMessages");
            }
        };
    }
}
