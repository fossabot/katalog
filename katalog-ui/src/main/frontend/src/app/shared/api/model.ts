export interface Namespace {
  id: string;
  createdOn: string;
  namespace: string;
}

export interface Schema {
  id: string;
  createdOn: string;
  namespace: Namespace;
  schema: string;
}

export interface Version {
  id: string;
  createdOn: string;
  schemaId: string;
  version: string;
  major: number;
  stable: boolean;
  current: boolean;
}

export interface Artifact {
  id: string;
  filename: string;
  filesize: number;
  mediaType: MediaType;
  repositoryPath: string;
}

export interface Group {
  name: string;
}

export type GroupPermission = "CREATE" | "READ" | "UPDATE" | "DELETE";

export interface UserGroup {
  group: Group;
  permissions: GroupPermission[];
}

export function hasPermission(userGroup: UserGroup, permission: GroupPermission) {
  return userGroup.permissions.indexOf(permission) !== -1;
}

export type MediaType = "application/json";

