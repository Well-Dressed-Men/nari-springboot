package welldressedmen.narispringboot.dto;


import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FashionInfo {

    private List<String> fashionStr; //추천패션 목록

}