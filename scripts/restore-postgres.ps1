param(
    [Parameter(Mandatory = $true)] [string] $BackupPath,
    [Parameter(Mandatory = $true)] [string] $TargetDatabase,
    [string] $PgHost = '127.0.0.1',
    [int] $Port = 5432,
    [string] $Username = 'postgres'
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'postgres-tools.ps1')
$archive = (Resolve-Path -LiteralPath $BackupPath).Path
$confirmation = Read-Host "Esto reemplazará los objetos existentes en '$TargetDatabase'. Escribe RESTAURAR $TargetDatabase para continuar"
if ($confirmation -cne "RESTAURAR $TargetDatabase") { throw 'Restauración cancelada.' }
$pgRestore = Get-PostgresExecutable 'pg_restore'
$arguments = Get-PostgresConnectionArguments $PgHost $Port $Username
$arguments += @('--clean', '--if-exists', '--no-owner', '--no-acl', "--dbname=$TargetDatabase", $archive)
& $pgRestore @arguments
if ($LASTEXITCODE -ne 0) { throw "pg_restore terminó con código $LASTEXITCODE." }
Write-Output "Restauración completada en '$TargetDatabase'."
