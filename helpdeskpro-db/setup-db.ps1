# Equivalente en PowerShell de setup-db.sh. Lo llama start-all.ps1, pero también se puede correr solo:
#   cd helpdeskpro-db; .\setup-db.ps1
# Seguro correrlo varias veces: el SQL sólo crea la base/usuario si no existen.
#
# Guardar como "UTF-8 con BOM" (ver nota en start-all.ps1).

$ErrorActionPreference = 'Stop'
$DbDir = $PSScriptRoot
$EnvPath = Join-Path $DbDir '.env'
$Container = 'helpdeskpro-sqlserver'
$Sqlcmd = '/opt/mssql-tools18/bin/sqlcmd'   # ruta DENTRO del contenedor Linux, por eso usa "/"

# Corre un programa externo en silencio y dice si terminó bien. Ver explicación en start-all.ps1:
# en PS 5.1, con 'Stop', redirigir stderr de un programa externo lanza una excepción.
function Test-NativeSuccess([scriptblock]$Command) {
    $ErrorActionPreference = 'Continue'
    & $Command *> $null
    return ($LASTEXITCODE -eq 0)
}

if (-not (Test-Path $EnvPath)) {
    Write-Host 'Falta helpdeskpro-db\.env. Copia .env.example a .env y coloca los valores reales.' -ForegroundColor Red
    exit 1
}

# Leemos .env a una tabla hash (PowerShell no tiene "source" para archivos .env como bash).
$vars = @{}
foreach ($line in Get-Content $EnvPath) {
    if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*?)\s*$') {
        $vars[$Matches[1]] = $Matches[2] -replace '^"(.*)"$', '$1' -replace "^'(.*)'$", '$1'
    }
}

Write-Host 'Esperando a que SQL Server esté listo dentro del contenedor...'
$sw = [Diagnostics.Stopwatch]::StartNew()
while ($true) {
    # Mientras SQL Server arranca, sqlcmd falla y escribe en stderr: es lo esperado, no un error.
    if (Test-NativeSuccess { docker exec $Container $Sqlcmd -S localhost -U sa -P $vars['MSSQL_SA_PASSWORD'] -C -Q 'SELECT 1' }) { break }
    # Límite de tiempo: con una contraseña incorrecta, esto esperaría para siempre.
    if ($sw.Elapsed.TotalSeconds -ge 90) {
        Write-Host "  [X] SQL Server no respondió en 90s. Revisa: docker logs $Container" -ForegroundColor Red
        exit 1
    }
    Start-Sleep -Seconds 2
    Write-Host '  ...todavía iniciando'
}

# Equivalente de "envsubst": reemplazamos ${VARIABLE} en la plantilla con los valores de .env.
# A diferencia de envsubst, si falta una variable FALLAMOS en vez de dejarla vacía
# (un "CREATE LOGIN  WITH PASSWORD = ''" vacío es peor que un error claro).
Write-Host 'Generando el SQL a partir de la plantilla (en memoria, sin escribir secretos a disco)...'
$template = Get-Content (Join-Path $DbDir 'init\init.sql.template') -Raw
$missing = [regex]::Matches($template, '\$\{(\w+)\}') |
    ForEach-Object { $_.Groups[1].Value } |
    Where-Object { -not $vars[$_] } |
    Select-Object -Unique
if ($missing) {
    Write-Host "Faltan en .env: $($missing -join ', ')" -ForegroundColor Red
    exit 1
}
# El scriptblock { ... } se usa como función de reemplazo: recibe cada coincidencia y devuelve el valor.
$sql = [regex]::Replace($template, '\$\{(\w+)\}', { param($m) $vars[$m.Groups[1].Value] })

Write-Host 'Ejecutando el SQL...'
# Al enviar texto por tubería (|) a un programa externo, PowerShell lo codifica con $OutputEncoding.
# En PS 5.1 el valor por defecto es ASCII: cualquier tilde se convertiría en "?".
# Usamos UTF-8 SIN BOM (el $false), porque un BOM al inicio confundiría a sqlcmd.
$OutputEncoding = New-Object System.Text.UTF8Encoding $false
$sql | docker exec -i $Container $Sqlcmd -S localhost -U sa -P $vars['MSSQL_SA_PASSWORD'] -C
if ($LASTEXITCODE -ne 0) {
    Write-Host 'sqlcmd devolvió un error (ver arriba).' -ForegroundColor Red
    exit 1
}

Write-Host "Listo. $($vars['DB_NAME']) y el usuario $($vars['DB_USER']) quedaron creados (o ya existían)."
