package com.ledgr;

import com.ledgr.config.DotenvLoader;
import com.ledgr.repository.AccountRepository;
import com.ledgr.sandbox.service.FiuIngestionService;
import com.ledgr.sandbox.service.RealtimeTransactionSimulatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class LedgrApplication {

    private static final Logger log = LoggerFactory.getLogger(LedgrApplication.class);

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("stdout.encoding", "UTF-8");
        System.setProperty("stderr.encoding", "UTF-8");
        DotenvLoader.load();
        SpringApplication.run(LedgrApplication.class, args);
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
                } else {
                    List<com.ledgr.entity.AccountEntity> accounts = accountRepository.findAll();
                    for (com.ledgr.entity.AccountEntity acc : accounts) {
                        if (acc.getCurrentBalance() == null || acc.getCurrentBalance().compareTo(java.math.BigDecimal.ZERO) == 0) {
                            if ("RECURRING".equalsIgnoreCase(acc.getAccountType()) || "RECURRING_DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                                acc.setCurrentBalance(java.math.BigDecimal.valueOf(35000));
                                accountRepository.save(acc);
                            } else if ("FIXED".equalsIgnoreCase(acc.getAccountType()) || "TERM-DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                                acc.setCurrentBalance(java.math.BigDecimal.valueOf(100000));
                                accountRepository.save(acc);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Automatic initial sync warning (can be re-triggered via UI/API): {}", e.getMessage());
            }

            // Automatically start continuous streaming with random delay (1.2s - 3.2s) & repetition so data flow never stops
            simulatorService.startLiveStreamingWithRandomDelay(1200, 3200);
        };
    }
}

