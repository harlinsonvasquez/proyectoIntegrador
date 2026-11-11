param(
    [string] $Database = 'gestion_estudiantil',
    [string] $PgHost = '127.0.0.1',
    [int] $Port = 5432,
    [string] $Username = 'postgres',
    [string] $BackupDirectory = (Join-Path $PSScriptRoot '..\backups'),
    [ValidateRange(1, 3650)] [int] $RetentionDays = 30
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'postgres-tools.ps1')
$pgDump = Get-PostgresExecutable 'pg_dump'
$destination = [IO.Path]::GetFullPath($BackupDirectory)
New-Item -ItemType Directory -Force -Path $destination | Out-Null
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupPath = Join-Path $destination "$Database-$stamp.backup"
$arguments = Get-PostgresConnectionArguments $PgHost $Port $Username
$arguments += @('--format=custom', '--no-owner', '--no-acl', "--file=$backupPath", $Database)
& $pgDump @arguments
if ($LASTEXITCODE -ne 0) { throw "pg_dump terminó con código $LASTEXITCODE." }
Get-ChildItem -LiteralPath $destination -Filter "$Database-*.backup" -File |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$RetentionDays) } |
    Remove-Item -Force
Write-Output "Copia creada: $backupPath"
