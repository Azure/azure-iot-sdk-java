package tests.unit.com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.exceptions.*;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpResponseVerification;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.AmqpError;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.junit.Test;

import static org.junit.Assert.*;

public class AmqpResponseVerificationTest
{
    @Mocked
    DeliveryState mockedDeliveryState;

    @Mocked
    Accepted mockedAccepted;

    @Mocked
    Rejected mockedRejected;

    @Mocked
    Received mockedReceived;

    @Mocked
    Released mockedReleased;

    @Mocked
    Modified mockedModified;

    @Mocked
    ErrorCondition mockedErrorCondition;


    @Test
    public void constructor_saves_if_rejected()
    {
        //Arrange
        new NonStrictExpectations()
        {
            {
                mockedRejected.getError();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = AmqpError.NOT_FOUND;

            }

        };
        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedRejected);

        //Assert
        new Verifications()
        {
            {
                assertNotNull(testVerification.getException());
            }
        };
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_001: [** The function shall save IotHubNotFoundException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-found **]**
    @Test
    public void constructor_saves_NOT_FOUND_exception()
    {
        //Arrange
        String errorDescription = "TestDescription";
        new NonStrictExpectations()
        {
            {
                mockedRejected.getError();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = AmqpError.NOT_FOUND;
                mockedErrorCondition.getDescription();
                result = errorDescription;
            }

        };

        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedRejected);

        //Assert
        new Verifications()
        {
            {
                assertTrue(testVerification.getException() instanceof IotHubNotFoundException);
            }
        };
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_002: [** The function shall save IotHubNotSupportedException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-implemented **]**
    @Test
    public void constructor_saves_NOT_IMPLEMENTED_exception()
    {
        //Arrange
        String errorDescription = "TestDescription";
        new NonStrictExpectations()
        {
            {
                mockedRejected.getError();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = AmqpError.NOT_IMPLEMENTED;
                mockedErrorCondition.getDescription();
                result = errorDescription;
            }

        };

        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedRejected);

        //Assert
        new Verifications()
        {
            {
                assertTrue(testVerification.getException() instanceof IotHubNotSupportedException);
            }
        };
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_003: [** The function shall save IotHubInvalidOperationException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-allowed **]**
    @Test
    public void constructor_saves_NOT_ALLOWED_exception()
    {
        //Arrange
        String errorDescription = "TestDescription";
        new NonStrictExpectations()
        {
            {
                mockedRejected.getError();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = AmqpError.NOT_ALLOWED;
                mockedErrorCondition.getDescription();
                result = errorDescription;
            }

        };

        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedRejected);

        //Assert
        new Verifications()
        {
            {
                assertTrue(testVerification.getException() instanceof IotHubInvalidOperationException);
            }
        };
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_004: [** The function shall save IotHubUnathorizedException if the amqp delivery state is rejected and error condition is amqp error code amqp:unauthorized-access **]**
    @Test
    public void constructor_saves_UNAUTHORIZED_ACCESS_exception()
    {
        //Arrange
        String errorDescription = "TestDescription";
        new NonStrictExpectations()
        {
            {
                mockedRejected.getError();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = AmqpError.UNAUTHORIZED_ACCESS;
                mockedErrorCondition.getDescription();
                result = errorDescription;
            }

        };

        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedRejected);

        //Assert
        new Verifications()
        {
            {
                assertTrue(testVerification.getException() instanceof IotHubUnathorizedException);
            }
        };
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_005: [** The function shall save IotHubDeviceMaximumQueueDepthExceededException if the amqp delivery state is rejected and error condition is amqp error code amqp:resource-limit-exceeded **]**
    @Test
    public void constructor_saves_RESOURCE_LIMIT_EXCEEDED_exception()
    {
        //Arrange
        String errorDescription = "TestDescription";
        new NonStrictExpectations()
        {
            {
                mockedRejected.getError();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = AmqpError.RESOURCE_LIMIT_EXCEEDED;
                mockedErrorCondition.getDescription();
                result = errorDescription;
            }

        };

        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedRejected);

        //Assert
        new Verifications()
        {
            {
                assertTrue(testVerification.getException() instanceof IotHubDeviceMaximumQueueDepthExceededException);
            }
        };
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_006: [** The function shall save null exception if the amqp delivery state is accepted or received or released or modified **]**
    @Test
    public void constructor_saves_if_accepted()
    {
        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedAccepted);

        //Assert
        new Verifications()
        {
            {
                assertNull(testVerification.getException());
            }
        };
    }

    @Test
    public void constructor_saves_if_received()
    {
        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedReceived);

        //Assert
        new Verifications()
        {
            {
                assertNull(testVerification.getException());
            }
        };
    }

    @Test
    public void constructor_saves_if_released()
    {
        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedReleased);

        //Assert
        new Verifications()
        {
            {
                assertNull(testVerification.getException());
            }
        };
    }

    @Test
    public void constructor_saves_if_modified()
    {
        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedModified);

        //Assert
        new Verifications()
        {
            {
                assertNull(testVerification.getException());
            }
        };
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_007: [** The function shall return the exception saved earlier by the constructor **]**
    @Test
    public void getExceptionGets()
    {
        //Arrange
        String errorDescription = "TestDescription";
        new NonStrictExpectations()
        {
            {
                mockedRejected.getError();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = AmqpError.RESOURCE_LIMIT_EXCEEDED;
                mockedErrorCondition.getDescription();
                result = errorDescription;
            }

        };

        //Act
        AmqpResponseVerification testVerification = new AmqpResponseVerification(mockedRejected);

        //Assert
        new Verifications()
        {
            {
                assertTrue(testVerification.getException() instanceof IotHubDeviceMaximumQueueDepthExceededException);
            }
        };

    }
}
