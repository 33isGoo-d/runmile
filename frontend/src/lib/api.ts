export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "/api/v1";

export type ApiError = { code: string; message: string };

export class RunMileApiError extends Error {
  constructor(public readonly status: number, public readonly code: string, message: string) {
    super(message);
  }
}

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...options?.headers },
    cache: "no-store"
  });
  if (!response.ok) {
    const fallback = "요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.";
    const error = (await response.json().catch(() => null)) as ApiError | null;
    throw new RunMileApiError(response.status, error?.code ?? "REQUEST_FAILED", error?.message ?? fallback);
  }
  return response.json() as Promise<T>;
}

export function getApi<T>(path: string) { return apiFetch<T>(path); }
export function postApi<T>(path: string, body: unknown) {
  return apiFetch<T>(path, { method: "POST", body: JSON.stringify(body) });
}

