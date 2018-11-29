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

### 直接在官方下载opencv并练习使用

[下载opencv](https://opencv.org/releases.html)  下载后拷贝其中的java文件夹 
```bash
# 将java包安装到本地仓库 然后在pom.xml中引入
mvn install:install-file -Dfile=opencv-344.jar -DgroupId=cn.com.demo -DartifactId=opencv -Dversion=3.4.4 -Dpackaging=jar
``` 
执行代码时需要添加jvm参数 `-Djava.library.path=D:/app/opencv/java/x64` 将opencv的dll文件引入到jvm  
这种方式只直接使用javacv的官方java类库来实现，但苦于没有`盲水印` 相关的演示代码，而我对opencv技术是一脸懵逼，最后进行了很多尝试也没有成功。

### 参考ww23同学的代码实现maven采坑
ww23同学的代码见 [ww23/BlindWatermark](https://github.com/ww23/BlindWatermark)，但他的代码是采用Gradle构建的，我也一度以为用Maven构建也一样，完全没问题，可现实就直接打脸了。  
按照其代码，引入maven依赖  
```xml
    <dependency>
      <groupId>org.bytedeco.javacpp-presets</groupId>
      <artifactId>opencv</artifactId>
      <version>3.4.3-1.4.3</version>
    </dependency>
```
编译通过，但是一运行就报错，说是找到jni模块。我也联系了ww23同学，他给我提供了一些帮助，最终虽然解决了问题，能成功运行，但是我依然不知道，是什么原因导致的报错。他给的方法是修改依赖为 
```xml
<dependency>
  <groupId>org.bytedeco</groupId>
  <artifactId>javacv-platform</artifactId>
  <version>1.4.2</version>
</dependency>
```

后来我通过一个日本朋友的[博客](https://spring-boot-camp.readthedocs.io/ja/latest/02-OpenCV.html)发现了问题所在: 原因出在maven上。 
`org.bytedeco.javacpp-presets:opencv`并不是通过一个独立的jar包来实现各个平台的opencv，而是各个平台分别有不同的实现。
maven中在“三围”的基础上增加第四维`classifier` 来指定这个属性。正确的引入`org.bytedeco.javacpp-presets:opencv`的方法如下
```xml
    <dependency><!-- 跨平台API -->
      <groupId>org.bytedeco.javacpp-presets</groupId>
      <artifactId>opencv</artifactId>
      <version>3.4.3-1.4.3</version>
    </dependency>
    <dependency><!-- 具体平台的实现 -->
      <groupId>org.bytedeco.javacpp-presets</groupId>
      <artifactId>opencv</artifactId>
      <version>3.4.3-1.4.3</version>
      <classifier>windows-x86_64</classifier>
      <!-- 可更换的平台
       <classifier>windows-x86_64</classifier>
       <classifier>windows-x86_64-gpu</classifier>
       <classifier>macosx-x86_64</classifier>
        <classifier>macosx-x86_64-gpu</classifier>
       <classifier>linux-x86_64</classifier>
       <classifier>linux-x86_64-gpu</classifier>
       <classifier>android-x86_64</classifier>
       <classifier>ios-x86_64</classifier>
      -->
    </dependency>
```
通过这件事，我发现我对maven的认知还存在很大的不足，我之前写过一篇文章分析过maven的`scope`和`option`的用法：[Maven中Scope和Optional的用法](http://www.javawa.top/posts/a8f3da/) ，看来我有必要补充下`classifier`的用法。

## todo

1. 目前我在调试代码时，发现添加中文水印，解出来后都是？？？
2. 我在运行添加图片水印时可以正常运行，但是解水印时报错，还没看出来什么原因。
3. 我发现不是所有格式的图片添加了水印后，解码后都可以看得出来，不知道是对图片有要求还是对图片格式有要求，有待印证。



