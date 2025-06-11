package com.example.eventorganizer.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "EventAppFirebaseSession";
    private static final String KEY_USER_ROLE = "userRole";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setUserRole(String role) {
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "user"); // Default to "user" if not found
    }

    public boolean isAdmin() {
        return "admin".equals(getUserRole());
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}