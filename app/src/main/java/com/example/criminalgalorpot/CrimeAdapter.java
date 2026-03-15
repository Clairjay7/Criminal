package com.example.criminalgalorpot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.criminalgalorpot.model.Crime;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CrimeAdapter extends RecyclerView.Adapter<CrimeAdapter.CrimeHolder> {

    private List<Crime> crimes;
    private final OnCrimeClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());

    public interface OnCrimeClickListener {
        void onCrimeClick(int position);
    }

    public CrimeAdapter(List<Crime> crimes, OnCrimeClickListener listener) {
        this.crimes = crimes != null ? crimes : new ArrayList<>();
        this.listener = listener;
    }

    public void setCrimes(List<Crime> crimes) {
        this.crimes = crimes != null ? crimes : new ArrayList<>();
    }

    @NonNull
    @Override
    public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_crime, parent, false);
        return new CrimeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
        if (crimes == null || position < 0 || position >= crimes.size()) return;
        Crime crime = crimes.get(position);
        holder.bind(crime, listener);
    }

    @Override
    public int getItemCount() {
        return crimes == null ? 0 : crimes.size();
    }

    class CrimeHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView dateTextView;
        private final ImageView photoImageView;
        private final ImageView solvedIconView;
        private final Button callPoliceButton;

        CrimeHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.crime_title);
            dateTextView = itemView.findViewById(R.id.crime_date);
            photoImageView = itemView.findViewById(R.id.crime_photo);
            solvedIconView = itemView.findViewById(R.id.crime_solved_icon);
            callPoliceButton = itemView.findViewById(R.id.call_police_button);
        }

        void bind(Crime crime, OnCrimeClickListener listener) {
            try {
                String title = crime != null && crime.getTitle() != null ? crime.getTitle().trim() : "";
                if (title.isEmpty()) title = "(No title)";
                if (titleTextView != null) titleTextView.setText(title);
                if (dateTextView != null) dateTextView.setText(crime != null && crime.getDate() != null ? dateFormat.format(crime.getDate()) : "");
                if (solvedIconView != null) {
                    solvedIconView.setVisibility(crime != null && crime.isSolved() ? View.VISIBLE : View.GONE);
                    solvedIconView.setClickable(false);
                    solvedIconView.setFocusable(false);
                }

                if (photoImageView != null) {
                    if (crime != null && crime.getPhotoPath() != null && !crime.getPhotoPath().isEmpty()) {
                        File f = new File(crime.getPhotoPath());
                        if (f.exists()) {
                            photoImageView.setVisibility(View.VISIBLE);
                            photoImageView.setImageURI(android.net.Uri.fromFile(f));
                        } else {
                            photoImageView.setVisibility(View.GONE);
                        }
                    } else {
                        photoImageView.setVisibility(View.GONE);
                    }
                    photoImageView.setClickable(false);
                    photoImageView.setFocusable(false);
                }

                if (callPoliceButton != null) {
                    callPoliceButton.setOnClickListener(v -> {
                        try {
                            Toast.makeText(v.getContext(), R.string.calling_police, Toast.LENGTH_SHORT).show();
                        } catch (Exception ignored) {
                        }
                    });
                }

                if (itemView != null) {
                    itemView.setOnClickListener(v -> {
                        try {
                            int pos = getAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION && listener != null) listener.onCrimeClick(pos);
                        } catch (Exception ignored) {
                        }
                    });
                }
            } catch (Exception ignored) {
            }
        }
    }
}
