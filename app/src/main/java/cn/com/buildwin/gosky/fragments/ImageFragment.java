package cn.com.buildwin.gosky.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import cn.com.buildwin.gosky.R;

public class ImageFragment extends Fragment {

    private static final String IMAGE_PATH = "imagePath";

    PhotoView photoView;
    private String mPhotoPath;

    public ImageFragment() {
        // Required empty public constructor
    }

    public static ImageFragment newInstance(String imagePath) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_PATH, imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPhotoPath = getArguments().getString(IMAGE_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        photoView = (PhotoView) view.findViewById(R.id.photoView);
        photoView.setImageURI(Uri.parse(mPhotoPath));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
