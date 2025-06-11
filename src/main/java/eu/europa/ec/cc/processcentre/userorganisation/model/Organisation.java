package eu.europa.ec.cc.processcentre.userorganisation.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Organisation {

  private String id;
  private String code;

}
