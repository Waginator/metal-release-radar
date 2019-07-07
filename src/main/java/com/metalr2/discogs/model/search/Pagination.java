package com.metalr2.discogs.model.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({
        "per_page",
        "items",
        "page",
        "urls",
        "pages"
})
public class Pagination {

  @JsonProperty("per_page")
  private int itemsPerPage;

  @JsonProperty("items")
  private int itemsTotal;

  @JsonProperty("page")
  private int currentPage;

  @JsonProperty("pages")
  private int pagesTotal;

  @JsonProperty("urls")
  private PaginationUrls urls;

}
