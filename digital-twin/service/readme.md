# Digital Twin Service Client
> see https://aka.ms/autorest

## Getting Started
To build the Digital; Twin Service Client, simply install AutoRest in Node.js via `npm` (`npm install -g autorest`) and then naviogate to this folder and run:

`autorest`

It will pick the configuration options mentioned below and output the generated files.

To see additional help and options, run:
`autorest --help`

For other options on installation see [Installing Autorest](https://aka.ms/autorest/install) on the AutoRest GitHub page.

### Adding dependency

Before building the project, add dependency to the Java Client Runtime for AutoRest in your `pom.xml`.

```xml
<dependencies>
    <!-- https://mvnrepository.com/artifact/com.microsoft.rest/client-runtime -->
    <dependency>
        <groupId>com.microsoft.rest</groupId>
        <artifactId>client-runtime</artifactId>
        <version>1.6.12</version>
    </dependency>
</dependencies>
```

---

## Configuration
The following settings are used for generating the protocol layer API with AutoRest:

```yaml
input-file: serviceDigitalTwinOnly.json

java:
    namespace: com.microsoft.azure.sdk.iot.digitaltwin.service.generated
    output-folder: ./
    add-credentials: true
```