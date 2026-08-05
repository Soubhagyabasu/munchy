$ErrorActionPreference = 'Stop'

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

$plainPassword = $null
try {
    Write-Host ''
    Write-Host 'Configure the Munchy Account Service database password' -ForegroundColor Cyan
    $securePassword = Read-Host 'Enter the munchy_account_app password' -AsSecureString
    $plainPassword = ConvertFrom-SecureValue $securePassword
    if ([string]::IsNullOrWhiteSpace($plainPassword)) {
        throw 'The password cannot be empty.'
    }
    [Environment]::SetEnvironmentVariable(
        'MUNCHY_ACCOUNT_DB_PASSWORD',
        $plainPassword,
        'User'
    )
    Write-Host ''
    Write-Host 'MUNCHY_ACCOUNT_DB_PASSWORD is configured for your Windows user.' -ForegroundColor Green
}
catch {
    Write-Host ''
    Write-Host "CONFIGURATION FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
finally {
    $plainPassword = $null
    Write-Host ''
    Read-Host 'Press Enter to close this window'
}
