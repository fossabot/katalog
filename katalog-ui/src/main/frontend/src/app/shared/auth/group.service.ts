import {Injectable} from '@angular/core';
import {Group} from "~/shared/api/model";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class GroupService {
  private _groups: Group[];

  constructor(private http: HttpClient) {
  }

  async updateGroups(): Promise<Group[]> {
    if (!this._groups) {
      try {
        this._groups = await this.http
          .get<Group[]>(`/api/v1/groups`, {withCredentials: true})
          .toPromise();
      } catch (e) {
        this._groups = [];
      }
    }

    return this._groups;
  }

  getGroups(): Group[] {
    return this._groups;
  }

  findGroupById(groupId: string): Group | undefined {
    const matching = this._groups.filter(group => group.id == groupId);
    if (matching.length !== 1) return;
    else return matching[0];
  }
}
