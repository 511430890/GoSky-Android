
@echo off
echo 
echo ##########  Start compile  ##########
color 4
call F:\Android\android-ndk-r10e\ndk-build.cmd
goto end

:end
@cd %cd%\