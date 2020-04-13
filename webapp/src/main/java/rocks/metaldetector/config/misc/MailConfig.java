package rocks.metaldetector.config.misc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.yml")
public class MailConfig {

  private final String fromEmail;
  private final String host;
  private final String port;

  public MailConfig(@Value("${spring.mail.properties.from}") String fromEmail,
                    @Value("${application.host}") String host,
                    @Value("${server.port}") String port) {
    this.fromEmail = fromEmail;
    this.host = host;
    this.port = port;
  }

  public String getFromEmail() {
    return fromEmail;
  }

  public String getHost() {
    return host + ":" + port;
  }

}
