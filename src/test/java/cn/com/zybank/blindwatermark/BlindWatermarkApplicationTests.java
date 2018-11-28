package cn.com.zybank.blindwatermark;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Core;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TODO 执行时增加参数 <br> -Djava.library.path=D:/app/opencv/java/x64
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class BlindWatermarkApplicationTests {

  @BeforeClass
  public static void loadLibrary() {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }


  @Test
  public void contextLoads() {
    BlindWatermarkApplication.loadOpenCvTest();
  }
}
