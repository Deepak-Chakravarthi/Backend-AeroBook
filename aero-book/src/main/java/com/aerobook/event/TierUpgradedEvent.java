package com.aerobook.event;


import com.aerobook.entity.LoyaltyAccount;
import com.aerobook.domain.enums.LoyaltyTier;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TierUpgradedEvent extends ApplicationEvent {

    private final LoyaltyAccount loyaltyAccount;
    private final LoyaltyTier    previousTier;
    private final LoyaltyTier    newTier;

    public TierUpgradedEvent(Object source,
                             LoyaltyAccount loyaltyAccount,
                             LoyaltyTier previousTier,
                             LoyaltyTier newTier) {
        super(source);
        this.loyaltyAccount = loyaltyAccount;
        this.previousTier   = previousTier;
        this.newTier        = newTier;
    }
}