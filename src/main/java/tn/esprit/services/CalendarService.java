package tn.esprit.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import tn.esprit.entities.Examen;
import tn.esprit.utils.GoogleCalendarConfig;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalendarService {

    private static final String PRIMARY_CALENDAR_ID = "primary";
    private static final String TIMEZONE = "Africa/Tunis";
    private static final Logger LOGGER = Logger.getLogger(CalendarService.class.getName());

    private Calendar calendarService;

    public CalendarService() throws GeneralSecurityException, IOException {
        initializeCalendarService();
    }

    private void initializeCalendarService() throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        this.calendarService = new Calendar.Builder(
                HTTP_TRANSPORT,
                GsonFactory.getDefaultInstance(),
                GoogleCalendarConfig.getCredentials(HTTP_TRANSPORT)
        )
                .setApplicationName(GoogleCalendarConfig.getApplicationName())
                .build();

        LOGGER.info("Google Calendar Service initialized successfully");
    }

    public String createExamEvent(Examen examen) throws IOException {

        Event event = buildEventFromExamen(examen);

        Event createdEvent = calendarService.events()
                .insert(PRIMARY_CALENDAR_ID, event)
                .execute();

        LOGGER.info("Event created: " + examen.getTitre());

        return createdEvent.getId();
    }

    public void updateExamEvent(Examen examen, String eventId) throws IOException {

        Event event = buildEventFromExamen(examen);

        calendarService.events()
                .update(PRIMARY_CALENDAR_ID, eventId, event)
                .execute();

        LOGGER.info("Event updated: " + examen.getTitre());
    }

    public void deleteExamEvent(String eventId) throws IOException {

        calendarService.events()
                .delete(PRIMARY_CALENDAR_ID, eventId)
                .execute();

        LOGGER.info("Event deleted");
    }

    private Event buildEventFromExamen(Examen examen) {

        Event event = new Event();

        event.setSummary("Examen - " + examen.getTitre());
        event.setDescription(
                "Type: " + examen.getType() + "\n" +
                        "Durée: " + examen.getDuree() + " min"
        );

        event.setLocation("Esprit");

        LocalDateTime start = convertDate(examen.getDateExamen());
        LocalDateTime end = start.plusMinutes(examen.getDuree());

        event.setStart(toEventDateTime(start));
        event.setEnd(toEventDateTime(end));

        event.setColorId("8");

        // ✅ FIX COMPLET REMINDERS
        event.setReminders(
                new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(
                                new EventReminder()
                                        .setMethod("popup")
                                        .setMinutes(60)
                        ))
        );

        return event;
    }

    private LocalDateTime convertDate(LocalDate date) {
        return (date != null)
                ? date.atTime(9, 0)
                : LocalDateTime.now().plusDays(1);
    }

    private EventDateTime toEventDateTime(LocalDateTime ldt) {
        DateTime dt = new DateTime(java.sql.Timestamp.valueOf(ldt));

        return new EventDateTime()
                .setDateTime(dt)
                .setTimeZone(TIMEZONE);
    }

    public boolean testConnection() {
        try {
            calendarService.calendars()
                    .get(PRIMARY_CALENDAR_ID)
                    .execute();

            LOGGER.info("Google Calendar OK");
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Calendar connection failed", e);
            return false;
        }
    }
}