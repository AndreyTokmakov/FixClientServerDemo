package server;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import quickfix.*;

import javax.annotation.PostConstruct;

@Log4j2
@Service
public class AcceptorService
{
    @Value("${fix.config.path}")
    private String configPath;

    private Connector createAcceptor(Application application, String fixConfigPath) throws ConfigError  {
        SessionSettings executorSettings = new SessionSettings(fixConfigPath);
        FileStoreFactory fileStoreFactory = new FileStoreFactory(executorSettings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        FileLogFactory fileLogFactory = new FileLogFactory(executorSettings);

        return new SocketAcceptor(application, fileStoreFactory, executorSettings, fileLogFactory, messageFactory);
    }

    @PostConstruct
    public void afterInitialize() throws ConfigError {
        Application fixApplication = new FixServerApplication();
        Connector connector = createAcceptor(fixApplication, configPath);
        connector.start();
    }
}
