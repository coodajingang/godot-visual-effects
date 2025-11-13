extends BaseModule
class_name SocialKit

signal leaderboard_updated(board: Dictionary)
signal message_sent(channel: String, payload: Dictionary)
signal mission_board_updated(missions: Array)

var friends: Array = []
var blacklist: Array = []
var pending_invites: Array = []

func _on_setup() -> void:
    event_bus.subscribe("auth.login_succeeded", Callable(self, "_on_login"))

func add_friend(player_id: String) -> void:
    if player_id in friends:
        return
    friends.append(player_id)
    event_bus.publish("social.friend_added", {"player_id": player_id})

func remove_friend(player_id: String) -> void:
    friends = friends.filter(func(id): return id != player_id)
    event_bus.publish("social.friend_removed", {"player_id": player_id})

func block_player(player_id: String) -> void:
    if player_id in blacklist:
        return
    blacklist.append(player_id)
    event_bus.publish("social.player_blocked", {"player_id": player_id})

func unblock_player(player_id: String) -> void:
    blacklist = blacklist.filter(func(id): return id != player_id)
    event_bus.publish("social.player_unblocked", {"player_id": player_id})

func send_chat_message(channel: String, message: String) -> Dictionary:
    var payload := {
        "channel": channel,
        "message": message,
        "sender": game_app.get_auth().get_profile().get("nickname", ""),
        "timestamp": Time.get_datetime_string_from_system()
    }
    event_bus.publish("social.chat_message", payload)
    message_sent.emit(channel, payload)
    return payload

func fetch_leaderboard(board_id: String) -> Dictionary:
    var board: Dictionary = await backend_adapter.fetch_leaderboard(board_id)
    leaderboard_updated.emit(board)
    event_bus.publish("social.leaderboard_updated", board)
    return board

func fetch_missions() -> Array:
    var missions: Array = await backend_adapter.fetch_missions()
    mission_board_updated.emit(missions)
    event_bus.publish("social.missions_updated", {"missions": missions})
    return missions

func _on_login(_payload: Dictionary) -> void:
    async func preload():
        await fetch_leaderboard("global_power")
        await fetch_missions()
    preload()
