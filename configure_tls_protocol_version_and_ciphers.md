# Configure TLS Protocol Version and Ciphers

## SDK
### How to configure TLS version
Use `jdk.tls.client.protocols` system property to enforce TLS version. This property was introduced to JDK7 `7u95` and to JDK6 `6u121`.
```
$ java -Djdk.tls.client.protocols="TLSv1.2" yourApp
```

### How to configure TLS ciphers
Use `jdk.tls.disabledAlgorithms` property of `java.security` file to disable unsupported ciphers.
On JDK 8 and earlier, `java.security` file is under folder `%JAVA_HOME%/lib/security`, for JDK 9, it’s  under folder `%JAVA_HOME%/conf/security`

## Android
To be done

## References
* [Additional information on Oracle's JDK and JRE Cryptographic Algorithms](https://www.java.com/en/configure_crypto.html)