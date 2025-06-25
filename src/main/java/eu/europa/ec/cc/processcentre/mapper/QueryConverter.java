package eu.europa.ec.cc.processcentre.mapper;

import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryParam.SecurityFilter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QueryConverter {

  @Mapping(target = "statuses",
      expression = "java(mapBooleansToStatuses(dto.getCompleted(), dto.getOngoing(), dto.getPaused(), dto.getCancelled()))")
  SearchProcessQueryParam toQueryParam(
      SearchProcessRequestDto dto, Locale locale,
      String username, int limit, int offset, Set<SecurityFilter> securityFilters);

  // Default method in the interface
  default Set<ProcessStatus> mapBooleansToStatuses(Boolean completed, Boolean ongoing, Boolean paused, Boolean cancelled) {
    Set<ProcessStatus> statuses = new HashSet<>();
    if (completed != null && completed) statuses.add(ProcessStatus.COMPLETED);
    if (ongoing != null && ongoing) statuses.add(ProcessStatus.ONGOING);
    if (paused != null && paused) statuses.add(ProcessStatus.PAUSED);
    if (cancelled != null && cancelled) statuses.add(ProcessStatus.CANCELLED);
    return statuses;
  }

}
