package welldressedmen.narispringboot.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateRequestDTO {
    private String memberSex;

    private String memberCold;
    private String memberHot;

    private String memberBody;

    private String memberPreferences;
}
