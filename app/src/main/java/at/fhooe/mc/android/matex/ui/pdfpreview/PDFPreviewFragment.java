package at.fhooe.mc.android.matex.ui.pdfpreview;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

import at.fhooe.mc.android.matex.R;
import at.fhooe.mc.android.matex.activities.EditorActivity;
import at.fhooe.mc.android.matex.network.RetrofitGetPdfTask;

public class PDFPreviewFragment extends Fragment {

    public PDFView mPDFView;
    public View mView;
    private AdView mAdView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pdf_preview, container, false);

        mPDFView = root.findViewById(R.id.pdfView);

        new RetrofitGetPdfTask(getContext(), this, EditorActivity.mDocument).execute();

        mView = root;
        setLoading(true);
        return root;
    }

    public void setLoading(boolean b) {
        View icon = mView.findViewById(R.id.fragment_pdf_loading);
        TextView adText = mView.findViewById(R.id.adText);
        if (b) {
            icon.setVisibility(View.VISIBLE);
            mAdView = mView.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    adText.setVisibility(View.VISIBLE);
                }
            });
            mAdView.loadAd(adRequest);
        } else {
            icon.setVisibility(View.GONE);
            if (mAdView != null) {
                mAdView.destroy();
                mAdView = null;
            }
        }
    }

    public void setError(final String error) {
        getActivity().runOnUiThread(
                () -> new AlertDialog.Builder(PDFPreviewFragment.this.getActivity()).
                        setTitle("Error while generating PDF")
                        .setMessage(error)
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show()
        );
    }


    public void loadPdf(File pdf) {
        setLoading(false);
        mPDFView.fromFile(pdf).spacing(1)
                .load();
    }
}