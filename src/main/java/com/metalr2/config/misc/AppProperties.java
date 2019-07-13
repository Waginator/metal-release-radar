package com.metalr2.config.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {

  private Environment env;

  @Autowired
  public AppProperties(Environment env) {
    this.env = env;
  }

  public String getDefaultMailFrom() {
    return env.getProperty("mail.from.email");
  }

  public int getHttpPort() {
    return env.getProperty("server.port", Integer.class, 80);
  }

  public String getHost() {
    return env.getProperty("application.host");
  }
	
}
