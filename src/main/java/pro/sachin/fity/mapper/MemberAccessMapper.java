package pro.sachin.fity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import pro.sachin.fity.dto.MemberAccessDTO;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.MemberAccess;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberAccessMapper {

    @Mapping(source = "member.memberCode", target = "memberCode")
    @Mapping(source = "member.firstName", target = "memberName", qualifiedByName = "getFullName")
    // @Mapping(source = "accessStatus", target = "accessStatus", qualifiedByName =
    // "accessStatusToBoolean")
    MemberAccessDTO toDto(MemberAccess memberAccess);

    @Named("getFullName")
    default String getFullName(String firstName) {
        // You can enhance this to include lastName if needed
        return firstName;
    }

    @Named("accessStatusToBoolean")
    default Boolean accessStatusToBoolean(AccessStatus accessStatus) {
        return accessStatus == AccessStatus.ALLOWED;
    }
}