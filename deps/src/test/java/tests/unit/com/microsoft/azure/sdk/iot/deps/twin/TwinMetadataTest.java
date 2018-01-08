// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.twin;

import com.microsoft.azure.sdk.iot.deps.twin.TwinMetadata;
import mockit.Deencapsulation;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.deps.Helpers;

import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Unit tests for the TwinMetadata
 * 100% methods, 100% lines covered
 */
public class TwinMetadataTest
{
    /* SRS_TWIN_METADATA_21_001: [The constructor shall parse the provided `lastUpdated` String to the Date and store it as the TwinMetadata lastUpdated.] */
    @Test
    public void constructorParseLastUpdatedSucceed()
    {
        // arrange
        String lastUpdated = "2017-09-21T02:07:44.238Z";

        // act
        TwinMetadata twinMetadata = Deencapsulation.newInstance(TwinMetadata.class, new Class[] {String.class, Integer.class}, lastUpdated, 5);

        // assert
        assertNotNull(twinMetadata);
        Helpers.assertDateWithError((Date)Deencapsulation.getField(twinMetadata, "lastUpdated"), lastUpdated);
    }

    /* SRS_TWIN_METADATA_21_001: [The constructor shall parse the provided `lastUpdated` String to the Date and store it as the TwinMetadata lastUpdated.] */
    @Test
    public void constructorLastUpdatedNullSucceed()
    {
        // arrange
        String lastUpdated = null;

        // act
        TwinMetadata twinMetadata = Deencapsulation.newInstance(TwinMetadata.class, new Class[] {String.class, Integer.class}, lastUpdated, 5);

        // assert
        assertNotNull(twinMetadata);
        assertNull(Deencapsulation.getField(twinMetadata, "lastUpdated"));
    }

    /* SRS_TWIN_METADATA_21_001: [The constructor shall parse the provided `lastUpdated` String to the Date and store it as the TwinMetadata lastUpdated.] */
    @Test
    public void constructorLastUpdatedEmptySucceed()
    {
        // arrange
        String lastUpdated = "";

        // act
        TwinMetadata twinMetadata = Deencapsulation.newInstance(TwinMetadata.class, new Class[] {String.class, Integer.class}, lastUpdated, 5);

        // assert
        assertNotNull(twinMetadata);
        assertNull(Deencapsulation.getField(twinMetadata, "lastUpdated"));
    }

    /* SRS_TWIN_METADATA_21_002: [The constructor shall throw IllegalArgumentException if it cannot convert the provided `lastUpdated` String to Date.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorLastUpdatedInvalidSucceed()
    {
        // arrange
        String lastUpdated = "This is a invalid date";

        // act
        Deencapsulation.newInstance(TwinMetadata.class, new Class[] {String.class, Integer.class}, lastUpdated, 5);

        // assert
    }

    /* SRS_TWIN_METADATA_21_003: [The constructor shall store the provided lastUpdatedVersion as is.] */
    @Test
    public void constructorPositiveLastUpdatedVersionSucceed()
    {
        // arrange
        Integer lastUpdatedVersion = 5;

        // act
        TwinMetadata twinMetadata = Deencapsulation.newInstance(TwinMetadata.class, new Class[] {String.class, Integer.class}, "2017-09-21T02:07:44.238Z", lastUpdatedVersion);

        // assert
        assertNotNull(twinMetadata);
        assertEquals(lastUpdatedVersion, Deencapsulation.getField(twinMetadata, "lastUpdatedVersion"));
    }

    /* SRS_TWIN_METADATA_21_003: [The constructor shall store the provided lastUpdatedVersion as is.] */
    @Test
    public void constructorNegativeLastUpdatedVersionSucceed()
    {
        // arrange
        Integer lastUpdatedVersion = -5;

        // act
        TwinMetadata twinMetadata = Deencapsulation.newInstance(TwinMetadata.class, new Class[] {String.class, Integer.class}, "2017-09-21T02:07:44.238Z", lastUpdatedVersion);

        // assert
        assertNotNull(twinMetadata);
        assertEquals(lastUpdatedVersion, Deencapsulation.getField(twinMetadata, "lastUpdatedVersion"));
    }

    /* SRS_TWIN_METADATA_21_003: [The constructor shall store the provided lastUpdatedVersion as is.] */
    @Test
    public void constructorLastUpdatedVersionNullSucceed()
    {
        // arrange
        Integer lastUpdatedVersion = null;

        // act
        TwinMetadata twinMetadata = Deencapsulation.newInstance(TwinMetadata.class, new Class[] {String.class, Integer.class}, "2017-09-21T02:07:44.238Z", lastUpdatedVersion);

        // assert
        assertNotNull(twinMetadata);
        assertNull(Deencapsulation.getField(twinMetadata, "lastUpdatedVersion"));
    }

    /* SRS_TWIN_METADATA_21_004: [The tryExtractFromMap shall return null if the provided metadata is not a Map.] */
    @Test
    public void tryExtractFromMapNotMapSucceed()
    {
        // arrange
        Object metadata = "This is not a Map";

        // act
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
        assertNull(twinMetadata);
    }

    /* SRS_TWIN_METADATA_21_005: [If the provide metadata contains date or version, the tryExtractFromMap shall return a new instance of TwinMetadata with this information.] */
    @Test
    public void tryExtractFromMapDateSucceed()
    {
        // arrange
        final String lastUpdated = "2017-09-21T02:07:44.238Z";
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
            }
        };

        // act
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
        assertNotNull(twinMetadata);
        Helpers.assertDateWithError((Date)Deencapsulation.getField(twinMetadata, "lastUpdated"), lastUpdated);
    }

    /* SRS_TWIN_METADATA_21_005: [If the provide metadata contains date or version, the tryExtractFromMap shall return a new instance of TwinMetadata with this information.] */
    @Test
    public void tryExtractFromMapVersionSucceed()
    {
        // arrange
        final Integer lastUpdatedVersion = 10;
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdatedVersion", lastUpdatedVersion);
                put("key2", "value2");
            }
        };

        // act
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
        assertNotNull(twinMetadata);
        assertEquals(lastUpdatedVersion, Deencapsulation.getField(twinMetadata, "lastUpdatedVersion"));
    }

    /* SRS_TWIN_METADATA_21_005: [If the provide metadata contains date or version, the tryExtractFromMap shall return a new instance of TwinMetadata with this information.] */
    @Test
    public void tryExtractFromMapDateAndVersionSucceed()
    {
        // arrange
        final String lastUpdated = "2017-09-21T02:07:44.238Z";
        final Integer lastUpdatedVersion = 10;
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
                put("$lastUpdatedVersion", lastUpdatedVersion);
            }
        };

        // act
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
        assertNotNull(twinMetadata);
        Helpers.assertDateWithError((Date)Deencapsulation.getField(twinMetadata, "lastUpdated"), lastUpdated);
        assertEquals(lastUpdatedVersion, Deencapsulation.getField(twinMetadata, "lastUpdatedVersion"));
    }

    /* SRS_TWIN_METADATA_21_005: [If the provide metadata contains date or version, the tryExtractFromMap shall return a new instance of TwinMetadata with this information.] */
    @Test
    public void tryExtractFromMapMinDateAndVersionSucceed()
    {
        // arrange
        final String lastUpdated = "0000-00-00T00:00:00.000Z";
        final Integer lastUpdatedVersion = 10;
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
                put("$lastUpdatedVersion", lastUpdatedVersion);
            }
        };

        // act
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
        assertNotNull(twinMetadata);
        Helpers.assertDateWithError((Date)Deencapsulation.getField(twinMetadata, "lastUpdated"), lastUpdated);
        assertEquals(lastUpdatedVersion, Deencapsulation.getField(twinMetadata, "lastUpdatedVersion"));
    }

    /* SRS_TWIN_METADATA_21_005: [If the provide metadata contains date or version, the tryExtractFromMap shall return a new instance of TwinMetadata with this information.] */
    @Test
    public void tryExtractFromMapNoMetadataSucceed()
    {
        // arrange
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };

        // act
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
        assertNull(twinMetadata);
    }

    /* SRS_TWIN_METADATA_21_006: [The tryExtractFromMap shall throw IllegalArgumentException if it cannot convert the provided `lastUpdated` String to Date or the version in a Number.] */
    @Test (expected = IllegalArgumentException.class)
    public void tryExtractFromMapValidDateAndInvalidVersionSucceed()
    {
        // arrange
        final String lastUpdated = "2017-09-21T02:07:44.238Z";
        final String lastUpdatedVersion = "This is not a Number";
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
                put("$lastUpdatedVersion", lastUpdatedVersion);
            }
        };

        // act
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
        assertNotNull(twinMetadata);
        Helpers.assertDateWithError((Date)Deencapsulation.getField(twinMetadata, "lastUpdated"), lastUpdated);
        assertNull(Deencapsulation.getField(twinMetadata, "lastUpdatedVersion"));
    }

    /* SRS_TWIN_METADATA_21_006: [The tryExtractFromMap shall throw IllegalArgumentException if it cannot convert the provided `lastUpdated` String to Date or the version in a Number.] */
    @Test (expected = IllegalArgumentException.class)
    public void tryExtractFromMapThrowsOnInvalidDateAndValidVersion()
    {
        // arrange
        final String lastUpdated = "This is not a valid date";
        final Integer lastUpdatedVersion = 10;
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
                put("$lastUpdatedVersion", lastUpdatedVersion);
            }
        };

        // act
        Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
    }

    /* SRS_TWIN_METADATA_21_006: [The tryExtractFromMap shall throw IllegalArgumentException if it cannot convert the provided `lastUpdated` String to Date or the version in a Number.] */
    @Test (expected = IllegalArgumentException.class)
    public void tryExtractFromMapInvalidDateAndInvalidVersionSucceed()
    {
        // arrange
        final String lastUpdated = "This is not a date";
        final String lastUpdatedVersion = "This is not a Number";
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
                put("$lastUpdatedVersion", lastUpdatedVersion);
            }
        };

        // act
        Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // assert
    }

    /* SRS_TWIN_METADATA_21_007: [The getLastUpdatedVersion shall return the stored lastUpdatedVersion.] */
    /* SRS_TWIN_METADATA_21_008: [The getLastUpdated shall return the stored lastUpdated.] */
    @Test
    public void gettersSucceed()
    {
        // arrange
        final String lastUpdated = "2017-09-21T02:07:44.238Z";
        final Integer lastUpdatedVersion = 10;
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
                put("$lastUpdatedVersion", lastUpdatedVersion);
            }
        };
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // act - assert
        Helpers.assertDateWithError(twinMetadata.getLastUpdated(), lastUpdated);
        assertEquals(lastUpdatedVersion, twinMetadata.getLastUpdatedVersion());
    }

    /* SRS_TWIN_METADATA_21_009: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementSucceed()
    {
        // arrange
        final String lastUpdated = "2017-09-21T02:07:44.238Z";
        final Integer lastUpdatedVersion = 10;
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
                put("$lastUpdatedVersion", lastUpdatedVersion);
            }
        };
        String expectedJson = "{\"$lastUpdated\":\"" + lastUpdated + "\",\"$lastUpdatedVersion\":" + lastUpdatedVersion + "}";
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // act - assert
        Helpers.assertJson(Deencapsulation.invoke(twinMetadata, "toJsonElement").toString(), expectedJson);
    }

    /* SRS_TWIN_METADATA_21_009: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementNoDateSucceed()
    {
        // arrange
        final Integer lastUpdatedVersion = 10;
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("key2", "value2");
                put("$lastUpdatedVersion", lastUpdatedVersion);
            }
        };
        String expectedJson = "{\"$lastUpdatedVersion\":" + lastUpdatedVersion + "}";
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // act - assert
        Helpers.assertJson(Deencapsulation.invoke(twinMetadata, "toJsonElement").toString(), expectedJson);
    }

    /* SRS_TWIN_METADATA_21_009: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementNoVersionSucceed()
    {
        // arrange
        final String lastUpdated = "2017-09-21T02:07:44.238Z";
        Object metadata = new HashMap<String, Object>()
        {
            {
                put("key1", "value1");
                put("$lastUpdated", lastUpdated);
                put("key2", "value2");
            }
        };
        String expectedJson = "{\"$lastUpdated\":\"" + lastUpdated + "\"}";
        TwinMetadata twinMetadata = Deencapsulation.invoke(TwinMetadata.class, "tryExtractFromMap", new Class[] {Object.class}, metadata);

        // act - assert
        Helpers.assertJson(Deencapsulation.invoke(twinMetadata, "toJsonElement").toString(), expectedJson);
    }

}
