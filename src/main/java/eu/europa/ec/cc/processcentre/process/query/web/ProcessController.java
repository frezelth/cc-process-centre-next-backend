package eu.europa.ec.cc.processcentre.process.query.web;

import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.springframework.http.ResponseEntity.ok;

import eu.europa.ec.cc.processcentre.exception.InvalidInputException;
import eu.europa.ec.cc.processcentre.process.command.service.ProcessService;
import eu.europa.ec.cc.processcentre.process.query.ProcessQueries;
import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.process.query.web.dto.SearchProcessResponseDto;
import eu.europa.ec.cc.processcentre.proto.RemoveProcessFavourite;
import eu.europa.ec.cc.processcentre.util.ApiHelper;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/processes")
public class ProcessController {

  private final ProcessService processService;
  private final ProcessQueries processQueries;

  public ProcessController(ProcessService processService, ProcessQueries processQueries) {
    this.processService = processService;
    this.processQueries = processQueries;
  }

  @PostMapping("/v2")
  @ResponseBody
  public SearchProcessResponseDto searchProcesses(
      @RequestParam("offset") int offset,
      @RequestParam("limit") int limit,
      @RequestBody(required = false) SearchProcessRequestDto filter
  ) {

    final var processesAndContexts = processQueries.searchProcesses(
        filter, offset, limit, LocaleContextHolder.getLocale(), ApiHelper.getUsername().orElseThrow(
            () -> new InvalidInputException("No user found in request")
        ));

    return processesAndContexts;
  }

  @PutMapping("/{processId}/favourite")
  public ResponseEntity<Void> favouriteProcess(@PathVariable String processId) {
    final var processInstanceId = trimToNull(processId);
    if (processInstanceId == null) {
      throw new InvalidInputException("The favourite process ID cannot be blank");
    }

    this.processService.setAsFavourite(processId, ApiHelper.getUsername()
        .orElseThrow(InvalidInputException::new));

    return ok().build();
  }

  @PutMapping("/{processId}/unfavourite")
  public ResponseEntity<Void> unfavouriteProcess(@PathVariable String processId) {

    final var processInstanceId = trimToNull(processId);
    if (processInstanceId == null) {
      throw new InvalidInputException("The unfavourite process ID cannot be blank");
    }

    this.processService.removeFromFavourites(processId, ApiHelper.getUsername()
        .orElseThrow(InvalidInputException::new));

    return ok().build();
  }

}
