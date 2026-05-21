package pro.sachin.fity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.model.Subscription;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    SubscriptionDTO toDto(Subscription subscription);

    Subscription toEntity(SubscriptionDTO subscriptionDTO);
}
