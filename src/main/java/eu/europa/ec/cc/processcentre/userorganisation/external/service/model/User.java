package eu.europa.ec.cc.processcentre.userorganisation.external.service.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representation of a user in the user/organisation microservice
 */
@Data
@Builder
@EqualsAndHashCode(of = {"userId"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

  private String userId;
  private String title;
  private String firstName;
  private String lastName;
  private Organisation organisation;
  private String email;

}
