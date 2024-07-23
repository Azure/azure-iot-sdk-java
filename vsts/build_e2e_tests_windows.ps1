if (($env:JAVA_VERSION).equals("8"))
{
    choco install openjdk8 -y
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    choco install openjdk11 -y
}

choco install maven -y

Import-Module $env:ChocolateyInstall\helpers\chocolateyProfile.psm1
refreshenv

mvn -v

mvn `-Dmaven.javadoc.skip=true --projects :iot-e2e-common `
    --also-make clean install `-DskipTests --batch-mode `-q