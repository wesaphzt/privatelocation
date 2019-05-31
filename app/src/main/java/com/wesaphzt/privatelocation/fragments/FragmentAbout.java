package com.wesaphzt.privatelocation.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wesaphzt.privatelocation.R;

public class FragmentAbout extends Fragment {

    private static String GITHUB_URI;
    private static final String LICENSE_URI = GITHUB_URI + "/blob/master/LICENSE.txt";
    private static final String BUG_REPORT_URI = GITHUB_URI + "/issues";

    private static String AUTHOR_GITHUB;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        GITHUB_URI = getString(R.string.app_github);
        AUTHOR_GITHUB = getString(R.string.app_github_dev);

        String versionName = "";
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView version = v.findViewById(R.id.about_text_version);
        version.setText(versionName);

        LinearLayout license = v.findViewById(R.id.about_layout_license);
        LinearLayout source = v.findViewById(R.id.about_layout_source);

        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(LICENSE_URI);
            }
        });
        source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(GITHUB_URI);
            }
        });

        LinearLayout authorLayout = v.findViewById(R.id.aboutLayoutAuthor);
        authorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(AUTHOR_GITHUB);
            }
        });

        LinearLayout bugReport = v.findViewById(R.id.about_layout_bugs);
        bugReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(BUG_REPORT_URI);
            }
        });

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    private void openURI(String uri) {
        try {
            Intent openURI = new Intent(Intent.ACTION_VIEW);
            openURI.setData(Uri.parse(uri));
            startActivity(openURI);
        } catch (Exception e) {
            Log.d("app-error", "error opening uri");
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //hide action bar menu
        menu.setGroupVisible(R.id.menu_top, false);
        menu.setGroupVisible(R.id.menu_bottom, false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set title
        getActivity().setTitle(getString(R.string.fragment_about_title));
    }
}