package com.example.dropbox.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamInfoDTO {

    private String name;

    @JsonProperty("team_id")
    private String teamId;

    @JsonProperty("num_licensed_users")
    private Integer numLicensedUsers;

    @JsonProperty("num_provisioned_users")
    private Integer numProvisionedUsers;

    @JsonProperty("num_used_licenses")
    private Integer numUsedLicenses;
}
