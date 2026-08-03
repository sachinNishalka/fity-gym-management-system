package pro.sachin.fity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pro.sachin.fity.dto.GraceExtensionDTO;
import pro.sachin.fity.model.GraceExtension;

@Mapper(componentModel = "spring")
public interface GraceExtensionMapper {

    GraceExtensionDTO toDto(GraceExtension graceExtension);

}
