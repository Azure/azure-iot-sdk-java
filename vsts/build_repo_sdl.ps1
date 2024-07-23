choco install maven -y

if (($env:JAVA_VERSION).equals("8"))
{
    choco install openjdk8 -y
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    choco install openjdk11 -y
}

Import-Module $env:ChocolateyInstall\helpers\chocolateyProfile.psm1
refreshenv

mvn -v

mvn install `-Dmaven.test.skip=true `-Dmaven.javadoc.skip=true
