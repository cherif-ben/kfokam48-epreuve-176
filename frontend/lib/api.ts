import { ErreurApi } from "@/shared/types";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    public readonly code: string,
    public readonly message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export interface FetchOptions extends RequestInit {
  params?: Record<string, string | number | boolean | undefined | null>;
}

function buildUrl(path: string, params?: FetchOptions["params"]): string {
  const queryString = new URLSearchParams();
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        queryString.append(key, String(value));
      }
    });
  }
  const query = queryString.toString();
  return `${API_URL}${path}${query ? `?${query}` : ""}`;
}

export async function apiFetch<T>(
  path: string,
  options: FetchOptions = {},
): Promise<T> {
  const { params, ...init } = options;
  const url = buildUrl(path, params);

  const response = await fetch(url, {
    ...init,
  });

  if (!response.ok) {
    let body: ErreurApi | { message?: string };
    try {
      body = await response.json();
    } catch {
      body = { message: response.statusText };
    }

    const err: ErreurApi =
      "code" in body && "message" in body
        ? { code: body.code, message: body.message }
        : { code: `HTTP_${response.status}`, message: body.message || response.statusText };

    throw new ApiError(err.code, err.message, response.status);
  }

  if (response.status === 204 || response.headers.get("content-length") === "0") {
    return undefined as T;
  }

  return response.json() as T;
}
