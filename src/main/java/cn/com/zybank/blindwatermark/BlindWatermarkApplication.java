package cn.com.zybank.blindwatermark;

import java.io.IOException;
import java.util.Arrays;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.opencv.core.CvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Aaron
 */
@SpringBootApplication
public class BlindWatermarkApplication {

  private static Logger logger = LoggerFactory.getLogger(BlindWatermarkApplication.class);


  public static void main(String[] args) throws IOException, InterruptedException {
    SpringApplication.run(BlindWatermarkApplication.class, args);
    loadOpenCvTest();
  }

  public static void loadOpenCvTest() {
    opencv_core.Mat mat = Mat.eye(3, 3, CvType.CV_8UC1).asMat();
    byte[] bts = new byte[mat.channels() * mat.cols() * mat.rows()];
    mat.data().get(bts);
    logger.info("====== opencv load success : " + Arrays.toString(bts));
  }
}
