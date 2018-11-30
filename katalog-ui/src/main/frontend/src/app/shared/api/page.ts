export interface Page<T> {
  data: T[],
  totalElements: number;
  totalPages: number;
}

export interface PaginationRequest {
  page: number;
  size: number;
}
