package eu.europa.ec.cc.processcentre.mapper;

import eu.europa.ec.cc.processcentre.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import java.util.Locale;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QueryConverter {

  SearchProcessQueryParam toQueryParam(
      SearchProcessRequestDto dto, Locale locale, String username, int limit, int offset);

}
