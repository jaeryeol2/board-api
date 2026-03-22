package com.project.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.project.api.zconfig.database.DataSourceConfig;
import com.project.api.zconfig.database.PostgreConfig;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PostgreConfig.class),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DataSourceConfig.class),})
public class BoardApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(BoardApiApplication.class, args);
  }

}
