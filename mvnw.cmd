@echo off
setlocal
set "DIR=%~dp0"

if not defined JAVA_HOME (
    if exist "C:\Users\HP\.jdks\openjdk-26.0.2" (
        set "JAVA_HOME=C:\Users\HP\.jdks\openjdk-26.0.2"
        set "PATH=C:\Users\HP\.jdks\openjdk-26.0.2\bin;%PATH%"
    )
)

if exist "%DIR%.tools\apache-maven-3.9.6\bin\mvn.cmd" (
    call "%DIR%.tools\apache-maven-3.9.6\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    call mvn %*
    exit /b %ERRORLEVEL%
)

if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
    call "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

for /d %%D in ("C:\Program Files\JetBrains\IntelliJ*") do (
    if exist "%%D\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
        call "%%D\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" %*
        exit /b %ERRORLEVEL%
    )
)

echo [ERROR] Maven not found. Please install Apache Maven or set it in PATH.
exit /b 1
