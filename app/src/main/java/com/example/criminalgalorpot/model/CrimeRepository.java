package com.example.criminalgalorpot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeRepository {
    private static final CrimeRepository INSTANCE = new CrimeRepository();
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
}
