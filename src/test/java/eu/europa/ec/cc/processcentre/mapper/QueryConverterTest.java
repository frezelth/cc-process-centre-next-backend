package eu.europa.ec.cc.processcentre.mapper;

import eu.europa.ec.cc.processcentre.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

public class QueryConverterTest {

  private final QueryConverter converter = Mappers.getMapper(QueryConverter.class);

  @Test
  void testConvertSearchProcessRequestDto(){
    PodamFactory factory = new PodamFactoryImpl();

    SearchProcessRequestDto searchProcessRequestDto = factory.manufacturePojo(SearchProcessRequestDto.class);

    SearchProcessQueryParam queryParam = converter.toQueryParam(searchProcessRequestDto,
        Locale.FRENCH, "username", 20, 0);

    Assertions.assertEquals("username", queryParam.username());
  }

}
