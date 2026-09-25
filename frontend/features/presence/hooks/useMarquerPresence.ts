import { useState, useCallback } from "react";
import { presenceApi } from "../api/presenceApi";
import type { Presence, ApiError } from "@/shared/types";

interface UseMarquerPresenceResult {
  presence: Presence | null;
  loading: boolean;
  error: ApiError | null;
  marquerPresence: (code: string, etudiantId: number) => Promise<void>;
  reset: () => void;
}

export function useMarquerPresence(): UseMarquerPresenceResult {
  const [presence, setPresence] = useState<Presence | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  const marquerPresence = useCallback(
    async (code: string, etudiantId: number) => {
      setLoading(true);
      setError(null);
      try {
        const result = await presenceApi.marquerPresence(code, etudiantId);
        setPresence(result);
      } catch (e) {
        if (e instanceof Error && "code" in e && "status" in e) {
          setError(e as ApiError);
        } else {
          setError({ code: "ERREUR_INCONNU", message: (e as Error).message, status: 0 });
        }
      } finally {
        setLoading(false);
      }
    },
    [],
  );

  const reset = useCallback(() => {
    setPresence(null);
    setError(null);
  }, []);

  return { presence, loading, error, marquerPresence, reset };
}
