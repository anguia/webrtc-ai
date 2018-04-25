package com.waypal.video;

import static org.bytedeco.javacpp.flandmark.flandmark_detect;
import static org.bytedeco.javacpp.flandmark.flandmark_init;
import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.helper.opencv_objdetect.cvHaarDetectObjects;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvCreateMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FILLED;
import static org.bytedeco.javacpp.opencv_imgproc.cvCircle;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static org.bytedeco.javacpp.opencv_objdetect.cvLoadHaarClassifierCascade;

import java.io.File;
import java.io.IOException;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.flandmark.FLANDMARK_Model;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceDetector {
  private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);
  private Boolean drawFlag = false;
  private CvHaarClassifierCascade cascadeFrontal;
  private FLANDMARK_Model model;
  private double[] landmarks;
  private int[] bbox = new int[4];
  private CvMemStorage storage;
  
  public FaceDetector(String modelFile, String markFile){
    File cascadeFrontalFile = new File(modelFile);
    File flandmarkModelFile = new File(markFile);
    cascadeFrontal = loadFaceCascade(cascadeFrontalFile);      
    model = loadFLandmarkModel(flandmarkModelFile);
    landmarks = new double[2 * model.data().options().M()];
    storage = cvCreateMemStorage(0);
  }
  
  private CvHaarClassifierCascade loadFaceCascade(final File file) {
    if (!file.exists()) {
      logger.error("Face cascade file does not exist:{} ", file.getAbsolutePath());
    }
    CvHaarClassifierCascade faceCascade = null;
    try {
      faceCascade = cvLoadHaarClassifierCascade(file.getCanonicalPath(), cvSize(0, 0));
    } catch (IOException e) {
      logger.error("Create face classifier is failure");
      e.printStackTrace();
    }

    if (faceCascade == null) {
      logger.error("Failed to load face cascade from file:{} ", file.getAbsolutePath());
    }

    return faceCascade;
  }
  
  private FLANDMARK_Model loadFLandmarkModel(final File file) {
    if (!file.exists()) {
      logger.error("FLandmark model file does not exist: {}", file.getAbsolutePath());
    }

    FLANDMARK_Model model = null;
    try {
      model = flandmark_init(file.getCanonicalPath());
    } catch (IOException e) {
      logger.error("Create landmark classifier is failure");
      e.printStackTrace();
    }
    if (model == null) {
      logger.error("Failed to load FLandmark model from file: {}", file.getAbsolutePath());
    }
    return model;
  }
  
  private int detectFaces(final IplImage orig,
      final Boolean frontalFlag){

    cvClearMemStorage(storage);
    int flags = CV_HAAR_DO_CANNY_PRUNING;
    CvSeq rects = cvHaarDetectObjects(orig, cascadeFrontal, storage, 1.1, 3, flags);
    
    int nFaces = rects.total();
    if (nFaces == 0) {
      logger.info("No faces detected");
      storage.close();
      return -1;
    }
    
    if (drawFlag){
      for (int iface = 0; iface < nFaces; ++iface) {
        BytePointer elem = cvGetSeqElem(rects, iface);
        CvRect rect = new CvRect(elem);
        
        bbox[0] = rect.x();
        bbox[1] = rect.y();
        bbox[2] = rect.x() + rect.width();
        bbox[3] = rect.y() + rect.height();
        
        // display faces
        cvRectangle(orig, 
            cvPoint(bbox[0], bbox[1]), cvPoint(bbox[2], bbox[3]), CV_RGB(255, 0, 0));
        cvRectangle(orig,
            cvPoint((int) model.bb().get(0), (int) model.bb().get(1)),
            cvPoint((int) model.bb().get(2), (int) model.bb().get(3)), CV_RGB(0, 0, 255));
        // display landmarks
        int landFlag = 0;
        if (landFlag != 0){
          flandmark_detect(orig, bbox, model, landmarks);
          cvCircle(orig, 
              cvPoint((int) landmarks[0], (int) landmarks[1]), 3, CV_RGB(0, 0, 255), CV_FILLED, 8, 0);
          for (int i = 2; i < 2 * model.data().options().M(); i += 2) {
            cvCircle(orig, cvPoint((int) (landmarks[i]), (int) (landmarks[i + 1])), 3, CV_RGB(255, 0, 0), CV_FILLED, 8, 0);
          }
        }
        rect.deallocate();
        rect.close();
      }
    }

    logger.info("Detected faces num: {}", nFaces);

    rects.deallocate();
    rects.close();
    storage.close();
    return nFaces;
  }
  
  public int detect(IplImage image, Boolean showFlag) {
    int frontalFaceNum = -1;       
    this.drawFlag = showFlag;
    logger.info("Firstly, detect frontal face");
    frontalFaceNum = detectFaces(image, true);
    logger.info("Frontal face num: {}", frontalFaceNum);
    return frontalFaceNum;
  }
}
