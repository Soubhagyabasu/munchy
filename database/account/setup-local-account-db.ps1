$ErrorActionPreference = 'Stop'

$psqlPath = 'C:\Program Files\PostgreSQL\18\bin\psql.exe'
$schemaPath = Join-Path $PSScriptRoot 'V1__create_account_schema.sql'
$verificationPath = Join-Path $PSScriptRoot 'verify_account_schema.sql'

function ConvertFrom-SecureValue {
    param([Security.SecureString]$SecureValue)

    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureValue)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
}

function Invoke-CheckedPsql {
    param([string[]]$PsqlArguments)

    & $psqlPath @PsqlArguments
    if ($LASTEXITCODE -ne 0) {
        throw "PostgreSQL command failed with exit code $LASTEXITCODE."
    }
}

if (-not (Test-Path -LiteralPath $psqlPath)) {
    throw "psql was not found at $psqlPath."
}

$postgresPassword = $null
$applicationPassword = $null

try {
    Write-Host ''
    Write-Host 'Munchy local account database setup' -ForegroundColor Cyan
    Write-Host 'Typed passwords stay only in this process and are not displayed.'
    Write-Host ''

    $postgresSecurePassword = Read-Host 'Enter the PostgreSQL postgres-user password' -AsSecureString
    $applicationSecurePassword = Read-Host 'Enter the munchy_account_app password' -AsSecureString
    $postgresPassword = ConvertFrom-SecureValue $postgresSecurePassword
    $applicationPassword = ConvertFrom-SecureValue $applicationSecurePassword

    $env:PGPASSWORD = $postgresPassword

    $roleExists = & $psqlPath -X -qAt -h localhost -p 5432 -U postgres -d postgres -v ON_ERROR_STOP=1 -c "SELECT 1 FROM pg_roles WHERE rolname = 'munchy_account_app';"
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not authenticate as postgres. Check the postgres-user password.'
    }
    if (($roleExists | Out-String).Trim() -ne '1') {
        throw 'The munchy_account_app role does not exist on localhost:5432. Create it in pgAdmin first.'
    }

    $databaseExists = & $psqlPath -X -qAt -h localhost -p 5432 -U postgres -d postgres -v ON_ERROR_STOP=1 -c "SELECT 1 FROM pg_database WHERE datname = 'munchy_account';"
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not check whether the munchy_account database exists.'
    }

    if (($databaseExists | Out-String).Trim() -ne '1') {
        Write-Host 'Creating munchy_account...' -ForegroundColor Yellow
        Invoke-CheckedPsql -PsqlArguments @(
            '-X', '-h', 'localhost', '-p', '5432', '-U', 'postgres', '-d', 'postgres',
            '-v', 'ON_ERROR_STOP=1',
            '-c', 'CREATE DATABASE munchy_account OWNER munchy_account_app;'
        )
    }
    else {
        Write-Host 'munchy_account already exists; keeping it.' -ForegroundColor Yellow
        Invoke-CheckedPsql -PsqlArguments @(
            '-X', '-h', 'localhost', '-p', '5432', '-U', 'postgres', '-d', 'postgres',
            '-v', 'ON_ERROR_STOP=1',
            '-c', 'ALTER DATABASE munchy_account OWNER TO munchy_account_app;'
        )
    }

    $env:PGPASSWORD = $applicationPassword

    Write-Host 'Creating the account schema and tables...' -ForegroundColor Yellow
    Invoke-CheckedPsql -PsqlArguments @(
        '-X', '-h', 'localhost', '-p', '5432', '-U', 'munchy_account_app', '-d', 'munchy_account',
        '-v', 'ON_ERROR_STOP=1', '-f', $schemaPath
    )

    Write-Host ''
    Write-Host 'Verifying tables, roles, and relationships...' -ForegroundColor Yellow
    Invoke-CheckedPsql -PsqlArguments @(
        '-X', '-h', 'localhost', '-p', '5432', '-U', 'munchy_account_app', '-d', 'munchy_account',
        '-v', 'ON_ERROR_STOP=1', '-f', $verificationPath
    )

    Write-Host ''
    Write-Host 'SUCCESS: munchy_account and its user-side tables are ready.' -ForegroundColor Green
}
catch {
    Write-Host ''
    Write-Host "SETUP FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
finally {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    $postgresPassword = $null
    $applicationPassword = $null
    Write-Host ''
    Read-Host 'Press Enter to close this window'
}
