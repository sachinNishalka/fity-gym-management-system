package pro.sachin.fity.mapper;

import org.mapstruct.Mapper;

import pro.sachin.fity.dto.MemberDTO;
import pro.sachin.fity.model.Member;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    MemberDTO toDto(Member member);

    Member toEntity(MemberDTO memberDTO);

}
