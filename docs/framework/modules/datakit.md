# DataKit

DataKit manages local caches (profile, inventory, progress) and synchronizes them with the cloud. It acts as the broker between gameplay systems and persistent storage, abstracting the underlying backend provider.

## Responsibilities

- Cache the profile returned by AuthKit.
- Fetch and merge cloud save snapshots.
- Push local changes back to the server.
- Publish synchronization lifecycle events for UI feedback.

## API surface

```gdscript
GameApp.get_data().cache_profile(profile_dict)
var profile := GameApp.get_data().get_profile()
var snapshot := await GameApp.get_data().synchronize_cloud_save()
var local_save := GameApp.get_data().get_cloud_save()
GameApp.get_data().update_cloud_save({"inventory": updated_inventory})
```

`update_cloud_save` performs a shallow merge; use nested dictionaries when updating specific sections.

## EventBus topics

| Event | Payload |
|-------|---------|
| `datakit.profile_cached` | `{ profile }`
| `datakit.cloud_sync_started` | `{}`
| `datakit.cloud_synced` | `{ snapshot }`

## Signals

- `profile_cached(profile: Dictionary)`
- `cloud_sync_started()`
- `cloud_sync_completed(snapshot: Dictionary)`

## Sync strategy

1. Fetch the remote snapshot using `backend_adapter.fetch_cloud_save()`.
2. Merge it with local overrides (`_merge_save`).
3. Push the merged result from the client with `backend_adapter.push_cloud_save()`.
4. Emit `datakit.cloud_synced` for downstream modules (UI, analytics).

You can swap `_merge_save` with a conflict-resolution strategy (server-authoritative, CRDT, last-write-wins) depending on your game design.
