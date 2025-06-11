package eu.europa.ec.cc.processcentre.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Scope {
  String scopeTypeId;
  String scopeId;
}
