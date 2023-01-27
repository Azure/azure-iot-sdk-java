// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tools.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderDiceEmulator;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Provisioning X509 Cert generator to generate X509 certificates using DICE emulator
 */
public class ProvisioningX509CertGen
{
    public static void main(String[] args)
    {
        try
        {
            String aliasCertCnName, rootCertCnName, signerCertCnName;
            SecurityProviderDiceEmulator securityClient;
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
            System.out.println("Do you want to input common name : Y/N(use default)");
            String isCnName = scanner.next();
            if (isCnName.equalsIgnoreCase("Y"))
            {
                System.out.println("Input Client Cert commonName :");
                aliasCertCnName = scanner.next();

                System.out.println("Input Root Cert commonName :");
                rootCertCnName = scanner.next();

                System.out.println("Input Signer Cert commonName :");
                signerCertCnName = scanner.next();

                securityClient = new SecurityProviderDiceEmulator(aliasCertCnName, signerCertCnName, rootCertCnName);
            }
            else
            {
                securityClient = new SecurityProviderDiceEmulator();
            }
            System.out.println("Your registration Id is : " + securityClient.getRegistrationId());
            System.out.println("Client Cert");
            System.out.println(securityClient.getAliasCertPem());
            System.out.println("Client Cert Private Key");
            System.out.println(securityClient.getAliasCertPrivateKeyPem());
            System.out.println("Signer (Intermediate) Cert");
            System.out.println(securityClient.getSignerCertPem());
            System.out.println("Root Cert");
            System.out.println(securityClient.getRootCertPem());
            System.out.println("Do you want to input Verification Code Y/N");

            String isVerify = scanner.next();
            try
            {
                if (isVerify.equalsIgnoreCase("Y"))
                {
                    System.out.println("Input Verification Code");
                    scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
                    {
                        String verificationCode = scanner.next();
                        if (verificationCode != null)
                        {
                            System.out.println("Verification Cert");
                            System.out.println(securityClient.generateLeafCert(verificationCode));
                        }
                    }
                }
            }
            catch (SecurityProviderException e)
            {
                e.printStackTrace();
            }
            finally
            {
                scanner.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Shutting down...");
        }
    }
}
