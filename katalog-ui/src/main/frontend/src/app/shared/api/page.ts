export interface Page<T> {
  data: T[],
  totalElements: number;
  totalPages: number;
}

export interface PageRequest {
  page: number;
  size: number;
}
