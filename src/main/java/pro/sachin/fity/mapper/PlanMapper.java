package pro.sachin.fity.mapper;

import org.mapstruct.Mapper;

import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.model.Plan;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    PlanDTO toDto(Plan plan);

    Plan toEntity(PlanDTO planDTO);
}
