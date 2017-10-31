// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tools.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityClientDiceEmulator;

import java.util.Scanner;

/**
 * Provisioning Sample for DICE
 */
public class ProvisioningDiceCertGen
{
    public static void main(String[] args)
    {
        try
        {
            String aliasCertCnName, rootCertCnName, signerCertCnName = null;
            SecurityClientDiceEmulator securityClient = null;
            Scanner scanner = new Scanner(System.in);
            System.out.println("Do you want to input common name : Y/N(use default)");
            String isCnName = scanner.next();
            if (isCnName.equalsIgnoreCase("Y"))
            {
                System.out.println("Input Alias Cert commonName :");
                scanner = new Scanner(System.in);
                aliasCertCnName = scanner.next();

                System.out.println("Input Root Cert commonName :");
                scanner = new Scanner(System.in);
                rootCertCnName = scanner.next();

                System.out.println("Input Signer Cert commonName :");
                scanner = new Scanner(System.in);
                signerCertCnName = scanner.next();

                securityClient = new SecurityClientDiceEmulator(aliasCertCnName, signerCertCnName, rootCertCnName);
            }
            else
            {
                securityClient = new SecurityClientDiceEmulator();
            }

            System.out.println("Alias Cert");
            System.out.println(securityClient.getAliasCertPem());
            System.out.println("Root Cert");
            System.out.println(securityClient.getRootCertPem());
            System.out.println("Do you want to input Verification Code Y/N");

            scanner = new Scanner(System.in);
            String isVerify = scanner.next();
            try
            {
                if (isVerify.equalsIgnoreCase("Y"))
                {
                    System.out.println("Input Verification Code");
                    scanner = new Scanner(System.in);
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
            catch (SecurityClientException e)
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
