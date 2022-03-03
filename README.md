# SAATM

## Run

### ATM
```
./gradlew runATM -Pargs="-f1 arg1 -f2 arg2 -f3 arg3"
```

### bank.Bank
```
./gradlew runBank -Pargs="-f1 arg1 -f2 arg2 -f3 arg3"
```

## Export as Jar
Change the main class of the jar manifest in build.gradle and then run
```
./gradlew fatJar
```
A jar file will be created in build/libs with all dependencies
