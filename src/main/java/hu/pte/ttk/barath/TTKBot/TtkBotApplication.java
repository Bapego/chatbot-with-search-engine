package hu.pte.ttk.barath.TTKBot;

import hu.pte.ttk.barath.handlers.HTMLHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class TtkBotApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(TtkBotApplication.class, args);
    }

    @Scheduled(cron = "0 30 1 * * MON")
    public void readWebPage() throws Exception {
        new HTMLHandler("http://www.ttk.pte.hu/","qa");
    }
}