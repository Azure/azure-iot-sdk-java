mvn clean install -DskipTests -T 2C
cd iot-e2e-tests\longhaul

mvn -DRUN_LONGHAUL_TESTS=true '-Dtest=tests.integration.com.microsoft.azure.sdk.iot.longhaul.*Tests' surefire:test
