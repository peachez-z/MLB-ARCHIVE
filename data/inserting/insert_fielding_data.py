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
log_file_handler = logging.FileHandler('logs/insert_fielding.log')
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
                    if stat.get("group").get("displayName") == "fielding":
                        if stat.get("type").get("displayName") == "career":
                            # print("fielding career")
                            season = -1
                            error = stat.get("splits")[0].get("stat").get("errors")
                            assist = stat.get("splits")[0].get("stat").get("assists")
                            putout = stat.get("splits")[0].get("stat").get("putOuts")
                            games_played = stat.get("splits")[0].get("stat").get("gamesPlayed")
                            if split.get("stat").get("position") != None:
                                position = split.get("stat").get("position").get("name").upper()
                            else:
                                position = data.get("primaryPosition").get("name").upper()
                            position = position.replace(" ", "_")
                            position = position.replace("-", "_")
                            # 데이터베이스에 데이터 삽입
                            cursor.execute('''
                                            INSERT INTO `Fielding` (player_id, season, error, assist, putout, games_played, position)
                                            VALUES (%s, %s, %s, %s, %s, %s, %s)
                                            ''', (player_id, season, error, assist, putout, games_played, position))
                

                        elif stat.get("type").get("displayName") == "yearByYear" :
                            # print("fielding YBY")
                            splits = stat.get("splits")
                            for split in splits:
                                season = split.get("season")
                                error = split.get("stat").get("errors")
                                assist = split.get("stat").get("assists")
                                putout = split.get("stat").get("putOuts")
                                games_played = split.get("stat").get("gamesPlayed")
                                if split.get("stat").get("position") != None:
                                    position = split.get("stat").get("position").get("name").upper()
                                else:
                                    position = data.get("primaryPosition").get("name").upper()
                                position = position.replace(" ", "_")
                                position = position.replace("-", "_")
                                cursor.execute('''
                                                INSERT INTO `Fielding` (player_id, season, error, assist, putout, games_played, position)
                                                VALUES (%s, %s, %s, %s, %s, %s, %s)
                                                ''', (player_id, season, error, assist, putout, games_played, position))
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