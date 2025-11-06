@echo off
:: Automatically detect the folder where KIWI is installed
set KIWI_HOME=%~dp0
:: Run the KIWI main class with all passed arguments
java -cp "%KIWI_HOME%" src.KIWI %*
