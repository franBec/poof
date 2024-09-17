package dev.pollito.poof.config;

import dev.pollito.poof.filter.LogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogFilterConfig {

  @Bean
  public FilterRegistrationBean<LogFilter> loggingFilter() {
    FilterRegistrationBean<LogFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(new LogFilter());
    registrationBean.addUrlPatterns("/*");

    return registrationBean;
  }
}
