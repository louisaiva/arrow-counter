@echo off
REM Arrow Counter Mod - Build Batch Wrapper
REM This script makes it easy to build from Windows Command Prompt
cd /d "%~dp0"
python build.py %*
pause
