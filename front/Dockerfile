# Node.js 기반 이미지 사용
FROM node:18-alpine

WORKDIR /app

# package.json과 package-lock.json 복사
COPY package*.json ./

# 의존성 설치
RUN npm install

# 소스 코드 복사
COPY . .

# 포트 노출
EXPOSE 3000

# 개발 서버 실행 (환경 변수로 호스트 바인딩 설정)
ENV HOST=0.0.0.0
CMD ["npm", "start"]


