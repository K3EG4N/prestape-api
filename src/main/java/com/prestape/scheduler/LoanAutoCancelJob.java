package com.prestape.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.prestape.constants.LoanConstants;
import com.prestape.service.LoanService;

@Component
public class LoanAutoCancelJob {
    private static final Logger log = LoggerFactory.getLogger(LoanAutoCancelJob.class);
    private final LoanService loanService;

    public LoanAutoCancelJob(LoanService loanService) {
        this.loanService = loanService;
    }

    @Scheduled(fixedDelay = LoanConstants.AUTO_CANCEL_INTERVAL_MS, initialDelay = LoanConstants.AUTO_CANCEL_INTERVAL_MS)
    public void cancelExpiredPickups() {
        int cancelled = loanService.cancelExpiredPickups();
        if (cancelled > 0) {
            log.info("Préstamos sin recoger cancelados automáticamente: {}", cancelled);
        }
    }
}
