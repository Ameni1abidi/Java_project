package tn.esprit.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;

import java.io.*;
import java.util.List;

public class GoogleCalendarConfig {

    private static final String APPLICATION_NAME = "EduFlex";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR);
    private static final String TOKENS_DIRECTORY = "tokens";

    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        InputStream in = GoogleCalendarConfig.class
                .getClassLoader()
                .getResourceAsStream("credentials.json");

        if (in == null) {
            throw new FileNotFoundException("credentials.json introuvable dans resources");
        }

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static String getApplicationName() {
        return APPLICATION_NAME;
    }
}