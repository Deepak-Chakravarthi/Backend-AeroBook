package com.aerobook.mapper;


import com.aerobook.entity.LoyaltyAccount;
import com.aerobook.entity.MileTransaction;
import com.aerobook.domain.dto.response.LoyaltyAccountResponse;
import com.aerobook.domain.dto.response.MileTransactionResponse;
import com.aerobook.domain.enums.LoyaltyTier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoyaltyMapper {

    @Mapping(target = "userId",   source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "milesToNextTier",
            expression = "java(calculateMilesToNextTier(account))")
    @Mapping(target = "nextTier",
            expression = "java(resolveNextTier(account))")
    LoyaltyAccountResponse toResponse(LoyaltyAccount account);

    @Mapping(target = "loyaltyAccountId",  source = "loyaltyAccount.id")
    @Mapping(target = "membershipNumber",  source = "loyaltyAccount.membershipNumber")
    MileTransactionResponse toTransactionResponse(MileTransaction transaction);

    default Long calculateMilesToNextTier(LoyaltyAccount account) {
        long qualifying = account.getTierQualifyingMiles();
        return switch (account.getTier()) {
            case BLUE     -> 10_000L - qualifying;
            case SILVER   -> 50_000L - qualifying;
            case GOLD     -> 100_000L - qualifying;
            case PLATINUM -> 0L;
        };
    }

    default String resolveNextTier(LoyaltyAccount account) {
        return switch (account.getTier()) {
            case BLUE     -> LoyaltyTier.SILVER.name();
            case SILVER   -> LoyaltyTier.GOLD.name();
            case GOLD     -> LoyaltyTier.PLATINUM.name();
            case PLATINUM -> "NONE";
        };
    }
}