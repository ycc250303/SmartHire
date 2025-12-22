@echo off
chcp 65001 >nul
echo ========================================
echo 后端代码检查脚本
echo ========================================
echo.

cd /d "%~dp0"

@REM echo [1/5] 格式化代码...
@REM call mvn spotless:apply -q
@REM if %errorlevel% neq 0 (
@REM     echo [错误] 代码格式化失败！
@REM     pause
@REM     exit /b %errorlevel%
@REM )
@REM echo [✓] 代码格式化完成
@REM echo.

@REM echo [2/5] 检查代码格式...
@REM call mvn spotless:check -q
@REM if %errorlevel% neq 0 (
@REM     echo [错误] 代码格式检查失败！请运行 mvn spotless:apply 修复格式问题
@REM     pause
@REM     exit /b %errorlevel%
@REM )
@REM echo [✓] 代码格式检查通过
@REM echo.

echo [1/3] 运行单元测试...
call mvn test -q
if %errorlevel% neq 0 (
    echo [错误] 单元测试失败！
    pause
    exit /b %errorlevel%
)
echo [✓] 单元测试通过
echo.

echo [2/3] PMD 静态代码分析...
call mvn pmd:check -q
if %errorlevel% neq 0 (
    echo [警告] PMD 检查发现问题！详细报告请查看 target\pmd 文件夹
    echo 继续执行 SpotBugs 检查...
    echo.
) else (
    echo [✓] PMD 检查通过
    echo.
)

echo [3/3] SpotBugs + FindSecBugs 检查...
call mvn clean verify -q
if %errorlevel% neq 0 (
    echo [错误] SpotBugs 检查失败！
    pause
    exit /b %errorlevel%
)
echo [✓] SpotBugs 检查通过
echo.

echo ========================================
echo 所有检查完成！代码已准备好提交 PR
echo ========================================
pause

