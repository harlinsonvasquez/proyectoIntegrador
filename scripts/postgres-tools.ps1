function Get-PostgresExecutable([string] $Name) {
    if ($env:POSTGRES_BIN) {
        $candidate = Join-Path $env:POSTGRES_BIN "$Name.exe"
        if (Test-Path -LiteralPath $candidate) { return $candidate }
    }
    $defaultBin = 'C:\Program Files\PostgreSQL\16\bin'
    $candidate = Join-Path $defaultBin "$Name.exe"
    if (Test-Path -LiteralPath $candidate) { return $candidate }
    $command = Get-Command "$Name.exe" -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    throw "No encuentro $Name.exe. Define POSTGRES_BIN con la carpeta bin de PostgreSQL."
}

function Get-PostgresConnectionArguments([string] $PgHost, [int] $Port, [string] $Username) {
    return @("--host=$PgHost", "--port=$Port", "--username=$Username", '--no-password')
}
