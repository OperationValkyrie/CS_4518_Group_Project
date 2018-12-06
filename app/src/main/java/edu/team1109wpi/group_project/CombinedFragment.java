package edu.team1109wpi.group_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

public class CombinedFragment extends Fragment {
    private static final int REQUEST_TAKE_PHOTO = 1;

    String imagePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.combined_image, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button saveImageButton = (Button) getView().findViewById(R.id.saveButton);
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save Image
                saveImage();
            }
        });
    }

    private File createTempImage() throws IOException {
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("image", ".jpg", storageDir);

        imagePath = image.getAbsolutePath();
        return image;
    }

    private void saveImage() {
        Intent takeImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takeImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createTempImage();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(), "edu.team1109wpi.group_project.fileprovider", photoFile);
                takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takeImageIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}
