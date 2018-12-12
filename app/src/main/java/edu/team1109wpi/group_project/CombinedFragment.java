package edu.team1109wpi.group_project;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CombinedFragment extends Fragment {

    private ImageFragment image1Fragment;
    private ImageFragment image2Fragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.combined_image, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button combineButton = (Button) getView().findViewById(R.id.combineButton);
        combineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new processingImages(getContext(), getView().findViewById(android.R.id.content), image1Fragment, image2Fragment).execute();
            }
        });
    }

    public void setImage1Fragment(ImageFragment image1F) {
        image1Fragment = image1F;
    }

    public void setImage2Fragment(ImageFragment image2F) {
        image2Fragment = image2F;
    }
}
