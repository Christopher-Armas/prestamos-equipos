@echo off
setlocal
title Preparar base de datos - Prestamos de equipos
rem ================================================================
rem  1. Si PostgreSQL no esta instalado, lo instala (PostgreSQL 18).
rem  2. Arranca el servicio de PostgreSQL.
rem  3. Crea la base prestamos_equipos y carga los datos iniciales.
rem  4. Guarda la variable DB_PASSWORD que usa el proyecto.
rem  Se puede ejecutar las veces que quieras: lo que ya esta hecho no se repite.
rem ================================================================

set "CLAVE_GRUPO=Admin2026"
set "BASE=prestamos_equipos"
set "SQL=%~dp0prestamos_equipos.sql"

rem --- Pedir permisos de administrador (necesarios para instalar y arrancar el servicio)
net session >nul 2>&1
if errorlevel 1 (
  echo Pidiendo permisos de administrador...
  powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
  exit /b
)

echo.
echo [1/4] Buscando PostgreSQL...
call :buscar_psql
if defined PGBIN (
  echo       Ya esta instalado en "%PGBIN%". No se instala nada.
  set "RECIEN_INSTALADO="
  goto :servicio
)

echo       No esta instalado. Instalando PostgreSQL 18 ^(tarda unos minutos, no cierres esta ventana^)...
where winget >nul 2>&1
if errorlevel 1 (
  echo.
  echo No encontre winget en este equipo. Instala PostgreSQL a mano desde:
  echo   https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
  echo Usa la contrasena %CLAVE_GRUPO% y el puerto 5432. Luego vuelve a ejecutar este archivo.
  goto :error
)
winget install --id PostgreSQL.PostgreSQL.18 -e --source winget --silent --accept-package-agreements --accept-source-agreements --override "--mode unattended --unattendedmodeui minimal --superpassword %CLAVE_GRUPO% --serverport 5432"
call :buscar_psql
if not defined PGBIN (
  echo La instalacion no termino bien. Revisa el mensaje de arriba.
  goto :error
)
set "RECIEN_INSTALADO=1"
echo       PostgreSQL instalado. Contrasena del usuario postgres: %CLAVE_GRUPO%

:servicio
echo.
echo [2/4] Arrancando el servicio de PostgreSQL...
for /f "tokens=2 delims=:" %%s in ('sc query state^= all ^| findstr /i "SERVICE_NAME: postgresql"') do (
  sc config %%s start= auto >nul
  net start %%s >nul 2>&1
)
"%PGBIN%\pg_isready.exe" -h localhost -p 5432 -t 30 >nul
if errorlevel 1 (
  echo PostgreSQL no responde en el puerto 5432.
  goto :error
)
echo       Servicio en marcha ^(queda en inicio automatico^).

echo.
echo [3/4] Comprobando la contrasena del usuario postgres...
set "PGCLIENTENCODING=UTF8"
if defined RECIEN_INSTALADO (set "PGPASSWORD=%CLAVE_GRUPO%") else if defined DB_PASSWORD (set "PGPASSWORD=%DB_PASSWORD%") else (set "PGPASSWORD=%CLAVE_GRUPO%")
set /a INTENTOS=0
:probar_clave
"%PGBIN%\psql.exe" -h localhost -U postgres -d postgres -tAc "SELECT 1" >nul 2>&1
if not errorlevel 1 goto :clave_ok
set /a INTENTOS+=1
if %INTENTOS% geq 4 (
  echo Demasiados intentos con una contrasena incorrecta.
  goto :error
)
echo       La contrasena no funciono.
set /p PGPASSWORD=      Escribe la contrasena que pusiste al instalar PostgreSQL:
goto :probar_clave
:clave_ok
echo       Contrasena correcta.

echo.
echo [4/4] Creando la base %BASE%...
"%PGBIN%\psql.exe" -h localhost -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='%BASE%'" | find "1" >nul
if not errorlevel 1 (
  echo       La base %BASE% ya existe. No se toca para no perder datos.
  goto :variable
)
"%PGBIN%\createdb.exe" -h localhost -U postgres %BASE%
if errorlevel 1 goto :error
if exist "%SQL%" (
  "%PGBIN%\psql.exe" -h localhost -U postgres -d %BASE% -v ON_ERROR_STOP=1 -q -f "%SQL%"
  if errorlevel 1 goto :error
  echo       Base creada y datos iniciales cargados.
) else (
  echo       Base creada vacia ^(no encontre prestamos_equipos.sql; las tablas las crea la aplicacion al arrancar^).
)

:variable
setx DB_PASSWORD "%PGPASSWORD%" >nul
if errorlevel 1 (
  echo No pude guardar DB_PASSWORD. Definela a mano con:  setx DB_PASSWORD "tu_clave"
  goto :error
)
echo.
echo ================================================================
echo  LISTO. PostgreSQL, la base %BASE% y DB_PASSWORD estan configurados.
echo  Cierra y vuelve a abrir IntelliJ y la consola para que tomen DB_PASSWORD.
echo ================================================================
pause
exit /b 0

:buscar_psql
set "PGBIN="
for %%v in (18 17 16 15 14) do if not defined PGBIN if exist "C:\Program Files\PostgreSQL\%%v\bin\psql.exe" set "PGBIN=C:\Program Files\PostgreSQL\%%v\bin"
exit /b 0

:error
echo.
echo SE DETUVO POR UN ERROR. Revisa el mensaje de arriba.
pause
exit /b 1
