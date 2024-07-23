choco install maven

if (($env:JAVA_VERSION).equals("8"))
{
    choco install openjdk8 -y
    $env:JAVA_HOME=$env:JAVA_HOME_8_X64
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    choco install openjdk11 -y
    $env:JAVA_HOME=$env:JAVA_HOME_11_X64
}

Import-Module $env:ChocolateyInstall\helpers\chocolateyProfile.psm1
refreshenv

mvn -v

mvn -DRUN_PROVISIONING_TESTS="$Env:runProvisioningTests" -DRUN_DIGITAL_TESTS="$Env:runDigitalTwinTests" -DRUN_IOTHUB_TESTS="$Env:runIotHubTests" -DIS_PULL_REQUEST="$isPullRequestBuild" install -T 2C -DskipUnitTests --batch-mode -q
