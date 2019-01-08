import {User} from "~/shared/auth/user";

export interface Namespace {
  id: string;
  createdOn: string;
  group: string;
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

export function hasUserPermission(user: User, group: string, permission: GroupPermission) {
  if (user.authorities.indexOf('ROLE_ADMIN') !== -1) return true;

  const matchingUserGroup = user.groups.filter(ug => ug.group.name == group);
  if (matchingUserGroup.length !== 1) return false;

  return hasPermission(matchingUserGroup[0], permission)
}

export function hasUserPermissions(user: User, group: string, permissions: GroupPermission[]) {
  for (let permission of permissions) {
    if (hasUserPermission(user, group, permission)) return true;
  }

  return false;
}

export type MediaType = "application/json";

