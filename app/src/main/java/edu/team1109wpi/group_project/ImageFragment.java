package edu.team1109wpi.group_project;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ImageFragment extends Fragment {
    private static final int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private String filepath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.image, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button takeImageButton = (Button) getView().findViewById(R.id.takePictureButton);
        takeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeImage();
            }
        });

        Button loadImageButton = (Button) getView().findViewById(R.id.loadImageButton);
        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               loadImage();
            }
        });
    }

    private void takeImage() {
        Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = File.createTempFile("picture", ".jpeg", directory);
            Uri uri = FileProvider.getUriForFile(getActivity(), "edu.team1109wpi.group_project.fileprovider", file);
            takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            filepath = file.getAbsolutePath();
            Log.v("TAGFP", "Filepath:" + filepath);

            List<ResolveInfo> activities = getActivity().getPackageManager().
                    queryIntentActivities(takeImageIntent, 0);
            if(activities.size() >= 1) {
                startActivityForResult(takeImageIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void loadImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
                File file = new File(filepath);
                InputStream imageStream = new FileInputStream(file);
                imageView.setImageBitmap(BitmapFactory.decodeStream(imageStream));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK) {
            try {
                ImageView imageView = (ImageView) getView().findViewById(R.id.imageView);
                Uri imageUri = data.getData();
                InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
