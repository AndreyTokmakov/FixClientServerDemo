package client;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import quickfix.*;

import javax.annotation.PostConstruct;

@Log4j2
@Service
public class InitiatorService {

    @Value("${fix.config.path}")
    private String configPath;

    private Connector createInitiator(Application application, String fixConfigPath) throws ConfigError  {
        SessionSettings executorSettings = new SessionSettings(fixConfigPath);
        FileStoreFactory fileStoreFactory = new FileStoreFactory(executorSettings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        FileLogFactory fileLogFactory = new FileLogFactory(executorSettings);

        return new SocketInitiator(application, fileStoreFactory, executorSettings, fileLogFactory, messageFactory);
    }

    @PostConstruct
    public void afterInitialize() throws ConfigError {
        Application fixApplication = new FixClientApplication();
        Connector connector = createInitiator(fixApplication, configPath);
        connector.start();
    }
}
