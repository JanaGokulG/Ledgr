package com.ledgr.sandbox;

import com.ledgr.sandbox.repository.AccountRepository;
import com.ledgr.sandbox.service.FiuIngestionService;
import com.ledgr.sandbox.service.RealtimeTransactionSimulatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LedgrAaSandboxApplication {

    private static final Logger log = LoggerFactory.getLogger(LedgrAaSandboxApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(LedgrAaSandboxApplication.class, args);
    }

    @Bean
    public ApplicationRunner dataInitializer(AccountRepository accountRepository,
                                             FiuIngestionService fiuIngestionService,
                                             RealtimeTransactionSimulatorService simulatorService) {
        return args -> {
            try {
                if (accountRepository.count() == 0) {
                    log.info("⚡ Performing automatic initial ReBIT AA sandbox sync for seamless experience...");
                    fiuIngestionService.executeFullSync("student@setuaa");
                }
            } catch (Exception e) {
                log.warn("Automatic initial sync warning (can be re-triggered via UI/API): {}", e.getMessage());
            }

            // Automatically start continuous streaming with random delay (1.2s - 3.2s) & repetition so data flow never stops
            simulatorService.startLiveStreamingWithRandomDelay(1200, 3200);
        };
    }
}

