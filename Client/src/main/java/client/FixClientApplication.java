package client;

import lombok.extern.log4j.Log4j2;
import quickfix.*;
import quickfix.field.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;


@Log4j2
public class FixClientApplication implements Application
{
    private static final String publicKey = "<PUBLIC_KEY_OR_PASSWORD>";
    private static final String privateKeyFile = "<PRIVATE_KEY_ED25519>";
    private static final String TIMESTAMP_PATTERN = "yyyyMMdd-HH:mm:ss.SSS";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

    private final Signature signer;
    private PrivateKey privateKey = null;

    public FixClientApplication()
    {

        try {
            signer = Signature.getInstance("Ed25519");
            privateKey = loadPrivateKey();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        String privateKeyStr = Files.readAllLines(Paths.get(privateKeyFile)).get(1);
        byte[] decoded = Base64.getDecoder().decode(privateKeyStr.getBytes(StandardCharsets.UTF_8));

        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

        return keyFactory.generatePrivate(keySpec);
    }

    public static String getCurrentTime() {
        return FORMATTER.format( new Date().toInstant().atZone(ZoneOffset.UTC));
    }

    @Override
    public void onCreate(SessionID sessionID) {
        log.info("onCreate");
    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.info("Session is logged on");
    }

    @Override
    public void onLogout(SessionID sessionID) {
        log.info("Session {} is over", sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        log.info("To admin ({}): {}", sessionId, message);
        try {
            final String msgType = message.getHeader().getString(MsgType.FIELD);
            if (MsgType.LOGON.compareTo(msgType) == 0) {
                addBinanceAuthMessageParams(message);
            }
        } catch (FieldNotFound | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        log.info("From admin ({}): {}", sessionID, message);
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        log.info("Message is being sent:{}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        log.info("Message received:{}", message);
    }

    private void addBinanceAuthMessageParams(Message message) throws InvalidKeyException, SignatureException
    {
        final String timestamp = getCurrentTime();
        List<String> params = List.of(
                "A",
                "1001",
                "SPOT",
                "1",
                timestamp);

        final String data = String.join( Character.toString(1), params);
        final byte[] payload = data.getBytes();

        signer.initSign(privateKey);
        signer.update(payload);
        byte[] signatureBytes = signer.sign();

        final String rawData = new String(Base64.getEncoder().encode(signatureBytes));

        message.setString(EncryptMethod.FIELD,  String.valueOf(0));
        message.setString(Username.FIELD, publicKey);  // ApiKey (when using PrivateKey-based auth)
        // message.setString(Password.FIELD, publicKey);     // Password
        message.setString(RawDataLength.FIELD,  String.valueOf(rawData.length()));
        message.setString(RawData.FIELD,  rawData);    // Signature generated
    }

}