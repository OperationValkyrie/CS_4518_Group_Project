package edu.team1109wpi.group_project;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {
    private Fragment image1Fragment;
    private Fragment image2Fragment;
    private Fragment combinedImageFragment;

    private FragmentAdapter fragmentAdapter;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private TabLayout tabLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image1Fragment = new ImageFragment();
        image2Fragment = new ImageFragment();
        combinedImageFragment = new CombinedFragment();

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(fragmentAdapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch(Exception e) {
            e.printStackTrace();
        }

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


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
            InputStream istream = getAssets().open("model/labels.txt");
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

    public class FragmentAdapter extends FragmentPagerAdapter {
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch(i) {
                case 0:
                    return image1Fragment;
                case 1:
                    return image2Fragment;
                case 2:
                    return combinedImageFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return "Image 1";
                case 1:
                    return "Image 2";
                case 2:
                    return "Combined Image";
                default:
                    return "Title";
            }
        }
    }

    //comparing button function
    public void onClickCompare(View v){
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

        img1Map = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(iStream1), SIZE_X, SIZE_Y, false);
        convertBitmapToByteBuffer(img1Map);
        img2Map = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(iStream2), SIZE_X, SIZE_Y, false);
        convertBitmap2ToByteBuffer(img2Map);

        tflite.run(imgData, labelProbArray);
        tflite.run(img2Data, labelProbArray2);


    }


    //TFLITE functions

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(getModelPath());
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
                addPixelValue(val);
            }
        }
    }

    protected void addPixelValue(int pixelValue) {
        imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }


}
