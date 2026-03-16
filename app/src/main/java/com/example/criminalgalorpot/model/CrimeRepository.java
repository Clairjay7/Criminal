package com.example.criminalgalorpot.model;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeRepository {
    private static final CrimeRepository INSTANCE = new CrimeRepository();
    private static final String FILENAME = "crimes.json";
    private final List<Crime> crimes = new ArrayList<>();

    static {
        for (int i = 0; i < 5; i++) {
            Crime crime = new Crime("Crime #" + i);
            crime.setSolved(i % 2 == 0);
            INSTANCE.crimes.add(crime);
        }
    }

    public static CrimeRepository getInstance() {
        return INSTANCE;
    }

    public List<Crime> getCrimes() {
        return crimes;
    }

    public Crime getCrime(UUID id) {
        for (Crime crime : crimes) {
            if (crime.getId().equals(id)) {
                return crime;
            }
        }
        return null;
    }

    public void addCrime(Crime crime) {
        crimes.add(crime);
    }

    public void load(Context context) {
        if (context == null) return;
        try {
            File dir = context.getFilesDir();
            if (dir == null) return;
            File file = new File(dir, FILENAME);
            if (!file.exists() || !file.isFile()) return;
            long len = file.length();
            if (len <= 0 || len > 2 * 1024 * 1024) return;
            int size = (int) len;
            InputStream is = new FileInputStream(file);
            byte[] buf = new byte[size];
            int read = 0;
            while (read < buf.length) {
                int n = is.read(buf, read, buf.length - read);
                if (n <= 0) break;
                read += n;
            }
            is.close();
            String json = new String(buf, 0, read, StandardCharsets.UTF_8);
            JSONArray arr = new JSONArray(json);
            List<Crime> loaded = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                UUID id = UUID.fromString(o.getString("id"));
                String title = o.optString("title", "");
                long dateLong = o.optLong("date", System.currentTimeMillis());
                boolean solved = o.optBoolean("solved", false);
                String suspect = o.has("suspect") && !o.isNull("suspect") ? o.getString("suspect") : null;
                String photoPath = o.has("photoPath") && !o.isNull("photoPath") ? o.getString("photoPath") : null;
                Crime c = new Crime(id, title, new java.util.Date(dateLong), solved, suspect, photoPath);
                loaded.add(c);
            }
            crimes.clear();
            crimes.addAll(loaded);
        } catch (Throwable ignored) {
        }
    }

    public void save(Context context) {
        if (context == null) return;
        try {
            File dir = context.getFilesDir();
            if (dir == null) return;
            JSONArray arr = new JSONArray();
            for (Crime c : crimes) {
                JSONObject o = new JSONObject();
                o.put("id", c.getId() != null ? c.getId().toString() : "");
                o.put("title", c.getTitle() != null ? c.getTitle() : "");
                o.put("date", c.getDate() != null ? c.getDate().getTime() : System.currentTimeMillis());
                o.put("solved", c.isSolved());
                o.put("suspect", c.getSuspect() != null ? c.getSuspect() : JSONObject.NULL);
                o.put("photoPath", c.getPhotoPath() != null ? c.getPhotoPath() : JSONObject.NULL);
                arr.put(o);
            }
            File file = new File(dir, FILENAME);
            try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
                w.write(arr.toString());
            }
        } catch (Throwable ignored) {
        }
    }
}
