package github.velon.newsseacher;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@SpringBootApplication
@Configuration
public class NewsSeacherApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsSeacherApplication.class, args);
    }

    @Bean
    @Qualifier("manticore")
    public JdbcTemplate manticoreJdbcTemplate(
            @Value("${manticore.url}") String url,
            @Value("${manticore.driver-class-name}") String driver) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(url);
        ds.setDriverClassName(driver);

        return new JdbcTemplate(ds);
    }
}
