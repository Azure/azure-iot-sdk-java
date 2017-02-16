package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.IoTHubDeviceProperties;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for IoTHubDeviceProperties serializer
 */
public class IoTHubDevicePropertiesTest {

    private static final String validDeviceId = "MyValidDeviceId";
    private static final String validGenerationId = "MyValidGenerationId";
    private static final String bigString_150chars =
            "01234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789" +
            "01234567890123456789012345678901234567890123456789";
    private static final String validReasonToDisableDevice = "Good reason to disable the device";
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'";
    private static final String TIMEZONE = "UTC";


    /**
     * CONSTRUCTOR
     */

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_001: [The constructor shall receive the deviceId and store it using the SetDevice.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_002: [The constructor shall receive the generationId and store it using the SetGeneration.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_003: [The constructor shall set the etag as `1`.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_004: [The constructor shall set the device status as enabled.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_005: [The constructor shall store the string `provisioned` in the statusReason.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_006: [The constructor shall store the current date and time in statusUpdateTime.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_007: [If one of the parameters do not fit the criteria, the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_032: [The GetDeviceId shall return a string with the device name stored in the deviceId.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_033: [The GetGenerationId shall return a string with the device generation stored in the generationId.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_034: [The GetETag shall return a string with the last message ETag stored in the etag.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_035: [The GetStatus shall return the device status stored in the status.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_036: [The GetStatusReason shall return a string with the status reason stored in the statusReason.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_037: [The GetStatusUpdateTime shall return a string with the last status update time stored in the statusUpdateTime.] */  
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_038: [All data and time shall use ISO8601 UTC format.] */
    @Test
    public void constructorTest_succeed() throws ParseException
    {
        Date expectedLowDate = new Date();
        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, validGenerationId);
        Date expectedHighDate = new Date();

        String StrDate = properties.GetStatusUpdateTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        Date returnedDate = dateFormat.parse(StrDate);

        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("1"));
        assertThat(properties.GetStatus(), is(IoTHubDeviceProperties.DeviceStatusEnum.enabled));
        assertThat(properties.GetStatusReason(), is("provisioned"));
        assertThat(expectedHighDate, greaterThanOrEqualTo(returnedDate));
        assertThat(expectedLowDate, lessThanOrEqualTo(returnedDate));
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_001: [The constructor shall receive the deviceId and store it using the SetDevice.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_007: [If one of the parameters do not fit the criteria, the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_039: [All strings shall be up to 128 char long.] */
    @Test
    public void deviceIdToBig_failed()
    {
        Boolean testSucceed = false;

        try {
            new IoTHubDeviceProperties(bigString_150chars, validGenerationId);
        }
        catch (IllegalArgumentException e) {
            testSucceed = true;
        }

        assertTrue("Constructor do not throws IllegalArgumentException", testSucceed);
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_002: [The constructor shall receive the generationId and store it using the SetGeneration.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_007: [If one of the parameters do not fit the criteria, the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_039: [All strings shall be up to 128 char long.] */
    @Test
    public void generationIdToBig_failed()
    {

        Boolean testSucceed = false;

        try {
            new IoTHubDeviceProperties(validDeviceId, bigString_150chars);
        }
        catch (IllegalArgumentException e) {
            testSucceed = true;
        }

        assertTrue("Constructor do not throws IllegalArgumentException", testSucceed);
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_001: [The constructor shall receive the deviceId and store it using the SetDevice.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_007: [If one of the parameters do not fit the criteria, the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_040: [All strings shall not be null.] */
    @Test
    public void nullDeviceId_failed()
    {
        Boolean testSucceed = false;

        try {
            new IoTHubDeviceProperties(null, validGenerationId);
        }
        catch (IllegalArgumentException e) {
            testSucceed = true;
        }

        assertTrue("Constructor do not throws IllegalArgumentException", testSucceed);
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_002: [The constructor shall receive the generationId and store it using the SetGeneration.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_007: [If one of the parameters do not fit the criteria, the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_040: [All strings shall not be null.] */
    @Test
    public void nullGenerationId_failed()
    {
        Boolean testSucceed = false;

        try {
            new IoTHubDeviceProperties(validDeviceId, null);
        }
        catch (IllegalArgumentException e) {
            testSucceed = true;
        }

        assertTrue("Constructor do not throws IllegalArgumentException", testSucceed);
    }


    /**
     * SetDevice
     */

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_008: [The SetDevice shall receive the device name and store copy it into the deviceId.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_009: [The SetDevice shall receive the device generation and store copy it into the generationId.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_010: [The SetDevice shall increment the etag by `1`.] */
    @Test
    public void setDevice_succeed()
    {
        IoTHubDeviceProperties properties = new IoTHubDeviceProperties("oldDeviceId", "oldGenerationId");

        properties.SetDevice(validDeviceId, validGenerationId);

        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("2"));
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_011: [If the provided name is null, the SetDevice not change the deviceId.] */
    @Test
    public void setDeviceWithNULLDeviceId_succeed()
    {
        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, "oldGenerationId");

        properties.SetDevice(null, validGenerationId);

        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("2"));
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_014: [If the provided generation is null, the SetDevice shall not change the generationId.] */
    @Test
    public void setDeviceWithNULLGenerationId_succeed()
    {
        IoTHubDeviceProperties properties = new IoTHubDeviceProperties("oldDeviceId", validGenerationId);

        properties.SetDevice(validDeviceId, null);

        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("2"));
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_012: [If the provided name do not fits the json criteria, the SetDevice shall throw IllegalArgumentException.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_039: [All strings shall be up to 128 char long.] */
    @Test
    public void setDeviceDeviceIdToBig_failed()
    {

        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, "oldGenerationId");

        Boolean testSucceed = false;

        try {
            properties.SetDevice(bigString_150chars, validGenerationId);
        }
        catch (IllegalArgumentException e) {
            testSucceed = true;
        }

        assertTrue("Constructor do not throws IllegalArgumentException", testSucceed);
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_015: [If the provided generation do not fits the json criteria, the SetDevice shall throw IllegalArgumentException.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_039: [All strings shall be up to 128 char long.] */
    @Test
    public void setDeviceGenerationIdToBig_failed()
    {

        IoTHubDeviceProperties properties = new IoTHubDeviceProperties("oldDeviceId", validGenerationId);

        Boolean testSucceed = false;

        try {
            properties.SetDevice(validDeviceId, bigString_150chars);
        }
        catch (IllegalArgumentException e) {
            testSucceed = true;
        }

        assertTrue("Constructor do not throws IllegalArgumentException", testSucceed);
    }


    /**
     * EnableDevice
     */

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_017: [The EnableDevice shall set the device as `enabled`.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_018: [The EnableDevice shall store the string `provisioned` in the statusReason.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_019: [The EnableDevice shall increment the etag by `1`.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_020: [The EnableDevice shall store the current date and time in statusUpdateTime.] */
    @Test
    public void enableDevice_succeed() throws ParseException
    {
        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, validGenerationId);
        properties.DisableDevice(validReasonToDisableDevice);

        Date expectedLowDate = new Date();
        properties.EnableDevice();
        Date expectedHighDate = new Date();

        String StrDate = properties.GetStatusUpdateTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        Date returnedDate = dateFormat.parse(StrDate);

        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("3"));
        assertThat(properties.GetStatus(), is(IoTHubDeviceProperties.DeviceStatusEnum.enabled));
        assertThat(properties.GetStatusReason(), is("provisioned"));
        assertThat(expectedHighDate, greaterThanOrEqualTo(returnedDate));
        assertThat(expectedLowDate, lessThanOrEqualTo(returnedDate));
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_013: [If the device is already enable, the EnableDevice shall not do anything.] */
    @Test
    public void enableDeviceAlreadyEnabled_succeed() throws ParseException
    {
        Date expectedLowDate = new Date();
        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, validGenerationId);
        Date expectedHighDate = new Date();

        properties.EnableDevice();

        String StrDate = properties.GetStatusUpdateTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        Date returnedDate = dateFormat.parse(StrDate);

        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("1"));
        assertThat(properties.GetStatus(), is(IoTHubDeviceProperties.DeviceStatusEnum.enabled));
        assertThat(properties.GetStatusReason(), is("provisioned"));
        assertThat(expectedHighDate, greaterThanOrEqualTo(returnedDate));
        assertThat(expectedLowDate, lessThanOrEqualTo(returnedDate));
    }


    /**
     * DisableDevice
     */

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_021: [The DisableDevice shall set the device as `disabled`.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_022: [The DisableDevice shall store the provided reason in the statusReason.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_023: [The DisableDevice shall increment the etag by `1`.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_024: [The DisableDevice shall store the current date and time in statusUpdateTime.] */
    @Test
    public void disableDeviceTest_succeed() throws ParseException
    {

        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, validGenerationId);

        Date expectedLowDate = new Date();
        properties.DisableDevice(validReasonToDisableDevice);
        Date expectedHighDate = new Date();

        String StrDate = properties.GetStatusUpdateTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        Date returnedDate = dateFormat.parse(StrDate);

        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("2"));
        assertThat(properties.GetStatus(), is(IoTHubDeviceProperties.DeviceStatusEnum.disabled));
        assertThat(properties.GetStatusReason(), is(validReasonToDisableDevice));
        assertThat(expectedHighDate, greaterThanOrEqualTo(returnedDate));
        assertThat(expectedLowDate, lessThanOrEqualTo(returnedDate));
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_026: [If the provided reason do not fits the json criteria, the DisableDevice shall throw IllegalArgumentException.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_039: [All strings shall be up to 128 char long.] */
    @Test
    public void disableDeviceReasonTooBig_failed()
    {

        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, validGenerationId);

        Boolean testSucceed = false;

        try {
            properties.DisableDevice(bigString_150chars);
        }
        catch (IllegalArgumentException e) {
            testSucceed = true;
        }

        assertTrue("Constructor do not throws IllegalArgumentException", testSucceed);
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_025: [If the provided reason is null, the DisableDevice shall throw IllegalArgumentException.] */
    @Test
    public void disableDeviceNULLReason_failed()
    {

        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, validGenerationId);

        Boolean testSucceed = false;

        try {
            properties.DisableDevice(null);
        }
        catch (IllegalArgumentException e) {
            testSucceed = true;
        }

        assertTrue("Constructor do not throws IllegalArgumentException", testSucceed);
    }

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_027: [If the device is already disabled, the DisableDevice shall not do anything.] */
    @Test
    public void disableDeviceAlreadyDisabled_succeed() throws ParseException
    {

        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, validGenerationId);

        Date expectedLowDate = new Date();
        properties.DisableDevice(validReasonToDisableDevice);
        Date expectedHighDate = new Date();

        properties.DisableDevice("new reason to disable");

        String StrDate = properties.GetStatusUpdateTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        Date returnedDate = dateFormat.parse(StrDate);

        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("2"));
        assertThat(properties.GetStatus(), is(IoTHubDeviceProperties.DeviceStatusEnum.disabled));
        assertThat(properties.GetStatusReason(), is(validReasonToDisableDevice));
        assertThat(expectedHighDate, greaterThanOrEqualTo(returnedDate));
        assertThat(expectedLowDate, lessThanOrEqualTo(returnedDate));
    }


    /**
     * fromJson/toJson
     */

    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_028: [The toJson shall create a String with information in the IoTHubDeviceProperties using json format.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_029: [The toJson shall not include null fields.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_030: [The fromJson shall fill the fields in IoTHubDeviceProperties with the values provided in the json string.] */
    /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_031: [The fromJson shall not change fields that is not reported in the json string.] */
    @Test
    public void fromJson_succeed()
    {
        final String newJson = "{\"deviceId\":\"jsonDeviceId\"," +
                "\"generationId\":\"jsonGenerationId\"," +
                "\"etag\":\"10\"," +
                "\"status\":\"disabled\"," +
                "\"statusReason\":\"device dies from unknown cause\"," +
                "\"statusUpdateTime\":\"2017-02-01T01:02:3.0004Z\"}";

        IoTHubDeviceProperties properties = new IoTHubDeviceProperties(validDeviceId, validGenerationId);
        assertThat(properties.GetDeviceId(), is(validDeviceId));
        assertThat(properties.GetGenerationId(), is(validGenerationId));
        assertThat(properties.GetETag(), is("1"));
        assertThat(properties.GetStatus(), is(IoTHubDeviceProperties.DeviceStatusEnum.enabled));
        assertThat(properties.GetStatusReason(), is("provisioned"));

        properties.fromJson(newJson);
        assertThat(properties.GetDeviceId(), is("jsonDeviceId"));
        assertThat(properties.GetGenerationId(), is("jsonGenerationId"));
        assertThat(properties.GetETag(), is("10"));
        assertThat(properties.GetStatus(), is(IoTHubDeviceProperties.DeviceStatusEnum.disabled));
        assertThat(properties.GetStatusReason(), is("device dies from unknown cause"));

        String json = properties.toJson();
        assertThat(newJson, is(json));
    }
}
