package com.example.histogram_simple;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import com.example.histogram_simple.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AppController {
        @FXML
        private JFXToggleButton gryScl;
        @FXML
        private ImageView hImagei;
        @FXML
        private ImageView Imagei;
        @FXML
        private JFXToggleButton logobt;
        @FXML
        private JFXButton strtButton;
        private ScheduledExecutorService timer;
        private VideoCapture capture;
        private boolean cameraActive;
        private Mat Mlogo;

        public void initialize(){
                this.capture = new VideoCapture();
                this.cameraActive = false;
        }
        @FXML
        void setLogo(ActionEvent event) {
                if(logobt.isSelected()){
                        this.Mlogo = Imgcodecs.imread("src/main/resources/music.png");
                }
        }

        @FXML
        void startCamera(ActionEvent event) {
                this.Imagei.setFitWidth(600);
                this.Imagei.setPreserveRatio(true);

                if(!this.cameraActive){
                        this.capture.open(0);

                        if(this.capture.isOpened()){
                                this.cameraActive = true;
                                Runnable frameGrabber  = new Runnable() {
                                        @Override
                                        public void run() {
                                                Mat frame = grabFrame();
                                                Image imageToShow = Utils.mat2Image(frame);
                                                updateImageView(Imagei,imageToShow);
                                        }
                                };
                                this.timer = Executors.newSingleThreadScheduledExecutor();
                                this.timer.scheduleAtFixedRate(frameGrabber,0,33,TimeUnit.MILLISECONDS);
                                this.strtButton.setStyle("-fx-background-color: red; -fx-border-color: white;");
                                this.strtButton.setText("Stop Camera");
                        }
                        else{
                                System.err.println("Impossible to open the camera connection...");
                        }
                }
                else{
                        this.cameraActive = false;
                        this.stopAcquisition();
                        this.strtButton.setStyle("-fx-background-color: green; -fx-border-color: white;");
                        this.strtButton.setText("Start Camera");
                }
        }

        private Mat grabFrame(){
                Mat frame = new Mat();
                if(this.capture.isOpened()){
                        try{
                                this.capture.read(frame);

                                if(!frame.empty()){
                                       if(logobt.isSelected() && this.Mlogo!=null){
                                               Rect roi = new Rect(frame.cols() - Mlogo.cols(),
                                                       frame.rows() - Mlogo.rows(),
                                                       Mlogo.cols(),Mlogo.rows());
                                               Mat imageRoI = frame.submat(roi);
                                               Core.addWeighted(imageRoI,1.0,Mlogo,0.8,0.0,imageRoI);
//                                               Mlogo.copyTo(imageRoI,Mlogo);
                                       }

                                       if(gryScl.isSelected()){
                                               Imgproc.cvtColor(frame,frame,Imgproc.COLOR_BGR2GRAY);
                                       }

                                       this.showHistogram(frame,gryScl.isSelected());
                                }
                        }
                        catch(Exception e){
                                System.err.println("Exception during the frame elaboration: " + e);
                        }
                }
                return frame;
        }
        protected void setClose(){
                this.stopAcquisition();
        }
        private void stopAcquisition(){
                if(this.timer != null && !this.timer.isShutdown()){
                        try{
                                this.timer.shutdown();
                                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
                        }
                        catch (Exception e){
                                e.printStackTrace();
                        }
                }
                if(this.capture.isOpened()){
                        this.capture.release();
                }
        }

        private void updateImageView(ImageView view, Image image){
                Utils.onFXThread(view.imageProperty(),image);
        }
        private void showHistogram(Mat frame, boolean gray){
                List<Mat> images = new ArrayList<Mat>();
                Core.split(frame,images);

                MatOfInt histSize = new MatOfInt(256);
                MatOfInt channels = new MatOfInt(0);
                MatOfFloat histRange = new MatOfFloat(0,256);

                Mat hist_b = new Mat();
                Mat hist_g = new Mat();
                Mat hist_r = new Mat();

                Imgproc.calcHist(images.subList(0,1),channels,new Mat(),
                        hist_b,histSize,histRange,false);
                if(!gray){
                        Imgproc.calcHist(images.subList(1,2),channels,new Mat(),
                                hist_g,histSize,histRange,false);
                        Imgproc.calcHist(images.subList(2,3),channels,new Mat(),
                                hist_r,histSize,histRange,false);
                }

                int hist_w = 1000;
                int hist_h = 130;
                int bin_w = (int) Math.round(hist_w/histSize.get(0,0)[0]);

                Mat histImage = new Mat(hist_h,hist_w,CvType.CV_8UC3,new Scalar(0,0,0));

                Core.normalize(hist_b,hist_b,0,histImage.rows(), Core.NORM_MINMAX,-1,new Mat());

                if(!gray){
                        Core.normalize(hist_g,hist_g,0,histImage.rows(),Core.NORM_MINMAX,-1,new Mat());
                        Core.normalize(hist_r,hist_r,0,histImage.rows(),Core.NORM_MINMAX,-1,new Mat());
                }

                for(int i=1;i<histSize.get(0,0)[0];i++){
                        Imgproc.line(histImage,new Point(bin_w*(i-1), hist_h - Math.round(hist_b.get(i-1,0)[0])),
                                new Point(bin_w*i,hist_h - Math.round(hist_b.get(i,0)[0])),
                                new Scalar(255,0,0),2,8,0);

                        if(!gray){
                                Imgproc.line(histImage, new Point(bin_w*(i-1), hist_h - Math.round(hist_g.get(i-1, 0)[0])),
                                        new Point(bin_w*(i), hist_h-Math.round(hist_g.get(i, 0)[0])),
                                        new Scalar(0, 255, 0), 2, 8, 0);
                                Imgproc.line(histImage, new Point(bin_w*(i-1), hist_h - Math.round(hist_r.get(i-1, 0)[0])),
                                        new Point(bin_w*i, hist_h - Math.round(hist_r.get(i, 0)[0])),
                                        new Scalar(0, 0, 255), 2, 8, 0);
                        }
                }

                Image histImg = Utils.mat2Image(histImage);
                updateImageView(hImagei,histImg);
        }

    }

