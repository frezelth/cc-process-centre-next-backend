package eu.europa.ec.cc.processcentre.model;

import static java.util.Collections.emptyMap;

import com.google.common.collect.Lists;
import eu.europa.ec.cc.processcentre.config.AccessRight;
import eu.europa.ec.cc.processcentre.config.AccessRight.Right;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Slf4j
public class AccessRightsConverter {

  public static List<eu.europa.ec.cc.processcentre.proto.AccessRight> domainToProto(
    Map<Right, List<AccessRight>> accessRights
  ) {

    List<eu.europa.ec.cc.processcentre.proto.AccessRight> result = Lists.newArrayList();
    if (accessRights != null) {
      accessRights.forEach((key, values) -> {
        if (!CollectionUtils.isEmpty(values)) {
          values.forEach(value -> result.add(convert(key, value)));
        }
      }
      ) ;
    }
    return result;
  }

  
  @NotNull
  public static Map<Right, List<AccessRight>> protoToDomain(
    List<eu.europa.ec.cc.processcentre.proto.AccessRight> protoAccessRights
  ) {
    return protoAccessRights == null
      ? emptyMap()
      : protoAccessRights
        .stream()
        // group by right
        .collect(Collectors.groupingBy(eu.europa.ec.cc.processcentre.proto.AccessRight::getRight))
        // convert proto -> our model
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> Right.valueOf(entry.getKey().name()),
            entry -> entry.getValue().stream().map(
                AccessRightsConverter::convert
            ).collect(Collectors.toList())
          )
        );
  }

  private static eu.europa.ec.cc.processcentre.proto.AccessRight convert(Right key,
      AccessRight value) {

    if (value.applicationId() != null && !AccessRight.PROCESS_CENTRE
        .equals(value.applicationId()) && key == Right.VIEW) {
      LOG.warn("Configuration for {} with task {} does not use PROCESS_CENTRE application, process won't be visible",
          key, value.permissionId());
    }
    
    return eu.europa.ec.cc.processcentre.proto.AccessRight
      .newBuilder()
      .setPermissionId(value.permissionId())
      .setOrClearScopeTypeId(value.scopeTypeId())
      .setOrClearScopeId(value.scopeId())
      .setRight(eu.europa.ec.cc.processcentre.proto.AccessRight.Right.valueOf(key.name()))
      .setOrClearOrganisationId(value.organisationId())
      .build();
  }


  private static AccessRight convert(
      eu.europa.ec.cc.processcentre.proto.AccessRight protoValue) {
    return new AccessRight(protoValue.getPermissionId(), AccessRight.PROCESS_CENTRE, protoValue.getScopeTypeId(), protoValue.getScopeId(), protoValue.getOrganisationId());
  }

  private static boolean isFinalSpecificData(eu.europa.ec.cc.processcentre.proto.AccessRight accessRight) {
    if (accessRight == null) {
      return false;
    }
    
    return StringUtils.isNotEmpty(accessRight.getScopeId()) || StringUtils.isNotEmpty(accessRight.getOrganisationId());
  }

  private static boolean isFinalSpecificData(AccessRight accessRight) {
    if (accessRight == null) {
      return false;
    }
    
    return accessRight.scopeTypeId() != null || accessRight.organisationId() != null;
  }
}
