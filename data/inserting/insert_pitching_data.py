# fielding
import logging
import mysql.connector
import json
from datetime import datetime
from dotenv import load_dotenv
import os

load_dotenv()
# 로그 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s: %(message)s')
log_file_handler = logging.FileHandler('logs/insert_pitching.log')
log_file_handler.setLevel(logging.INFO)
log_formatter = logging.Formatter('%(asctime)s - %(levelname)s: %(message)s')
log_file_handler.setFormatter(log_formatter)
logger = logging.getLogger()
logger.addHandler(log_file_handler)

db_user = os.getenv("DB_USER")
db_password = os.getenv("DB_PASSWORD")
db_host = os.getenv("DB_HOST")
print(db_host)
# MariaDB 연결 설정
config = {
    'user': db_user,
    'password': db_password,
    'host': db_host,
    "port": 3306,
    "database": "S09P22A301",  
}

# 연결 생성
connection = mysql.connector.connect(**config)

# 커서 생성
cursor = connection.cursor()

folder_path = 'players_stat'
for filename in os.listdir(folder_path):
    if filename.endswith('.json'):
        try:
            with open(os.path.join(folder_path, filename), 'r') as json_file:
                data = json.load(json_file)
                player_id = data.get("id")
                stats= data.get("stats")
                if(stats == None):
                    continue

                for stat in stats:
                    if stat.get("group").get("displayName") == "pitching":
                        if stat.get("type").get("displayName") == "career":
                            # print("fielding career")
                            season = -1

                            career_stat = stat.get("splits")[0].get("stat")
                            games_played = career_stat.get("gamesPitched")
                            
                            innings_played  = float(career_stat.get("inningsPitched"))
                            win  = career_stat.get("wins")
                            lose  = career_stat.get("losses")
                            era  = season_stat.get("era")
                            if era == "-.--": era = 0
                            era = float(era)
                            save  = season_stat.get("saves")
                            whip  = season_stat.get("whip")
                            if whip == "-.--": whip = 0
                            whip = float(whip)
                            
                            baseOnballs = int(career_stat.get("baseOnBalls"))
                            baseOnballs =  1 if baseOnballs == 0 else baseOnballs
                            kbb  = int(career_stat.get("strikeOuts"))/baseOnballs
                            blownsave  = career_stat.get("blownSaves")

                            # 데이터베이스에 데이터 삽입
                            cursor.execute('''
                                            INSERT INTO `Pitching` (player_id, season, games_played, innings_played, win, lose, era, save, whip, kbb, blownsave)
                                            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                                            ''', (player_id, season, games_played, innings_played, win, lose, era, save, whip, kbb, blownsave))
                

                        elif stat.get("type").get("displayName") == "yearByYear" :
                            # print("fielding YBY")
                            splits = stat.get("splits")
                            for split in splits:
                                season = split.get("season")
                                season_stat = split.get("stat")
                                games_played = season_stat.get("gamesPitched")
                            
                                innings_played  = float(season_stat.get("inningsPitched"))
                                win  = season_stat.get("wins")
                                lose  = season_stat.get("losses")
                                era  = season_stat.get("era")
                                if era == "-.--": era = 0
                                era = float(era)
                                save  = season_stat.get("saves")
                                whip  = season_stat.get("whip")
                                if whip == "-.--": whip = 0
                                whip = float(whip)
                                baseOnballs = int(career_stat.get("baseOnBalls"))
                                baseOnballs =  1 if baseOnballs == 0 else baseOnballs
                                kbb  = int(career_stat.get("strikeOuts"))/baseOnballs
                                blownsave  = season_stat.get("blownSaves")
                                
                                cursor.execute('''
                                            INSERT INTO `Pitching` (player_id, season, games_played, innings_played, win, lose, era, save, whip, kbb, blownsave)
                                            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                                            ''', (player_id, season, games_played, innings_played, win, lose, era, save, whip, kbb, blownsave))
                        connection.commit()
                            # INSERT 작업 로그를 파일에 저장
                        logging.info(f"데이터 삽입 성공: {filename}")
        
        except mysql.connector.IntegrityError as e :
            continue
        except Exception as e2:
            # 오류 발생 시 로그를 파일에 저장
            logging.error(f"{filename} 에러 발생: {str(e2)}")
# 연결 종료
cursor.close()
connection.close()