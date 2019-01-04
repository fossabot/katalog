export interface LoginOptions {
  type: "FORM" | "OAUTH2",
  oauth2ProviderName: string,
  oauth2LoginUrl: string
}
