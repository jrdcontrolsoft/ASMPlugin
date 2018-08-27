@echo off
del winapi.dll
del winapi.obj
del winapi.tds
set OLDPATH=%Path%
set Path=%Path%;D:\Bcc55\Bin
bcc32.exe -c -Id:/PROGRA~1/Java/JDK15~2.0_0/include -Id:/PROGRA~1/Java/JDK15~2.0_0/include/win32 winapi.cpp
bcc32.exe -tWD winapi.obj
SET Path=%OLDPATH%
SET OLDPATH=
pause