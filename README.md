# blind-watermark
blindwatermark project for Spring Boot

## download opencv
https://opencv.org/releases.html

## build
```
mvn clean package -Dmaven.test.skip=true
```

## run
```
java -Djava.library.path=D:/app/opencv/java/x64 -Djavacpp.platform.dependencies=true -jar target\blind-watermark-0.0.1-SNAPSHOT.jar
mvn compile exec:java -Dexec.mainClass=cn.com.zybank.blindwatermark.BlindWatermarkApplication -Pwindows-x86_64
```

