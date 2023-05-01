if (($env:JAVA_VERSION).equals("8"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_8_X64
    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', $env:JAVA_HOME_8_X64, 'User')
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_11_X64
    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', $env:JAVA_HOME_11_X64, 'User')
}

mvn -v