package com.pohribnyi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Sport(
        @JsonProperty("id") long id,
        @JsonProperty("name") String name,
        @JsonProperty("family") String family,
        @JsonProperty("regions") List<Region> regionList
) {
}
