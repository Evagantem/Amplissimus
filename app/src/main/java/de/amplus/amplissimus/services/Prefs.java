package de.amplus.amplissimus.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.sematre.dsbmobile.utils.GZIP;

public class Prefs {

    public Prefs(@NotNull Activity activity) {
        authPreferences = activity.getSharedPreferences("auth", Context.MODE_PRIVATE);
        cachePreferences = activity.getSharedPreferences("cache", Context.MODE_PRIVATE);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public Prefs(@NotNull Service service) {
        authPreferences = service.getSharedPreferences("auth", Context.MODE_PRIVATE);
        cachePreferences = service.getSharedPreferences("cache", Context.MODE_PRIVATE);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(service);
    }

    SharedPreferences authPreferences;
    SharedPreferences cachePreferences;
    SharedPreferences defaultPreferences;

    public boolean bgServicesOn() {
        return defaultPreferences.getBoolean("background_services", false);
    }

    public void setUsername(String s) {
        authPreferences.edit().putString("username", s).apply();
    }

    public void setPassword(String s) {
        authPreferences.edit().putString("password", s).apply();
    }

    public String getUsername() {
        return authPreferences.getString("username", null);
    }

    public String getPassword() {
        return authPreferences.getString("password", null);
    }

    public void clear() {
        authPreferences.edit().clear().apply();
    }

    public List<DSBService.Plan> getPlans() {
        if(cachePreferences.getString("plans", null) == null) return null;
        try {
            String parseableString = GZIP.decompress(Base64.decode(
                    cachePreferences.getString("plans", null),
                    Base64.DEFAULT
            ));
            DSBService.Plan[] plans = new Gson().fromJson(
                    parseableString,
                    DSBService.Plan[].class
            );
            return Arrays.asList(plans);
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public void setPlans(List<DSBService.Plan> plans) {
        try {
            String plansString =
                    Base64.encodeToString(GZIP.compress(new Gson().toJson(plans)), Base64.DEFAULT);
            cachePreferences.edit().putString("plans", plansString).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
