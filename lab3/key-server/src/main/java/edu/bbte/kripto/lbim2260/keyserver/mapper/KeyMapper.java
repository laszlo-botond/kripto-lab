package edu.bbte.kripto.lbim2260.keyserver.mapper;

import edu.bbte.kripto.lbim2260.keyserver.dto.KeyAndIdDto;
import edu.bbte.kripto.lbim2260.keyserver.dto.PublicKeyDto;
import edu.bbte.kripto.lbim2260.keyserver.model.KeyModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class KeyMapper {

    public abstract KeyModel keyAndIdDtoToKeyModel(KeyAndIdDto dto);

    @Mapping(target = "id", ignore = true)
    public abstract KeyModel publicKeyDtoToKeyModel(PublicKeyDto dto);

    public abstract KeyAndIdDto keyModelToKeyAndIdDto(KeyModel model);

    public abstract PublicKeyDto keyModelToPublicKeyDto(KeyModel model);
}
