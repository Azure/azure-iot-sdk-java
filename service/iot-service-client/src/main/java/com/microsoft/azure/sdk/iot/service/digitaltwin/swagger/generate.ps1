# TEMPORARY: Until https://msazure.visualstudio.com/One/_workitems/edit/8354260 is resolved.
# Update the swagger json file to accept payload as "optional" for invoke command APIs.
$swaggerJson = Get-Content .\digitalTwin.json -raw | ConvertFrom-Json
$commandRestPath = '/digitaltwins/{id}/commands/{commandName}'
$commandParameters = $swaggerJson.paths.$commandRestPath.post.parameters
Foreach ($parameter in $commandParameters) {
    if ($parameter.name -eq 'payload') {
        $parameter.required = $false
    }
}
$componentCommandRestPath = '/digitaltwins/{id}/components/{componentPath}/commands/{commandName}'
$componentCommandParameters = $swaggerJson.paths.$componentCommandRestPath.post.parameters
Foreach ($parameter in $componentCommandParameters) {
    if ($parameter.name -eq 'payload') {
        $parameter.required = $false
    }
}
$swaggerJson | ConvertTo-Json -Depth 10 | Set-Content .\DigitalTwin.json

# Generate code using autorest and replace necessary names
autorest $PSScriptRoot/readme.md
