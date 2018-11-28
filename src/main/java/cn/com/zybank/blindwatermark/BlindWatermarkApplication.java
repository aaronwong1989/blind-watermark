package cn.com.zybank.blindwatermark;

import java.io.IOException;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TODO 执行时增加参数 <br> -Djava.library.path=D:/app/opencv/java/x64
 *
 * @author Aaron
 */
@SpringBootApplication
public class BlindWatermarkApplication {

  private static Logger logger = LoggerFactory.getLogger(BlindWatermarkApplication.class);

  public static void main(String[] args) throws IOException, InterruptedException {
    SpringApplication.run(BlindWatermarkApplication.class, args);
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    loadOpenCvTest();
    try {
      Loader.load(opencv_core.class);
    } catch (UnsatisfiedLinkError e) {
      String path = Loader.cacheResource(opencv_core.class, "windows-x86_64/jniopencv_core.dll").getPath();
      new ProcessBuilder("d:/app/opencv/depends.exe", path).start().waitFor();
    }
  }

  public static void loadOpenCvTest() {
    Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
    logger.info("opencv load success ! \n" + mat.dump());
  }
}
