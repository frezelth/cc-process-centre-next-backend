package eu.europa.ec.cc.processcentre.template;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

/**
 * 
 * @author oleksra
 *
 */
@Data
public class TemplateModel {

  private Map<String, Object> templateModel = new HashMap<>();
  public static final String TEMPLATE_MARKER = "${";

  private enum Model {
    ID("id"),
    PROCESS_ID("processId"),
    PROCESS_TYPE_ID("processTypeId"),
    PROVIDER_ID("providerId"),
    PROCESS_TYPE_NAME("processTypeName"),
    NAME("name"),
    DESCRIPTION("description"),
    RESPONSIBLE("responsible"),
    BUSINESS_STATUS("businessStatus"),
    STATUS("status"),
    START_DATE("startDate"),
    END_DATE("endDate"),
    PAUSE_DATE("pauseDate"),
    RESTART_DATE("restartDate"),
    CANCEL_DATE("cancelDate"),
    PORTFOLIO_ITEM_BUSINESS_IDS("portfolioItemBusinessIds"),
    PORTFOLIO_ITEMS("portfolioItems"),
    RESPONSIBLE_ORGANISATION("responsibleOrganisation")
    ;

    public final String tag;

    Model(String tag) {
      this.tag = tag;
    }
  }

  private enum ModelGroup {
    PROCESS_VARIABLES("processVariables");

    public final String tag;

    ModelGroup(String tag) {
      this.tag = tag;
    }
  }

  public TemplateModel(FindProcessByIdQueryResponse process) {
    add(Model.ID, process.getProcessInstanceId());
    add(Model.PROCESS_ID, process.getProcessInstanceId());
    add(Model.PROCESS_TYPE_ID, process.getProcessTypeId());
    add(Model.PROVIDER_ID, process.getProviderId());

    if (isNotEmpty(process.getProcessResponsibleOrganisationCode())) {
      add(Model.RESPONSIBLE, process.getProcessResponsibleOrganisationCode());
    } else {
      add(Model.RESPONSIBLE, process.getProcessResponsibleOrganisation());
    }

    add(Model.STATUS, process.getStatus());
    add(Model.BUSINESS_STATUS, process.getBusinessStatus());
    add(Model.START_DATE, process.getStartedOn());
    add(Model.END_DATE, process.getCompletedOn());
    add(Model.PAUSE_DATE, process.getPausedOn());
    if (process.getPausedOn() != null) {
      add(Model.RESTART_DATE, process.getStartedOn());
    } else {
      add(Model.RESTART_DATE, null);
    }
    add(Model.CANCEL_DATE, process.getCancelledOn());

    String items = null;
    if (!CollectionUtils.isEmpty(process.getPortfolioItemIds())) {
      items = String.join(",", process.getPortfolioItemIds());
    }
    add(Model.PORTFOLIO_ITEMS, items);

    Map<String, Serializable> processVariables = process.getVariables()
            .stream().collect(Collectors.toMap(
                FindProcessVariableQueryResponse::getName, FindProcessVariableQueryResponse::getVariableValue
        ));

    add(ModelGroup.PROCESS_VARIABLES.tag, processVariables);
  }

  public Map<String, Object> getModel() {
    return templateModel;
  }

  private static final String MODEL_MUST_NOT_BE_NULL = "model must not be null";
  private static final String NAME_MUST_NOT_BE_NULL = "name must not be null";
  private static final String DOT = ".";

  private void add(@NonNull TemplateModel.Model key, Object val) {
    Objects.requireNonNull(key.tag, "key must not be null");
    templateModel.put(key.tag, val);
  }

  private void add(@NonNull String key, Object val) {
    Objects.requireNonNull(key, "key must not be null");
    templateModel.put(key, val);
  }

}
