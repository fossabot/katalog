import {ClrDatagridStateInterface} from "@clr/angular";
import {Page, PaginationRequest} from "~/shared/api/page";
import {SortDirection, SortingRequest} from "~/shared/api/sorting";

export class DataGridState<T> {
  pagination: PaginationRequest;
  sorting: SortingRequest;
  isLoading: boolean;
  data: T[];
  totalElements: number;

  constructor(
    private defaultSortColumn: string,
    private defaultSortDirection: SortDirection
  ) {
  }

  public applyClrState(state: ClrDatagridStateInterface<T>) {
    if (!state.page) {
      // Happens when navigating away from the page
      return;
    }

    const page = (state.page.from / state.page.size) + 1;
    this.pagination = {page: page, size: 10};

    if (state.sort) {
      this.sorting =
        {
          column: state.sort.by.toString(),
          direction: this.defaultSortDirection == "DESC" ? (state.sort.reverse ? "DESC" : "ASC") : (state.sort.reverse ? "ASC" : "DESC")
        };
    } else {
      this.sorting = {
        column: this.defaultSortColumn,
        direction: this.defaultSortDirection
      }
    }
  }

  public async load(fn: (DataGridState) => Promise<Page<T>>) {
    window.setTimeout(() => {
      this.isLoading = true;
    }, 0);

    try {
      const options = {
        pagination: this.pagination,
        sorting: this.sorting
      };
      const response = await fn(options);
      this.data = response.data;
      this.totalElements = response.totalElements;
    } finally {
      window.setTimeout(() => {
        this.isLoading = false;
      }, 0);
    }
  }
}
