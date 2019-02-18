export interface Namespace {
  id: string;
  createdOn: string;
  groupId: string;
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
  id: string;
  name: string;
  permissions: GroupPermission[];
}

export type GroupPermission = "CREATE" | "READ" | "UPDATE" | "DELETE";

export interface Token {
  id: string;
  description: string;
  createdOn: string;
  token: string;
}

export interface CreatedToken {
  id: string;
  token: string;
}

export function hasPermission(group: Group, permission: GroupPermission) {
  return group.permissions.indexOf(permission) !== -1;
}

export function hasAnyPermission(group: Group, permissions: GroupPermission[]) {
  for (let permission of permissions) {
    if (hasPermission(group, permission)) return true;
  }

  return false;
}

export type MediaType = "application/json";

export interface ApplicationVersion {
  version: string,
  buildTime: string
}
