import {GroupPermission} from "~/shared/api/model";

export interface MenuItem {
  title: string;
  commands: any[];
  permissions?: GroupPermission[];
}
