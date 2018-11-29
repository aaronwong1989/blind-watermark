# blind-watermark
盲水印demo  
BlindWaterMark project for Spring Boot

## build
```
mvn clean package -Dmaven.test.skip=true
```

## run
```
java -jar target\blind-watermark-0.0.1-SNAPSHOT.jar
```

## test

```bash
# 对图片添加文字水印
curl -X POST \
  http://localhost:8080/water-mark/encode \
  -H 'Cache-Control: no-cache' \
  -H 'Postman-Token: 3cedf70b-4285-41dd-8893-c3ff2725251e' \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F 'image=@D:\app\opencv\srcImg.png' \
  -F isText=true \
  -F 'text=pretty girl'
```

```bash
# 获取已加水印文件的文字水印
curl -X POST \
  http://localhost:8080/water-mark/decode/text \
  -H 'Cache-Control: no-cache' \
  -H 'Postman-Token: 95d4667c-fc0e-4a9c-a6ae-46a223dd9bd8' \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F 'wmImg=@D:\outPutImg\text_1543460862880_srcImg.png'
```

## 采坑记

* 直接在官方下载opencv并练习使用

[下载opencv](https://opencv.org/releases.html)  
下载后拷贝其中的java文件夹 
```bash
# 将java包安装到本地仓库 然后在pom.xml中引入
mvn install:install-file -Dfile=opencv-344.jar -DgroupId=cn.com.demo -DartifactId=opencv -Dversion=3.4.4 -Dpackaging=jar
``` 
执行代码时需要添加jvm参数 `-Djava.library.path=D:/app/opencv/java/x64` 将opencv的dll文件引入到jvm  
这种方式只直接使用javacv的官方java类库来实现，但苦于没有`盲水印` 相关的演示代码，而我对opencv技术是一脸懵逼，最后进行了很多尝试也没有成功。

* 

