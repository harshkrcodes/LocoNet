@echo off
echo Starting LocoNet Development Environment...

:: Start the Spring Boot Backend with a CLEAN FRESH BUILD
echo Wiping old code and starting fresh Spring Boot Backend...
start cmd /k "cd Backend && mvn clean spring-boot:run"

:: Wait for a few seconds to let backend compile and start
timeout /t 15

:: Start the Expo Frontend with cleared cache
echo Starting React Native Frontend...
cd LocoNet-Frontend
npx expo start -c