/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.IotHubX509;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Tests IotHubX509
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubX509Test
{
    @Mocked
    Paths mockPaths;

    @Mocked
    Files mockFiles;

    @Mocked
    File mockPublicKeyCertFile;

    @Mocked
    File mockPrivateKeyFile;

    @Mocked
    Path mockedPublicKeyCertPath;

    @Mocked
    Path mockedPrivateKeyPath;

    private static final String someCert = "someCert";
    private static final String someKey = "someKey";

    private static final String someCertPath = "someCertPath";
    private static final String someKeyPath = "someKeyPath";

    //Tests_SRS_IOTHUBX509_34_001: [If the provided public key certificate or private key is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyPublicKey() throws IOException
    {
        //act
        Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, "", false, someKey, false);
    }

    //Tests_SRS_IOTHUBX509_34_001: [If the provided public key certificate or private key is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyPrivateKey() throws IOException
    {
        //act
        Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class},someCert, false, "", false);
    }

    //Tests_SRS_IOTHUBX509_34_001: [If the provided public key certificate or private key is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullPublicKey() throws IOException
    {
        //act
        Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, null, false, someKey, false);
    }

    //Tests_SRS_IOTHUBX509_34_001: [If the provided public key certificate or private key is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullPrivateKey() throws IOException
    {
        //act
        Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, someCert, false, null, false);
    }

    //Tests_SRS_IOTHUBX509_34_017: [If the public key certificate was provided as a path in the constructor, this function shall read the public key certificate from its file.]
    //Tests_SRS_IOTHUBX509_34_018: [This function shall return the saved public key certificate string.]
    @Test
    public void publicKeyGetter() throws IOException
    {
        //arrange
        fileReadExpectations();
        IotHubX509 iotHubX509 = Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, someCertPath, true, someKey, false);

        //act
        String actualPublicKeyCert = Deencapsulation.invoke(iotHubX509, "getPublicKeyCertificate");

        //assert
        assertEquals(someCert, actualPublicKeyCert);
    }

    //Tests_SRS_IOTHUBX509_34_019: [If the private key was provided as a path in the constructor, this function shall read the private key from its file.]
    //Tests_SRS_IOTHUBX509_34_020: [This function shall return the saved private key string.]
    @Test
    public void privateKeyGetter() throws IOException
    {
        //arrange
        fileReadExpectations();
        IotHubX509 iotHubX509 = Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, someCert, false, someKeyPath, true);

        //act
        String actualPrivateKey = Deencapsulation.invoke(iotHubX509, "getPrivateKey");

        //assert
        assertEquals(someKey, actualPrivateKey);
    }

    //Tests_SRS_IOTHUBX509_34_015: [If a path is provided for the private key, the path will be saved and the contents of the file shall be read and saved as a string.]
    @Test
    public void testConstructorWithPathForKey()
    {
        //act
        IotHubX509 iotHubX509 = Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, someCert, false, someKeyPath, true);

        //assert
        String actualKeyPath = Deencapsulation.getField(iotHubX509, "pathToPrivateKey");
        assertEquals(someKeyPath, actualKeyPath);
    }

    //Tests_SRS_IOTHUBX509_34_013: [If a path is provided for the public key certificate, the path will be saved and the contents of the file shall be read and saved as a string.]
    @Test
    public void testConstructorWithPathForCert()
    {
        //act
        IotHubX509 iotHubX509 = Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, someCertPath, true, someKey, false);

        //assert
        String actualCertPath = Deencapsulation.getField(iotHubX509, "pathToPublicKeyCertificate");
        assertEquals(someCertPath, actualCertPath);
    }

    //Tests_SRS_IOTHUBX509_34_014: [If the public key certificate is not provided as a path, no path will be saved and the value of the public key certificate will be saved as a string.]
    @Test
    public void testConstructorWithStringForCert()
    {
        //act
        IotHubX509 iotHubX509 = Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, someCert, false, someKeyPath, true);

        //assert
        String actualPublicKeyCert = Deencapsulation.getField(iotHubX509, "publicKeyCertificate");
        assertEquals(someCert, actualPublicKeyCert);
    }

    //Tests_SRS_IOTHUBX509_34_016: [If the private key is not provided as a path, no path will be saved and the value of the private key will be saved as a string.]
    @Test
    public void testConstructorWithStringForKey()
    {
        //act
        IotHubX509 iotHubX509 = Deencapsulation.newInstance(IotHubX509.class, new Class[] {String.class, boolean.class, String.class, boolean.class}, someCertPath, true, someKey, false);

        //assert
        String actualPrivateKey = Deencapsulation.getField(iotHubX509, "privateKey");
        assertEquals(someKey, actualPrivateKey);
    }

    private void fileReadExpectations() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                Paths.get(someCertPath);
                result = mockedPublicKeyCertPath;

                Files.readAllBytes(mockedPublicKeyCertPath);
                result = someCert.getBytes(StandardCharsets.UTF_8);

                Paths.get(someKeyPath);
                result = mockedPrivateKeyPath;

                Files.readAllBytes(mockedPrivateKeyPath);
                result = someKey.getBytes(StandardCharsets.UTF_8);
            }
        };
    }
}
