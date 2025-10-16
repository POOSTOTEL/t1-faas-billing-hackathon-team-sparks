# t1-faas-billing-hackathon-team-sparks

## 🚀 Быстрый старт (Windows + WSL2)

1. Убедитесь, что в Docker Desktop включена интеграция с WSL2.
2. Откройте **Ubuntu** из Windows Terminal.
3. Выполните:
   ```bash
   git clone https://github.com/.../t1-faas-billing-hackathon-team-sparks.git
   cd t1-faas-billing-hackathon-team-sparks
   chmod +x build.sh infra/*.sh
   ./build.sh
   cd infra && ./setup.sh