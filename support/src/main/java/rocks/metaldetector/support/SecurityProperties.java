package rocks.metaldetector.support;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

import javax.crypto.SecretKey;

import static java.nio.charset.StandardCharsets.UTF_8;

@PropertySource(value = "classpath:application.yml")
@ConfigurationProperties(prefix = "security")
@Data
public class SecurityProperties {

  private static SecretKey key;

  private String jwtIssuer;
  private String jwtSecret;
  private long accessTokenExpirationInMin;
  private long refreshTokenExpirationInMin;
  private boolean secureCookie;

  public SecretKey getKey() {
    if (key == null) {
      key = Keys.hmacShaKeyFor(jwtSecret.getBytes(UTF_8));
    }

    return key;
  }
}
