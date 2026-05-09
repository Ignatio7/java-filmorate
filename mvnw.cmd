@ECHO OFF
SETLOCAL EnableDelayedExpansion

SET BASE_DIR=%~dp0
SET PROPS_FILE=%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties

FOR /F "tokens=1,2 delims==" %%A IN (%PROPS_FILE%) DO (
  IF "%%A"=="distributionUrl" SET DIST_URL=%%B
)

IF "%DIST_URL%"=="" (
  ECHO distributionUrl is not set in %PROPS_FILE%
  EXIT /B 1
)

FOR %%F IN (%DIST_URL%) DO SET DIST_NAME=%%~nxF
SET DIST_DIR=%BASE_DIR%\.mvn\wrapper\dists
SET ARCHIVE_PATH=%DIST_DIR%\%DIST_NAME%

IF NOT EXIST "%DIST_DIR%" MKDIR "%DIST_DIR%"

IF NOT EXIST "%ARCHIVE_PATH%" (
  ECHO Downloading Maven distribution...
  powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ARCHIVE_PATH%'"
)

SET MAVEN_HOME=
FOR /F "tokens=*" %%I IN ('tar -tzf "%ARCHIVE_PATH%" ^| findstr /R "^[^/][^/]*"') DO (
  IF "!MAVEN_HOME!"=="" SET MAVEN_HOME=%%I
)

SET MAVEN_HOME=%DIST_DIR%\%MAVEN_HOME:~0,-1%

IF NOT EXIST "%MAVEN_HOME%\bin\mvn.cmd" (
  ECHO Extracting Maven distribution...
  tar -xzf "%ARCHIVE_PATH%" -C "%DIST_DIR%"
)

CALL "%MAVEN_HOME%\bin\mvn.cmd" %*
