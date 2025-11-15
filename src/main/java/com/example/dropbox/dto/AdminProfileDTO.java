package com.example.dropbox.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileDTO {

    @JsonProperty("team_member_id")
    private String teamMemberId;

    private String email;

    @JsonProperty("display_name")
    private String displayName;
}
