# player__id별
import statsapi
import json

group="[hitting,pitching,fielding]"
type="[career, yearByYear]"

with open("all_players.json", "r") as f2:
    all_players_id = json.load(f2)

players_stat = []
for player in all_players_id:
    player_id = player["id"]
    params = {
    "personId": player_id,
        "hydrate": "stats(group="
        + group
        + ",type="
        + type
        + ",sportId="
        + str(1)
        + ")",
}
    player_stat = statsapi.get("person",params)
    with open(f"players_stat/players_stat_{player_id}.json", "w") as f:
        f.write(json.dumps(player_stat))