package com.example.criminalgalorpot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CrimeDetailActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_INDEX = "com.example.criminalgalorpot.crime_index";
    private static final String SAVE_CRIME_INDEX = "crime_index";
    private static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_CONTACT = 1;

    private Crime crime;
    private int crimeIndex = -1;
    private EditText titleField;
    private TextView dateButton;
    private Switch solvedSwitch;
    private EditText suspectField;
    private Button shareButton;
    private ImageView photoView;
    private TextView pageIndicator;
    // Show both date and time in the UI
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy h:mm a", Locale.getDefault());
    private volatile CountDownLatch photoCopyLatch;

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
        if (crimes == null || index < 0 || index >= crimes.size()) {
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
                titleField.setText(crime.getTitle() != null ? crime.getTitle() : "");
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
            if (dateButton != null) dateButton.setText(crime.getDate() != null ? dateFormat.format(crime.getDate()) : "");
            Button changeDateButton = findViewById(R.id.change_date_button);
            if (changeDateButton != null) changeDateButton.setOnClickListener(v -> {
                try { showDatePicker(); } catch (Exception ignored) { }
            });

            solvedSwitch = findViewById(R.id.crime_solved);
            if (solvedSwitch != null) {
                solvedSwitch.setChecked(crime.isSolved());
                updateCaseStatusText();
                solvedSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    crime.setSolved(isChecked);
                    updateCaseStatusText();
                });
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

            Button chooseSuspectButton = findViewById(R.id.choose_suspect_button);
            if (chooseSuspectButton != null) {
                chooseSuspectButton.setOnClickListener(v -> {
                    try { pickContact(); } catch (Exception ignored) { }
                });
            }

            photoView = findViewById(R.id.crime_photo);
            updatePhotoImage();
            if (photoView != null) {
                photoView.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(intent, REQUEST_PHOTO);
                    } catch (Exception ignored) { }
                });
            }

            shareButton = findViewById(R.id.crime_share);
            if (shareButton != null) shareButton.setOnClickListener(v -> {
                try { shareReport(); } catch (Exception ignored) { }
            });

            Button saveButton = findViewById(R.id.save_button);
            if (saveButton != null) saveButton.setOnClickListener(v -> {
                try {
                    saveAndReturnToList();
                } catch (Throwable ignored) {
                }
            });

            // Setup swipe + paging controls AFTER views are bound
            setupPaging(index);
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
            try { saveAndReturnToList(); } catch (Throwable ignored) { }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAndReturnToList() {
        try {
            CrimeRepository.getInstance().save(getApplicationContext());
        } catch (Throwable ignored) {
        }
        finish();
    }

    /**
     * Load the current crime and update all views + indicator.
     */
    private void bindCrime() {
        java.util.List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        if (crimes == null || crimeIndex < 0 || crimeIndex >= crimes.size()) {
            finish();
            return;
        }
        crime = crimes.get(crimeIndex);

        if (titleField != null) {
            titleField.setText(crime.getTitle() != null ? crime.getTitle() : "");
        }
        if (dateButton != null) {
            Date d = crime.getDate();
            dateButton.setText(d != null ? dateFormat.format(d) : "");
        }
        if (solvedSwitch != null) {
            solvedSwitch.setChecked(crime.isSolved());
        }
        if (suspectField != null) {
            suspectField.setText(crime.getSuspect() != null ? crime.getSuspect() : "");
        }
        updatePhotoImage();

        if (pageIndicator != null) {
            int current = crimeIndex + 1;
            java.util.List<Crime> list = crimes;
            int total = (list != null) ? list.size() : 0;
            pageIndicator.setText("Crime " + current + " / " + total);
        }
    }

    /**
     * Initializes swipe navigation and first/last buttons.
     */
    private void setupPaging(int index) {
        crimeIndex = index;
        bindCrime();

        // Optional first/last buttons if present in layout
        Button firstButton = findViewById(R.id.first_button);
        Button lastButton = findViewById(R.id.last_button);
        if (firstButton != null) {
            firstButton.setOnClickListener(v -> goToFirstCrime());
        }
        if (lastButton != null) {
            lastButton.setOnClickListener(v -> goToLastCrime());
        }

        // Indicator text view
        pageIndicator = findViewById(R.id.crime_page_indicator);

        final android.view.GestureDetector gestureDetector =
                new android.view.GestureDetector(this, new android.view.GestureDetector.SimpleOnGestureListener() {
                    private static final int SWIPE_THRESHOLD = 50;
                    private static final int SWIPE_VELOCITY_THRESHOLD = 50;

                    @Override
                    public boolean onFling(android.view.MotionEvent e1, android.view.MotionEvent e2,
                                           float velocityX, float velocityY) {
                        if (e1 == null || e2 == null) return false;
                        float diffX = e2.getX() - e1.getX();
                        float diffY = e2.getY() - e1.getY();

                        if (Math.abs(diffX) > Math.abs(diffY)
                                && Math.abs(diffX) > SWIPE_THRESHOLD
                                && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                            // Finger moves to the right => go to NEXT crime
                            if (diffX > 0) {
                                goToNextCrime();
                            } else {
                                goToPreviousCrime();
                            }
                            return true;
                        }
                        return false;
                    }
                });

        // Attach to the ScrollView so it reliably receives horizontal flings
        View scroll = findViewById(R.id.crime_detail_scroll);
        if (scroll != null) {
            scroll.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                // Don't consume event so normal clicks/scrolls still work
                return false;
            });
        }
    }

    @Override
    public void onBackPressed() {
        saveAndReturnToList();
    }

<<<<<<< HEAD
    private void goToNextCrime() {
        java.util.List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        if (crimes == null) return;
        if (crimeIndex < crimes.size() - 1) {
            crimeIndex++;
            bindCrime();
        }
    }

    private void goToPreviousCrime() {
        if (crimeIndex > 0) {
            crimeIndex--;
            bindCrime();
        }
    }

    private void goToFirstCrime() {
        java.util.List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        if (crimes == null || crimes.isEmpty()) return;
        crimeIndex = 0;
        bindCrime();
    }

    private void goToLastCrime() {
        java.util.List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        if (crimes == null || crimes.isEmpty()) return;
        crimeIndex = crimes.size() - 1;
        bindCrime();
    }

    private void updateCaseStatusText() {
        TextView statusView = findViewById(R.id.case_status_text);
        if (statusView == null || crime == null) return;
        if (crime.isSolved()) {
            statusView.setText("CLOSE CASE");
        } else {
            statusView.setText("OPEN CASE");
        }
    }

=======
>>>>>>> origin/main
    private void goToNextCrime() {
        java.util.List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        if (crimes == null) return;
        if (crimeIndex < crimes.size() - 1) {
            crimeIndex++;
            bindCrime();
        }
    }

    private void goToPreviousCrime() {
        if (crimeIndex > 0) {
            crimeIndex--;
            bindCrime();
        }
    }

    private void goToFirstCrime() {
        java.util.List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        if (crimes == null || crimes.isEmpty()) return;
        crimeIndex = 0;
        bindCrime();
    }

    private void goToLastCrime() {
        java.util.List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        if (crimes == null || crimes.isEmpty()) return;
        crimeIndex = crimes.size() - 1;
        bindCrime();
    }

    private void updateCaseStatusText() {
        TextView statusView = findViewById(R.id.case_status_text);
        if (statusView == null || crime == null) return;
        if (crime.isSolved()) {
            statusView.setText("CLOSE CASE");
        } else {
            statusView.setText("OPEN CASE");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode != RESULT_OK || data == null) return;
            Uri uri = data.getData();
            if (uri == null) return;
            if (requestCode == REQUEST_PHOTO) {
                if (crime == null) return;
                final Uri photoUri = uri;
                final android.content.Context appContext = getApplicationContext();
                final Crime currentCrime = crime;
                photoCopyLatch = new CountDownLatch(1);
                new Thread(() -> {
                    try {
                        final String savedPath = copyPhotoToAppStorage(appContext, currentCrime, photoUri);
                        runOnUiThread(() -> {
                            try {
                                if (crime != null && savedPath != null) {
                                    crime.setPhotoPath(savedPath);
                                    try {
                                        CrimeRepository.getInstance().save(appContext);
                                    } catch (Exception ignored) { }
                                }
                                updatePhotoImage();
                            } catch (Exception ignored) { }
                            finally {
                                if (photoCopyLatch != null) {
                                    photoCopyLatch.countDown();
                                    photoCopyLatch = null;
                                }
                            }
                        });
                    } catch (Throwable t) {
                        if (photoCopyLatch != null) {
                            photoCopyLatch.countDown();
                            photoCopyLatch = null;
                        }
                    }
                }).start();
            } else if (requestCode == REQUEST_CONTACT) {
                String name = getContactName(uri);
                if (name != null && crime != null) {
                    crime.setSuspect(name);
                    if (suspectField != null) suspectField.setText(name);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void pickContact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT);
            return;
        }
        launchContactPicker();
    }

    private void launchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchContactPicker();
        }
    }

    private String getContactName(Uri contactUri) {
        String[] projection = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    private String copyPhotoToAppStorage(Context context, Crime c, Uri sourceUri) {
        if (c == null || sourceUri == null || context == null) return null;
        try {
            File dir = context.getFilesDir();
            if (dir == null) return null;
            File photosDir = new File(dir, "crime_photos");
            if (!photosDir.exists() && !photosDir.mkdirs()) return null;
            File destFile = new File(photosDir, c.getId().toString() + ".jpg");
            InputStream in = null;
            try {
                in = context.getContentResolver().openInputStream(sourceUri);
                if (in == null) return null;
                try (FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    long total = 0;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                        total += len;
                        if (total > 20 * 1024 * 1024) break;
                    }
                }
                return destFile.exists() ? destFile.getAbsolutePath() : null;
            } finally {
                if (in != null) try { in.close(); } catch (Exception ignored) { }
            }
        } catch (Throwable e) {
            try {
                File dir = context.getFilesDir();
                if (dir != null) {
                    File f = new File(new File(dir, "crime_photos"), c.getId().toString() + ".jpg");
                    if (f.exists()) f.delete();
                }
            } catch (Throwable ignored) { }
            return null;
        }
    }

    private void updatePhotoImage() {
        if (crime == null || photoView == null) return;
        String path = crime.getPhotoPath();
        if (path != null && !path.isEmpty()) {
            File f = new File(path);
            if (f.exists()) {
                photoView.setImageURI(Uri.fromFile(f));
            } else if (path.startsWith("content://")) {
                photoView.setImageURI(Uri.parse(path));
            } else {
                photoView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            photoView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (photoCopyLatch != null) {
                try {
                    photoCopyLatch.await(4, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                photoCopyLatch = null;
            }
            CrimeRepository.getInstance().save(getApplicationContext());
        } catch (Exception ignored) {
        }
    }

    private void showDatePicker() {
        if (crime == null) return;
        final Calendar cal = Calendar.getInstance();
        if (crime.getDate() != null) {
            cal.setTime(crime.getDate());
        }

        new android.app.DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // After picking the date, show a TimePicker to select time
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int minute = cal.get(Calendar.MINUTE);

                    new android.app.TimePickerDialog(this,
                            (timeView, hourOfDay, minuteOfHour) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                cal.set(Calendar.MINUTE, minuteOfHour);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);

                                Date newDateTime = cal.getTime();
                                crime.setDate(newDateTime);
                                if (dateButton != null) {
                                    dateButton.setText(dateFormat.format(newDateTime));
                                }
                            },
                            hour,
                            minute,
                            android.text.format.DateFormat.is24HourFormat(this)
                    ).show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void shareReport() {
        if (crime == null) return;
        String title = crime.getTitle() != null && !crime.getTitle().isEmpty() ? crime.getTitle() : "(No title)";
        String solvedString = crime.isSolved() ? "Solved" : "Unsolved";
        String suspectString = crime.getSuspect() != null ? crime.getSuspect() : "No suspect";
        String dateStr = crime.getDate() != null ? dateFormat.format(crime.getDate()) : "";
        String report = "Crime: " + title + "\n"
                + "Date: " + dateStr + "\n"
                + "Status: " + solvedString + "\n"
                + "Suspect: " + suspectString;

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Crime Report");
        sendIntent.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(sendIntent, "Share crime report with"));
    }
}
