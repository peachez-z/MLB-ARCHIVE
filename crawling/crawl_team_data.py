import statsapi
import json

with open("../team_ids/team_ids2023.json", "r") as f:
    teams = json.load(f)

for team in teams:
    team_id = team["id"]

    try:
        teamInfo = statsapi.get("team", {"teamId" : team_id})["teams"][0]
        print(teamInfo)
    except:
        print(f"threre is no team id: {team_id}")


    team_info =dict()
    team_info["team_id"] = team_id
    team_info["team_name"] = teamInfo["name"]
    team_info["created_year"] = teamInfo["firstYearOfPlay"]
    team_info["team_logo"] = ""
    team_info["team_location"] = teamInfo["locationName"]
    team_info_json = json.dumps(team_info)
    try:
        with open(f"../team_data/team_data_{team_id}.json", "w") as of:
            of.write(team_info_json)
        
    except:
        print(f"fail to create json file : id{team_id}")
    
    