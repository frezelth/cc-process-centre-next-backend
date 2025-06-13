package eu.europa.ec.cc.processcentre.template;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import eu.europa.ec.cc.processcentre.model.ProcessStatus;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessByIdQueryResponse;
import eu.europa.ec.cc.processcentre.process.command.repository.model.FindProcessVariableQueryResponse;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import javax.cache.Cache;

/**
 * 
 * @author oleksra
 *
 */
@Data
public class TemplateModel {

  private Map<String, Object> templateModel = new HashMap<>();
  public static final String TEMPLATE_MARKER = "${";

  public enum Model {
    ID("id"),
    PROCESS_ID("processId"),
    PROCESS_TYPE_ID("processTypeId"),
    PROVIDER_ID("providerId"),
    PROCESS_TYPE_NAME("processTypeName"),
    NAME("name"),
    DESCRIPTION("description"),
    RESPONSIBLE("responsible"),
    BUSINESS_STATUS("businessStatus"),
    STATUS("action"),
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

  public enum ModelGroup {
    PROCESS_VARIABLES("processVariables");

    public final String tag;

    ModelGroup(String tag) {
      this.tag = tag;
    }
  }

  public TemplateModel() {

  }

  public TemplateModel(ProcessCreated process) {
    add(Model.ID, process.getProcessInstanceId());
    add(Model.PROCESS_ID, process.getProcessInstanceId());
    add(Model.PROCESS_TYPE_ID, process.getProcessTypeId());
    add(Model.PROVIDER_ID, process.getProviderId());
    add(Model.RESPONSIBLE, process.getResponsibleOrganisationId());

    add(Model.STATUS, ProcessStatus.ONGOING);
    add(Model.BUSINESS_STATUS, process.getBusinessStatus());
    add(Model.START_DATE, ProtoUtils.timestampToInstant(process.getCreatedOn()));
    add(Model.END_DATE, null);
    add(Model.PAUSE_DATE, null);
    add(Model.RESTART_DATE, null);
    add(Model.CANCEL_DATE, null);

    String items = null;
    if (!CollectionUtils.isEmpty(process.getAssociatedPortfolioItemIdsList())) {
      items = String.join(",", process.getAssociatedPortfolioItemIdsList());
    }
    add(Model.PORTFOLIO_ITEMS, items);

    Map<String, Serializable> processVariables = process.getProcessVariablesMap()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> toValue(e.getValue())
            ));

    add(ModelGroup.PROCESS_VARIABLES.tag, processVariables);
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

  public void add(@NonNull TemplateModel.Model key, Object val) {
    Objects.requireNonNull(key.tag, "key must not be null");
    templateModel.put(key.tag, val);
  }

  public void add(@NonNull String key, Object val) {
    Objects.requireNonNull(key, "key must not be null");
    templateModel.put(key, val);
  }

  public void addProcessVariables(Map<String, VariableValue> processVariables) {
    Map<String,Object> variables = (Map<String,Object>)templateModel.getOrDefault(ModelGroup.PROCESS_VARIABLES.tag,
        new HashMap<>());
    processVariables.forEach((k, v) ->
      variables.put(k, toValue(v))
    );
  }

  public static Serializable toValue(VariableValue variableValue) {
    if (variableValue != null && !variableValue.getDeleted()) {
      switch (variableValue.getKindCase()) {
        case BOOLEANVALUE -> {
          return variableValue.getBooleanValue();
        }
        case INTEGERVALUE -> {
          return variableValue.getIntegerValue();
        }
        case LONGVALUE -> {
          return variableValue.getLongValue();
        }
        case DOUBLEVALUE -> {
          return variableValue.getDoubleValue();
        }
        case TIMEVALUE -> {
          return ProtoUtils.timestampToInstant(variableValue.getTimeValue());
        }
        case BYTESVALUE -> {
          return variableValue.getBytesValue().toByteArray();
        }
        default -> {
          return variableValue.getStringValue();
        }
      }
    } else {
      return null;
    }
  }

}
