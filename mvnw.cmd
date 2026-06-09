@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@if "%DEBUG%"=="" @echo off
@REM set %HOME% to equivalent of $HOME
if "%HOME%"=="" (set HOME=%HOMEDRIVE%%HOMEPATH%)

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

set WRAPPER_JAR="%APP_HOME%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

set MAVEN_DOWNLOAD_URL="https://repo1.maven.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"

if exist %WRAPPER_JAR% goto validate

echo Downloading Maven...
powershell -Command "(New-Object Net.WebClient).DownloadFile('%MAVEN_DOWNLOAD_URL%', '%TEMP%\maven.zip')"
echo done.

:validate
@REM Find the project base dir
if not defined MAVEN_PROJECTBASEDIR (
  for /f "tokens=*" %%a in ('dir /b /s "%APP_HOME%\pom.xml" 2^>nul') do set MAVEN_PROJECTBASEDIR=%%~dpa
)

@REM set local scope for variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

set "MAVEN_OPTS=-Xmx1024m"

@REM Resolve maven home
for /f "tokens=*" %%i in ('"%JAVA_HOME%\bin\java" -cp %WRAPPER_JAR% %WRAPPER_LAUNCHER% resolve "%APP_HOME%\.mvn\wrapper\maven-wrapper.properties" 2^>nul') do set "MAVEN_HOME=%%i"
if "%MAVEN_HOME%"=="" (
  echo Downloading Maven distribution...
  "%JAVA_HOME%\bin\java" -cp %WRAPPER_JAR% %WRAPPER_LAUNCHER% download "%APP_HOME%\.mvn\wrapper\maven-wrapper.properties"
)

:end
@REM End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @endlocal

:omega
