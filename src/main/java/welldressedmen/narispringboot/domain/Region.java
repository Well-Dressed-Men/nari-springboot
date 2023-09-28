package welldressedmen.narispringboot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {
    private short id;
    private short nx; // x좌표
    private short ny; // y좌표

    private String midLandCode;
    private String midTempCode;

    private String stationName; //관측소

}