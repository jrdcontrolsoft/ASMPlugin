@echo off
del winapi.h > nul
cd ..\bin
cls
javah -jni -classpath . -o winapi.h de.fh_zwickau.asmplugin.WinApi
echo.
copy winapi.h ..\jni\winapi.h > nul
del winapi.h > nul
pause
