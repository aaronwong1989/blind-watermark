package cn.com.demo.blindwatermark.util;

import static org.bytedeco.javacpp.opencv_core.BORDER_CONSTANT;
import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_core.NORM_MINMAX;
import static org.bytedeco.javacpp.opencv_core.add;
import static org.bytedeco.javacpp.opencv_core.addWeighted;
import static org.bytedeco.javacpp.opencv_core.copyMakeBorder;
import static org.bytedeco.javacpp.opencv_core.dft;
import static org.bytedeco.javacpp.opencv_core.flip;
import static org.bytedeco.javacpp.opencv_core.getOptimalDFTSize;
import static org.bytedeco.javacpp.opencv_core.idft;
import static org.bytedeco.javacpp.opencv_core.log;
import static org.bytedeco.javacpp.opencv_core.magnitude;
import static org.bytedeco.javacpp.opencv_core.merge;
import static org.bytedeco.javacpp.opencv_core.normalize;
import static org.bytedeco.javacpp.opencv_core.split;
import static org.bytedeco.javacpp.opencv_core.subtract;
import static org.bytedeco.javacpp.opencv_core.vconcat;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FONT_HERSHEY_COMPLEX;
import static org.bytedeco.javacpp.opencv_imgproc.putText;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;

/**
 * 水印工具类
 *
 * @author Aaron
 */
public class BlindWaterMarkUtil {

  /**
   * 为图片添加水印
   *
   * @param srcImg 原始图片，同时作为输出
   * @param watermark 文字水印或图片水印的路径
   * @param isText 是否时文字
   * @param outImg 输出新图片
   * @param newPlanes 图片运算过程中的3D画布 MatVector类型 。 MatVector newPlanes = new MatVector(3);
   */
  public static void encode(Mat srcImg, boolean isText, String watermark, String outImg,
      MatVector newPlanes) {
    //split color channels
    MatVector color = new MatVector(3);
    split(srcImg, color);

    MatVector[] planes = {new MatVector(2), new MatVector(2), new MatVector(2)};
    for (int i = 0; i < color.size(); i++) {
      color.get(i).convertTo(color.get(i), CV_32F);
      Mat comImg = startDFT(color.get(i));
      if (isText) {
        addTextWaterMark(comImg, watermark);
      } else {
        addImageWaterMark(comImg, watermark);
      }
      inverseDFT(comImg, planes[i]);
      newPlanes.put(i, comImg);
    }

    Mat nImg = new Mat();
    merge(newPlanes, nImg);

    imwrite(outImg, nImg);
  }

  /**
   * 文本水印解码
   *
   * @param wmImg 加了文本水印的图像
   * @param output 图像中文本水印
   */
  public static void decode(String wmImg, String output) {

    Mat decImg = imread(wmImg, CV_LOAD_IMAGE_GRAYSCALE);
    if (decImg.empty()) {
      System.exit(1);
    }
    decImg.convertTo(decImg, CV_32F);

    decImg = startDFT(decImg);
    MatVector newPlanes = new MatVector(2);
    Mat mag = new Mat();
    split(decImg, newPlanes);
    magnitude(newPlanes.get(0), newPlanes.get(1), mag);
    add(Mat.ones(mag.size(), CV_32F).asMat(), mag, mag);
    log(mag, mag);
    shiftDFT(mag);
    mag.convertTo(mag, CV_8UC1);
    normalize(mag, mag, 0, 255, NORM_MINMAX, CV_8UC1, null);

    imwrite(output, mag);
  }

  /**
   * 图片水印解码
   *
   * @param srcImg 原图
   * @param wmImg 加了图片水印的图像
   * @param output 图像中的水印
   */
  public static void decode(String srcImg, String wmImg, String output) {
    Mat decImg = imread(srcImg, CV_LOAD_IMAGE_GRAYSCALE);
    Mat wm = imread(wmImg, CV_LOAD_IMAGE_GRAYSCALE);

    decImg.convertTo(decImg, CV_32F);
    wm.convertTo(wm, CV_32F);
    if (decImg.empty() || wm.empty()) {
      System.exit(1);
    }

    //srcImg -= wmImg
    subtract(wm, startDFT(decImg), startDFT(wm));

    MatVector newPlanes = new MatVector(2);
    split(wm, newPlanes);
    wm = newPlanes.get(0);

    imwrite(output, wm);
  }


  /**
   * 将图像进行DFT
   *
   * @param srcImg 源图像
   * @return 转化后的图像
   */
  private static Mat startDFT(Mat srcImg) {
    MatVector planes = new MatVector(2);
    Mat comImg = new Mat();
    planes.put(0, srcImg);
    planes.put(1, Mat.zeros(srcImg.size(), CV_32F).asMat());
    merge(planes, comImg);
    dft(comImg, comImg);
    return comImg;
  }

  /**
   * DFT逆变换
   *
   * @param comImg DFT后的图像
   * @param planes 图像变量
   */
  private static void inverseDFT(Mat comImg, MatVector planes) {
    idft(comImg, comImg);
    split(comImg, planes);
    normalize(planes.get(0), comImg, 0, 255, NORM_MINMAX, CV_8UC3, null);
  }

  /**
   * 优化图像大小
   *
   * @param srcImg 源图像
   * @return 优化后的图像
   */
  private static Mat optimizedImage(Mat srcImg) {
    Mat padded = new Mat();
    int opRows = getOptimalDFTSize(srcImg.rows());
    int opCols = getOptimalDFTSize(srcImg.cols());
    copyMakeBorder(srcImg, padded, 0, opRows - srcImg.rows(),
        0, opCols - srcImg.cols(), BORDER_CONSTANT, Scalar.all(0));
    return padded;
  }

  /**
   * 交换频谱图四个象限
   *
   * @param comImg 频谱图
   */
  private static void shiftDFT(Mat comImg) {
    comImg = new Mat(comImg, new Rect(0, 0, comImg.cols() & -2, comImg.rows() & -2));
    int cx = comImg.cols() / 2;
    int cy = comImg.rows() / 2;

    Mat q0 = new Mat(comImg, new Rect(0, 0, cx, cy));
    Mat q1 = new Mat(comImg, new Rect(cx, 0, cx, cy));
    Mat q2 = new Mat(comImg, new Rect(0, cy, cx, cy));
    Mat q3 = new Mat(comImg, new Rect(cx, cy, cx, cy));

    Mat tmp = new Mat();
    q0.copyTo(tmp);
    q3.copyTo(q0);
    tmp.copyTo(q3);

    q1.copyTo(tmp);
    q2.copyTo(q1);
    tmp.copyTo(q2);
  }

  /**
   * 添加文本水印
   *
   * @param comImg 频谱图
   */
  private static void addTextWaterMark(Mat comImg, String watermark) {

    Scalar s = new Scalar(0x00, 0);
    Point p = new Point(comImg.size().width() / 4, comImg.size().height() / 4);

    // add text
    putText(comImg, watermark, p, CV_FONT_HERSHEY_COMPLEX, 1.5, s, 3, 20, false);
    // 旋转图片
    flip(comImg, comImg, -1);

    putText(comImg, watermark, p, CV_FONT_HERSHEY_COMPLEX, 1.5, s, 3,20, false);
    flip(comImg, comImg, -1);
  }

  /**
   * 添加图片水印
   *
   * @param comImg 频谱图
   */
  private static void addImageWaterMark(Mat comImg, String watermark) {
    Mat wm = imread(watermark, CV_LOAD_IMAGE_GRAYSCALE);
    MatVector planes = new MatVector(2);
    wm.convertTo(wm, CV_32F);
    if (wm.empty()) {
      System.exit(1);

    }
    //same size
    createWaterMark(comImg, wm);
    //水印编码
    //...

    //same channel
    planes.put(0, wm);
    planes.put(1, wm);
    merge(planes, wm);

    //add mark
    addWeighted(wm, 0.5, comImg, 1, 0.0, comImg);
  }

  private static void createWaterMark(Mat comImg, Mat wm) {
    MatVector combine = new MatVector(2);
    Mat iwm = new Mat();
    copyMakeBorder(wm, wm, 0, comImg.rows() / 2 - wm.rows(),
        0, comImg.cols() - wm.cols(), BORDER_CONSTANT, Scalar.all(0));
    combine.put(0, wm);
    flip(wm, iwm, -1);
    combine.put(1, iwm);
    vconcat(combine, wm);
  }

}
