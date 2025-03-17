package com.barogo.app.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

/**
 * @author sskim
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(final FormatterRegistry registry) {

        final DateTimeFormatterRegistrar dateTimeFormatterRegistrar = new DateTimeFormatterRegistrar();
        // LocalDate를 위한 포맷 지정 (년월일)
        dateTimeFormatterRegistrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // LocalDateTime을 위한 포맷 지정 (년월일시분초)
        dateTimeFormatterRegistrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // LocalTime을 위한 포맷 지정 (시분초)
        dateTimeFormatterRegistrar.setTimeFormatter(DateTimeFormatter.ofPattern("HHmmss"));

        dateTimeFormatterRegistrar.registerFormatters(registry);
    }
}
