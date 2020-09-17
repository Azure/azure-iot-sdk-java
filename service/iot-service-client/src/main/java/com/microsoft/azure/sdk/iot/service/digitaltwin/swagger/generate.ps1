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

$generatedFolder = (Get-Item $PSScriptRoot).Parent | Join-Path -ChildPath "generated"

Get-ChildItem $generatedFolder -File -Recurse |
Foreach-Object {
    # Update content
    (Get-Content $_.FullName).replace('DigitalTwinGetDigitalTwinHeaders', 'DigitalTwinGetHeaders') | Set-Content $_.FullName
    (Get-Content $_.FullName).replace('DigitalTwinUpdateDigitalTwinHeaders', 'DigitalTwinUpdateHeaders') | Set-Content $_.FullName

    # Update file names
    if($_.Name -eq 'DigitalTwinGetDigitalTwinHeaders.java')
    {
        Move-Item -Force -Path $_.FullName $_.FullName.Replace('DigitalTwinGetDigitalTwinHeaders.java', 'DigitalTwinGetHeaders.java')
    }
    if($_.Name -eq 'DigitalTwinUpdateDigitalTwinHeaders.java')
    {
        Move-Item -Force -Path $_.FullName $_.FullName.Replace('DigitalTwinUpdateDigitalTwinHeaders.java', 'DigitalTwinUpdateHeaders.java')
    }
}