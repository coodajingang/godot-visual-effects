# SocialKit

SocialKit centralizes player-to-player interactions: friends, blocking, chat, leaderboard access, and mission/achievement tracking.

## Responsibilities

- Maintain lightweight friend/blacklist caches on the client.
- Relay chat messages through the event bus for UI rendering.
- Fetch leaderboard snapshots and mission boards from the backend adapter.
- Emit events so other systems (notifications, UI) can react to social updates.

## API surface

```gdscript
GameApp.get_social().add_friend("player_123")
GameApp.get_social().block_player("toxic_guy")
var board := await GameApp.get_social().fetch_leaderboard("global_power")
var missions := await GameApp.get_social().fetch_missions()
GameApp.get_social().send_chat_message("world", "Hello explorers!")
```

## EventBus topics

| Event | Payload |
|-------|---------|
| `social.friend_added` | `{ player_id }`
| `social.player_blocked` | `{ player_id }`
| `social.chat_message` | `{ channel, message, sender, timestamp }`
| `social.leaderboard_updated` | `{ board_id, entries }`
| `social.missions_updated` | `{ missions }`

## Signals

- `leaderboard_updated(board: Dictionary)`
- `message_sent(channel: String, payload: Dictionary)`
- `mission_board_updated(missions: Array)`

## Extending SocialKit

- **Guild support** – add methods like `create_guild`, `invite_member`, wiring them to backend endpoints.
- **Realtime chat** – connect `NetKit` WebSocket streams and publish `social.chat_message` when new payloads arrive.
- **Presence** – integrate with backend presence tracking and raise events such as `social.friend_online`.
