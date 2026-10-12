# Colocar en la raíz del repo (hermano de helpdeskpro-db, helpdeskpro-backend, helpdeskpro-frontend).
# Uso: .\start-all.ps1
# Si PowerShell bloquea la ejecución: Set-ExecutionPolicy -Scope CurrentUser RemoteSigned

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "== 1/3: Base de datos =="
Set-Location "$RootDir\helpdeskpro-db"
docker compose up -d

if (-not (Test-Path ".env")) {
    Write-Error "Falta helpdeskpro-db\.env. Copia .env.example a .env y complétalo antes de continuar."
    exit 1
}

$envVars = @{}
Get-Content ".env" | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') { $envVars[$matches[1].Trim()] = $matches[2].Trim() }
}
$saPassword = $envVars["MSSQL_SA_PASSWORD"]

Write-Host "Esperando a que SQL Server esté listo..."
$ready = $false
while (-not $ready) {
    docker exec helpdeskpro-sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P $saPassword -C -Q "SELECT 1" *> $null
    if ($LASTEXITCODE -eq 0) { $ready = $true } else { Start-Sleep -Seconds 2; Write-Host "  ...todavía iniciando" }
}

Write-Host "Verificando/creando base y usuario (seguro correrlo aunque ya exista)..."
.\setup-db.ps1

Write-Host "== 2/3: Backend (Spring Boot) en una ventana nueva =="
Set-Location "$RootDir\helpdeskpro-backend"
Start-Process powershell -ArgumentList "-NoExit", "-Command", ".\mvnw.cmd spring-boot:run"

Write-Host "== 3/3: Frontend (React/Vite) en una ventana nueva =="
Set-Location "$RootDir\helpdeskpro-frontend"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "npm run dev"

Write-Host ""
Write-Host "Listo. Backend y frontend abrieron cada uno en su propia ventana."
Write-Host "Para detenerlos: cierra esas ventanas, o Ctrl+C dentro de cada una."
Write-Host "Para detener la base de datos: cd $RootDir\helpdeskpro-db; docker compose down"
