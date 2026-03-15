package com.example.criminalgalorpot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.criminalgalorpot.model.Crime;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CrimeAdapter extends RecyclerView.Adapter<CrimeAdapter.CrimeHolder> {

    private List<Crime> crimes;
    private final OnCrimeClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());

    public interface OnCrimeClickListener {
        void onCrimeClick(Crime crime);
    }

    public CrimeAdapter(List<Crime> crimes, OnCrimeClickListener listener) {
        this.crimes = crimes;
        this.listener = listener;
    }

    public void setCrimes(List<Crime> crimes) {
        this.crimes = crimes;
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
        Crime crime = crimes.get(position);
        holder.bind(crime, listener);
    }

    @Override
    public int getItemCount() {
        return crimes.size();
    }

    class CrimeHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView dateTextView;
        private final TextView solvedTextView;
        private final ImageView photoImageView;

        CrimeHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.crime_title);
            dateTextView = itemView.findViewById(R.id.crime_date);
            solvedTextView = itemView.findViewById(R.id.crime_solved_label);
            photoImageView = itemView.findViewById(R.id.crime_photo);
        }

        void bind(Crime crime, OnCrimeClickListener listener) {
            String title = crime.getTitle().trim().isEmpty() ? "(No title)" : crime.getTitle();
            titleTextView.setText(title);
            dateTextView.setText(dateFormat.format(crime.getDate()));
            solvedTextView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);

            if (crime.getPhotoPath() != null) {
                photoImageView.setVisibility(View.VISIBLE);
                photoImageView.setImageURI(android.net.Uri.parse(crime.getPhotoPath()));
            } else {
                photoImageView.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onCrimeClick(crime));
        }
    }
}
