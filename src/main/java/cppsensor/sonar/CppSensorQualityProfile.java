package cppsensor.sonar;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;

public class CppSensorQualityProfile extends ProfileDefinition {

  public static final String PROFILE_NAME = "CPP Sensor Quality Profile";

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    RulesProfile profile = RulesProfile.create(PROFILE_NAME, CppLanguage.KEY);
    return profile;
  }

}
