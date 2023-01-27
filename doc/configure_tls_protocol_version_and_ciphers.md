# Configure TLS Protocol Version and Ciphers

When using the Azure IoT Java SDK in your application, you may wish to control which version of TLS is used and which ciphers are used by TLS.

## How to configure TLS version

To restrict the TLS version, one should use the **jdk.tls.client.protocols** system property. This property was introduced to JDK7 **7u95** and to JDK6 **6u121**.

To restrict to the _most secure_ version (e.g. **TLS 1.2**) one should configure explicitly to that version.

```
$ java -Djdk.tls.client.protocols="TLSv1.2" yourApp
```

## How to configure TLS ciphers

Additionally, if one wishes to control which ciphers are employed by TLS when used by their application, one should use the **jdk.tls.disabledAlgorithms** property of the **java.security** file. This file works as a blacklist, disabling unsupported ciphers.

On JDK 8 and earlier, the **java.security** file is under folder **%JAVA_HOME%/lib/security**. For JDK 9, it’s under folder **%JAVA_HOME%/conf/security**.

To see syntax examples on disabling ciphers, see [Oracle's JDK and JRE Cryptographic Algorithms] section **Disable the TLS DES cipher suites**.

## References

- Additional information on [Oracle's JDK and JRE Cryptographic Algorithms]


[Oracle's JDK and JRE Cryptographic Algorithms]: https://www.java.com/en/configure_crypto.html