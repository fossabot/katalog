export class Namespace {
  id: string;
  namespace: string;
}

export class Schema {
  id: string;
  namespaceId: string;
  schema: string;
}

export class Version {
  id: string;
  schemaId: string;
  version: string;
  major: number;
  stable: boolean;
  current: boolean;
}
