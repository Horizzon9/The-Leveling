@echo off
title Minecraft Client - The Leveling Mod
cd /d "%~dp0"
echo Starting Minecraft client (runClient)...
call gradlew.bat runClient
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Minecraft kapandi veya bir hata olustu. Hata kodu: %ERRORLEVEL%
)
pause
