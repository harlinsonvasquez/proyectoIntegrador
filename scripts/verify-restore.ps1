param(
    [Parameter(Mandatory = $true)] [string] $BackupPath,
    [string] $SourceDatabase = 'gestion_estudiantil',
    [string] $PgHost = '127.0.0.1',
    [int] $Port = 5432,
    [string] $Username = 'postgres'
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'postgres-tools.ps1')
$archive = (Resolve-Path -LiteralPath $BackupPath).Path
$createdb = Get-PostgresExecutable 'createdb'
$dropdb = Get-PostgresExecutable 'dropdb'
$pgRestore = Get-PostgresExecutable 'pg_restore'
$psql = Get-PostgresExecutable 'psql'
$temporaryDatabase = 'pi_restore_verify_' + (Get-Date -Format 'yyyyMMddHHmmss') + '_' + ([guid]::NewGuid().ToString('N').Substring(0, 6))
$connection = Get-PostgresConnectionArguments $PgHost $Port $Username
$tables = @(
    'rol', 'usuario', 'programa', 'estudiante', 'docente', 'plan_estudio', 'asignatura',
    'plan_asignatura', 'prerrequisito', 'periodo_academico', 'oferta_asignatura',
    'matricula_asignatura', 'solicitud_cancelacion', 'seguimiento_solicitud',
    'perfil_sociodemografico', 'actividad_estudiantil', 'auditoria_cambio', 'outbox_evento'
)
$sourceCounts = @{}
foreach ($table in $tables) {
    $count = (& $psql @connection "--dbname=$SourceDatabase" --tuples-only --no-align `
            "--command=SELECT count(*) FROM gestion_estudiantil.$table;").Trim()
    if ($LASTEXITCODE -ne 0) { throw "No pude leer la tabla origen '$table' de '$SourceDatabase'." }
    $sourceCounts[$table] = [long]$count
}
$created = $false
try {
    & $createdb @connection $temporaryDatabase
    if ($LASTEXITCODE -ne 0) { throw "No se pudo crear la base temporal ($LASTEXITCODE)." }
    $created = $true

    $restoreArguments = Get-PostgresConnectionArguments $PgHost $Port $Username
    $restoreArguments += @('--no-owner', '--no-acl', "--dbname=$temporaryDatabase", $archive)
    & $pgRestore @restoreArguments
    if ($LASTEXITCODE -ne 0) { throw "La restauración temporal falló ($LASTEXITCODE)." }

    $queryArguments = Get-PostgresConnectionArguments $PgHost $Port $Username
    $tableCount = (& $psql @queryArguments --dbname=$temporaryDatabase --tuples-only --no-align `
            --command="SELECT count(*) FROM information_schema.tables WHERE table_schema = 'gestion_estudiantil' AND table_type = 'BASE TABLE';").Trim()
    if ($LASTEXITCODE -ne 0 -or [int]$tableCount -lt 18) {
        throw "La restauración no contiene las 18 tablas esperadas del esquema (encontradas: $tableCount)."
    }
    $roleCount = (& $psql @queryArguments --dbname=$temporaryDatabase --tuples-only --no-align `
            --command="SELECT count(*) FROM gestion_estudiantil.rol WHERE nombre IN ('ESTUDIANTE','DOCENTE','ADMINISTRADOR');").Trim()
    if ($LASTEXITCODE -ne 0 -or [int]$roleCount -ne 3) {
        throw "La verificación de datos semilla falló (roles encontrados: $roleCount)."
    }
    foreach ($table in $tables) {
        $restoredCount = (& $psql @queryArguments "--dbname=$temporaryDatabase" --tuples-only --no-align `
                "--command=SELECT count(*) FROM gestion_estudiantil.$table;").Trim()
        if ($LASTEXITCODE -ne 0 -or [long]$restoredCount -ne $sourceCounts[$table]) {
            throw "La cantidad de filas de '$table' no coincide: origen=$($sourceCounts[$table]), restaurada=$restoredCount."
        }
    }
    Write-Output "Restauración verificada en '$temporaryDatabase': $tableCount tablas, $roleCount roles y conteos iguales en $($tables.Count) tablas."
}
finally {
    if ($created) {
        & $dropdb @connection --if-exists --force $temporaryDatabase
        if ($LASTEXITCODE -ne 0) { Write-Warning "No se pudo eliminar la base temporal '$temporaryDatabase'." }
    }
}
