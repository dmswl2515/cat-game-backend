# Oracle Cloud 배포 가이드

Oracle Cloud 영구 무료 티어 ARM 인스턴스에 비용 0원으로 배포하는 가이드.

---

## 1. Oracle Cloud 계정 생성

1. [https://cloud.oracle.com](https://cloud.oracle.com) 접속
2. 무료 계정 가입 (신용카드 필요하지만 과금되지 않음)
3. 리전 선택 (서울: `ap-seoul-1` 권장)

---

## 2. VM 인스턴스 생성

Oracle Cloud Console에서 직접 수행:

1. **Compute → Instances → Create Instance**
2. 설정:
   - **Name:** `cat-game-backend`
   - **Image:** Ubuntu 22.04 (aarch64)
   - **Shape:** VM.Standard.A1.Flex
     - OCPU: **4**
     - Memory: **24 GB**
   - **Network:** 기본 VCN 사용 또는 새로 생성
   - **SSH Key:** 키 페어 생성 또는 기존 공개키 업로드
3. **Create** 클릭

> VM.Standard.A1.Flex는 영구 무료 티어에 포함 (최대 4 OCPU, 24GB RAM).

---

## 3. 네트워크 설정 (포트 80 열기)

1. **Networking → Virtual Cloud Networks → VCN 선택**
2. **Security Lists → Default Security List**
3. **Add Ingress Rules:**
   - Source CIDR: `0.0.0.0/0`
   - IP Protocol: TCP
   - Destination Port Range: `80`

---

## 4. SSH 접속

```bash
ssh -i <private-key-path> ubuntu@<public-ip>
```

---

## 5. Docker 설치

```bash
# Docker 설치
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 현재 유저를 docker 그룹에 추가 (재접속 필요)
sudo usermod -aG docker $USER
```

재접속 후 확인:
```bash
docker --version
docker compose version
```

---

## 6. 배포

### 6.1 디렉토리 및 파일 생성

```bash
mkdir -p ~/cat-game && cd ~/cat-game
```

`docker-compose.prod.yml` 파일을 서버에 복사하거나 직접 생성:
```bash
scp -i <private-key-path> docker-compose.prod.yml ubuntu@<public-ip>:~/cat-game/
```

### 6.2 환경변수 설정

```bash
cat > ~/cat-game/.env << 'EOF'
DB_USERNAME=catgame
DB_PASSWORD=여기에-강력한-비밀번호-입력
JWT_SECRET=여기에-Base64-인코딩된-256비트-이상-시크릿-입력
GITHUB_REPOSITORY=your-username/cat-game-backend
EOF
```

> JWT_SECRET 생성 예시:
> ```bash
> openssl rand -base64 64
> ```

### 6.3 GHCR 로그인 (Private 레포지토리인 경우)

```bash
echo <GITHUB_PAT> | docker login ghcr.io -u <GITHUB_USERNAME> --password-stdin
```

### 6.4 서비스 시작

```bash
cd ~/cat-game
docker compose -f docker-compose.prod.yml up -d
```

---

## 7. 헬스체크

```bash
# 컨테이너 상태 확인
docker compose -f docker-compose.prod.yml ps

# Actuator health
curl http://localhost/actuator/health

# Swagger UI (브라우저에서)
# http://<public-ip>/swagger-ui/index.html
```

---

## 8. 운영

### 로그 확인

```bash
docker compose -f docker-compose.prod.yml logs -f app
docker compose -f docker-compose.prod.yml logs -f postgres
```

### 업데이트 배포

```bash
cd ~/cat-game
docker compose -f docker-compose.prod.yml pull app
docker compose -f docker-compose.prod.yml up -d app
```

### 서비스 중지

```bash
docker compose -f docker-compose.prod.yml down
```

### DB 백업

```bash
docker exec cat-game-db-prod pg_dump -U catgame catgame > backup_$(date +%Y%m%d).sql
```

### DB 복원

```bash
cat backup_YYYYMMDD.sql | docker exec -i cat-game-db-prod psql -U catgame catgame
```

---

## 9. Ubuntu 방화벽 설정 (iptables)

Oracle Cloud Ubuntu 인스턴스는 기본적으로 iptables 규칙이 있을 수 있음:

```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo netfilter-persistent save
```

---

## 10. 아키텍처

```
[Client] → :80 → [Docker: app (Spring Boot :8567)] → [Docker: postgres :5432]
                         ↑
                   GHCR 이미지 (linux/arm64)
```
