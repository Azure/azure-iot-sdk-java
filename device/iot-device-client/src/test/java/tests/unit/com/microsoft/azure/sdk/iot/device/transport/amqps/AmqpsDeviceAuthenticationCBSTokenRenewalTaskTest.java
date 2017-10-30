package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsDeviceAuthenticationCBSTokenRenewalTask;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsSessionDeviceOperation;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for AmqpsDeviceAuthenticationCBSTest
 * 100% methods covered
 * 76% lines covered
 */
public class AmqpsDeviceAuthenticationCBSTokenRenewalTaskTest
{
    @Mocked
    AmqpsSessionDeviceOperation mockAmqpsSessionDeviceOperation;

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_12_001: [The constructor shall throw IllegalArgumentException if the amqpsSessionDeviceOperation parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientIsNull() throws IllegalArgumentException
    {
        // act
        new AmqpsDeviceAuthenticationCBSTokenRenewalTask(null);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_002: [The constructor shall save the amqpsSessionDeviceOperation.]
    @Test
    public void constructorSavesParameters() throws IllegalArgumentException
    {
        // act
        AmqpsDeviceAuthenticationCBSTokenRenewalTask amqpsSessionManagerTask = new AmqpsDeviceAuthenticationCBSTokenRenewalTask(mockAmqpsSessionDeviceOperation);

        // assert
        AmqpsSessionDeviceOperation actualAmqpsDeviceAuthenticationCBS = Deencapsulation.getField(amqpsSessionManagerTask, "amqpsSessionDeviceOperation");
        assertTrue(mockAmqpsSessionDeviceOperation == actualAmqpsDeviceAuthenticationCBS);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_12_003: [The function shall call the amqpsSessionDeviceOperation.renewToken.]
    @Test
    public void run() throws IllegalArgumentException
    {
        // arrange
        AmqpsDeviceAuthenticationCBSTokenRenewalTask amqpsSessionManagerTask = new AmqpsDeviceAuthenticationCBSTokenRenewalTask(mockAmqpsSessionDeviceOperation);

        // act
        amqpsSessionManagerTask.run();

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "renewToken");
            }
        };
    }
}
