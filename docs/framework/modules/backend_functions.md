# Cloud Function Templates

The framework expects a few backend functions to exist when you wire Supabase or Firebase. The exact implementation will depend on your infrastructure, but the templates below illustrate the required inputs/outputs.

## Login validation

Used to verify OAuth tokens or device credentials before issuing a session.

```ts
// TypeScript pseudo code
export async function validateLogin(request: Request) {
  const { provider, accessToken } = await request.json();
  const isValid = await verifyToken(provider, accessToken);
  if (!isValid) {
    return new Response(JSON.stringify({ success: false, error: 'INVALID_TOKEN' }), { status: 401 });
  }
  const profile = await upsertProfileFromProvider(accessToken);
  return Response.json({ success: true, profile });
}
```

## Cloud save synchronization

Validates and merges client-side saves to avoid tampering.

```ts
export async function syncSave(request: Request) {
  const { userId, snapshot } = await request.json();
  const serverSnapshot = await loadSnapshot(userId);
  const merged = resolveConflicts(serverSnapshot, snapshot);
  await saveSnapshot(userId, merged);
  return Response.json({ success: true, snapshot: merged });
}
```

## Leaderboard update

Recalculates leaderboard positions with server-side validation.

```ts
export async function updateLeaderboard(request: Request) {
  const { userId, boardId, score } = await request.json();
  const sanitized = clampScore(score);
  await insertOrUpdateScore(boardId, userId, sanitized);
  const topRanks = await loadTopEntries(boardId, 100);
  return Response.json({ success: true, entries: topRanks });
}
```

These functions can be deployed as Supabase Edge Functions or Firebase Cloud Functions. The framework-side adapters simply need an endpoint to call and a normalized response payload (`success`, `error`, `entries`, etc.).
