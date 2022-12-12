if (($env:JAVA_VERSION).equals("8"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_8_X64
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_11_X64
}

mvn -v