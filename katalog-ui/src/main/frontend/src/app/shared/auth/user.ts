import {UserGroup} from "~/shared/api/model";

export class User {
  username: string;
  authorities: string[];
  groups: UserGroup[];
}
