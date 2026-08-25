@echo off
REM ============================================================
REM  Run the CTB Depot Management System WITHOUT NetBeans.
REM  Uses the bundled JavaFX JDK (Zulu CA-FX) and the Maven
REM  wrapper (mvnw) so nothing else needs to be installed.
REM
REM  Usage:  just double-click this file, or run  run.cmd  in a terminal.
REM ============================================================

REM JavaFX-bundled JDK 21 (note the doubled folder name is correct on this machine)
set "JAVA_HOME=C:\java\zulu21.40.17-ca-fx-jdk21.0.6-win_x64\zulu21.40.17-ca-fx-jdk21.0.6-win_x64"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using JAVA_HOME=%JAVA_HOME%
echo Building and launching the application...
echo.

REM Launch the JavaFX app via the Maven wrapper
call "%~dp0mvnw.cmd" clean javafx:run

echo.
echo Application exited.
pause
