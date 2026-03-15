package com.example.criminalgalorpot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.criminalgalorpot.model.Crime;
import com.example.criminalgalorpot.model.CrimeRepository;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CrimeDetailActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_INDEX = "com.example.criminalgalorpot.crime_index";
    private static final String SAVE_CRIME_INDEX = "crime_index";

    private Crime crime;
    private int crimeIndex = -1;
    private EditText titleField;
    private TextView dateButton;
    private Switch solvedSwitch;
    private EditText suspectField;
    private Button photoButton;
    private Button shareButton;
    private ImageView photoView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());

    /** Open detail for a crime by its position in the list. Use this for list clicks and for new crime (pass size-1). */
    public static Intent newIntent(Context context, int crimeIndex) {
        Intent intent = new Intent(context, CrimeDetailActivity.class);
        intent.putExtra(EXTRA_CRIME_INDEX, crimeIndex);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int index = -1;
        if (savedInstanceState != null) {
            index = savedInstanceState.getInt(SAVE_CRIME_INDEX, -1);
        }
        if (index < 0 && getIntent() != null) {
            index = getIntent().getIntExtra(EXTRA_CRIME_INDEX, -1);
        }
        java.util.List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        if (index < 0 || index >= crimes.size()) {
            finish();
            return;
        }
        crime = crimes.get(index);
        crimeIndex = index;

        try {
            setContentView(R.layout.activity_crime_detail);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Crime Details");
            }

            titleField = findViewById(R.id.crime_title);
            if (titleField != null) {
                titleField.setText(crime.getTitle());
                titleField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        crime.setTitle(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            dateButton = findViewById(R.id.crime_date);
            if (dateButton != null) dateButton.setText(dateFormat.format(crime.getDate()));
            Button changeDateButton = findViewById(R.id.change_date_button);
            if (changeDateButton != null) changeDateButton.setOnClickListener(v -> showDatePicker());

            solvedSwitch = findViewById(R.id.crime_solved);
            if (solvedSwitch != null) {
                solvedSwitch.setChecked(crime.isSolved());
                solvedSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> crime.setSolved(isChecked));
            }

            suspectField = findViewById(R.id.crime_suspect);
            if (suspectField != null) {
                suspectField.setText(crime.getSuspect() != null ? crime.getSuspect() : "");
                suspectField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        crime.setSuspect(s.toString().trim().isEmpty() ? null : s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }

            photoView = findViewById(R.id.crime_photo);
            photoButton = findViewById(R.id.crime_photo_button);
            updatePhotoButtonAndImage();
            if (photoButton != null) {
                photoButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select photo"), 0);
                });
            }

            shareButton = findViewById(R.id.crime_share);
            if (shareButton != null) shareButton.setOnClickListener(v -> shareReport());

            Button saveButton = findViewById(R.id.save_button);
            if (saveButton != null) saveButton.setOnClickListener(v -> finish());
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (crimeIndex >= 0) outState.putInt(SAVE_CRIME_INDEX, crimeIndex);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            crime.setPhotoPath(uri.toString());
            updatePhotoButtonAndImage();
        }
    }

    private void updatePhotoButtonAndImage() {
        if (crime == null || photoButton == null || photoView == null) return;
        if (crime.getPhotoPath() != null) {
            photoButton.setText(R.string.change_photo);
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageURI(Uri.parse(crime.getPhotoPath()));
        } else {
            photoButton.setText(R.string.attach_photo);
            photoView.setVisibility(View.GONE);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(crime.getDate());
        new android.app.DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    Date newDate = cal.getTime();
                    crime.setDate(newDate);
                    dateButton.setText(dateFormat.format(newDate));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void shareReport() {
        String title = crime.getTitle().isEmpty() ? "(No title)" : crime.getTitle();
        String solvedString = crime.isSolved() ? "Solved" : "Unsolved";
        String suspectString = crime.getSuspect() != null ? crime.getSuspect() : "No suspect";
        String report = "Crime: " + title + "\n"
                + "Date: " + dateFormat.format(crime.getDate()) + "\n"
                + "Status: " + solvedString + "\n"
                + "Suspect: " + suspectString;

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Crime Report");
        sendIntent.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(sendIntent, "Share crime report with"));
    }
}
