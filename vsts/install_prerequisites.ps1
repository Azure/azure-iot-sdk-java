if (($env:JAVA_VERSION).equals("8"))
{
    choco install jdk8 -y
    $env:JAVA_HOME=$env:JAVA_HOME_8_X64
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    choco install jdk11 -y
    $env:JAVA_HOME=$env:JAVA_HOME_11_X64
}

choco install maven -y

refreshenv



