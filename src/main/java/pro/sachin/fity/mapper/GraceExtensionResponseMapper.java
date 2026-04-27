package pro.sachin.fity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pro.sachin.fity.dto.GraceExtensionDTO;
import pro.sachin.fity.dto.GraceExtensonResponseDTO;
import pro.sachin.fity.model.GraceExtension;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Subscription;

@Mapper(componentModel = "spring")
public interface GraceExtensionResponseMapper {

    @Mapping(source = "subscription.id", target = "subscriptionId")
    @Mapping(source = "subscription.graceEndDate", target = "oldGraceEndDate")
    @Mapping(source = "graceExtension.id", target = "id")
    @Mapping(source = "graceExtension.newGraceEndDate", target = "newGraceEndDate")
    @Mapping(source = "graceExtension.reason", target = "reason")
    @Mapping(source = "graceExtension.createdAt", target = "lastExtensionHappened")
    @Mapping(source = "member.firstName", target = "memberFirstName")
    @Mapping(source = "member.lastName", target = "memberLastName")
    @Mapping(source = "subscription.endDate", target = "originalPaymentDate")
    GraceExtensonResponseDTO toDto(GraceExtension graceExtension, Subscription subscription, Member member);
}
