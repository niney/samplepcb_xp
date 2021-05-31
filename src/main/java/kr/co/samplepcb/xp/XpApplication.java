package kr.co.samplepcb.xp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;

@SpringBootApplication
@EnableElasticsearchAuditing
public class XpApplication {

    public static void main(String[] args) {
        SpringApplication.run(XpApplication.class, args);
    }

}
