package com.vs18.taskmanager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import java.io.IOException;
import java.util.Collections;
import java.util.TimeZone;

public class GoogleCalendarHelper {
    private static final String TAG = "GoogleCalendarHelper";
    private static final int REQUEST_CODE_SIGN_IN = 1001;
    private static final int REQUEST_CODE_AUTH = 1002;
    private final GoogleSignInClient googleSignInClient;
    private final Activity activity;
    private final GoogleAccountCredential credential;
    private GoogleSignInAccount signedInAccount;

    public GoogleCalendarHelper(Activity activity) {
        this.activity = activity;
        this.credential = GoogleAccountCredential.usingOAuth2(
                        activity, Collections.singleton(com.google.api.services.calendar.CalendarScopes.CALENDAR))
                .setBackOff(new ExponentialBackOff());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/calendar"))
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public boolean isUserSignedIn() {
        signedInAccount = GoogleSignIn.getLastSignedInAccount(activity);
        return signedInAccount != null;
    }

    public void signInIfNeeded() {
        if (!isUserSignedIn()) {
            activity.startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        } else {
            Log.i(TAG, "User already signed in.");
            setAccountName(signedInAccount.getEmail());
        }
    }

    public void setAccountName(String accountName) {
        if (accountName != null && !accountName.isEmpty()) {
            credential.setSelectedAccountName(accountName);
            Log.d(TAG, "Account name set: " + accountName);
        } else {
            Log.e(TAG, "❌ Account name is null or empty.");
        }
    }

    public void saveEventId(String eventId) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("GoogleCalendarPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("eventId", eventId);
        editor.apply();
        Log.d(TAG, "Event ID saved: " + eventId);
    }

    public String getSavedEventId() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("GoogleCalendarPrefs", Activity.MODE_PRIVATE);
        return sharedPreferences.getString("eventId", null);
    }

    public void addTaskToCalendar(String title, String description, String dateTime) {
        new Thread(() -> {
            try {
                if (credential.getSelectedAccountName() == null) {
                    Log.e(TAG, "❌ Account not signed in");
                    signInIfNeeded();
                    return;
                }

                GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                Calendar service = new Calendar.Builder(
                        new com.google.api.client.http.javanet.NetHttpTransport(),
                        jsonFactory, credential)
                        .setApplicationName("TaskManager")
                        .build();

                DateTime startDateTime = DateTime.parseRfc3339(dateTime);
                DateTime endDateTime = new DateTime(startDateTime.getValue() + 3600000);

                Event event = new Event()
                        .setSummary(title)
                        .setDescription(description)
                        .setStart(new EventDateTime().setDateTime(startDateTime).setTimeZone(TimeZone.getDefault().getID()))
                        .setEnd(new EventDateTime().setDateTime(endDateTime).setTimeZone(TimeZone.getDefault().getID()));

                Event createdEvent = service.events().insert("primary", event).execute();
                String eventId = createdEvent.getId();
                saveEventId(eventId);

                Log.d(TAG, "Event created with ID: " + eventId);

            } catch (UserRecoverableAuthIOException e) {
                Log.e(TAG, "❌ Authentication required: " + e.getMessage(), e);
                activity.startActivityForResult(e.getIntent(), REQUEST_CODE_AUTH);
            } catch (IOException e) {
                Log.e(TAG, "❌ Error creating event: " + e.getMessage(), e);
            }
        }).start();
    }

    public void updateEventDate(String eventId, String newStartDateTime, String newEndDateTime) {
        new Thread(() -> {
            try {
                if (credential.getSelectedAccountName() == null) {
                    Log.e(TAG, "❌ Account not signed in");
                    signInIfNeeded();
                    return;
                }

                GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                Calendar service = new Calendar.Builder(
                        new com.google.api.client.http.javanet.NetHttpTransport(),
                        jsonFactory, credential)
                        .setApplicationName("TaskManager")
                        .build();

                Event event = service.events().get("primary", eventId).execute();
                Log.d(TAG, "Retrieved event: " + event.toString());

                DateTime startDateTime = DateTime.parseRfc3339(newStartDateTime);
                DateTime endDateTime = DateTime.parseRfc3339(newEndDateTime);

                event.setStart(new EventDateTime().setDateTime(startDateTime).setTimeZone(TimeZone.getDefault().getID()));
                event.setEnd(new EventDateTime().setDateTime(endDateTime).setTimeZone(TimeZone.getDefault().getID()));

                service.events().update("primary", event.getId(), event).execute();
                Log.d(TAG, "✅ Event date updated in Google Calendar!");

            } catch (UserRecoverableAuthIOException e) {
                Log.e(TAG, "❌ Authentication required: " + e.getMessage(), e);
                activity.startActivityForResult(e.getIntent(), REQUEST_CODE_AUTH);
            } catch (IOException e) {
                Log.e(TAG, "❌ Error updating event: " + e.getMessage(), e);
            }
        }).start();
    }

    public void updateEventFromSavedId(String newStartDateTime, String newEndDateTime) {
        String eventId = getSavedEventId();
        if (eventId != null && !eventId.isEmpty()) {
            updateEventDate(eventId, newStartDateTime, newEndDateTime);
        } else {
            Log.e(TAG, "❌ Event ID not found or invalid. Cannot update event.");
        }
    }
}