if (($env:JAVA_VERSION).equals("8"))
{
    Write-Host "Installing Java 8"
    choco install openjdk8 -y
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    Write-Host "Installing Java 8"
    choco install openjdk11 -y
}
else
{
    Write-Error "Unrecognized Java version specified: " $env:JAVA_VERSION
}

choco install maven -y

Import-Module $env:ChocolateyInstall\helpers\chocolateyProfile.psm1
refreshenv

mvn -v

mvn `-Dmaven.javadoc.skip=true --projects :iot-e2e-common `
    --also-make clean install `-DskipTests --batch-mode `-q