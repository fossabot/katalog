import {ClrDatagridStateInterface} from "@clr/angular";
import {PaginationRequest} from "~/shared/api/page";
import {SortDirection, SortingRequest} from "~/shared/api/sorting";

export function stateToPage<T>(state: ClrDatagridStateInterface<T>, defaultSortColumn: string, defaultSortDirection: SortDirection): [PaginationRequest, SortingRequest] {
  if (!state.page) {
    // Happens when navigating away from the page
    return [null, null];
  }

  const page = (state.page.from / state.page.size) + 1;
  const pagination = {page: page, size: 10};

  let sorting: SortingRequest;
  if (state.sort) {
    sorting =
      {
        column: state.sort.by.toString(),
        direction: defaultSortDirection == "DESC" ? (state.sort.reverse ? "DESC" : "ASC") : (state.sort.reverse ? "ASC" : "DESC")
      };
  } else {
    sorting = {
      column: defaultSortColumn,
      direction: defaultSortDirection
    }
  }

  return [pagination, sorting];
}
