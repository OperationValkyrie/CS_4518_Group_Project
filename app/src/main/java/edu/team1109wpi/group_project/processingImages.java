package edu.team1109wpi.group_project;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class processingImages extends AsyncTask<String, Float, Long> {
    private Context context;
    private View view;
    private ImageFragment image1Fragment;
    private ImageFragment image2Fragment;

    //TFlite stuff starts here
    private ByteBuffer imgData;
    private ByteBuffer img2Data;
    private int IMAGE_MEAN = 128;
    private float IMAGE_STD = 128;
    private int DIM_BATCH_SIZE = 1;
    private int SIZE_X = 299;
    private int SIZE_Y = 299;
    private Bitmap img1Map;
    private Bitmap img2Map;
    private String[] mLabels;
    private float[][] labelProbArray;
    private float[][] labelProbArray2;

    public processingImages(Context rootContext, View rootView, ImageFragment image1Fragment, ImageFragment image2Fragment) {
        context = rootContext;
        view = rootView;
        this.image1Fragment = image1Fragment;
        this.image2Fragment = image2Fragment;

        //TFLite OnCreate Setup here
        imgData = ByteBuffer.allocateDirect(
                DIM_BATCH_SIZE
                        * SIZE_X
                        * SIZE_Y
                        * 3
                        * 4);
        img2Data = ByteBuffer.allocateDirect(
                DIM_BATCH_SIZE
                        * SIZE_X
                        * SIZE_Y
                        * 3
                        * 4
        );
        imgData.order(ByteOrder.nativeOrder());
        img2Data.order(ByteOrder.nativeOrder());

        mLabels = new String[1001];
        labelProbArray = new float[DIM_BATCH_SIZE][1001];
        labelProbArray2 = new float[DIM_BATCH_SIZE][1001];

        try {
            //String line = "";
            InputStream istream = context.getAssets().open("model/labels.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(istream, "UTF-8"));
            for (int x = 0; x < 1001; x++){
                mLabels[x] = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("MYTAG", "notfound");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MYTAG", "ioexcept");
        }
    }

    protected Long doInBackground(String... img_files) {
        InputStream iStream1 = null;
        InputStream iStream2 = null;
        Interpreter tflite = null;

        try{
            //get imagestreams from images on screen

            MappedByteBuffer tfliteModel = loadModelFile();
            tflite  = new Interpreter(tfliteModel);
        } catch (IOException e){
            e.printStackTrace();
        }

        img1Map = Bitmap.createScaledBitmap(image1Fragment.getImageBitmap(), SIZE_X, SIZE_Y, false);
        convertBitmapToByteBuffer(img1Map);
        tflite.run(imgData, labelProbArray);

        img2Map = Bitmap.createScaledBitmap(image2Fragment.getImageBitmap(), SIZE_X, SIZE_Y, false);
        convertBitmap2ToByteBuffer(img2Map);
        tflite.run(img2Data, labelProbArray2);

        Log.v("TAG", "Ran");
        return null;
    }

    protected void onPostExecute(Long result) {
        TextView t1 = (TextView) view.findViewById(R.id.img1Data);
        TextView t2 = (TextView) view.findViewById(R.id.img2Data);

        int max1location = 0;
        int max2location = 0;

        for (int x = 0; x < 1001; x++){
            if (labelProbArray[0][x] >= labelProbArray[0][max1location]){
                //max = labelProbArray[0][x];
                max1location = x;
            }
            if (labelProbArray2[0][x] >= labelProbArray2[0][max2location]){
                //max = labelProbArray[0][x];
                max2location = x;
            }
        }
        String theResult = mLabels[max1location] + " " + labelProbArray[0][max1location] + " percent sure";
        String theResult2 = mLabels[max2location] + " " + labelProbArray2[0][max2location] + " percent sure";
        t1.setText(theResult);
        t2.setText(theResult2);
    }

    //TFLITE functions

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private String getModelPath(){
        return "model/inception_v3.tflite";
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        int[] intValues = new int[SIZE_X * SIZE_Y];

        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < SIZE_X; ++i) {
            for (int j = 0; j < SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                addPixelValue(val);
            }
        }
    }

    private void convertBitmap2ToByteBuffer(Bitmap bitmap) {
        int[] intValues = new int[SIZE_X * SIZE_Y];

        if (img2Data == null) {
            return;
        }
        img2Data.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < SIZE_X; ++i) {
            for (int j = 0; j < SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                addPixelValue2(val);
            }
        }
    }

    protected void addPixelValue(int pixelValue) {
        imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }

    protected void addPixelValue2(int pixelValue) {
        img2Data.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        img2Data.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        img2Data.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }

}
