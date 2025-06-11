package eu.europa.ec.cc.processcentre.userorganisation.model;

import java.util.Comparator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = {"userId"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements Comparable<User> {

  private String userId;
  private String title;
  private String firstName;
  private String lastName;
  private Organisation organisation;
  private String email;

  @Override
  public int compareTo(User o) {
    return Comparator.comparing(User::getLastName, Comparator.nullsLast(Comparator.naturalOrder()))
      .thenComparing(User::getFirstName, Comparator.nullsLast(Comparator.naturalOrder()))
      .compare(this, o);
  }
}
