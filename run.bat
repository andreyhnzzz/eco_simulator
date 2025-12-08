@echo off
REM Eco Simulator - Windows Launcher
REM This script builds and runs the Eco Simulator application

echo ========================================
echo    Eco Simulator - Starting...
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/
    echo.
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo [1/3] Compiling application...
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed. Please check the error messages above.
    echo.
    pause
    exit /b 1
)

echo [2/3] Build successful!
echo [3/3] Launching Eco Simulator...
echo.

REM Run the application with JavaFX
call mvn javafx:run

echo.
echo ========================================
echo    Eco Simulator - Closed
echo ========================================
pause
