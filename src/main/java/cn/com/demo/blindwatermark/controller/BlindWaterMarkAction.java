package cn.com.demo.blindwatermark.controller;

import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import cn.com.demo.blindwatermark.util.BlindWaterMarkUtil;
import java.io.File;
import java.io.IOException;
import javax.annotation.PostConstruct;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 水印控制器
 *
 * @author Aaron
 */
@RestController
@RequestMapping("/water-mark")
public class BlindWaterMarkAction {

  private static final String FILE_PROTOCOL = "file:";
  private static Logger logger = LoggerFactory.getLogger(BlindWaterMarkAction.class);

  @Value("${opencv.imageMarkFile}")
  private String imageMarkFile;
  @Value("${opencv.outputImageDir}")
  private String outputImageDir;
  @Value("${opencv.uploadImageDir}")
  private String uploadImageDir;

  private final ResourceLoader resourceLoader;

  @Autowired
  public BlindWaterMarkAction(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void init() {
    File dir = new File(outputImageDir);
    boolean ok = dir.mkdirs();
    logger.info("outPutImgDir is {}, prepared {}", dir, ok);
    dir = new File(uploadImageDir);
    ok = dir.mkdirs();
    logger.info("uploadImageDir is {}, prepared {}", dir, ok);
  }

  /**
   * @param image 待加水印图片
   * @param isText 是否添加文字水印
   * @param text isText=true 时，必须，表示添加的水印文字内容
   * @return 添加水印后的图片
   */
  @PostMapping("/encode")
  public ResponseEntity<?> addWaterMark(
      @RequestParam("image") MultipartFile image,
      @RequestParam("isText") boolean isText,
      @RequestParam(name = "text", required = false) String text) throws IOException {

    File srcFile = saveFile(image);
    Mat srcImg = imread(srcFile.getAbsolutePath(), CV_LOAD_IMAGE_COLOR);

    if (!srcImg.empty()) {
      String type = isText ? "text_" : "image_";
      String newFile =
          outputImageDir + File.separator + type + System.currentTimeMillis() + "_" + image
              .getOriginalFilename();

      text = isText ? text : imageMarkFile;
      BlindWaterMarkUtil.encode(srcImg, isText, text, newFile, new MatVector(3));
      Resource res = resourceLoader.getResource(FILE_PROTOCOL + newFile);

      return ResponseEntity
          .ok()
          .contentType(MediaType.IMAGE_PNG)
          .body(res);
    }

    return ResponseEntity.ok().build();
  }

  /**
   * 获取图片水印 // TODO 目前报错
   *
   * @param srcImg 未加水印原图
   * @param wmImg 已加水印图片
   * @return 水印
   */
  @PostMapping("/decode/img")
  public ResponseEntity<?> decodeImgWM(
      @RequestParam("srcImg") MultipartFile srcImg,
      @RequestParam("wmImg") MultipartFile wmImg) throws IOException {

    File srcFile = saveFile(srcImg);
    File wmFile = saveFile(wmImg);
    if (srcFile.exists() && wmFile.exists()) {
      // 水印文件
      String waterMarkImg =
          uploadImageDir + File.separator + "wm_" + System.currentTimeMillis() + ".png";
      BlindWaterMarkUtil.decode(srcFile.getAbsolutePath(), wmFile.getAbsolutePath(), waterMarkImg);

      Resource res = resourceLoader.getResource(FILE_PROTOCOL + waterMarkImg);

      return ResponseEntity
          .ok()
          .contentType(MediaType.IMAGE_PNG)
          .body(res);
    }

    return ResponseEntity.ok().build();
  }

  /**
   * 获取文字水印
   *
   * @param wmImg 加了文本水印的图像
   * @return 水印
   */
  @PostMapping("/decode/text")
  public ResponseEntity<?> decodeTextWM(
      @RequestParam("wmImg") MultipartFile wmImg) throws IOException {

    File wmFile = saveFile(wmImg);
    if (wmFile.exists()) {
      // 水印文件
      String waterMarkImg =
          uploadImageDir + File.separator + "wm_" + System.currentTimeMillis() + ".png";
      BlindWaterMarkUtil.decode(wmFile.getAbsolutePath(), waterMarkImg);

      Resource res = resourceLoader.getResource(FILE_PROTOCOL + waterMarkImg);

      return ResponseEntity
          .ok()
          .contentType(MediaType.IMAGE_PNG)
          .body(res);
    }

    return ResponseEntity.ok().build();
  }


  /**
   * 保存上传文件到本地
   *
   * @param image 上传的文件
   * @return 本地文件引用
   */
  private File saveFile(MultipartFile image) throws IOException {
    //获取文件名
    String fileName = image.getOriginalFilename();
    // 设置文件存储路径
    String path = uploadImageDir + File.separator + fileName;
    File dest = new File(path);
    image.transferTo(dest);
    return dest;
  }
}
