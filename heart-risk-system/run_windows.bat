@echo off
setlocal
cd /d "%~dp0"
set "PROJECT_DIR=%~dp0"
echo ============================================
echo        HeartRisk - Windows Launcher
echo ============================================

where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java was not found. Install JDK 17 or newer and reopen this window.
  pause
  exit /b 1
)
where mvn >nul 2>nul
if errorlevel 1 (
  echo ERROR: Maven was not found. Install Maven 3.8+ and add it to PATH.
  pause
  exit /b 1
)
where py >nul 2>nul
if errorlevel 1 (
  echo ERROR: Python launcher was not found. Install Python 3.10+ and enable Add Python to PATH.
  pause
  exit /b 1
)

if not exist "frontend\.venv\Scripts\python.exe" (
  echo Creating Python virtual environment...
  py -3 -m venv frontend\.venv
  if errorlevel 1 goto :failed
)

echo Installing/checking frontend requirements...
call "frontend\.venv\Scripts\activate.bat"
python -m pip install --upgrade pip
pip install -r frontend\requirements.txt
if errorlevel 1 goto :failed

echo Building and starting Java backend in a separate window...
start "HeartRisk Java Backend" cmd /k "cd /d ""%PROJECT_DIR%backend"" && mvn clean package && java -jar target\heart-risk.jar"

echo Waiting briefly for backend startup...
timeout /t 12 /nobreak >nul

echo Starting Streamlit frontend in a separate window...
start "HeartRisk Streamlit Frontend" cmd /k "cd /d ""%PROJECT_DIR%frontend"" && call .venv\Scripts\activate.bat && streamlit run app.py"
echo.
echo HeartRisk is starting. Open http://localhost:8501 if the browser does not open.
echo Keep both terminal windows open while using the application.
pause
exit /b 0

:failed
echo.
echo Startup failed. Check the error above.
pause
exit /b 1
