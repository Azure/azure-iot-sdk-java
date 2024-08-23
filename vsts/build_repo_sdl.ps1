﻿choco install maven -y
choco install openjdk8 -y

Import-Module $env:ChocolateyInstall\helpers\chocolateyProfile.psm1
refreshenv

Write-Host "Java home value: " ($env:JAVA_HOME)

mvn -v

mvn install `-Dmaven.test.skip=true `-Dmaven.javadoc.skip=true
