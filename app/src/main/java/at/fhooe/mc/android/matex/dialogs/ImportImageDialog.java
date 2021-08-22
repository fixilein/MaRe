package at.fhooe.mc.android.matex.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import at.fhooe.mc.android.matex.R;
import at.fhooe.mc.android.matex.activities.EditorActivity;
import at.fhooe.mc.android.matex.document.Document;
import at.fhooe.mc.android.matex.ui.editor.EditorFragment;

public class ImportImageDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_import_image, null);

        final List<String> list = EditorActivity.Companion.getDocument().getImageNamesList(getContext());
        final EditText et = view.findViewById(R.id.dialog_image_import_editText_title);

        Context context;
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose name for image.")
                .setMessage("Enter a name for the image you selected. (Without a file extension.)")
                .setCancelable(false) // does not work ??
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = et.getText().toString().trim() + ".jpeg";
                    loadInImage(data, name);
                    mFragment.insertImageLink(name);
                });

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        final TextView tv = view.findViewById(R.id.dialog_image_import_textView_already_exits);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (list.contains(s.toString().trim() + ".jpeg")) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    tv.setVisibility(View.VISIBLE);
                } else if (s.toString().equals("")) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    tv.setVisibility(View.GONE);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    tv.setVisibility(View.GONE);
                }
            }
        });

        dialog.setOnShowListener(dialog1 -> ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));


        return dialog;
    }

    private Document mDocument;

    public ImportImageDialog setDocument(Document d) {
        mDocument = d;
        return this;
    }

    private Intent data;

    public ImportImageDialog setIntentData(Intent i) {
        data = i;
        return this;
    }

    private void loadInImage(Intent data, String name) {
        try {
            File img = new File(mDocument.getImageDir(getContext()), name);
            InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            FileOutputStream outStream = new FileOutputStream(img.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    private EditorFragment mFragment;

    public ImportImageDialog setFragment(EditorFragment fragment) {
        mFragment = fragment;
        return this;
    }
}
