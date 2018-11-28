export type SortDirection = "ASC" | "DESC";

export interface SortingRequest {
  column: string;
  direction: SortDirection
}
