package cn.com.demo.blindwatermark;

import java.util.Arrays;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.CvType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TODO 执行时增加参数 <br> -Djava.library.path=D:/app/opencv/java/x64
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BlindWatermarkApplicationTests {

  private static Logger logger = LoggerFactory.getLogger(BlindWatermarkApplicationTests.class);

  @Test
  public void contextLoads() {
    loadOpenCvTest();
  }


  public void loadOpenCvTest() {
    Mat mat = Mat.eye(3, 3, CvType.CV_8UC1).asMat();
    byte[] bts = new byte[mat.channels() * mat.cols() * mat.rows()];
    mat.data().get(bts);
    logger.info("opencv load success ! \n" + Arrays.toString(bts));
  }
}
