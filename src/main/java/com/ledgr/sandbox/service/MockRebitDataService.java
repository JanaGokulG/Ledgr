package com.ledgr.sandbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgr.sandbox.dto.rebit.AccountDataDto;
import com.ledgr.sandbox.dto.rebit.AccountsWrapperDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockRebitDataService {

    private final ObjectMapper objectMapper;

    @Value("${ledgr.sandbox.mock-data-path:designmode_sample.json}")
    private String mockDataPath;

    private AccountsWrapperDto cachedPayload;

    @PostConstruct
    public void init() {
        loadMockData();
    }

    public synchronized AccountsWrapperDto loadMockData() {
        try {
            File localFile = new File(mockDataPath);
            if (localFile.exists()) {
                log.info("Loading AA mock data from local path: {}", localFile.getAbsolutePath());
                cachedPayload = objectMapper.readValue(localFile, AccountsWrapperDto.class);
            } else {
                ClassPathResource resource = new ClassPathResource(mockDataPath);
                if (resource.exists()) {
                    log.info("Loading AA mock data from classpath resource: {}", mockDataPath);
                    try (InputStream is = resource.getInputStream()) {
                        cachedPayload = objectMapper.readValue(is, AccountsWrapperDto.class);
                    }
                } else {
                    log.warn("Mock data file not found at path '{}', trying working directory...", mockDataPath);
                    File fallback = new File("designmode_sample.json");
                    if (fallback.exists()) {
                        cachedPayload = objectMapper.readValue(fallback, AccountsWrapperDto.class);
                    } else {
                        log.error("Could not locate designmode_sample.json. Initializing with empty accounts.");
                        cachedPayload = AccountsWrapperDto.builder().build();
                    }
                }
            }
            int accCount = cachedPayload.getAccounts() != null ? cachedPayload.getAccounts().size() : 0;
            log.info("Successfully loaded {} bank accounts from mock ReBIT AA dataset.", accCount);
            return cachedPayload;
        } catch (Exception e) {
            log.error("Failed to parse AA mock data JSON from {}: {}", mockDataPath, e.getMessage(), e);
            cachedPayload = AccountsWrapperDto.builder().build();
            return cachedPayload;
        }
    }

    public AccountsWrapperDto getFullPayload() {
        if (cachedPayload == null || cachedPayload.getAccounts().isEmpty()) {
            return loadMockData();
        }
        return cachedPayload;
    }

    public List<AccountDataDto> getAccounts() {
        return getFullPayload().getAccounts();
    }
}
