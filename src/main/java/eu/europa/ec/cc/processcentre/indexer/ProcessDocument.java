package eu.europa.ec.cc.processcentre.indexer;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ProcessDocument {

  String id;
  String domainKey;
  String providerId;
  String processTypeKey;
  TranslatedText title;

  String status;

  Instant startedOn;
  Instant indexedOn;
  Instant pausedOn;
  Instant restartedOn;
  Instant cancelledOn;
  Instant endedOn;

  String securitySecundaTask;
  String securitySecundaScopeId;
  String securitySecundaScopeTypeId;
  String securitySecundaApplicationId;

  List<ActiveTaskDocument> activeTasks;

  @Builder
  @Value
  public static class ActiveTaskDocument {

    String id;
    TranslatedText title;
    Instant timestamp;
    String status;

  }

  @Builder
  @Value
  public static class User {
    String userId;
    String title;
    String firstName;
    String lastName;
    String email;
    Organisation organisation;
  }

  @Builder
  @Value
  public static class Organisation {
    String id;
    String code;
  }

  @Builder
  @Value
  public static class TranslatedText {
    String language;
    String value;
    boolean isDefault;
  }

  @Builder
  @Value
  public static class PortfolioItem {
    String id;
    String businessKey;
  }

  @Builder
  @Value
  public static class Tags {
    String catalog;
    List<String> values;
  }

}
