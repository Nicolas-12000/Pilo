# AWS demo (eu-west-1)

Provisioning and CD for the public demo. Profile: `NicolasAlejandroGarcia` (or `export AWS_PROFILE=...`).

## First time

```bash
chmod +x scripts/aws/*.sh pilo-worker/scripts/build.sh
./scripts/aws/provision-demo.sh
./scripts/aws/verify-deployment.sh
```

Outputs (gitignored): `scripts/aws/out/` — `env.aws`, `secrets.env`, `outputs.json`, `pilo-ec2.pem`.

Network layout (RDS private, API on EC2, Amplify → `/api` rewrite): `NETWORK-SECURITY.md`.

## Deploy

| Trigger | What |
|--------|------|
| Push to `main` touching `pilo-backend/**` | GitHub Actions → SSM → `pilo-update.sh` on EC2 |
| Push touching `pilo-worker/**` | Lambda zip update |
| Amplify | Frontend on every `main` push |

CD needs GitHub OIDC role `pilo-github-actions`. If assume-role fails: `./scripts/aws/fix-github-oidc.sh` and re-run **deploy-aws**.

After new Amplify URL:

```bash
./scripts/aws/sync-after-amplify.sh https://XXXX.amplifyapp.com
```

## Gemini on Lambda

```bash
./scripts/aws/set-lambda-gemini.sh   # reads GEMINI_API_KEY from .env
```

## Day to day

```bash
./scripts/aws/start-demo.sh   # EC2 + RDS
./scripts/aws/stop-demo.sh    # save cost
```

Catalog seed: `PILO_SEED_ENABLED=true` on EC2 (first boot). Then set `false` and restart compose.

Optional IaC mirror: `infra/terraform/pilo-demo/` (do not `apply` on top of CLI provision without import).

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Health not UP | EC2 still building; `curl` EIP `/actuator/health` |
| deploy-aws git dubious ownership | `pilo-update.sh` on server must be current (runs git as `ubuntu`) |
| Login CORS | `sync-after-amplify.sh` with exact HTTPS URL |
| OIDC denied | `fix-github-oidc.sh` + GitHub Actions workflow permissions |
