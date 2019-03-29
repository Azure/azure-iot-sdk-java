﻿$pr = $false
if ($env:COMMIT_FROM -match "^[\d]+$") 
{
    Write-Host -ForegroundColor Cyan "PR #$($env:COMMIT_FROM)"
    $pr = $true
}
else
{
    Write-Host -ForegroundColor Cyan "BRANCH $($env:COMMIT_FROM)"
}