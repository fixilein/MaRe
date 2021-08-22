package at.fhooe.mc.android.matex.ui.config;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import at.fhooe.mc.android.matex.R;
import at.fhooe.mc.android.matex.activities.EditorActivity;
import at.fhooe.mc.android.matex.document.DocHeader;
import at.fhooe.mc.android.matex.document.Document;
import at.fhooe.mc.android.matex.document.FontAdapter;

public class ConfigFragment extends Fragment {

    private Document mDocument;
    private DocHeader mHeader;

    private CheckBox cbToc, cbDate, cbLinkColor;
    private EditText edDate, edTitle, edSubTitle, edAuthor;
    private String dateBuffer;
    private TextView tvFontSize, tvMarginVert, tvMarginHor;
    private SeekBar seekBarFontSize, seekBarMarginVert, seekBarMarginHor;
    private Spinner fontSpinner;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_config, container, false);
        setHasOptionsMenu(false);

        mDocument = EditorActivity.Companion.getDocument();
        mHeader = mDocument.getHeader();

        cbToc = root.findViewById(R.id.fragment_config_checkBox_toc);
        cbToc.setChecked(mHeader.getToc());

        cbLinkColor = root.findViewById(R.id.fragment_config_checkBox_link_color);
        cbLinkColor.setChecked(mHeader.getLinkColor());

        edTitle = root.findViewById(R.id.fragment_config_editText_title);
        edTitle.setText(mHeader.getTitle());

        edAuthor = root.findViewById(R.id.fragment_config_editText_author);
        edAuthor.setText(mHeader.getAuthor());

        edSubTitle = root.findViewById(R.id.fragment_config_editText_subtitle);
        edSubTitle.setText(mHeader.getSubtitle());

        initDate(root);
        initFontFamily(root);
        initFontSize(root);


        tvMarginVert = root.findViewById(R.id.fragment_config_textView_margin_vert);
        tvMarginHor = root.findViewById(R.id.fragment_config_textView_margin_hor);

        seekBarMarginVert = root.findViewById(R.id.fragment_config_seekbar_margin_vert);
        seekBarMarginHor = root.findViewById(R.id.fragment_config_seekbar_margin_hor);
        seekBarMarginVert.setProgress(mHeader.getMarginTopBot());
        seekBarMarginHor.setProgress(mHeader.getMarginLeftRight());

        seekBarMarginVert.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMarginVert.setText(String.format(getString(R.string.margin_mm), String.valueOf(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarMarginHor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMarginHor.setText(String.format(getString(R.string.margin_mm), String.valueOf(progress)));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tvMarginVert.setText(String.format(getString(R.string.margin_mm), String.valueOf(mHeader.getMarginTopBot())));
        tvMarginHor.setText(String.format(getString(R.string.margin_mm), String.valueOf(mHeader.getMarginLeftRight())));

        return root;
    }

    private void initFontFamily(View _view) {
        String fontFam = mHeader.getFontFamily();
        fontSpinner = _view.findViewById(R.id.fragment_config_spinner_font);
        FontAdapter adapter = new FontAdapter(getContext());
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        adapter.addAll(DocHeader.fontFamilyMap().keySet());
        fontSpinner.setAdapter(adapter);
        fontSpinner.setSelection(adapter.getPosition(fontFam));
    }

    private void initDate(View _view) {
        dateBuffer = mHeader.getDate();
        if (dateBuffer.equals(getString(R.string.LaTeX_today))) dateBuffer = "";

        edDate = _view.findViewById(R.id.fragment_config_editText_date);
        edDate.setText(mHeader.getDate());

        cbDate = _view.findViewById(R.id.fragment_config_checkBox_date_today);
        cbDate.setChecked(mHeader.getDate().equals("\\today"));
        edDate.setEnabled(!cbDate.isChecked());
        cbDate.setOnCheckedChangeListener((buttonView, _isChecked) -> {
            edDate.setEnabled(!_isChecked);
            if (_isChecked) {
                dateBuffer = edDate.getText().toString();
                edDate.setText(getString(R.string.LaTeX_today));
            } else {
                edDate.setText(dateBuffer);
            }
        });


        Button btnPickDate = _view.findViewById(R.id.fragment_config_button_pick_date);
        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(getContext());
            dpd.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                edDate.setEnabled(true);
                cbDate.setChecked(false);
                edDate.setText(getStringDate(year, month, dayOfMonth));
            });
            dpd.show();
        });
    }

    private void initFontSize(View _view) {
        tvFontSize = _view.findViewById(R.id.fragment_config_textView_font_size);

        seekBarFontSize = _view.findViewById(R.id.fragment_config_seekBarFontSize);
        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fontSize = mapFontSize(progress);
                String text = fontSize + "pt";
                tvFontSize.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        int progress = unmapFontSize(mHeader.getFontSize());
        seekBarFontSize.setProgress(progress);
        String text = mHeader.getFontSize() + "pt";
        tvFontSize.setText(text);
    }

    // possible: 8pt, 9pt, 10pt, 11pt, 12pt, 14pt, 17pt, 20pt.
    //           1      2   3       4   5       6   7     8
    private int mapFontSize(int progress) {
        switch (progress) {
            case 6: {
                return 14;
            }
            case 7: {
                return 17;
            }
            case 8: {
                return 20;
            }
            default: {
                return progress + 7;
            }
        }
    }

    // return progressbar value
    private int unmapFontSize(int size) {
        switch (size) {
            case 14: {
                return 6;
            }
            case 17: {
                return 7;
            }
            case 20: {
                return 8;
            }
            default: {
                return size - 7;
            }
        }
    }

    /**
     * Turns year, month and day to a formatted string based on the current locale.
     *
     * @param year       Year
     * @param month      Month
     * @param dayOfMonth Day of Month
     * @return A formatted String of the Date.
     */
    private String getStringDate(int year, int month, int dayOfMonth) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, getResources().getConfiguration().getLocales().get(0));
        return df.format(new GregorianCalendar(year, month, dayOfMonth).getTime());
    }


    private void reloadHeader() {
        mHeader.setTitle(edTitle.getText().toString());
        mHeader.setSubtitle(edSubTitle.getText().toString());
        mHeader.setAuthor(edAuthor.getText().toString());
        mHeader.setDate(edDate.getText().toString());
        mHeader.setToc(cbToc.isChecked());
        mHeader.setFontSize(mapFontSize(seekBarFontSize.getProgress()));
        mHeader.setMarginLeftRight(seekBarMarginHor.getProgress());
        mHeader.setMarginTopBot(seekBarMarginVert.getProgress());
        mHeader.setFontFamily((String) fontSpinner.getSelectedItem());
        mHeader.setLinkColor(cbLinkColor.isChecked());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_config, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPause() {
        super.onPause();
        save();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem _item) {
        return super.onOptionsItemSelected(_item);
    }

    private void save() {
        reloadHeader();
        mDocument.saveFile(mHeader.toString(), mDocument.getContent());
    }


}