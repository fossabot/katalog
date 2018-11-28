export class Namespace {
  id: string;
  namespace: string;
}

export class Schema {
  id: string;
  createdOn: string;
  namespace: Namespace;
  schema: string;
}

export class Version {
  id: string;
  createdOn: string;
  schemaId: string;
  version: string;
  major: number;
  stable: boolean;
  current: boolean;
}
