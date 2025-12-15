@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@echo off
setlocal EnableDelayedExpansion

set ERROR_CODE=0

set MAVEN_WRAPPER_DISABLE_SSL_VERIFY=%MAVEN_WRAPPER_DISABLE_SSL_VERIFY%

set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "x%MAVEN_PROJECTBASEDIR%" == "x" goto endDetectBaseDir

set EXEC_DIR=%CD%
set WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%\mvnw.cmd" goto baseDirFound
cd ..
IF "%WDIR%" == "%CD%" goto baseDirNotFound
set WDIR=%CD%
goto findBaseDir

:baseDirFound
set MAVEN_PROJECTBASEDIR=%WDIR%
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%EXEC_DIR%
cd "%EXEC_DIR%"

:endDetectBaseDir

REM Preconfigurar URL del wrapper para evitar expansión anticipada dentro del bloque
if "%MAVEN_WRAPPER_JAR_URL%"=="" set "MAVEN_WRAPPER_JAR_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  echo Downloading Maven Wrapper jar from !MAVEN_WRAPPER_JAR_URL!
  powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $ErrorActionPreference = 'Stop'; $webclient = New-Object System.Net.WebClient; if ('$env:MAVEN_WRAPPER_DISABLE_SSL_VERIFY' -eq 'true') { $webclient.Proxy = [System.Net.WebRequest]::GetSystemWebProxy(); $webclient.Proxy.Credentials = [System.Net.CredentialCache]::DefaultNetworkCredentials }; $webclient.DownloadFile('!MAVEN_WRAPPER_JAR_URL!', '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar') }"
  IF "%ERRORLEVEL%" NEQ "0" (
    echo Failed to download Maven Wrapper jar from !MAVEN_WRAPPER_JAR_URL!
    EXIT /B 1
  )
)

REM Detección de Java robusta
set "JAVA_EXE=java.exe"
if defined JAVA_HOME set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

if exist "%JAVA_EXE%" goto haveJava
where java.exe >NUL 2>&1
IF "%ERRORLEVEL%" NEQ "0" (
  echo JAVA_HOME is not set and no 'java' command could be found in your PATH.
  echo Please set the JAVA_HOME variable in your environment to match the
  echo location of your Java installation.
  exit /B 1
)
set "JAVA_EXE=java.exe"

:haveJava
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

"%JAVA_EXE%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
set ERROR_CODE=%ERRORLEVEL%

:end
if not "%OS%" == "Windows_NT" exit %ERROR_CODE%
exit /B %ERROR_CODE%
