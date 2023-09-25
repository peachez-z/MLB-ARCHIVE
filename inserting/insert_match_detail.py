import logging
import mysql.connector
import json
from datetime import datetime
from dotenv import load_dotenv
import os

def setup_logger(logger_name, log_file):
    logger = logging.getLogger(name=logger_name)
    logger.setLevel(logging.INFO)
    formatter = logging.Formatter('|%(asctime)s||%(name)s||%(levelname)s|\n%(message)s',
                                  datefmt='%Y-%m-%d %H:%M:%S')

    if logger.hasHandlers():
        logger.handlers.clear()

    file_handler = logging.FileHandler(log_file)
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)

    return logger


def insert_math_detail(cursor, match_id, linescore, boxscore):
    linescore_dump = json.dumps(linescore)
    boxscore_dump = json.dumps(boxscore)
    cursor.execute('''
        INSERT INTO `match_detail` (match_id,linescore, boxscore)
        VALUES (%s, %s, %s)
    ''', (match_id, linescore_dump, boxscore_dump))
    connection.commit()

load_dotenv()
db_user = os.getenv("DB_USER")
db_password = os.getenv("DB_PASSWORD")
db_host = os.getenv("DB_HOST")

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

# 로그 설정
log_file_path = 'logs/insert_match_detail.log'
logger = setup_logger('insert_match_detail', log_file_path)

try:
    for year in range(2013, 2024):
        linescore_path = "linescores_new/"+str(year)
        boxscore_path = "boxscores/"+str(year)
        for file_name in os.listdir(linescore_path):
            file_path = os.path.join(linescore_path, file_name)

            try:
                with open(file_path, "r") as file:
                    linescore = json.load(file)
                game_id = linescore["game_id"]
                boxscore_file_path = boxscore_path+"/boxscore_"+str(game_id)+".json"

                with open(boxscore_file_path, "r") as bs:
                    boxscore = json.load(bs)

                insert_math_detail(cursor, game_id, linescore, boxscore)
                logger.info(f"삽입성공: {file_path} {game_id}")
            except Exception as e:
                error_message = f'오류 발생: {file_path} 에러 메시지: {str(e)}'
                logger.error(error_message)

except Exception as e:
    error_message = f'오류 발생: {str(e)}'
    logger.error(error_message)

finally:
    # 연결 종료 (try 또는 except에서 오류가 발생하더라도 항상 실행)
    cursor.close()
    connection.close()
