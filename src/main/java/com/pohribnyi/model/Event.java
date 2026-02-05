package com.pohribnyi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Event(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("kickoff") long kickoff,
        @JsonProperty("league") LeagueRef league,
        @JsonProperty("markets") List<Market> markets
) {
}
