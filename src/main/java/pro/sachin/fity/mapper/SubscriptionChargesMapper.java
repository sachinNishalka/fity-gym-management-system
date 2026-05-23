package pro.sachin.fity.mapper;

import org.mapstruct.Mapper;

import pro.sachin.fity.dto.SubscriptionChargesDTO;
import pro.sachin.fity.model.SubscriptionCharges;

@Mapper(componentModel = "spring")
public interface SubscriptionChargesMapper {

    SubscriptionChargesDTO toDto(SubscriptionCharges subscriptionCharges);

    SubscriptionCharges toEntity(SubscriptionChargesDTO subscriptionChargesDTO);
}
