package com.gsm.blabla.common.application;

import com.gsm.blabla.common.dto.CommonCodeDto;
import com.gsm.blabla.common.enums.Tag;
import com.gsm.blabla.common.enums.Keyword;
import com.gsm.blabla.common.enums.Level;
import com.gsm.blabla.common.enums.PreferMember;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommonService {
    public Map<String, String> getLevels(String language) {
        Map<String, String> result = new HashMap<>();

        for (Level level : Level.values()) {
            if (level.getLanguage().equals(language)) {
                result.put(level.getDegree(), level.getDescription());
            }
        }

        return result;
    }

    public Map<String, List<CommonCodeDto>> getKeywords(String language) {
        Map<String, List<CommonCodeDto>> result = new HashMap<>();

        List<CommonCodeDto> entertainments = createKeywordDtos(language, "엔터테인먼트");
        List<CommonCodeDto> characteristics = createKeywordDtos(language, "성격");
        List<CommonCodeDto> hobbies = createKeywordDtos(language, "취미/관심사");

        result.put("엔터테인먼트", entertainments);
        result.put("성격", characteristics);
        result.put("취미/관심사", hobbies);

        return result;
    }

    public Map<String, List<CommonCodeDto>> getCrewTags(String language) {
        Map<String, List<CommonCodeDto>> result = new HashMap<>();

        List<CommonCodeDto> crewTags = new ArrayList<>();
        for (Tag tag : Tag.values()) {
            if (Objects.equals(language, "ko")) {
                crewTags.add(CommonCodeDto.of(tag.getEmoji(), tag.getKoreanName(), tag.name()));
            } else {
                crewTags.add(CommonCodeDto.of(tag.getEmoji(), tag.getEnglishName(), tag.name()));
            }
        }
        result.put("tags", crewTags);

        return result;
    }

    public Map<String, List<CommonCodeDto>> getPreferMembers(String language) {
        Map<String, List<CommonCodeDto>> result = new HashMap<>();

        List<CommonCodeDto> preferMembers = new ArrayList<>();
        for (PreferMember preferMember : PreferMember.values()) {
            if (Objects.equals(language, "ko")) {
                preferMembers.add(CommonCodeDto.of(preferMember.getEmoji(), preferMember.getKoreanName(), preferMember.name()));
            } else {
                preferMembers.add(CommonCodeDto.of(preferMember.getEmoji(), preferMember.getEnglishName(), preferMember.name()));
            }
        }
        result.put("preferMembers", preferMembers);

        return result;
    }

    private List<CommonCodeDto> createKeywordDtos(String language, String category) {
        List<CommonCodeDto> keywords = new ArrayList<>();

        for (Keyword keyword : Keyword.values()) {
            if (Objects.equals(language, "ko")) {
                if (Objects.equals(keyword.getCategory(), category)) {
                    keywords.add(
                        CommonCodeDto.of(keyword.getEmoji(), keyword.getKoreanName(), keyword.name()));
                }
            } else {
                if (Objects.equals(keyword.getCategory(), category)) {
                    keywords.add(
                        CommonCodeDto.of(keyword.getEmoji(), keyword.getEnglishName(), keyword.name()));
                }
            }
        }

        return keywords;
    }
}
