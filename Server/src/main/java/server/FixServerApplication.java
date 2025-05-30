package server;

import lombok.extern.log4j.Log4j2;
import quickfix.*;
import quickfix.field.Text;
import quickfix.fix44.QuoteRequest;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class FixServerApplication implements Application
{
    private Session defaultSession;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public FixServerApplication()
    {
        scheduledExecutorService.scheduleAtFixedRate(this::sendMessage, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onCreate(SessionID sessionID) {

    }

    @Override
    public void onLogon(SessionID sessionID)
    {
        log.info("Session is logged on");
        defaultSession = Session.lookupSession(sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID)
    {
        log.info("Session {} is over", sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID)
    {
        log.info("To admin ({}): {}", sessionID, message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon
    {
        log.info("From admin ({}): {}", sessionID, message);
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend
    {
        log.info("Message is being sent:{}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType
    {
        log.info("Message received:{}", message);
    }

    private void sendMessage()
    {
        final String testMessage = "*** SERVER HELLO ***";
        if (isLoggedOn())
        {
            QuoteRequest quoteRequest = new QuoteRequest();
            quoteRequest.setString(Text.FIELD, testMessage);
            defaultSession.send(quoteRequest);
            log.info("Test messages '" +testMessage + "' has been send");
        } else {
            log.info("session is not logged in, message will not send");
        }
    }

    private boolean isLoggedOn() {
        return defaultSession != null && defaultSession.isLoggedOn();
    }
}