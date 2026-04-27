package tn.esprit.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String FROM_NUMBER = "+19205261882";

    static {
        if (ACCOUNT_SID == null || AUTH_TOKEN == null) {
            throw new RuntimeException("Twilio credentials missing in environment variables");
        }
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendSms(String to, String text) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(FROM_NUMBER),
                text
        ).create();
    }
}